package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.parameter.C1G2Read;
import havis.llrpservice.data.message.parameter.Parameter;
import havis.middleware.ale.base.KeyValuePair;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.Port;
import havis.middleware.ale.base.operation.port.PortObservation;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.port.result.Result.State;
import havis.middleware.ale.base.operation.tag.Operation;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.FaultResult;
import havis.middleware.ale.base.operation.tag.result.KillResult;
import havis.middleware.ale.base.operation.tag.result.LockResult;
import havis.middleware.ale.base.operation.tag.result.PasswordResult;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.base.operation.tag.result.WriteResult;
import havis.middleware.ale.exit.Exits.Reader;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.Capability;
import havis.middleware.ale.reader.ReaderConnector;
import havis.middleware.ale.reader.ReaderUtils;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.llrp.ALLRPManager.PortEventArgs;
import havis.middleware.reader.llrp.ALLRPManager.TagEventArgs;
import havis.middleware.reader.llrp.LLRPInventoryOperation.UserReadMode;
import havis.middleware.reader.llrp.client.LLRPConnection;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;
import havis.middleware.utils.data.Calculator;
import havis.util.monitor.Capabilities;
import havis.util.monitor.CapabilityType;
import havis.util.monitor.Configuration;
import havis.util.monitor.ConfigurationType;
import havis.util.monitor.ConnectionError;
import havis.util.monitor.DeviceCapabilities;
import havis.util.monitor.ReaderEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/*****************************************************************************/
/*    FILE  NAME               	: ALLRPReaderConnector                       */
/*    HOLDER OF RIGHTS OF USE	: HARTING KGaA                               */
/*    DATE OF FIRST RELEASE 	: ??? 2011                                   */
/*    AUTHOR                	: IT-AS RFID SW TEAM                         */
/*    DESCRIPTION           	: This file contains base functions to       */
/*                            	  communicate with all LLRP Readers          */
/*---------------------------------------------------------------------------*/
/* CHANGE HISTORY :                                                          */
/*---------------------------------------------------------------------------*/
/* VERSION    DATE/                   DESCRIPTION OF CHANGE/                 */
/*            MODIFIED BY             FAULT REPORT NO                        */
/*---------------------------------------------------------------------------*/
/* 1.5.0.0    ??? 2011                Initial Create.                        */
/*            IT-AS RFID SW TEAM                                             */
/*****************************************************************************/

/**
 * Abstract class that provides base functionality for communicating with all
 * LLRP readers.
 * 
 */
public abstract class ALLRPReaderConnector implements ReaderConnector, ErrorHandler {
	/**
	 * Indicates if reader connector is initialize.
	 */
	private boolean isInitialize = false;
	/**
	 * Indicates if reader connector is connected.
	 */
	private boolean isConnected = false;

	/**
	 * Object to synchronize access to tag operation list.
	 */
	private Object syncTagOperationList = new Object();

	/**
	 * The List of defined tag operations.
	 */
	private Map<Long, TagOperation> tagOperationList = new HashMap<Long, TagOperation>();

	/**
	 * Object to sychronize access to tag observer list.
	 */
	private Object syncTagObserverList = new Object();

	/**
	 * The List of active tag operations.
	 */
	private List<Long> tagObserverList = new ArrayList<Long>();

	/**
	 * Object to synchornize acces to execute tag operation.
	 */
	private Object syncExecuteTagOperation = new Object();

	private Semaphore executeEvent = new Semaphore(1);
	/**
	 * The current execute tag operation
	 */
	private Map.Entry<Long, TagOperation> executeTagOperation = new KeyValuePair<Long, TagOperation>();

	/**
	 * Object to synchronize access to port observation list.
	 */
	private Object syncPortObservationList = new Object();

	/**
	 * The List of defined port observations.
	 */
	private Map<Long, PortObservation> portObservationList = new HashMap<Long, PortObservation>();

	/**
	 * Object to sychronize access to port observer list.
	 */
	private Object syncPortObserverList = new Object();

	/**
	 * The List of active port observations.
	 */
	private List<Long> portObserverList = new ArrayList<Long>();

	/**
	 * Object to synchornize acces to execute port operation.
	 */
	private Object syncExecutePortOperation = new Object();

	private Semaphore executePortEvent = new Semaphore(1);

	/**
	 * The current execute port operation
	 */
	private Map.Entry<Long, PortOperation> executePortOperation;

	private ConnectionError lastConError;

	private LLRPEventHandler.LLRPEvent<PortEventArgs> notifyExecutePortLlrpEvent = new LLRPEventHandler.LLRPEvent<PortEventArgs>() {
		@Override
		public void fire(Object sender, PortEventArgs eventArgs) {
			notifyExecutePortEvent(sender, eventArgs);
		}
	};

	private LLRPEventHandler.LLRPEvent<PortEventArgs> notifyObservationPortLlrpEvent = new LLRPEventHandler.LLRPEvent<PortEventArgs>() {
		@Override
		public void fire(Object sender, PortEventArgs eventArgs) {
			notifyObservationPortEvent(sender, eventArgs);
		}
	};

	private LLRPEventHandler.LLRPEvent<TagEventArgs> notifyExecuteTagLlrpEvent = new LLRPEventHandler.LLRPEvent<TagEventArgs>() {
		@Override
		public void fire(Object sender, TagEventArgs eventArgs) {
			notifyExecuteTagEvent(sender, eventArgs);
		}
	};

	LLRPEventHandler.LLRPEvent<TagEventArgs> notifyInventoryTagLlrpEvent = new LLRPEventHandler.LLRPEvent<TagEventArgs>() {
		@Override
		public void fire(Object sender, TagEventArgs eventArgs) {
			notifyInventoryTagEvent(sender, eventArgs);
		}
	};

	LLRPEventHandler.LLRPEvent<EventObject> notifyConnectionLostLlrpEvent = new LLRPEventHandler.LLRPEvent<EventObject>() {
		@Override
		public void fire(Object sender, EventObject eventArgs) {
			notifyConnectionLost(sender, eventArgs);
		}
	};

	/**
	 * Represents the llrp manager.
	 */
	protected ALLRPManager manager;
	/**
	 * Lock object to sync Reader access.
	 */
	protected Object syncManager = new Object();
	/**
	 * The client callback class.
	 */
	protected Callback clientCallback;
	/**
	 * The name of the reader type.
	 */
	protected String readerType;

	protected DeviceCapabilities deviceCapabilities = new DeviceCapabilities();

	/**
	 * Initializes a new instance of the
	 * {@link havis.middleware.reader.llrp.ALLRPReaderConnector} class.
	 */
	public ALLRPReaderConnector() {
	}

	/**
	 * Initializes a new instance of the
	 * {@link havis.middleware.reader.llrp.ALLRPReaderConnector} class.
	 * 
	 * @param callback
	 */
	public ALLRPReaderConnector(Callback callback) {
		this();
		this.clientCallback = callback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#setProperties(java.util.Map)
	 */
	@Override
	public void setProperties(Map<String, String> properties) throws ValidationException, ImplementationException {
		try {
			// Validate and set configuration and connection properties
			LLRPConnection connection = LLRPConnection.validateConnectorProperties(properties);
			this.manager.setReaderConfiguration(LLRPConfiguration.validateConfigurationProperties(properties));

			if (!this.isInitialize) {
				this.manager.setConnectorConfiguration(connection);
				this.isInitialize = true;
			} else if (!this.isConnected) {
				this.manager.setConnectorConfiguration(connection);
			} else if (this.manager.getConnectorConfiguration().equals(connection)) {
				this.manager.applyReaderConfig();
			} else {
				// Reconnect
				this.manager.setConnectorConfiguration(connection);
				this.disconnect();
				this.connect();
			}
		} catch (ValidationException e) {
			throw e;
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#getCapability(java.lang.String
	 * )
	 */
	@Override
	public String getCapability(String name) throws ValidationException {
		switch (name) {
		case Capability.LostEPCOnWrite:
			return "true";
		default:
			throw new ValidationException("Unkown capabilty name '" + name + "' for " + this.readerType + "!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#connect()
	 */
	@Override
	public void connect() throws ValidationException, ImplementationException {
		if (!this.isInitialize) {
			throw new ImplementationException(this.readerType + " ReaderConnector was not initialized!");
		}

		try {
			synchronized (this.syncManager) {
				// Connect to the LLRP Reader
				if (!(this.manager.connect())) {
					throw new ImplementationException("Unable to connect to " + this.readerType + "!" + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				this.isConnected = true;

				// Subscribe to Manager events
				this.manager.getNoDataReceivedEvent().add(notifyConnectionLostLlrpEvent);

				this.manager.getInventoryTagEvent().add(notifyInventoryTagLlrpEvent);
				this.manager.getExecuteTagEvent().add(notifyExecuteTagLlrpEvent);

				this.manager.getObservationPortEvent().add(notifyObservationPortLlrpEvent);
				this.manager.getExecutePortEvent().add(notifyExecutePortLlrpEvent);

				// Apply Configuration on the LLRP Reader
				this.manager.applyReaderConfig();

				// Initialite the Manager
				this.manager.initialize();

				// Assign Inventory Operation
				this.manager.assign(false);

				// If needed Start Inventory Operation
				if (this.tagObserverList.size() > 0) {
					this.manager.start(false);
				}

				notifyConnectionErrorResolved();
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			this.disconnect();
			notifyError(e);

			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			this.disconnect();
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#disconnect()
	 */
	@Override
	public void disconnect() throws ImplementationException {
		if (this.isConnected) {
			try {
				this.isConnected = false;

				synchronized (this.syncManager) {
					// Unsubscribe from Manager events
					this.manager.getExecutePortEvent().remove(notifyExecutePortLlrpEvent);
					this.manager.getObservationPortEvent().remove(notifyObservationPortLlrpEvent);
					this.manager.getExecuteTagEvent().remove(notifyExecuteTagLlrpEvent);
					this.manager.getInventoryTagEvent().remove(notifyInventoryTagLlrpEvent);
					this.manager.getNoDataReceivedEvent().remove(notifyConnectionLostLlrpEvent);

					// Disconnect from LLRP Reader
					this.manager.disconnect();
				}
			} catch (Exception e) {
				throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#defineTagOperation(long,
	 * havis.middleware.ale.base.operation.tag.TagOperation)
	 */
	@Override
	public void defineTagOperation(long id, TagOperation operation) throws ValidationException, ImplementationException {
		try {
			synchronized (this.syncTagOperationList) {
				// Manage Tag Operation List
				if (!this.tagOperationList.containsKey(Long.valueOf(id))) {
					this.tagOperationList.put(Long.valueOf(id), operation);
				} else {
					throw new ImplementationException("Tag operation with id '" + id + "' was already defined for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				// Set Inventory Operation to new values
				if (this.setInventoryOperation()) {
					synchronized (this.syncManager) {
						try {
							this.manager.updateInventory(this.inventoryOperation);
						} catch (Exception e) {
							// If exception rollback
							this.tagOperationList.remove(Long.valueOf(id));
							this.setInventoryOperation();
							throw e;
						}

					}
				}
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#undefineTagOperation(long)
	 */
	@Override
	public void undefineTagOperation(long id) throws ImplementationException {
		try {
			synchronized (this.syncTagOperationList) {
				// Manager Tag Operation List
				if (!this.tagOperationList.containsKey(Long.valueOf(id))) {
					throw new ImplementationException("Unkown tag operation ID '" + id + "' for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				if (this.tagObserverList.contains(Long.valueOf(id))) {
					this.disableTagOperation(id);
				}

				this.tagOperationList.remove(Long.valueOf(id));

				// Set Inventory Operation to new values
				if (this.setInventoryOperation()) {
					synchronized (this.syncManager) {
						try {
							this.manager.updateInventory(this.inventoryOperation);
						} catch (Exception e) {
							throw e;
						}
					}
				}
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#enableTagOperation(long)
	 */
	@Override
	public void enableTagOperation(long id) throws ImplementationException {
		try {
			synchronized (this.syncTagObserverList) {
				// Handle Tag Operation List
				if (this.tagOperationList.containsKey(Long.valueOf(id)))
					this.tagObserverList.add(Long.valueOf(id));
				else
					throw new ImplementationException("Unkown reader operation ID '" + id + "' for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");

				// Id needed Start Inventory Operation
				if (this.isConnected && this.tagObserverList.size() == 1) {
					synchronized (this.syncManager) {
						this.manager.start(false);
					}
				}
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#disableTagOperation(long)
	 */
	@Override
	public void disableTagOperation(long id) throws ImplementationException {
		try {
			synchronized (this.syncTagObserverList) {
				// Manage Tag Operation List
				if (this.tagObserverList.contains(Long.valueOf(id)))
					this.tagObserverList.remove(Long.valueOf(id));
				else
					throw new ImplementationException("Reader operation ID '" + id + "' was not active for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");

				// If needed Stop Inventory Operation
				if (this.isConnected && this.tagObserverList.size() == 0) {
					synchronized (this.syncManager) {
						this.manager.stop(false);
					}
				}
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#executeTagOperation(long,
	 * havis.middleware.ale.base.operation.tag.TagOperation)
	 */
	@Override
	public void executeTagOperation(long id, TagOperation operation) throws ValidationException, ImplementationException {
		try {
			synchronized (this.syncExecuteTagOperation) {
				if (!this.isConnected) {
					throw new ValidationException("ReaderConnector was not connected to " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				// if (this.executeTagOperation.Value != null)
				// throw new
				// ImplementationFaultException("ReaderConnector can handle only one execute tag operation at time ("
				// + this.Manager.ConnectorConfiguration.ToString() + ")!");

				this.executeEvent.acquire();

				synchronized (this.syncManager) {
					LLRPReturnContainerUtil<Map<Integer, Result>> response = this.manager.validateExecuteTagOperation(operation);
					boolean isTrue = response.isTrue();
					// Validate Execute Tag operation

					if (!isTrue) {
						Map<Integer, Result> errorList = response.getValue();

						// Handle Validation Failed
						for (Operation op : operation.getOperations()) {
							Integer operationId = Integer.valueOf(op.getId());
							if (errorList.containsKey(operationId)) {
								continue;
							}

							switch (op.getType()) {
							case KILL:
								errorList.put(operationId, new KillResult(ResultState.MISC_ERROR_TOTAL));
								break;
							case LOCK:
								errorList.put(operationId, new LockResult(ResultState.MISC_ERROR_TOTAL));
								break;
							case PASSWORD:
								errorList.put(operationId, new PasswordResult(ResultState.MISC_ERROR_TOTAL));
								break;
							case READ:
								errorList.put(operationId, new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]));
								break;
							case WRITE:
								errorList.put(operationId, new WriteResult(ResultState.MISC_ERROR_TOTAL, 0));
								break;
							default:
								break;
							}
						}
						// Notify Error Result

						Tag tag = new Tag((byte[]) null);
						tag.setResult(errorList);
						tag.setSighting(new Sighting("", (short) 0, 0, tag.getFirstTime()));

						this.clientCallback.notify(id, tag);

						if (executeEvent.availablePermits() == 0) {
							executeEvent.release();
						}

					} else {
						// Add Execute Operation to the Reader

						this.executeTagOperation = new KeyValuePair<Long, TagOperation>(Long.valueOf(id), operation);
						this.manager.addExecuteTagOperation(id, operation);
					}
				}
			}
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#abortTagOperation(long)
	 */
	@Override
	public void abortTagOperation(long id) throws ImplementationException {
		try {
			boolean isCurrent = false;
			synchronized (this.syncExecuteTagOperation) {
				if (this.executeTagOperation.getKey() != null && this.executeTagOperation.getKey().longValue() == id) {
					this.executeTagOperation = new KeyValuePair<Long, TagOperation>();
					isCurrent = true;
				}

				if (executeEvent.availablePermits() == 0) {
					executeEvent.release();
				}
			}
			synchronized (this.syncManager) {
				// Remove Execute Tag Operation form the Reader
				if (isCurrent) {
					this.manager.removeExecuteTagOperation(id);
				}
			}
		} catch (LLRPException e) {
			notifyError(e);
			throw new ImplementationException(e.getMessage() + " (" + this.manager.getConnectorConfiguration().toString() + ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#definePortObservation(long,
	 * havis.middleware.ale.base.operation.port.PortObservation)
	 */
	@Override
	public void definePortObservation(long id, PortObservation observation) throws ValidationException, ImplementationException {
		try {
			synchronized (this.syncPortObservationList) {
				// Manage Port Observation List
				if (!this.portObservationList.containsKey(Long.valueOf(id))) {
					this.portObservationList.put(Long.valueOf(id), observation);
				} else
					throw new ImplementationException("Port observation with id '" + id + "' was already defined for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
			}
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#undefinePortObservation(long)
	 */
	@Override
	public void undefinePortObservation(long id) throws ImplementationException {
		try {
			synchronized (this.syncPortObservationList) {
				// Manager Port Observation List
				if (!this.portObservationList.containsKey(Long.valueOf(id))) {
					throw new ImplementationException("Unkown port observation ID '" + id + "' for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				if (this.portObserverList.contains(Long.valueOf(id))) {
					this.disablePortObservation(id);
				}

				this.portObservationList.remove(Long.valueOf(id));
			}
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#enablePortObservation(long)
	 */
	@Override
	public void enablePortObservation(long id) throws ImplementationException {
		try {
			synchronized (this.syncPortObserverList) {
				// Handle port observation List
				if (this.portObservationList.containsKey(Long.valueOf(id))) {
					this.portObserverList.add(Long.valueOf(id));
				} else {
					throw new ImplementationException("Unkown port observation ID '" + id + "' for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}
			}
		} catch (ImplementationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#disablePortObservation(long)
	 */
	@Override
	public void disablePortObservation(long id) throws ImplementationException {
		try {
			synchronized (this.syncPortObserverList) {
				// Manage Port observation List
				if (this.portObserverList.contains(Long.valueOf(id))) {
					this.portObserverList.remove(Long.valueOf(id));
				} else {
					throw new ImplementationException("Port observation ID '" + id + "' was not active for " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}
			}
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#executePortOperation(long,
	 * havis.middleware.ale.base.operation.port.PortOperation)
	 */
	@Override
	public void executePortOperation(long id, PortOperation operation) throws ValidationException, ImplementationException {
		try {
			synchronized (this.syncExecutePortOperation) {
				if (!this.isConnected) {
					throw new ValidationException("ReaderConnector was not connected to " + this.readerType + " ("
							+ this.manager.getConnectorConfiguration().toString() + ")!");
				}

				// if (this.executePortOperation.Value != null)
				// throw new
				// ImplementationFaultException("ReaderConnector can handle only one execute port operation at time ("
				// + this.Manager.ConnectorConfiguration.ToString() + ")!");

				this.executePortEvent.acquire();

				synchronized (this.syncManager) {
					// Add Execute Operation to the Reader
					this.executePortOperation = new KeyValuePair<Long, PortOperation>(Long.valueOf(id), operation);
					this.manager.addExecutePortOperation(id, operation);
				}
			}
		} catch (ValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new ImplementationException(e.getMessage() + Arrays.toString(e.getStackTrace()) + " (" + this.manager.getConnectorConfiguration().toString()
					+ ")!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.middleware.ale.reader.ReaderConnector#getConfig()
	 */
	@Override
	public RCConfig getConfig() {
		synchronized (this.syncManager) {
			return this.manager.getReaderConfig();
		}
	}

	/**
	 * Current Inventory Operation
	 */
	private LLRPInventoryOperation inventoryOperation = new LLRPInventoryOperation();

	/**
	 * Method to set current Inventory Operation
	 * 
	 * @return Returns true if update on the reader is needed, false otherwise
	 */
	private boolean setInventoryOperation() {
		LLRPInventoryOperation inventoryOperation = new LLRPInventoryOperation();
		inventoryOperation.setTid(Tag.isExtended());

		boolean[] dataBlocks = new boolean[0];
		
		for (TagOperation tagOp : this.tagOperationList.values()) {
			for (Operation op : tagOp.getOperations()) {
				switch (op.getField().getBank()) {
				case 0:
					inventoryOperation.setReserved(true);
					break;
				case 1:
					inventoryOperation.setEpc(true);
					break;
				case 2:
					inventoryOperation.setTid(true);
					break;
				case 3:
					if (!inventoryOperation.isUser()) {
						inventoryOperation.setUser(UserReadMode.ON);
					}

					int lengthBit = op.getField().getLength();
					
					if (lengthBit > 0) { // prepare to read specified ranges

						int offsetBit = op.getField().getOffset();
						int offsetWord = offsetBit / 16;
						
						int ceiling = Calculator.size(lengthBit + offsetBit, 16);
						
						if (dataBlocks.length < ceiling) {
							dataBlocks = Arrays.copyOf(dataBlocks, ceiling);
						}
						
						for (int i = offsetWord; i < ceiling; i++) {
							dataBlocks[i] = true;
						}						
					} else {
						// force complete read
						inventoryOperation.setUser(UserReadMode.ON_COMPLETE);
					}

					break;
				default:
					break;
				}
			}
		}

		inventoryOperation.setUserDataBlocks(dataBlocks);

		if (!this.inventoryOperation.equals(inventoryOperation)) {
			this.inventoryOperation = inventoryOperation;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Static Method to Notify Error to Middleware
	 * 
	 * @param e
	 *            The Exception to notify
	 */
	@Override
	public void notifyError(Exception error) {
		this.clientCallback.notify(new Message(Reader.Controller.Error, error.getMessage(), error));
	}

	/**
	 * Method to notify Connection Lost event to the Middleware.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event
	 */
	private void notifyConnectionLost(Object sender, EventObject e) {
		this.clientCallback.notify(new Message(Reader.Controller.ConnectionLost, "Connection lost to " + this.manager.getConnectorConfiguration() + "!"));
		notifyConnectionError("Connection lost to " + this.manager.getConnectorConfiguration() + "!");
	}

	/**
	 * Method to notify inventory tag event to the middleware.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event
	 */
	private void notifyInventoryTagEvent(Object sender, TagEventArgs e) {
		// Get TID
		Result tid = e.getTag().getResult().get(Integer.valueOf(2));
		List<Parameter> opSpecList = e.getOpSpecList();

		if (Tag.isExtended() && (tid != null) && (tid instanceof ReadResult)) {
			e.getTag().setTid(((ReadResult) tid).getData());
		}

		int userBankLength = 0;
		// calculate the length of the user bank array
		for (Parameter parameter : opSpecList) {
			if (parameter instanceof C1G2Read) {
				C1G2Read spec = (C1G2Read) parameter;
				if (spec.getMemoryBank() == 3) { // user bank
					ReadResult result = (ReadResult) e.getTag().getResult().get(Integer.valueOf(spec.getOpSpecId()));
					if (result != null) {
						// offset plus length of actually read data
						int length = spec.getWordPointer() * 2 + (result.getData() != null ? result.getData().length : 0);
						userBankLength = Math.max(userBankLength, length);
					}
				}
			}
		}

		// transform to read result list
		ReadResult[] readResult = new ReadResult[4];
		byte[] userBankReadResult = new byte[userBankLength];
		ResultState userBankResultState = ResultState.SUCCESS;

		for (Map.Entry<Integer, Result> result : e.getTag().getResult().entrySet()) {

			ReadResult value = (ReadResult) result.getValue();

			if (result.getKey().intValue() < 3) {
				readResult[result.getKey().intValue()] = value;
			} else {
				byte[] data = value.getData();
				userBankResultState = value.getState();

				if (userBankResultState != ResultState.SUCCESS) {
					break;
				}

				// find spec for result
				C1G2Read c1g2Read = null;
				for (Parameter parameter : opSpecList) {
					if (parameter instanceof C1G2Read) {
						C1G2Read spec = (C1G2Read) parameter;
						if (spec.getOpSpecId() == result.getKey().intValue()) {
							c1g2Read = spec;
							break;
						}
					}
				}

				if (c1g2Read != null) {
					// copy read data into the result
					System.arraycopy(data, 0, userBankReadResult, c1g2Read.getWordPointer() * 2, data.length);
				}
			}
		}

		if (userBankReadResult.length > 0) {
			readResult[3] = new ReadResult(userBankResultState, userBankReadResult);
		}

		// Deliver Cloned Tag Object for each observer
		synchronized (this.syncTagObserverList) {
			for (Long id : this.tagObserverList) {
				TagOperation op = this.tagOperationList.get(id);

				if (op != null) {
					Map<Integer, Result> opResultList;
					if (op.getOperations() != null) {
						opResultList = ReaderUtils.toResult(readResult, op.getOperations());
					} else {
						opResultList = new HashMap<Integer, Result>();
					}

					Tag tag = e.getTag().clone();
					tag.setResult(opResultList);
					this.clientCallback.notify(id.longValue(), tag);
				}
			}
		}
	}

	/**
	 * Method to notify execute tag event to the middleware.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event
	 */
	private void notifyExecuteTagEvent(Object sender, TagEventArgs e) {
		synchronized (this.syncExecuteTagOperation) {
			if (this.executeTagOperation.getValue() != null) {
				Tag tag = e.getTag().clone();
				tag.setResult(new HashMap<Integer, Result>());

				// Create Result List with correct indices for middleware
				for (Map.Entry<Integer, Result> result : e.getTag().getResult().entrySet()) {
					Integer operationId = Integer.valueOf(this.executeTagOperation.getValue().getOperations().get(result.getKey().intValue()).getId());
					if (result.getValue() instanceof ReadResult) {
						tag.getResult().put(operationId, result.getValue());
					}
					if (result.getValue() instanceof WriteResult) {
						tag.getResult().put(operationId, result.getValue());
					}
					if (result.getValue() instanceof KillResult) {
						tag.getResult().put(operationId, result.getValue());
					}
					if (result.getValue() instanceof LockResult) {
						tag.getResult().put(operationId, result.getValue());
					}
				}

				// Add Missing Results
				for (Operation operation : this.executeTagOperation.getValue().getOperations()) {
					Integer operationId = Integer.valueOf(operation.getId());
					Result result = tag.getResult().get(operationId);

					if (result == null) {
						switch (operation.getType()) {
						case KILL:
							tag.getResult().put(operationId, new KillResult(ResultState.MISC_ERROR_TOTAL));
							break;
						case LOCK:
							tag.getResult().put(operationId, new LockResult(ResultState.MISC_ERROR_TOTAL));
							break;
						case PASSWORD:
							tag.getResult().put(operationId, new PasswordResult(ResultState.SUCCESS));
							break;
						case READ:
							tag.getResult().put(operationId, new ReadResult(ResultState.MISC_ERROR_TOTAL, new byte[0]));
							break;
						case WRITE:
							tag.getResult().put(operationId, new WriteResult(ResultState.MISC_ERROR_TOTAL, 0));
							break;
						default:
							tag.getResult().put(operationId, new FaultResult(ResultState.MISC_ERROR_TOTAL));
							break;
						}
					}
				}

				// Deliver Tag Object to Middleware
				this.clientCallback.notify(e.getId(), tag);
				// Resett Execute Tag Operation
				this.executeTagOperation = new KeyValuePair<Long, TagOperation>();

				if (this.executeEvent.availablePermits() == 0) {
					this.executeEvent.release();
				}
			}
		}
	}

	/**
	 * Method to notify observation port event to the middleware.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event
	 */
	private void notifyObservationPortEvent(Object sender, PortEventArgs e) {
		// Deliver Cloned Port Object for each observer
		synchronized (this.syncPortObserverList) {
			for (long id : this.portObserverList) {
				this.clientCallback.notify(id, (e.getPort().clone()));
			}
		}
	}

	/**
	 * Method to notify execute port event to the middleware.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event
	 */
	private void notifyExecutePortEvent(Object sender, PortEventArgs e) {
		synchronized (this.syncExecutePortOperation) {
			if (this.executePortOperation.getValue() != null) {
				Port port = e.getPort().clone();
				// Add Missing Results
				for (havis.middleware.ale.base.operation.port.Operation operation : this.executePortOperation.getValue().getOperations()) {
					Integer operationId = Integer.valueOf(operation.getId());
					havis.middleware.ale.base.operation.port.result.Result result = port.getResult().get(operationId);

					if (result == null) {
						switch (operation.getType()) {
						case READ:
							port.getResult().put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(State.MISC_ERROR_TOTAL));
							break;
						case WRITE:
							port.getResult().put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(State.MISC_ERROR_TOTAL));
							break;
						default:
							port.getResult().put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(State.MISC_ERROR_TOTAL));
							break;
						}
					}
				}
				// Deliver Tag Object to Middleware
				this.clientCallback.notify(e.getId(), port);
			}
			// Resett Execute Tag Operation
			this.executePortOperation = new KeyValuePair<Long, PortOperation>();

			if (this.executePortEvent.availablePermits() == 0) {
				this.executePortEvent.release();
			}
		}
	}

	@Override
	public List<Capabilities> getCapabilities(CapabilityType capabilityType) {
		List<Capabilities> ret = new ArrayList<>();		
		if (capabilityType == CapabilityType.ALL || capabilityType == CapabilityType.DEVICE_CAPABILITIES) {
			ret.add(new DeviceCapabilities(
				deviceCapabilities.getName(),
				deviceCapabilities.getManufacturer(),
				deviceCapabilities.getModel(),
				deviceCapabilities.getFirmware()));
		}		
		return ret;		
	}

	@Override
	public List<Configuration> getConfiguration(ConfigurationType type, short antennaId) {
		return null;
	}

	@Override
	public void setConfiguration(List<Configuration> configuration) {
		throw new UnsupportedOperationException();
	}

	private void notifyEvent(ReaderEvent event) {
		this.clientCallback.notify(event);
	}

	protected void notifyConnectionError(String msg) {
		this.lastConError = new ConnectionError(new Date(), true, msg);
		notifyEvent(this.lastConError);
	}

	protected void notifyConnectionErrorResolved() {
		if (this.lastConError != null) {
			this.lastConError.setTimestamp(new Date());
			this.lastConError.setState(false);
			notifyEvent(this.lastConError);
			this.lastConError = null;
		}
	}

	private boolean isDisposed = false;

	/**
	 * Disposes this instance.
	 */
	@Override
	public void dispose() throws ImplementationException {
		try {
			this.dispose(true);
		} catch (IOException e) {
			notifyError(e);
		}
	}

	/**
	 * Disposes this instance. According to <paramref name="disposing"/> also
	 * managed resources will be disposed.
	 * 
	 * @param disposing
	 *            Indicator if also managed resources should be disposed.
	 * @throws ImplementationException
	 * @throws IOException
	 */
	protected void dispose(boolean disposing) throws ImplementationException, IOException {
		if (!this.isDisposed) {
			if (this.isConnected) {
				this.clientCallback.notify(new Message(Reader.Controller.Warning, "Disconnect on Dispose!"));
				this.disconnect();
			}
			if (disposing) {
				synchronized (this.syncManager) {
					if (this.manager != null)
						this.manager.dispose();
				}
			}

			this.manager = null;
			this.isDisposed = true;
		}
	}
}

package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.AddAccessSpec;
import havis.llrpservice.data.message.AddAccessSpecResponse;
import havis.llrpservice.data.message.AddROSpec;
import havis.llrpservice.data.message.AddROSpecResponse;
import havis.llrpservice.data.message.CloseConnection;
import havis.llrpservice.data.message.CloseConnectionResponse;
import havis.llrpservice.data.message.DeleteAccessSpec;
import havis.llrpservice.data.message.DeleteAccessSpecResponse;
import havis.llrpservice.data.message.DeleteROSpec;
import havis.llrpservice.data.message.DeleteROSpecResponse;
import havis.llrpservice.data.message.EnableAccessSpec;
import havis.llrpservice.data.message.EnableAccessSpecResponse;
import havis.llrpservice.data.message.EnableEventsAndReports;
import havis.llrpservice.data.message.EnableROSpec;
import havis.llrpservice.data.message.EnableROSpecResponse;
import havis.llrpservice.data.message.GetAccessSpecs;
import havis.llrpservice.data.message.GetAccessSpecsResponse;
import havis.llrpservice.data.message.GetROSpecs;
import havis.llrpservice.data.message.GetROSpecsResponse;
import havis.llrpservice.data.message.GetReaderCapabilities;
import havis.llrpservice.data.message.GetReaderCapabilitiesRequestedData;
import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.GetReaderConfig;
import havis.llrpservice.data.message.GetReaderConfigRequestedData;
import havis.llrpservice.data.message.GetReaderConfigResponse;
import havis.llrpservice.data.message.Keepalive;
import havis.llrpservice.data.message.KeepaliveAck;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.SetReaderConfig;
import havis.llrpservice.data.message.SetReaderConfigResponse;
import havis.llrpservice.data.message.StartROSpec;
import havis.llrpservice.data.message.StartROSpecResponse;
import havis.llrpservice.data.message.StopROSpec;
import havis.llrpservice.data.message.StopROSpecResponse;
import havis.llrpservice.data.message.parameter.AISpec;
import havis.llrpservice.data.message.parameter.AISpecStopTrigger;
import havis.llrpservice.data.message.parameter.AISpecStopTriggerType;
import havis.llrpservice.data.message.parameter.AccessCommand;
import havis.llrpservice.data.message.parameter.AccessReportSpec;
import havis.llrpservice.data.message.parameter.AccessReportTrigger;
import havis.llrpservice.data.message.parameter.AccessSpec;
import havis.llrpservice.data.message.parameter.AccessSpecStopTrigger;
import havis.llrpservice.data.message.parameter.AccessSpecStopTriggerType;
import havis.llrpservice.data.message.parameter.AntennaConfiguration;
import havis.llrpservice.data.message.parameter.C1G2Kill;
import havis.llrpservice.data.message.parameter.C1G2KillOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2Lock;
import havis.llrpservice.data.message.parameter.C1G2LockOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2LockPayload;
import havis.llrpservice.data.message.parameter.C1G2LockPayloadDataField;
import havis.llrpservice.data.message.parameter.C1G2LockPayloadPrivilege;
import havis.llrpservice.data.message.parameter.C1G2PC;
import havis.llrpservice.data.message.parameter.C1G2Read;
import havis.llrpservice.data.message.parameter.C1G2ReadOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2TagSpec;
import havis.llrpservice.data.message.parameter.C1G2TargetTag;
import havis.llrpservice.data.message.parameter.C1G2Write;
import havis.llrpservice.data.message.parameter.C1G2WriteOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2XPCW1;
import havis.llrpservice.data.message.parameter.C1G2XPCW2;
import havis.llrpservice.data.message.parameter.ConnectionAttemptEventStatusType;
import havis.llrpservice.data.message.parameter.GPIPortCurrentState;
import havis.llrpservice.data.message.parameter.GPIPortCurrentStateGPIState;
import havis.llrpservice.data.message.parameter.GPOWriteData;
import havis.llrpservice.data.message.parameter.InventoryParameterSpec;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.Parameter;
import havis.llrpservice.data.message.parameter.ProtocolId;
import havis.llrpservice.data.message.parameter.ROBoundarySpec;
import havis.llrpservice.data.message.parameter.ROSpec;
import havis.llrpservice.data.message.parameter.ROSpecCurrentState;
import havis.llrpservice.data.message.parameter.ROSpecStartTrigger;
import havis.llrpservice.data.message.parameter.ROSpecStartTriggerType;
import havis.llrpservice.data.message.parameter.ROSpecStopTrigger;
import havis.llrpservice.data.message.parameter.ROSpecStopTriggerType;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.llrpservice.data.message.parameter.TagObservationTrigger;
import havis.llrpservice.data.message.parameter.TagObservationTriggerType;
import havis.llrpservice.data.message.parameter.TagReportData;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.operation.port.Pin;
import havis.middleware.ale.base.operation.port.Port;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.tag.LockType;
import havis.middleware.ale.base.operation.tag.Operation;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.CustomResult;
import havis.middleware.ale.base.operation.tag.result.KillResult;
import havis.middleware.ale.base.operation.tag.result.LockResult;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.base.operation.tag.result.WriteResult;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.llrp.client.LLRPConnection;
import havis.middleware.reader.llrp.service.LLRPService;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler;
import havis.middleware.reader.llrp.service.exception.LLRPErrorException;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.service.exception.LLRPTimeoutException;
import havis.middleware.reader.llrp.util.BitConverter;
import havis.middleware.reader.llrp.util.IDGenerator;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class that provides base functionality to manage communication with
 * LLRP readers.
 */

public abstract class ALLRPManager {

	private final static Logger log = Logger.getLogger(ALLRPManager.class.getName());

	/**
	 * Holds all connection informations.
	 */
	private LLRPConnection connectorConfiguration;

	private boolean flag = false;

	/**
	 * Provides access to a reader configuration Instance.
	 */
	protected LLRPConfiguration llrpConfiguration;

	/**
	 * Holds a List of available input pins;
	 */
	List<Integer> inputPins = new ArrayList<Integer>();

	/**
	 * Holds a List of available output pins;
	 */
	List<Integer> outputPins = new ArrayList<Integer>();

	private ROSpec inventoryROSpec;

	/**
	 * Object to synchronise the access to inventory access und reader operation
	 * spec.
	 */
	private Object syncInventory = new Object();

	/**
	 * Indicates if the inventory specs are currently assigned.
	 */
	private boolean isAssigned = false;

	/**
	 * Indicates if the inventory specs were assigned from the connector.
	 */
	private boolean wasAssigned = false;

	/**
	 * Indicates if the inventory specs are currently started.
	 */
	private boolean isStarted = false;

	/**
	 * Indicates if the inventory specs were started from the connector.
	 */
	private boolean wasStarted = false;

	/**
	 * Represents the Inventory Access Spec.
	 */
	private AccessSpec inventoryAccessSpec;

	private boolean isDisposed = false;

	private LLRPEventHandler.LLRPEvent<LLRPEventArgs<ROAccessReport>> roAccessReportLlrpEvent = new LLRPEventHandler.LLRPEvent<LLRPEventArgs<ROAccessReport>>() {
		@Override
		public void fire(Object sender, LLRPEventArgs<ROAccessReport> eventArgs) {
			serviceROAccessReportEvent(sender, eventArgs);
		}
	};

	private LLRPEventHandler.LLRPEvent<LLRPEventArgs<Keepalive>> keepaliveLlrpEvent = new LLRPEventHandler.LLRPEvent<LLRPEventArgs<Keepalive>>() {
		@Override
		public void fire(Object sender, LLRPEventArgs<Keepalive> eventArgs) {
			serviceKeepaliveEvent(sender, eventArgs);
		}
	};

	private LLRPEventHandler.LLRPEvent<EventObject> noDataReceivedLlrpEvent = new LLRPEventHandler.LLRPEvent<EventObject>() {
		@Override
		public void fire(Object sender, EventObject eventArgs) {
			serviceNoDataReceivedEvent(sender, eventArgs);
		}
	};

	private LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>> readerNotificationLlrpEvent = new LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>>() {
		@Override
		public void fire(Object sender, LLRPEventArgs<ReaderEventNotification> eventArgs) {
			serviceReaderNotificationEvent(sender, eventArgs);
		}
	};

	/**
	 * Represents the LLRP Service to communicate with an LLRP Reader.
	 */
	protected LLRPService service;

	/**
	 * Represents the LLRP Protocol Version that is used for all LLRP Messages.
	 */
	protected ProtocolVersion version;

	/**
	 * Represents the LLRP Reader Capabilities that were retrieved from the LLRP
	 * Reader.
	 */
	protected GetReaderCapabilitiesResponse readerCapabilities;

	/**
	 * Provides the reader configuration that was set on the LLRP Reader.
	 */
	protected LLRPConfiguration.LLRPConfig readerConfiguration;

	/**
	 * Represents the current Execute Reader Operation Spec.
	 */
	protected ROSpec executeROSpec;

	/**
	 * Object to synchronise the access to execute reader operation spec.
	 */
	protected Object syncExecute = new Object();

	/**
	 * The event will be raised if LLRP Manager received no data within
	 * keepalive timespan.
	 */
	private LLRPEventHandler<EventObject> noDataReceivedEvent = new LLRPEventHandler<EventObject>();

	/**
	 * The event will be raised if LLRP Manager received an Inventory Tag Event.
	 */
	private LLRPEventHandler<TagEventArgs> inventoryTagEvent = new LLRPEventHandler<ALLRPManager.TagEventArgs>();

	/**
	 * The event will be raised if LLRP Manager received an Execute Tag Event.
	 */
	private LLRPEventHandler<TagEventArgs> executeTagEvent = new LLRPEventHandler<ALLRPManager.TagEventArgs>();

	/**
	 * The event will be raised if LLRP Manager received an Observation Port
	 * Event.
	 */

	private LLRPEventHandler<PortEventArgs> observationPortEvent = new LLRPEventHandler<ALLRPManager.PortEventArgs>();

	/**
	 * The event will be raised if LLRP Manager received an Execute Port Event.
	 */

	private LLRPEventHandler<PortEventArgs> executePortEvent = new LLRPEventHandler<ALLRPManager.PortEventArgs>();

	protected ErrorHandler errorHandler;

	/**
	 * Creates a new Instance of Havis.Middleware.Reader.ALLRPManager.
	 */
	public ALLRPManager(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.service = new LLRPService();
	}

	/**
	 * @return {@link LLRPConfiguration.LLRPConfig}
	 */
	public LLRPConfiguration.LLRPConfig getReaderConfiguration() {
		return readerConfiguration;
	}

	/**
	 * @param readerConfiguration
	 *            To set
	 */
	public void setReaderConfiguration(LLRPConfiguration.LLRPConfig readerConfiguration) {
		this.readerConfiguration = readerConfiguration;
	}

	/**
	 * @return {@link LLRPConnection}
	 */
	public LLRPConnection getConnectorConfiguration() {
		return connectorConfiguration;
	}

	/**
	 * @param connectorConfiguration
	 *            To set
	 */
	public void setConnectorConfiguration(LLRPConnection connectorConfiguration) {
		this.connectorConfiguration = connectorConfiguration;
	}

	/**
	 * @return noDataReceivedEvent
	 */
	public LLRPEventHandler<EventObject> getNoDataReceivedEvent() {
		return noDataReceivedEvent;
	}

	/**
	 * @return inventoryTagEvent
	 */
	public LLRPEventHandler<TagEventArgs> getInventoryTagEvent() {
		return inventoryTagEvent;
	}

	/**
	 * @return executeTagEvent
	 */
	public LLRPEventHandler<TagEventArgs> getExecuteTagEvent() {
		return executeTagEvent;
	}

	/**
	 * @return observationPortEvent
	 */
	public LLRPEventHandler<PortEventArgs> getObservationPortEvent() {
		return observationPortEvent;
	}

	/**
	 * @return executePortEvent
	 */
	public LLRPEventHandler<PortEventArgs> getExecutePortEvent() {
		return executePortEvent;
	}

	/**
	 * @return {@link AccessSpec}
	 */
	protected AccessSpec getInventoryAccessSpec() {
		return inventoryAccessSpec;
	}

	/**
	 * @param inventoryAccessSpec
	 *            To set
	 */
	protected void setInventoryAccessSpec(AccessSpec inventoryAccessSpec) {
		this.inventoryAccessSpec = inventoryAccessSpec;
	}

	/**
	 * @return {@link GetReaderCapabilitiesResponse}
	 */
	protected GetReaderCapabilitiesResponse getReaderCapabilities() {
		return readerCapabilities;
	}

	/**
	 * @param readerCapabilities
	 *            To set
	 */
	protected void setReaderCapabilities(GetReaderCapabilitiesResponse readerCapabilities) {
		this.readerCapabilities = readerCapabilities;
	}

	/**
	 * Returns the Inventory Reader Operation Spec.
	 * 
	 * @return {@link ROSpec}
	 */
	protected ROSpec getInventoryROSpec() {
		if (this.inventoryROSpec != null) {
			return this.inventoryROSpec;
		} else {

			ROSpecStartTrigger roSpecStartTrigger = new ROSpecStartTrigger(new TLVParameterHeader(), ROSpecStartTriggerType.NULL_NO_START_TRIGGER);
			ROSpecStopTrigger roSpecStopTrigger = new ROSpecStopTrigger(new TLVParameterHeader(), ROSpecStopTriggerType.NULL, 0);
			ROBoundarySpec roBoundarySpec = new ROBoundarySpec(new TLVParameterHeader(), roSpecStartTrigger, roSpecStopTrigger);

			AISpecStopTrigger aiSpecStopTrigger = new AISpecStopTrigger(new TLVParameterHeader(), AISpecStopTriggerType.NULL, 0);
			List<AntennaConfiguration> antennaConfigurations = new ArrayList<AntennaConfiguration>();
			InventoryParameterSpec inventoryParameterSpec = new InventoryParameterSpec(new TLVParameterHeader(), 1, ProtocolId.EPC_GLOBAL_C1G2);
			List<InventoryParameterSpec> inventoryParameterSpecs = new ArrayList<InventoryParameterSpec>();

			inventoryParameterSpecs.add(inventoryParameterSpec);

			List<Integer> antennaIdList = new ArrayList<Integer>();
			antennaIdList.add(Integer.valueOf(0));

			antennaConfigurations.add(new AntennaConfiguration(new TLVParameterHeader(), 0));
			inventoryParameterSpec.setAntennaConfigList(antennaConfigurations);

			AISpec aiSpec = new AISpec(new TLVParameterHeader(), antennaIdList, aiSpecStopTrigger, inventoryParameterSpecs);

			List<Parameter> listOfSpecs = new ArrayList<Parameter>();
			listOfSpecs.add(aiSpec);

			this.inventoryROSpec = new ROSpec(new TLVParameterHeader(), 1, this.readerCapabilities.getLlrpCap().getMaxPriorityLevelSupported(),
					ROSpecCurrentState.DISABLED, roBoundarySpec, listOfSpecs);

			return this.inventoryROSpec;
		}
	}

	/**
	 * Method to establish the connection to the reader.
	 * 
	 * @return True if connection was established successfull, false otherwise.
	 */
	public boolean connect() {
		log.log(Level.FINE, "Attempting to connect LLRP manager to \"" + this.connectorConfiguration.getHost() + ":" + this.connectorConfiguration.getPort() + "\"");
		final Object monitor = new Object();
		// Delegate to handel connection attempt event.

		LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>> delegate = new LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>>() {
			@Override
			public void fire(Object sender, LLRPEventArgs<ReaderEventNotification> e) {
				if (log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Received event: " + e.getMessage().toString());
				}
				if (e.getMessage().getReaderEventNotificationData().getConnectionAttemptEvent() != null
						&& e.getMessage().getReaderEventNotificationData().getConnectionAttemptEvent().getStatus() == ConnectionAttemptEventStatusType.SUCCESS) {
					synchronized (monitor) {
						flag = true;
						monitor.notify();
					}
				}
			}
		};

		// Register delegate
		this.service.getReaderNotificationEvent().add(delegate);
		synchronized (monitor) {
			try {
				if (!this.service.openConnection(this.connectorConfiguration)) {
					log.log(Level.FINE, "Failed to connect LLRP manager to \"" + this.connectorConfiguration.getHost() + ":" + this.connectorConfiguration.getPort() + "\"");
					return false;
				} else {
					// Wait for connection attempt event

					int timeout = this.connectorConfiguration.getTimeout();
					monitor.wait(timeout);
					if (flag) {
						this.negotiateProtocolVersion();
					} else {
						this.service.closeConnection();
						log.log(Level.FINE, "Timed out while waiting for connection attempt event to \"" + this.connectorConfiguration.getHost() + ":" + this.connectorConfiguration.getPort());
						return false;
					}
				}

				// Get LLRP Configuration instance.
				this.llrpConfiguration = new LLRPConfiguration(this.service, this.version);
				log.log(Level.FINE, "Timed out while waiting for connection attempt event to \"" + this.connectorConfiguration.getHost() + ":" + this.connectorConfiguration.getPort());
				return true;
			} catch (Exception e) {
				log.log(Level.FINE, "Successfully connected LLRP manager to \"" + this.connectorConfiguration.getHost() + ":" + this.connectorConfiguration.getPort() + "\"");
				this.service.closeConnection();
				return false;
			} finally {
				this.service.getReaderNotificationEvent().remove(delegate);
			}
		}
	}

	/**
	 * Method to apply a configuration to the reader.
	 * 
	 * @throws LLRPException
	 */
	public void applyReaderConfig() throws LLRPException {
		// Get LLRP Reader Capabilities
		this.readerCapabilities = this.service.getReaderCapabilities(new GetReaderCapabilities(new MessageHeader((byte) 0, this.version, IDGenerator
				.getUniqueMessageID()), GetReaderCapabilitiesRequestedData.ALL));

		if (this.readerCapabilities.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("Initialize: " + this.readerCapabilities.getStatus().getStatusCode().toString() + ": "
					+ this.readerCapabilities.getStatus().getErrorDescription());
		}

		// Set LLRP Reader Configuration

		this.llrpConfiguration.applyReaderConfig(this.readerConfiguration, this.connectorConfiguration.getConnectionProperties().getKeepalive(),
				this.readerCapabilities);

		// Get GPIOS
		GetReaderConfigResponse gpiCurrentState = this.service.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, this.version, IDGenerator
				.getUniqueMessageID()), 0, GetReaderConfigRequestedData.GPI_CURRENT_STATE, 0, 0));

		for (GPIPortCurrentState inputPort : gpiCurrentState.getGpiPortCurrentStateList()) {
			this.inputPins.add(Integer.valueOf(inputPort.getGpiPortNum()));
		}

		GetReaderConfigResponse gpoWriteData = this.service.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, this.version, IDGenerator
				.getUniqueMessageID()), 0, GetReaderConfigRequestedData.GPO_WRITE_DATA, 0, 0));

		for (GPOWriteData outputPort : gpoWriteData.getGpoWriteDataList()) {
			this.outputPins.add(Integer.valueOf(outputPort.getGpoPortNum()));
		}
	}

	/**
	 * Method to initialize the reader with current specs.
	 * 
	 * @throws LLRPException
	 */
	public void initialize() throws LLRPException {
		// Remove all Specs from Reader
		this.removeSpecsFromReader();

		// Subscribe to Events and Reports
		this.service.getROAccessReportEvent().add(roAccessReportLlrpEvent);
		this.service.getKeepaliveEvent().add(keepaliveLlrpEvent);
		this.service.getNoDataReceivedEvent().add(noDataReceivedLlrpEvent);
		this.service.getReaderNotificationEvent().add(readerNotificationLlrpEvent);

		// Enable Events and Reports
		try {
			this.service.enableEventsAndReports(new EnableEventsAndReports(new MessageHeader((byte) 0, this.version, IDGenerator.getUniqueMessageID())));
		} catch (IOException | InvalidMessageTypeException | InvalidParameterTypeException e) {
			throw new LLRPException(e.getMessage());
		}
	}

	/**
	 * Method to disconnect from the reader.
	 */
	public void disconnect() {
		// Try to clean up before disconnect
		try {
			// Unassign Inventory Operation
			this.unassign(false);

			// Remove all Specs from Reader
			this.removeSpecsFromReader();

			// Inform reader that the connection will be closed.
			CloseConnectionResponse response = this.service.closeConnection(new CloseConnection(new MessageHeader((byte) 0, this.version, IDGenerator
					.getUniqueMessageID())));
			if (response.getStatus().getStatusCode() == LLRPStatusCode.M_SUCCESS) {
				// Close the connection.
				this.service.closeConnection();
			} else {
				throw new LLRPException("Disconnect: " + response.getStatus().getStatusCode().toString() + ": " + response.getStatus().getErrorDescription());
			}
		} catch (Exception exc) {
			// If cleanup failed close connection finally
			this.isAssigned = false;
			this.isStarted = false;
			this.service.closeConnection();
		}

		// Unsubscribe from Events and Reports
		this.service.getROAccessReportEvent().remove(roAccessReportLlrpEvent);
		this.service.getKeepaliveEvent().remove(keepaliveLlrpEvent);
		this.service.getNoDataReceivedEvent().remove(noDataReceivedLlrpEvent);
		this.service.getReaderNotificationEvent().remove(readerNotificationLlrpEvent);
	}

	/**
	 * Method to add a inventory reader operation spec to the reader.
	 * 
	 * @param execute
	 *            Indicates if this was called by an execute operation
	 * @throws LLRPException
	 */
	public void assign(boolean execute) throws LLRPException {
		synchronized (this.syncInventory) {
			// If the method was called by the reader connector and inventory is
			// currently not assigned
			// Or Method was called by an execute operation and inventory was
			// assigend but is currently not assigned
			if ((!execute || (this.wasAssigned && execute)) && !this.isAssigned) {
				// Add and Enable Inventory Reader Operation Spec
				AddROSpecResponse addROSpecResponse = this.service.addROSpec(new AddROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
						.getUniqueMessageID()), this.getInventoryROSpec()));
				if (addROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
					throw new LLRPException("Assign: " + addROSpecResponse.getStatus().getStatusCode().toString() + ": "
							+ addROSpecResponse.getStatus().getErrorDescription());
				}

				EnableROSpecResponse enableROSpecResponse = this.service.enableROSpec(new EnableROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
						.getUniqueMessageID()), this.getInventoryROSpec().getRoSpecID()));

				if (enableROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
					throw new LLRPException("Assign: " + enableROSpecResponse.getStatus().getStatusCode().toString() + ": "
							+ enableROSpecResponse.getStatus().getErrorDescription());
				}

				// If needed Add and Enable Inventory Access Spec
				if (this.getInventoryAccessSpec() != null) {
					AddAccessSpecResponse addAccessSpecResponse = this.service.addAccessSpec(new AddAccessSpec(new MessageHeader((byte) 0, this.version,
							IDGenerator.getUniqueMessageID()), this.getInventoryAccessSpec()));

					if (addAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
						throw new LLRPException("Assign: " + addAccessSpecResponse.getStatus().getStatusCode().toString() + ": "
								+ addAccessSpecResponse.getStatus().getErrorDescription());
					}

					EnableAccessSpecResponse enableAccessSpecResponse = this.service.enableAccessSpec(new EnableAccessSpec(new MessageHeader((byte) 0,
							this.version, IDGenerator.getUniqueMessageID()), this.getInventoryAccessSpec().getAccessSpecId()));

					if (enableAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
						throw new LLRPException("Assign: " + enableAccessSpecResponse.getStatus().getStatusCode().toString() + ": "
								+ enableAccessSpecResponse.getStatus().getErrorDescription());
					}
				}
				// Inventory is now assigend
				this.isAssigned = true;
			}
			// If called from reader connector
			if (!execute) {
				this.wasAssigned = true;
			}
		}
	}

	/**
	 * Method to remove a inventory reader operation spec from the reader.
	 * 
	 * @param execute
	 *            Indicates if this was called by an execute // operation
	 * @throws LLRPException
	 */
	public void unassign(boolean execute) throws LLRPException {
		synchronized (this.syncInventory) {
			// If the method was called by the reader connector and inventory is
			// currently assigned
			// Or Method was called by an execute operation and inventory was
			// assigend and is still assigned
			if ((!execute || (this.wasAssigned && execute)) && this.isAssigned) {
				// If needed Delete Inventory Access Spec
				if (this.inventoryAccessSpec != null) {
					DeleteAccessSpecResponse deleteAccessSpecResponse = this.service.deleteAccessSpec(new DeleteAccessSpec(new MessageHeader((byte) 0,
							this.version, IDGenerator.getUniqueMessageID()), this.getInventoryAccessSpec().getAccessSpecId()));

					if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
						throw new LLRPException("Unassign: " + deleteAccessSpecResponse.getStatus().getStatusCode().toString() + ": "
								+ deleteAccessSpecResponse.getStatus().getErrorDescription());
					}
				}

				// Delete Inventory Reader Operation Spec
				DeleteROSpecResponse deleteROSpecResponse = this.service.deleteROSpec(new DeleteROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
						.getUniqueMessageID()), this.getInventoryROSpec().getRoSpecID()));

				if (deleteROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
					throw new LLRPException("Unassign: " + deleteROSpecResponse.getStatus().getStatusCode().toString() + ": "
							+ deleteROSpecResponse.getStatus().getErrorDescription());
				}

				// Inventory is now unassigend
				this.isAssigned = false;
			}
			// If called from reader connector
			if (!execute) {
				this.wasAssigned = false;
			}
		}
	}

	/**
	 * Method to start a inventory reader operation spec on the reader.
	 * 
	 * @param execute
	 *            Indicates if this was called by an execute operation
	 * @throws LLRPException
	 */
	public void start(boolean execute) throws LLRPException {
		synchronized (this.syncInventory) {
			// If the method was called by the reader connector and inventory is
			// currently not started
			// Or Method was called by an execute operation and inventory was
			// started but is currently not started
			if ((!execute || (this.wasStarted && execute)) && !this.isStarted) {
				// Start Inventory Reader Operation Spec
				StartROSpecResponse startROSpecResponse = this.service.startROSpec(new StartROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
						.getUniqueMessageID()), this.getInventoryROSpec().getRoSpecID()));

				if (startROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
					throw new LLRPException("Start: " + startROSpecResponse.getStatus().getStatusCode().toString() + ": "
							+ startROSpecResponse.getStatus().getErrorDescription());
				}

				// Inventory is now started
				this.isStarted = true;
			}
			// If called from reader connector
			if (!execute) {
				this.wasStarted = true;
			}
		}
	}

	/**
	 * Method to stop a inventory reader operation spec on the reader.
	 * 
	 * @param execute
	 *            Indicates if this was called by an execute operation
	 * @throws LLRPException
	 */
	public void stop(boolean execute) throws LLRPException {
		synchronized (this.syncInventory) {
			// If the method was called by the reader connector and inventory is
			// currently started
			// Or Method was called by an execute operation and inventory was
			// started and is still started
			if ((!execute || (this.wasStarted && execute)) && this.isStarted) {
				// Start Inventory Reader Operation Spec
				StopROSpecResponse stopROSpecResponse = this.service.stopROSpec(new StopROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
						.getUniqueMessageID()), this.getInventoryROSpec().getRoSpecID()));

				if (stopROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
					throw new LLRPException("Stop: " + stopROSpecResponse.getStatus().getStatusCode().toString() + ": "
							+ stopROSpecResponse.getStatus().getErrorDescription());
				}

				// Inventory is now unstarted
				this.isStarted = false;
			}
			// If called from reader connector
			if (!execute) {
				this.wasStarted = false;
			}
		}
	}

	/**
	 * Method to update a inventory access operation spec on the reader.
	 * 
	 * @param inventoryOperation
	 *            The inventory operation
	 * @throws LLRPException
	 */
	public void updateInventory(LLRPInventoryOperation inventoryOperation) throws LLRPException {
		synchronized (this.syncInventory) {
			// If any memory bank is requested
			if (inventoryOperation.isReserved() || inventoryOperation.isTid() || inventoryOperation.isEpc() || inventoryOperation.isUser()) {
				// Create Inventory Access Spec

				List<Parameter> opSpecs = new ArrayList<Parameter>();

				if (inventoryOperation.isReserved()) {
					opSpecs.add(new C1G2Read(new TLVParameterHeader(), 0, 0, (byte) 0, 0, 0));
				}

				if (inventoryOperation.isEpc()) {
					opSpecs.add(new C1G2Read(new TLVParameterHeader(), 1, 0, (byte) 1, 0, 0));
				}

				if (inventoryOperation.isTid()) {
					opSpecs.add(new C1G2Read(new TLVParameterHeader(), 2, 0, (byte) 2, 0, 0));
				}

				if (inventoryOperation.isUser()) {
					boolean[] userDataBlocks = inventoryOperation.getUserDataBlocks();
					int userDataBlocksCount = userDataBlocks == null ? 0 : userDataBlocks.length;
					int opSpecId = 3;
					
					if (userDataBlocksCount == 0 || inventoryOperation.isForceUserComplete()) {
						// read complete bank
						opSpecs.add(new C1G2Read(new TLVParameterHeader(), opSpecId++, 0, (byte) 3, 0, 0));
					}

					if (userDataBlocksCount > 0 && userDataBlocks != null) {
						// read specified bank range

						for (int offset = 0; offset < userDataBlocksCount; offset++) {
							int length = 0;

							while (!userDataBlocks[offset] && (offset < userDataBlocksCount)) {
								offset++;
							}

							if (offset < userDataBlocksCount) {
								while (((offset + length) < userDataBlocksCount) && userDataBlocks[offset + length]) {
									length++;
								}

								opSpecs.add(new C1G2Read(new TLVParameterHeader(), opSpecId++, 0, (byte) 3, offset, length));

								offset += (offset + length);
							}
						}
					}
				}

				AccessSpecStopTrigger accessSpecStopTrigger = new AccessSpecStopTrigger(new TLVParameterHeader(), AccessSpecStopTriggerType.NULL, 0);
				C1G2TargetTag tagPattern = new C1G2TargetTag(new TLVParameterHeader(), (byte) 0, true, 0, new BitSet(0), new BitSet(0));
				C1G2TagSpec tagSpec = new C1G2TagSpec(new TLVParameterHeader(), tagPattern);
				AccessCommand accessCommand = new AccessCommand(new TLVParameterHeader(), tagSpec, opSpecs);

				AccessSpec accessSpec = new AccessSpec(new TLVParameterHeader(), 1, 0, ProtocolId.EPC_GLOBAL_C1G2, false, this.getInventoryROSpec()
						.getRoSpecID(), accessSpecStopTrigger, accessCommand);
				accessSpec.setAccessReportSpec(new AccessReportSpec(new TLVParameterHeader(), AccessReportTrigger.WHENEVER_ROREPORT_IS_GENERATED));

				// If Inventory is currently assigned update inventory access
				// spec on the reader.
				if (this.isAssigned) {
					this.updateInventoryAccessSpec(accessSpec);
				} else {
					this.inventoryAccessSpec = accessSpec;
				}
			} else {
				// If no memory is requested and inventory access is not null
				// and is currently assigned
				if (this.isAssigned && this.inventoryAccessSpec != null) {
					// Delete Inventory access spec
					DeleteAccessSpecResponse deleteAccessSpecResponse = this.service.deleteAccessSpec(new DeleteAccessSpec(new MessageHeader((byte) 0,
							this.version, IDGenerator.getUniqueMessageID()), this.getInventoryAccessSpec().getAccessSpecId()));

					if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
						throw new LLRPException("UpdateInventory: " + deleteAccessSpecResponse.getStatus().getStatusCode().toString() + ": "
								+ deleteAccessSpecResponse.getStatus().getErrorDescription());
					}
				}
				// Set inventory access spec to null
				this.inventoryAccessSpec = null;
			}
		}
	}

	/**
	 * Methode to validate an Execute Tag Operation and returns a errorlist if
	 * 
	 * @param operation
	 *            The operation to validate
	 * @return {@link LLRPReturnContainerUtil} that contains the error list if
	 *         validation failed and true property if validation passed, false
	 *         otherwise
	 */
	public LLRPReturnContainerUtil<Map<Integer, Result>> validateExecuteTagOperation(TagOperation operation) {
		Map<Integer, Result> errorList = new HashMap<Integer, Result>();
		LLRPReturnContainerUtil<Map<Integer, Result>> returnContainerUtil = new LLRPReturnContainerUtil<Map<Integer, Result>>();

		returnContainerUtil.setValue(errorList);

		// To Many Filters specified
		if (operation.getFilter().size() > 2) {
			return returnContainerUtil;
		}

		// To Many Operations specified
		if (this.readerCapabilities.getLlrpCap().getMaxNumOpSpecsPerAccessSpec() > 0
				&& operation.getOperations().size() > this.readerCapabilities.getLlrpCap().getMaxNumOpSpecsPerAccessSpec()) {
			return returnContainerUtil;
		}

		// Wrong field definition for operation
		for (Operation op : operation.getOperations()) {
			switch (op.getType()) {
			case KILL:
				break;
			case LOCK:
				try {
					LockType lockType = LockType.values()[op.getData()[0]];

					switch (lockType) {
					case PERMAUNLOCK:
					case UNLOCK:
					case PERMALOCK:
					case LOCK:
						break;
					}
				} catch (ArrayIndexOutOfBoundsException aiooe) {
					errorList.put(Integer.valueOf(op.getId()), new LockResult(ResultState.OP_NOT_POSSIBLE_ERROR));
				}

				if (!(/* killPwd */(op.getField().getBank() == 0 && op.getField().getOffset() == 0 && op.getField().getLength() == 32) ||
				/* accessPwd */(op.getField().getBank() == 0 && op.getField().getOffset() == 32 && op.getField().getLength() == 32) ||
				/* epcBank */(op.getField().getBank() == 1 && op.getField().getOffset() == 0 && op.getField().getLength() == 0) ||
				/* tidBank */(op.getField().getBank() == 2 && op.getField().getOffset() == 0 && op.getField().getLength() == 0) ||
				/* userBank */(op.getField().getBank() == 3 && op.getField().getOffset() == 0 && op.getField().getLength() == 0))) {
					errorList.put(Integer.valueOf(op.getId()), new LockResult(ResultState.OP_NOT_POSSIBLE_ERROR));
				}

				break;
			case PASSWORD:
				break;
			case READ:
				break;
			case WRITE:
				if ((op.getData().length % 2 != 0 || op.getField().getOffset() % 2 != 0 || op.getField().getLength() % 2 != 0))
					errorList.put(Integer.valueOf(op.getId()), new WriteResult(ResultState.OP_NOT_POSSIBLE_ERROR, 0));
				break;
			case CUSTOM:
				errorList.put(Integer.valueOf(op.getId()), new CustomResult(ResultState.OP_NOT_POSSIBLE_ERROR));
			default:
				// Unknown Operation Type
				return returnContainerUtil;
			}
		}
		// If errorList contains zero error results return true, false otherwise

		returnContainerUtil.setTrue(errorList.isEmpty());

		return returnContainerUtil;
	}

	/**
	 * Method to add an Execute Tag Operation to the reader.
	 * 
	 * @param id
	 *            The id of the execute tag operation to add
	 * @param operation
	 *            The execute tag operation to add
	 * @throws ImplementationException
	 * @throws LLRPException
	 */
	public void addExecuteTagOperation(long id, TagOperation operation) throws ImplementationException, LLRPException {
		synchronized (this.syncExecute) {
			// If an execute tag operation is currently defined
			if (this.executeROSpec != null) {
				throw new ImplementationException("ReaderConnector can handle only one execute operation at time (" + this.connectorConfiguration.toString()
						+ ")!");
			}

			// Init ROSpec start
			ROSpecStartTrigger roSpecStartTrigger = new ROSpecStartTrigger(new TLVParameterHeader(), ROSpecStartTriggerType.NULL_NO_START_TRIGGER);
			ROSpecStopTrigger roSpecStopTrigger = new ROSpecStopTrigger(new TLVParameterHeader(), ROSpecStopTriggerType.NULL, 0);
			ROBoundarySpec roBoundarySpec = new ROBoundarySpec(new TLVParameterHeader(), roSpecStartTrigger, roSpecStopTrigger);

			List<Integer> antennaIDs = new ArrayList<Integer>();
			antennaIDs.add(Integer.valueOf(0));

			AISpecStopTrigger aiSpecStopTrigger = new AISpecStopTrigger(new TLVParameterHeader(), AISpecStopTriggerType.TAG_OBSERVATION, 0);

			// Set Stop Trigger to LR Property Connector.InventoryAttempts
			TagObservationTrigger tagObservationTrigger = new TagObservationTrigger(new TLVParameterHeader(),
					TagObservationTriggerType.N_ATTEMPTS_TO_SEE_ALL_TAGS_IN_THE_FOV_OR_TIMEOUT, 0, this.connectorConfiguration.getConnectionProperties()
							.getInventoryAttempts(), 0, 0);

			aiSpecStopTrigger.setTagOT(tagObservationTrigger);

			List<InventoryParameterSpec> inventoryParameterSpecs = new ArrayList<InventoryParameterSpec>();
			InventoryParameterSpec inventoryParameterSpec = new InventoryParameterSpec(new TLVParameterHeader(), 1, ProtocolId.EPC_GLOBAL_C1G2);
			List<AntennaConfiguration> antennaConfigList = new ArrayList<AntennaConfiguration>();
			AntennaConfiguration antennaConfiguration = new AntennaConfiguration(new TLVParameterHeader(), 0);
			antennaConfigList.add(antennaConfiguration);
			inventoryParameterSpec.setAntennaConfigList(antennaConfigList);

			inventoryParameterSpecs.add(inventoryParameterSpec);

			AISpec aiSpec = new AISpec(new TLVParameterHeader(), antennaIDs, aiSpecStopTrigger, inventoryParameterSpecs);

			List<Parameter> specList = new ArrayList<Parameter>();
			specList.add(aiSpec);

			this.executeROSpec = new ROSpec(new TLVParameterHeader(), id, (short) 0, ROSpecCurrentState.DISABLED, roBoundarySpec, specList);
			// Init ROSpec end

			// Set stop trigger that only one tag is processed.
			AccessSpecStopTrigger accessSpecStopTrigger = new AccessSpecStopTrigger(new TLVParameterHeader(), AccessSpecStopTriggerType.OPERATION_COUNT, 1);
			C1G2TagSpec c1g2TagSpec = new C1G2TagSpec();
			List<Parameter> opSpecList = new ArrayList<Parameter>();

			AccessCommand accessCommand = new AccessCommand(new TLVParameterHeader(), c1g2TagSpec, opSpecList);
			AccessSpec accessSpec = new AccessSpec(new TLVParameterHeader(), id, 0, ProtocolId.EPC_GLOBAL_C1G2, false, id, accessSpecStopTrigger, accessCommand);

			if (operation.getFilter() != null) {
				// If no filter is specified
				if (operation.getFilter().size() == 0) {
					C1G2TargetTag c1g2TargetTag = new C1G2TargetTag(new TLVParameterHeader(), (byte) 0, true, 0, new BitSet(0), new BitSet(0));
					accessCommand.getC1g2TagSpec().setTagPattern1(c1g2TargetTag);
				}

				// Use first specified filter as epc filter
				if (operation.getFilter().size() >= 1) {
					byte[] mask = new byte[operation.getFilter().get(0).getLength() / 8];

					for (int i = 0; i < mask.length; i++) {
						mask[i] = (byte) 0xFF;
					}

					C1G2TargetTag c1g2TargetTag = new C1G2TargetTag(new TLVParameterHeader(), (byte) operation.getFilter().get(0).getBank(), true, operation
							.getFilter().get(0).getOffset(), BitConverter.toBitSet(mask), BitConverter.toBitSet(operation.getFilter().get(0).getMask()));
					c1g2TargetTag.setMaskBitCount(operation.getFilter().get(0).getLength());
					c1g2TargetTag.setDataBitCount(operation.getFilter().get(0).getLength());
					accessCommand.getC1g2TagSpec().setTagPattern1(c1g2TargetTag);
				}
				// Use second specified filter as tid filter
				if (operation.getFilter().size() == 2) {
					byte[] mask = new byte[operation.getFilter().get(1).getLength() / 8];

					for (int i = 0; i < mask.length; i++) {
						mask[i] = (byte) 0xFF;
					}

					C1G2TargetTag c1g2TargetTag = new C1G2TargetTag(new TLVParameterHeader(), (byte) operation.getFilter().get(1).getBank(), true, operation
							.getFilter().get(1).getOffset(), BitConverter.toBitSet(mask), BitConverter.toBitSet(operation.getFilter().get(1).getMask()));
					c1g2TargetTag.setMaskBitCount(operation.getFilter().get(1).getLength());
					c1g2TargetTag.setDataBitCount(operation.getFilter().get(1).getLength());
					accessCommand.getC1g2TagSpec().setTagPattern2(c1g2TargetTag);
				}
			}

			int accessPassword = 0;
			int opSpecID = 0;

			for (Operation op : operation.getOperations()) {
				switch (op.getType()) {
				case KILL:
					// Array.Reverse(op.Data, 0, 4); Little-Endian?
					accessCommand.getOpSpecList().add(new C1G2Kill(new TLVParameterHeader(), opSpecID, BitConverter.toInt(op.getData())));
					break;
				case LOCK:
					C1G2LockPayloadPrivilege privilege = C1G2LockPayloadPrivilege.READ_WRITE;
					int index = op.getData()[0];

					try {
						LockType lockType = LockType.values()[index];

						switch (lockType) {
						case LOCK:
							privilege = C1G2LockPayloadPrivilege.READ_WRITE;
							break;
						case PERMALOCK:
							privilege = C1G2LockPayloadPrivilege.PERMALOCK;
							break;
						case PERMAUNLOCK:
							privilege = C1G2LockPayloadPrivilege.PERMAUNLOCK;
							break;
						case UNLOCK:
							privilege = C1G2LockPayloadPrivilege.UNLOCK;
							break;
						}
					} catch (ArrayIndexOutOfBoundsException aiooe) {

					}

					if ((op.getField().getBank() == 0) && (op.getField().getOffset() == 0) && (op.getField().getLength() == 32)) {
						List<C1G2LockPayload> lockCommandPayloadList = new ArrayList<C1G2LockPayload>();
						C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, C1G2LockPayloadDataField.KILL_PASSWORD);

						lockCommandPayloadList.add(c1g2LockPayload);

						C1G2Lock c1g2Lock = new C1G2Lock(new TLVParameterHeader(), opSpecID, accessPassword, lockCommandPayloadList);

						accessCommand.getOpSpecList().add(c1g2Lock);

						break;
					}
					if ((op.getField().getBank() == 0) && (op.getField().getOffset() == 32) && (op.getField().getLength()) == 32) {
						List<C1G2LockPayload> lockCommandPayloadList = new ArrayList<C1G2LockPayload>();
						C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, C1G2LockPayloadDataField.ACCESS_PASSWORD);

						lockCommandPayloadList.add(c1g2LockPayload);

						C1G2Lock c1g2Lock = new C1G2Lock(new TLVParameterHeader(), opSpecID, accessPassword, lockCommandPayloadList);

						accessCommand.getOpSpecList().add(c1g2Lock);
						break;
					}
					if ((op.getField().getBank() == 1) && (op.getField().getOffset() == 0) && (op.getField().getLength() == 0)) {
						List<C1G2LockPayload> lockCommandPayloadList = new ArrayList<C1G2LockPayload>();
						C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, C1G2LockPayloadDataField.EPC_MEMORY);

						lockCommandPayloadList.add(c1g2LockPayload);

						C1G2Lock c1g2Lock = new C1G2Lock(new TLVParameterHeader(), opSpecID, accessPassword, lockCommandPayloadList);

						accessCommand.getOpSpecList().add(c1g2Lock);
						break;
					}
					if ((op.getField().getBank() == 2) && (op.getField().getOffset() == 0) && (op.getField().getLength() == 0)) {
						List<C1G2LockPayload> lockCommandPayloadList = new ArrayList<C1G2LockPayload>();
						C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, C1G2LockPayloadDataField.TID_MEMORY);

						lockCommandPayloadList.add(c1g2LockPayload);

						C1G2Lock c1g2Lock = new C1G2Lock(new TLVParameterHeader(), opSpecID, accessPassword, lockCommandPayloadList);

						accessCommand.getOpSpecList().add(c1g2Lock);
						break;
					}
					if ((op.getField().getBank() == 3) && (op.getField().getOffset() == 0) && (op.getField().getLength() == 0)) {
						List<C1G2LockPayload> lockCommandPayloadList = new ArrayList<C1G2LockPayload>();
						C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, C1G2LockPayloadDataField.USER_MEMORY);

						lockCommandPayloadList.add(c1g2LockPayload);

						C1G2Lock c1g2Lock = new C1G2Lock(new TLVParameterHeader(), opSpecID, accessPassword, lockCommandPayloadList);

						accessCommand.getOpSpecList().add(c1g2Lock);
						break;
					}
					break;
				case PASSWORD:
					// Array.Reverse(op.Data, 0, 4); Little-Endian?
					accessPassword = BitConverter.toInt(op.getData());
					break;
				case READ:
					accessCommand.getOpSpecList().add(
							new C1G2Read(new TLVParameterHeader(), opSpecID, accessPassword, (byte) op.getField().getBank(), (op.getField().getOffset() / 16),
									(op.getField().getLength() / 16)));
					break;
				case WRITE:
					accessCommand.getOpSpecList().add(
							new C1G2Write(new TLVParameterHeader(), opSpecID, accessPassword, (byte) op.getField().getBank(), (op.getField().getOffset() / 16), op.getData()));
					break;
				default:
					break;
				}
				opSpecID++;
			}

			accessSpec.setAccessReportSpec(new AccessReportSpec(new TLVParameterHeader(), AccessReportTrigger.WHENEVER_ROREPORT_IS_GENERATED));
			this.addExecuteAccessSpec(accessSpec);
		}
	}

	/**
	 * Methode to remove an Execute Tag Operation from the reader.
	 * 
	 * @param id
	 *            The id of the execute tag operation to remove
	 * @throws LLRPException
	 */
	public void removeExecuteTagOperation(long id) throws LLRPException {
		synchronized (this.syncExecute) {
			// If execute tag operation was not already removed
			if (this.executeROSpec != null && this.executeROSpec.getRoSpecID() == id) {
				this.removeExecuteReaderOperationSpec(id);
				this.executeROSpec = null;
			}
		}
	}

	/**
	 * Method to retrieve a configuration from the reader.
	 * 
	 * @return The current reader configuration
	 */
	public RCConfig getReaderConfig() {
		return null;
	}

	/**
	 * Method to add an Execute Port Operation to the reader.
	 * 
	 * @param id
	 *            The id of the execute port operation to add
	 * @param operation
	 *            The execute port operation to add
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public void addExecutePortOperation(long id, PortOperation operation) {
		Map<Integer, havis.middleware.ale.base.operation.port.result.Result> result = new HashMap<Integer, havis.middleware.ale.base.operation.port.result.Result>();
		boolean error = false;

		for (havis.middleware.ale.base.operation.port.Operation op : operation.getOperations()) {
			Integer operationId = Integer.valueOf(op.getId());
			switch (op.getType()) {
			case READ:
				if (error) {
					result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
							havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
				} else {
					switch (op.getPin().getType()) {
					case INPUT:
						if (!this.inputPins.contains(Integer.valueOf(op.getPin().getId()))) {
							result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
									havis.middleware.ale.base.operation.port.result.Result.State.PORT_NOT_FOUND_ERROR));
							error = true;
						} else {
							GetReaderConfigResponse gpiRead = null;
							try {
								// Read GPI Current State from Reader
								gpiRead = this.service.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), 0, GetReaderConfigRequestedData.GPI_CURRENT_STATE, op.getPin().getId(), 0));
							} catch (LLRPException e) {
								errorHandler.notifyError(e);
							}

							if (gpiRead != null && gpiRead.getStatus().getStatusCode() == LLRPStatusCode.M_SUCCESS) {
								havis.middleware.ale.base.operation.port.result.ReadResult readResult = new havis.middleware.ale.base.operation.port.result.ReadResult(
										havis.middleware.ale.base.operation.port.result.Result.State.SUCCESS, (byte) (gpiRead.getGpiPortCurrentStateList()
												.get(0).getState() == GPIPortCurrentStateGPIState.HIGH ? 0x01 : 0x00));

								result.put(operationId, readResult);
							} else {
								result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
										havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
								error = true;
							}
						}
						break;
					case OUTPUT:
						if (!this.outputPins.contains(Integer.valueOf(op.getPin().getId()))) {
							result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
									havis.middleware.ale.base.operation.port.result.Result.State.PORT_NOT_FOUND_ERROR));
							error = true;
						} else {
							GetReaderConfigResponse gpoRead = null;
							try {
								// Read GPO Write Data from Reader
								gpoRead = this.service.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), 0, GetReaderConfigRequestedData.GPO_WRITE_DATA, 0, op.getPin().getId()));
							} catch (LLRPException e) {
								errorHandler.notifyError(e);
							}
							if (gpoRead != null && gpoRead.getStatus().getStatusCode() == LLRPStatusCode.M_SUCCESS) {
								havis.middleware.ale.base.operation.port.result.ReadResult readResult = new havis.middleware.ale.base.operation.port.result.ReadResult(
										havis.middleware.ale.base.operation.port.result.Result.State.SUCCESS, (byte) (gpoRead.getGpoWriteDataList().get(0)
												.getGpoState() ? 0x01 : 0x00));

								result.put(operationId, readResult);
							} else {
								result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
										havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
								error = true;
							}
						}
						break;
					default:
						// Unknown Pin Type
						result.put(operationId, new havis.middleware.ale.base.operation.port.result.ReadResult(
								havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
						error = true;
						break;
					}
				}
				break;
			case WRITE:
				if (error) {
					result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
							havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
				} else {
					// If Duration specified
					if (op.getDuration() != null && op.getDuration().intValue() > 0) {
						result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
								havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
						error = true;
					} else {
						switch (op.getPin().getType()) {
						case INPUT:
							result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
									havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
							error = true;
							break;
						case OUTPUT:
							if (!this.outputPins.contains(Integer.valueOf(op.getPin().getId()))) {
								result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
										havis.middleware.ale.base.operation.port.result.Result.State.PORT_NOT_FOUND_ERROR));
								error = true;
							} else {
								List<GPOWriteData> gpoWriteData = new ArrayList<GPOWriteData>();
								SetReaderConfig setReaderConfig = new SetReaderConfig(new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), false);
								int gpoPortNumber = op.getPin().getId();
								boolean gpoState = op.getData().byteValue() == 0x01 ? true : false;

								GPOWriteData data = new GPOWriteData(new TLVParameterHeader(), gpoPortNumber, gpoState);
								gpoWriteData.add(data);

								setReaderConfig.setGpoWriteDataList(gpoWriteData);

								SetReaderConfigResponse gpoWrite = null;
								try {
									// Set GPO Write Data on Reader
									gpoWrite = this.service.setReaderConfig(setReaderConfig);
								} catch (LLRPException e) {
									errorHandler.notifyError(e);
								}

								if (gpoWrite != null && gpoWrite.getStatus().getStatusCode() == LLRPStatusCode.M_SUCCESS) {
									result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
											havis.middleware.ale.base.operation.port.result.Result.State.SUCCESS));
								} else {
									result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
											havis.middleware.ale.base.operation.port.result.Result.State.MISC_ERROR_TOTAL));
									error = true;
								}
							}
							break;
						default:
							// Unknown Pin Type
							result.put(operationId, new havis.middleware.ale.base.operation.port.result.WriteResult(
									havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
							error = true;
							break;
						}
					}
				}
				break;
			default:
				if (error) {
					result.put(operationId, new havis.middleware.ale.base.operation.port.result.Result(
							havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
				} else {
					result.put(operationId, new havis.middleware.ale.base.operation.port.result.Result(
							havis.middleware.ale.base.operation.port.result.Result.State.OP_NOT_POSSIBLE_ERROR));
					error = true;
				}
				break;
			}
		}
		// Deliever Result
		this.onExecutePortEvent(id, new havis.middleware.ale.base.operation.port.Port(result));
	}

	/**
	 * Method to handle reports send from the LLRP Service.
	 * 
	 * @param sender
	 *            The sender of the report
	 * @param e
	 *            The report that was send
	 */
	public void serviceROAccessReportEvent(Object sender, LLRPEventArgs<ROAccessReport> e) {
		try {
			List<Parameter> opSpecList;
			
			if((this.inventoryAccessSpec != null) && (this.inventoryAccessSpec.getAccessCommand() != null)) {
				opSpecList = this.inventoryAccessSpec.getAccessCommand().getOpSpecList();
			} else {
				opSpecList = new ArrayList<>();
			}

			for (TagReportData tagReportData : e.getMessage().getTagReportDataList()) {
				// Create Middleware Tag Object
				Tag tag = null;
				// Set EPC Value
				if (tagReportData.getEpcData() != null) {
					tag = new Tag(BitConverter.fromBitSet(tagReportData.getEpcData().getEpc(), tagReportData.getEpcData().getEpcLengthBits()));
				} else if (tagReportData.getEpc96() != null) {
					tag = new Tag(tagReportData.getEpc96().getEpc());
				} else {
					throw new UnsupportedOperationException("Unsupported parameter type: " + tagReportData.getEpcData().getParameterHeader().getParameterType());
				}

				// Set PC and XPC values
				byte[] pc = new byte[2];
				byte[] xpc = new byte[4];

				for (Parameter tagData : tagReportData.getC1g2TagDataList()) {
					switch (tagData.getParameterHeader().getParameterType()) {
					case C1G2_PC:
						pc = BitConverter.getBytes(((C1G2PC) tagData).getPcBits());
						break;
					case C1G2_CRC:
						break;
					case C1G2_XPCW1:
						xpc = BitConverter.getBytes(((C1G2XPCW1) tagData).getXpcW1());
						break;
					case C1G2_XPCW2:
						xpc = BitConverter.getBytes(((C1G2XPCW2) tagData).getXpcW2());
						break;
					default:
						break;
					}
				}

				tag.setPc(pc);
				tag.setXPC(xpc);

				// Set Sighting Value
				int rssi = (tagReportData.getPeakRSSI() != null) ? tagReportData.getPeakRSSI().getPeakRSSI() : 0;
				tag.setSighting(new Sighting(this.connectorConfiguration.getHost(), (short) tagReportData.getAntID().getAntennaId(), rssi, tag.getFirstTime()));

				Map<Integer, Result> tagResults = this.getTagResults(tagReportData.getOpSpecResultList());
				tag.setResult(tagResults);

				// Check if report refers to execute or inventory operation.
				synchronized (this.syncExecute) {
					if (this.executeROSpec != null && tagReportData.getRoSpecID().getRoSpecID() == this.executeROSpec.getRoSpecID()
							&& tagReportData.getOpSpecResultList().size() > 0) {
						this.onExecuteTagEvent(tagReportData.getRoSpecID().getRoSpecID(), tag);
					} else if (this.getInventoryROSpec() != null && tagReportData.getRoSpecID().getRoSpecID() == this.getInventoryROSpec().getRoSpecID()) {
						this.onInventoryTagEvent(tag, opSpecList);
					}
				}
			}
		} catch (LLRPException ex) {
			errorHandler.notifyError(ex);
		}
	}

	/**
	 * Method to handle keepalive event send from the LLRP Service.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event that was send
	 */
	void serviceKeepaliveEvent(Object sender, LLRPEventArgs<Keepalive> e) {
		// Send Keepalive Acknowledge to the LLRP Reader
		try {
			this.service.keepaliveAck(new KeepaliveAck(new MessageHeader((byte) 0, this.version, IDGenerator.getUniqueMessageID())));
		} catch (IOException | InvalidMessageTypeException | InvalidParameterTypeException e1) {
			errorHandler.notifyError(e1);
		}
	}

	/**
	 * Method to handle not data received event send from the LLRP Service.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event that was send
	 */
	void serviceNoDataReceivedEvent(Object sender, EventObject e) {
		this.onNoDataReceivedEvent(e);
	}

	/**
	 * Method to handle reader notification event send from the LLRP Service.
	 * 
	 * @param sender
	 *            The sender of the event
	 * @param e
	 *            The event that was send
	 */
	void serviceReaderNotificationEvent(Object sender, LLRPEventArgs<ReaderEventNotification> e) {
		if (e.getMessage().getReaderEventNotificationData().getGpiEvent() != null) {
			Pin pin = new Pin(e.getMessage().getReaderEventNotificationData().getGpiEvent().getGpiPortNumber(), Pin.Type.INPUT);
			Map<Integer, havis.middleware.ale.base.operation.port.result.Result> results = new HashMap<Integer, havis.middleware.ale.base.operation.port.result.Result>();
			byte readData = (byte) (e.getMessage().getReaderEventNotificationData().getGpiEvent().isState() ? 1 : 0);
			havis.middleware.ale.base.operation.port.result.ReadResult result = new havis.middleware.ale.base.operation.port.result.ReadResult(
					havis.middleware.ale.base.operation.port.result.Result.State.SUCCESS, readData);
			results.put(0, result);

			Port port = new Port(pin, null, results);

			this.onObservationPortEvent(port);
		}
	}

	/**
	 * Method to remove all specs from the reader.
	 * 
	 * @throws LLRPException
	 */
	private void removeSpecsFromReader() throws LLRPException {
		// AccessSpecs Entfernen
		GetAccessSpecsResponse getAccessSpecsResponse = this.service.getAccessSpecs(new GetAccessSpecs(new MessageHeader((byte) 0, this.version, IDGenerator
				.getUniqueMessageID())));

		if (getAccessSpecsResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("RemoveSpecsFromReader: " + getAccessSpecsResponse.getStatus().getStatusCode().toString() + ": "
					+ getAccessSpecsResponse.getStatus().getErrorDescription());
		}

		for (AccessSpec accessSpec : getAccessSpecsResponse.getAccessSpecList()) {
			DeleteAccessSpecResponse deleteAccessSpecResponse = this.service.deleteAccessSpec(new DeleteAccessSpec(new MessageHeader((byte) 0, this.version,
					IDGenerator.getUniqueMessageID()), accessSpec.getAccessSpecId()));

			if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
				throw new LLRPException("RemoveSpecsFromReader: " + deleteAccessSpecResponse.getStatus().getStatusCode().toString() + ": "
						+ deleteAccessSpecResponse.getStatus().getErrorDescription());
			}
		}

		// ROSpecs Entfernen
		GetROSpecsResponse getROSpecsResponse = this.service.getROSpecs(new GetROSpecs(new MessageHeader((byte) 0, this.version, IDGenerator
				.getUniqueMessageID())));

		if (getROSpecsResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("RemoveSpecsFromReader: " + getROSpecsResponse.getStatus().getStatusCode().toString() + ": "
					+ getROSpecsResponse.getStatus().getErrorDescription());
		}

		for (ROSpec roSpec : getROSpecsResponse.getRoSpecList()) {
			DeleteROSpecResponse deleteROSpecResponse = this.service.deleteROSpec(new DeleteROSpec(new MessageHeader((byte) 0, this.version, IDGenerator
					.getUniqueMessageID()), roSpec.getRoSpecID()));

			if (deleteROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
				throw new LLRPException("RemoveSpecsFromReader: " + deleteROSpecResponse.getStatus().getStatusCode().toString() + ": "
						+ deleteROSpecResponse.getStatus().getErrorDescription());
			}
		}
	}

	/**
	 * Method to get a Middleware Tag Result List from an LLRP Operation Result
	 * List
	 * 
	 * @param opSecResults
	 *            The given LLRP Operation Result List
	 * @return A Middleware Tag Result List
	 */
	private Map<Integer, Result> getTagResults(List<Parameter> opSecResults) {
		Map<Integer, Result> tagResults = new HashMap<Integer, Result>();

		for (Parameter opSpecResult : opSecResults) {
			switch (opSpecResult.getParameterHeader().getParameterType()) {
			case C1G2_READ_OP_SPEC_RESULT:
				ReadResult readResult = new ReadResult();
				readResult.setData(((C1G2ReadOpSpecResult) opSpecResult).getReadData());

				// Transform Result State
				switch (((C1G2ReadOpSpecResult) opSpecResult).getResult()) {
				case SUCCESS:
					readResult.setState(ResultState.SUCCESS);
					break;
				case NON_SPECIFIC_TAG_ERROR:
				case NO_RESPONSE_FROM_TAG:
				case NON_SPECIFIC_READER_ERROR:
					readResult.setState(ResultState.MISC_ERROR_TOTAL);
					break;
				case MEMORY_OVERRUN_ERROR:
					readResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
					break;
				case MEMORY_LOCKED_ERROR:
					readResult.setState(ResultState.PERMISSION_ERROR);
					break;
				case INCORRECT_PASSWORD_ERROR:
					readResult.setState(ResultState.PASSWORD_ERROR);
					break;
				default:
					break;
				}
				tagResults.put(Integer.valueOf(((C1G2ReadOpSpecResult) opSpecResult).getOpSpecID()), readResult);
				break;
			case C1G2_WRITE_OP_SPEC_RESULT:
				WriteResult writeResult = new WriteResult();
				// Transform Result State
				switch (((C1G2WriteOpSpecResult) opSpecResult).getResult()) {
				case SUCCESS:
					writeResult.setState(ResultState.SUCCESS);
					break;
				case TAG_MEMORY_OVERRUN_ERROR:
					writeResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
					break;
				case TAG_MEMORY_LOCKED_ERROR:
					writeResult.setState(ResultState.PERMISSION_ERROR);
					break;
				case INSUFFICIENT_POWER_TO_PERFORM_MEMORY_WRITE_OPERATION:
				case NON_SPECIFIC_TAG_ERROR:
				case NO_RESPONSE_FROM_TAG:
				case NON_SPECIFIC_READER_ERROR:
					writeResult.setState(ResultState.MISC_ERROR_TOTAL);
					break;
				case INCORRECT_PASSWORD_ERROR:
					writeResult.setState(ResultState.PASSWORD_ERROR);
					break;
				default:
					break;
				}
				writeResult.setNumWordsWritten(((C1G2WriteOpSpecResult) opSpecResult).getNumWordsWritten());
				tagResults.put(Integer.valueOf(((C1G2WriteOpSpecResult) opSpecResult).getOpSpecID()), writeResult);
				break;
			case C1G2_KILL_OP_SPEC_RESULT:
				KillResult killResult = new KillResult();
				// Transform Result State
				switch (((C1G2KillOpSpecResult) opSpecResult).getResult()) {
				case SUCCESS:
					killResult.setState(ResultState.SUCCESS);
					break;
				case ZERO_KILL_PASSWORD_ERROR:
					killResult.setState(ResultState.PASSWORD_ERROR);
					break;
				case INSUFFICIENT_POWER_TO_PERFORM_KILL_OPERATION:
					killResult.setState(ResultState.MISC_ERROR_PARTIAL);
					break;
				case NON_SPECIFIC_TAG_ERROR:
				case NO_RESPONSE_FROM_TAG:
				case NON_SPECIFIC_READER_ERROR:
					killResult.setState(ResultState.MISC_ERROR_TOTAL);
					break;
				case INCORRECT_PASSWORD_ERROR:
					killResult.setState(ResultState.PASSWORD_ERROR);
					break;
				default:
					break;
				}
				tagResults.put(Integer.valueOf(((C1G2KillOpSpecResult) opSpecResult).getOpSpecID()), killResult);
				break;
			case C1G2_LOCK_OP_SPEC_RESULT:
				LockResult lockResult = new LockResult();

				// Transform Result State
				switch (((C1G2LockOpSpecResult) opSpecResult).getResult()) {
				case SUCCESS:
					lockResult.setState(ResultState.SUCCESS);
					break;
				case INSUFFICIENT_POWER_TO_PERFORM_LOCK_OPERATION:
				case NON_SPECIFIC_TAG_ERROR:
				case NO_RESPONSE_FROM_TAG:
				case NON_SPECIFIC_READER_ERROR:
					lockResult.setState(ResultState.MISC_ERROR_TOTAL);
					break;
				case INCORRECT_PASSWORD_ERROR:
					lockResult.setState(ResultState.PASSWORD_ERROR);
					break;
				case TAG_MEMORY_OVERRUN_ERROR:
					lockResult.setState(ResultState.MEMORY_OVERFLOW_ERROR);
					break;
				case TAG_MEMORY_LOCKED_ERROR:
					lockResult.setState(ResultState.PERMISSION_ERROR);
					break;
				default:
					break;
				}
				tagResults.put(Integer.valueOf(((C1G2LockOpSpecResult) opSpecResult).getOpSpecID()), lockResult);
				break;
			default:
				break;
			}
		}
		return tagResults;
	}

	/**
	 * Template method to determinate the llrp protocol version.
	 * 
	 * @throws LLRPException
	 */
	protected abstract void negotiateProtocolVersion() throws LLRPException;

	/**
	 * Template method to Update the inventory access spec on the reader.
	 * 
	 * @param accessSpec
	 *            The access spec
	 * @throws LLRPException
	 */
	protected abstract void updateInventoryAccessSpec(AccessSpec accessSpec) throws LLRPException;

	/**
	 * Template method to add on execute access spec to the reader.
	 * 
	 * @param accessSpec
	 *            The access spec to update
	 * @throws LLRPException
	 */
	protected abstract void addExecuteAccessSpec(AccessSpec accessSpec) throws LLRPException;

	/**
	 * Template method to remove the execute reader operation spec on the
	 * 
	 * @param id
	 *            The id of the spec to remove
	 * @throws LLRPException
	 */
	protected abstract void removeExecuteReaderOperationSpec(long id) throws LLRPException;

	/**
	 * Method to inform Subscriber about no data received event occurred.
	 * 
	 * @param e
	 *            event arguments
	 */
	public void onNoDataReceivedEvent(EventObject e) {
		if (noDataReceivedEvent != null) {
			noDataReceivedEvent.handleEvent(this, e);
		}
	}

	public static class TagEventArgs extends EventObject {
		private static final long serialVersionUID = -4443642923291955446L;
		private long id;
		private Tag tag;
		private List<Parameter> opSpecList = new ArrayList<>();

		public TagEventArgs(long id, Tag tag, Object o) {
			super(o);

			this.id = id;
			this.tag = tag;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public Tag getTag() {
			return tag;
		}

		public List<Parameter> getOpSpecList() {
			return opSpecList;
		}

		public void setOpSpecList(List<Parameter> opSpecList) {
			this.opSpecList = opSpecList;
		}
	}

	/**
	 * Method to inform Subscribes about an Inventory Tag Event occurred.
	 * 
	 * @param tag
	 *            The Tag of the Inventory Event
	 */
	public void onInventoryTagEvent(Tag tag, List<Parameter> opSpecList) {
		if (inventoryTagEvent != null) {
			TagEventArgs eventArgs = new TagEventArgs(0, tag, this);
			eventArgs.setOpSpecList(opSpecList);

			inventoryTagEvent.handleEvent(this, eventArgs);
		}
	}

	/**
	 * Method to inform Subscribes about an Execute Tag Event occurred.
	 * 
	 * @param id
	 *            The id of the execute operation
	 * @param tag
	 *            The Tag of the Execute Event
	 * @throws LLRPException
	 */
	public void onExecuteTagEvent(long id, Tag tag) throws LLRPException {
		synchronized (this.syncExecute) {
			if (this.executeROSpec != null && this.executeROSpec.getRoSpecID() == id) {
				// Remove execute Tag Operation
				this.removeExecuteTagOperation(id);
				// Deliever execute Tag operation result
				if (executeTagEvent != null) {
					executeTagEvent.handleEvent(this, new TagEventArgs(id, tag, this));
				}
			}
		}
	}

	public static class PortEventArgs extends EventObject {
		private static final long serialVersionUID = 6225560249662311970L;
		private long id;
		private Port port;

		public PortEventArgs(long id, Port port, Object o) {
			super(o);

			this.id = id;
			this.port = port;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public Port getPort() {
			return port;
		}
	}

	/**
	 * Method to inform Subscribes about an Observation Port Event occurred.
	 * 
	 * @param port
	 *            The Port of the Observation Event
	 */
	public void onObservationPortEvent(Port port) {
		if (observationPortEvent != null) {
			observationPortEvent.handleEvent(this, new PortEventArgs(0, port, this));
		}
	}

	/**
	 * Method to inform Subscribes about an Eexcute Port Event occurred.
	 * 
	 * @param id
	 *            The id of the execute operation
	 * @param port
	 *            The Port of the Execute Event
	 */
	public void onExecutePortEvent(final long id, final Port port) {
		if (executePortEvent != null) {
			executePortEvent.handleEvent(this, new PortEventArgs(id, port, this));
		}
	}

	/**
	 * Disposes this instance.
	 * 
	 * @throws IOException
	 */
	public void dispose() throws IOException {
		this.dispose(true);
	}

	/**
	 * Disposes this instance. According to {@link disposing} also managed
	 * resources will be disposed.
	 * 
	 * @param disposing
	 *            Indicator if also managed resources should be dispoed.
	 * @throws IOException
	 */
	protected void dispose(boolean disposing) throws IOException {
		if (!this.isDisposed) {
			if (disposing) {
				if (this.service != null)
					this.service.dispose();
			}
			this.readerConfiguration = null;
			this.service = null;
			this.isDisposed = true;
		}
	}
}

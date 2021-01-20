package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.AddAccessSpec;
import havis.llrpservice.data.message.AddAccessSpecResponse;
import havis.llrpservice.data.message.AddROSpec;
import havis.llrpservice.data.message.AddROSpecResponse;
import havis.llrpservice.data.message.DeleteAccessSpec;
import havis.llrpservice.data.message.DeleteAccessSpecResponse;
import havis.llrpservice.data.message.DeleteROSpec;
import havis.llrpservice.data.message.DeleteROSpecResponse;
import havis.llrpservice.data.message.EnableAccessSpec;
import havis.llrpservice.data.message.EnableAccessSpecResponse;
import havis.llrpservice.data.message.EnableROSpec;
import havis.llrpservice.data.message.EnableROSpecResponse;
import havis.llrpservice.data.message.GetAccessSpecs;
import havis.llrpservice.data.message.GetAccessSpecsResponse;
import havis.llrpservice.data.message.GetSupportedVersion;
import havis.llrpservice.data.message.GetSupportedVersionResponse;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.SetProtocolVersion;
import havis.llrpservice.data.message.SetProtocolVersionResponse;
import havis.llrpservice.data.message.StartROSpec;
import havis.llrpservice.data.message.StartROSpecResponse;
import havis.llrpservice.data.message.parameter.AISpecEventType;
import havis.llrpservice.data.message.parameter.AccessSpec;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.ROSpecEventType;
import havis.middleware.ale.base.operation.tag.Sighting;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler;
import havis.middleware.reader.llrp.service.exception.LLRPErrorException;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.IDGenerator;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that provides base functionality to manage communication with LLRP
 * readers.
 */
public class LLRPManager extends ALLRPManager {
	/**
	 * Creates a new Instance of Havis.Middleware.Reader.LLRPManager.
	 */
	public LLRPManager(ErrorHandler errorHandler) {
		super(errorHandler);
	}

	/**
	 * Method to determinate the llrp protocol version.
	 * 
	 * @throws LLRPException
	 */
	@Override
	protected void negotiateProtocolVersion() throws LLRPException {
		try {
			// Get Supported Version
			GetSupportedVersionResponse getSupportedVersionResponse = this.service
					.getSupportedVersion(new GetSupportedVersion(
							new MessageHeader((byte) 0,
									ProtocolVersion.LLRP_V1_1, IDGenerator
											.getUniqueMessageID())));
			if (getSupportedVersionResponse.getStatus().getStatusCode() == LLRPStatusCode.M_SUCCESS) {
				// Set Protocol Version
				this.version = getSupportedVersionResponse
						.getSupportedVersion();
				SetProtocolVersionResponse setProtocolVersionResponse = this.service
						.setProtocolVersion(new SetProtocolVersion(
								new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()),
								this.version));

				if (setProtocolVersionResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
					throw new LLRPException("Connect: "
							+ setProtocolVersionResponse.getStatus()
									.getStatusCode().toString()
							+ ": "
							+ setProtocolVersionResponse.getStatus()
									.getErrorDescription());
			} else
				throw new LLRPException("Connect: "
						+ getSupportedVersionResponse.getStatus()
								.getStatusCode().toString()
						+ ": "
						+ getSupportedVersionResponse.getStatus()
								.getErrorDescription());
		} catch (LLRPErrorException e) {
			// If Get Supported Version Message is not Supported by the LLRP
			// Reader
			if (e.getLLRPError().getStatus().getStatusCode() == LLRPStatusCode.M_UNSUPPORTED_VERSION) {
				this.version = ProtocolVersion.LLRP_V1_0_1;
			} else
				throw e;
		}
	}

	/**
	 * Method to update the inventory access spec on the reader.
	 * 
	 * @param accessSpec
	 * @throws LLRPException
	 */
	@Override
	protected void updateInventoryAccessSpec(AccessSpec accessSpec)
			throws LLRPException {
		// If LLRP Reader can handle 2 or more Access Specs at time.
		if (this.readerCapabilities.getLlrpCap().getMaxNumAccessSpecs() == 0
				|| this.readerCapabilities.getLlrpCap().getMaxNumAccessSpecs() >= 2) {
			if (this.getInventoryAccessSpec() != null)
				accessSpec.setAccessSpecId((this.getInventoryAccessSpec()
						.getAccessSpecId() == 1 ? 2 : 1));
			// Add new inventory access spec
			AddAccessSpecResponse addAccessSpecResponse = this.service
					.addAccessSpec(new AddAccessSpec(new MessageHeader(
							(byte) 0, this.version, IDGenerator
									.getUniqueMessageID()), accessSpec));

			if (addAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
				throw new LLRPException("UpdateInventoryAccessSpec: "
						+ addAccessSpecResponse.getStatus().getStatusCode()
								.toString()
						+ ": "
						+ addAccessSpecResponse.getStatus()
								.getErrorDescription());

			EnableAccessSpecResponse enableAccessSpecResponse = this.service
					.enableAccessSpec(new EnableAccessSpec(new MessageHeader(
							(byte) 0, this.version, IDGenerator
									.getUniqueMessageID()), accessSpec
							.getAccessSpecId()));
			if (enableAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
				throw new LLRPException("UpdateInventoryAccessSpec: "
						+ enableAccessSpecResponse.getStatus().getStatusCode()
								.toString()
						+ ": "
						+ enableAccessSpecResponse.getStatus()
								.getErrorDescription());

			// Delete old inventory if needed access spec
			if (this.getInventoryAccessSpec() != null) {
				DeleteAccessSpecResponse deleteAccessSpecResponse = this.service
						.deleteAccessSpec(new DeleteAccessSpec(
								new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), this
										.getInventoryAccessSpec()
										.getAccessSpecId()));
				if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
					throw new LLRPException("UpdateInventory: "
							+ deleteAccessSpecResponse.getStatus()
									.getStatusCode().toString()
							+ ": "
							+ deleteAccessSpecResponse.getStatus()
									.getErrorDescription());
			}
			this.setInventoryAccessSpec(accessSpec);
		}
		// If LLRP Reader can handle only 1 Access Spec at time.
		else {
			// Delete old inventory if needed access spec
			if (this.getInventoryAccessSpec() != null) {
				DeleteAccessSpecResponse deleteAccessSpecResponse = this.service
						.deleteAccessSpec(new DeleteAccessSpec(
								new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), this
										.getInventoryAccessSpec()
										.getAccessSpecId()));
				if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
					throw new LLRPException("UpdateInventory: "
							+ deleteAccessSpecResponse.getStatus()
									.getStatusCode().toString()
							+ ": "
							+ deleteAccessSpecResponse.getStatus()
									.getErrorDescription());
			}

			// Add new inventory access spec
			AddAccessSpecResponse addAccessSpecResponse = this.service
					.addAccessSpec(new AddAccessSpec(new MessageHeader(
							(byte) 0, this.version, IDGenerator
									.getUniqueMessageID()), accessSpec));
			if (addAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
				throw new LLRPException("UpdateInventoryAccessSpec: "
						+ addAccessSpecResponse.getStatus().getStatusCode()
								.toString()
						+ ": "
						+ addAccessSpecResponse.getStatus()
								.getErrorDescription());

			EnableAccessSpecResponse enableAccessSpecResponse = this.service
					.enableAccessSpec(new EnableAccessSpec(new MessageHeader(
							(byte) 0, this.version, IDGenerator
									.getUniqueMessageID()), accessSpec
							.getAccessSpecId()));
			if (enableAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
				throw new LLRPException("UpdateInventoryAccessSpec: "
						+ enableAccessSpecResponse.getStatus().getStatusCode()
								.toString()
						+ ": "
						+ enableAccessSpecResponse.getStatus()
								.getErrorDescription());
		}
	}

	/**
	 * Method to add on execute access spec to the reader.
	 * 
	 * @param accessSpec
	 *            The access spec to update
	 * @throws LLRPException
	 */
	@Override
	protected void addExecuteAccessSpec(AccessSpec accessSpec)
			throws LLRPException {
		boolean multipleSpecs = true;

		// If LLRP reader can not handle multiple specs
		if (!((this.getReaderCapabilities().getLlrpCap().getMaxNumROSpecs() == 0 || this
				.getReaderCapabilities().getLlrpCap().getMaxNumROSpecs() >= 2) && (this
				.getReaderCapabilities().getLlrpCap().getMaxNumAccessSpecs() == 0 || this
				.getReaderCapabilities().getLlrpCap().getMaxNumAccessSpecs() >= 3))) {
			multipleSpecs = false;
			// Unassign inventory specs
			this.unassign(true);
		}

		// Add execute reader operation and access spec
		AddROSpecResponse addROSpecResponse = this.service
				.addROSpec(new AddROSpec(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID()),
						this.executeROSpec));
		if (addROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("UpdateExecuteAccessSpec: "
					+ addROSpecResponse.getStatus().getStatusCode().toString()
					+ ": "
					+ addROSpecResponse.getStatus().getErrorDescription());
		}

		EnableROSpecResponse enableROSpecResponse = this.service
				.enableROSpec(new EnableROSpec(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID()),
						this.executeROSpec.getRoSpecID()));
		if (enableROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("UpdateExecuteAccessSpec: "
					+ enableROSpecResponse.getStatus().getStatusCode()
							.toString() + ": "
					+ enableROSpecResponse.getStatus().getErrorDescription());
		}

		AddAccessSpecResponse addAccessSpecResponse = this.service
				.addAccessSpec(new AddAccessSpec(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID()),
						accessSpec));
		if (addAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("UpdateExecuteAccessSpec: "
					+ addAccessSpecResponse.getStatus().getStatusCode()
							.toString() + ": "
					+ addAccessSpecResponse.getStatus().getErrorDescription());
		}

		EnableAccessSpecResponse enableAccessSpecResponse = this.service
				.enableAccessSpec(new EnableAccessSpec(new MessageHeader(
						(byte) 0, this.version, IDGenerator
								.getUniqueMessageID()), accessSpec
						.getAccessSpecId()));
		if (enableAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("UpdateExecuteAccessSpec: "
					+ enableAccessSpecResponse.getStatus().getStatusCode()
							.toString()
					+ ": "
					+ enableAccessSpecResponse.getStatus()
							.getErrorDescription());
		}

		// If LLRP Reader can handle multiple specs remove stop inventory specs
		if (multipleSpecs) {
			this.stop(true);
		}

		final long executeROSpecID = this.executeROSpec.getRoSpecID();
		// Indicates of event was already received
		final AtomicBoolean eventReceived = new AtomicBoolean(false);
		// Delegate to handle notification send by the LLRP Service
		final boolean tmpMultipleSpecs = multipleSpecs;

		LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>> event = new LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>>() {
			@Override
			public void fire(Object sender,
					LLRPEventArgs<ReaderEventNotification> e) {
				notificationEvent(sender, e, executeROSpecID, eventReceived,
						tmpMultipleSpecs, this);
			}
		};

		this.service.getReaderNotificationEvent().add(event);

		StartROSpecResponse startROSpecResponse = this.service
				.startROSpec(new StartROSpec(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID()),
						this.executeROSpec.getRoSpecID()));
		if (startROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
			throw new LLRPException("Start: "
					+ startROSpecResponse.getStatus().getStatusCode()
							.toString() + ": "
					+ startROSpecResponse.getStatus().getErrorDescription());
	}

	/**
	 * 
	 * @param sender
	 * @param e
	 * @param executeROSpecID
	 * @param eventReceived
	 * @param multipleSpecs
	 * @param ev
	 */
	private void notificationEvent(
			Object sender,
			LLRPEventArgs<ReaderEventNotification> e,
			long executeROSpecID,
			AtomicBoolean eventReceived,
			boolean multipleSpecs,
			LLRPEventHandler.LLRPEvent<LLRPEventArgs<ReaderEventNotification>> ev) {
		try {
			synchronized (LLRPManager.this.syncExecute) {
				// If execute spec was deleted restart inventory specs
				if (!eventReceived.get()
						&& e.getMessage().getReaderEventNotificationData()
								.getRoSpecEvent() != null
						&& e.getMessage().getReaderEventNotificationData()
								.getRoSpecEvent().getEventType() == ROSpecEventType.END_OF_ROSPEC
						&& e.getMessage().getReaderEventNotificationData()
								.getRoSpecEvent().getRoSpecID() == executeROSpecID) {
					eventReceived.set(true);

					this.service.getReaderNotificationEvent().remove(ev);

					// only restart/reassign inventory if no other execute is running/scheduled
					if (this.executeROSpec == null || this.executeROSpec.getRoSpecID() == executeROSpecID) {
						if (!multipleSpecs) {
							this.assign(true);
						}

						this.start(true);
					}
				}
				// If execute spec ended without result send error result and
				// restart inventory specs
				if (!eventReceived.get()
						&& e.getMessage().getReaderEventNotificationData()
								.getAiSpecEvent() != null
						&& e.getMessage().getReaderEventNotificationData()
								.getAiSpecEvent().getEventType() == AISpecEventType.END_OF_AISPEC
						&& e.getMessage().getReaderEventNotificationData()
								.getAiSpecEvent().getRoSpecID() == executeROSpecID) {
					eventReceived.set(true);

					this.service.getReaderNotificationEvent().remove(ev);

					if (this.executeROSpec != null
							&& (this.executeROSpec.getRoSpecID() == executeROSpecID)) {
						// Create and send error result
						Tag tag = new Tag((byte[]) null);
						tag.setResult(new HashMap<Integer, Result>());
						tag.setSighting(new Sighting("", (short) 0, 0, tag.getFirstTime()));

						this.onExecuteTagEvent(
								this.executeROSpec.getRoSpecID(), tag);
					}

					if (!multipleSpecs) {
						this.assign(true);
					}

					this.start(true);
				}
			}
		} catch (LLRPException ex) {
			errorHandler.notifyError(ex);
		}
	}

	/**
	 * Method to remove the execute reader operation spec on the reader.
	 * 
	 * @param id
	 *            The id of the spec to remove
	 * @throws LLRPException
	 */
	@Override
	protected void removeExecuteReaderOperationSpec(long id)
			throws LLRPException {
		// Delete execute access Spec
		GetAccessSpecsResponse getAccessSpecsResponse = this.service
				.getAccessSpecs(new GetAccessSpecs(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID())));
		if (getAccessSpecsResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
			throw new LLRPException("RemoveExecuteReaderOperationSpec: "
					+ getAccessSpecsResponse.getStatus().getStatusCode()
							.toString() + ": "
					+ getAccessSpecsResponse.getStatus().getErrorDescription());

		for (AccessSpec accessSpec : getAccessSpecsResponse.getAccessSpecList()) {
			if (accessSpec.getAccessSpecId() == id) {
				DeleteAccessSpecResponse deleteAccessSpecResponse = this.service
						.deleteAccessSpec(new DeleteAccessSpec(
								new MessageHeader((byte) 0, this.version,
										IDGenerator.getUniqueMessageID()), id));
				if (deleteAccessSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
					throw new LLRPException(
							"RemoveExecuteReaderOperationSpec: "
									+ deleteAccessSpecResponse.getStatus()
											.getStatusCode().toString()
									+ ": "
									+ deleteAccessSpecResponse.getStatus()
											.getErrorDescription());
			}
		}

		// Delete execute reader operation spec
		DeleteROSpecResponse deleteROSpecResponse = this.service
				.deleteROSpec(new DeleteROSpec(new MessageHeader((byte) 0,
						this.version, IDGenerator.getUniqueMessageID()), id));
		if (deleteROSpecResponse.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS)
			throw new LLRPException("RemoveExecuteReaderOperationSpec: "
					+ deleteROSpecResponse.getStatus().getStatusCode()
							.toString() + ": "
					+ deleteROSpecResponse.getStatus().getErrorDescription());
	}
}

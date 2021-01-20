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
import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.GetReaderConfig;
import havis.llrpservice.data.message.GetReaderConfigRequestedData;
import havis.llrpservice.data.message.GetReaderConfigResponse;
import havis.llrpservice.data.message.GetSupportedVersion;
import havis.llrpservice.data.message.GetSupportedVersionResponse;
import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.MessageTypes.MessageType;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.SetProtocolVersion;
import havis.llrpservice.data.message.SetProtocolVersionResponse;
import havis.llrpservice.data.message.SetReaderConfig;
import havis.llrpservice.data.message.SetReaderConfigResponse;
import havis.llrpservice.data.message.StartROSpec;
import havis.llrpservice.data.message.StartROSpecResponse;
import havis.llrpservice.data.message.StopROSpec;
import havis.llrpservice.data.message.StopROSpecResponse;
import havis.llrpservice.data.message.parameter.AISpecEvent;
import havis.llrpservice.data.message.parameter.AISpecEventType;
import havis.llrpservice.data.message.parameter.AccessCommand;
import havis.llrpservice.data.message.parameter.AccessSpec;
import havis.llrpservice.data.message.parameter.AccessSpecStopTrigger;
import havis.llrpservice.data.message.parameter.AccessSpecStopTriggerType;
import havis.llrpservice.data.message.parameter.AntennaId;
import havis.llrpservice.data.message.parameter.C1G2CRC;
import havis.llrpservice.data.message.parameter.C1G2KillOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2KillOpSpecResultValues;
import havis.llrpservice.data.message.parameter.C1G2Lock;
import havis.llrpservice.data.message.parameter.C1G2LockOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2LockOpSpecResultValues;
import havis.llrpservice.data.message.parameter.C1G2LockPayload;
import havis.llrpservice.data.message.parameter.C1G2LockPayloadDataField;
import havis.llrpservice.data.message.parameter.C1G2LockPayloadPrivilege;
import havis.llrpservice.data.message.parameter.C1G2ReadOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2ReadOpSpecResultValues;
import havis.llrpservice.data.message.parameter.C1G2TagSpec;
import havis.llrpservice.data.message.parameter.C1G2TargetTag;
import havis.llrpservice.data.message.parameter.C1G2WriteOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2WriteOpSpecResultValues;
import havis.llrpservice.data.message.parameter.ConnectionAttemptEvent;
import havis.llrpservice.data.message.parameter.ConnectionAttemptEventStatusType;
import havis.llrpservice.data.message.parameter.EPC96;
import havis.llrpservice.data.message.parameter.EPCData;
import havis.llrpservice.data.message.parameter.GPIEvent;
import havis.llrpservice.data.message.parameter.GPIPortCurrentState;
import havis.llrpservice.data.message.parameter.GPIPortCurrentStateGPIState;
import havis.llrpservice.data.message.parameter.GPOWriteData;
import havis.llrpservice.data.message.parameter.LLRPCapabilities;
import havis.llrpservice.data.message.parameter.LLRPStatus;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.Parameter;
import havis.llrpservice.data.message.parameter.ProtocolId;
import havis.llrpservice.data.message.parameter.ROSpec;
import havis.llrpservice.data.message.parameter.ROSpecCurrentState;
import havis.llrpservice.data.message.parameter.ROSpecEvent;
import havis.llrpservice.data.message.parameter.ROSpecEventType;
import havis.llrpservice.data.message.parameter.ROSpecID;
import havis.llrpservice.data.message.parameter.ReaderEventNotificationData;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.llrpservice.data.message.parameter.TVParameterHeader;
import havis.llrpservice.data.message.parameter.TagReportData;
import havis.llrpservice.data.message.parameter.UTCTimestamp;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.operation.port.Operation.Type;
import havis.middleware.ale.base.operation.port.Pin;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.tag.Field;
import havis.middleware.ale.base.operation.tag.Filter;
import havis.middleware.ale.base.operation.tag.Operation;
import havis.middleware.ale.base.operation.tag.OperationType;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.reader.Property.Connector;
import havis.middleware.reader.llrp.ALLRPManager.PortEventArgs;
import havis.middleware.reader.llrp.LLRPConfiguration.LLRPConfig;
import havis.middleware.reader.llrp.LLRPInventoryOperation.UserReadMode;
import havis.middleware.reader.llrp.client.LLRPClient;
import havis.middleware.reader.llrp.client.LLRPConnection;
import havis.middleware.reader.llrp.client.LLRPProperties.PropertyName;
import havis.middleware.reader.llrp.service.LLRPMessageHandler;
import havis.middleware.reader.llrp.service.LLRPService;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.BitConverter;
import havis.middleware.reader.llrp.util.IDGenerator;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class LLRPManagerTest implements ErrorHandler {

	protected static final AccessSpecStopTriggerType AccessSpecStopTriggerType = null;

	@Test
	public void checkConnectFail(final @Mocked LLRPService llrpService) throws LLRPException, IOException, ValidationException {
		final LLRPManager llrpManager = new LLRPManager(this);

		new NonStrictExpectations() {
			{
				llrpService.openConnection(null);
				result = true;
			}
		};

		boolean result = llrpManager.connect();

		Assert.assertFalse(result);

		new Verifications() {
			{
				llrpService.closeConnection();
				times = 1;
			}
		};

		new Verifications() {
			{
				llrpService.closeConnection();
				times = 1;
			}
		};
	}

	@Test
	public void checkConnectFail(final @Mocked LLRPClient llrpClient) throws LLRPException, IOException, ValidationException {
		final LLRPManager llrpManager = new LLRPManager(this);
		LLRPConnection connection = LLRPConnection.validateConnectorProperties(getProperties());

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public boolean openConnection(LLRPConnection llrpConnection) {
				return true;
			}
		};

		llrpManager.setConnectorConfiguration(connection);
		boolean result = llrpManager.connect();

		Assert.assertFalse(result);

		new Verifications() {
			{
				llrpClient.closeConnection();
				times = 1;
			}
		};
	}

	@Test
	public void checkConnectFail() throws LLRPException, IOException, ValidationException, InterruptedException {
		final LLRPManager llrpManager = new LLRPManager(this);

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public boolean openConnection(LLRPConnection llrpConnection) {
				return false;
			}
		};

		boolean value = llrpManager.connect();

		Assert.assertFalse(value);
	}

	@Test
	public void checkConnectDisconnect(final @Mocked LLRPClient llrpClient) throws LLRPException, IOException, ValidationException, InterruptedException {
		final CountDownLatch ready = new CountDownLatch(1);
		final LLRPManager llrpManager = new LLRPManager(this);
		final LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(getProperties());
		final ProtocolVersion currentVersion = ProtocolVersion.LLRP_V1_1;
		final ProtocolVersion supportedVersion = ProtocolVersion.LLRP_V1_1;
		final LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");

		new MockUp<LLRPService>() {

			@SuppressWarnings("unused")
			@Mock
			public boolean openConnection(LLRPConnection llrpConnection) {
				return true;
			}

			@SuppressWarnings("unused")
			@Mock
			public CloseConnectionResponse closeConnection(CloseConnection request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				CloseConnectionResponse closeConnectionResponse = new CloseConnectionResponse(header, status);

				return closeConnectionResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetSupportedVersionResponse getSupportedVersion(GetSupportedVersion request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				GetSupportedVersionResponse getSupportedVersionResponse = new GetSupportedVersionResponse(header, currentVersion, supportedVersion, status);
				return getSupportedVersionResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public SetProtocolVersionResponse setProtocolVersion(SetProtocolVersion request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				SetProtocolVersionResponse setProtocolVersionResponse = new SetProtocolVersionResponse(header, status);

				return setProtocolVersionResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetAccessSpecsResponse getAccessSpecs(GetAccessSpecs request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				GetAccessSpecsResponse getAccessSpecsResponse = new GetAccessSpecsResponse(header, status);

				getAccessSpecsResponse.setAccessSpecList(new ArrayList<AccessSpec>());

				return getAccessSpecsResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteAccessSpecResponse deleteAccessSpec(DeleteAccessSpec request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				DeleteAccessSpecResponse deleteAccessSpecResponse = new DeleteAccessSpecResponse(header, status);

				return deleteAccessSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetROSpecsResponse getROSpecs(GetROSpecs request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				GetROSpecsResponse getROSpecsResponse = new GetROSpecsResponse(header, status);
				getROSpecsResponse.setRoSpecList(new ArrayList<ROSpec>());

				return getROSpecsResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteROSpecResponse deleteROSpec(DeleteROSpec request) throws LLRPException {
				MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				DeleteROSpecResponse deleteROSpecResponse = new DeleteROSpecResponse(header, status);

				return deleteROSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public void enableEventsAndReports(EnableEventsAndReports request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {

			}
		};

		llrpManager.setConnectorConfiguration(llrpConnection);
		llrpManager.initialize();

		Thread waitingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ready.countDown();
				// run code that waits
				llrpManager.connect();
			}
		});
		waitingThread.start();

		boolean success = false;

		try {
			Assert.assertTrue("Thread is ready", ready.await(100, TimeUnit.MILLISECONDS));

			// thread is waiting now, let it do that for some time
			Thread.sleep(50);
			Assert.assertTrue("Thread is waiting", waitingThread.isAlive());

			// run code that makes the waiting thread continue
			ReaderEventNotification readerEventNotification = createReaderEventNotification();
			LLRPEventArgs<ReaderEventNotification> eventArgs = new LLRPEventArgs<ReaderEventNotification>(readerEventNotification);
			llrpManager.service.getReaderNotificationEvent().handleEvent(this, eventArgs);

			// wait for thread to finish
			waitingThread.join(100);

			Assert.assertFalse("Thread is not waiting", waitingThread.isAlive());

			success = true;

			LLRPConfiguration llrpConfiguration = llrpManager.llrpConfiguration;

			Assert.assertNotNull(llrpConfiguration);
			Assert.assertEquals(ProtocolVersion.LLRP_V1_1, llrpConfiguration.getVersion());
		} finally {
			if (!success) {
				// make sure the thread is stopped in case of error
				waitingThread.interrupt();
			}
		}

		Assert.assertFalse(llrpManager.service.getROAccessReportEvent().getDelegates().isEmpty());
		Assert.assertFalse(llrpManager.service.getKeepaliveEvent().getDelegates().isEmpty());
		Assert.assertFalse(llrpManager.service.getNoDataReceivedEvent().getDelegates().isEmpty());
		Assert.assertFalse(llrpManager.service.getReaderNotificationEvent().getDelegates().isEmpty());

		llrpManager.disconnect();

		Assert.assertTrue(llrpManager.service.getROAccessReportEvent().getDelegates().isEmpty());
		Assert.assertTrue(llrpManager.service.getKeepaliveEvent().getDelegates().isEmpty());
		Assert.assertTrue(llrpManager.service.getNoDataReceivedEvent().getDelegates().isEmpty());
		Assert.assertTrue(llrpManager.service.getReaderNotificationEvent().getDelegates().isEmpty());
	}

	@Test
	public void checkAddExecuteTagOperationRead(final @Mocked GetReaderCapabilitiesResponse readerCapabilities) throws ImplementationException, LLRPException,
			ValidationException {
		TagOperation tagOperation = getEmptyFilterTagOperation();
		LLRPManager llrpManager = new LLRPManager(this);

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public AddROSpecResponse addROSpec(AddROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				AddROSpecResponse addROSpecResponse = new AddROSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getRoSpec().getRoSpecID());
				Assert.assertEquals(MessageType.ADD_ROSPEC, request.getMessageHeader().getMessageType());

				return addROSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableROSpecResponse enableROSpec(EnableROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				EnableROSpecResponse enableROSpecResponse = new EnableROSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getRoSpecID());
				Assert.assertEquals(MessageType.ENABLE_ROSPEC, request.getMessageHeader().getMessageType());

				return enableROSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public AddAccessSpecResponse addAccessSpec(AddAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				AddAccessSpecResponse accessSpecResponse = new AddAccessSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getAccessSpec().getAccessSpecId());
				Assert.assertEquals(12, request.getAccessSpec().getRoSpecId());
				Assert.assertEquals(0, request.getAccessSpec().getAntennaId());
				Assert.assertEquals(ProtocolId.EPC_GLOBAL_C1G2, request.getAccessSpec().getProtocolId());
				Assert.assertEquals(false, request.getAccessSpec().isCurrentState());
				Assert.assertEquals(MessageType.ADD_ACCESSSPEC, request.getMessageHeader().getMessageType());

				return accessSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableAccessSpecResponse enableAccessSpec(EnableAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				EnableAccessSpecResponse enableAccessSpecResponse = new EnableAccessSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getAccessSpecID());
				Assert.assertEquals(MessageType.ENABLE_ACCESSSPEC, request.getMessageHeader().getMessageType());

				return enableAccessSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public StartROSpecResponse startROSpec(StartROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				StartROSpecResponse startROSpecResponse = new StartROSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getRoSpecID());
				Assert.assertEquals(MessageType.START_ROSPEC, request.getMessageHeader().getMessageType());

				return startROSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetAccessSpecsResponse getAccessSpecs(GetAccessSpecs request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				GetAccessSpecsResponse getAccessSpecsResponse = new GetAccessSpecsResponse(messageHeader, status);

				long accessSpecID = 12;
				int antennaID = 1;
				ProtocolId protocolID = ProtocolId.EPC_GLOBAL_C1G2;
				boolean currentState = true;
				long roSpecID = 1;
				int operationCountValue = 1;
				AccessSpecStopTriggerType accessSpecStopTriggerType = havis.llrpservice.data.message.parameter.AccessSpecStopTriggerType.OPERATION_COUNT;
				AccessSpecStopTrigger accessSpecStopTrigger = new AccessSpecStopTrigger(new TLVParameterHeader(), accessSpecStopTriggerType,
						operationCountValue);
				byte memoryBank = 1;
				boolean isMatch = false;
				int pointer = 1;
				BitSet tagMask = BitConverter.toBitSet(new byte[] { 12, 23 });
				BitSet tagData = BitConverter.toBitSet(new byte[] { 35, 24 });
				C1G2TargetTag tagPattern1 = new C1G2TargetTag(new TLVParameterHeader(), memoryBank, isMatch, pointer, tagMask, tagData);
				C1G2TagSpec c1g2TagSpec = new C1G2TagSpec(new TLVParameterHeader(), tagPattern1);
				List<Parameter> opSpecList = new ArrayList<>();
				AccessCommand accessCommand = new AccessCommand(new TLVParameterHeader(), c1g2TagSpec, opSpecList);
				AccessSpec accessSpec1 = new AccessSpec(new TLVParameterHeader(), accessSpecID, antennaID, protocolID, currentState, roSpecID,
						accessSpecStopTrigger, accessCommand);
				List<AccessSpec> accessSpecList = new ArrayList<>();
				accessSpecList.add(accessSpec1);

				getAccessSpecsResponse.setAccessSpecList(accessSpecList);

				Assert.assertEquals(MessageType.GET_ACCESSSPECS, request.getMessageHeader().getMessageType());

				return getAccessSpecsResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteAccessSpecResponse deleteAccessSpec(DeleteAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				DeleteAccessSpecResponse deleteAccessSpecResponse = new DeleteAccessSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getAccessSpecID());
				Assert.assertEquals(MessageType.DELETE_ACCESSSPEC, request.getMessageHeader().getMessageType());

				return deleteAccessSpecResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteROSpecResponse deleteROSpec(DeleteROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				DeleteROSpecResponse deleteROSpecResponse = new DeleteROSpecResponse(messageHeader, status);

				Assert.assertEquals(12, request.getRoSpecID());
				Assert.assertEquals(MessageType.DELETE_ROSPEC, request.getMessageHeader().getMessageType());

				return deleteROSpecResponse;
			}

		};

		llrpManager.setReaderCapabilities(readerCapabilities);
		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(getProperties());

		llrpManager.setConnectorConfiguration(llrpConnection);
		llrpManager.addExecuteTagOperation(12, tagOperation);

		tagOperation = getFilterSize2TagOperation();

		try {
			llrpManager.addExecuteTagOperation(12, tagOperation);
		} catch (ImplementationException ie) {
			Assert.assertTrue(true);
		}

		llrpManager = new LLRPManager(this);
		llrpManager.setReaderCapabilities(readerCapabilities);

		llrpManager.setConnectorConfiguration(llrpConnection);
		llrpManager.addExecuteTagOperation(12, tagOperation);

		llrpManager.removeExecuteTagOperation(12);

		Assert.assertNull(llrpManager.executeROSpec);
	}

	@Test
	public void checkAddExecuteTagOperationKill(@Mocked LLRPService llrpService, @Mocked GetReaderCapabilitiesResponse readerCapabilities)
			throws ImplementationException, ValidationException, LLRPException {
		checkAddExecuteTagOperation(readerCapabilities, llrpService, getTagOperation(OperationType.KILL, new ArrayList<Filter>()));
	}

	@Test
	public void checkAddExecuteTagOperationWrite(@Mocked LLRPService llrpService, @Mocked GetReaderCapabilitiesResponse readerCapabilities)
			throws ImplementationException, ValidationException, LLRPException {
		checkAddExecuteTagOperation(readerCapabilities, llrpService, getTagOperation(OperationType.WRITE, new ArrayList<Filter>()));
	}

	@Test
	public void checkAddExecuteTagOperationPassword(@Mocked LLRPService llrpService, @Mocked GetReaderCapabilitiesResponse readerCapabilities)
			throws ImplementationException, ValidationException, LLRPException {
		checkAddExecuteTagOperation(readerCapabilities, llrpService, getTagOperation(OperationType.PASSWORD, new ArrayList<Filter>()));
	}

	@Test
	public void checkAddExecuteTagOperationLockLock(@Mocked LLRPService llrpService, @Mocked GetReaderCapabilitiesResponse readerCapabilities)
			throws ImplementationException, ValidationException, LLRPException {
		Field bank01 = new Field("KILL", 0, 0, 32);
		Field bank02 = new Field("ACCESS", 0, 32, 32);
		Field bank1 = new Field("EPC", 1, 0, 0);
		Field bank2 = new Field("TID", 2, 0, 0);
		Field bank3 = new Field("USER", 3, 0, 0);

		OperationType operationType = OperationType.LOCK;

		Operation reserved1 = new Operation(1, operationType, bank01);
		Operation reserved2 = new Operation(1, operationType, bank02);

		Operation epc = new Operation(1, operationType, bank1);
		Operation tid = new Operation(1, operationType, bank2);
		Operation user = new Operation(1, operationType, bank3);

		reserved1.setData(new byte[] { 0 });
		reserved2.setData(new byte[] { 0 });
		epc.setData(new byte[] { 1 });
		tid.setData(new byte[] { 2 });
		user.setData(new byte[] { 3 });

		List<Operation> operations = new ArrayList<>();
		operations.add(reserved1);
		operations.add(reserved2);
		operations.add(epc);
		operations.add(tid);
		operations.add(user);

		TagOperation tagOperation = new TagOperation(operations);
		tagOperation.setFilter(new ArrayList<Filter>());

		checkAddExecuteTagOperation(readerCapabilities, llrpService, tagOperation);
	}

	@Test
	public void checkApplyReaderConfig(final @Mocked LLRPConfiguration llrpConfiguration) throws LLRPException, ValidationException {
		LLRPManager llrpManager = new LLRPManager(this);

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public GetReaderCapabilitiesResponse getReaderCapabilities(GetReaderCapabilities request) throws LLRPException {
				final MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				final LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				GetReaderCapabilitiesResponse capabilitiesResponse = new GetReaderCapabilitiesResponse(messageHeader, status);

				return capabilitiesResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetReaderConfigResponse getReaderConfig(GetReaderConfig request) throws LLRPException {
				final MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				final LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				GetReaderConfigResponse configResponse = new GetReaderConfigResponse(messageHeader, status);

				if (request.getRequestedData() == GetReaderConfigRequestedData.GPI_CURRENT_STATE) {
					List<GPIPortCurrentState> gpiPortCurrentStateList = new ArrayList<>();

					GPIPortCurrentState inputPort1 = new GPIPortCurrentState(new TLVParameterHeader(), 1, true, GPIPortCurrentStateGPIState.HIGH);
					GPIPortCurrentState inputPort2 = new GPIPortCurrentState(new TLVParameterHeader(), 2, true, GPIPortCurrentStateGPIState.HIGH);

					gpiPortCurrentStateList.add(inputPort1);
					gpiPortCurrentStateList.add(inputPort2);

					configResponse.setGpiPortCurrentStateList(gpiPortCurrentStateList);
				} else if (request.getRequestedData() == GetReaderConfigRequestedData.GPO_WRITE_DATA) {
					List<GPOWriteData> gpoWriteDataList = new ArrayList<>();
					GPOWriteData gpoWriteData1 = new GPOWriteData(new TLVParameterHeader(), 3, true);
					GPOWriteData gpoWriteData2 = new GPOWriteData(new TLVParameterHeader(), 4, true);

					gpoWriteDataList.add(gpoWriteData1);
					gpoWriteDataList.add(gpoWriteData2);

					configResponse.setGpoWriteDataList(gpoWriteDataList);
				}

				return configResponse;
			}
		};

		llrpManager.llrpConfiguration = llrpConfiguration;
		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(getProperties());
		llrpManager.setConnectorConfiguration(llrpConnection);
		llrpManager.applyReaderConfig();

		Assert.assertEquals(llrpManager.readerCapabilities.getStatus().getStatusCode(), LLRPStatusCode.M_SUCCESS);
		Assert.assertEquals(llrpManager.outputPins.size(), 2);
		Assert.assertEquals(llrpManager.inputPins.size(), 2);
	}

	private void checkAddExecuteTagOperation(final GetReaderCapabilitiesResponse readerCapabilities, final LLRPService llrpService, TagOperation tagOperation)
			throws ImplementationException, LLRPException, ValidationException {
		LLRPManager llrpManager = new LLRPManager(this);
		final MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		final LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");

		new NonStrictExpectations() {
			{
				llrpService.addROSpec((AddROSpec) withNotNull());
				result = new AddROSpecResponse(messageHeader, status);

				llrpService.enableROSpec((EnableROSpec) withNotNull());
				result = new EnableROSpecResponse(messageHeader, status);

				llrpService.addAccessSpec((AddAccessSpec) withNotNull());
				result = new AddAccessSpecResponse(messageHeader, status);

				llrpService.enableAccessSpec((EnableAccessSpec) withNotNull());
				result = new EnableAccessSpecResponse(messageHeader, status);

				llrpService.startROSpec((StartROSpec) withNotNull());
				result = new StartROSpecResponse(messageHeader, status);
			}
		};

		llrpManager.setReaderCapabilities(readerCapabilities);
		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(getProperties());

		llrpManager.setConnectorConfiguration(llrpConnection);
		llrpManager.addExecuteTagOperation(12, tagOperation);

		new Verifications() {
			{
				llrpService.startROSpec((StartROSpec) withCapture());
				times = 1;
			}
		};
	}

	@Test
	public void checkAssignStartStopUnassign(final @Mocked GetReaderCapabilitiesResponse readerCapabilities) throws LLRPException {
		final LLRPManager llrpManager = new LLRPManager(this);
		final TLVParameterHeader header = new TLVParameterHeader();
		final LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		final LLRPStatus status = new LLRPStatus(header, statusCode, "");

		llrpManager.setReaderCapabilities(readerCapabilities);

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public AddROSpecResponse addROSpec(AddROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new AddROSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableROSpecResponse enableROSpec(EnableROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new EnableROSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public AddAccessSpecResponse addAccessSpec(AddAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new AddAccessSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableAccessSpecResponse enableAccessSpec(EnableAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new EnableAccessSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public StartROSpecResponse startROSpec(StartROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new StartROSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public StopROSpecResponse stopROSpec(StopROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new StopROSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteAccessSpecResponse deleteAccessSpec(DeleteAccessSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new DeleteAccessSpecResponse(messageHeader, status);
			}

			@SuppressWarnings("unused")
			@Mock
			public DeleteROSpecResponse deleteROSpec(DeleteROSpec request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				return new DeleteROSpecResponse(messageHeader, status);
			}
		};

		try {
			llrpManager.assign(false);
			Assert.assertTrue(true);
		} catch (LLRPException exc) {
			Assert.assertTrue(false);
		}

		LLRPInventoryOperation inventoryOperation = new LLRPInventoryOperation();
		inventoryOperation.setEpc(true);
		inventoryOperation.setReserved(true);
		inventoryOperation.setTid(true);
		inventoryOperation.setUser(UserReadMode.ON);
		inventoryOperation.setUserDataBlocks(new boolean[0]);
		llrpManager.updateInventory(inventoryOperation);

		try {
			llrpManager.start(false);
			Assert.assertTrue(true);
		} catch (LLRPException exc) {
			Assert.assertTrue(false);
		}

		try {
			llrpManager.stop(false);
			Assert.assertTrue(true);
		} catch (LLRPException exc) {
			Assert.assertTrue(false);
		}

		try {
			llrpManager.unassign(false);
			Assert.assertTrue(true);
		} catch (LLRPException exc) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void checkValidateExecuteTagOperationLock() {
		Field bank0 = new Field("RESERVED", 0, 0, 32);
		Field bank1 = new Field("EPC", 1, 0, 0);
		Field bank2 = new Field("TID", 2, 0, 0);
		Field bank3 = new Field("USER", 3, 0, 0);

		OperationType operationType = OperationType.LOCK;
		Operation reserved = new Operation(1, operationType, bank0);
		Operation epc = new Operation(1, operationType, bank1);
		Operation tid = new Operation(1, operationType, bank2);
		Operation user = new Operation(1, operationType, bank3);

		reserved.setData(new byte[] { 0, 13, 14, 15, 16, 17 });
		epc.setData(new byte[] { 1, 19, 20, 21, 22, 23 });
		tid.setData(new byte[] { 2, 25, 26, 27, 28, 29 });
		user.setData(new byte[] { 3, 31, 32, 33, 34, 35 });

		List<Operation> operations = new ArrayList<>();
		operations.add(reserved);
		operations.add(epc);
		operations.add(tid);
		operations.add(user);

		TagOperation operation = new TagOperation(operations);
		operation.setFilter(new ArrayList<Filter>());

		LLRPManager llrpManager = new LLRPManager(this);
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		TLVParameterHeader tlvheader = new TLVParameterHeader();
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		String errorDescription = "";
		LLRPStatus status = new LLRPStatus(tlvheader, statusCode, errorDescription);

		long maxNumOpSpecsPerAccessSpec = 6;
		long maxNumAccessSpecs = 3;
		long maxNumInventoryParameterSpecsPerAISpec = 3;
		long maxNumSpecsPerROSpec = 3;
		long maxNumROSpecs = 3;
		int clientRequestOpSpecTimeout = 3;
		byte maxPriorityLevelSupported = 3;
		Boolean supportsEventAndReportHolding = Boolean.valueOf("true");
		Boolean canDoTagInventoryStateAwareSingulation = Boolean.valueOf("true");
		Boolean supportsClientRequestOpSpec = Boolean.valueOf("true");
		Boolean canReportBufferFillWarning = Boolean.valueOf("true");
		Boolean canDoRFSurvey = Boolean.valueOf("true");

		LLRPCapabilities llrpCap = new LLRPCapabilities(new TLVParameterHeader(), canDoRFSurvey, canReportBufferFillWarning, supportsClientRequestOpSpec,
				canDoTagInventoryStateAwareSingulation, supportsEventAndReportHolding, maxPriorityLevelSupported, clientRequestOpSpecTimeout, maxNumROSpecs,
				maxNumSpecsPerROSpec, maxNumInventoryParameterSpecsPerAISpec, maxNumAccessSpecs, maxNumOpSpecsPerAccessSpec);
		GetReaderCapabilitiesResponse readerCapabilities = new GetReaderCapabilitiesResponse(header, status, llrpCap);
		llrpManager.setReaderCapabilities(readerCapabilities);

		LLRPReturnContainerUtil<Map<Integer, Result>> validateExecuteTagOperation = llrpManager.validateExecuteTagOperation(operation);

		Assert.assertTrue(validateExecuteTagOperation.isTrue());
	}

	@Test
	public void checkValidateExecuteTagOperationWrite() {
		Field bank1 = new Field("EPC", 1, 0, 0);

		OperationType operationType = OperationType.WRITE;
		Operation epc = new Operation(1, operationType, bank1);

		epc.setData(new byte[] { 1, 19, 20, 21, 22, 23 });

		List<Operation> operations = new ArrayList<>();
		operations.add(epc);

		TagOperation operation = new TagOperation(operations);
		operation.setFilter(new ArrayList<Filter>());

		LLRPManager llrpManager = new LLRPManager(this);
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		TLVParameterHeader tlvheader = new TLVParameterHeader();
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		String errorDescription = "";
		LLRPStatus status = new LLRPStatus(tlvheader, statusCode, errorDescription);

		long maxNumOpSpecsPerAccessSpec = 6;
		long maxNumAccessSpecs = 3;
		long maxNumInventoryParameterSpecsPerAISpec = 3;
		long maxNumSpecsPerROSpec = 3;
		long maxNumROSpecs = 3;
		int clientRequestOpSpecTimeout = 3;
		byte maxPriorityLevelSupported = 3;
		Boolean supportsEventAndReportHolding = Boolean.valueOf("true");
		Boolean canDoTagInventoryStateAwareSingulation = Boolean.valueOf("true");
		Boolean supportsClientRequestOpSpec = Boolean.valueOf("true");
		Boolean canReportBufferFillWarning = Boolean.valueOf("true");
		Boolean canDoRFSurvey = Boolean.valueOf("true");

		LLRPCapabilities llrpCap = new LLRPCapabilities(new TLVParameterHeader(), canDoRFSurvey, canReportBufferFillWarning, supportsClientRequestOpSpec,
				canDoTagInventoryStateAwareSingulation, supportsEventAndReportHolding, maxPriorityLevelSupported, clientRequestOpSpecTimeout, maxNumROSpecs,
				maxNumSpecsPerROSpec, maxNumInventoryParameterSpecsPerAISpec, maxNumAccessSpecs, maxNumOpSpecsPerAccessSpec);
		GetReaderCapabilitiesResponse readerCapabilities = new GetReaderCapabilitiesResponse(header, status, llrpCap);
		llrpManager.setReaderCapabilities(readerCapabilities);

		LLRPReturnContainerUtil<Map<Integer, Result>> validateExecuteTagOperation = llrpManager.validateExecuteTagOperation(operation);

		Assert.assertTrue(validateExecuteTagOperation.isTrue());
	}

	@Test
	public void checkValidateExecuteTagOperationLockError() {
		Field bank1 = new Field("EPC", 1, 0, 64);

		OperationType operationType = OperationType.LOCK;
		Operation epc = new Operation(1, operationType, bank1);

		epc.setId(0);
		epc.setData(new byte[] { 4, 19, 20, 21, 22, 23 });

		List<Operation> operations = new ArrayList<>();
		operations.add(epc);

		TagOperation operation = new TagOperation(operations);
		operation.setFilter(new ArrayList<Filter>());

		LLRPManager llrpManager = new LLRPManager(this);
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		TLVParameterHeader tlvheader = new TLVParameterHeader();
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		String errorDescription = "";
		LLRPStatus status = new LLRPStatus(tlvheader, statusCode, errorDescription);

		long maxNumOpSpecsPerAccessSpec = 6;
		long maxNumAccessSpecs = 3;
		long maxNumInventoryParameterSpecsPerAISpec = 3;
		long maxNumSpecsPerROSpec = 3;
		long maxNumROSpecs = 3;
		int clientRequestOpSpecTimeout = 3;
		byte maxPriorityLevelSupported = 3;
		Boolean supportsEventAndReportHolding = Boolean.valueOf("true");
		Boolean canDoTagInventoryStateAwareSingulation = Boolean.valueOf("true");
		Boolean supportsClientRequestOpSpec = Boolean.valueOf("true");
		Boolean canReportBufferFillWarning = Boolean.valueOf("true");
		Boolean canDoRFSurvey = Boolean.valueOf("true");

		LLRPCapabilities llrpCap = new LLRPCapabilities(new TLVParameterHeader(), canDoRFSurvey, canReportBufferFillWarning, supportsClientRequestOpSpec,
				canDoTagInventoryStateAwareSingulation, supportsEventAndReportHolding, maxPriorityLevelSupported, clientRequestOpSpecTimeout, maxNumROSpecs,
				maxNumSpecsPerROSpec, maxNumInventoryParameterSpecsPerAISpec, maxNumAccessSpecs, maxNumOpSpecsPerAccessSpec);
		GetReaderCapabilitiesResponse readerCapabilities = new GetReaderCapabilitiesResponse(header, status, llrpCap);
		llrpManager.setReaderCapabilities(readerCapabilities);

		LLRPReturnContainerUtil<Map<Integer, Result>> validateExecuteTagOperation = llrpManager.validateExecuteTagOperation(operation);

		Assert.assertEquals(1, validateExecuteTagOperation.getValue().size());
		Assert.assertNotNull(validateExecuteTagOperation.getValue());
		Assert.assertNotNull(validateExecuteTagOperation.getValue().get(0));
		Assert.assertEquals(ResultState.OP_NOT_POSSIBLE_ERROR, validateExecuteTagOperation.getValue().get(Integer.valueOf(epc.getId())).getState());
	}

	@Test
	public void checkAddExecutePortOperation(final @Mocked LLRPConfiguration llrpConfiguration, final @Mocked LLRPConfig readerConfig) throws LLRPException,
			ValidationException {
		doCheckAddExecutePortOperation(llrpConfiguration, Type.READ, readerConfig);
		doCheckAddExecutePortOperation(llrpConfiguration, Type.WRITE, readerConfig);
	}

	private void doCheckAddExecutePortOperation(LLRPConfiguration llrpConfiguration, Type type, final LLRPConfig readerConfiguration)
			throws ValidationException, LLRPException {
		LLRPManager llrpManager = new LLRPManager(this);

		new MockUp<LLRPService>() {
			@SuppressWarnings("unused")
			@Mock
			public GetReaderCapabilitiesResponse getReaderCapabilities(GetReaderCapabilities request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				GetReaderCapabilitiesResponse capabilitiesResponse = new GetReaderCapabilitiesResponse(messageHeader, status);

				Assert.assertEquals(MessageType.GET_READER_CAPABILITIES, request.getMessageHeader().getMessageType());
				Assert.assertEquals(ProtocolVersion.LLRP_V1_1, request.getMessageHeader().getVersion());

				return capabilitiesResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public GetReaderConfigResponse getReaderConfig(GetReaderConfig request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				GetReaderConfigResponse configResponse = new GetReaderConfigResponse(messageHeader, status);

				if (request.getRequestedData() == GetReaderConfigRequestedData.GPI_CURRENT_STATE) {
					List<GPIPortCurrentState> gpiPortCurrentStateList = new ArrayList<>();

					GPIPortCurrentState inputPort1 = new GPIPortCurrentState(new TLVParameterHeader(), 1, true, GPIPortCurrentStateGPIState.HIGH);
					GPIPortCurrentState inputPort2 = new GPIPortCurrentState(new TLVParameterHeader(), 2, true, GPIPortCurrentStateGPIState.HIGH);

					gpiPortCurrentStateList.add(inputPort1);
					gpiPortCurrentStateList.add(inputPort2);

					configResponse.setGpiPortCurrentStateList(gpiPortCurrentStateList);
				} else if (request.getRequestedData() == GetReaderConfigRequestedData.GPO_WRITE_DATA) {
					List<GPOWriteData> gpoWriteDataList = new ArrayList<>();
					GPOWriteData gpoWriteData1 = new GPOWriteData(new TLVParameterHeader(), 3, true);
					GPOWriteData gpoWriteData2 = new GPOWriteData(new TLVParameterHeader(), 4, true);

					gpoWriteDataList.add(gpoWriteData1);
					gpoWriteDataList.add(gpoWriteData2);

					configResponse.setGpoWriteDataList(gpoWriteDataList);
				}

				Assert.assertEquals(MessageType.GET_READER_CONFIG, request.getMessageHeader().getMessageType());
				Assert.assertEquals(0, request.getAntennaID());
				Assert.assertTrue((request.getRequestedData() == GetReaderConfigRequestedData.GPI_CURRENT_STATE)
						|| (request.getRequestedData() == GetReaderConfigRequestedData.GPO_WRITE_DATA));
				Assert.assertEquals(ProtocolVersion.LLRP_V1_1, request.getMessageHeader().getVersion());

				return configResponse;
			}

			@SuppressWarnings("unused")
			@Mock
			public SetReaderConfigResponse setReaderConfig(SetReaderConfig request) throws LLRPException {
				MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
				LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
				SetReaderConfigResponse configResponse = new SetReaderConfigResponse(messageHeader, status);

				Assert.assertEquals(MessageType.SET_READER_CONFIG, request.getMessageHeader().getMessageType());

				List<GPOWriteData> gpoWriteData = new ArrayList<GPOWriteData>();
				int gpoPortNumber = 3;
				boolean gpoState = false;

				GPOWriteData data = new GPOWriteData(new TLVParameterHeader(), gpoPortNumber, gpoState);
				gpoWriteData.add(data);

				Assert.assertEquals(gpoWriteData.size() == 1, request.getGpoWriteDataList().size() == 1);
				Assert.assertEquals(gpoWriteData.get(0).getGpoPortNum(), request.getGpoWriteDataList().get(0).getGpoPortNum());
				Assert.assertEquals(gpoWriteData.get(0).getGpoState(), request.getGpoWriteDataList().get(0).getGpoState());
				Assert.assertEquals(gpoWriteData.get(0).getParameterHeader().getParameterType(), request.getGpoWriteDataList().get(0).getParameterHeader()
						.getParameterType());
				Assert.assertEquals(ProtocolVersion.LLRP_V1_1, request.getMessageHeader().getVersion());

				return configResponse;
			}
		};

		new NonStrictExpectations() {
			{
				readerConfiguration.getGpoState();
				Map<Integer, Boolean> map = new HashMap<>();
				map.put(1, true);
				map.put(0, true);
				result = map;
			}
		};

		llrpManager.llrpConfiguration = llrpConfiguration;
		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(getProperties());
		llrpManager.setConnectorConfiguration(llrpConnection);

		long id = 2;
		Pin pin1 = new Pin(1, havis.middleware.ale.base.operation.port.Pin.Type.INPUT);
		Pin pin2 = new Pin(3, havis.middleware.ale.base.operation.port.Pin.Type.OUTPUT);
		Long duration1 = 0L;
		Byte data1 = Byte.valueOf((byte) 12);
		Type type1 = type;
		String name1 = "Test";
		havis.middleware.ale.base.operation.port.Operation operation1 = new havis.middleware.ale.base.operation.port.Operation(name1, type1, data1, duration1,
				pin1);
		havis.middleware.ale.base.operation.port.Operation operation2 = new havis.middleware.ale.base.operation.port.Operation(name1, type1, data1, duration1,
				pin2);

		List<havis.middleware.ale.base.operation.port.Operation> operations = new ArrayList<>();
		operations.add(operation2);
		operations.add(operation1);
		PortOperation operation = new PortOperation(operations);

		llrpManager.version = ProtocolVersion.LLRP_V1_1;
		llrpManager.applyReaderConfig();
		llrpManager.setReaderConfiguration(readerConfiguration);
		llrpManager.addExecutePortOperation(id, operation);

		Assert.assertEquals(readerConfiguration, llrpManager.getReaderConfiguration());
	}

	@Test
	public void checkServiceROAccessReportEvent(final @Mocked GetReaderCapabilitiesResponse readerCapabilities) throws ValidationException {
		MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		ROAccessReport message = new ROAccessReport(messageHeader);

		EPC96 epc96 = new EPC96(new TVParameterHeader(), new byte[] { 0, 1, 2, 3 });
		EPCData epcData = new EPCData(new TLVParameterHeader(), BitConverter.toBitSet(new byte[] { 4, 5, 6, 7 }));
		epcData.setEpcLengthBits(32);

		TagReportData tagReportData1 = new TagReportData(new TLVParameterHeader(), epc96);
		TagReportData tagReportData2 = new TagReportData(new TLVParameterHeader(), epcData);

		C1G2LockPayloadPrivilege privilege = C1G2LockPayloadPrivilege.READ_WRITE;
		C1G2LockPayloadDataField dataField = C1G2LockPayloadDataField.EPC_MEMORY;
		C1G2LockPayload c1g2LockPayload = new C1G2LockPayload(new TLVParameterHeader(), privilege, dataField);
		C1G2LockPayloadPrivilege privilege2 = C1G2LockPayloadPrivilege.PERMALOCK;
		C1G2LockPayloadDataField dataField2 = C1G2LockPayloadDataField.EPC_MEMORY;
		C1G2LockPayload c1g2LockPayload2 = new C1G2LockPayload(new TLVParameterHeader(), privilege2, dataField2);

		List<C1G2LockPayload> c1g2LockPayloads = new ArrayList<>();
		c1g2LockPayloads.add(c1g2LockPayload);
		c1g2LockPayloads.add(c1g2LockPayload2);

		Parameter param1 = new C1G2Lock(new TLVParameterHeader(), 12, 0, c1g2LockPayloads);
		Parameter param2 = new C1G2CRC(new TVParameterHeader(), 2);

		List<Parameter> c1g2TagDataList1 = new ArrayList<>();
		List<Parameter> c1g2TagDataList2 = new ArrayList<>();

		c1g2TagDataList1.add(param1);
		c1g2TagDataList2.add(param2);

		tagReportData1.setC1g2TagDataList(c1g2TagDataList1);
		tagReportData2.setC1g2TagDataList(c1g2TagDataList2);

		AntennaId antID1 = new AntennaId(new TVParameterHeader(), 1);
		AntennaId antID2 = new AntennaId(new TVParameterHeader(), 2);

		tagReportData1.setAntID(antID1);
		tagReportData2.setAntID(antID2);

		Parameter res1 = new C1G2ReadOpSpecResult(new TLVParameterHeader(), C1G2ReadOpSpecResultValues.SUCCESS, 55, new byte[] { 1, 2, 3 });
		Parameter res2 = new C1G2WriteOpSpecResult(new TLVParameterHeader(), C1G2WriteOpSpecResultValues.SUCCESS, 56, 2);
		Parameter res3 = new C1G2KillOpSpecResult(new TLVParameterHeader(), C1G2KillOpSpecResultValues.SUCCESS, 57);
		Parameter res4 = new C1G2LockOpSpecResult(new TLVParameterHeader(), C1G2LockOpSpecResultValues.SUCCESS, 58);

		List<Parameter> opSpecResultList1 = new ArrayList<>();
		List<Parameter> opSpecResultList2 = new ArrayList<>();

		opSpecResultList1.add(res1);
		opSpecResultList1.add(res2);

		opSpecResultList2.add(res3);
		opSpecResultList2.add(res4);

		tagReportData1.setOpSpecResultList(opSpecResultList1);
		tagReportData2.setOpSpecResultList(opSpecResultList2);

		ROSpecID roSpecID1 = new ROSpecID(new TVParameterHeader(), 1);
		ROSpecID roSpecID2 = new ROSpecID(new TVParameterHeader(), 2);

		tagReportData1.setRoSpecID(roSpecID1);
		tagReportData2.setRoSpecID(roSpecID2);

		List<TagReportData> tagReportDataList = new ArrayList<>();

		tagReportDataList.add(tagReportData1);
		tagReportDataList.add(tagReportData2);

		message.setTagReportDataList(tagReportDataList);

		LLRPConnection connection = LLRPConnection.validateConnectorProperties(getProperties());

		LLRPManager llrpManager = new LLRPManager(this);
		llrpManager.setReaderCapabilities(readerCapabilities);
		llrpManager.setConnectorConfiguration(connection);
		LLRPEventArgs<ROAccessReport> e = new LLRPEventArgs<ROAccessReport>(message);
		llrpManager.serviceROAccessReportEvent(this, e);

		// TODO How to verify?
	}

	@Test
	public void checkServiceReaderNotificationEvent(final @Mocked LLRPEventHandler<PortEventArgs> eventHandler) throws LLRPException, IOException,
			InvalidMessageTypeException, InvalidParameterTypeException {
		ReaderEventNotification readerEventNotification = createReaderEventNotificationWithGpiEvent();
		LLRPEventArgs<ReaderEventNotification> eventArgs = new LLRPEventArgs<ReaderEventNotification>(readerEventNotification);
		final LLRPManager llrpManager = new LLRPManager(this);
		llrpManager.serviceReaderNotificationEvent(this, eventArgs);

		new MockUp<ALLRPManager>() {
			@SuppressWarnings("unused")
			@Mock
			private void removeSpecsFromReader() throws LLRPException {

			}
		};

		new Verifications() {
			{
				eventHandler.handleEvent(withCapture(), (PortEventArgs) withCapture());
				times = 1;
			}
		};
	}
	
	@Test
	public void checkNotificationEventROSpecEvent(final @Mocked GetReaderCapabilitiesResponse readerCapabilities) throws LLRPException, InterruptedException {
		long roSpecID = 12;
		ROSpecEvent roSpecEvent = new ROSpecEvent(new TLVParameterHeader(), ROSpecEventType.END_OF_ROSPEC, roSpecID, 1);
		LLRPManager llrpManager = new LLRPManager(this);
		
		checkNotificationEvent(readerCapabilities, roSpecEvent, llrpManager);
	}

	@Test
	public void checkNotificationEventAISpecEvent(final @Mocked GetReaderCapabilitiesResponse readerCapabilities) throws LLRPException, InterruptedException {
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		
		long roSpecID = 12;
		int specIndex = 1;
		AISpecEventType eventType = AISpecEventType.END_OF_AISPEC;
		AISpecEvent aiSpecEvent = new AISpecEvent(new TLVParameterHeader(), eventType, roSpecID, specIndex);
		
		
		LLRPManager llrpManager = new LLRPManager(this) {
			@Override
			public void removeExecuteTagOperation(long id) throws LLRPException {
				//do nothing
				//Unusually test method
				//onExecuteTagEvent was called
				atomicInteger.incrementAndGet();
				Assert.assertTrue(true);
			}
		};
		
		checkNotificationEvent(readerCapabilities, aiSpecEvent, llrpManager);
		
		//wait until LLRPManager.notificationEvent execution completed
		Thread.sleep(1000);
		
		Assert.assertEquals(1, atomicInteger.get());
	}
	
	private void checkNotificationEvent(GetReaderCapabilitiesResponse readerCapabilities, Parameter parameter, LLRPManager llrpManager) throws LLRPException, InterruptedException {
		
		llrpManager.setReaderCapabilities(readerCapabilities);
		llrpManager.executeROSpec = new ROSpec(new TLVParameterHeader(), 12, (short)13,ROSpecCurrentState.ACTIVE, null, null);
		LLRPMessageHandler llrpMessageHandler = new LLRPMessageHandler(llrpManager.service);

		AccessSpec accessSpec = new AccessSpec(new TLVParameterHeader(), 0, 0, ProtocolId.EPC_GLOBAL_C1G2, false, 0, null, null);

		new MockUp<LLRPService>() {

			private MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
			private LLRPStatus status = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");

			@SuppressWarnings("unused")
			@Mock
			public AddROSpecResponse addROSpec(AddROSpec request) throws LLRPException {
				AddROSpecResponse response = new AddROSpecResponse(messageHeader, status);
				return response;
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableROSpecResponse enableROSpec(EnableROSpec request) throws LLRPException {
				EnableROSpecResponse response = new EnableROSpecResponse(messageHeader, status);
				return response;
			}

			@SuppressWarnings("unused")
			@Mock
			public AddAccessSpecResponse addAccessSpec(AddAccessSpec request) throws LLRPException {
				AddAccessSpecResponse response = new AddAccessSpecResponse(messageHeader, status);
				return response;
			}

			@SuppressWarnings("unused")
			@Mock
			public EnableAccessSpecResponse enableAccessSpec(EnableAccessSpec request) throws LLRPException {
				EnableAccessSpecResponse response = new EnableAccessSpecResponse(messageHeader, status);
				return response;
			}

			@SuppressWarnings("unused")
			@Mock
			public StartROSpecResponse startROSpec(StartROSpec request) throws LLRPException {
				StartROSpecResponse response = new StartROSpecResponse(messageHeader, status);
				return response;
			}
		};

		llrpManager.addExecuteAccessSpec(accessSpec);

		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		ReaderEventNotificationData data = new ReaderEventNotificationData(new TLVParameterHeader(), new UTCTimestamp(new TLVParameterHeader(),
				BigInteger.valueOf(123465)));

		if(parameter instanceof AISpecEvent) {
			data.setAiSpecEvent((AISpecEvent)parameter);	
		} else if(parameter instanceof ROSpecEvent) {
			data.setRoSpecEvent((ROSpecEvent)parameter);	
		}
		
		Message evt = new ReaderEventNotification(header, data);
		
		llrpMessageHandler.notifyEvent(evt);
	}

	@Override
	public void notifyError(Exception error) {

	};

	private TagOperation getFilterSize2TagOperation() {
		List<Filter> arrayList = new ArrayList<Filter>();

		Filter filter1 = new Filter(1, 48, 0, "urn:epc:pat:sgtin-96:X.*.*.*".getBytes());
		Filter filter2 = new Filter(3, 48, 0, "urn:epc:pat:gsrn-96:X.*.*.*".getBytes());

		arrayList.add(filter1);
		arrayList.add(filter2);

		return getTagOperation(arrayList);
	}

	private TagOperation getEmptyFilterTagOperation() {
		return getTagOperation(new ArrayList<Filter>());
	}

	private TagOperation getTagOperation(List<Filter> filters) {
		return getTagOperation(OperationType.READ, filters);
	}

	private TagOperation getTagOperation(OperationType operationType, List<Filter> filters) {
		Field bank0 = new Field("RESERVED", 0, 0, 64);
		Field bank1 = new Field("EPC", 1, 0, 96);
		Field bank2 = new Field("TID", 2, 0, 96);
		Field bank3 = new Field("USER", 3, 0, 512);

		Operation reserved = new Operation(1, operationType, bank0);
		Operation epc = new Operation(1, operationType, bank1);
		Operation tid = new Operation(1, operationType, bank2);
		Operation user = new Operation(1, operationType, bank3);

		reserved.setData(new byte[] { 12, 13, 14, 15, 16, 17 });
		epc.setData(new byte[] { 18, 19, 20, 21, 22, 23 });
		tid.setData(new byte[] { 24, 25, 26, 27, 28, 29 });
		user.setData(new byte[] { 30, 31, 32, 33, 34, 35 });

		List<Operation> operations = new ArrayList<>();
		operations.add(reserved);
		operations.add(epc);
		operations.add(tid);
		operations.add(user);

		TagOperation tagOperation = new TagOperation(operations);
		tagOperation.setFilter(filters);

		return tagOperation;
	}

	private ReaderEventNotification createReaderEventNotification() {
		MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 1);
		UTCTimestamp uptime = new UTCTimestamp(new TLVParameterHeader(), new BigInteger("10000"));
		ReaderEventNotificationData notificationData = new ReaderEventNotificationData(new TLVParameterHeader(), uptime);
		ConnectionAttemptEvent connectionAttemptEvent = new ConnectionAttemptEvent(new TLVParameterHeader(), ConnectionAttemptEventStatusType.SUCCESS);
		notificationData.setConnectionAttemptEvent(connectionAttemptEvent);
		ReaderEventNotification eventNotification = new ReaderEventNotification(messageHeader, notificationData);

		return eventNotification;
	}

	private ReaderEventNotification createReaderEventNotificationWithGpiEvent() {
		MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 1);
		UTCTimestamp uptime = new UTCTimestamp(new TLVParameterHeader(), new BigInteger("10000"));
		ReaderEventNotificationData notificationData = new ReaderEventNotificationData(new TLVParameterHeader(), uptime);
		ConnectionAttemptEvent connectionAttemptEvent = new ConnectionAttemptEvent(new TLVParameterHeader(), ConnectionAttemptEventStatusType.SUCCESS);
		notificationData.setConnectionAttemptEvent(connectionAttemptEvent);
		GPIEvent gpiEvent = new GPIEvent(new TLVParameterHeader(), 1, true);
		notificationData.setGpiEvent(gpiEvent);
		ReaderEventNotification eventNotification = new ReaderEventNotification(messageHeader, notificationData);

		return eventNotification;
	}

	private Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "1000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		return properties;
	}
}

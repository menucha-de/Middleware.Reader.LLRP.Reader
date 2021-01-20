package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.parameter.AccessSpec;
import havis.llrpservice.data.message.parameter.AntennaId;
import havis.llrpservice.data.message.parameter.C1G2Read;
import havis.llrpservice.data.message.parameter.C1G2ReadOpSpecResult;
import havis.llrpservice.data.message.parameter.C1G2ReadOpSpecResultValues;
import havis.llrpservice.data.message.parameter.EPC96;
import havis.llrpservice.data.message.parameter.LLRPCapabilities;
import havis.llrpservice.data.message.parameter.Parameter;
import havis.llrpservice.data.message.parameter.ParameterTypes.ParameterType;
import havis.llrpservice.data.message.parameter.ROSpecID;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.llrpservice.data.message.parameter.TVParameterHeader;
import havis.llrpservice.data.message.parameter.TagReportData;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.base.message.Message;
import havis.middleware.ale.base.operation.port.Pin;
import havis.middleware.ale.base.operation.port.Pin.Type;
import havis.middleware.ale.base.operation.port.Port;
import havis.middleware.ale.base.operation.port.PortObservation;
import havis.middleware.ale.base.operation.port.PortOperation;
import havis.middleware.ale.base.operation.port.result.Result.State;
import havis.middleware.ale.base.operation.tag.Field;
import havis.middleware.ale.base.operation.tag.Operation;
import havis.middleware.ale.base.operation.tag.OperationType;
import havis.middleware.ale.base.operation.tag.Tag;
import havis.middleware.ale.base.operation.tag.TagOperation;
import havis.middleware.ale.base.operation.tag.result.KillResult;
import havis.middleware.ale.base.operation.tag.result.LockResult;
import havis.middleware.ale.base.operation.tag.result.ReadResult;
import havis.middleware.ale.base.operation.tag.result.Result;
import havis.middleware.ale.base.operation.tag.result.ResultState;
import havis.middleware.ale.base.operation.tag.result.WriteResult;
import havis.middleware.ale.exit.Exits.Reader;
import havis.middleware.ale.reader.Callback;
import havis.middleware.ale.reader.Capability;
import havis.middleware.ale.reader.Property.Connector;
import havis.middleware.ale.service.rc.RCConfig;
import havis.middleware.reader.llrp.ALLRPManager.PortEventArgs;
import havis.middleware.reader.llrp.ALLRPManager.TagEventArgs;
import havis.middleware.reader.llrp.client.LLRPConnection;
import havis.middleware.reader.llrp.client.LLRPProperties.PropertyName;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler.LLRPEvent;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.IDGenerator;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;
import havis.util.monitor.ReaderEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class LLRPReaderConnectorTest {
	
	@Test
	public void checkEtbFeature(final @Mocked GetReaderCapabilitiesResponse readerCapabilities, final @Mocked LLRPCapabilities llrpCapabilities) throws LLRPException, ValidationException, ImplementationException {
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(new Callback() {
			
			@Override
			public void notify(long id, Port port) {
			}
			
			@Override
			public void notify(long id, Tag tag) {
				byte[][] expected = new byte[][]{{(byte)0xFE,(byte)0xC0,(byte)0xBA,(byte)0x98,(byte)0x12,(byte)0x34,(byte)0x56,(byte)0x78,(byte)0x90,(byte)0xAB,(byte)0xCD,(byte)0xEF},
												 {(byte)0x12,(byte)0x34},
												 {(byte)0x00,(byte)0x00,(byte)0xAF,(byte)0xFE}};
				int counter = 0;
				
				for(Result result : tag.getResult().values()) {
					if(result instanceof ReadResult) {
						
						ReadResult readResult = (ReadResult)result;
						byte[] data = readResult.getData();
						
						Assert.assertArrayEquals(expected[counter], data);
						
						counter++;
					}
				}
				
				Assert.assertEquals(3, counter);
			}
			
			@Override
			public void notify(Message message) {
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		});
		
		ALLRPManager llrpManager = llrpReaderConnector.manager;
		LLRPConnection connection = LLRPConnection.validateConnectorProperties(getProperties());
		llrpManager.setReaderCapabilities(readerCapabilities);
		llrpManager.setConnectorConfiguration(connection);
		
		new NonStrictExpectations() {{
			llrpCapabilities.getMaxPriorityLevelSupported();
			result = 2;
		}};
		
		llrpManager.getInventoryTagEvent().add(llrpReaderConnector.notifyInventoryTagLlrpEvent);
		
		Field field1 = new Field("USER1", 3, 0, 96);
		Field field1a = new Field("USER2", 3, 32, 16);
		Field field2 = new Field("USER3", 3, 480, 32);
		
		List<Operation> operations = new ArrayList<>();
		Operation operation1 = new Operation(1, OperationType.READ, field1);
		Operation operation1a = new Operation(2, OperationType.READ, field1a);
		Operation operation2 = new Operation(3, OperationType.READ, field2);
		
		operations.add(operation1);
		operations.add(operation1a);
		operations.add(operation2);
		
		TagOperation tagOperation = new TagOperation(operations);
		
		llrpReaderConnector.defineTagOperation(122, tagOperation);
		llrpReaderConnector.enableTagOperation(122);
		
		ROAccessReport roAccessReport = new ROAccessReport(new MessageHeader((byte)0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID()));
		byte[] data1 = new byte[]{(byte)0xFE,(byte)0xC0,(byte)0xBA,(byte)0x98, (byte)0x12,(byte)0x34,(byte)0x56,(byte)0x78, (byte)0x90,(byte)0xAB,(byte)0xCD,(byte)0xEF};
		//byte[] data1a = new byte[]{(byte)0xBA,(byte)0x98};
		byte[] data2 = new byte[]{(byte)0x00,(byte)0x00, (byte)0xAF,(byte)0xFE};
				
		EPC96 epc96 = new EPC96(new TVParameterHeader(), data1);
		
		TagReportData tagReportData = new TagReportData(new TLVParameterHeader(), epc96);
		
		AntennaId antID = new AntennaId(new TVParameterHeader(), 1);
		
		tagReportData.setC1g2TagDataList(new ArrayList<Parameter>());
		tagReportData.setAntID(antID);
		
		ROSpecID roSpecID = new ROSpecID(new TVParameterHeader(), 1);
		tagReportData.setRoSpecID(roSpecID);
		
		List<Parameter> opSpecResultList = new ArrayList<Parameter>();
		
		TLVParameterHeader header = new TLVParameterHeader();
		header.setParameterType(ParameterType.C1G2_READ_OP_SPEC_RESULT);
		C1G2ReadOpSpecResultValues result = C1G2ReadOpSpecResultValues.SUCCESS;
		
		Parameter parameter1 = new C1G2ReadOpSpecResult(header, result, 3, data1);
		//Parameter parameter1a = new C1G2ReadOpSpecResult(header, result, 4, data1a);
		Parameter parameter2 = new C1G2ReadOpSpecResult(header, result, 4, data2);
		
		opSpecResultList.add(parameter1);
		//opSpecResultList.add(parameter1a);
		opSpecResultList.add(parameter2);
		
		tagReportData.setOpSpecResultList(opSpecResultList);
				
		List<TagReportData> tagReportDataList = new ArrayList<>();
		tagReportDataList.add(tagReportData);
		
		roAccessReport.setTagReportDataList(tagReportDataList);
		
		LLRPEventArgs<ROAccessReport> e = new LLRPEventArgs<ROAccessReport>(roAccessReport);
		llrpManager.serviceROAccessReportEvent(this, e);
	}

	@Test
	public void checkReadFullUserBank(final @Mocked GetReaderCapabilitiesResponse readerCapabilities, final @Mocked LLRPCapabilities llrpCapabilities)
			throws LLRPException, ValidationException, ImplementationException {
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		ALLRPManager llrpManager = llrpReaderConnector.manager;
		LLRPConnection connection = LLRPConnection.validateConnectorProperties(getProperties());
		llrpManager.setReaderCapabilities(readerCapabilities);
		llrpManager.setConnectorConfiguration(connection);

		new NonStrictExpectations() {
			{
				llrpCapabilities.getMaxPriorityLevelSupported();
				result = 2;
			}
		};

		Field field1 = new Field("USER", 3, 0, 96);
		Field field2 = new Field("USER", 3, 0, 0); // full bank

		List<Operation> operations = new ArrayList<>();
		Operation operation1 = new Operation(1, OperationType.READ, field1);
		Operation operation2 = new Operation(3, OperationType.READ, field2);

		operations.add(operation1);
		operations.add(operation2);

		TagOperation tagOperation = new TagOperation(operations);

		llrpReaderConnector.defineTagOperation(122, tagOperation);

		AccessSpec inventoryAccessSpec = llrpManager.getInventoryAccessSpec();
		Assert.assertEquals(2, inventoryAccessSpec.getAccessCommand().getOpSpecList().size());
		Assert.assertTrue(inventoryAccessSpec.getAccessCommand().getOpSpecList().get(0) instanceof C1G2Read);
		Assert.assertEquals(0, ((C1G2Read) inventoryAccessSpec.getAccessCommand().getOpSpecList().get(0)).getWordCount());
		Assert.assertTrue(inventoryAccessSpec.getAccessCommand().getOpSpecList().get(1) instanceof C1G2Read);
		Assert.assertEquals(6, ((C1G2Read) inventoryAccessSpec.getAccessCommand().getOpSpecList().get(1)).getWordCount());
	}

	@Test
	public void checkConstructors() {
		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long id, Tag tag) {
			}

			@Override
			public void notify(Message message) {
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);

		Assert.assertSame(cb, llrpReaderConnector.clientCallback);

		LLRPReaderConnector llrpReaderConnector2 = new LLRPReaderConnector();
		Assert.assertNotNull(llrpReaderConnector2.manager);
		Assert.assertNotNull(llrpReaderConnector2.readerType);
	}

	@Test
	public void checkProperties(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException {
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();

		Map<String, String> properties = getProperties();

		llrpReaderConnector.setProperties(properties);

		new Verifications() {
			{
				llrpManager.setConnectorConfiguration((LLRPConnection) withCapture());
				times = 1;
			}
		};
	}

	@Test
	public void checkGetCapability() throws ValidationException, ImplementationException {
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();

		String cap = llrpReaderConnector.getCapability(Capability.LostEPCOnWrite);

		Assert.assertEquals("true", cap);

		try {
			llrpReaderConnector.getCapability("test"); // Illegal argument
		} catch (ValidationException ve) {
			Assert.assertTrue(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void checkConnectDisconnect(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {

		final Callback callback = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long id, Tag tag) {
			}

			@Override
			public void notify(Message message) {
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		Map<String, String> properties = getProperties();

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = true;
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(callback);
		llrpReaderConnector.setProperties(properties);
		llrpReaderConnector.connect();

		new Verifications() {
			{
				llrpManager.getNoDataReceivedEvent().add((LLRPEvent<EventObject>) withCapture());
				times = 1;
				llrpManager.getInventoryTagEvent().add((LLRPEvent<TagEventArgs>) withCapture());
				times = 1;
				llrpManager.getExecuteTagEvent().add((LLRPEvent<TagEventArgs>) withCapture());
				times = 1;
				llrpManager.getObservationPortEvent().add((LLRPEvent<PortEventArgs>) withCapture());
				times = 1;
				llrpManager.getExecutePortEvent().add((LLRPEvent<PortEventArgs>) withCapture());
				times = 1;
				llrpManager.applyReaderConfig();
				times = 1;
				llrpManager.initialize();
				times = 1;
				llrpManager.assign(false);
				times = 1;
			}
		};

		llrpReaderConnector.dispose();

		new Verifications() {
			{
				llrpManager.getNoDataReceivedEvent().remove((LLRPEvent<EventObject>) withCapture());
				times = 1;
				llrpManager.getInventoryTagEvent().remove((LLRPEvent<TagEventArgs>) withCapture());
				times = 1;
				llrpManager.getExecuteTagEvent().remove((LLRPEvent<TagEventArgs>) withCapture());
				times = 1;
				llrpManager.getObservationPortEvent().remove((LLRPEvent<PortEventArgs>) withCapture());
				times = 1;
				llrpManager.getExecutePortEvent().remove((LLRPEvent<PortEventArgs>) withCapture());
				times = 1;
				llrpManager.disconnect();
				times = 1;
			}
		};

	}

	@Test
	public void checkConnectionFail(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {

		Map<String, String> properties = getProperties();

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = false;
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		llrpReaderConnector.setProperties(properties);

		try {
			llrpReaderConnector.connect();
		} catch (ImplementationException ie) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void checkDefineUndefineTagOperation(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {

		Map<String, String> properties = getProperties();

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = true;
			}
		};

		long id = 12;
		TagOperation tagOperation = getTagOperation();
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();

		llrpReaderConnector.setProperties(properties);
		llrpReaderConnector.connect();

		llrpReaderConnector.defineTagOperation(id, tagOperation);
		llrpReaderConnector.enableTagOperation(id);

		new Verifications() {
			{
				llrpManager.updateInventory((LLRPInventoryOperation) withCapture());
				times = 1;
			}
		};

		llrpReaderConnector.undefineTagOperation(id);

		new Verifications() {
			{
				llrpManager.updateInventory((LLRPInventoryOperation) withCapture());
				times = 2;
			}
		};
	}

	@Test
	public void checkDefineTagOperationFail() throws ValidationException, ImplementationException {
		long id = 12;
		TagOperation tagOperation = getTagOperation();
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();

		try {
			llrpReaderConnector.defineTagOperation(id, tagOperation);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void checkExecuteTagOperationNotConnected(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {
		long id = 12;
		TagOperation tagOperation = getTagOperation();
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();

		try {
			llrpReaderConnector.executeTagOperation(id, tagOperation);
		} catch (ImplementationException ie) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void checkExecuteTagOperationMiscErrorTotal(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException,
			LLRPException, InterruptedException {
		prepareExecuteTagOperationMiscErrorTotal(llrpManager, OperationType.KILL);
		prepareExecuteTagOperationMiscErrorTotal(llrpManager, OperationType.LOCK);
		prepareExecuteTagOperationMiscErrorTotal(llrpManager, OperationType.PASSWORD);
		prepareExecuteTagOperationMiscErrorTotal(llrpManager, OperationType.READ);
		prepareExecuteTagOperationMiscErrorTotal(llrpManager, OperationType.WRITE);
	}

	@Test
	public void checkExecuteTagOperation(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {

		Map<String, String> properties = getProperties();

		final long id = 12;
		final TagOperation tagOperation = getTagOperation();

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = true;

				llrpManager.validateExecuteTagOperation(tagOperation);
				LLRPReturnContainerUtil<Map<Integer, Result>> returnContainerUtil = new LLRPReturnContainerUtil<>();
				returnContainerUtil.setTrue(true);
				returnContainerUtil.setValue(new HashMap<Integer, Result>());
				result = returnContainerUtil;
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		llrpReaderConnector.setProperties(properties);
		llrpReaderConnector.connect();

		llrpReaderConnector.executeTagOperation(id, tagOperation);

		new Verifications() {
			{
				llrpManager.addExecuteTagOperation(id, tagOperation);
				times = 1;
			}
		};
	}

	@Test
	public void checkPortObservation(final @Mocked LLRPManager llrpManager) throws ValidationException, ImplementationException, LLRPException {

		final long id = 12;
		Pin pin1 = new Pin(1, Type.INPUT);
		Pin pin2 = new Pin(2, Type.OUTPUT);
		List<Pin> pins = new ArrayList<>();
		pins.add(pin1);
		pins.add(pin2);
		PortObservation portObservation = new PortObservation(pins);
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		llrpReaderConnector.definePortObservation(id, portObservation);
		llrpReaderConnector.enablePortObservation(id);
		llrpReaderConnector.undefinePortObservation(id);

		// TODO How to verify?
	}

	@Test
	public void checkExecutePortOperation(final @Mocked LLRPManager llrpManager) throws LLRPException, ValidationException, ImplementationException {
		final long id = 12;
		Map<String, String> properties = getProperties();
		List<havis.middleware.ale.base.operation.port.Operation> portOperations = new ArrayList<>();
		Pin pin1 = new Pin(1, Type.INPUT);
		havis.middleware.ale.base.operation.port.Operation operation = new havis.middleware.ale.base.operation.port.Operation("IN",
				havis.middleware.ale.base.operation.port.Operation.Type.READ, (byte) 123, 1000L, pin1);

		portOperations.add(operation);

		final PortOperation portOperation = new PortOperation(portOperations);

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = true;
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		llrpReaderConnector.setProperties(properties);
		llrpReaderConnector.connect();
		llrpReaderConnector.executePortOperation(id, portOperation);

		new Verifications() {
			{
				llrpManager.addExecutePortOperation(id, portOperation);
				times = 1;
			}
		};
	}

	@Test
	public void checkGetConfig() throws LLRPException, ValidationException, ImplementationException {
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector();
		RCConfig config = llrpReaderConnector.getConfig();

		Assert.assertNull(config);
	}

	@Test
	public void checkNotifyError(final @Mocked LLRPManager llrpManager) throws LLRPException, ValidationException, ImplementationException, IOException {
		final Message expected = new Message(Reader.Controller.Error, "IOException occured!");

		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long id, Tag tag) {
			}

			@Override
			public void notify(Message actual) {
				Assert.assertEquals(expected.getType(), actual.getType());
				Assert.assertEquals(expected.getText(), actual.getText());
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		new NonStrictExpectations() {
			{
				llrpManager.dispose();
				result = new IOException("IOException occured!");
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);
		llrpReaderConnector.dispose();
	}

	@Test
	public void checkNotifyInventoryTagEvent(final @Mocked GetReaderCapabilitiesResponse readerCapabilities, final @Mocked LLRPCapabilities llrpCapabilities)
			throws LLRPException, ValidationException, ImplementationException, IOException {
		final long id = 12;
		final Tag tag = new Tag(new byte[] { 0x30, 0x38, 0x78, (byte) 0x90, 0x04, (byte) 0xB5, (byte) 0xA1, (byte) 0xC0, 0x00, 0x00, 0x27, 0x10 });

		Map<Integer, Result> tagResults = new HashMap<Integer, Result>();
		ReadResult readResult = new ReadResult(ResultState.SUCCESS);
		tagResults.put(0, readResult);
		tagResults.put(1, readResult);
		tagResults.put(2, readResult);
		tagResults.put(3, readResult);

		tag.setResult(tagResults);

		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long actualId, Tag actualTag) {
				Assert.assertEquals(id, actualId);
				byte[] expectedEpc = tag.getEpc();
				byte[] actualEpc = actualTag.getEpc();
				Assert.assertArrayEquals(expectedEpc, actualEpc);
			}

			@Override
			public void notify(Message actual) {

			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);

		new MockUp<LLRPManager>() {
			@SuppressWarnings("unused")
			@Mock
			public boolean connect() {
				return true;
			}

			@SuppressWarnings("unused")
			@Mock
			public void applyReaderConfig() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void initialize() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void assign(boolean execute) throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void disconnect() {

			}

			@SuppressWarnings("unused")
			@Mock
			public void start(boolean execute) throws LLRPException {

			}
		};

		new NonStrictExpectations() {
			{
				llrpCapabilities.getMaxPriorityLevelSupported();
				result = (byte) 1;
			}
		};

		llrpReaderConnector.manager.setReaderCapabilities(readerCapabilities);
		llrpReaderConnector.setProperties(getProperties());
		llrpReaderConnector.connect();
		llrpReaderConnector.defineTagOperation(id, getTagOperation());
		llrpReaderConnector.enableTagOperation(id);
		llrpReaderConnector.manager.onInventoryTagEvent(tag, new ArrayList<Parameter>());
		llrpReaderConnector.undefineTagOperation(id);
		llrpReaderConnector.dispose();
	}

	@Test
	public void checkNotifyExecuteTagEvent(final @Mocked GetReaderCapabilitiesResponse readerCapabilities, final @Mocked LLRPCapabilities llrpCapabilities)
			throws LLRPException, ValidationException, ImplementationException, IOException {
		byte[] byteSet = new byte[] { 0x30, 0x38, 0x78, (byte) 0x90, 0x04, (byte) 0xB5, (byte) 0xA1, (byte) 0xC0, 0x00, 0x00, 0x27, 0x10 };
		prepareNotifyExecuteTagEvent(readerCapabilities, llrpCapabilities, OperationType.READ, new ReadResult(ResultState.SUCCESS), true, byteSet);
		prepareNotifyExecuteTagEvent(readerCapabilities, llrpCapabilities, OperationType.WRITE, new WriteResult(ResultState.SUCCESS), true, byteSet);
		prepareNotifyExecuteTagEvent(readerCapabilities, llrpCapabilities, OperationType.KILL, new KillResult(ResultState.SUCCESS), true, byteSet);
		prepareNotifyExecuteTagEvent(readerCapabilities, llrpCapabilities, OperationType.LOCK, new LockResult(ResultState.SUCCESS), true, byteSet);
	}
	
	@Test
	public void prepareNotifyObservationPortEvent() throws LLRPException, ValidationException,
			ImplementationException, IOException {
		Pin pin = new Pin(1, Type.INPUT);
		Map<Integer, havis.middleware.ale.base.operation.port.result.Result> m = new HashMap<>();
		m.put(1, new havis.middleware.ale.base.operation.port.result.ReadResult(State.SUCCESS));
		final Port port = new Port(pin, "INPUT Port", m);
		
		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
				
			}

			@Override
			public void notify(long actualId, Tag actualTag) {
			}

			@Override
			public void notify(Message actual) {

			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);

		new MockUp<LLRPManager>() {
			@SuppressWarnings("unused")
			@Mock
			public boolean connect() {
				return true;
			}

			@SuppressWarnings("unused")
			@Mock
			public void applyReaderConfig() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void initialize() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void assign(boolean execute) throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void disconnect() {

			}

			@SuppressWarnings("unused")
			@Mock
			public void start(boolean execute) throws LLRPException {

			}
		};

		llrpReaderConnector.setProperties(getProperties());
		llrpReaderConnector.connect();

		llrpReaderConnector.manager.onObservationPortEvent(port);

		llrpReaderConnector.dispose();

		Assert.assertNull(llrpReaderConnector.manager);
	}
	
	@Test
	public void checkServiceNoDataReceivedEvent() {
		Callback callback = new Callback() {
			
			@Override
			public void notify(long id, Port port) {
			}
			
			@Override
			public void notify(long id, Tag tag) {
			}
			
			@Override
			public void notify(havis.middleware.ale.base.message.Message message) {
				Assert.assertNotNull(message);
				Assert.assertEquals(Reader.Controller.ConnectionLost, message.getType());
				Assert.assertNotNull(message.getText());
			}
			
			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};
		
		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(callback);
		ALLRPManager llrpManager = llrpReaderConnector.manager;
		
		llrpManager.getNoDataReceivedEvent().add(llrpReaderConnector.notifyConnectionLostLlrpEvent);
		
		EventObject e = new EventObject("String");
		
		llrpManager.serviceNoDataReceivedEvent(this, e);
	}

	private void prepareNotifyExecuteTagEvent(GetReaderCapabilitiesResponse readerCapabilities, final LLRPCapabilities llrpCapabilities,
			OperationType operationType, final Result expectedResult, final boolean isNotError, byte[] byteSet) throws LLRPException, ValidationException,
			ImplementationException, IOException {
		final long id = 12;
		final Tag tag = new Tag(byteSet);

		Map<Integer, Result> tagResults = new HashMap<Integer, Result>();
		tagResults.put(1, expectedResult);

		tag.setResult(tagResults);

		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long actualId, Tag actualTag) {
				Assert.assertEquals(id, actualId);
				byte[] expectedEpc = tag.getEpc();
				byte[] actualEpc = actualTag.getEpc();

				Assert.assertTrue("Not same instance", actualTag.getResult().get(1) instanceof Result);

				Result actualResult = actualTag.getResult().get(1);

				Assert.assertEquals(String.format("State not equal. Expected: %s; Current: %s", expectedResult.getState(), actualResult.getState()),
						expectedResult.getState(), actualResult.getState());

				Assert.assertArrayEquals(expectedEpc, actualEpc);
			}

			@Override
			public void notify(Message actual) {

			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);
		TagOperation operation = getTagOperation();

		new MockUp<LLRPManager>() {
			@SuppressWarnings("unused")
			@Mock
			public boolean connect() {
				return true;
			}

			@SuppressWarnings("unused")
			@Mock
			public void applyReaderConfig() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void initialize() throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void assign(boolean execute) throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			public void disconnect() {

			}

			@SuppressWarnings("unused")
			@Mock
			public void start(boolean execute) throws LLRPException {

			}

			@SuppressWarnings("unused")
			@Mock
			protected void addExecuteAccessSpec(AccessSpec accessSpec) {

			}

			@SuppressWarnings("unused")
			@Mock
			protected void removeExecuteReaderOperationSpec(long id) {

			}

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<Map<Integer, Result>> validateExecuteTagOperation(TagOperation operation) {
				LLRPReturnContainerUtil<Map<Integer, Result>> returnContainerUtil = new LLRPReturnContainerUtil<>();
				returnContainerUtil.setTrue(isNotError);
				returnContainerUtil.setValue(new HashMap<Integer, Result>());

				return returnContainerUtil;
			}
		};

		new NonStrictExpectations() {
			{
				llrpCapabilities.getMaxPriorityLevelSupported();
				result = (byte) 1;
			}
		};

		llrpReaderConnector.manager.setReaderCapabilities(readerCapabilities);
		llrpReaderConnector.setProperties(getProperties());
		llrpReaderConnector.connect();
		llrpReaderConnector.defineTagOperation(id, getTagOperation(operationType));
		llrpReaderConnector.enableTagOperation(id);
		llrpReaderConnector.executeTagOperation(id, operation);
		llrpReaderConnector.manager.onExecuteTagEvent(id, tag);
		llrpReaderConnector.undefineTagOperation(id);

		Assert.assertNull(llrpReaderConnector.manager.executeROSpec);

		llrpReaderConnector.dispose();

		Assert.assertNull(llrpReaderConnector.manager);
	}

	private void prepareExecuteTagOperationMiscErrorTotal(final LLRPManager llrpManager, OperationType operationType) throws ValidationException,
			ImplementationException, LLRPException, InterruptedException {
		final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

		Callback cb = new Callback() {

			@Override
			public void notify(long id, Port port) {
			}

			@Override
			public void notify(long id, Tag tag) {
				atomicBoolean.set(true);
			}

			@Override
			public void notify(Message message) {
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public void notify(ReaderEvent event) {
			}

			@Override
			public int getReaderCycleDuration() {
				return 0;
			}

			@Override
			public int getNetworkPort() {
				return 0;
			}

			@Override
			public void resetNetwortPort(int port) {
			}
		};

		Map<String, String> properties = getProperties();

		final long id = 12;
		final TagOperation tagOperation = getTagOperation(operationType);

		new NonStrictExpectations() {
			{
				llrpManager.connect();
				result = true;

				llrpManager.validateExecuteTagOperation(tagOperation);
				LLRPReturnContainerUtil<Map<Integer, Result>> returnContainerUtil = new LLRPReturnContainerUtil<>();
				returnContainerUtil.setTrue(false);
				Map<Integer, Result> errors = new HashMap<Integer, Result>();
				returnContainerUtil.setValue(errors);
				result = returnContainerUtil;
			}
		};

		LLRPReaderConnector llrpReaderConnector = new LLRPReaderConnector(cb);
		llrpReaderConnector.setProperties(properties);
		llrpReaderConnector.connect();

		llrpReaderConnector.executeTagOperation(id, tagOperation);

		Assert.assertTrue(atomicBoolean.get());
	}

	private TagOperation getTagOperation(OperationType operationType) {
		Field bank0 = new Field("RESERVED", 0, 0, 64);
		Field bank1 = new Field("EPC", 1, 0, 96);
		Field bank2 = new Field("TID", 2, 0, 96);
		Field bank3 = new Field("USER", 3, 0, 512);

		Operation reserved = new Operation(1, operationType, bank0);
		Operation epc = new Operation(1, operationType, bank1);
		Operation tid = new Operation(1, operationType, bank2);
		Operation user = new Operation(1, operationType, bank3);

		List<Operation> operations = new ArrayList<>();
		operations.add(reserved);
		operations.add(epc);
		operations.add(tid);
		operations.add(user);

		TagOperation tagOperation = new TagOperation(operations);

		return tagOperation;
	}

	private TagOperation getTagOperation() {
		return getTagOperation(OperationType.READ);
	}

	private Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		return properties;
	}
}

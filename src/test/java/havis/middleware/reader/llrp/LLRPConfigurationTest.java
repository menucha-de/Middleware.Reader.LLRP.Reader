package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.GetReaderConfig;
import havis.llrpservice.data.message.GetReaderConfigRequestedData;
import havis.llrpservice.data.message.GetReaderConfigResponse;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.SetReaderConfig;
import havis.llrpservice.data.message.SetReaderConfigResponse;
import havis.llrpservice.data.message.parameter.AntennaConfiguration;
import havis.llrpservice.data.message.parameter.AntennaProperties;
import havis.llrpservice.data.message.parameter.C1G2InventoryCommand;
import havis.llrpservice.data.message.parameter.GPIPortCurrentState;
import havis.llrpservice.data.message.parameter.GPIPortCurrentStateGPIState;
import havis.llrpservice.data.message.parameter.GPOWriteData;
import havis.llrpservice.data.message.parameter.GeneralDeviceCapabilities;
import havis.llrpservice.data.message.parameter.LLRPStatus;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.RFReceiver;
import havis.llrpservice.data.message.parameter.RFTransmitter;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.reader.Prefix;
import havis.middleware.ale.reader.Property.Connector;
import havis.middleware.reader.llrp.LLRPConfiguration.LLRPConfig;
import havis.middleware.reader.llrp.client.LLRPProperties.PropertyName;
import havis.middleware.reader.llrp.service.LLRPService;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.IDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class LLRPConfigurationTest {

	@Test
	public void checkSetterAndGetter() {
		LLRPService llrpService = new LLRPService();
		ProtocolVersion protocolVersion = ProtocolVersion.LLRP_V1_1;

		LLRPConfiguration llrpConfiguration = new LLRPConfiguration(llrpService, protocolVersion);

		Assert.assertSame(llrpService, llrpConfiguration.getService());
		Assert.assertSame(protocolVersion, llrpConfiguration.getVersion());

		LLRPService otherLlrpService = new LLRPService();
		ProtocolVersion otherProtocolVersion = ProtocolVersion.LLRP_V1_0_1;

		llrpConfiguration.setService(otherLlrpService);
		llrpConfiguration.setVersion(otherProtocolVersion);

		Assert.assertSame(otherLlrpService, llrpConfiguration.getService());
		Assert.assertSame(otherProtocolVersion, llrpConfiguration.getVersion());
	}

	@Test
	public void checkValidation() throws ValidationException {

		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		properties.put("Reader.Config.Hold", "0");

		properties.put("Reader.Config.Antenna.AntennaProperties.2.Connected", "0");
		properties.put("Reader.Config.Antenna.AntennaProperties.2.Gain", "20");

		properties.put("Reader.Config.Antenna.AntennaConfiguration.2.RF.Receiver.Sensitivity", "20");
		properties.put("Reader.Config.Antenna.AntennaConfiguration.2.RF.Transmitter.Power", "20");
		properties.put("Reader.Config.Antenna.AntennaConfiguration.2.RF.Transmitter.Hop", "20");
		properties.put("Reader.Config.Antenna.AntennaConfiguration.2.RF.Transmitter.Channel", "20");

		properties.put("Reader.Config.Ports.Inputs.1.Enable", "0");
		properties.put("Reader.Config.Ports.Outputs.1.State", "0");

		LLRPConfiguration.LLRPConfig config = LLRPConfiguration.validateConfigurationProperties(properties);

		Assert.assertEquals(config.getAntennaChannel().get(2), Integer.valueOf(20));
		Assert.assertFalse(config.getAntennaConnected().get(2));
		Assert.assertEquals(config.getAntennaGain().get(2), Integer.valueOf(20));
		Assert.assertEquals(config.getAntennaHop().get(2), Integer.valueOf(20));
		Assert.assertEquals(config.getAntennaPower().get(2), Integer.valueOf(20));
		Assert.assertEquals(config.getAntennaSensitivity().get(2), Integer.valueOf(20));
		Assert.assertFalse(config.getGpiEnable().get(1));
		Assert.assertFalse(config.getGpoState().get(1));
		Assert.assertFalse(config.getHold());
	}

	@Test
	public void checkValidationFail() throws ValidationException {

		Map<String, String> properties = new HashMap<>();

		properties.put(Prefix.Reader + "Test", "true");

		boolean fail = false;

		try {
			LLRPConfiguration.validateConfigurationProperties(properties);
		} catch (ValidationException ve) {
			fail = true;
		}

		Assert.assertTrue(fail);
	}

	@Test
	public void checkApplyReaderConfig(final @Mocked LLRPService llrpService, final @Mocked GetReaderConfigResponse currentReaderConfiguration,
			final @Mocked GetReaderCapabilitiesResponse capabilities, final @Mocked LLRPConfig config,
			final @Mocked SetReaderConfigResponse setReaderConfigResponse) throws LLRPException {
		new NonStrictExpectations() {
			{
				llrpService.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID()), 0,
						GetReaderConfigRequestedData.ALL, 0, 0));
				result = currentReaderConfiguration;

				currentReaderConfiguration.getStatus();
				LLRPStatus llrpStatus = new LLRPStatus();
				llrpStatus.setStatusCode(LLRPStatusCode.M_SUCCESS);
				result = llrpStatus;

				capabilities.getGeneralDeviceCap();
				GeneralDeviceCapabilities generalDeviceCapabilities = new GeneralDeviceCapabilities();
				generalDeviceCapabilities.setCanSetAntennaProperties(true);
				result = generalDeviceCapabilities;

				currentReaderConfiguration.getAntennaPropertiesList();
				AntennaProperties antennaProperties = new AntennaProperties(new TLVParameterHeader(), true, 1, (short) 3);
				List<AntennaProperties> listAntennaProperties = new ArrayList<>();
				listAntennaProperties.add(antennaProperties);
				result = listAntennaProperties;

				config.getAntennaConnected();
				Map<Integer, Boolean> antCon = new HashMap<>();
				antCon.put(1, true);
				result = antCon;

				config.getAntennaGain();
				Map<Integer, Integer> antGai = new HashMap<>();
				antGai.put(1, 0);
				result = antGai;

				currentReaderConfiguration.getAntennaConfigurationList();
				AntennaConfiguration antennaConfiguration = new AntennaConfiguration(new TLVParameterHeader(), 1);
				antennaConfiguration.setC1g2InventoryCommandList(new ArrayList<C1G2InventoryCommand>());
				antennaConfiguration.setRfReceiver(new RFReceiver(new TLVParameterHeader(), 2));
				antennaConfiguration.setRfTransmitter(new RFTransmitter());
				List<AntennaConfiguration> listAntennaConfigurations = new ArrayList<>();
				listAntennaConfigurations.add(antennaConfiguration);
				result = listAntennaConfigurations;

				config.getAntennaSensitivity();
				HashMap<Integer, Integer> sens = new HashMap<Integer, Integer>();
				sens.put(1, 2);
				result = sens;

				config.getAntennaPower();
				HashMap<Integer, Integer> powe = new HashMap<Integer, Integer>();
				powe.put(1, 20);
				result = powe;

				config.getAntennaHop();
				HashMap<Integer, Integer> hop = new HashMap<Integer, Integer>();
				hop.put(1, 30);
				result = hop;

				config.getAntennaChannel();
				HashMap<Integer, Integer> chan = new HashMap<Integer, Integer>();
				chan.put(1, 40);
				result = chan;

				currentReaderConfiguration.getGpoWriteDataList();
				GPOWriteData gpoWriteData = new GPOWriteData(new TLVParameterHeader(), 1, true);
				List<GPOWriteData> listGpoWriteDatas = new ArrayList<>();
				listGpoWriteDatas.add(gpoWriteData);
				result = listGpoWriteDatas;

				config.getGpoState();
				HashMap<Integer, Boolean> gpost = new HashMap<Integer, Boolean>();
				gpost.put(1, true);
				result = gpost;

				currentReaderConfiguration.getGpiPortCurrentStateList();
				GPIPortCurrentState gpiPortCurrentState = new GPIPortCurrentState(new TLVParameterHeader(), 2, true, GPIPortCurrentStateGPIState.HIGH);
				List<GPIPortCurrentState> listGpiPortCurrentStates = new ArrayList<>();
				listGpiPortCurrentStates.add(gpiPortCurrentState);
				result = listGpiPortCurrentStates;
				
				config.getGpiEnable();
				HashMap<Integer, Boolean> gpist = new HashMap<Integer, Boolean>();
				gpist.put(2, true);
				result = gpist;
				
				setReaderConfigResponse.getStatus();
				LLRPStatus llrpStatus2 = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "Success");
				result = llrpStatus2;
			}
		};

		LLRPConfiguration llrpConfiguration = new LLRPConfiguration(llrpService, ProtocolVersion.LLRP_V1_1);

		llrpConfiguration.applyReaderConfig(config, 10000, capabilities);

		new Verifications() {
			{
				llrpService.setReaderConfig((SetReaderConfig) withCapture());
				times = 1;
			}
		};
	}

	@Test
	public void checkApplyReaderConfigFail(final @Mocked LLRPService llrpService, final @Mocked GetReaderConfigResponse getReaderConfigResponse,
			final @Mocked GetReaderCapabilitiesResponse capabilities, final @Mocked LLRPConfig config) throws LLRPException {
		new NonStrictExpectations() {
			{
				llrpService.getReaderConfig(new GetReaderConfig(new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID()), 0,
						GetReaderConfigRequestedData.ALL, 0, 0));

				result = getReaderConfigResponse;

				LLRPStatus llrpStatus = getReaderConfigResponse.getStatus();

				llrpStatus.getStatusCode();

				result = LLRPStatusCode.M_UNKNOWN_PARAMETER;
			}
		};

		LLRPConfiguration llrpConfiguration = new LLRPConfiguration(llrpService, ProtocolVersion.LLRP_V1_1);

		boolean exception = false;

		try {
			llrpConfiguration.applyReaderConfig(config, 10000, capabilities);
		} catch (LLRPException exc) {
			exception = true;
		}

		Assert.assertTrue(exception);
	}
}

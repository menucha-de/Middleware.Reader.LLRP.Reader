package havis.middleware.reader.llrp;

import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.GetReaderConfig;
import havis.llrpservice.data.message.GetReaderConfigRequestedData;
import havis.llrpservice.data.message.GetReaderConfigResponse;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.SetReaderConfig;
import havis.llrpservice.data.message.SetReaderConfigResponse;
import havis.llrpservice.data.message.parameter.AccessReportSpec;
import havis.llrpservice.data.message.parameter.AccessReportTrigger;
import havis.llrpservice.data.message.parameter.AntennaConfiguration;
import havis.llrpservice.data.message.parameter.AntennaProperties;
import havis.llrpservice.data.message.parameter.C1G2EPCMemorySelector;
import havis.llrpservice.data.message.parameter.EventNotificationState;
import havis.llrpservice.data.message.parameter.EventNotificationStateEventType;
import havis.llrpservice.data.message.parameter.EventsAndReports;
import havis.llrpservice.data.message.parameter.GPIPortCurrentState;
import havis.llrpservice.data.message.parameter.GPOWriteData;
import havis.llrpservice.data.message.parameter.KeepaliveSpec;
import havis.llrpservice.data.message.parameter.KeepaliveSpecTriggerType;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.ROReportSpec;
import havis.llrpservice.data.message.parameter.ROReportTrigger;
import havis.llrpservice.data.message.parameter.ReaderEventNotificationSpec;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.llrpservice.data.message.parameter.TagReportContentSelector;
import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.reader.Prefix;
import havis.middleware.reader.llrp.service.LLRPService;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.util.IDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class provides objects to hold LLRP configuration parameters.
 */
public class LLRPConfiguration {
	private LLRPService service;

	private ProtocolVersion version;

	/**
	 * Provides the LLRP Reader Service
	 * 
	 * @return {@link LLRPService}
	 */
	public LLRPService getService() {
		return service;
	}

	/**
	 * @param service
	 *            {@link LLRPService}
	 */
	public void setService(LLRPService service) {
		this.service = service;
	}

	/**
	 * Provides the LLRP Version
	 * 
	 * @return {@link ProtocolVersion}
	 */
	public ProtocolVersion getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            {@link ProtocolVersion}
	 */
	public void setVersion(ProtocolVersion version) {
		this.version = version;
	}

	/**
	 * Initializes a new instance of the
	 * havis.middleware.reader.LLRPConfiguration class.
	 * 
	 * @param service
	 *            {@link LLRPService}
	 * @param version
	 *            {@link ProtocolVersion}
	 */
	public LLRPConfiguration(LLRPService service, ProtocolVersion version) {
		this.service = service;
		this.version = version;
	}

	/**
	 * Validates the configuration and returns a new dictionary with parsed
	 * objects
	 * 
	 * @param properties
	 *            The properties as strings
	 * @return The properties as parsed object
	 * @throws ValidationException
	 */
	public static LLRPConfig validateConfigurationProperties(
			Map<String, String> properties) throws ValidationException {
		LLRPConfig config = new LLRPConfig();

		for (Map.Entry<String, String> property : properties.entrySet()) {
			try {
				switch (property.getKey()) {
				case "Reader.Config." + "Hold":
					config.setHold("1".equals(property.getValue()) ? Boolean.TRUE
							: Boolean.FALSE);
					break;
				default:
					Pattern antProperties = Pattern
							.compile("Reader.Config.Antenna\\.AntennaProperties\\.(?<id>[\\d]+)\\.(?<details>.+)");
					Pattern antConfiguration = Pattern
							.compile("Reader.Config.Antenna\\.AntennaConfiguration\\.(?<id>[\\d]+)\\.(?<details>.+)");
					Pattern gpi = Pattern
							.compile("Reader.Config.Ports\\.Inputs\\.(?<id>[\\d]+)\\.(?<details>.+)");
					Pattern gpo = Pattern
							.compile("Reader.Config.Ports\\.Outputs\\.(?<id>[\\d]+)\\.(?<details>.+)");
					Matcher matchAntProperties = antProperties.matcher(property
							.getKey());
					Matcher matchAntConfiguration = antConfiguration
							.matcher(property.getKey());
					Matcher matchGPI = gpi.matcher(property.getKey());
					Matcher matchGPO = gpo.matcher(property.getKey());

					if (matchAntProperties.matches()) {
						Integer id = Integer.valueOf(matchAntProperties
								.group("id"));
						switch (matchAntProperties.group("details")) {
						case "Connected":
							config.getAntennaConnected().put(id, ("1"
									.equals(property.getValue()) ? Boolean.TRUE
									: Boolean.FALSE));
							break;
						case "Gain":
							config.getAntennaGain().put(id,
									Integer.valueOf(property.getValue()));
							break;
						}
					}

					else if (matchAntConfiguration.matches()) {
						Integer id = Integer.valueOf(matchAntConfiguration
								.group("id"));
						switch (matchAntConfiguration.group("details")) {
						case "RF.Receiver.Sensitivity":
							config.getAntennaSensitivity().put(id,
									Integer.valueOf(property.getValue()));
							break;
						case "RF.Transmitter.Power":
							config.getAntennaPower().put(id,
									Integer.valueOf(property.getValue()));
							break;
						case "RF.Transmitter.Hop":
							config.getAntennaHop().put(id,
									Integer.valueOf(property.getValue()));
							break;
						case "RF.Transmitter.Channel":
							config.getAntennaChannel().put(id,
									Integer.valueOf(property.getValue()));
							break;
						}
					}

					else if (matchGPI.matches()) {
						Integer id = Integer.valueOf(matchGPI.group("id"));
						switch (matchGPI.group("details")) {
						case "Enable":
							config.getGpiEnable()
									.put(id,
											("1".equals(property.getValue()) ? Boolean.TRUE
													: Boolean.FALSE));
							break;
						}
					}

					else if (matchGPO.matches()) {
						Integer id = Integer.valueOf(matchGPO.group("id"));
						switch (matchGPO.group("details")) {
						case "State":
							config.getGpoState()
									.put(id,
											("1".equals(property.getValue()) ? Boolean.TRUE
													: Boolean.FALSE));
							break;
						}
					} else {
						if (property.getKey().startsWith(Prefix.Reader)) {
							throw new ValidationException(
									"Unkown reader property '" + property
											+ "' for LLPP reader!");
						}
					}
					break;
				}
			} catch (ValidationException ve) {
				throw ve;
			} catch (Exception e) {
				throw new ValidationException(property.getKey() + ": "
						+ e.getMessage());
			}
		}
		return config;
	}

	/**
	 * Class that provides all the settable prorperties of LLRP Reader
	 * DeviceConfiguration.
	 */
	public static class LLRPConfig {
		/**
		 * The EventsAndReports Parameter that represents wether the reader will
		 * deliver reports or events.
		 */
		private Boolean hold = null;

		private Map<Integer, Boolean> antennaConnected;

		/**
		 * The AntennaProperties Parameter AntennaGain that represents the
		 * compsite gain of the antenna.
		 */
		private Map<Integer, Integer> antennaGain;

		/**
		 * The RFReceiver Parameter ReceiveSensitivity that represents the RF
		 * receiver information.
		 */
		private Map<Integer, Integer> antennaSensitivity;

		/**
		 * The RFTransmitter Parameter Transmit Power that represents the
		 * transmit power for the antenna expressed as an index into the
		 * TransmitPowerTable.
		 */
		private Map<Integer, Integer> antennaPower;

		/**
		 * The RFTransmitter Parameter HopTableID that represents the index of
		 * the frequency hop tabel to be used by the reader.
		 */
		private Map<Integer, Integer> antennaHop;

		/**
		 * The RFTransmitter Parameter ChannelIndex that represents the
		 * one-based channel index in the FixedFrequencyTable to use during
		 * transmission.
		 */
		private Map<Integer, Integer> antennaChannel;

		/**
		 * The GPOWriteData Parameter GPO Data that represents the state to
		 * output on the specified GPO port.
		 */
		private Map<Integer, Boolean> gpoState;

		/**
		 * The GPIPortCurrentState Parameter GPIConfig that represents whether
		 * enable or disable the GPI port.
		 */
		private Map<Integer, Boolean> gpiEnable;

		public LLRPConfig() {
			setAntennaConnected (new HashMap<Integer, Boolean>());

			setAntennaGain(new HashMap<Integer, Integer>());

			setAntennaSensitivity(new HashMap<Integer, Integer>());

			setAntennaPower(new HashMap<Integer, Integer>());

			setAntennaHop(new HashMap<Integer, Integer>());

			setAntennaChannel(new HashMap<Integer, Integer>());

			setGpoState(new HashMap<Integer, Boolean>());

			setGpiEnable(new HashMap<Integer, Boolean>());
		}
		
		/**
		 * The AntennaProperties Parameter AntennaConnected that represents the
		 * connectivity status of the antenna.
		 * 
		 * @return connectivity status of the antenna
		 */
		public Boolean getHold() {
			return hold;
		}

		public void setHold(Boolean hold) {
			this.hold = hold;
		}

		public Map<Integer, Boolean> getAntennaConnected() {
			return antennaConnected;
		}

		public void setAntennaConnected(Map<Integer, Boolean> antennaConnected) {
			this.antennaConnected = antennaConnected;
		}

		public Map<Integer, Integer> getAntennaGain() {
			return antennaGain;
		}

		public void setAntennaGain(Map<Integer, Integer> antennaGain) {
			this.antennaGain = antennaGain;
		}

		public Map<Integer, Integer> getAntennaSensitivity() {
			return antennaSensitivity;
		}

		public void setAntennaSensitivity(
				Map<Integer, Integer> antennaSensitivity) {
			this.antennaSensitivity = antennaSensitivity;
		}

		public Map<Integer, Integer> getAntennaPower() {
			return antennaPower;
		}

		public void setAntennaPower(Map<Integer, Integer> antennaPower) {
			this.antennaPower = antennaPower;
		}

		public Map<Integer, Integer> getAntennaHop() {
			return antennaHop;
		}

		public void setAntennaHop(Map<Integer, Integer> antennaHop) {
			this.antennaHop = antennaHop;
		}

		public Map<Integer, Integer> getAntennaChannel() {
			return antennaChannel;
		}

		public void setAntennaChannel(Map<Integer, Integer> antennaChannel) {
			this.antennaChannel = antennaChannel;
		}

		public Map<Integer, Boolean> getGpoState() {
			return gpoState;
		}

		public void setGpoState(Map<Integer, Boolean> gpoState) {
			this.gpoState = gpoState;
		}

		public Map<Integer, Boolean> getGpiEnable() {
			return gpiEnable;
		}

		public void setGpiEnable(Map<Integer, Boolean> gpiEnable) {
			this.gpiEnable = gpiEnable;
		}
	}

	/**
	 * Applys the configuration to reader
	 * 
	 * @param configuration
	 *            The configuration with parsed parameter values
	 * @param keepalive
	 *            The keepalive value
	 * @param capabilities
	 *            The llrp capabilities of the reader
	 * @throws LLRPException
	 */
	public void applyReaderConfig(LLRPConfig configuration, int keepalive,
			GetReaderCapabilitiesResponse capabilities) throws LLRPException {
		GetReaderConfigResponse currentReaderConfiguration = this.service
				.getReaderConfig(new GetReaderConfig(new MessageHeader(
						(byte) 0, this.version, IDGenerator
								.getUniqueMessageID()), 0,
						GetReaderConfigRequestedData.ALL, 0, 0));
		if (currentReaderConfiguration.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("ApplyReaderConfig: "
					+ currentReaderConfiguration.getStatus().getStatusCode()
							.toString() + ":"
					+ currentReaderConfiguration.getStatus().getStatusCode());
		}

		SetReaderConfig newReaderConfiguration = new SetReaderConfig(
				new MessageHeader((byte) 0, this.version,
						IDGenerator.getUniqueMessageID()), false);
		// Set ReaderEventNotification

		List<EventNotificationState> eventNotificationStates = new ArrayList<EventNotificationState>();

		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.UPON_HOPPING_TO_NEXT_CHANNEL,
				true));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.GPI_EVENT, true));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.ROSPEC_EVENT, true));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.REPORT_BUFFER_FILL_WARNING,
				false));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.READER_EXCEPTION_EVENT, true));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.RFSURVEY_EVENT, false));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.AISPEC_EVENT, true));
		eventNotificationStates
				.add(new EventNotificationState(
						new TLVParameterHeader(),
						EventNotificationStateEventType.AISPEC_EVENT_WITH_SINGULATION_DETAILS,
						false));
		eventNotificationStates.add(new EventNotificationState(
				new TLVParameterHeader(),
				EventNotificationStateEventType.ANTENNA_EVENT, true));

		newReaderConfiguration
				.setReaderEventNotificationSpec(new ReaderEventNotificationSpec(
						new TLVParameterHeader(), eventNotificationStates));

		// Set EventsAndReports
		if (configuration.getHold() != null) {
			newReaderConfiguration.setEventAndReports(new EventsAndReports(
					new TLVParameterHeader(), Boolean.TRUE.equals(configuration
							.getHold())));
		}

		// Set AntennaProperties
		if (capabilities.getGeneralDeviceCap().isCanSetAntennaProperties()) {
			newReaderConfiguration
					.setAntennaPropertiesList(currentReaderConfiguration
							.getAntennaPropertiesList());
			for (AntennaProperties property : newReaderConfiguration
					.getAntennaPropertiesList()) {
				Integer antennaId = Integer.valueOf(property.getAntennaID());
				if (configuration.getAntennaConnected().containsKey(antennaId)) {
					property.setConnected(Boolean.TRUE.equals(configuration
							.getAntennaConnected().get(antennaId)));
				}
				if (configuration.getAntennaGain().containsKey(antennaId)) {
					property.setAntennaGain(configuration.getAntennaGain()
							.get(antennaId).shortValue());
				}
			}
		}

		// Set AntennaConfiguration
		newReaderConfiguration
				.setAntennaConfigurationList(currentReaderConfiguration
						.getAntennaConfigurationList());
		for (AntennaConfiguration property : newReaderConfiguration
				.getAntennaConfigurationList()) {
			property.getC1g2InventoryCommandList().clear();
			Integer antennaId = Integer.valueOf(property.getAntennaID());
			if (configuration.getAntennaSensitivity().containsKey(antennaId)) {
				property.getRfReceiver().setReceiverSensitivity(
						configuration.getAntennaSensitivity().get(antennaId)
								.intValue());
			}

			if (configuration.getAntennaPower().containsKey(antennaId)) {
				property.getRfTransmitter().setTransmitPower(
						configuration.getAntennaPower().get(antennaId)
								.intValue());
			}

			if (configuration.getAntennaHop().containsKey(antennaId)) {
				property.getRfTransmitter()
						.setHopTableID(
								configuration.getAntennaHop().get(antennaId)
										.intValue());
			}
			if (configuration.getAntennaChannel().containsKey(antennaId)) {
				property.getRfTransmitter().setChannelIndex(
						configuration.getAntennaChannel().get(antennaId)
								.intValue());
			}
		}

		// Set ROReportSpec
		newReaderConfiguration
				.setRoReportSpec(new ROReportSpec(
						new TLVParameterHeader(),
						ROReportTrigger.UPON_N_TAGREPORTDATA_PARAMETERS_OR_END_OF_ROSPEC,
						1, new TagReportContentSelector(
								new TLVParameterHeader(), true, true, true,
								true, false, true, false, false, false, true)));
		newReaderConfiguration
				.getRoReportSpec()
				.getTagReportContentSelector()
				.setC1g2EPCMemorySelectorList(
						new ArrayList<C1G2EPCMemorySelector>());
		newReaderConfiguration
				.getRoReportSpec()
				.getTagReportContentSelector()
				.getC1g2EPCMemorySelectorList()
				.add(new C1G2EPCMemorySelector(new TLVParameterHeader(), true,
						true, false));

		// Set AccessReportSpec
		newReaderConfiguration.setAccessReportSpec(new AccessReportSpec(
				new TLVParameterHeader(),
				AccessReportTrigger.WHENEVER_ROREPORT_IS_GENERATED));

		// Set Keepalive
		newReaderConfiguration.setKeepaliveSpec(new KeepaliveSpec(
				new TLVParameterHeader(), KeepaliveSpecTriggerType.PERIODIC,
				keepalive));

		// Set GPOWriteDate
		newReaderConfiguration.setGpoWriteDataList(currentReaderConfiguration
				.getGpoWriteDataList());

		for (GPOWriteData property : newReaderConfiguration
				.getGpoWriteDataList()) {
			Integer gpoPortNum = Integer.valueOf(property.getGpoPortNum());
			if (configuration.getGpoState().containsKey(gpoPortNum)) {
				property.setGpoState(Boolean.TRUE.equals(configuration
						.getGpiEnable().get(gpoPortNum)));
			}
		}

		// Set GPIPortCurrentState
		newReaderConfiguration
				.setGpiPortCurrentStateList(currentReaderConfiguration
						.getGpiPortCurrentStateList());

		for (GPIPortCurrentState property : newReaderConfiguration.getGpiPortCurrentStateList()) {
			Integer gpiPortNum = Integer.valueOf(property.getGpiPortNum());
			if (configuration.getGpiEnable().containsKey(gpiPortNum)) {
				property.setGpiConfig(Boolean.TRUE.equals(configuration.getGpiEnable().get(gpiPortNum)));
			} else {
				// default to true for inputs
				property.setGpiConfig(true);
			}
		}

		SetReaderConfigResponse response = this.service
				.setReaderConfig(newReaderConfiguration);

		if (response.getStatus().getStatusCode() != LLRPStatusCode.M_SUCCESS) {
			throw new LLRPException("ApplyReaderConfig: "
					+ response.getStatus().getStatusCode().toString() + ": "
					+ response.getStatus().getErrorDescription());
		}
	}
}

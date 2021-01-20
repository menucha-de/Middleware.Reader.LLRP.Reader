package havis.middleware.reader.llrp;

import havis.middleware.ale.reader.Callback;

/**
 * Class that provide all functionality for communicating with an LLRP reader.
 */
public class LLRPReaderConnector extends ALLRPReaderConnector {
	/**
	 * Initializes a new instance of the
	 * {@link havis.middleware.reader.llrp.LLRPReaderConnector} class.
	 */
	public LLRPReaderConnector() {
		this.manager = new LLRPManager(this);
		this.readerType = "LLRP";
	}

	/**
	 * Initializes a new instance of the
	 * {@link havis.middleware.reader.llrp.LLRPReaderConnector} class.
	 * 
	 * @param callback
	 */
	public LLRPReaderConnector(Callback callback) {
		super(callback);

		this.manager = new LLRPManager(this);
		this.readerType = "LLRP";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * havis.middleware.ale.reader.ReaderConnector#setCallback(havis.middleware
	 * .ale.reader.Callback)
	 */
	@Override
	public void setCallback(Callback callback) {
		this.clientCallback = callback;
		if (callback != null) {
			deviceCapabilities.setName(callback.getName());
		}
	}
}

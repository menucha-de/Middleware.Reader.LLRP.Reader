package havis.middleware.reader.llrp;

/**
 * Error handler
 */
public interface ErrorHandler {
	/**
	 * Notify about an error
	 * @param error the error
	 */
	public void notifyError(Exception error);
}

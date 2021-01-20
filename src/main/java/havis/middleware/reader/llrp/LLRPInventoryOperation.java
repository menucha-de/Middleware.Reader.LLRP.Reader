package havis.middleware.reader.llrp;

import java.util.Arrays;

/**
 * Class that represents an Inventory Operation
 */
public class LLRPInventoryOperation implements Cloneable {
	private boolean reserved = false;
	private boolean epc = false;
	private boolean tid = false;
	private UserReadMode user;
	private boolean[] userDataBlocks;

	public enum UserReadMode {
		OFF, ON, ON_COMPLETE
	}

	/**
	 * Gets an indicator if reserved bank should be read during inventory.
	 * 
	 * @return
	 */
	public boolean isReserved() {
		return reserved;
	}

	/**
	 * Sets an indicator if reserved bank should be read during inventory.
	 * 
	 * @param reserved
	 */
	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	/**
	 * Gets an indicator if EPC bank should be read during inventory.
	 * 
	 * @return
	 */
	public boolean isEpc() {
		return epc;
	}

	/**
	 * Sets an indicator if EPC bank should be read during inventory.
	 * 
	 * @param epc
	 */
	public void setEpc(boolean epc) {
		this.epc = epc;
	}

	/**
	 * Gets an indicator if TID bank should be read during inventory.
	 * 
	 * @return
	 */
	public boolean isTid() {
		return tid;
	}

	/**
	 * Sets an indicator if TID bank should be read during inventory.
	 * 
	 * @param tid
	 */
	public void setTid(boolean tid) {
		this.tid = tid;
	}

	/**
	 * Gets an indicator if user bank should be read during inventory.
	 * 
	 * @return the user
	 */
	public boolean isUser() {
		return user == UserReadMode.ON || user == UserReadMode.ON_COMPLETE;
	}

	/**
	 * Gets an indicator if user bank should be read completely during
	 * inventory.
	 * 
	 * @return the indicator
	 */
	public boolean isForceUserComplete() {
		return user == UserReadMode.ON_COMPLETE;
	}

	/**
	 * Sets an indicator if user bank should be read during inventory.
	 * 
	 * @param user
	 */
	public void setUser(UserReadMode user) {
		this.user = user;
	}

	/**
	 * Gets the data blocks that will be read of user bank.
	 * 
	 * @return
	 */
	public boolean[] getUserDataBlocks() {
		return userDataBlocks;
	}

	/**
	 * Sets the data blocks that will be read of user bank.
	 * 
	 * @param userDataBlocks
	 */
	public void setUserDataBlocks(boolean[] userDataBlocks) {
		this.userDataBlocks = userDataBlocks;
	}

	@Override
	protected LLRPInventoryOperation clone() {
		try {
			return (LLRPInventoryOperation) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "LLRPInventoryOperation [reserved=" + reserved + ", epc=" + epc + ", tid=" + tid + ", user=" + user + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (epc ? 1231 : 1237);
		result = prime * result + (reserved ? 1231 : 1237);
		result = prime * result + (tid ? 1231 : 1237);
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + Arrays.hashCode(userDataBlocks);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LLRPInventoryOperation))
			return false;
		LLRPInventoryOperation other = (LLRPInventoryOperation) obj;
		if (epc != other.epc)
			return false;
		if (reserved != other.reserved)
			return false;
		if (tid != other.tid)
			return false;
		if (user != other.user)
			return false;
		if (!Arrays.equals(userDataBlocks, other.userDataBlocks))
			return false;
		return true;
	}
}

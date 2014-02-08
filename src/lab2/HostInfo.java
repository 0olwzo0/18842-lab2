package lab2;

import java.net.*;

public class HostInfo {
	private InetAddress address;
	private int port;

	public HostInfo(String address, int port) {
		try {
			this.address = InetAddress.getByName(address);
			this.port = port;
		} catch (Exception e) {
			System.err.println("Cannot get IP address" + address + ":" + port);
		}
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public String toString() {
		return "HostInfo[IP=" + this.address + ",port=" + this.port + "]";
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof HostInfo))
			return false;
		if (other == this)
			return true;
		HostInfo otherHost = (HostInfo) other;
		if (this.address.equals(otherHost.address) && this.port == otherHost.port) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Also need to override this for the HashMap's key
	 * 
	 */
	@Override
	public int hashCode() {
		int result = new String(this.address + ":" + this.port).hashCode();
		return result;
	}
}

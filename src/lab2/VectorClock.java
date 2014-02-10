package lab2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class VectorClock extends Clock implements Serializable {

	private static final long serialVersionUID = 3969168318779636522L;
	private Hashtable<String, Integer> clock;
	private String hostName;

	public VectorClock() {
		this.clock = new Hashtable<String, Integer>();
	};

	public VectorClock(Hosts hosts, String hostName) {
		this.clock = new Hashtable<String, Integer>();
		this.hostName = hostName;
		for (String host : hosts.getHosts().keySet()) {
			this.clock.put(host, 0);
		}
	}
	
	/*
	 * Constructor for multicast clocks
	 */
	public VectorClock(List<String> groupMembers, String hostName) {
		this.clock = new Hashtable<String, Integer>();
		this.hostName = hostName;
		for (String member : groupMembers) {
			this.clock.put(member, 0);
		}
	}

	@Override
	synchronized public void addClock() {
		if (this.clock.containsKey(this.hostName)) {
			this.clock.put(this.hostName, this.clock.get(this.hostName) + 1);
		}
	}

	@Override
	synchronized public void adjustClock(Clock receivedTimeStamp) {
		if (receivedTimeStamp == null)
			System.out.println("---------------------------debug: received group clock null!!");
		Hashtable<String, Integer> tmpTimeStamp = (Hashtable<String, Integer>) receivedTimeStamp.getClock();
		for (String tmpKey : this.clock.keySet()) {
			if (tmpTimeStamp.containsKey(tmpKey)) {
				if (!tmpKey.equals(this.hostName)) {
					this.clock.put(
							tmpKey,
							Math.max(tmpTimeStamp.get(tmpKey),
									this.clock.get(tmpKey)));
				} else {
					this.clock.put(
							tmpKey,
							Math.max(tmpTimeStamp.get(tmpKey),
									this.clock.get(tmpKey)) + 1);
				}
			}
		}
	}

	@Override
	public String toString() {
		String[] hostNameStr = new String[this.clock.size()];
		int index = 0;
		for(String key: this.clock.keySet()){
			hostNameStr[index++] = key;
		}
		Arrays.sort(hostNameStr);
		String tmp = "";

		for (int i = 0; i < hostNameStr.length; i++) {
			if (this.clock.containsKey(hostNameStr[i])) {
				tmp += "(" + hostNameStr[i] + " = "
						+ this.clock.get(hostNameStr[i]) + ") ";
			}
		}

		return "VectorTimeStampClock[" + tmp + "]";
	}

	@Override
	public Object getClock() {
		return (Object) this.clock;
	}


	@Override
	public Clock deepCopy() {
		VectorClock newClock = new VectorClock();
		newClock.hostName = this.hostName;
		for (String key : this.clock.keySet()) {
			newClock.clock.put(key, this.clock.get(key));
		}
		return newClock;
	}
	
	@Override
	public int compareClock(Clock otherClock) {
		VectorClock otherVectorClock = (VectorClock)otherClock;
		boolean greater = true;
		boolean less = true;
		for (String tmpKey : this.clock.keySet()) {
			if (otherVectorClock.clock.containsKey(tmpKey)) {
				if (this.clock.get(tmpKey) > otherVectorClock.clock.get(tmpKey)) {
					less = false;
				}
				if (this.clock.get(tmpKey) < otherVectorClock.clock.get(tmpKey)) {
					greater = false;
				}
			} else {
				System.out.println("ERROR, NO THIS HOST VECTORCLOCK");
			}
		}

		// thisClock < otherClock
		
		if (less && !greater)
			return -1;
		// thisClock < otherClock
		if (greater && !less)
			return 1;
		// thisClock = otherClock
		return 0;
	}
}

package lab2;

import java.io.Serializable;

public class LogicClock extends Clock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8977412908820943347L;
	int clock;

	public LogicClock() {
		this.clock = 0;
	}

	synchronized public void addClock() {
		this.clock++;
	}

	synchronized public void adjustClock(Clock receivedTimeStamp) {
		this.clock = Math.max(this.clock,
				((LogicClock) receivedTimeStamp).clock) + 1;
	}

	@Override
	public String toString() {
		return "LogicTimeStampClock[" + this.clock + "]";
	}

	@Override
	public Object getClock() {
		return (Object) this.clock;
	}

	@Override
	public Clock deepCopy() {
		LogicClock newClock = new LogicClock();
		newClock.clock = this.clock;
		return newClock;
	}
	
	
	public int compareClock(Clock otherClock) {
		LogicClock otherLocalClock = (LogicClock)otherClock;
		// thisClock < otherClock
		if (this.clock < otherLocalClock.clock)
			return -1;
		// thisClock > otherClock
		if (this.clock > otherLocalClock.clock)
			return 1;
		// thisClock = otherClock
		return 0;
	}
}

package lab2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class Clock 
 * variables: vectorClock, logicClock;
 * @author wenzheli
 *
 */
public abstract class Clock implements Serializable{
	
	private static final long serialVersionUID = -1572979670042116707L;
	public static Clock makeClock(String clockType, Hosts hosts, String hostName){
		Clock clock = null;
		switch(clockType){
		case "vector":
			clock =  (Clock)(new VectorClock(hosts, hostName));
			break;
		case "logical":
			clock = (Clock)(new LogicClock());
			break;
		
		}
		return clock;
	}
	
	public abstract void addClock();
	public abstract void adjustClock(Clock receivedTimeStamp);
	public abstract Object getClock();
	public abstract Clock deepCopy();
	public abstract int compareClock(Clock otherClock);
}

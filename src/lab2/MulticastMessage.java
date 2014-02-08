package lab2;

import java.io.Serializable;
import java.util.Hashtable;

public class MulticastMessage extends Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 403804568197137128L;
	protected Clock timeStamp;
	private boolean concurrent;
	private String groupName;
 	protected Hashtable <String, Integer> acknowledgement = null;
 	
 	
 	public MulticastMessage(){};
 	public MulticastMessage(String dest, String kind, Object data, Clock timeStamp){
 		super(dest, kind, data);
 		this.timeStamp = timeStamp;
 		this.concurrent = false;
 		this.groupName = null;
 	}
 	
 	public MulticastMessage(Message originalMessage, String gName, Clock timeStamp, Hashtable <String, Integer> Rpg){
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 	 	this.kind = originalMessage.kind;
 	 	this.seqNum = originalMessage.seqNum;
 	 	this.duplicate = originalMessage.duplicate;
 	 	this.data = originalMessage.data;
 	 	this.timeStamp = timeStamp;
 	 	this.groupName = gName;
 		this.concurrent = false;
 		for(String tmpKey : Rpg.keySet()){
 			this.acknowledgement.put(tmpKey, Rpg.get(tmpKey));
 		}
 	}
 	
 	public MulticastMessage(TimeStampedMessage originalMessage, Hashtable <String, Integer> Rpg) {
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 		this.kind = originalMessage.kind;
 		this.seqNum = originalMessage.seqNum;
 		this.duplicate = originalMessage.duplicate; // * Important
 		this.data = originalMessage.data; // clone?
 		this.concurrent = false;
 		this.timeStamp = originalMessage.timeStamp;
 		for(String tmpKey : Rpg.keySet()){
 			this.acknowledgement.put(tmpKey, Rpg.get(tmpKey));
 		}
 	}
 	
 	public MulticastMessage(MulticastMessage originalMessage) {
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 		this.kind = originalMessage.kind;
 		this.seqNum = originalMessage.seqNum;
 		this.groupName = originalMessage.groupName;
 		this.duplicate = originalMessage.duplicate; // * Important
 		this.data = originalMessage.data; // clone?
 		this.concurrent = true;
 		this.timeStamp = originalMessage.timeStamp;
 		this.acknowledgement = originalMessage.acknowledgement;
 	}
 	
 	public void setConcurrent(){
 		this.concurrent = true;
 	}
 	
 	public boolean getConcurrent(){
 		return this.concurrent;
 	}
 	
 	public void setGroupName(String gName){
 		this.groupName = gName;
 	}
 	
 	public String getGroupName(){
 		return this.groupName;
 	}
 	
 	
 	
 	public void setTimeStamp(Clock hostTimeStamp){
 		this.timeStamp = hostTimeStamp;
 	}
 	
 	public Clock getTimeStamp(){
 		return this.timeStamp;
 	}
 	
 	public Hashtable <String, Integer> getAcknowledgement(){
 		return this.acknowledgement;
 	}
 	
 	public void setAcknowledgement(Hashtable <String, Integer> Rpg){
 		for(String key : Rpg.keySet()){
 			this.acknowledgement.put(key, Rpg.get(key));
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return "Message["+ source +"->"+ dest +" seqNum:"+ seqNum +" duplicate:"+ duplicate +" kind:"+ kind
 				+" data:"+ data +" \nTimeStamp:" + this.timeStamp + " \nAcknowledgement:" + this.acknowledgement + "]\n";
 	}
}

package lab2;

import java.io.Serializable;

public class TimeStampedMessage extends Message implements Serializable {
 	/**
	 * 
	 */
	private static final long serialVersionUID = 403804568197137128L;
	protected Clock timeStamp;
	private boolean concurrent;
 	
 	public TimeStampedMessage(String dest, String kind, Object data, Clock timeStamp){
 		super(dest, kind, data);
 		this.timeStamp = timeStamp;
 		this.concurrent = false;
 	}
 	
 	public TimeStampedMessage(Message originalMessage, Clock timeStamp){
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 	 	this.kind = originalMessage.kind;
 	 	this.seqNum = originalMessage.seqNum;
 	 	this.duplicate = originalMessage.duplicate;
 	 	this.data = originalMessage.data;
 	 	this.timeStamp = timeStamp;
 		this.concurrent = false;

 	}
 	
 	/**
 	 * Create a duplicate of the original message.
 	 * @param originalMessage
 	 */
 	public TimeStampedMessage(TimeStampedMessage originalMessage) {
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 		this.kind = originalMessage.kind;
 		this.seqNum = originalMessage.seqNum;
 		this.duplicate = true; // * Important
 		this.data = originalMessage.data; // clone?
 		this.timeStamp = originalMessage.getTimeStamp().deepCopy(); // clone?
 		this.concurrent = originalMessage.concurrent;

 	}
 	
 	public void setConcurrent(){
 		this.concurrent = true;
 	}
 	
 	public boolean getConcurrent(){
 		return this.concurrent;
 	}
 	
 	public void setTimeStamp(Clock hostTimeStamp){
 		this.timeStamp = hostTimeStamp;
 	}
 	
 	public Clock getTimeStamp(){
 		return this.timeStamp;
 	}
 	
 	@Override
 	public String toString() {
 		return "Message["+source+"->"+dest+" seqNum:"+seqNum+" duplicate:"+duplicate+" kind:"+kind+" data:"+data+" TimeStamp:" + this.timeStamp + "]";
 	}
 	
 	

}

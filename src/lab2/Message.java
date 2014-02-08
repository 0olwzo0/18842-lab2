package lab2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -837352800857240059L;
	protected String source;
	protected String dest;
 	protected String kind;
 	protected int seqNum;
 	protected boolean duplicate;
 	protected Object data;
 	
 	
 	public Message(){};
 	public Message(String dest, String kind, Object data){
 		this.dest = dest;
 		this.kind = kind;
 		this.data = data;
 	}
 	
 	/**
 	 * Create a duplicate of the original message.
 	 * @param originalMessage
 	 */
 	public Message(Message originalMessage) {
 		this.source = originalMessage.source;
 		this.dest = originalMessage.dest;
 		this.kind = originalMessage.kind;
 		this.seqNum = originalMessage.seqNum;
 		this.duplicate = true; // * Important
 		this.data = originalMessage.data; // clone?
 	}
 	
 	public void setSource(String source){
 		this.source = source;
 	}
 	
 	public String getSource(){
 		return this.source;
 	}
 	
 	public String getDest(){
 		return this.dest;
 	}
 	
 	public void setSeqNum(int seqNum){
 		this.seqNum = seqNum;
 	}
 	
 	public int getSeqNum(){
 		return this.seqNum;
 	}
 	
 	public void setDuplicate(boolean duplicate){
 		this.duplicate = duplicate;
 	}
 	
 	public boolean getDuplicate(){
 		return this.duplicate;
 	}
 	
 	public String getKind(){
 		return this.kind;
 	}
 	
 	public Object getData(){
 		return this.data;
 	}
 	
 	public String toString() {
 		return "Message["+source+"->"+dest+" seqNum:"+seqNum+" duplicate:"+duplicate+" kind:"+kind+" data:"+data+"]";
 	}

}

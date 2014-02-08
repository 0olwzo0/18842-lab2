package lab2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

public class MulticastMessagePasser{
	/**
	 * groupRpg stores the Rpg for each node in each group;
	 */
	public String localHostName;
	public MessagePasser messagePasser;
	public GroupsRpg groupRpg = null;
	/**
	 * groupSpg stores the local Spg in each group;
	 */
	public Hashtable<String, Integer> groupSpg = null;
	public Hashtable<Integer, MulticastMessage> sentQueue = new Hashtable<Integer, MulticastMessage>();
	
	
	public MulticastMessagePasser(MessagePasser gMessagePasser){
		this.groupSpg = new Hashtable<String, Integer> ();
		this.localHostName = gMessagePasser.getLocalName();
		this.messagePasser = gMessagePasser;
		for(String key : gMessagePasser.groups.groups.keySet()){
			this.groupSpg.put(key, 0);
		}
		this.groupRpg = gMessagePasser.groups;
		
		this.sentQueue = new Hashtable<Integer, MulticastMessage>();

	}
	
	
	public void send(String groupName, Message message) {
		
		message.setSeqNum(this.groupSpg.get(groupName));
		this.groupSpg.put(groupName, this.groupSpg.get(groupName) + 1);

		for(String mem : this.groupRpg.groups.get(groupName).keySet()){
			if(mem.equals(this.localHostName)){
				message.setDest(mem);
				MulticastMessage multicastMsg = new MulticastMessage(message, groupName, this.messagePasser.getTimeStamp(),
						new Hashtable<String, Integer>(this.groupRpg.groups.get(groupName)));
				
				this.messagePasser.send(multicastMsg);
			}
		}
	}
	
	public void receive(){
		
	}
	
	public adjustRpg(){
		
	}
	
	
	
	
	
	
	

}

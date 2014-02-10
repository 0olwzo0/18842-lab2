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
	public Hashtable<String, Hashtable<Integer, MulticastMessage>> sentQueue;
	
	
	public MulticastMessagePasser(MessagePasser gMessagePasser){
		this.groupSpg = new Hashtable<String, Integer> ();
		this.localHostName = gMessagePasser.getLocalName();
		this.messagePasser = gMessagePasser;
		for(String key : gMessagePasser.groupsRpg.groups.keySet()){
			this.groupSpg.put(key, 0);
		}
		this.groupRpg = gMessagePasser.groupsRpg;
		this.sentQueue = new Hashtable <String, Hashtable<Integer, MulticastMessage>>(); 
		for(String tmpGroup : gMessagePasser.groupsRpg.groups.keySet()){
			Hashtable <Integer, MulticastMessage> sentMessage = new Hashtable<Integer, MulticastMessage>();
			this.sentQueue.put(tmpGroup, sentMessage);
		}

	}
	
	
	public void send(String groupName, Message message) {
		
		message.setSeqNum(this.groupSpg.get(groupName));
		this.groupRpg.groups.get(groupName).put(this.localHostName, this.groupSpg.get(groupName));
		this.groupSpg.put(groupName, this.groupSpg.get(groupName) + 1);
		
		// update local group vector clock
		this.groupRpg.getClockGroup().get(groupName).addClock();
		
		// get local group vector clock's copy
		Clock clockForThisGroup = this.groupRpg.getClockGroup().get(groupName).deepCopy();
		

		for(String mem : this.groupRpg.groups.get(groupName).keySet()){
			if(!mem.equals(this.localHostName)){
				message.setDest(mem);
				MulticastMessage multicastMsg = new MulticastMessage(message, groupName, this.messagePasser.getTimeStamp(),
						new Hashtable<String, Integer>(this.groupRpg.groups.get(groupName)), 
						clockForThisGroup);
				
				this.sentQueue.get(groupName).put(multicastMsg.getSeqNum(), multicastMsg);
				this.messagePasser.send(multicastMsg);
			}
		}
	}
	
	public MulticastMessage receive(MulticastMessage mMsg){
		//if NACK
		//resent
		if(mMsg.getKind().equals("NACK")){
			int lastSeq = mMsg.getSeqNum();
			String groupName = mMsg.getGroupName();
			System.out.println("RECEIVE NACK MESSAGE, START RESEND");
			for(int i = lastSeq; i < this.groupSpg.get(groupName); i++){
				MulticastMessage reSendMessage = new MulticastMessage(this.sentQueue.get(groupName).get(i));
				//change set
				//if()
				reSendMessage.setKind("normal");
				this.messagePasser.send(reSendMessage);
			}
			return null;
		}
		else{
			int thisSeq = mMsg.getSeqNum();
			String groupName = mMsg.getGroupName();
			String src = mMsg.getSource();
			String dest = mMsg.getDest();
			boolean validOriginMsg = true;
			
			if(thisSeq == this.groupRpg.groups.get(groupName).get(src) + 1){
				//return mMsg;
				this.groupRpg.groups.get(groupName).put(src, thisSeq);
			}
			else if(thisSeq > this.groupRpg.groups.get(groupName).get(src) + 1){ // send NACK ask for resend
				Message NACKmsg = new Message(src, "NACK", null);
				NACKmsg.setSource(this.localHostName);
				NACKmsg.setSeqNum(this.groupRpg.groups.get(groupName).get(src) + 1);
				MulticastMessage multicastNackMsg = new MulticastMessage(NACKmsg, groupName, this.messagePasser.getTimeStamp(),
						new Hashtable<String, Integer>(this.groupRpg.groups.get(groupName)),
						groupRpg.getClockGroup().get(groupName));
				this.messagePasser.send(multicastNackMsg);
				//mMsg = null;
				validOriginMsg = false;
			}
			else {
				//mMsg = null;
				validOriginMsg = false;
			}
			
			for(String memName : this.groupRpg.groups.get(groupName).keySet()){
				if((!memName.equals(src)) && (!memName.equals(this.localHostName))&& (this.groupRpg.groups.get(groupName).get(memName) 
						< mMsg.acknowledgement.get(memName))){
					Message NACKmsg = new Message(memName, "NACK", null);
					NACKmsg.setSource(this.localHostName);
					NACKmsg.setSeqNum(this.groupRpg.groups.get(groupName).get(memName) + 1);
					MulticastMessage multicastNackMsg = new MulticastMessage(NACKmsg, groupName, this.messagePasser.getTimeStamp(),
							new Hashtable<String, Integer>(this.groupRpg.groups.get(groupName)), 
							groupRpg.getClockGroup().get(groupName));
					this.messagePasser.send(multicastNackMsg);
				}
				else if((!memName.equals(src)) && (!memName.equals(this.localHostName)) && this.groupRpg.groups.get(groupName).get(memName) 
				>= mMsg.acknowledgement.get(memName)){
					this.groupRpg.groups.get(groupName)
					.put(memName,this.groupRpg.groups.get(groupName).get(memName));
				}
			}
			
			if(!validOriginMsg)
				mMsg = null;
			return mMsg;
		}
		
		
		
	}

}

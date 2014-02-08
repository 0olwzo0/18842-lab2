package lab2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GroupsRpg {
	public Hashtable<String, Hashtable <String, Integer>> groups = new Hashtable<String, Hashtable <String, Integer>> ();
	
	public GroupsRpg(){}
	public GroupsRpg(Object groups){
		ArrayList<Object> groupList = (ArrayList<Object>) groups;
		for(Object group : groupList){
			String groupName = null;
			ArrayList<String> groupMem = new ArrayList<String>();
			Map<String, Object> groupInfo = (Map<String, Object>) group;
			for (Map.Entry<String, Object> entry : groupInfo.entrySet()){
				Object value = entry.getValue();
				String key = entry.getKey();
				if("name".equals(key)){
					groupName = (String) value;
				}
				else if("members".equals(key)){
					groupMem =  (ArrayList<String>) value;
				}
				else{
					System.out.println("ERROR IN GROUP FILE");
				}
				
				Hashtable <String, Integer> Rpg = new Hashtable<String, Integer>(); 
				for(String tmp : groupMem){
					Rpg.put(tmp, -1);
				}
				
				this.groups.put(groupName, Rpg);
			}
			
			
		}
	}
	
	public GroupsRpg deepCopy(){
		GroupsRpg copy = new GroupsRpg();
		for(String key : this.groups.keySet()){
			copy.groups.put(key, new Hashtable<String, Integer> (this.groups.get(key)));
		}
		return copy;
	}
	
	
	public Hashtable <String, Integer> getGroupByName(String groupName){
		return this.groups.get(groupName);
	}
	
	
	@Override
	public String toString() {
		String toString = "{\n";
		for (Entry<String, Hashtable<String, Integer>> entry : this.groups.entrySet()) {
			Hashtable<String, Integer> tmp = entry.getValue();
			toString += entry.getKey() + ":";
			Object [] arrMem = tmp.keySet().toArray(); 
			Arrays.sort(arrMem);
			for(int i = 0; i < arrMem.length; i++){
				toString +=  "[" + arrMem[i] + ": " + entry.getValue().get(arrMem[i]) + "] ";
			}
			//toString += tmp.keySet().toString();
			toString += "\n";
		}
		toString += "}\n";
		return toString;
	}
}

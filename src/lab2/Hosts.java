package lab2;

import java.util.*;

public class Hosts {
	private HashMap<String, HostInfo> hosts;
	
	public Hosts(Object hosts){
		this.hosts = new HashMap<String, HostInfo>();
		String name;
		String IP;
		Integer port;
		Object value;
		ArrayList<Object> hostList = (ArrayList<Object>) hosts;
		for (Object host : hostList) {
			name = null;
			IP = null;
			port = null;
			Map<String, Object> hostMap = (Map<String, Object>) host;
			for (Map.Entry<String, Object> entry : hostMap.entrySet()) {
				value = entry.getValue();
				String key = entry.getKey();
				if ("name".equals(key)) {
					name = (String) value;
				} else if ("ip".equals(key)) {
					IP = (String) value;
				} else if ("port".equals(key)) {
					port = (Integer) value;
				}
			}
			this.hosts.put(name, new HostInfo(IP, port));
		}
	}

	public HostInfo getHostByName(String name) {
		return this.hosts.get(name);
	}
	
	public HashMap<String, HostInfo> getHosts(){
		return this.hosts;
	}
	
	@Override
	public String toString() {
		String toString = "{\n";
		for (Map.Entry<String, HostInfo> entry : this.hosts.entrySet()) {
			toString += entry.getKey() + "\n";
			toString += entry.getValue() + "\n";
		}
		toString += "}\n";
		return toString;
	}
}

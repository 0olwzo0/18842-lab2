package lab2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class MessagePasser {

	/**
	 * Config file name of this MessagePasser
	 */
	private String configFileName;

	/**
	 * Local name of this MessagePasser
	 */
	private String localName;

	/**
	 * Sequence number for a new message.
	 */
	private int nextSeqNum = 0;

	/**
	 * Store output sockets so that we can consider creating a new connection if
	 * config is changed, or reuse it if the config is the same.
	 */
	private HashMap<HostInfo, Socket> outputSockets = new HashMap<HostInfo, Socket>();

	/**
	 * Store output streams so that we can reuse it. This is mandatory since
	 * re-creating output stream every time we send a message will cause an
	 * error in the input stream of the other side.
	 */
	private HashMap<HostInfo, ObjectOutputStream> outputStreams = new HashMap<HostInfo, ObjectOutputStream>();

	/**
	 * Every node config read from config file.
	 */
	private Hosts hosts;

	/**
	 * SendRules
	 */
	private Rules sendRules;

	/**
	 * ReceiveRules
	 */
	private Rules receiveRules;

	/**
	 * Store messages to be sent to the socket
	 */
	private List<Message> sendBuffer = new LinkedList<Message>();

	/**
	 * Store all incoming messages to be checked with ReceiveRules. Each message
	 * would be delivered to "inputQueue", depended on its consequence action.
	 */
	private List<Message> receiveBuffer = Collections.synchronizedList(new LinkedList<Message>());

	/**
	 * Store messages delivered from "receiveBuffer" to be displayed by
	 * "MessagePasser.receive()"
	 */
	private List<Message> inputQueue = new LinkedList<Message>();

	/**
	 * Reference number for each ClientThread. For debugging purpose.
	 */
	private static int nextClientThreadID = 0;
	
	/**
	 * 
	 */
	private Clock hostTimeStamp = null;

	/**
   *
   */
	private ArrayList<ClientThread> connectionThreads = new ArrayList<ClientThread>();

	/**
	 * Receive 2 arguments for config filename and local name
	 * 
	 * @param argv
	 *            <config filename> <local name>
	 */
	public static void main(String[] argv) {
		if (argv.length < 2) {
			System.out.println("Incorrect parameters.\nUsage: java -jar lab0.jar <config_filename> <local_name>");
		} else {
		System.out.println("== MessagePasser [wkanchan + ytobioka] ==\n" + "Usage:\n"
				+ "s <dest> <kind>\tSend <dest> a message with a kind of <kind>\n"
				+ "r\t\tReceive a message from an input queue\n"
				+ "c\t\tShow local TimeStamp\n"
				+ "q\t\tQuit");
			MessagePasser mp = new MessagePasser(argv[0], argv[1]);
			mp.listen();
			mp.showCommandPrompt();
		}
	}

	/**
	 * MessagePasser constructor. Defined by the assignment.
	 * 
	 * @param configuration_filename
	 * @param local_name
	 */
	public MessagePasser(String configuration_filename, String local_name) {
		/*
		System.out.println("== MessagePasser [wkanchan + ytobioka] ==\n" + "Usage:\n"
				+ "s <dest> <kind>\tSend <dest> a message with a kind of <kind>\n"
				+ "r\t\tReceive a message from an input queue\n"
				+ "c\t\tShow local TimeStamp\n"
				+ "q\t\tQuit");
				*/
		this.localName = local_name;
		this.configFileName = configuration_filename;
		this.readConfig();
		/*
		listen();
		showCommandPrompt();
		*/
	}

	/**
	 * Read YAML configuration file. Synchronized from 2 threads reading config
	 * file at the same time
	 */
	synchronized private void readConfig() {
		InputStream input = null;
		Map<String, Object> config = null;

		// Load config YAML from file
		try {
			input = new FileInputStream(new File(this.configFileName));
			Yaml yaml = new Yaml();
			config = ((Map<String, Object>) yaml.load(input));
		} catch (Exception e) {
			System.err.println("Cannot open " + this.configFileName);
			System.err.println(e);
			System.exit(1);
		}

		// Parse each entry of config YAML and set MP's properties
		for (Map.Entry<String, Object> entry : config.entrySet()) {
			String key = entry.getKey();
			if ("configuration".equals(key)) {
				this.hosts = new Hosts(entry.getValue());
			} else if( this.hostTimeStamp == null && "clock".equals(key)){
				this.hostTimeStamp = Clock.makeClock((String)entry.getValue(),this.hosts,this.localName);
				if( this.hostTimeStamp == null){
					System.err.println("Input error: You need to configure clock type");
					System.exit(1);
				}
			}
			else if ("sendRules".equals(key)) {
				this.sendRules = new Rules(entry.getValue());
			} else if ("receiveRules".equals(key)) {
				this.receiveRules = new Rules(entry.getValue());
			}
		}
	}

	/**
	 * Create a thread to listen to incoming connections. Once a connection is
	 * accepted, create another dedicated thread to handle it.
	 */
	private void listen() {
		ListenThread thread = new ListenThread();
		thread.start();
	}

	/**
	 * Handle listening to incoming connections at a specific port.
	 * 
	 * @author kwittawat
	 * 
	 */
	private class ListenThread extends Thread {

		public void run() {
			HostInfo currentHostInfo = hosts.getHostByName(localName);
			ServerSocket welcomeSocket;
			try {
				welcomeSocket = new ServerSocket(currentHostInfo.getPort());
				while (true) {
					Socket connectionSocket = welcomeSocket.accept();
					ClientThread thread = new ClientThread(connectionSocket);
					connectionThreads.add(thread);
					thread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Handle a connection of each client after it is accepted at ListenThread.
	 * 
	 * @author kwittawat
	 * 
	 */
	private class ClientThread extends Thread {

		private Socket connectionSocket;
		private int connectionID;

		public ClientThread(Socket connectionSocket) {
			this.connectionSocket = connectionSocket;
			this.connectionID = nextClientThreadID++;
		}

		public void run() {
			System.out.println("\n<" + connectionID + "> Message sender connected!");
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(connectionSocket.getInputStream());
				// Wait for incoming messages forever (until something happens)
				while (true) {
					// Modifed Message to TimeStampledMessage
					TimeStampedMessage incomingMessage = (TimeStampedMessage) in.readObject();
					System.out.println(incomingMessage);
					if (incomingMessage == null) {
						break;
					}
					// Re-read configuration file to load Rules
					readConfig();
					// Check ReceiveRules for this message
					Rule.Action action = receiveRules.getMatchedAction(incomingMessage);
					System.out.println("\nReceived from " + incomingMessage.getSource() + "!");
					if (action == null) {
						// No rules applied. First, deliver this message.
						// Then, deliver delayed messages in receiveBuffer.
						System.out.println("Action: none");
						inputQueue.add(incomingMessage);
						inputQueue.addAll(receiveBuffer);
						receiveBuffer.clear();
					} else {
						// Execute an action
						System.out.println("Action: " + action);
						switch (action) {
						case delay:
							// Just put this message to receiveBuffer
							receiveBuffer.add(incomingMessage);
							break;
						case drop:
							// Do nothing
							break;
						case duplicate:
							// First, deliver both current message and its
							// duplicate.
							// Then, deliver all delayed messages in
							// receiveBuffer
							inputQueue.add(incomingMessage);
							inputQueue.add(new TimeStampedMessage(incomingMessage));
							inputQueue.addAll(receiveBuffer);
							receiveBuffer.clear();
							break;
						}
					}
					printBuffersStatus();
					System.out.print(localName + "# ");
				}
			} catch (IOException e) {
				// Possibly EOF from the sender. Just close the socket.
			} catch (ClassNotFoundException e) {
				// Internal error: Message deserialization failed
				e.printStackTrace();
			}
			try {
				System.out.println("test");
				connectionSocket.close();
				in.close();
			} catch (IOException e) {
			} finally {
				System.out.println("\n<" + connectionID + "> Disconnected by the message sender.");
				System.out.print(localName + "# ");
			}
		}

		public void closeSockets() {
			System.out.println("close socket");
			if (!connectionSocket.isClosed()) {
				try {
					connectionSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				super.finalize();
			} finally {
				destruction();
			}
		}

		private void destruction() {
			System.out.println("TestTesT");
			if (!connectionSocket.isClosed()) {
				try {
					connectionSocket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Show a command prompt to user.
	 */
	private void showCommandPrompt() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(localName + "# ");
		String inputCommand;
		try {
			while ((inputCommand = in.readLine()) != null) {
				String[] tokens = inputCommand.split(" ");
				if (tokens.length > 0) {
					if ("s".equals(tokens[0]) && tokens.length == 3) {
						// Send: Create a new message and send
						Message message = new Message(tokens[1], tokens[2], null);
						message.setSource(localName);
						message.setSeqNum(nextSeqNum++);
						// Increment Local TimeStamp
						this.hostTimeStamp.addClock();
						// Add TimeStamp to Message
						TimeStampedMessage t_message = new TimeStampedMessage(message,this.hostTimeStamp.deepCopy());
						send(t_message);
					} else if ("r".equals(tokens[0])) {
						// Receive
						TimeStampedMessage message;
						while ((message = (TimeStampedMessage)receive()) != null) {
							System.out.println(message);
							// Adjust the local timeStamp
							this.hostTimeStamp.adjustClock(message.getTimeStamp());
						}
					} else if ("q".equals(tokens[0])) {
						for (ClientThread thread : connectionThreads) {
							thread.closeSockets();
						}
						System.exit(0);
					} else if ("c".equals(tokens[0])){
						System.out.println("CurrentClock:"+this.hostTimeStamp);
						this.hostTimeStamp.addClock();
					} else {
						// Invalid command
						System.out.println("Couldn't recognize this command.");
					}
				}
				System.out.print(localName + "# ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message.
	 * 
	 * @param message
	 *            The message to send.
	 */
///	void send(Message message) {
	void send(TimeStampedMessage message) {
		// read configuration file to load Rules
		this.readConfig();
		// Check whether current connection is exists.
		HostInfo destHostInfo = this.hosts.getHostByName(message.getDest());
		if (destHostInfo == null) {
			System.out.println("Unknown destination name.");
			return;
		}
		Socket socket = outputSockets.get(destHostInfo);
		ObjectOutputStream out = outputStreams.get(message.getDest());
		if (socket != null && socket.isClosed()) {
			// If this socket is closed, remove this and its output stream.
			outputSockets.remove(destHostInfo);
			outputStreams.remove(destHostInfo);
			socket = null;
		}
		// If this outgoing socket is not exists or closed. Create it and its
		// output stream
		if (socket == null) {
			System.out.println("Connecting to " + message.getDest() + " at " + destHostInfo.getAddress() + ":"
					+ destHostInfo.getPort() + "...");
			try {
				socket = new Socket(destHostInfo.getAddress(), destHostInfo.getPort());
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Cannot connect to " + message.getDest() + ".");
				return;
			}
			System.out.println(this.hostTimeStamp+":Connected.");
			// Save this socket and its output stream for future use
			outputSockets.put(destHostInfo, socket);
			outputStreams.put(destHostInfo, out);
		}
		try {
			// Get output stream that we will write on
			out = outputStreams.get(destHostInfo);
			// Apply SendRules
			Rule.Action action = sendRules.getMatchedAction(message);
			if (action == null) {
				// No rules applied. Send this message first.
				// Then send all delayed messages in sendBuffer.
					
				out.reset();
				out.writeObject(message);
				System.out.println(message + " :: Sent");
				while (!sendBuffer.isEmpty()) {
					Message delayedMessage = sendBuffer.remove(0);
					out.writeObject(delayedMessage);
					System.out.println(delayedMessage + " :: Sent (after delayed)");
				}
			} else {
				// Execute an action
				switch (action) {
				case delay:
					// Just add to sendBuffer
					sendBuffer.add(message);
					System.out.println(message + " :: Delayed");
					break;
				case drop:
					// Do nothing
					System.out.println(message + " :: Dropped");
					break;
				case duplicate:
					// First, send this message and its duplicate.
					// Then, send delayed messages in sendBuffer
					out.writeObject(message);
					System.out.println(message + " :: Sent");
					TimeStampedMessage duplicatedMessage = new TimeStampedMessage(message);
					out.writeObject(duplicatedMessage);
					System.out.println(duplicatedMessage + " :: Sent");
					while (!sendBuffer.isEmpty()) {
						Message delayedMessage = sendBuffer.remove(0);
						out.writeObject(delayedMessage);
						System.out.println(delayedMessage + " :: Sent (after delayed)");
					}
					break;
				}
			}
			printBuffersStatus();
		} catch (SocketException e) {
			System.out.println("Error: The destination has closed the connection. Re-sending..");
			try {
				socket.close();
				out.close();
			} catch (IOException e2) {
			} finally {
				outputSockets.remove(destHostInfo);
				outputStreams.remove(destHostInfo);
			}
			send(message); // * Re-send only if SocketException: Broker Pipe

		} catch (IOException e) {
			System.out.println("Connection error. Please try again.");
			try {
				socket.close();
				out.close();
			} catch (IOException e2) {
			} finally {
				outputSockets.remove(destHostInfo);
				outputStreams.remove(destHostInfo);
			}
		}
	}

	/**
	 * Retrieve a message from the front of the input queue and display it.
	 * Modified from Message to TimeStampedMessage
	 * 
	 * @return
	 */
	Message receive() {
		try {
			return inputQueue.remove(0);
		} catch (Exception e) {
			return null;
		}
	}

	private void printBuffersStatus() {
		System.out.println("{sendBuffer:" + sendBuffer.size() + " receiveBuffer:" + receiveBuffer.size()
				+ " inputQueue:" + inputQueue.size() + "}");
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
		} 
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Hosts getHosts(){
		return this.hosts;
	}
}

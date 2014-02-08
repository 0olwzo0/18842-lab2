package lab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.io.FileWriter;
import java.io.FileReader;

public class Logger {
	private MessagePasser logMP;
	private String localName;
	private String confName;
	private List<TimeStampedMessage> logs = Collections
			.synchronizedList(new LinkedList<TimeStampedMessage>());
	private ArrayList<LogClientThread> connectionThreads = new ArrayList<LogClientThread>();
	private ArrayList<TimeStampedMessage> sortedLogs = new ArrayList<TimeStampedMessage>();
	private String logFile;
	private FileWriter fr;

	/**
	 * Reference number for each ClientThread. For debugging purpose.
	 */
	private static int nextClientThreadID = 0;

	public static void main(String[] argv) {
		if (argv.length < 2) {
			System.out
					.println("Incorrect parameters.\nUsage: java -cp bin:libs/snakeyaml-1.13.jar lab0.Logger <config_filename> <local_name>");
		} else {
			Logger logger = new Logger(argv[0], argv[1]);
			logger.listen();
			logger.showCommandPrompt();
		}
	}

	/**
	 * display logs
	 */
	public void displayLogs(ArrayList<TimeStampedMessage> sortedLogs) {
		System.out.println("LOG FILE WITH ORDER AND CONCURRENT FLAG");
		if (sortedLogs.size() == 0) {
			System.out.println("LOGFILE IS EMPTY");
			return;
		}

		System.out.println(sortedLogs.get(0).toString() + " ConCurrent:"
				+ sortedLogs.get(0).getConcurrent());
		for (int i = 1; i < sortedLogs.size(); i++) {
			if ((sortedLogs.get(i).timeStamp).compareClock(sortedLogs
					.get(i - 1).timeStamp) != 0) {
				System.out.println("");
			}
			System.out.println(sortedLogs.get(i).toString() + " ConCurrent:"
					+ sortedLogs.get(i).getConcurrent());
		}
		System.out.println("LOG FILE END");
	}

	public Logger(String confName, String localName) {
		this.localName = localName;
		this.confName = confName;
		this.logMP = new MessagePasser(confName, localName);
		this.logFile = "log/log.txt";
		try{
			this.fr = new FileWriter(this.logFile,true);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
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
			HostInfo currentHostInfo = logMP.getHosts()
					.getHostByName(localName);
			ServerSocket welcomeSocket;
			try {
				welcomeSocket = new ServerSocket(currentHostInfo.getPort());
				while (true) {
					Socket connectionSocket = welcomeSocket.accept();
					LogClientThread thread = new LogClientThread(
							connectionSocket);
					thread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/*
	 * Handle each log connectino after eatablishing connection
	 */

	private class LogClientThread extends Thread {

		private Socket connectionSocket;
		private int connectionID;

		public LogClientThread(Socket connectionSocket) {
			this.connectionSocket = connectionSocket;
			this.connectionID = nextClientThreadID++;
		}

		public void run() {
			System.out.println("\n<" + connectionID
					+ "> Message sender connected!");
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(connectionSocket.getInputStream());
				// Wait for incoming messages forever (until something happens)
				while (true) {
					// Modifed Message to TimeStampledMessage
					TimeStampedMessage incomingMessage = (TimeStampedMessage) in
							.readObject();
					// System.out.println(incomingMessage);
					logs.add(incomingMessage);
					if (incomingMessage == null) {
						break;
					}
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
				System.out.println("\n<" + connectionID
						+ "> Disconnected by the message sender.");
				System.out.print(localName + "# ");
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
					if ("show".equals(tokens[0])) {
							ArrayList<TimeStampedMessage> tmpLogs= new ArrayList<TimeStampedMessage>();
							TimeStampedMessage message;
							while ((message = (TimeStampedMessage) receive()) != null) {
								tmpLogs.add(message);
								// System.out.println(message);
							}
							Collections.sort(tmpLogs,
									new TimeStampedMessageComparator());
							this.sortedLogs.addAll(tmpLogs);
							if(tokens.length == 1){
								this.displayLogs(this.sortedLogs);
							}
							else if("new".equals(tokens[1])){
								this.displayLogs(tmpLogs);
							}
					} else if ("q".equals(tokens[0])) {
						System.exit(0);
					} else {
						// Invalid command
						System.out.println("Couldn't recognize this command.");
					}
				}
				System.out.print(localName + "# ");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	Message receive() {
		try {
			return this.logs.remove(0);
		} catch (Exception e) {
			return null;
		}
	}

}

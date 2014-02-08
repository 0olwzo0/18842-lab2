package lab2;

import java.io.*;
import java.net.*;

public class TestClient
{
	public static final int PORT = 12344;
	public static void main(String argv[]) throws Exception
	{
		String sentence;
		
		System.out.print("Connecting to port "+PORT+"... ");
		Socket clientSocket = new Socket("localhost", PORT);
		System.out.println("Connected");		
		ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input (Ctrl-D to exit): ");
		int seqNum = 0;
		while ((sentence = inFromUser.readLine()) != null) {
			Message message = new Message("dest", "kind", sentence);
			message.setSource("alice");
			message.setSeqNum(seqNum++);
			out.writeObject(message);
			System.out.println("Message sent.");
			System.out.print("Input (Ctrl-D to exit): ");
		}
		
		System.out.println("<Ctrl-D>");
		clientSocket.close();
		System.out.println("Disconnected");
	}
	

}
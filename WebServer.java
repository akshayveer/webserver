import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.Client;

//import server.HttpMessage;

public class WebServer {
	static ServerSocket listenSocket;
	static Client client;

	public WebServer(int p) {
		try {
			listenSocket = new ServerSocket(p);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String argv[]) {
		int port = 8080;
		if (argv.length > 0) {
			port = Integer.parseInt(argv[0]);
		}
		new WebServer(port);
		Socket clientConnectionSocket = null;

		try {
			while (true) {
				
					clientConnectionSocket = listenSocket.accept();
					client = new Client(clientConnectionSocket);
					client.start();
				
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

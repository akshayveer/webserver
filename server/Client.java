package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client extends Thread {
	Socket clientSocket;
	private Thread thread;

	public Client(Socket s) {
		clientSocket = s;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		String[] paths = getRequestedFile(clientSocket);
		if(paths[1]==null) return;
		sendMessage(clientSocket, paths[0], paths[1]);
		/*try {
			clientSocket.close();
			this.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		this.stop();
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public String[] getRequestedFile(Socket socket) {
		String filePath = null;
		String origFile = null;
		try {
			BufferedReader clientRequest = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String clientRequestLine = clientRequest.readLine();
			System.out.println(clientRequestLine);
			if (clientRequestLine == null) {
				return new String[] { filePath, origFile };
			}
			
			StringTokenizer tokenizedLine = new StringTokenizer(
					clientRequestLine);
			if (tokenizedLine.nextToken().equals("GET")) {
				filePath = tokenizedLine.nextToken();
				origFile = filePath;
				if (filePath.length() <= 2 ) {
					return new String[] { " ", origFile };
				}
				if(filePath.charAt(1)!='~'){
					return new String[] { " ", origFile };
				}
				filePath = filePath.substring(2);
				String[] s = filePath.split("/");
				filePath = s[0] + "/public_html";
				for (int i = 1; i < s.length; i++) {
					filePath = filePath + "/" + s[i];
				}
				if (s.length == 1) {
					if(origFile.charAt(origFile.length()-1)=='/'){
						filePath = filePath + "/" + "index.html";
					}
					else{
						filePath="@moved";
					}
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new String[] { filePath, origFile };
	}

	public void sendMessage(Socket socket, String filePath, String origfile) {

		try {
			DataOutputStream serverReply = new DataOutputStream(
					socket.getOutputStream());
			String httpStartLine = "HTTP/1.0 200 Document Follows\r\n";
			File file = new File(filePath);
			if (!file.exists()) {

				
				httpStartLine = "HTTP/1.1 404 Not Found";
				filePath = "fileNotFound.html";

				PrintWriter writer = new PrintWriter(filePath, "UTF-8");
				writer.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
				writer.println("<html><head>");
				writer.println("<title>404 Not Found</title>");
				writer.println("</head><body>");
				writer.println("<h1>Not Found</h1>");
				writer.println("<p>The requested URL " + origfile
						+ " was not found on this server.</p>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();

				file = new File(filePath);
			}
			int fileByteCount = (int) file.length();
			byte[] bytesInFile = new byte[fileByteCount];
			FileInputStream requestFileStream = new FileInputStream(filePath);
			requestFileStream.read(bytesInFile);

			requestFileStream.close();
			serverReply.writeBytes(httpStartLine);
			serverReply.writeBytes("Content-Length: " + fileByteCount + "\r\n");
			serverReply.writeBytes("\r\n");
			serverReply.write(bytesInFile, 0, fileByteCount);
			serverReply.flush();
			serverReply.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

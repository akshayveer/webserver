package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class Client extends Thread {
	Socket clientSocket;
	private Thread thread;
	private BufferedReader clientRequest;
	private DataOutputStream serverReply;
	boolean stop;
	int threadNum;
	public Client(Socket s,int i) {
		clientSocket = s;
		threadNum=i;
		stop=false;
		try {
			serverReply = new DataOutputStream(
					clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	void closeConnection(){
		
		try {
			clientRequest.close();
		} catch (IOException e) {
			
		}
		try {
			serverReply.close();
		} catch (IOException e) {
			
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			
		}
	}
	public void run() {
		try {
			clientSocket.setSoTimeout(5000);
			long start = System.currentTimeMillis();
			long end = start + 5*1000; // 60 seconds * 1000 ms/sec
			while(System.currentTimeMillis() < end){
				System.out.print(" tcp connectoin listening ");
				System.out.println(threadNum);
				String[] paths = getRequestedFile();
				if(paths[1]==null) continue;
				sendMessage(paths[0], paths[1]);
			}
			closeConnection();
		} catch (SocketException e1) {
			closeConnection();
		}
		System.out.print("tcp connection closed ");
		System.out.println(threadNum);
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public String[] getRequestedFile() {
		String filePath = null;
		String origFile = null;
		try {
			try {
				clientRequest = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String clientRequestLine = clientRequest.readLine();
			System.out.print(clientRequestLine);
			System.out.println(threadNum);
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
			closeConnection();
		}
		
		return new String[] { filePath, origFile };
	}

	public void sendMessage(String filePath, String origfile) {

		try {
			 
			String httpStartLine = "HTTP/1.1 200 Document Follows\r\n";
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

		} catch (IOException e) {
			closeConnection();
		}
	}
}

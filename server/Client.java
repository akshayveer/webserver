package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Client extends Thread {
	Socket clientSocket;
	private Thread thread;
	private BufferedReader clientRequest;
	private DataOutputStream serverReply;
	boolean stop;
	int threadNum;
	String filePath;
	String origFile;
	String httpStartLine;
	String host;
	boolean headRequest;
	public Client(Socket s, int i) {
		clientSocket = s;
		threadNum = i;
		stop = false;
		filePath = null;
		origFile = null;
		httpStartLine = null;
		host=null;
		try {
			serverReply = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void closeConnection() {

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
			long end = start + 5 * 1000; // 60 seconds * 1000 ms/sec
			while (System.currentTimeMillis() < end & !stop) {
				System.out.print(" tcp connectoin listening ");
				System.out.println(threadNum);
				getRequestedFile();
				if (origFile == null)
					continue;
				sendMessage();
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

	public void getRequestedFile() {

		try {
			try {
				clientRequest = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String clientRequestLine = clientRequest.readLine();
			System.out.print(clientRequestLine);
			System.out.print(" ");
			System.out.println(threadNum);
			if (clientRequestLine == null) {
				return;
			}
			host= (clientRequest.readLine()).split(" ")[1];
			StringTokenizer tokenizedLine = new StringTokenizer(
					clientRequestLine);
			String firstLine=tokenizedLine.nextToken();
			if (firstLine.equals("GET") || firstLine.equals("HEAD")) {
				if (firstLine.equals("HEAD")) headRequest=true;
				filePath = tokenizedLine.nextToken();
				origFile = filePath;
				if(!filePath.endsWith("/") && !filePath.contains(".")){
					filePath = origFile + "/";
					origFile = " ";
					return;
				}
				if (filePath.length() <= 2) {
					filePath = " ";
					return;
				}
				if (filePath.charAt(1) != '~') {
					filePath = " ";
					return;
				}
				filePath = filePath.substring(2);
				String[] s = filePath.split("/");
				filePath = s[0] + "/public_html";
				for (int i = 1; i < s.length; i++) {
					filePath = filePath + "/" + s[i];
				}
				if (s.length == 1) {
					if (origFile.charAt(origFile.length() - 1) == '/') {
						filePath = filePath + "/" + "index.html";
					} else {
						filePath = origFile + "/";
						origFile = " ";
					}
				}

			}
			

		} catch (IOException e) {
			closeConnection();
		}

		return;
	}
	
	String getContentType(){
		if(filePath.endsWith("jpg")) return "Content-Type: image/jpeg\r\n";
		if(filePath.endsWith("png")) return "Content-Type: image/png\r\n";
		return "text/html; charset=iso-8859-1\r\n";
		
	}

	void httpMessage(String title, String body) {

		try {
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");
			writer.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
			writer.println("<html><head>");
			writer.println("<title>" + title + "</title>");
			writer.println("</head><body>");
			writer.println("<h1>Not Found</h1>");
			writer.println("<p>" + body + "</p>");
			writer.println("</body></html>");
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage() {

		try {
			String temp=null;
			File file;
			if(origFile.equals(" ")){
				temp=filePath;
				filePath="movedPermanently.html";
				httpStartLine="HTTP/1.1 301 Moved Permanently\r\n";
				httpMessage("301 Moved Permanently","The document has moved href=http://"+host+temp+"here.");
			}
			else{
				file = new File(filePath);
				if(file.exists()){
					if(file.isFile()){
						httpStartLine = "HTTP/1.1 200 OK\r\n";
					}
					if(file.isDirectory()){
						temp=filePath+"/"+"index.html";
						file=new File(temp);
						if(file.exists()){
							filePath=temp;
							httpStartLine = "HTTP/1.1 200 OK\r\n";
						}
						if(!file.exists()){
							stop=true;
							httpStartLine = "HTTP/1.1 404 Not Found\r\n";
							filePath = "fileNotFound.html";
							httpMessage("404 Not Found","The requested URL " + origFile + " was not found on this server.");
						}
					}
				}
				else {
					stop=true;
					httpStartLine = "HTTP/1.1 404 Not Found\r\n";
					filePath = "fileNotFound.html";
					httpMessage("404 Not Found","The requested URL " + origFile + " was not found on this server.");
				}
			}
			file = new File(filePath);
			int fileByteCount = (int) file.length();
			byte[] bytesInFile = new byte[fileByteCount];
			FileInputStream requestFileStream = new FileInputStream(filePath);
			requestFileStream.read(bytesInFile);
			requestFileStream.close();
			
			Date d1 = new Date();
			SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
			String formattedDate = df.format(d1);
						
			serverReply.writeBytes(httpStartLine);
			serverReply.writeBytes("Date: "+formattedDate+"\r\n");
			serverReply.writeBytes("Server: Ak-server/1.0\r\n");
			serverReply.writeBytes("Last-Modified:"+df.format(file.lastModified())+"\r\n");
			serverReply.writeBytes("Content-Length: " + fileByteCount + "\r\n");
			if(origFile.equals(" ")){
				System.out.println("Location: http://"+host+temp);
				serverReply.writeBytes("Location: http://"+host+temp);
				stop=true;
			}
			/*if(stop){
				serverReply.writeBytes("Connection: close\r\n");
			}*/
//			serverReply.writeBytes("Content-Type: "+getContentType());
			serverReply.writeBytes("\r\n");
			if(!headRequest) serverReply.write(bytesInFile, 0, fileByteCount);
			serverReply.flush();

		} catch (IOException e) {
			closeConnection();
		}
	}
}

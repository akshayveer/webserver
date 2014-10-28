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
import java.util.Date;
import java.text.SimpleDateFormat;
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
	boolean error;
	public Client(Socket s,int i) {
		clientSocket = s;
		threadNum=i;
		stop=false;
		filePath=null;
		origFile = null;
		httpStartLine=null;
		error=false;
		host=null;
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
			Date d1 = new Date();
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/YYYY HH:mm a");
			String formattedDate = df.format(d1);
			System.out.println(formattedDate);
			clientSocket.setSoTimeout(5000);
			long start = System.currentTimeMillis();
			long end = start + 5*1000; // 60 seconds * 1000 ms/sec
			while(System.currentTimeMillis() < end){
				System.out.print(" tcp connectoin listening ");
				System.out.println(threadNum);
				getRequestedFile();
				if(origFile==null) continue;
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
				return ;
			}
//			host= (clientRequest.readLine()).split(" ")[1];
			
			StringTokenizer tokenizedLine = new StringTokenizer(
					clientRequestLine);
			if (tokenizedLine.nextToken().equals("GET")) {
				filePath = tokenizedLine.nextToken();
				origFile = filePath;
				if (filePath.length() <= 2 ){
					filePath=" ";
					return;
				}
				if(filePath.charAt(1)!='~'){
					filePath=" ";
					return;
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
						filePath=origFile+"/";
						origFile=" ";
					}		
				}
			}
			
		} catch (IOException e) {
			closeConnection();
		}	
	}
	

	void httpMessage(String title,String body){

		try {
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");
			writer.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
			writer.println("<html><head>");
			writer.println("<title>"+title+"</title>");
			writer.println("</head><body>");
			writer.println("<h1>Not Found</h1>");
			writer.println("<p>"+body+"</p>");
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
			File file;
			String temp = null;
			/*if(origFile.equals(" ")){
				temp=filePath;
				filePath="movedPermanently.html";
				httpStartLine="HTTP/1.1 301 Moved Permanently";
				httpMessage("301 Moved Permanently","The document has moved href=http://"+host+temp+"here.");
				error=true;
			}
			
			else{
				file = new File(filePath);
				if (file.exists()) {
					httpStartLine = "HTTP/1.1 200 OK";
				}
				else{
					filePath="fileNotFound.html";
					httpStartLine = "HTTP/1.1 404 Not Found";
					httpMessage("404 Not Found","The requested URL " + origFile + " was not found on this server.");
					error=true;
				}
			}*/
			httpStartLine = "HTTP/1.1 200 OK";
			file = new File(filePath);
			
			int fileByteCount = (int) file.length();
			byte[] bytesInFile = new byte[fileByteCount];
			FileInputStream requestFileStream = new FileInputStream(filePath);
			requestFileStream.read(bytesInFile);

			requestFileStream.close();

			Date d1 = new Date();
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/YYYY HH:mm a");
			String formattedDate = df.format(d1);
			serverReply.writeBytes(httpStartLine);
			serverReply.writeBytes("Content-Length: " + fileByteCount + "\r\n");
			if(origFile.equals(" ")) serverReply.writeBytes("Location: http://"+host+temp);
			serverReply.writeBytes("\r\n");
			serverReply.write(bytesInFile, 0, fileByteCount);
			serverReply.flush();
			
		} catch (IOException e) {
			closeConnection();
		}
	}
}

package server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class HttpMessage {
	public void sendMessage(Socket socket,String filePath,String origfile){
		
		try {
			DataOutputStream serverReply = new DataOutputStream(
					socket.getOutputStream());
			String httpStartLine="HTTP/1.0 200 Document Follows\r\n";
			File file = new File(filePath);
			if(!file.exists()){
				
				System.out.println(filePath);
				httpStartLine="HTTP/1.1 404 Not Found";
				filePath="fileNotFound.html";
				
				PrintWriter writer = new PrintWriter(filePath, "UTF-8");
				writer.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
				writer.println("<html><head>");
				writer.println("<title>404 Not Found</title>");
				writer.println("</head><body>");
				writer.println("<h1>Not Found</h1>");
				writer.println("<p>The requested URL "+origfile+ " was not found on this server.</p>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
				
				
				file = new File(filePath);
			}
				int fileByteCount = (int) file.length();
				byte[] bytesInFile = new byte[fileByteCount];
				FileInputStream requestFileStream = new FileInputStream(
						filePath);
				requestFileStream.read(bytesInFile);
				
				requestFileStream.close();
				serverReply.writeBytes(httpStartLine);
				serverReply.writeBytes("Content-Length: " + fileByteCount
						+ "\r\n");
				serverReply.writeBytes("\r\n");
				serverReply.write(bytesInFile, 0, fileByteCount);
				serverReply.flush();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

//import server.HttpMessage;

public class WebServer {
	static ServerSocket listenSocket;

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
		BufferedReader clientRequest;
		DataOutputStream serverReply;
		try {
			while (true) {
				clientConnectionSocket = listenSocket.accept();
				clientRequest = new BufferedReader(new InputStreamReader(
						clientConnectionSocket.getInputStream()));
				serverReply = new DataOutputStream(
						clientConnectionSocket.getOutputStream());
				String clientRequestLine = clientRequest.readLine();
				String filePath;
				StringTokenizer tokenizedLine = new StringTokenizer(
						clientRequestLine);
				if (tokenizedLine.nextToken().equals("GET")) {
					filePath = tokenizedLine.nextToken();
					if(filePath.equals("/favicon.ico")) continue;
					filePath = filePath.substring(2);
					String[] s = filePath.split("/");
					filePath = s[0] + "/public_html";
					for (int i = 1; i < s.length; i++) {
						filePath = filePath + "/" + s[i];
					}
					if (s.length == 1) {
						filePath = filePath + "/" + "index.html";
					}

					File file = new File(filePath);
					if(!file.exists()){
						
					}
					int fileByteCount = (int) file.length();
					byte[] bytesInFile = new byte[fileByteCount];
					FileInputStream requestFileStream = new FileInputStream(
							filePath);
					requestFileStream.read(bytesInFile);
					requestFileStream.close();
					serverReply.writeBytes("HTTP/1.0 200 Document Follows\r\n");
					serverReply.writeBytes("Content-Length: " + fileByteCount
							+ "\r\n");
					serverReply.writeBytes("\r\n");
					serverReply.write(bytesInFile, 0, fileByteCount);
					clientConnectionSocket.close();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

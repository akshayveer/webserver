package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

public class ClientRequestParser {
	public String[] getRequestedFile(Socket socket){
		String filePath = null;
		String origFile = null;
		try {
			BufferedReader clientRequest = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String clientRequestLine = clientRequest.readLine();
			System.out.println(clientRequestLine);
			if(clientRequestLine==null){
				 return new String[]{filePath,origFile};
			}
			
			StringTokenizer tokenizedLine = new StringTokenizer(
					clientRequestLine);
			if (tokenizedLine.nextToken().equals("GET")) {
				filePath = tokenizedLine.nextToken();
				origFile=filePath;
				if(filePath.length()<=2){
					 return new String[]{" ",origFile};
				}
				filePath = filePath.substring(2);
				String[] s = filePath.split("/");
				filePath = s[0] + "/public_html";
				for (int i = 1; i < s.length; i++) {
					filePath = filePath + "/" + s[i];
				}
				if (s.length == 1) {
					filePath = filePath + "/" + "index.html";
				}
			
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String[]{filePath,origFile};
	}
}

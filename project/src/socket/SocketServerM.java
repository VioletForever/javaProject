package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServerM {

	public static void main(String[] args) throws IOException {
		int port = 12000;
		int clientNo = 1;
		ServerSocket serverSocket = new ServerSocket(port);
		try {
			while(true) {
				Socket socket = serverSocket.accept();
				SingleServer singleServer = new SingleServer(socket,clientNo);
				Thread thread = new Thread(singleServer);
				thread.start();
				clientNo++;
			}
		}finally {
			serverSocket.close();
		}

	}

}

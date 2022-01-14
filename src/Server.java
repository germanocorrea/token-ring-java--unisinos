import java.io.*;
import java.net.*;
import java.util.Objects;

class Server {

    public ServerSocket receptionSocket;
    public Socket socket;
    public BufferedReader fromClient;
    public DataOutputStream toClient;

    Server(Integer port) throws IOException {
        this.receptionSocket = new ServerSocket(port);
        this.socket = receptionSocket.accept();
        this.fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.toClient = new DataOutputStream(socket.getOutputStream());
    }

    protected void finalize() throws IOException {
        receptionSocket.close();
        socket.close();
    }

    public String waitForToken() throws Exception {
        String token = null;
        while (token == null) {
            Thread.sleep(50);
            token = fromClient.readLine();
        }
        toClient.writeBytes("Token ACK");
        toClient.flush();
        return token;
    }

}
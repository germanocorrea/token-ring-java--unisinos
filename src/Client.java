import java.io.*;
import java.net.*;
import java.util.Objects;

class Client {

    public Socket socket;
    public BufferedReader fromServer;
    public DataOutputStream toServer;

    Client(Integer port) throws IOException {
        this.socket = new Socket("127.0.0.1", port);
        this.fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.toServer = new DataOutputStream(socket.getOutputStream());
    }

    protected void finalize() throws IOException {
        socket.close();
    }

    public String sendToken(String token) throws Exception {
        toServer.writeBytes(token);
        toServer.flush();
        String response = null;
        while (response == null) {
            Thread.sleep(50);
            response = fromServer.readLine();
        }
        return response;
    }

}
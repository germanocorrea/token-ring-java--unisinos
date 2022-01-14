import java.io.*;
import java.util.Base64;
import java.util.Objects;

public class Node {

    Integer id;
    Integer nextNodeId;

    Server server;
    Client client;

    Boolean waitingForAck = false;
    Boolean firstRun = false;
    Integer initPort = 8990;
    Token firstRunToken = new Token();

    Node(Integer id, Integer nextNodeId) {
        this.id = id;
        this.nextNodeId = nextNodeId;
    }

    public void finalize() {
        this.log("thread finalizada");
    }

    public void setFirstRun() {
        this.firstRun = true;
    }

    public void setSockets() throws IOException {
        this.createClient();
        this.server = new Server(this.initPort + this.id);
        while (this.client == null) {
            this.createClient();
        }

        this.log("servidor e cliente iniciados");
    }

    public void createClient() throws IOException {
        try {
            this.client = new Client(this.initPort + this.nextNodeId);
        } catch (IOException e) {}
    }

    public void process() throws Exception {
        Token token;
        if (!this.firstRun) {
            this.log("aguardando token...");
            token = this.deserializeToken(server.waitForToken());
            this.log("token " + token.toString() + " recebido");
        } else {
            this.firstRun = false;
            token = this.firstRunToken;
        }

        String serializedToken = this.processToken(token);
        this.log("enviando token para Node " + this.nextNodeId + "");
        String response = client.sendToken(serializedToken);
        this.log("response: " + response);
        this.postProcessToken(token);

        this.process();
    }

    private String processToken(Token token) throws Exception {
        if (Objects.equals(token.currentSignal, Token.EMPTY_SIGNAL)) {
            token = this.processEmptySignalToken(token);
        } else if (Objects.equals(token.currentSignal, Token.ACK_SIGNAL)) {
            token = this.processAckSignalToken(token);
        } else if (Objects.equals(token.currentSignal, Token.SEND_SIGNAL)) {
            token = this.processSendSignalToken(token);
        }

        return this.serializeToken(token);
    }

    private Token processEmptySignalToken(Token token) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        this.log("deseja enviar alguma mensagem? [S/N]");
        if (!Objects.equals(input.readLine(), "S")) {
            return token;
        }

        this.waitingForAck = true;
        Token sendToken = new Token();
        sendToken.currentSignal = Token.SEND_SIGNAL;
        sendToken.senderNodeId = this.id;

        this.log("informe o ID do nó");
        sendToken.destinationNodeId = Integer.parseInt(input.readLine());

        this.log("informe a mensagem a ser enviada");
        sendToken.data = input.readLine();

        return sendToken;
    }

    private Token processAckSignalToken(Token token) throws Exception {
        if (!Objects.equals(token.destinationNodeId, this.id)) {
            return token;
        }

        if (!this.waitingForAck) {
            throw new Exception("Sinal ACK para nó que não esperava por ACK");
        }

        return new Token();
    }

    private Token processSendSignalToken(Token token) {
        if (!Objects.equals(token.destinationNodeId, this.id)) {
            return token;
        }

        this.waitingForAck = true;

        Token ackToken = new Token();
        ackToken.currentSignal = Token.ACK_SIGNAL;
        ackToken.destinationNodeId = token.senderNodeId;
        ackToken.senderNodeId = this.id;

        return ackToken;
    }

    private void postProcessToken(Token token) throws Exception {
        if (Objects.equals(token.currentSignal, Token.TERMINATE_SIGNAL)) {
            this.log("sinal para interromper execução recebido");
            throw new Exception("Interrompendo Nó");
        }
    }

    private void log(String log) {
        System.out.println("[Node " + this.id + "] " + log);
    }

    private String serializeToken(Token token) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(token);
        outputStream.close();
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    private Token deserializeToken(String serializedToken) throws Exception {
        byte [] data = Base64.getDecoder().decode(serializedToken);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        Token object  = (Token) objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

}

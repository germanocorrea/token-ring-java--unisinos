import java.io.Serializable;

public class Token implements Serializable {

    public static final Integer TERMINATE_SIGNAL = -1;
    public static final Integer EMPTY_SIGNAL = 0;
    public static final Integer SEND_SIGNAL = 1;
    public static final Integer ACK_SIGNAL = 2;

    public String data;
    public Integer destinationNodeId;
    public Integer senderNodeId;
    public Integer currentSignal = Token.EMPTY_SIGNAL;

    public String toString() {
        return this.data;
    }

}

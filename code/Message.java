package code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class Message implements Serializable {
    private Integer sender;
    private MessageType msgType;
    // private Integer data;
    private Long clock;

    public Message(Integer senderId, MessageType msgType, Long clock) {
        this.sender = senderId;
        this.msgType = msgType;
        this.clock = clock;
    }

    public enum MessageType {
        REQUEST,
        REPLY,
        FINISH,
        ;

    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType mType) {
        this.msgType = mType;
    }

    // public Integer getData() {
    //     return data;
    // }

    // public void setData(Integer data) {
    //     this.data = data;
    // }

    public Long getClock() {
        return clock;
    }

    // Convert current instance of Message to ByteBuffer
    // in order to send message over SCTP
    public ByteBuffer toByteBuffer() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();

        ByteBuffer buf = ByteBuffer.allocateDirect(bos.size());
        buf.put(bos.toByteArray());

        oos.close();
        bos.close();

        // Buffer needs to be flipped after writing
        // Buffer flip should happen only once
        buf.flip();
        return buf;
    }

    // Retrieve Message from ByteBuffer received from SCTP
    public static Message fromByteBuffer(ByteBuffer buf) throws IOException, ClassNotFoundException {
        // Buffer needs to be flipped before reading
        // Buffer flip should happen only once
        buf.flip();
        byte[] data = new byte[buf.limit()];
        buf.get(data);
        buf.clear();

        if (data.length == 0) {
            return null;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Message msg = (Message) ois.readObject();

        bis.close();
        ois.close();

        return msg;
    }

    public void print() {
        switch (msgType) {
            case REQUEST:
            case REPLY:
                System.out.println("Sender: " + sender + " MsgType: " + msgType + " Clock: " + clock);
                break;
            case FINISH:
                System.out.println("Sender: " + sender + " MsgType: " + msgType);
                break;
            // case CC:
            //     System.out.println("Sender: " + sender + " MsgType: " + mType + " StateRecords: "
            //             + stateRecords.stream().map(StateRecord::toString).collect(Collectors.joining()));
            //     break;
        }
    }

    public void print(String postfix) {
        switch (msgType) {
            case REQUEST:
            case REPLY:
                System.out.println("Sender: " + sender + " MsgType: " + msgType + " Clock: " + clock + postfix);
                break;
            case FINISH:
                System.out.println("Sender: " + sender + " MsgType: " + msgType + postfix);
                break;
            // case CC:
            //     System.out.println("Sender: " + sender + " MsgType: " + mType
            //             + " StateRecords: "
            //             + stateRecords.stream().map(StateRecord::toString).collect(Collectors.joining()) + postfix);
            //     break;
        }
    }

}
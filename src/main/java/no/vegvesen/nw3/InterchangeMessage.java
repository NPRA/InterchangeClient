package no.vegvesen.nw3;

import java.time.Instant;
import java.time.ZoneId;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

public class InterchangeMessage implements MessageInterface {
    String publisherId;// o
    String publisherName;// m
    String originatingCountry;// m
    String protocolVersion;// m
    MessageType messageType;// m
    String contentType;// o
    double latitude;// m
    double longitude;// m
    String quadTree = "";// m
    long timestamp = -1;// o
    String relation;// o
    Object body;

    MessageInterface messageTypeImpl;

    InterchangeMessage(String publisherName, String publisherId, String originatingCountry, String protocolVersion, MessageType messageType,
            double latitude, double longitude, String[] qTree) {
        this.publisherName = publisherName;
        this.publisherId = publisherId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
        this.messageType = messageType;
        this.latitude = latitude;
        this.longitude = longitude;
        for (String qt : qTree) {
            this.quadTree = this.quadTree + "," + qt;
        }
        this.quadTree = this.quadTree + ",";
        this.timestamp = Instant.now().toEpochMilli();
    }

    InterchangeMessage(String publisherName, String publisherId, String originatingCountry, String protocolVersion, MessageType messageType,
            double latitude, double longitude) {
        this.publisherName = publisherName;
        this.publisherId = publisherId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
        this.messageType = messageType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.quadTree = "," + Quadtree.latLonToQuadtree(latitude, longitude, 18) + ",";
        this.timestamp = Instant.now().toEpochMilli();
    }


    public InterchangeMessage(JmsMessage message) throws JMSException {

        messageType = getMessageType(message);
        // TODO: permitt messages without mandatory fields?
        if (message.propertyExists("publisherName"))
            publisherName = message.getStringProperty("publisherName");
        if (message.propertyExists("originatingCountry"))
            originatingCountry = message.getStringProperty("originatingCountry");
        if (message.propertyExists("protocolVersion"))
            protocolVersion = message.getStringProperty("protocolVersion");
        if (message.propertyExists("quadTree"))
            quadTree = message.getStringProperty("quadTree");
        // TODO: handle both double and float:
        if (message.propertyExists("latitude"))
            latitude = message.getDoubleProperty("latitude");
        if (message.propertyExists("longitude"))
            longitude = message.getDoubleProperty("longitude");
        if (message.propertyExists("publisherId")) 
            publisherId = message.getStringProperty("publisherId");
        if (message.propertyExists("relation"))
            relation = message.getStringProperty("relation");
        if (message.propertyExists("timestamp"))
            timestamp = message.getLongProperty("timestamp");
        if(message instanceof JmsTextMessage)
            this.body = ((JmsTextMessage)message).getText();
        if(message instanceof JmsBytesMessage) {
            byte[] messageBytes = message.getBody(byte[].class);
            this.body = messageBytes;
            
        }
    }

    public static MessageType getMessageType(JmsMessage message) throws JMSException {
        if(message.propertyExists("messageType"))
        {
            if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.CAM.toString()))
                return MessageType.CAM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.DATEX2.toString()))
                return MessageType.DATEX2;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.DENM.toString()))
                return MessageType.DENM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.IVIM.toString()) || message.getStringProperty("messageType").equalsIgnoreCase("IVI"))
                return MessageType.IVIM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.MAPEM.toString()))
                return MessageType.MAPEM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.SPATEM.toString()))
                return MessageType.SPATEM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.SREM.toString()))
                return MessageType.SREM;
            else if(message.getStringProperty("messageType").equalsIgnoreCase(MessageType.SSEM.toString()))
                return MessageType.SSEM;
            else
            {
                System.out.println(message.getStringProperty("messageType"));
                System.out.println(MessageType.DENM.toString());
                return MessageType.UNKNOWN;
            }   
        }
        else {
            throw new JMSException("no message type found");
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public InterchangeMessage setPublisherId(String publisherId) {
        this.publisherId = publisherId;
        return this;
    }

    public InterchangeMessage setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public InterchangeMessage setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public InterchangeMessage setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public enum MessageType {
        DATEX2, DENM, IVIM, SPATEM, MAPEM, SREM, SSEM, CAM, UNKNOWN
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("| publisherName:\t" + publisherName);
        sb.append("\n| originatingCountry:\t" + originatingCountry);
        sb.append("\n| protocolVersion:\t" + protocolVersion);
        sb.append("\n| messageType:\t-\t" + messageType);
        sb.append("\n| quadTree:\t-\t" + this.quadTree);
        sb.append("\n| latitude:\t-\t" + latitude);
        sb.append("\n| longitude:\t-\t" + longitude);
        if (publisherId != null)
            sb.append("\n| publisherId:\t-\t" + publisherId);
        if (contentType != null)
            sb.append("\n| content-type:\t-\t" + contentType);
        if (relation != null)
            sb.append("\n| relation:\t-\t" + relation);
        if (timestamp != -1)
            sb.append("\n| timestamp:\t-\t" + timestamp + " ("
                    + Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()) + ")");
        return sb.toString();
    }

    @Override
    // TODO: give better name/structure. this method copies props from "this" to the jms message
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException {
        message.setStringProperty("publisherName", publisherName);
        message.setStringProperty("originatingCountry", originatingCountry);
        message.setStringProperty("protocolVersion", protocolVersion);
        message.setStringProperty("messageType", messageType.name());
        message.setStringProperty("quadTree", quadTree);
        message.setDoubleProperty("latitude", latitude);
        message.setDoubleProperty("longitude", longitude);
        if (publisherId != null)
            message.setStringProperty("publisherId", publisherId);
        if (relation != null)
            message.setStringProperty("relation", relation);
        if (timestamp != -1)
            message.setLongProperty("timestamp", timestamp);
        return message;
    }

    @Override
    public JmsMessage getJmsMessage(Session session, Object messageBody) throws JMSException {
        JmsBytesMessage message = (JmsBytesMessage) session.createBytesMessage();
        this.setBody(messageBody);
        message.writeObject(messageBody);
        this.addPropertiesToJmsMsg(message);
        return message;
    }
    @Override
    public JmsMessage getJmsMessage(Session session) throws JMSException {
        JmsBytesMessage message = (JmsBytesMessage) session.createBytesMessage();
        message.writeObject(this.getBody());
        this.addPropertiesToJmsMsg(message);
        return message;
    }

    @Override
    public Object getBody() {
        return body;
    }

    @Override
    public InterchangeMessage setBody(Object body) {
        this.body = body;
        return this;
    }

    // FIX THIS; STREIGHT FROM STACK OVERFLOW
    /* s must be an even-length string. */
    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ", "");
        s = s.trim();
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String printHexBinary(byte[] data) {
        char[] hexCode = "0123456789ABCDEF".toCharArray();
        StringBuilder r = new StringBuilder(data.length*2);
        for ( byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}

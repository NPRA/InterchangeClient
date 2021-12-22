package no.vegvesen.nw3;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;

public class MapemMessage extends InterchangeMessage {
    String id;//o
    String name;//o

    public MapemMessage(String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude, String[] quadTree) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.MAPEM, latitude, longitude, quadTree);
    }

    public MapemMessage(String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.MAPEM, latitude, longitude, new String[]{Quadtree.latLonToQuadtree(latitude, longitude, 18)});
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException {
        super.addPropertiesToJmsMsg(message);
        if(id != null) message.setStringProperty("id", this.id);
        if(name != null) message.setStringProperty("name", this.name);
        return message;
    }

    @Override
    public JmsBytesMessage getJmsMessage(Session session, Object messageBody) throws JMSException {
        JmsBytesMessage message = (JmsBytesMessage) session.createBytesMessage();
        message.writeObject(messageBody);
        this.addPropertiesToJmsMsg(message);
        return message;
    }

    @Override
    public JmsBytesMessage getJmsMessage(Session session) throws JMSException {
        JmsBytesMessage message = (JmsBytesMessage) super.getJmsMessage(session);
        this.addPropertiesToJmsMsg(message);
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(id != null) sb.append("\n| id:\t-\t"+id);
        if(name != null) sb.append("\n| name:\t-\t"+name);
        return super.toString()+sb.toString();
    }

    public static void main(String[] args) {
        MapemMessage im = new MapemMessage("Norwegian Public Roads Administration", "NO00000",
            "NO", "MAPEM:1.0", 64.4, 10.4);
        im.setId("123");
        im.setName("navn");
        System.out.println(im);
    }
}

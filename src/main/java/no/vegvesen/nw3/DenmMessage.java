package no.vegvesen.nw3;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;

public class DenmMessage extends InterchangeMessage {

    String serviceType;//o
    String causeCode;//m
    String subCauseCode;//o

    public DenmMessage(String causeCode, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion, double latitude, double longitude, String[] quadTree) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DENM, latitude, longitude, quadTree);
        this.causeCode = causeCode;
    }

    public DenmMessage(String causeCode, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DENM, latitude, longitude, new String[]{Quadtree.latLonToQuadtree(latitude, longitude, 18)});
        this.causeCode = causeCode;
    }

    public DenmMessage(JmsMessage message) throws JMSException {
        super(message);
        if (message.propertyExists("serviceType"))
            serviceType = message.getStringProperty("serviceType");
        if (message.propertyExists("causeCode"))
            causeCode = message.getStringProperty("causeCode");
        if (message.propertyExists("subCauseCode"))
            subCauseCode = message.getStringProperty("subCauseCode");
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setSubCauseCode(String subCauseCode) {
        this.subCauseCode = subCauseCode;
    }

    @Override
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException {
        super.addPropertiesToJmsMsg(message);
        if(serviceType != null) message.setStringProperty("serviceType", this.serviceType);
        if(subCauseCode != null) message.setStringProperty("subCauseCode", this.subCauseCode);
        message.setStringProperty("causeCode", this.causeCode);
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
        if(serviceType != null) sb.append("\n| serviceType:\t-\t"+serviceType);
        if(subCauseCode != null) sb.append("\n| subCauseCode:\t-\t"+subCauseCode);
        sb.append("\n| causeCode:\t-\t"+causeCode);
        return super.toString()+sb.toString();
    }
    
    public static void main(String[] args) {
        DenmMessage dm = new DenmMessage("1","Norwegian Public Roads Administration", "NO00000",
            "NO", "DENM:1.0", 64.4, 10.4);
        dm.setServiceType("HLN-RLX");
        dm.setSubCauseCode("3");
        System.out.println(dm);
    }
}

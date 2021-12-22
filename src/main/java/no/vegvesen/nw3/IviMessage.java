package no.vegvesen.nw3;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;

public class IviMessage extends InterchangeMessage {

    String serviceType;//o
    int iviType = -1;//o
    String pictogramCategoryCode;//o
    String iviContainer;

    public IviMessage(JmsMessage message) throws JMSException {
        super(message);
        if (message.propertyExists("serviceType"))
            serviceType = message.getStringProperty("serviceType");
        if (message.propertyExists("iviType"))
            iviType = message.getIntProperty("iviType");
        if (message.propertyExists("pictogramCategoryCode"))
            pictogramCategoryCode = message.getStringProperty("pictogramCategoryCode");
        if (message.propertyExists("iviContainer"))
            iviContainer = message.getStringProperty("iviContainer");
            

    }

    public IviMessage(String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude, String[] quadTree) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.IVIM, latitude, longitude, quadTree);
    }

    public IviMessage(String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.IVIM, latitude, longitude, new String[]{Quadtree.latLonToQuadtree(latitude, longitude, 18)});
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setIviType(int iviType) {
        this.iviType = iviType;
    }

    public void setPictogramCategoryCode(String pictogramCategoryCode) {
        this.pictogramCategoryCode = pictogramCategoryCode;
    }

    public void setIviContainer(String iviContainer) {
        this.iviContainer = iviContainer;
    }

    @Override
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException {
        super.addPropertiesToJmsMsg(message);
        if(serviceType != null) message.setStringProperty("serviceType", this.serviceType);
        if(iviType != -1) message.setIntProperty("iviType", this.iviType);
        if(pictogramCategoryCode != null) message.setStringProperty("pictogramCategoryCode", this.pictogramCategoryCode);
        if(iviContainer != null) message.setStringProperty("iviContainer", this.iviContainer);
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
        if(iviType != -1) sb.append("\n| iviType:\t-\t"+iviType);
        if(pictogramCategoryCode != null) sb.append("\n| pictogramCategoryCode:\t"+pictogramCategoryCode);
        if(iviContainer != null) sb.append("\n| iviContainer:\t-\t"+iviContainer);
        return super.toString()+sb.toString();
    }

    public static void main(String[] args) {
        IviMessage im = new IviMessage("Norwegian Public Roads Administration", "NO00000",
            "NO", "IVIM:1.0", 64.4, 10.4);
        im.setServiceType("HLN-RLX");
        im.setIviType(32);
        System.out.println(im);
    }
}

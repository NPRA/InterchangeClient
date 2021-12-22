package no.vegvesen.nw3;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsMessage;

public interface MessageInterface {
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException;
    public JmsMessage getJmsMessage(Session session, Object messageBody) throws JMSException;
    public JmsMessage getJmsMessage(Session session) throws JMSException;
    public Object getBody();
    public MessageInterface setBody(Object body);
}

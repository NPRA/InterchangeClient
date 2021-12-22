package no.vegvesen.interchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsMessage;

import no.vegvesen.nw3.DatexMessage;
import no.vegvesen.nw3.DenmMessage;
import no.vegvesen.nw3.InterchangeMessage;
import no.vegvesen.nw3.IviMessage;
import no.vegvesen.nw3.DatexMessage.Datex2v2PublicationType;
import no.vegvesen.nw3.InterchangeMessage.MessageType;

public class NW3client implements MessageListener
{
	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;
	private MessageConsumer messageConsumer;

	/**
	 * Inits the client.
	 * Edit settings in /resources/jndi.properties
	 * 
	 * @param createConsumer Should we consume messages
	 * @param createProducer Will we produce messages
	 * @param callback	Callback class. Runs when we receive messages.
	 */
    public void init(boolean createConsumer, boolean createProducer, MessageListener callback)
    {
    	try
		{
			Context context = new InitialContext();
			
			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("TLS");
			factory.setPopulateJMSXUserID(true);
			
			System.out.println( Color.GREEN+"Connecting to: "+factory.getRemoteURI());
            Destination queueR = (Destination) context.lookup("ReadQueue");
            Destination queueS = (Destination) context.lookup("WriteQueue");
            System.out.println( Color.GREEN+"rece queue: "+queueR.toString());
            System.out.println( Color.GREEN+"send queue: "+queueS.toString());
            
            connection = factory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            if(createConsumer) messageConsumer = session.createConsumer(queueR);
            if(createConsumer) messageConsumer.setMessageListener(callback);
			if(createProducer) messageProducer = session.createProducer(queueS);
			System.out.println(Color.YELLOW+"Waiting for messages.."+Color.RESET);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
	/**
	 * Callback method that runs when we receive a message.
	 * Prints the message application properties and body.
	 */
	@Override
	public void onMessage(Message pmsg)
	{	
		try {
			JmsMessage msg = (JmsMessage) pmsg;
			msg.acknowledge();
 
			MessageType type = InterchangeMessage.getMessageType(msg);
			System.out.println("Got message ("+type.name()+"):");
			if(type == MessageType.DATEX2){
				DatexMessage datexmessage = new DatexMessage(msg);
				System.out.println(Color.CYAN+datexmessage.toString()+Color.RESET);
				System.out.println(Color.MAGENTA+datexmessage.getBody()+Color.RESET);
			}
			else if(type == MessageType.DENM){
				DenmMessage denmmessage = new DenmMessage(msg);
				System.out.println(Color.CYAN+denmmessage.toString()+Color.RESET);
				System.out.println(Color.MAGENTA+"Body of length: "+((byte[])denmmessage.getBody()).length+Color.RESET);
			}
			else if(type == MessageType.IVIM){
				IviMessage ivimmessage = new IviMessage(msg);
				System.out.println(Color.CYAN+ivimmessage.toString()+Color.RESET);
				System.out.println(Color.MAGENTA+"Body of length: "+((ByteBuffer)ivimmessage.getBody()).capacity()+Color.RESET);
			}
			else{
				System.err.println(Color.RED+"Could not recognise the message type"+Color.RESET);
			}
			// Print hex if the type is not datex
			if (type != MessageType.DATEX2)
				System.out.println("Body:"+InterchangeMessage.printHexBinary(msg.getBody(byte[].class)));
		} 
		catch (Exception e4) { 
			System.err.println("Not a Byte message");
			e4.printStackTrace();
		}
	}
	
	public void sendMessage(InterchangeMessage message) 
	{	
		try {
			messageProducer.send(message.getJmsMessage(session));
		} 
		catch (JMSException e) {
			e.printStackTrace();
		}
	}


	public void sendMessage(String msg)
	{
		sendMessage(MessageType.DENM, msg);
	}
	
	/**Sends a test message to the producer 
	 * 
	 * @param messageType Type of message to send
	 * @param body The body of the message (can be text for datex, or hex-string for binary messages)
	 */
	private void sendMessage(MessageType messageType, String body)
	{
		System.out.println(Color.RED);

		InterchangeMessage msg = null;
		if(messageType == MessageType.DATEX2)
		{
			msg = new DatexMessage(Datex2v2PublicationType.SITUATIONPUBLICATION, 
				new String[]{"MaintenanceWorks","RoadOrCarriagewayOrLaneManagement"}, "Norwegian Public Roads Administration", "NO00000",
				"NO", 
				"DATEX2:2.3", 
				63.4, 
				10.4);
		}
		else if(messageType == MessageType.DENM)
		{
			DenmMessage denm = new DenmMessage("1","Norwegian Public Roads Administration", "NO00000", "NO", "DENM:1.2.2", 63.1, 10.4);
			denm.setSubCauseCode("3");
			msg = denm;
		}
		else if(messageType == MessageType.IVIM)
		{
			IviMessage ivim = new IviMessage("Norwegian Public Roads Administration", "NO00000", "NO", "IVIM:1.2.2", 63.1, 10.4);
			msg = ivim;
		}
		if(msg != null) 
		{
			System.out.println("Sending message: \n"+msg.toString());
		}
		try {
			if(msg == null)
				System.err.println("Could not create message (message == null)");
			else if(msg.getMessageType() == MessageType.DENM || msg.getMessageType() == MessageType.IVIM)
				messageProducer.send(msg.getJmsMessage(session, InterchangeMessage.hexStringToByteArray(body)));
			else if(msg.getMessageType() == MessageType.DATEX2)
				messageProducer.send(msg.getJmsMessage(session, body));
			else
				System.err.println("Could not create message");
		} 
		catch (JMSException e) {
			e.printStackTrace();
		}
		System.out.println(Color.RESET);
	}

	/**
	 * 
	 * @return The current session used by the client
	 */
	public Session getSession()
	{
		return session;
	}
	
	/**
	 * Closes the connection to the AMQP server
	 */
	public void close()
	{
		try
		{
			System.out.println("closing");
			System.out.println(Color.RESET);
			connection.close();
		} 
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * connects to the AMQP server defined in src/main/resources/jndi.properties
	 * 
	 * type s <body> to send a message
	 * type e to exit
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.setProperty("javax.net.ssl.keyStoreType","pkcs12");
		if(args.length > 0)
		{
			System.setProperty("javax.net.ssl.keyStore",args[0]);
			System.setProperty("javax.net.ssl.trustStore",args[1]);
		}
		else
		{
			System.setProperty("javax.net.ssl.keyStore","C:\\path\\to\\keystore\\nw3test.p12");
			System.setProperty("javax.net.ssl.trustStore","C:\\path\\to\\truststore\\nw3test.jks");
		}

		System.setProperty("javax.net.ssl.keyStorePassword","PASSWORD");
        System.setProperty("javax.net.ssl.trustStorePassword","PASSWORD");
        //System.setProperty("javax.net.debug","ssl:handshake"); //use this to debug SSL errors

		NW3client c = new NW3client();
		c.init(true, true, c);
		
		BufferedReader commandLine = new java.io.BufferedReader(new InputStreamReader(System.in));
		
		while(true)
        {
			try
			{
				String s = commandLine.readLine();
			
	            if (s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("e") ||  s.equalsIgnoreCase("c")) 
	            {
	                c.close();
	                System.out.println("exiting..");
	                System.exit(0);
	            }
	            else if(s.startsWith("s ") || s.startsWith("send "))
	            {
	            	c.sendMessage(s.substring(s.indexOf(" ")));
	            }
            } catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
        }
	}



	enum Color {
		//Color end string, color reset
		RESET("\033[0m"),
	
		// Regular Colors. Normal color, no bold, background color etc.
		BLACK("\033[0;30m"),    // BLACK
		RED("\033[0;31m"),      // RED
		GREEN("\033[0;32m"),    // GREEN
		YELLOW("\033[0;33m"),   // YELLOW
		BLUE("\033[0;34m"),     // BLUE
		MAGENTA("\033[0;35m"),  // MAGENTA
		CYAN("\033[0;36m"),     // CYAN
		WHITE("\033[0;37m"),    // WHITE
	
		// Bold
		BLACK_BOLD("\033[1;30m"),   // BLACK
		RED_BOLD("\033[1;31m"),     // RED
		GREEN_BOLD("\033[1;32m"),   // GREEN
		YELLOW_BOLD("\033[1;33m"),  // YELLOW
		BLUE_BOLD("\033[1;34m"),    // BLUE
		MAGENTA_BOLD("\033[1;35m"), // MAGENTA
		CYAN_BOLD("\033[1;36m"),    // CYAN
		WHITE_BOLD("\033[1;37m"),   // WHITE
	
		// Underline
		BLACK_UNDERLINED("\033[4;30m"),     // BLACK
		RED_UNDERLINED("\033[4;31m"),       // RED
		GREEN_UNDERLINED("\033[4;32m"),     // GREEN
		YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
		BLUE_UNDERLINED("\033[4;34m"),      // BLUE
		MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
		CYAN_UNDERLINED("\033[4;36m"),      // CYAN
		WHITE_UNDERLINED("\033[4;37m"),     // WHITE
	
		// Background
		BLACK_BACKGROUND("\033[40m"),   // BLACK
		RED_BACKGROUND("\033[41m"),     // RED
		GREEN_BACKGROUND("\033[42m"),   // GREEN
		YELLOW_BACKGROUND("\033[43m"),  // YELLOW
		BLUE_BACKGROUND("\033[44m"),    // BLUE
		MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
		CYAN_BACKGROUND("\033[46m"),    // CYAN
		WHITE_BACKGROUND("\033[47m"),   // WHITE
	
		// High Intensity
		BLACK_BRIGHT("\033[0;90m"),     // BLACK
		RED_BRIGHT("\033[0;91m"),       // RED
		GREEN_BRIGHT("\033[0;92m"),     // GREEN
		YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
		BLUE_BRIGHT("\033[0;94m"),      // BLUE
		MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
		CYAN_BRIGHT("\033[0;96m"),      // CYAN
		WHITE_BRIGHT("\033[0;97m"),     // WHITE
	
		// Bold High Intensity
		BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
		RED_BOLD_BRIGHT("\033[1;91m"),      // RED
		GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
		YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
		BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
		MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
		CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
		WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE
	
		// High Intensity backgrounds
		BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
		RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
		GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
		YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
		BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
		MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
		CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
		WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE
	
		private final String code;
	
		Color(String code) {
			this.code = code;
		}
	
		@Override
		public String toString() {
			return code;
		}
	}
}

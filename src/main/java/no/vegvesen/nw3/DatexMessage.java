package no.vegvesen.nw3;

import java.time.Instant;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

public class DatexMessage extends InterchangeMessage {

    String publicationType;
    String publicationSubType = "";

    public DatexMessage(DatexPublicationTypeInterface datexType, String[] publSubType, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion, double latitude, double longitude, String[] quadTree) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DATEX2, latitude, longitude, quadTree);
        this.publicationType = datexType.getPublicationType();
        for (String type : publSubType) {
            this.publicationSubType = this.publicationSubType+","+type;
        }
        this.publicationSubType = this.publicationSubType+",";
    }

    public DatexMessage(String publicationType, String[] publSubType, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion, double latitude, double longitude, String[] quadTree) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DATEX2, latitude, longitude, quadTree);
        this.publicationType = publicationType;
        for (String type : publSubType) {
            this.publicationSubType = this.publicationSubType+","+type;
        }
        this.publicationSubType = this.publicationSubType+",";
    }

    public DatexMessage(String publicationType, String[] publSubType, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion,double latitude, double longitude) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DATEX2, latitude, longitude, new String[]{Quadtree.latLonToQuadtree(latitude, longitude, 18)});
        this.publicationType = publicationType;
        for (String type : publSubType) {
            this.publicationSubType = this.publicationSubType+","+type;
        }
        this.publicationSubType = this.publicationSubType+",";
    }

    public DatexMessage(DatexPublicationTypeInterface datexType, String[] publSubType, String publisherName, String publisherId, String originatingCountry, 
            String protocolVersion, double latitude, double longitude) {
        super(publisherName, publisherId, originatingCountry, protocolVersion, MessageType.DATEX2, latitude, longitude, new String[]{Quadtree.latLonToQuadtree(latitude, longitude, 18)});
        this.publicationType = datexType.getPublicationType();
        for (String type : publSubType) {
            this.publicationSubType = this.publicationSubType+","+type;
        }
        this.publicationSubType = this.publicationSubType+",";
    }

    public DatexMessage(JmsMessage message) throws JMSException {
        super(message);
        if (message.propertyExists("publicationType"))
            publicationType = message.getStringProperty("publicationType");
        if (message.propertyExists("publicationSubType"))
            publicationSubType = message.getStringProperty("publicationSubType");
    }

    @Override
    public DatexMessage setBody(Object bodyString)
    {
        this.body =  bodyString;
        return this;
    }

    @Override
    public String getBody()
    {
        return (String) body;
    }

    public enum DatexType {
        DATEX2V3, DATEX2V2
    }

    public interface DatexPublicationTypeInterface {

        public String getPublicationType();

    }

    public enum Datex2v2PublicationType implements DatexPublicationTypeInterface {
        SITUATIONPUBLICATION {
            @Override
            public String getPublicationType() {

                return "SituationPublication";
            }
        },
        ELABORATEDDATAPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ElaboratedDataPublication";
            }
        },
        MEASUREMENTSITETABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "MeasurementSiteTablePublication";
            }
        },
        MEASUREDDATAPUBLICATION {
            @Override
            public String getPublicationType() {

                return "MeasuredDataPublication";
            }
        },
        PREDEFINEDLOCATIONSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "PredefinedLocationsPublication";
            }
        },
        PARKINGTABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ParkingTablePublication";
            }
        },
        PARKINGSTATUSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ParkingStatusPublication";
            }
        },
        PARKINGVEHICLESPUBLICATION {
            @Override
            public String getPublicationType() {

                return "SituationPublication";
            }
        },
        VMSTABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "VmsTablePublication";
            }
        },
        VMSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "VmsPublication";
            }
        };
    }

    public enum Datex2v3PublicationType implements DatexPublicationTypeInterface {
        SITUATIONPUBLICATION {
            @Override
            public String getPublicationType() {

                return "SituationPublication";
            }
        },
        ELABORATEDDATAPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ElaboratedDataPublication";
            }
        },
        MEASUREMENTSITETABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "MeasurementSiteTablePublication";
            }
        },
        MEASUREDDATAPUBLICATION {
            @Override
            public String getPublicationType() {

                return "MeasuredDataPublication";
            }
        },
        PREDEFINEDLOCATIONSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "PredefinedLocationsPublication";
            }
        },
        PARKINGTABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ParkingTablePublication";
            }
        },
        PARKINGSTATUSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ParkingStatusPublication";
            }
        },
        PARKINGVEHICLESPUBLICATION {
            @Override
            public String getPublicationType() {

                return "ParkingVehiclesPublication";
            }
        },
        VMSTABLEPUBLICATION {
            @Override
            public String getPublicationType() {

                return "VmsTablePublication";
            }
        },
        VMSPUBLICATION {
            @Override
            public String getPublicationType() {

                return "VmsPublication";
            }
        };
    }

    @Override
    public JmsMessage addPropertiesToJmsMsg(JmsMessage message) throws JMSException {
        message = super.addPropertiesToJmsMsg(message);
        message.setStringProperty("publicationType", this.publicationType);
        message.setStringProperty("publicationSubType", this.publicationSubType);
        return message;
    }

    @Override
    public JmsTextMessage getJmsMessage(Session session, Object messageBodyString) throws JMSException {
        JmsTextMessage message = (JmsTextMessage) session.createTextMessage((String)messageBodyString);
        this.addPropertiesToJmsMsg(message);
        return message;
    }

    @Override
    public JmsTextMessage getJmsMessage(Session session) throws JMSException {
        JmsTextMessage message = (JmsTextMessage) session.createTextMessage(this.getBody());
        this.addPropertiesToJmsMsg(message);
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n| publicationType:\t"+publicationType);
        sb.append("\n| publicationSubType:\t"+publicationSubType);
        return super.toString()+sb.toString();
    }

    public String toStringBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n| publicationType:\t"+publicationType);
        sb.append("\n| publicationSubType:\t"+publicationSubType);
        sb.append("\n-------------- Body: --------------\n");
        if(body != null) sb.append((String) body);
        sb.append("\n-------------- Body end --------------\n");
        return super.toString()+sb.toString();
    }

    public static void main(String[] args) {
        DatexMessage dm = new DatexMessage(
            Datex2v2PublicationType.MEASUREDDATAPUBLICATION, 
            new String[]{"IndividualVehicleDataValues"}, 
            "Norwegian Public Roads Administration", 
            "NO00000",
            "NO", "DATEX2:2.3", 64.4, 10.4);
        dm.setContentType("application/xml");
        dm.setPublisherId("NO0000");
        dm.setTimestamp(Instant.now().toEpochMilli());
        System.out.println(dm);
    }
}

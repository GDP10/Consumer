package net.robinjam.aes;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Endpoint;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.robinjam.aes.wsn.NotificationBrokerService;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@javax.jws.WebService(
	serviceName = "NotificationConsumerService",
	portName = "NotificationConsumerPort",
	targetNamespace = "http://robinjam.net/aes/wsn",
	wsdlLocation = "wsn.wsdl",
	endpointInterface = "org.oasis_open.docs.wsn.bw_2.NotificationConsumer"
)
public class Consumer implements NotificationConsumer {
	
	public static void main(String[] args) throws NotifyMessageNotSupportedFault, UnacceptableInitialTerminationTimeFault, ResourceUnknownFault, InvalidTopicExpressionFault, InvalidMessageContentExpressionFault, TopicNotSupportedFault, TopicExpressionDialectUnknownFault, InvalidProducerPropertiesExpressionFault, SubscribeCreationFailedFault, UnrecognizedPolicyRequestFault, InvalidFilterFault, UnsupportedPolicyRequestFault, SAXException, IOException, ParserConfigurationException {
		Consumer consumer = new Consumer();
		Endpoint endpoint = Endpoint.create(consumer);
		endpoint.publish("http://localhost:9000/wsn/NotificationConsumer");
		URL wsdlLocation = null;
		try {
			wsdlLocation = new URL("http://localhost:8080/aes-0.0.1-SNAPSHOT/aes/NotificationBroker?wsdl");
		} catch (MalformedURLException e) {}
		NotificationBrokerService notificationBrokerService = new NotificationBrokerService(wsdlLocation);
		NotificationBroker notificationBroker = notificationBrokerService.getNotificationBrokerPort();
		Subscribe subscribeRequest = new Subscribe();
		subscribeRequest.setConsumerReference(endpoint.getEndpointReference(W3CEndpointReference.class));
		FilterType filters = new FilterType();
		String source = "<ogc:Filter xmlns:ogc=\"foo\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:BBOX><ogc:PropertyName>Geometry</ogc:PropertyName><gml:Envelope><gml:lowerCorner>-100 -100</gml:lowerCorner><gml:upperCorner>100 100</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
		filters.getAny().add(doc.getDocumentElement());
		subscribeRequest.setFilter(filters);
		notificationBroker.subscribe(subscribeRequest);
		System.out.println("Subscribed!");
	}

	public void notify(Notify notification) {
		System.out.println(notification);
		List<NotificationMessageHolderType> notificationMessage = notification.getNotificationMessage();
		if (!notificationMessage.isEmpty()) {
			NotificationMessageHolderType notificationMessageHolder = notificationMessage.get(0);
			try {
				System.out.println(getSource((Element) notificationMessageHolder.getMessage().getAny()));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getSource(Element doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.getBuffer().toString();
	}


}

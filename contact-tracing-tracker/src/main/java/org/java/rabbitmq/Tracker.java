package org.java.rabbitmq;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.model.PersonDetail;
import org.java.model.PersonTrackerDetails;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Tracker {
	 private final static String EXCHANGE_NAME = "position";
	 private final static String EXCHANGE_NAME_QUERY = "query";
	 private final static String BINDING_KEY ="#";
	 
	 
	 Channel receiveChannel, queryReceiveChannel;
	 String queueName;
	 Connection connection;
	 
	 boolean terminate;
	 Map<String, PersonDetail> playerMap;
	 List<PersonTrackerDetails> personTrackerDetailsList;
	 
	 Tracker() {
		 try {
			 terminate = false;
			 playerMap = new HashMap<String, PersonDetail>();
			 personTrackerDetailsList = new ArrayList<PersonTrackerDetails>();
			 
			 ConnectionFactory factory = new ConnectionFactory();
		     factory.setHost("localhost");	        
		     connection = factory.newConnection();
		     
		     receiveChannel = connection.createChannel();
		     receiveChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
		     queueName = receiveChannel.queueDeclare().getQueue();		        
		     receiveChannel.queueBind(queueName, EXCHANGE_NAME, BINDING_KEY);
		     
		     queryReceiveChannel = connection.createChannel();
		     queryReceiveChannel.exchangeDeclare(EXCHANGE_NAME_QUERY, "topic");       
		     queryReceiveChannel.queueBind(queryReceiveChannel.queueDeclare().getQueue(), EXCHANGE_NAME_QUERY, BINDING_KEY);
		     
		     
		     
		 }catch(Exception e) {
				e.printStackTrace();
			}
	 }
	
	public void receiveMessages() {
		try {
				DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				byte[] byteArray = delivery.getBody();

				try
				{
					PersonDetail personDetail = (PersonDetail) deserialize(byteArray);
					playerMap.put(personDetail.getName(), personDetail.clone());						
					checkPlayersPosition(personDetail.getName(), personDetail.getX(),personDetail.getY());					
					displayPlayersPosition();
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			};
			receiveChannel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void displayPlayersPosition() {

		try {
			new ProcessBuilder("cmd","/c","cls").inheritIO().start().waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Current Positions");
		System.out.println("------------------");
		
		for(PersonDetail pd:playerMap.values()) {
			System.out.println(pd.getName() +"|"+ pd.getX() +"|"+pd.getY());
		}
		
	}
	
	public void checkPlayersPosition(String name, int x, int y) {
		PersonTrackerDetails personTrackerDetails = new PersonTrackerDetails();
		boolean isMatchFound = false;
		for(PersonDetail personDetail:playerMap.values()) {
			if(x == personDetail.getX() && y == personDetail.getY()) {
				personTrackerDetails.addName(personDetail.getName());
				isMatchFound = true;
			}
		}
		
		if(isMatchFound) {			
			personTrackerDetails.addName(name);
			personTrackerDetailsList.add(personTrackerDetails);
		}
	}
	
	public List<String> getUniquePersonContactList(String name) {
		
		Set<String> tempSet = new HashSet<String>();
						
		for(PersonTrackerDetails personTrackerDetails :personTrackerDetailsList) {
			for(String tempName:personTrackerDetails.getNameList()) {
				if(tempName.equalsIgnoreCase(name)) {
					tempSet.addAll(personTrackerDetails.getNameList());
				}
			}
		}
		
		List<String> tempList = new ArrayList<String>(tempSet);
        Collections.sort(tempList,Comparator.reverseOrder());
        return tempList;
	}
	
	public void queryResponse(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
        	
        	Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(EXCHANGE_NAME_QUERY, false, false, false, null);
//            channel.queuePurge(EXCHANGE_NAME_QUERY);

            channel.basicQos(1);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                byte[] byteArray = null;

                try {
                	String name = new String(delivery.getBody(), "UTF-8");
                    
                	byteArray = getByteArray(getUniquePersonContactList(name));

                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, byteArray);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);                   
                }
            };

            channel.basicConsume(EXCHANGE_NAME_QUERY, false, deliverCallback, (consumerTag -> { }));
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }
	
	public static byte[] getByteArray(List<String> personTrackerDetailsList) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(personTrackerDetailsList);
		return out.toByteArray();
	}
	
	public static Object deserialize(byte[] byteArray) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
	
	public void startProcess() {	
		queryResponse();
		while(!terminate)
			receiveMessages();
	}

	public static void main(String[] args) throws Exception {
		
		Tracker tracker = new Tracker();
		tracker.startProcess();
		
		if(tracker.connection != null)
			tracker.connection.close();
		
		System.out.println("Exiting Process");		
		System.exit(0);
	}
	

}

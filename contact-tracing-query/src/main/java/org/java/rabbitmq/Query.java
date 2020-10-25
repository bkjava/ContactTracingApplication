package org.java.rabbitmq;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Query {
		 private final static String EXCHANGE_NAME_QUERY = "query";
	 static Scanner sc = new Scanner(System.in);
	 
	 String name;
	 
	 Channel sendChannel;
	 String queueName;
	 Connection connection;
	 
	 Query() {
		 try {
			 ConnectionFactory factory = new ConnectionFactory();
		     factory.setHost("localhost");	        
		     connection = factory.newConnection();
		     
		     sendChannel = connection.createChannel();	        	
		     sendChannel.exchangeDeclare(EXCHANGE_NAME_QUERY, "topic");
		     queueName = sendChannel.queueDeclare().getQueue();	
		     		     
		 }catch(Exception e) {
				e.printStackTrace();
			}
	 }
	
	public void sendMessage() {		
		try { 			
			final String corrId = UUID.randomUUID().toString();

	        String replyQueueName = sendChannel.queueDeclare().getQueue();
	        AMQP.BasicProperties props = new AMQP.BasicProperties
	                .Builder()
	                .correlationId(corrId)
	                .replyTo(replyQueueName)
	                .build();

	        sendChannel.basicPublish("", EXCHANGE_NAME_QUERY, props, name.getBytes("UTF-8"));


	        String ctag = sendChannel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
	            if (delivery.getProperties().getCorrelationId().equals(corrId)) {            	                
	                try {
						List<String> personTrackerDetailsList = (List<String>) deserialize(delivery.getBody());
						
						if(personTrackerDetailsList != null && personTrackerDetailsList.size() > 0)	{
							System.out.println("Contact Persons ");
							System.out.println("-----------------");
							for(String tempName: personTrackerDetailsList)
								if(!tempName.equalsIgnoreCase(name))
									System.out.println(tempName);
						}
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
	            }
	        }, consumerTag -> {
	        });
	       
	        sendChannel.basicCancel(ctag);    
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
    }
	
	
	public static Object deserialize(byte[] byteArray) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
	
	public void startProcess() {		
		sendMessage();
	}

	public static void main(String[] args) throws Exception{
		
		Query query = new Query();
		
		System.out.println("Enter the name ");
		
		String tempName = sc.nextLine();
		
		if(tempName != null && tempName.trim().length() > 0) {
			query.name = tempName;			
			query.startProcess();		
		} 
		else {
			System.out.println("Name cannot be blanks!!! Exiting the Process..");
		}		
		
		if(sc != null)
			sc.close();		
		
		if(query.connection != null)
			query.connection.close();
		
		System.exit(0);
	}
	

}

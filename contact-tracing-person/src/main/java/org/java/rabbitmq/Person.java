package org.java.rabbitmq;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import org.java.model.PersonDetail;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Person {
	 private final static String EXCHANGE_NAME = "position";
	 private final static String ROUTING_KEY ="#";
	 static Scanner sc = new Scanner(System.in);
	 
	 Connection connection;
	 
	 String personIdentifier;
	 Integer speed;
	 String middlewareEndpoint;
	 Integer maxBoardX;
	 Integer maxBoardY;
	 
	 Integer boardX;
	 Integer boardY;
	 
	 Channel sendChannel;
	 boolean terminate;
	 	
	public void sendMessage() {		
		try { 
			
			PersonDetail msg = new PersonDetail();
			msg.setName(personIdentifier);
			msg.setX(boardX);
			msg.setY(boardY);
//			System.out.println(personIdentifier+":"+boardX+":"+boardY);
			byte[] byteArray = getByteArray(msg);
			
		     sendChannel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, byteArray);	        
		}catch(Exception e) {
			e.printStackTrace();
		}		
    }
	
	public static byte[] getByteArray(PersonDetail msgDetail) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(msgDetail);
		return out.toByteArray();
	}
	
	public void startProcess() throws InterruptedException {
		
		try {
			 terminate = false;
			 ConnectionFactory factory = new ConnectionFactory();
		     factory.setHost(middlewareEndpoint);	        
		     Connection connection = factory.newConnection();
		     
		     sendChannel = connection.createChannel();	        	
		     sendChannel.exchangeDeclare(EXCHANGE_NAME, "topic");		     	     
		     
		 }catch(Exception e) {
				e.printStackTrace();
		 }
		
		boardX = (int) Math.floor(Math.random()*maxBoardX);
		boardY = (int) Math.floor(Math.random()*maxBoardY);	
		
		Random random = new Random();
		
		boolean moveSelected = false;
						
		while(!terminate) {
					
			sendMessage();		
			
			while(!moveSelected) {				
				int rand = (int)Math.round(Math.random()* (random.nextBoolean() ? -1 : 1));	
				if(random.nextBoolean()) {
					if(boardX == maxBoardX) {
						boardX--;
						moveSelected = true;
					}
					else if(boardX == 0) {
						boardX++;
						moveSelected = true;
					}
					else if(boardX < maxBoardX && boardX > 0 && rand != 0) {					
						boardX = boardX + rand;
						moveSelected = true;
					}	
				}
				else {
					if(boardY == maxBoardY) {
						boardY--;
						moveSelected = true;
					}
					else if(boardY == 0) {
						boardY++;
						moveSelected = true;
					}
					else if(boardY < maxBoardY && boardY > 0 && rand != 0) {					
						boardY = boardY + rand;
						moveSelected = true;
					}					
				}							
			}
			
			moveSelected = false;
			Thread.sleep(speed*3000);
		}
				 
	}

	public static void main(String[] args) throws Exception {
		
		Person person = new Person();
		
		System.out.print("Enter the Middleware Endpoint: ");		
		String middlewareEndpoint = sc.nextLine();
		
		System.out.print("Enter the Person Identifier:");
		String personIdentifier = sc.nextLine();
		
		System.out.println("Enter the movement speed, fast(f) / slow(s):");
		String movementSpeed = sc.nextLine();
		
				
		try (InputStream input = Person.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            prop.load(input);

            person.maxBoardX = Integer.parseInt(prop.getProperty("boardsize.x"));
            person.maxBoardY = Integer.parseInt(prop.getProperty("boardsize.y"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
		
		try {
			
			if((middlewareEndpoint != null && middlewareEndpoint.trim().length() > 0)
					&& (personIdentifier != null && personIdentifier.trim().length() > 0)
					&& (movementSpeed !=null && (movementSpeed.equalsIgnoreCase("F") || movementSpeed.equalsIgnoreCase("S")))) {
				person.personIdentifier = personIdentifier;	
				person.middlewareEndpoint = middlewareEndpoint;
				if(movementSpeed.equalsIgnoreCase("F"))
					person.speed = 1;
				else 
					person.speed = 2;
				person.startProcess();		
			} 
			else {
				System.out.println("Invalid Input Parameters!!! Exiting the Process..");
			}		
		}catch (Exception e) {
			
		}
		
		if(sc != null)
			sc.close();	
		
		if(person.connection != null)
			person.connection.close();		
		System.exit(0);
	}

}

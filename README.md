# ContactTracingApplication
 Contact Tracing Application has 3 modules.
 1. Person - This module connects to the localhost rabbitmq, randomly generates (x,y) coordinates and push this information to the queue. 
 2. Tracker - This module manages the updated Person positions and if any players occupies the same grid, then the person contacts will be stored in a list.
 3. Query - This module queries the Person Contact by providing the person name to the Tracker, which in turn shares all the person's name with whom the provided person had contacts.
  
To use:
	1 - Create a docker container running rabbitmq with the command:
		docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 2. Navigate to \contact-tracing-person\target and open the command prompt  and execute with the command:
  java -jar contact-tracing-0.0.1-SNAPSHOT.jar
 3. Navigate to \contact-tracing-tracker\target and open the command prompt  and execute with the command:
  java -jar contact-tracing-0.0.1-SNAPSHOT.jar
 4.  Navigate to \contact-tracing-query\target and open the command prompt  and execute with the command:
  java -jar contact-tracing-0.0.1-SNAPSHOT.jar


References:
	For rabbitmq, made with reference to tutorials found at:
		RabbitMQ.RabbitMQ Tutorials. Retrieved from https://www.rabbitmq.com/getstarted.html
  
  @Author - Mousa Aldabbas

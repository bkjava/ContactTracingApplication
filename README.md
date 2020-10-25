# ContactTracingApplication
 Contact Tracing Application has 3 projects.
 Person - This module connects to the localhost rabbitmq, randomly generates (x,y) coordinates and push this information to the queue. 
 Tracker - This module manages the updated Person positions and if any players occupies the same grid, then the person contacts will be stored in a list.
 Query - This module queries the Person Contact by providing the person name to the Tracker, which in turn shares all the person's name with whom the provided person had contacts.

To run the program, simply run the Main class. 
If you want to configure the number of threads or the number of requests each thread sends and any other configuration related to the program, you can do so by changing the values in the PostSkiersEndpointTest class inside the PostSkiersEndpointTest folder.
Note that since the Test and the ResultHandling are in separate files, you can run them separately. 
But the main method inside the Main class calls them in a stream. So to make sure behave correctly, Make sure the csvPath passed into the ResultHandler is the same as the CSV_PATH inside the PostSkiersEndpointTest class. 
Also note the CSVWriter will overwrite the file if it already exists.

To change the request Endpoint, you can change the BASE_URL in the PostSkiersEndpointTest class. 

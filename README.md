# Java-Web-Server
Simple Java web server which can respond to some HTTP/1.1 requests.

Server handles multiple connections with maximum of 2 Threads.  
Server can return binary images (GIF, JPEG and PNG).  
In header it also sends time and date when it was send and File location.  
Sends not found html if file does not exist.  
Implements HEAD, GET, DELETE and JOKE requests.  

Run: java WebServerMain  
Usage: java WebServerMain <\document_root>\ </port>/

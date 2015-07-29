# Fault-Tolerant-Storage-System

This project is a storage system with RESTful API access for storing data. The system minimizes data replication while maintaining high levels of fault tolerance by deriving special codes from the data and storing both the codes data across multiple machines. Even if numerous machines fail, a decoding algorithm can regenerate the lost data using the remaining codes and data stored on other machines.

To run this project:
- Install and run [MongoDB](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows/). MongoDB is used by the storage system to store metadata.
- Edit the config file in the main directory - this specifies the MongoDB settings and the URL addresses of the storage servers that the system will write data to.
- Do: mvn jetty:run -Djetty.port=<port> on all the machines used in this system

Accessing the APIs:
- GET to /storage/ with parameter "id" -> this retrieves the data with the specified ID and returns it in the response body. A response header titled "status" specifies the response code (see below)
- POST to /storage/ with the data in the request body -> stores the data in the system. A JSON object with the key-value "storageId" is returned - this is the ID used to retrieve the data
- PUT to /storage/ with parameter "id" -> regenerates lost data with the given ID

Response Codes:
- 0 -> Everything went well
- 10 -> Could not find data with the specified ID
- 11 -> Data is retrieved, but something went wrong and we need to repair it
- 99 -> Too many storage machines failed, and the data cannot be regenerated

Inspired by [this article](https://code.facebook.com/posts/536638663113101/saving-capacity-with-hdfs-raid/) in the Facebook Engineering Blog! It seemed really cool so I used it as inspiration for building my own storage system :)

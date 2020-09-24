Summary
=======

180__MarketPrice__Websocket is an example of an OMM Interactive Provider
application written to the EMA library.

This application demonstrates the basic usage of the EMA library in providing
of OMM MarketPrice data to the Advanced Distribution Hub with websocket
connection.

180__MarketPrice__Websocket illustrates how to create and publish a single OMM
streaming item. This application uses source directory configured in
the EmaConfig.xml file.


Detailed Description
====================

180__MarketPrice__Websocket implements the following high-level steps:

+ Instantiates and modifies an OmmIProviderConfig object:
  - Sets the Provider Name to "Provider_2" to use websocket connection
+ Instantiates an OmmProvider object which:
  - listens on the port from the EmaConfig.xml file
  - specifies the websocket connection type and websocket sub protocols from the EmaConfig.xml file
  - loads source directory from the EmaConfi.xml file
+ Accepts a login request
+ Processes an item request for MarketPrice domain.
 - Creates streaming item (refresh and updates) and publishes them
 - Publishes updates 1 per second for 60 seconds.
+ Rejects subsequent item requests until an existing item is closed.
+ Exits

Note: If needed, these and other details may be modified to fit your local
      environment. For details on standard configuration, refer to the EMA
      library ReadMe.txt file and EMA Configuration Guide.

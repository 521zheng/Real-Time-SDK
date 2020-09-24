///*|----------------------------------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      	--
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  					--
// *|           Copyright (C) 2019 Refinitiv. All rights reserved.            		--
///*|----------------------------------------------------------------------------------------------------

package com.rtsdk.ema.examples.training.consumer.series100.ex140_MBO_Streaming;

import com.rtsdk.ema.access.Msg;

import java.nio.ByteBuffer;

import com.rtsdk.ema.access.AckMsg;
import com.rtsdk.ema.access.GenericMsg;
import com.rtsdk.ema.access.RefreshMsg;
import com.rtsdk.ema.access.StatusMsg;
import com.rtsdk.ema.access.UpdateMsg;
import com.rtsdk.ema.access.DataType;
import com.rtsdk.ema.access.DataType.DataTypes;
import com.rtsdk.ema.access.EmaFactory;
import com.rtsdk.ema.access.EmaUtility;
import com.rtsdk.ema.access.FieldEntry;
import com.rtsdk.ema.access.FieldList;
import com.rtsdk.ema.access.Map;
import com.rtsdk.ema.access.MapEntry;
import com.rtsdk.ema.access.OmmConsumer;
import com.rtsdk.ema.access.OmmConsumerClient;
import com.rtsdk.ema.access.OmmConsumerEvent;
import com.rtsdk.ema.access.OmmException;
import com.rtsdk.ema.rdm.EmaRdm;

class AppClient implements OmmConsumerClient
{
	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)
	{
		System.out.println("Item Name: " + (refreshMsg.hasName() ? refreshMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (refreshMsg.hasServiceName() ? refreshMsg.serviceName() : "<not set>"));
		
		System.out.println("Item State: " + refreshMsg.state());
		
		if (DataType.DataTypes.MAP == refreshMsg.payload().dataType())
			decode(refreshMsg.payload().map());
		
		System.out.println();
	}
	
	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event) 
	{
		System.out.println("Item Name: " + (updateMsg.hasName() ? updateMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (updateMsg.hasServiceName() ? updateMsg.serviceName() : "<not set>"));
		
		if (DataType.DataTypes.MAP == updateMsg.payload().dataType())
			decode(updateMsg.payload().map());
		
		System.out.println();
	}

	public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event) 
	{
		System.out.println("Item Name: " + (statusMsg.hasName() ? statusMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (statusMsg.hasServiceName() ? statusMsg.serviceName() : "<not set>"));

		if (statusMsg.hasState())
			System.out.println("Item State: " +statusMsg.state());
		
		System.out.println();
	}
	
	public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
	public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
	public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent){}
	
	void decode(FieldList fieldList)
	{
		for (FieldEntry fieldEntry : fieldList)
		{
			System.out.println("Fid: " + fieldEntry.fieldId() + " Name: " + fieldEntry.name() + " value: " + fieldEntry.load());
		}
	}
	
	void decode(Map map)
	{
				
		for (MapEntry map_entry : map) {
			//String element = map_entry.key().buffer().toString();
			//APIQA
			String element = EmaUtility.asAsciiString(map_entry.key().buffer());
			System.out.println(element);
		}
			//APIQA
/*
		if (DataTypes.FIELD_LIST == map.summaryData().dataType())
		{
			System.out.println("Map Summary data:");
			decode(map.summaryData().fieldList());
			System.out.println();
		}
		
		for (MapEntry mapEntry : map)
		{					
			if (DataTypes.BUFFER == mapEntry.key().dataType())
			//	System.out.println("Action: " + mapEntry.mapActionAsString() + " key value: " + EmaUtility.asHexString(mapEntry.key().buffer().buffer()));
				System.out.println("Action: " + mapEntry.mapActionAsString() + " key value: " + new String(mapEntry.key().buffer().buffer().array()));
					
			
			if (DataTypes.ASCII == mapEntry.key().dataType())
				//		System.out.println("Action: " + mapEntry.mapActionAsString() + " key value: " + EmaUtility.asHexString(mapEntry.key().buffer().buffer()));
						System.out.println("Action: " + mapEntry.mapActionAsString() + " key value: " + mapEntry.key().ascii().toString());
			
			if (DataTypes.FIELD_LIST == mapEntry.loadType())
			{
				System.out.println("Entry data:");
				decode(mapEntry.fieldList());
				System.out.println();
			}
		}
	*/	
	}
	
}

public class Consumer 
{
	public static void main(String[] args)
	{
		OmmConsumer consumer = null;
		try
		{
			AppClient appClient = new AppClient();
			
			consumer  = EmaFactory.createOmmConsumer(EmaFactory.createOmmConsumerConfig().host("localhost:14002").username("user"));
			
			consumer.registerClient( EmaFactory.createReqMsg().domainType(EmaRdm.MMT_MARKET_BY_ORDER)
															.serviceName("DIRECT_FEED").name("AGG.V"), appClient, 0);
			
			Thread.sleep(60000);			// API calls onRefreshMsg(), onUpdateMsg() and onStatusMsg()
		}
		catch (InterruptedException | OmmException excp)
		{
			System.out.println(excp.getMessage());
		}
		finally 
		{
			if (consumer != null) consumer.uninitialize();
		}
	}
}

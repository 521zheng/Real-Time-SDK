///*|----------------------------------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      	--
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  					--
// *|           Copyright (C) 2019 Refinitiv. All rights reserved.            		--
///*|----------------------------------------------------------------------------------------------------

package com.thomsonreuters.ema.examples.training.consumer.series100.example170__MarketPrice__ChannelInfo;

import com.thomsonreuters.ema.access.Msg;

import com.thomsonreuters.ema.access.AckMsg;
import com.thomsonreuters.ema.access.ChannelInformation;
import com.thomsonreuters.ema.access.GenericMsg;
import com.thomsonreuters.ema.access.RefreshMsg;
import com.thomsonreuters.ema.access.StatusMsg;
import com.thomsonreuters.ema.access.UpdateMsg;
import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.OmmConsumer;
import com.thomsonreuters.ema.access.OmmConsumerClient;
import com.thomsonreuters.ema.access.OmmConsumerEvent;
import com.thomsonreuters.ema.access.OmmException;

class AppClient implements OmmConsumerClient
{
	boolean updateCalled = false;	
	
	public void onRefreshMsg( RefreshMsg refreshMsg, OmmConsumerEvent event )
	{
		System.out.println( refreshMsg + "\nevent channel info (refresh)\n" + event.channelInformation() );
	}

	public void onUpdateMsg( UpdateMsg updateMsg, OmmConsumerEvent event ) 
	{
		if (!updateCalled)
		{
			updateCalled = true;
			System.out.println( updateMsg + "\nevent channel info (update)\n" + event.channelInformation() );
		}
		else
			System.out.println( "skipped printing updateMsg" );			
	}

	public void onStatusMsg( StatusMsg statusMsg, OmmConsumerEvent event ) 
	{
		System.out.println( statusMsg + "\nevent channel info (status)\n" + event.channelInformation() );
	}

	public void onGenericMsg( GenericMsg genericMsg, OmmConsumerEvent consumerEvent ){}
	public void onAckMsg( AckMsg ackMsg, OmmConsumerEvent consumerEvent ){}
	public void onAllMsg( Msg msg, OmmConsumerEvent consumerEvent ){}
}

public class Consumer 
{
	public static void main(String[] args)
	{
		OmmConsumer consumer = null;
		try
		{
			AppClient appClient = new AppClient();
			ChannelInformation ci = EmaFactory.createChannelInformation();

			consumer  = EmaFactory.createOmmConsumer(EmaFactory.createOmmConsumerConfig( "EmaConfig.xml" ).username( "user" ));
			consumer.channelInformation( ci );
			System.out.println( "channel information (consumer):\n\t" + ci );
			
			consumer.registerClient( EmaFactory.createReqMsg().serviceName( "DIRECT_FEED" ).name( "IBM.N" ), appClient, 0);

			Thread.sleep( 60000 );			// API calls onRefreshMsg(), onUpdateMsg() and onStatusMsg()
		}
		catch (InterruptedException | OmmException excp)
		{
			System.out.println( excp.getMessage() );
		}
		finally 
		{
			if (consumer != null) consumer.uninitialize();
		}
	}
}

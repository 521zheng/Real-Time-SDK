package com.refinitiv.ema.examples.RCC_Contribution_Example;
///*|----------------------------------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      	--
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  					--
// *|           Copyright Refinitiv 2020. All rights reserved.            		--
///*|----------------------------------------------------------------------------------------------------

import com.refinitiv.ema.access.*;
import com.refinitiv.ema.access.AckMsg;
import com.refinitiv.ema.access.ElementList;
import com.refinitiv.ema.access.FieldList;
import com.refinitiv.ema.access.GenericMsg;
import com.refinitiv.ema.access.Msg;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;
import com.refinitiv.ema.rdm.EmaRdm;
import com.refinitiv.eta.codec.*;

//import com.refinitiv.eta.codec.RealImpl;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.refinitiv.ema.examples.RCC_Contribution_Example.Contributor.bMsgSendFinished;
import static com.refinitiv.ema.examples.RCC_Contribution_Example.Contributor.qMsgs;
class AppClient implements OmmConsumerClient	{

	private OmmConsumer _ommConsumer;
	private long _tunnelStreamHandle, _subStreamHandle;
	private int _postStreamID;
	private boolean _subItemOpen;
	private EciborgMessage _ecibMsg;
	private ConcurrentHashMap<Long, EciborgMessage> _hashMap = new ConcurrentHashMap<Long, EciborgMessage>();
	long _postID = 1;
	int _bid = 340, _ask = 341;


	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)	{
		System.out.println("----- Refresh message ----");
		System.out.println("event subStreamHandle: " + event.handle());
		System.out.println("event parStreamHandle: " + event.parentHandle());
		System.out.println("event closure: " + event.closure());

		System.out.println(refreshMsg);
		if(refreshMsg.hasMsgKey()){

        }
		if(refreshMsg.domainType() == EmaRdm.MMT_LOGIN && refreshMsg.state().streamState() == OmmState.StreamState.OPEN && refreshMsg.state().dataState() == OmmState.DataState.OK)	{
			// 3. Login accepted, app can post data now
			System.out.println("Login accepted, starting posting...");
			
			_postStreamID = refreshMsg.streamId();
			/*will be used in send post message*/
			System.out.println("Login accepted, postStreamID:=" + _postStreamID);
			//postMessage();
			//sendTartanGenericMsg(event);
			partialUpdate();
		}
		else	{
			System.out.println("Stream not open");
		}
	}

	
	public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event)	{
		System.out.println("----- Status message ----");
		System.out.println(statusMsg);
		
		if(!_subItemOpen && event.handle() == _tunnelStreamHandle && statusMsg.state().streamState() == OmmState.StreamState.OPEN)	{
			// create a login request message
			ElementList elementList = EmaFactory.createElementList();
			elementList.add(EmaFactory.createElementEntry().ascii("Password", "3Fvx9KYig84m0F9w51QNXiiT14wlB7"));
			ReqMsg rMsg = EmaFactory.createReqMsg()
				.domainType(EmaRdm.MMT_LOGIN)
				.name("GE-A-00740437-3-6545")
				.attrib(elementList)
				// .privateStream(true)
				.serviceId(statusMsg.serviceId())
				.streamId(statusMsg.streamId());
			
			System.out.println("Sending client login request...");
			// get events from login substream	
			_subStreamHandle = _ommConsumer.registerClient(rMsg, this, 2, _tunnelStreamHandle);
			_subItemOpen = true;
			System.out.println("register client parStreamHandle: " + _tunnelStreamHandle);
			System.out.println("register client subStreamHandle: " + _subStreamHandle);
		}
	}
	
	
	public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent)	{
		System.out.println("----- Ack message ----");
		System.out.println("Received AckMsg. Item Handle: " + consumerEvent.handle() + " Closure: " + consumerEvent.closure());
		System.out.println(ackMsg);
		if(!isPostOk(ackMsg)){
			System.out.println("AckMsg shows posting message FAILED");
		}
		System.out.println("Continue posting...");
		//postMessage();
		//postCleardownMessage();
		postDateTimeMessage();
	}
	public void postDateTimeMessage(){
		try	{ Thread.sleep(300); } catch(Exception e) {}

		// populate the contributed FIDs and values
		FieldList nestedFieldList = EmaFactory.createFieldList();
		//2. DATE
		//GV3_DATE   "GV3 DATE"            1789  NULL        DATE               11  DATE             4
		//nestedFieldList.add( EmaFactory.createFieldEntry().date(1789, 0, 0, 0 ) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().date(1789, ""));
		//nestedFieldList.add( EmaFactory.createFieldEntry().date(1789, 2022, 4, 4 ) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().date(1789, "12 MAR 22"));


		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 5, 255, 255, 255) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 5,"8:31") );
		nestedFieldList.add( EmaFactory.createFieldEntry().time( 5,"") );
		// create an update message for our item
		UpdateMsg nestedUpdateMsg = EmaFactory.createUpdateMsg()
				.streamId(_postStreamID)
				.name("JZJP0X3FTA=07")
				.payload(nestedFieldList);

		// post this market price message
		_ommConsumer.submit(EmaFactory.createPostMsg()
				.streamId(_postStreamID)
				.postId(_postID++)
				.domainType(EmaRdm.MMT_MARKET_PRICE)
				.solicitAck(true)
				.complete(true)
				.payload(nestedUpdateMsg), _subStreamHandle);
	}
	public void postCleardownMessage(){
		try	{ Thread.sleep(300); } catch(Exception e) {}

		// populate the contributed FIDs and values
		FieldList nestedFieldList = EmaFactory.createFieldList();
		//1. REAL
		//BID        "BID"                   22  BID_1       PRICE              17  REAL64           7
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(21, 21, OmmReal.MagnitudeType.EXPONENT_NEG_1));
		//nestedFieldList.add(EmaFactory.createFieldEntry().realBlank(25));         //force to blank, no input value specified
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "0"));  		//0  	--blank
		//	nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "0.00"));  	//0.00 	--blank

		//nestedFieldList.add(EmaFactory.createFieldEntry().realBlank(22));
		//2. DATE
		//GV3_DATE   "GV3 DATE"            1789  NULL        DATE               11  DATE             4
		//nestedFieldList.add( EmaFactory.createFieldEntry().date( 1789, 0, 0, 0 ) );
	    //3. TIME
		//TIMACT     "TIME OF UPDATE"         5  NULL        TIME                5  TIME             5
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 5, 255, 255, 255) );
		//GV3_TIME   "GENERIC TIME (3)"    2738  NULL        TIME_SECONDS        8  TIME             5
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 2738, 255, 255, 255) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().dateTime( -3, 0, 0, 0,255, 255, 255) );
		//4. INTEGER
		//ACC_DAYS   "ACCRUED DAYS"        3267  NULL        INTEGER            10  UINT64           5
		//nestedFieldList.add( EmaFactory.createFieldEntry().realBlank(3267));  //INTEGER same as real
		//nestedFieldList.add( EmaFactory.createFieldEntry().intValueBlank(3267)); //not work

		//5. BINARY
		//PREV_DISP  "PREV DISP TMPLT"     3263  NULL        BINARY              3  UINT32           2
		//nestedFieldList.add( EmaFactory.createFieldEntry().buffer(1080, ByteBuffer.wrap("TEST".getBytes())));
		//PREF_DISP  "PREF DISP TMPLT"     1080  NULL        BINARY              3  UINT32           2
		//nestedFieldList.add( EmaFactory.createFieldEntry().realBlank(1080  ));
		//6. BUFFER
		//MY_BUFFER  "MY_BUFFER"            -11  NULL        ALPHANUMERIC       10  BUFFER          4
		//nestedFieldList.add(EmaFactory.createFieldEntry().buffer( -11, ByteBuffer.wrap("ABCDEFGH".getBytes())));


		//7. STRING
		//GEN_TEXT16 "GEN TEXT16"           995  NULL        ALPHANUMERIC       16  RMTES_STRING    16
		//nestedFieldList.add( EmaFactory.createFieldEntry().rmtes( 995, ByteBuffer.wrap("".getBytes())) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().rmtes( 1000, ByteBuffer.wrap("TEST".getBytes())) );


		//8. ENUM
		//PUTCALLIND "PUT/CALL FLAG"        109  NULL        ENUMERATED    2 ( 4 )  ENUM             1
		//nestedFieldList.add(EmaFactory.createFieldEntry().enumValue(109, 3));
		nestedFieldList.add(EmaFactory.createFieldEntry().enumBlank(109));
		//nestedFieldList.add(EmaFactory.createFieldEntry().enumValue(37, 3));
		//nestedFieldList.info(1,1);

		// create an update message for our item
		UpdateMsg nestedUpdateMsg = EmaFactory.createUpdateMsg()
				.streamId(_postStreamID)
				.name("JZJP0X3FTA=07")
				.payload(nestedFieldList);

		// post this market price message
		_ommConsumer.submit(EmaFactory.createPostMsg()
				.streamId(_postStreamID)
				.postId(_postID++)
				.domainType(EmaRdm.MMT_MARKET_PRICE)
				.solicitAck(true)
				.complete(true)
				.payload(nestedUpdateMsg), _subStreamHandle);
	}
	public void postMessage()	{
		try	{ Thread.sleep(300); } catch(Exception e) {}

		// populate the contributed FIDs and values 
		FieldList nestedFieldList = EmaFactory.createFieldList();
		//nestedFieldList.add(EmaFactory.createFieldEntry().realBlank(22));

		//GV3_DATE   "GV3 DATE"            1789  NULL        DATE               11  DATE             4
		//nestedFieldList.add( EmaFactory.createFieldEntry().date( 1789, 0, 0, 0 ) );
		/*
		*  static final int BLANK_HOUR = 255;
		static final int BLANK_MINUTE = 255;
		static final int BLANK_SECOND = 255;
		static final int BLANK_MILLI = 65535;
		static final int BLANK_MICRO_NANO = 2047;
    	*/
		//TIMACT     "TIME OF UPDATE"         5  NULL        TIME                5  TIME             5
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 5, 02, 13, 04) );
		//GV3_TIME   "GENERIC TIME (3)"    2738  NULL        TIME_SECONDS        8  TIME             5
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 2738, 02, 13, 04, 005 ) );//TIMESCONDS 5
		//blank time
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 2738, 255, 255, 255) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 5, 255, 255, 255) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().time( 2738, 255, 255, 255, 65535,2047 , 2047) ); //not work
		//nestedFieldList.add( EmaFactory.createFieldEntry().dateTime( -3, 0, 0, 0,255, 255, 255) );

		//ACC_DAYS   "ACCRUED DAYS"        3267  NULL        INTEGER            10  UINT64           5
		//nestedFieldList.add( EmaFactory.createFieldEntry().realBlank(3267));  //INTEGER same as real
		//nestedFieldList.add( EmaFactory.createFieldEntry().intValue(3267, 0));
		//nestedFieldList.add( EmaFactory.createFieldEntry().intValueBlank(3267)); //not work
		//PREV_DISP  "PREV DISP TMPLT"     3263  NULL        BINARY              3  UINT32           2
		//nestedFieldList.add( EmaFactory.createFieldEntry().uintValue(3267 ,0 ));
		//nestedFieldList.add( EmaFactory.createFieldEntry().realBlank(1080  ));
		//nestedFieldList.add( EmaFactory.createFieldEntry().realBlank(3267));

		//nestedFieldList.info(1,1);

		//nestedFieldList.add(EmaFactory.createFieldEntry().real(22, _bid++, OmmReal.MagnitudeType.EXPONENT_NEG_1));
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, _ask++, OmmReal.MagnitudeType.EXPONENT_NEG_1));
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "1.88000")); //1.881
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "+0")); //<blank>
		nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "0.00"));  //0, 0.00 --blank
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, "-0"));  //0
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, -0, OmmReal.MagnitudeType.EXPONENT_0));
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(22, +0, OmmReal.MagnitudeType.EXPONENT_0));
		//nestedFieldList.add(EmaFactory.createFieldEntry().realBlank(22));

		nestedFieldList.add(EmaFactory.createFieldEntry().real(275, 0, OmmReal.MagnitudeType.EXPONENT_0));
		nestedFieldList.add(EmaFactory.createFieldEntry().real(393, +0, OmmReal.MagnitudeType.EXPONENT_0));
		nestedFieldList.add( EmaFactory.createFieldEntry().date( 1789, 1999, 11, 7 ) );
		nestedFieldList.add( EmaFactory.createFieldEntry().time( 2738, 02, 03, 04, 005 ) );
		nestedFieldList.add( EmaFactory.createFieldEntry().rmtes( 995, ByteBuffer.wrap(" ".getBytes())) );
		//nestedFieldList.add( EmaFactory.createFieldEntry().buffer(22,ByteBuffer.wrap("+0".getBytes())));


		//com.refinitiv.eta.codec.FieldEntry fieldEntry = CodecFactory.createFieldEntry();
		//fieldEntry.clear();
		//fieldEntry.fieldId(22);
		//fieldEntry.dataType(com.refinitiv.eta.codec.DataTypes.REAL);


		//nestedFieldList.add( EmaFactory.createFieldEntry().rmtes( 995, ByteBuffer.wrap("No Data".getBytes())) );
		//RealImpl real = (RealImpl) CodecFactory.createReal();
		//nestedFieldList.add(EmaFactory.createFieldEntry().real(25, -0, OmmReal.MagnitudeType.NEG_INFINITY));


		//nestedFieldList.add(EmaFactory.createFieldEntry().real(22, _bid++, OmmReal.MagnitudeType.EXPONENT_NEG_1));
		//nestedFieldList.add(EmaFactory.createFieldEntry().floatValue(275, (float)1.7));
		//nestedFieldList.add(EmaFactory.createFieldEntry().floatValue(393, (float)1.8));
		//nestedFieldList.add(EmaFactory.createFieldEntry().rmtes(22, ByteBuffer.wrap("1.8118".getBytes())));
		//nestedFieldList.add(EmaFactory.createFieldEntry().rmtes(25, ByteBuffer.wrap("1.008".getBytes())));
		// create an update message for our item
		UpdateMsg nestedUpdateMsg = EmaFactory.createUpdateMsg()
			.streamId(_postStreamID)
			.name("JZJP0X3FTA=07")
			.payload(nestedFieldList);
			
		// post this market price message
		_ommConsumer.submit(EmaFactory.createPostMsg()
			.streamId(_postStreamID)
			.postId(_postID++)
			.domainType(EmaRdm.MMT_MARKET_PRICE)
			.solicitAck(true)
			.complete(true)
			.payload(nestedUpdateMsg), _subStreamHandle);
		/***
		 *
		 * PostMsg postMsg = EmaFactory.createPostMsg();
		 * 		UpdateMsg upd = EmaFactory.createUpdateMsg();
		 * 		FieldList fl = EmaFactory.createFieldList();
		 *
		 * 		fl.add(EmaFactory.createFieldEntry().real(22, 34, OmmReal.MagnitudeType.EXPONENT_POS_1));
		 * 		fl.add(EmaFactory.createFieldEntry().real(25, 35, OmmReal.MagnitudeType.EXPONENT_POS_1));
		 * 		fl.add(EmaFactory.createFieldEntry().time(18, 11, 29, 30));
		 * 		fl.add(EmaFactory.createFieldEntry().enumValue(37, 3));
		 *
		 * 		upd.payload(fl);
		 *
		 * 		_ommConsumer.submit( postMsg.postId( 1 ).serviceId( 1 )
		 * 				.name( "IBM.N" ).solicitAck( true ).complete(true)
		 * 				.payload(upd), event.handle() );
		 */
	}

	
	public void setOmmConsumer(OmmConsumer ommConsumer)	{
		_ommConsumer = ommConsumer;
	}
	
	public void setTunnelHandle(long tunnelStreamHandle)	{
		_tunnelStreamHandle = tunnelStreamHandle;
	}

	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event)	{
		System.out.println("----- Update message ----");
		System.out.println("Handle: " + event.handle());
		System.out.println("Parent Handle: " + event.parentHandle());
		System.out.println("Closure: " + event.closure());

		System.out.println(updateMsg);
	}

	public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent)	{
		System.out.println("----- Generic message ----");
		System.out.println(genericMsg);
	}

	public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent)	{
        System.out.println(	"----- All message ----");
		//System.out.println(msg);
    }
	private void sendTartanGenericMsg(OmmConsumerEvent event)
	{
		System.out.println("---- Update page ric ----");
		try	{
			Thread.sleep(300);
		} catch(Exception e) {

		}
		if (qMsgs.isEmpty()) {
			bMsgSendFinished = true;
			return;
		}

		_ecibMsg = qMsgs.poll();
		UpdateMsg upd = _ecibMsg.getUpdateMsg();
		_ecibMsg.setRccPostId(_postID);
		upd.streamId(_postStreamID);
		_hashMap.putIfAbsent(_postID, _ecibMsg);

		_ommConsumer.submit(EmaFactory.createPostMsg()
				.streamId(_postStreamID)
				.postId(_postID++)
				.domainType(EmaRdm.MMT_MARKET_PRICE)
				.solicitAck(true)
				.complete(true)
				.payload(upd), _subStreamHandle);
	}
	private void partialUpdate(){
		System.out.println("---partialUpdate------");
		try	{ Thread.sleep(300); } catch(Exception e) {}
		FieldList fl = EmaFactory.createFieldList();
		UpdateMsg upd = EmaFactory.createUpdateMsg();

		Integer fid = new Integer(318);
		String line = "Mexican Partialeso     MXN          20.0740          20.0904         20.08220";
		String line2 =",,,,,,,,,,,,,,,,70,,,,,,,,,,,,,,,,,,,,,,00.00:00:19,,,,,,,,,,,,,,,00.00:00:00";
		//<CSI>18<RHPA>70
		byte[] bLine2= {0x1B, 0x5B,'1','8',0x60,'7','0',0x1B, 0x5B,'3','9',0x60, '0','0','.','0','0','0','0',':','1','9'};
		fid = new Integer(338);
		byte[] bLine3 ={0x1B,0x5B,'2','5',0x60,'F',0x1B,0x5B,'8',0x62};

		//byte[] bLine3 ={EciborgMessage.CS,EciborgMessage.SI,'2','5',EciborgMessage.RHPA,'F',EciborgMessage.CS,EciborgMessage.SI,'8',EciborgMessage.RREP};
		//byte[] bLine3 ={EciborgMessage.CS,EciborgMessage.SI,'2','5',EciborgMessage.RHPA,'F','a','i','l','e','d',' ',EciborgMessage.CS,EciborgMessage.SI,'8',EciborgMessage.RREP};
		//byte[] bLine= {0x1B,0x5B,0x31,0x38,0x60,0x37,0x30};
		//byte[] bLine= {0x1B,0x5B,0x31,0x38,0x60,'7','0'};
		//<CSI>25<RHPA>F<CSI>8<RREP>
		//<CSI>20<RHPA><CSI>10<CLR>
		byte[] bLine4 ={0x1B,0x5B,'2','0',0x60,0x1B,0x5B,'1','0',0x4e};
		//<CSI><0x31><0x35><0x60>ABCDEFGHIJ<CSI><0x34><0x30><0x60>WXYZ<CSI><0x33><0x4E>G<CSI><0x32><0x62>
		byte[] bLine5 = {EciborgMessage.CS, EciborgMessage.SI,0x31,0x35,EciborgMessage.RHPA,
				'A','B','C','D','E','F','G','H','I','J', EciborgMessage.CS, EciborgMessage.SI,0x34,0x30,EciborgMessage.RHPA,'W','X','Y','Z'};
				//,EciborgMessage.CS, EciborgMessage.SI,0x33,EciborgMessage.CLR,'G',EciborgMessage.CS, EciborgMessage.SI,0x32,EciborgMessage.RREP};

		byte[] bLine6 = {EciborgMessage.CS, EciborgMessage.SI,0x33,EciborgMessage.CLR,'G',EciborgMessage.CS, EciborgMessage.SI,0x32,EciborgMessage.RREP};
		/*
		*
		* case +0
		*
		* */
		//fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(line2.getBytes())));
		//fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(bLine2))); //RHPHA
		//fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(bLine4)));   //RREP
		//fl.add(EmaFactory.createFieldEntry().rmtes(316, ByteBuffer.wrap(bLine2)));
		fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(bLine5)));   //RREP
		/*String FULL_STRING= "abcdefghijklmnopqrstuvwx-abcdefghijklmnopqrstuvwx-abcdefghijklmnopqrstuvwx-yyzzz";
		int MAX_FID = 339;
		int FIRST_FID_TO_UPDATE = 320;
		Integer fid = new Integer(FIRST_FID_TO_UPDATE);
		while (fid <= MAX_FID){
			fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(FULL_STRING.getBytes())));
			fid += 1;
		}
		//upd.name(ricName);
		//upd.payload(fl);
		*/
		upd.name("MPLD-DDNMCIB01A2");
		upd.payload(fl);
		upd.streamId(_postStreamID);

		_ommConsumer.submit(EmaFactory.createPostMsg()
				.streamId(_postStreamID)
				.postId(_postID++)
				.domainType(EmaRdm.MMT_MARKET_PRICE)
				.solicitAck(true)
				.complete(true)
				.payload(upd), _subStreamHandle);
	}
	private boolean isPostOk( AckMsg ackMsg )
	{
		/*
			AckMsg
				streamId="5"
				domain="MarketPrice Domain"
				ackId="1"
				nackCode="SymbolUnknown"
				text="Symbol posted has not been recognised"
			AckMsgEnd
		*/
		if ( ackMsg.hasMsgKey() )
			System.out.println("Item Name: " + ( ackMsg.hasName() ? ackMsg.name() : "not set" ) +  "\nService Name: "
					+ ( ackMsg.hasServiceName() ? ackMsg.serviceName() : "not set" ) );

		System.out.println("Ack Id: "  + ackMsg.ackId());
		if(_ecibMsg!=null) {
			System.out.println("Ecib post id " + _ecibMsg.getRccPostId());
			if (_hashMap.containsKey(ackMsg.ackId())) {
				System.out.println("find in map for key: " + ackMsg.ackId());
				EciborgMessage em = _hashMap.remove(ackMsg.ackId());
				/*
				 * q = em.getSendMsgQueue();
				 *  sendMsgQueue.add(em)
				 *  socket send thread will pick up it and send via socket
				 * */
			}
		}
		if ( ackMsg.hasNackCode() )
			System.out.println("Nack Code: " + ackMsg.nackCodeAsString());

		if ( ackMsg.hasText() )
			System.out.println("Text: " + ackMsg.text());

		if (ackMsg.hasNackCode()){
			System.out.println("Msg Posting failed. And Ack's nack code is: " + ackMsg.nackCodeAsString());
			if (ackMsg.hasText()){
				System.out.println("Msg Posting failed. And Ack's nack text is: "  + ackMsg.text());
			}
			return false;
		}

		return true;
	}
}


public class Contributor	{
	public static final ConcurrentLinkedQueue<EciborgMessage> qMsgs = new ConcurrentLinkedQueue<>();
	public static boolean bMsgSendFinished = false;


	public static void main(String[] args)	{
		//testCleardown3();
		//testCleardown2();
		//testCleardown();

			TRCEFIDPageEncoder codec = new TRCEFIDPageEncoder();
			codec.encode("D:\\Workspace\\gtlipage.txt");
			//codec.encodeBlankPage();
			//codec.encodeFullPage();


		try		{
			System.out.println("Contributing to Refinitiv Contributions Channel");
			AppClient appClient = new AppClient();

			System.out.println("Starting encrypted connection...");
			// Create an OMM consumer
			OmmConsumer consumer = EmaFactory.createOmmConsumer(EmaFactory.createOmmConsumerConfig("./EmaConfig.xml")
				/*.tunnelingKeyStoreFile("KEYSTORE_FILE_NAME")
				.tunnelingKeyStorePasswd("KEYSTORE_PASSWORD")*/);

			ClassOfService cos = EmaFactory.createClassOfService()
				.authentication(EmaFactory.createCosAuthentication().type(CosAuthentication.CosAuthenticationType.NOT_REQUIRED))
				.dataIntegrity(EmaFactory.createCosDataIntegrity().type(CosDataIntegrity.CosDataIntegrityType.RELIABLE))
				.flowControl(EmaFactory.createCosFlowControl().type(CosFlowControl.CosFlowControlType.BIDIRECTIONAL).recvWindowSize(1200))
				.guarantee(EmaFactory.createCosGuarantee().type(CosGuarantee.CosGuaranteeType.NONE));

			System.out.println("Starting tunnel stream...");
			// Create a request for a tunnel stream
			TunnelStreamRequest tsr = EmaFactory.createTunnelStreamRequest()
				.classOfService(cos)
				.domainType(EmaRdm.MMT_SYSTEM)
				.name("E-CIBORG")
				//.serviceName("CONTRIBUTION_SERVICE_NAME");
			    .serviceName("DDS_TRCE");
			// Send the request and register for events from tunnel stream
			System.out.println("Send request for register tunnel stream...");
			long tunnelStreamHandle = consumer.registerClient(tsr, appClient,1);
			appClient.setOmmConsumer(consumer);
			appClient.setTunnelHandle(tunnelStreamHandle);
			System.out.println("parStreamHandle: " + tunnelStreamHandle);

			//while (!bMsgSendFinished) {
				Thread.sleep(1000000);
			//}
		} 
		catch (Exception excp)	{
			System.out.println(excp.getMessage());
		} 
	}
	public static void testCleardown2(){
		TestUtilities.printTestHead("testAckMsg_Decode", "eta encoding ema decoding");

		com.refinitiv.eta.codec.Buffer fieldListBuf = com.refinitiv.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		int retVal;
		System.out.println("Begin ETA FieldList Encoding");
		if ((retVal = TestUtilities.eta_EncodeFieldListAll(fieldListBuf, TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES )) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error encoding field list.");
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ ") encountered with TestUtilities.eta_EncodeFieldListAll.  " + "Error Text: "
					+ CodecReturnCodes.info(retVal));
			return;
		}
		com.refinitiv.eta.codec.FieldEntry fieldEntry = CodecFactory.createFieldEntry();
		boolean success = true;

		System.out.println("\tETA FieldList Header Encoded");


	}
	public static int testCleardown()
	{
		int retVal;
		/*com.refinitiv.eta.codec.Buffer buf = CodecFactory.createBuffer();
		buf.data(ByteBuffer.allocate(12300));
		com.refinitiv.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		// clear encode iterator
		encodeIter.clear();

		// set iterator buffer
		encodeIter.setBufferAndRWFVersion(buf, Codec.majorVersion(), Codec.minorVersion());

		com.refinitiv.eta.codec.FieldEntry fieldEntry = CodecFactory.createFieldEntry();
		fieldEntry.clear();

		com.refinitiv.eta.codec.DataDictionary dictionary = com.refinitiv.eta.codec.CodecFactory.createDataDictionary();
		TestUtilities.eta_encodeDictionaryMsg(dictionary);*/

		com.refinitiv.ema.access.FieldList fl = EmaFactory.createFieldList();

		TestUtilities.EmaEncodeFieldListAll(fl);
		//TestUtilities.eta_EncodeFieldListAll();
		//com.refinitiv.ema.access.PostMsg postMsg = EmaFactory.createPostMsg();


		return 0;
	}

	public static int testCleardown3(){

		com.refinitiv.eta.codec.Buffer fieldListBuf = com.refinitiv.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		int encodeOption = TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES;

		int retVal;

		int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded

		// Create and clear iterator to prepare for encoding
		com.refinitiv.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();

		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(fieldListBuf, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
					+ " Error Text: " + CodecReturnCodes.info(retVal));
			return retVal;
		}
		// create and initialize field list structure
		com.refinitiv.eta.codec.FieldList etaFieldList = CodecFactory.createFieldList();

		// populate field list structure prior to call to EncodeFieldListInit
		// NOTE: some of the fieldId, dictionaryId and fieldListNum values used here do not correspond to actual id values

		// indicate that standard data will be encoded and that dictionaryId and fieldListNum are included
		etaFieldList.flags(FieldListFlags.HAS_STANDARD_DATA /*| FieldListFlags.HAS_FIELD_LIST_INFO*/);
		// populate dictionaryId and fieldListNum with info needed to cross-reference fieldIds and cache
		//etaFieldList.dictionaryId(dictionary.infoDictionaryId());
		//etaFieldList.fieldListNum(65);

		// begin encoding of field list - assumes that encodeIter is already populated with buffer and version information
		if ((retVal = etaFieldList.encodeInit(encodeIter, null, 0)) < CodecReturnCodes.SUCCESS)
		{
			// print out message with return value string, value, and text
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal + ") encountered with EncodeFieldListInit.  "
					+ "Error Text: " + CodecReturnCodes.info(retVal));
			return retVal;
		}
		if ( (encodeOption & TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES) != 0 )
		{
			com.refinitiv.eta.codec.FieldEntry fieldEntry = CodecFactory.createFieldEntry();
			// FIFTH Field Entry: encode entry as a blank Real primitive type
			// Populate and encode field entry with fieldId and dataType information for this field.
			// Need to ensure that FieldEntry is appropriately cleared.
			// - clearing will ensure that encodedData is properly emptied

			fieldEntry.clear();
			fieldEntry.fieldId(22);
			fieldEntry.dataType(com.refinitiv.eta.codec.DataTypes.REAL);
			if ((retVal = fieldEntry.encodeBlank(encodeIter)) < CodecReturnCodes.SUCCESS)
			{
				System.out.println("ETA error " + CodecReturnCodes.toString(retVal) + "(" + retVal + ") encountered with EncodeFieldEntry.  "
						+ "Error Text: " + CodecReturnCodes.info(retVal));
				return retVal;
			}
			System.out.println("\t\tFID " + fieldEntry.fieldId() + " Encoded Real as blank.");

			com.refinitiv.eta.codec.Real real = CodecFactory.createReal();
			// SIXTH Field Entry: encode entry for a Real primitive type
			fieldEntry.fieldId(24);
			fieldEntry.dataType(com.refinitiv.eta.codec.DataTypes.REAL);
			real.value(227, RealHints.EXPONENT_2);

			if ((retVal = fieldEntry.encode(encodeIter, real)) < CodecReturnCodes.SUCCESS)
			{
				System.out.println("ETA error " + CodecReturnCodes.toString(retVal) + "(" + retVal + ") encountered with EncodeFieldEntry.  "
						+ "Error Text: " + CodecReturnCodes.info(retVal));
				return retVal;
			}
			System.out.println("\t\tFID " + fieldEntry.fieldId() + " Encoded Real: hint: " + real.hint() + " value: " + real.toLong());
		}
		return 0;

	}

}

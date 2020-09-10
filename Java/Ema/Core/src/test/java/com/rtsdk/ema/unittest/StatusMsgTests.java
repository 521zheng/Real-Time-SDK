///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.rtsdk.ema.unittest;

import java.nio.ByteBuffer;

import com.rtsdk.ema.access.*;
import com.rtsdk.ema.rdm.EmaRdm;
import com.rtsdk.ema.unittest.TestUtilities.EncodingTypeFlags;
import com.rtsdk.eta.codec.Buffer;
import com.rtsdk.eta.codec.Codec;
import com.rtsdk.eta.codec.CodecFactory;
import com.rtsdk.eta.codec.CodecReturnCodes;

import junit.framework.TestCase;

public class StatusMsgTests extends TestCase
{
	public StatusMsgTests(String name)
	{
		super(name);
	}
	
	public void testStatusMsg_Decode()
	{
		TestUtilities.printTestHead("testStatusMsg_Decode", "upa encoding ema decoding");

		com.rtsdk.eta.codec.Buffer fieldListBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		com.rtsdk.eta.codec.DataDictionary dictionary = com.rtsdk.eta.codec.CodecFactory
				.createDataDictionary();
		TestUtilities.upa_encodeDictionaryMsg(dictionary);

		int retVal;
		System.out.println("Begin UPA FieldList Encoding");
		if ((retVal = TestUtilities.upa_EncodeFieldListAll(fieldListBuf, EncodingTypeFlags.PRIMITIVE_TYPES)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error encoding field list.");
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ ") encountered with TestUtilities.upa_EncodeFieldListAll.  " + "Error Text: "
					+ CodecReturnCodes.info(retVal));
			return;
		}
		System.out.println("End UPA FieldList Encoding");
		System.out.println();

		fieldListBuf.data(fieldListBuf.data(), 0, fieldListBuf.length());

		System.out.println("Begin UPA StatusMsg Set");
		com.rtsdk.eta.codec.StatusMsg statusMsg = (com.rtsdk.eta.codec.StatusMsg) com.rtsdk.eta.codec.CodecFactory
				.createMsg();
		statusMsg.msgClass(com.rtsdk.eta.codec.MsgClasses.REFRESH);

		statusMsg.domainType(com.rtsdk.eta.rdm.DomainTypes.MARKET_PRICE);

		statusMsg.streamId(15);

		statusMsg.applyHasMsgKey();

		statusMsg.msgKey().applyHasName();
		statusMsg.msgKey().name().data("ABCDEF");

		statusMsg.msgKey().applyHasNameType();
		statusMsg.msgKey().nameType(com.rtsdk.eta.rdm.InstrumentNameTypes.RIC);

		statusMsg.msgKey().applyHasServiceId();
		statusMsg.msgKey().serviceId(5);

		statusMsg.msgKey().applyHasFilter();
		statusMsg.msgKey().filter(12);

		statusMsg.msgKey().applyHasIdentifier();
		statusMsg.msgKey().identifier(21);

		statusMsg.msgKey().applyHasAttrib();
		statusMsg.msgKey().attribContainerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.msgKey().encodedAttrib(fieldListBuf);

		statusMsg.applyHasState();
		statusMsg.state().code(com.rtsdk.eta.codec.StateCodes.NONE);
		statusMsg.state().streamState(com.rtsdk.eta.codec.StreamStates.OPEN);
		statusMsg.state().dataState(com.rtsdk.eta.codec.DataStates.OK);
		statusMsg.state().text().data("status complete");

		statusMsg.applyClearCache();

		statusMsg.applyHasPostUserInfo();
		statusMsg.postUserInfo().userAddr(15);
		statusMsg.postUserInfo().userId(30);

		statusMsg.containerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.encodedDataBody(fieldListBuf);

		System.out.println("End UPA StatusMsg Set");
		System.out.println();

		System.out.println("Begin UPA StatusMsg Buffer Encoding");

		com.rtsdk.eta.codec.Buffer msgBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		msgBuf.data(ByteBuffer.allocate(2048));

		com.rtsdk.eta.codec.EncodeIterator encIter = com.rtsdk.eta.codec.CodecFactory
				.createEncodeIterator();
		encIter.clear();
		int majorVersion = Codec.majorVersion();
		int minorVersion = Codec.minorVersion();
		if ((retVal = encIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ " encountered with setBufferAndRWFVersion. " + " Error Text: " + CodecReturnCodes.info(retVal));
			return;
		}

		statusMsg.encode(encIter);

		System.out.println("End UPA StatusMsg Buffer Encoding");
		System.out.println();

		System.out.println("Begin EMA StatusMsg Decoding");

		com.rtsdk.ema.access.StatusMsg emaStatusMsg = JUnitTestConnect.createStatusMsg();

		JUnitTestConnect.setRsslData(emaStatusMsg, msgBuf, majorVersion, minorVersion, dictionary, null);

		TestUtilities.checkResult(emaStatusMsg.domainType() == com.rtsdk.ema.rdm.EmaRdm.MMT_MARKET_PRICE,
				"StatusMsg.domainType()");

		TestUtilities.checkResult(emaStatusMsg.streamId() == 15, "StatusMsg.streamId()");

		TestUtilities.checkResult(emaStatusMsg.hasMsgKey(), "StatusMsg.hasMsgKey()");

		TestUtilities.checkResult(emaStatusMsg.hasId(), "StatusMsg.hasId()");

		TestUtilities.checkResult(emaStatusMsg.id() == 21, "StatusMsg.id()");

		TestUtilities.checkResult(emaStatusMsg.hasFilter(), "StatusMsg.hasFilter()");

		TestUtilities.checkResult(emaStatusMsg.filter() == 12, "StatusMsg.hasFilter()");

		TestUtilities.checkResult(emaStatusMsg.hasServiceId(), "StatusMsg.hasServiceId()");

		TestUtilities.checkResult(emaStatusMsg.serviceId() == 5, "StatusMsg.serviceId()");

		TestUtilities.checkResult(emaStatusMsg.hasNameType(), "StatusMsg.hasNameType()");

		TestUtilities.checkResult(emaStatusMsg.nameType() == com.rtsdk.ema.rdm.EmaRdm.INSTRUMENT_NAME_RIC,
				"StatusMsg.nameType()");

		TestUtilities.checkResult(emaStatusMsg.hasName(), "StatusMsg.hasName()");

		TestUtilities.checkResult(emaStatusMsg.name().compareTo("ABCDEF") == 0, "StatusMsg.name()");

		TestUtilities.checkResult(
				emaStatusMsg.attrib().dataType() == com.rtsdk.ema.access.DataType.DataTypes.FIELD_LIST,
				"StatusMsg.attrib().dataType()");

		TestUtilities.checkResult(emaStatusMsg.clearCache(), "StatusMsg.clearCache()");

		TestUtilities.checkResult(emaStatusMsg.hasPublisherId(), "StatusMsg.hasPublisherId()");

		TestUtilities.checkResult(emaStatusMsg.publisherIdUserAddress() == 15, "StatusMsg.publisherIdUserAddress()");

		TestUtilities.checkResult(emaStatusMsg.publisherIdUserId() == 30, "StatusMsg.publisherIdUserId()");

		TestUtilities.checkResult(
				emaStatusMsg.payload().dataType() == com.rtsdk.ema.access.DataType.DataTypes.FIELD_LIST,
				"StatusMsg.payload().dataType()");

		TestUtilities.checkResult(emaStatusMsg.hasState(), "StatusMsg.hasState()");

		TestUtilities.checkResult(emaStatusMsg.state().code() == OmmState.StatusCode.NONE, "StatusMsg.state().code()");

		TestUtilities.checkResult(emaStatusMsg.state().streamState() == OmmState.StreamState.OPEN,
				"StatusMsg.state().streamState()");

		TestUtilities.checkResult(emaStatusMsg.state().dataState() == OmmState.DataState.OK,
				"StatusMsg.state().dataState()");

		TestUtilities.checkResult(emaStatusMsg.state().statusText().compareTo("status complete") == 0,
				"StatusMsg.state().statusText()");

		System.out.println("End EMA StatusMsg Decoding");
		System.out.println();
	}

	public void testStatusMsg_toString()
	{
		TestUtilities.printTestHead("testStatusMsg_toString", "upa encoding ema toString");

		com.rtsdk.eta.codec.Buffer fieldListBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		com.rtsdk.eta.codec.DataDictionary dictionary = com.rtsdk.eta.codec.CodecFactory
				.createDataDictionary();
		TestUtilities.upa_encodeDictionaryMsg(dictionary);

		int retVal;
		System.out.println("Begin UPA FieldList Encoding");
		if ((retVal = TestUtilities.upa_EncodeFieldListAll(fieldListBuf, EncodingTypeFlags.PRIMITIVE_TYPES)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error encoding field list.");
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ ") encountered with TestUtilities.upa_EncodeFieldListAll.  " + "Error Text: "
					+ CodecReturnCodes.info(retVal));
			return;
		}
		System.out.println("End UPA FieldList Encoding");
		System.out.println();

		fieldListBuf.data(fieldListBuf.data(), 0, fieldListBuf.length());

		System.out.println("Begin UPA StatusMsg Set");
		com.rtsdk.eta.codec.StatusMsg statusMsg = (com.rtsdk.eta.codec.StatusMsg) com.rtsdk.eta.codec.CodecFactory
				.createMsg();
		statusMsg.msgClass(com.rtsdk.eta.codec.MsgClasses.REFRESH);

		statusMsg.domainType(com.rtsdk.eta.rdm.DomainTypes.MARKET_PRICE);

		statusMsg.streamId(15);

		statusMsg.applyHasMsgKey();

		statusMsg.msgKey().applyHasName();
		statusMsg.msgKey().name().data("ABCDEF");

		statusMsg.msgKey().applyHasNameType();
		statusMsg.msgKey().nameType(com.rtsdk.eta.rdm.InstrumentNameTypes.RIC);

		statusMsg.msgKey().applyHasServiceId();
		statusMsg.msgKey().serviceId(5);

		statusMsg.msgKey().applyHasFilter();
		statusMsg.msgKey().filter(12);

		statusMsg.msgKey().applyHasIdentifier();
		statusMsg.msgKey().identifier(21);

		statusMsg.msgKey().applyHasAttrib();
		statusMsg.msgKey().attribContainerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.msgKey().encodedAttrib(fieldListBuf);

		statusMsg.applyHasState();
		statusMsg.state().code(com.rtsdk.eta.codec.StateCodes.NONE);
		statusMsg.state().streamState(com.rtsdk.eta.codec.StreamStates.OPEN);
		statusMsg.state().dataState(com.rtsdk.eta.codec.DataStates.OK);
		statusMsg.state().text().data("status complete");

		statusMsg.applyClearCache();

		statusMsg.applyHasPostUserInfo();
		statusMsg.postUserInfo().userAddr(15);
		statusMsg.postUserInfo().userId(30);

		statusMsg.containerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.encodedDataBody(fieldListBuf);

		System.out.println("End UPA StatusMsg Set");
		System.out.println();

		System.out.println("Begin UPA StatusMsg Buffer Encoding");

		com.rtsdk.eta.codec.Buffer msgBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		msgBuf.data(ByteBuffer.allocate(2048));

		com.rtsdk.eta.codec.EncodeIterator encIter = com.rtsdk.eta.codec.CodecFactory
				.createEncodeIterator();
		encIter.clear();
		int majorVersion = Codec.majorVersion();
		int minorVersion = Codec.minorVersion();
		if ((retVal = encIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ " encountered with setBufferAndRWFVersion. " + " Error Text: " + CodecReturnCodes.info(retVal));
			return;
		}

		statusMsg.encode(encIter);

		System.out.println("End UPA StatusMsg Buffer Encoding");
		System.out.println();

		System.out.println("Begin EMA StatusMsg toString");

		com.rtsdk.ema.access.StatusMsg emaStatusMsg = JUnitTestConnect.createStatusMsg();

		JUnitTestConnect.setRsslData(emaStatusMsg, msgBuf, majorVersion, minorVersion, dictionary, null);

		System.out.println(emaStatusMsg);

		System.out.println("End EMA StatusMsg toString");
		System.out.println();
	}

	public void testStatusMsg_EncodeDecode()
	{
		TestUtilities.printTestHead("testStatusMsg_EncodeDecode", "ema encoding ema decoding");

		com.rtsdk.eta.codec.DataDictionary dictionary = com.rtsdk.eta.codec.CodecFactory
				.createDataDictionary();
		TestUtilities.upa_encodeDictionaryMsg(dictionary);

		com.rtsdk.ema.access.FieldList fl = EmaFactory.createFieldList();

		TestUtilities.EmaEncodeFieldListAll(fl);

		com.rtsdk.ema.access.StatusMsg statusMsg = EmaFactory.createStatusMsg();

		System.out.println("Begin EMA StatusMsg test after constructor");

		TestUtilities.checkResult(statusMsg.domainType() == com.rtsdk.ema.rdm.EmaRdm.MMT_MARKET_PRICE,
				"StatusMsg.domainType()");

		TestUtilities.checkResult(statusMsg.streamId() == 0, "StatusMsg.streamId()");

		TestUtilities.checkResult(!statusMsg.hasMsgKey(), "StatusMsg.hasMsgKey()");

		TestUtilities.checkResult(!statusMsg.hasId(), "StatusMsg.hasId()");

		TestUtilities.checkResult(!statusMsg.hasFilter(), "StatusMsg.hasFilter()");

		TestUtilities.checkResult(!statusMsg.hasServiceId(), "StatusMsg.hasServiceId()");

		TestUtilities.checkResult(!statusMsg.hasNameType(), "StatusMsg.hasNameType()");

		TestUtilities.checkResult(!statusMsg.hasName(), "StatusMsg.hasName()");

		TestUtilities.checkResult(
				statusMsg.attrib().dataType() == com.rtsdk.ema.access.DataType.DataTypes.NO_DATA,
				"StatusMsg.attrib().dataType()");

		TestUtilities.checkResult(!statusMsg.clearCache(), "StatusMsg.clearCache()");

		TestUtilities.checkResult(!statusMsg.hasPublisherId(), "StatusMsg.hasPublisherId()");

		TestUtilities.checkResult(
				statusMsg.payload().dataType() == com.rtsdk.ema.access.DataType.DataTypes.NO_DATA,
				"StatusMsg.payload().dataType()");

		TestUtilities.checkResult(!statusMsg.hasState(), "StatusMsg.hasState()");

		System.out.println("Begin EMA StatusMsg test after constructor");
		System.out.println();

		System.out.println("End EMA StatusMsg Set");

		statusMsg.domainType(com.rtsdk.ema.rdm.EmaRdm.MMT_MARKET_PRICE);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.streamId(15);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.name("ABCDEF");
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.nameType(com.rtsdk.eta.rdm.InstrumentNameTypes.RIC);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.serviceId(5);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.filter(12);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.id(21);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.attrib(fl);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "Status Complete");
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.clearCache(true);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.publisherId(30, 15);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		statusMsg.payload(fl);
		TestUtilities.checkResult("StatusMsg.toString() == toString() not supported", statusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n"));	    

		System.out.println("End EMA StatusMsg Set");
		System.out.println();

		System.out.println("Begin EMA StatusMsg Decoding");

		com.rtsdk.ema.access.StatusMsg emaStatusMsg = JUnitTestConnect.createStatusMsg();

		JUnitTestConnect.setRsslData(emaStatusMsg, statusMsg, 14, 0, dictionary, null);

		// check that we can still get the toString on encoded/decoded msg.
		TestUtilities.checkResult("StatusMsg.toString() != toString() not supported", !(emaStatusMsg.toString().equals("\nDecoding of just encoded object in the same application is not supported\n")));	 		

		
		
		TestUtilities.checkResult(emaStatusMsg.domainType() == com.rtsdk.ema.rdm.EmaRdm.MMT_MARKET_PRICE,
				"StatusMsg.domainType()");

		TestUtilities.checkResult(emaStatusMsg.streamId() == 15, "StatusMsg.streamId()");

		TestUtilities.checkResult(emaStatusMsg.hasMsgKey(), "StatusMsg.hasMsgKey()");

		TestUtilities.checkResult(emaStatusMsg.hasId(), "StatusMsg.hasId()");

		TestUtilities.checkResult(emaStatusMsg.id() == 21, "StatusMsg.id()");

		TestUtilities.checkResult(emaStatusMsg.hasFilter(), "StatusMsg.hasFilter()");

		TestUtilities.checkResult(emaStatusMsg.filter() == 12, "StatusMsg.hasFilter()");

		TestUtilities.checkResult(emaStatusMsg.hasServiceId(), "StatusMsg.hasServiceId()");

		TestUtilities.checkResult(emaStatusMsg.serviceId() == 5, "StatusMsg.serviceId()");

		TestUtilities.checkResult(emaStatusMsg.hasNameType(), "StatusMsg.hasNameType()");

		TestUtilities.checkResult(emaStatusMsg.nameType() == com.rtsdk.ema.rdm.EmaRdm.INSTRUMENT_NAME_RIC,
				"StatusMsg.nameType()");

		TestUtilities.checkResult(emaStatusMsg.hasName(), "StatusMsg.hasName()");

		TestUtilities.checkResult(emaStatusMsg.name().compareTo("ABCDEF") == 0, "StatusMsg.name()");

		TestUtilities.checkResult(
				emaStatusMsg.attrib().dataType() == com.rtsdk.ema.access.DataType.DataTypes.FIELD_LIST,
				"StatusMsg.attrib().dataType()");

		TestUtilities.checkResult(emaStatusMsg.clearCache(), "StatusMsg.clearCache()");

		TestUtilities.checkResult(emaStatusMsg.hasPublisherId(), "StatusMsg.hasPublisherId()");

		TestUtilities.checkResult(emaStatusMsg.publisherIdUserAddress() == 15, "StatusMsg.publisherIdUserAddress()");

		TestUtilities.checkResult(emaStatusMsg.publisherIdUserId() == 30, "StatusMsg.publisherIdUserId()");

		TestUtilities.checkResult(
				emaStatusMsg.payload().dataType() == com.rtsdk.ema.access.DataType.DataTypes.FIELD_LIST,
				"StatusMsg.payload().dataType()");

		TestUtilities.checkResult(statusMsg.hasState(), "StatusMsg.hasState()");

		TestUtilities.checkResult(emaStatusMsg.state().code() == OmmState.StatusCode.NONE, "StatusMsg.state().code()");

		TestUtilities.checkResult(emaStatusMsg.state().streamState() == OmmState.StreamState.OPEN,
				"StatusMsg.state().streamState()");

		TestUtilities.checkResult(emaStatusMsg.state().dataState() == OmmState.DataState.OK,
				"StatusMsg.state().dataState()");

		TestUtilities.checkResult(emaStatusMsg.state().statusText().compareTo("Status Complete") == 0,
				"StatusMsg.state().statusText()");

		System.out.println("End EMA StatusMsg Decoding");
		System.out.println();

		statusMsg.clear();

		System.out.println("Begin EMA StatusMsg test after clear");

		TestUtilities.checkResult(statusMsg.domainType() == com.rtsdk.ema.rdm.EmaRdm.MMT_MARKET_PRICE,
				"StatusMsg.domainType()");

		TestUtilities.checkResult(statusMsg.streamId() == 0, "StatusMsg.streamId()");

		TestUtilities.checkResult(!statusMsg.hasMsgKey(), "StatusMsg.hasMsgKey()");

		TestUtilities.checkResult(!statusMsg.hasId(), "StatusMsg.hasId()");

		TestUtilities.checkResult(!statusMsg.hasFilter(), "StatusMsg.hasFilter()");

		TestUtilities.checkResult(!statusMsg.hasServiceId(), "StatusMsg.hasServiceId()");

		TestUtilities.checkResult(!statusMsg.hasNameType(), "StatusMsg.hasNameType()");

		TestUtilities.checkResult(!statusMsg.hasName(), "StatusMsg.hasName()");

		TestUtilities.checkResult(
				statusMsg.attrib().dataType() == com.rtsdk.ema.access.DataType.DataTypes.NO_DATA,
				"StatusMsg.attrib().dataType()");

		TestUtilities.checkResult(!statusMsg.clearCache(), "StatusMsg.clearCache()");

		TestUtilities.checkResult(!statusMsg.hasPublisherId(), "StatusMsg.hasPublisherId()");

		TestUtilities.checkResult(
				statusMsg.payload().dataType() == com.rtsdk.ema.access.DataType.DataTypes.NO_DATA,
				"StatusMsg.payload().dataType()");

		TestUtilities.checkResult(!statusMsg.hasState(), "StatusMsg.hasState()");

		System.out.println("End EMA StatusMsg test after clear");
		System.out.println();
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithFieldListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithFieldListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherUpdateMsg_EMADecode", "Encode StatusMsg with UPA for FieldList as attrib, extended header and payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(8192));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.FIELD_LIST)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPAFieldListAll(decCopyStatusMsg.attrib().fieldList(), TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES);
	     TestUtilities.EmaDecode_UPAFieldListAll(decCopyStatusMsg.payload().fieldList(), TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithFieldListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherUpdateMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithElementListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithElementListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for ElementList as attrib, extended header and payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(8192));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.ELEMENT_LIST)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.ELEMENT_LIST);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPAElementListAll(decCopyStatusMsg.attrib().elementList(), TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES);
	     TestUtilities.EmaDecode_UPAElementListAll(decCopyStatusMsg.payload().elementList(), TestUtilities.EncodingTypeFlags.PRIMITIVE_TYPES);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithElementListTypeAsAttrib_ExtendedHeader_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithFilterListTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithFilterListTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for FilterList as attrib, payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(8192));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.FILTER_LIST)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	    TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.FILTER_LIST);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPAFilterListAll(decCopyStatusMsg.attrib().filterList(), TestUtilities.EncodingTypeFlags.MESSAGE_TYPES);
	     TestUtilities.EmaDecode_UPAFilterListAll(decCopyStatusMsg.payload().filterList(), TestUtilities.EncodingTypeFlags.MESSAGE_TYPES);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithFilterListTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithSeriesTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithSeriesTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for Series as attrib, payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(14000));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.SERIES)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	    TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.SERIES);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPASeriesAll(decCopyStatusMsg.attrib().series(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     TestUtilities.EmaDecode_UPASeriesAll(decCopyStatusMsg.payload().series(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithSeriesTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithVectorTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithVectorTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for Vector as attrib, payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(14000));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.VECTOR)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	    TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.VECTOR);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPAVectorAll(decCopyStatusMsg.attrib().vector(), com.rtsdk.eta.codec.DataTypes.ELEMENT_LIST);
	     TestUtilities.EmaDecode_UPAVectorAll(decCopyStatusMsg.payload().vector(), com.rtsdk.eta.codec.DataTypes.ELEMENT_LIST);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithVectorTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPARefreshWithMapTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPARefreshWithMapTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for Map as attrib, payload, Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(8192));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.MAP)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	    TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.MAP);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPAMapKeyUIntAll(decCopyStatusMsg.attrib().map(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     TestUtilities.EmaDecode_UPAMapKeyUIntAll(decCopyStatusMsg.payload().map(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPARefreshWithMapTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}
	
	public void testStatusMsg_EncodeUPAStatusMsgWithRefreshTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode()
	{ 
	    int retVal;
		 
		TestUtilities.printTestHead("testStatusMsg_EncodeUPAStatusMsgWithRefreshTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode", "Encode StatusMsg with UPA for RefreshMsg as attrib, payload. Encode it to another StatusMsg.");

        // Create a UPA Buffer to encode into
        com.rtsdk.eta.codec.Buffer buffer = com.rtsdk.eta.codec.CodecFactory.createBuffer();
        buffer.data(ByteBuffer.allocate(8192));
        
    	int majorVersion = Codec.majorVersion();  // This should be initialized to the MAJOR version of RWF being encoded
		int minorVersion = Codec.minorVersion();  // This should be initialized to the MINOR version of RWF being encoded
	
		// Create and clear iterator to prepare for encoding
		com.rtsdk.eta.codec.EncodeIterator encodeIter = CodecFactory.createEncodeIterator();
		encodeIter.clear();
	
		// Associate buffer and iterator and set proper protocol version information on iterator.
		if ((retVal = encodeIter.setBufferAndRWFVersion(buffer, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with setBufferAndRWFVersion. "
							+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		if ( ( retVal = TestUtilities.upa_EncodeStatusMsgAll(encodeIter, com.rtsdk.eta.codec.DataTypes.MSG)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" +retVal + " encountered with TestUtilities.upa_EncodeStatusMsgAll(). "
					+ " Error Text: " + CodecReturnCodes.info(retVal)); 
			return;
		}
		
		 // Decode StatusMsg with EMA.
	     com.rtsdk.ema.access.StatusMsg statusMsg = JUnitTestConnect.createStatusMsg();
	     JUnitTestConnect.setRsslData(statusMsg, buffer, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	    TestUtilities.EmaDecode_UPAStatusMsgAll(statusMsg, com.rtsdk.eta.codec.DataTypes.MSG);
	
	     com.rtsdk.ema.access.StatusMsg copyStatusMsg = EmaFactory.createStatusMsg();
	     
	     // Encode to another StatusMsg
	     copyStatusMsg.extendedHeader(statusMsg.extendedHeader());
	     copyStatusMsg.permissionData(statusMsg.permissionData());
	     copyStatusMsg.attrib(statusMsg.attrib().data());
	     copyStatusMsg.payload(statusMsg.payload().data());
	     
	     com.rtsdk.ema.access.StatusMsg decCopyStatusMsg = JUnitTestConnect.createStatusMsg();
	     
	     JUnitTestConnect.setRsslData(decCopyStatusMsg, copyStatusMsg, Codec.majorVersion(), Codec.minorVersion(), TestUtilities.getDataDictionary(), null);
	     
	     // Check result
	     TestUtilities.checkResult(decCopyStatusMsg.extendedHeader().equals(statusMsg.extendedHeader()));
	     TestUtilities.checkResult(decCopyStatusMsg.permissionData().equals(statusMsg.permissionData()));
	     TestUtilities.EmaDecode_UPARefreshMsgAll(decCopyStatusMsg.attrib().refreshMsg(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     TestUtilities.EmaDecode_UPARefreshMsgAll(decCopyStatusMsg.payload().refreshMsg(), com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
	     
	     System.out.println("\ttestStatusMsg_EncodeUPAStatusMsgWithRefreshTypeAsAttrib_Payload_EncodeEMA_ToAnotherStatusMsg_EMADecode passed");
	}

	public void testStatusMsg_cloneForEncodeSide()
	{
		TestUtilities.printTestHead("testStatusMsg_cloneIsNotSupportedFromTheEncodeSide", "cloning is not supported on encode side");
		StatusMsg msg = EmaFactory.createStatusMsg()
				.domainType(EmaRdm.MMT_MARKET_PRICE)
				.name("Some status")
				.state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "Login accepted")
				;

		try {
			StatusMsg cloneMessage = EmaFactory.createStatusMsg(msg);
			TestUtilities.checkResult(true, "Clone not supported  - exception IS NOT expected: ");
			compareEmaStatusMsgFields(msg, cloneMessage, "Status message with no payload clone");
		} catch ( OmmException excp ) {
			TestUtilities.checkResult(false, "Clone not supported  - exception IS NOT expected: " +  excp.getMessage() );
		}
	}

	public void testStatusMsg_cloneMsgKeyWLScenario()
	{
		TestUtilities.printTestHead("testStatusMsg_cloneMsgKeyWLScenario", "cloning for minimal ema status message");
		StatusMsg emaMsg = EmaFactory.createStatusMsg();
		emaMsg.payload(TestMsgUtilities.createFiledListBodySample());

		JUnitTestConnect.getRsslData(emaMsg);
		/** @see com.rtsdk.eta.valueadd.reactor.WlItemHandler#callbackUser(String, com.rtsdk.eta.codec.Msg, MsgBase, WlRequest, ReactorErrorInfo) */
		JUnitTestConnect.setRsslMsgKeyFlag(emaMsg, true);
		StatusMsg emaClonedMsg = EmaFactory.createStatusMsg(emaMsg);

		compareEmaStatusMsgFields(emaMsg, emaClonedMsg, "check clone for minimal message");
		JUnitTestConnect.setRsslMsgKeyFlag(emaMsg, false);

		System.out.println("End EMA StatusMsg Clone msgKey");
		System.out.println();
	}

	public void testStatusMsg_clone()
	{
		TestUtilities.printTestHead("testStatusMsg_clone", "cloning for ema status message");

		com.rtsdk.eta.codec.Buffer fieldListBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		com.rtsdk.eta.codec.DataDictionary dictionary = com.rtsdk.eta.codec.CodecFactory
				.createDataDictionary();
		TestUtilities.upa_encodeDictionaryMsg(dictionary);

		int retVal;
		System.out.println("Begin UPA FieldList Encoding");
		if ((retVal = TestUtilities.upa_EncodeFieldListAll(fieldListBuf, EncodingTypeFlags.PRIMITIVE_TYPES)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error encoding field list.");
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ ") encountered with TestUtilities.upa_EncodeFieldListAll.  " + "Error Text: "
					+ CodecReturnCodes.info(retVal));
			return;
		}
		System.out.println("End UPA FieldList Encoding");
		System.out.println();

		fieldListBuf.data(fieldListBuf.data(), 0, fieldListBuf.length());

		System.out.println("Begin UPA StatusMsg Set");
		com.rtsdk.eta.codec.StatusMsg statusMsg = (com.rtsdk.eta.codec.StatusMsg) com.rtsdk.eta.codec.CodecFactory
				.createMsg();
		statusMsg.msgClass(com.rtsdk.eta.codec.MsgClasses.REFRESH);

		statusMsg.domainType(com.rtsdk.eta.rdm.DomainTypes.MARKET_PRICE);

		statusMsg.streamId(15);

		statusMsg.applyHasMsgKey();

		statusMsg.msgKey().applyHasName();
		statusMsg.msgKey().name().data("ABCDEF");

		statusMsg.msgKey().applyHasNameType();
		statusMsg.msgKey().nameType(com.rtsdk.eta.rdm.InstrumentNameTypes.RIC);

		statusMsg.msgKey().applyHasServiceId();
		statusMsg.msgKey().serviceId(5);

		statusMsg.msgKey().applyHasFilter();
		statusMsg.msgKey().filter(12);

		statusMsg.msgKey().applyHasIdentifier();
		statusMsg.msgKey().identifier(21);

		statusMsg.msgKey().applyHasAttrib();
		statusMsg.msgKey().attribContainerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.msgKey().encodedAttrib(fieldListBuf);

		statusMsg.applyHasState();
		statusMsg.state().code(com.rtsdk.eta.codec.StateCodes.NONE);
		statusMsg.state().streamState(com.rtsdk.eta.codec.StreamStates.OPEN);
		statusMsg.state().dataState(com.rtsdk.eta.codec.DataStates.OK);
		statusMsg.state().text().data("status complete");

		statusMsg.applyClearCache();

		statusMsg.applyHasPostUserInfo();
		statusMsg.postUserInfo().userAddr(15);
		statusMsg.postUserInfo().userId(30);

		statusMsg.containerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.encodedDataBody(fieldListBuf);

		setMoreFields(statusMsg);

		System.out.println("End UPA StatusMsg Set");
		System.out.println();

		System.out.println("Begin UPA StatusMsg Buffer Encoding");

		com.rtsdk.eta.codec.Buffer msgBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		msgBuf.data(ByteBuffer.allocate(2048));

		com.rtsdk.eta.codec.EncodeIterator encIter = com.rtsdk.eta.codec.CodecFactory
				.createEncodeIterator();
		encIter.clear();
		int majorVersion = Codec.majorVersion();
		int minorVersion = Codec.minorVersion();
		if ((retVal = encIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ " encountered with setBufferAndRWFVersion. " + " Error Text: " + CodecReturnCodes.info(retVal));
			return;
		}

		statusMsg.encode(encIter);

		System.out.println("End UPA StatusMsg Buffer Encoding");
		System.out.println();

		System.out.println("Begin EMA StatusMsg clone");

		com.rtsdk.eta.codec.StatusMsg statusMsgDecode = (com.rtsdk.eta.codec.StatusMsg)com.rtsdk.eta.codec.CodecFactory.createMsg();

		com.rtsdk.eta.codec.DecodeIterator decIter = com.rtsdk.eta.codec.CodecFactory.createDecodeIterator();
		decIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion);
		statusMsgDecode.decode(decIter);

		com.rtsdk.ema.access.StatusMsg emaStatusMsg = JUnitTestConnect.createStatusMsg();
				
		JUnitTestConnect.setRsslData(emaStatusMsg, statusMsgDecode, majorVersion, minorVersion, dictionary, null);
		
		com.rtsdk.ema.access.StatusMsg emaStatusMsgClone = EmaFactory.createStatusMsg(emaStatusMsg);
		
		compareEmaStatusMsgFields(emaStatusMsg, emaStatusMsgClone, "Status cloned message");
		String emaStatusMsgString = emaStatusMsg.toString();
		String emaStatusMsgCloneString = emaStatusMsgClone.toString();
		
		System.out.println("Cloned EMA StatusMsg:");
		System.out.println(emaStatusMsgClone);
		
		TestUtilities.checkResult(emaStatusMsgString.equals(emaStatusMsgCloneString), "emaStatusMsgString.equals(emaStatusMsgCloneString)");

		com.rtsdk.ema.access.StatusMsg emaStatusMsgClone2 = EmaFactory.createStatusMsg(emaStatusMsgClone);
		compareEmaStatusMsgFields(emaStatusMsg, emaStatusMsgClone2, "Status double-cloned message");
		String emaStatusMsgClone2String = emaStatusMsgClone2.toString();
		TestUtilities.checkResult(emaStatusMsgString.equals(emaStatusMsgClone2String), "double-cloned emaStatusMsgString.equals(emaStatusMsgClone2String)");

		System.out.println("End EMA StatusMsg clone");
		System.out.println();
	}

	private void setMoreFields(com.rtsdk.eta.codec.StatusMsg statusMsg) {
		statusMsg.applyHasExtendedHdr();
		Buffer extendedHeader = CodecFactory.createBuffer();
		extendedHeader.data(ByteBuffer.wrap(new byte[] {5, -6, 7, -8}));
		statusMsg.extendedHeader(extendedHeader);

		Buffer itemGroup = CodecFactory.createBuffer();
		itemGroup.data(ByteBuffer.wrap(new byte[] {30, 40, 50, 60, 77, 77, 77, 77, 88}));
		statusMsg.groupId(itemGroup);

		statusMsg.applyHasPermData();
		Buffer permissionData = CodecFactory.createBuffer();
		permissionData.data(ByteBuffer.wrap(new byte[]{50, 51, 52, 53}));
		statusMsg.permData(permissionData);

		statusMsg.applyPrivateStream();
	}

	public void testStatusMsg_cloneEdit()
	{
		TestUtilities.printTestHead("testStatusMsg_clone", "cloning for ema status message");

		com.rtsdk.eta.codec.Buffer fieldListBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		fieldListBuf.data(ByteBuffer.allocate(1024));

		com.rtsdk.eta.codec.DataDictionary dictionary = com.rtsdk.eta.codec.CodecFactory
				.createDataDictionary();
		TestUtilities.upa_encodeDictionaryMsg(dictionary);

		int retVal;
		System.out.println("Begin UPA FieldList Encoding");
		if ((retVal = TestUtilities.upa_EncodeFieldListAll(fieldListBuf, EncodingTypeFlags.PRIMITIVE_TYPES)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error encoding field list.");
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ ") encountered with TestUtilities.upa_EncodeFieldListAll.  " + "Error Text: "
					+ CodecReturnCodes.info(retVal));
			return;
		}
		System.out.println("End UPA FieldList Encoding");
		System.out.println();

		fieldListBuf.data(fieldListBuf.data(), 0, fieldListBuf.length());

		System.out.println("Begin UPA StatusMsg Set");
		com.rtsdk.eta.codec.StatusMsg statusMsg = (com.rtsdk.eta.codec.StatusMsg) com.rtsdk.eta.codec.CodecFactory
				.createMsg();
		statusMsg.msgClass(com.rtsdk.eta.codec.MsgClasses.REFRESH);

		statusMsg.domainType(com.rtsdk.eta.rdm.DomainTypes.MARKET_PRICE);

		statusMsg.streamId(15);

		statusMsg.applyHasMsgKey();

		statusMsg.msgKey().applyHasName();
		statusMsg.msgKey().name().data("ABCDEF");

		statusMsg.msgKey().applyHasNameType();
		statusMsg.msgKey().nameType(com.rtsdk.eta.rdm.InstrumentNameTypes.RIC);

		statusMsg.msgKey().applyHasServiceId();
		statusMsg.msgKey().serviceId(5);

		statusMsg.msgKey().applyHasFilter();
		statusMsg.msgKey().filter(12);

		statusMsg.msgKey().applyHasIdentifier();
		statusMsg.msgKey().identifier(21);

		statusMsg.msgKey().applyHasAttrib();
		statusMsg.msgKey().attribContainerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.msgKey().encodedAttrib(fieldListBuf);

		statusMsg.applyHasState();
		statusMsg.state().code(com.rtsdk.eta.codec.StateCodes.NONE);
		statusMsg.state().streamState(com.rtsdk.eta.codec.StreamStates.OPEN);
		statusMsg.state().dataState(com.rtsdk.eta.codec.DataStates.OK);
		statusMsg.state().text().data("status complete");

		statusMsg.applyClearCache();

		statusMsg.applyHasPostUserInfo();
		statusMsg.postUserInfo().userAddr(15);
		statusMsg.postUserInfo().userId(30);

		statusMsg.containerType(com.rtsdk.eta.codec.DataTypes.FIELD_LIST);
		statusMsg.encodedDataBody(fieldListBuf);

		System.out.println("End UPA StatusMsg Set");
		System.out.println();

		System.out.println("Begin UPA StatusMsg Buffer Encoding");

		com.rtsdk.eta.codec.Buffer msgBuf = com.rtsdk.eta.codec.CodecFactory.createBuffer();
		msgBuf.data(ByteBuffer.allocate(2048));

		com.rtsdk.eta.codec.EncodeIterator encIter = com.rtsdk.eta.codec.CodecFactory
				.createEncodeIterator();
		encIter.clear();
		int majorVersion = Codec.majorVersion();
		int minorVersion = Codec.minorVersion();
		if ((retVal = encIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion)) < CodecReturnCodes.SUCCESS)
		{
			System.out.println("Error " + CodecReturnCodes.toString(retVal) + "(" + retVal
					+ " encountered with setBufferAndRWFVersion. " + " Error Text: " + CodecReturnCodes.info(retVal));
			return;
		}

		statusMsg.encode(encIter);

		System.out.println("End UPA StatusMsg Buffer Encoding");
		System.out.println();

		System.out.println("Begin EMA StatusMsg clone");

		com.rtsdk.eta.codec.StatusMsg statusMsgDecode = (com.rtsdk.eta.codec.StatusMsg)com.rtsdk.eta.codec.CodecFactory.createMsg();

		com.rtsdk.eta.codec.DecodeIterator decIter = com.rtsdk.eta.codec.CodecFactory.createDecodeIterator();
		decIter.setBufferAndRWFVersion(msgBuf, majorVersion, minorVersion);
		statusMsgDecode.decode(decIter);

		com.rtsdk.ema.access.StatusMsg emaStatusMsg = JUnitTestConnect.createStatusMsg();

		JUnitTestConnect.setRsslData(emaStatusMsg, statusMsgDecode, majorVersion, minorVersion, dictionary, null);
		
		com.rtsdk.ema.access.StatusMsg emaStatusMsgClone = EmaFactory.createStatusMsg(emaStatusMsg);

		compareEmaStatusMsgFields(emaStatusMsg, emaStatusMsgClone, "Status cloned message");

		String emaStatusMsgString = emaStatusMsg.toString();
		String emaStatusMsgCloneString = emaStatusMsgClone.toString();
		
		System.out.println("Cloned EMA StatusMsg:");
		System.out.println(emaStatusMsgClone);
		
		TestUtilities.checkResult(emaStatusMsgString.equals(emaStatusMsgCloneString), "emaStatusMsgString.equals(emaStatusMsgCloneString)");
		
		System.out.println("End EMA StatusMsg clone");
		System.out.println();
	}

	private void compareEmaStatusMsgFields(StatusMsg expected, StatusMsg actual, String checkPrefix) {
		TestMsgUtilities.compareMsgFields(expected, actual, checkPrefix + " base message");
		checkPrefix = checkPrefix + " compare: ";

		TestUtilities.checkResult(expected.hasServiceName() == actual.hasServiceName(), checkPrefix + "hasServiceName");
		if(expected.hasServiceName())
			TestUtilities.checkResult(expected.serviceName().equals(actual.serviceName()), checkPrefix + "serviceId" + "exp=" +actual.serviceName() + " act="+expected.serviceName());

		TestUtilities.checkResult(expected.hasState() == actual.hasState(), checkPrefix + "hasState");
		if(expected.hasState()) {
			TestUtilities.checkResult(expected.state().code() == actual.state().code(), checkPrefix + "state.code");
			TestUtilities.checkResult(expected.state().statusCode() == actual.state().statusCode(), checkPrefix + "state.statusCode");
			TestUtilities.checkResult(expected.state().streamState() == actual.state().streamState(), checkPrefix + "state.streamState()");
			TestUtilities.checkResult(expected.state().statusText().equals(actual.state().statusText()), checkPrefix + "state.statusText");
		}

		TestUtilities.checkResult(expected.hasPublisherId() == actual.hasPublisherId(), checkPrefix + "hasPublisherId");
		if(expected.hasPublisherId()) {
			TestUtilities.checkResult(expected.publisherIdUserId() == actual.publisherIdUserId(), checkPrefix + "publisherIdUserId");
			TestUtilities.checkResult(expected.publisherIdUserAddress() == actual.publisherIdUserAddress(), checkPrefix + "publisherIdUserAddress");
		}

		TestUtilities.checkResult(expected.clearCache() == actual.clearCache(), checkPrefix + "clearCache");
		TestUtilities.checkResult(expected.privateStream() == actual.privateStream(), checkPrefix + "privateStream");

		TestUtilities.checkResult(expected.hasItemGroup() == actual.hasItemGroup(), checkPrefix + "hasItemGroup");
		if(expected.hasItemGroup())
			TestUtilities.checkResult(expected.itemGroup().equals(actual.itemGroup()), checkPrefix + "itemGroup");

		TestUtilities.checkResult(expected.hasPermissionData() == actual.hasPermissionData(), checkPrefix + "hasPermissionData");
		if(expected.hasPermissionData())
			TestUtilities.checkResult(expected.permissionData().equals(actual.permissionData()), checkPrefix + "permissionData");
	}
}

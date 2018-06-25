///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.nio.ByteBuffer;

import com.thomsonreuters.ema.access.Data;
import com.thomsonreuters.ema.access.ConfigManager.ConfigAttributes;
import com.thomsonreuters.ema.access.ConfigManager.ConfigElement;
import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.transport.ConnectionTypes;
import com.thomsonreuters.ema.access.OmmConsumer;

//This class is created as a connect bridge between JUNIT test and EMA external/internal interface/classes.

public class JUnitTestConnect
{
	public static final int ConfigGroupTypeConsumer = 1;
	public static final int ConfigGroupTypeDictionary = 2;
	public static final int ConfigGroupTypeChannel = 3;
	
	// Consumer Parameters:
	public static final int ConsumerChannelSet  = ConfigManager.ChannelSet; 
	public static final int ConsumerDictionaryRequestTimeOut  = ConfigManager.DictionaryRequestTimeOut; 
	public static final int ConsumerDispatchTimeoutApiThread  = ConfigManager.DispatchTimeoutApiThread; 
	public static final int ConsumerItemCountHint  = ConfigManager.ItemCountHint ; 
	public static final int ConsumerMaxDispatchCountApiThread  = ConfigManager.MaxDispatchCountApiThread; 
	public static final int ConsumerMaxDispatchCountUserThread  = ConfigManager.MaxDispatchCountUserThread; 
	public static final int ConsumerMaxOutstandingPosts  = ConfigManager.MaxOutstandingPosts ; 
	public static final int ConsumerObeyOpenWindow  = ConfigManager.ObeyOpenWindow; 
	public static final int ConsumerPostAckTimeout  = ConfigManager.PostAckTimeout ; 
	public static final int ConsumerRequestTimeout  = ConfigManager.RequestTimeout; 
	public static final int ConsumerServiceCountHint  = ConfigManager.ServiceCountHint; 
	public static final int ConsumerMsgKeyInUpdates  = ConfigManager.MsgKeyInUpdates; 
	public static final int ConsumerReconnectAttemptLimit  = ConfigManager.ReconnectAttemptLimit; 
	public static final int ConsumerReconnectMaxDelay  = ConfigManager.ReconnectMaxDelay; 
	public static final int ConsumerReconnectMinDelay  = ConfigManager.ReconnectMinDelay; 
	public static final int ConsumerXmlTraceFileName  = ConfigManager.XmlTraceFileName; 
	public static final int ConsumerXmlTraceHex  = ConfigManager.XmlTraceHex; 
	public static final int ConsumerXmlTraceMaxFileSize  = ConfigManager.XmlTraceMaxFileSize; 
	public static final int ConsumerXmlTracePing  = ConfigManager.XmlTracePing; 
	public static final int ConsumerXmlTraceRead  = ConfigManager.XmlTraceRead; 
	public static final int ConsumerXmlTraceToFile  = ConfigManager.XmlTraceToFile; 
	public static final int ConsumerXmlTraceToMultipleFiles  = ConfigManager.XmlTraceToMultipleFiles; 
	public static final int ConsumerXmlTraceToStdout  = ConfigManager.XmlTraceToStdout; 
	public static final int ConsumerXmlTraceWrite  = ConfigManager.XmlTraceWrite; 	
	
	// Dictionary Parameters:
	public static final int DictionaryType  = ConfigManager.DictionaryType;
	public static final int DictionaryEnumTypeDefFileName  = ConfigManager.DictionaryEnumTypeDefFileName;
	public static final int DictionaryRDMFieldDictFileName  = ConfigManager.DictionaryRDMFieldDictFileName; 


	// Channel Parameters:
	public static final int ChannelConnectionPingTimeout  = ConfigManager.ConnectionPingTimeout; 
	public static final int ChannelGuaranteedOutputBuffers  = ConfigManager.GuaranteedOutputBuffers; 
	public static final int ChannelInterfaceName  = ConfigManager.InterfaceName; 
	public static final int ChannelNumInputBuffers  = ConfigManager.NumInputBuffers; 
	public static final int ChannelSysRecvBufSize  = ConfigManager.SysRecvBufSize; 
	public static final int ChannelSysSendBufSize  = ConfigManager.SysSendBufSize; 
	public static final int ChannelHighWaterMark  = ConfigManager.HighWaterMark; 
	public static final int ChannelCompressionThreshold  = ConfigManager.ChannelCompressionThreshold; 
	public static final int ChannelCompressionType  = ConfigManager.ChannelCompressionType; 
	public static final int ChannelHost  = ConfigManager.ChannelHost; 
	public static final int ChannelObjectName  = ConfigManager.ChannelObjectName; 
	public static final int ChannelPort  = ConfigManager.ChannelPort; 
	public static final int ChannelTcpNodelay  = ConfigManager.ChannelTcpNodelay;
	public static final int ChannelDirectWrite  = ConfigManager.ChannelDirectSocketWrite; 
	
	// Channel: Multicast
	public static final int ChannelDisconnectOnGap  = ConfigManager.ChannelDisconnectOnGap; 
	public static final int ChannelHsmInterface  = ConfigManager.ChannelHsmInterface; 
	public static final int ChannelHsmInterval  = ConfigManager.ChannelHsmInterval; 
	public static final int ChannelHsmMultAddress  = ConfigManager.ChannelHsmMultAddress; 
	public static final int ChannelHsmPort  = ConfigManager.ChannelHsmPort; 
	public static final int Channelndata  = ConfigManager.Channelndata; 
	public static final int Channelnmissing  = ConfigManager.Channelnmissing; 
	public static final int Channelnrreq  = ConfigManager.Channelnrreq; 
	public static final int ChannelPacketTTL  = ConfigManager.ChannelPacketTTL; 
	public static final int ChannelpktPoolLimitHigh  = ConfigManager.ChannelpktPoolLimitHigh; 
	public static final int ChannelpktPoolLimitLow  = ConfigManager.ChannelpktPoolLimitLow; 
	public static final int ChannelRecvAddress  = ConfigManager.ChannelRecvAddress; 
	public static final int ChannelRecvPort  = ConfigManager.ChannelRecvPort; 
	public static final int ChannelSendAddress  = ConfigManager.ChannelSendAddress;
	public static final int ChannelSendPort  = ConfigManager.ChannelSendPort;
	public static final int Channeltbchold  = ConfigManager.Channeltbchold;
	public static final int ChanneltcpControlPort  = ConfigManager.ChanneltcpControlPort;
	public static final int Channeltdata  = ConfigManager.Channeltdata; 
	public static final int Channeltpphold  = ConfigManager.Channeltpphold; 
	public static final int Channeltrreq  = ConfigManager.Channeltrreq; 
	public static final int Channeltwait  = ConfigManager.Channeltwait; 
	public static final int ChannelUnicastPort  = ConfigManager.ChannelUnicastPort; 
	public static final int ChanneluserQLimit  = ConfigManager.ChanneluserQLimit; 
	
	public static String _lastErrorText = "";
	public static EmaObjectManager _objManager = new EmaObjectManager();
	
	static {
		_objManager.initialize();
	}
	
	// used only for JUNIT tests
	public static FieldListImpl createFieldList()
	{
		return new FieldListImpl(_objManager);
	}

	// used only for JUNIT tests
	public static ElementListImpl createElementList()
	{
		return new ElementListImpl(_objManager);
	}

	// used only for JUNIT tests
	public static MapImpl createMap()
	{
		return new MapImpl(_objManager);
	}
	
	// used only for JUNIT tests
	public static VectorImpl createVector()
	{
		return new VectorImpl(_objManager);
	}

	// used only for JUNIT tests
	public static SeriesImpl createSeries()
	{
		return new SeriesImpl(_objManager);
	}

	// used only for JUNIT tests
	public static FilterListImpl createFilterList()
	{
		return new FilterListImpl(_objManager);
	}
	
	// used only for JUNIT tests
	public static OmmArrayImpl createOmmArray()
	{
		return new OmmArrayImpl(_objManager);
	}

	// used only for JUNIT tests
	public static RefreshMsgImpl createRefreshMsg()
	{
		return new RefreshMsgImpl(_objManager);
	}
	
	// used only for JUNIT tests
	public static ReqMsgImpl createReqMsg()
	{
		return new ReqMsgImpl(_objManager);
	}

	// used only for JUNIT tests
	public static UpdateMsgImpl createUpdateMsg()
	{
		return new UpdateMsgImpl(_objManager);
	}

	// used only for JUNIT tests
	public static StatusMsgImpl createStatusMsg()
	{
		return new StatusMsgImpl(_objManager);
	}

	// used only for JUNIT tests
	public static PostMsgImpl createPostMsg()
	{
		return new PostMsgImpl(_objManager);
	}

	// used only for JUNIT tests
	public static AckMsgImpl createAckMsg()
	{
		return new AckMsgImpl(_objManager);
	}

	// used only for JUNIT tests
	public static GenericMsgImpl createGenericMsg()
	{
		return new GenericMsgImpl(_objManager);
	}
	
	// used only for JUNIT tests
	public static com.thomsonreuters.upa.codec.DataDictionary loadDictionary(String dictPath)
	{
		 com.thomsonreuters.upa.transport.Error error = com.thomsonreuters.upa.transport.TransportFactory.createError();
	     com.thomsonreuters.upa.codec.DataDictionary dictionary = com.thomsonreuters.upa.codec.CodecFactory.createDataDictionary();
	    	
	     if ( CodecReturnCodes.SUCCESS != dictionary.loadFieldDictionary(dictPath+"RDMFieldDictionary", error))
	    	 return null;
	     if ( CodecReturnCodes.SUCCESS != dictionary.loadEnumTypeDictionary(dictPath+"enumtype.def", error))
	    	 return null;
	     
	     return dictionary;
	}
	
	// used only for JUNIT tests
	public static void setRsslData(Data data, com.thomsonreuters.upa.codec.Msg rsslMsgEncoded, int majVer, int minVer,
			com.thomsonreuters.upa.codec.DataDictionary rsslDictionary, Object localFlSetDefDb)
	{
		((CollectionDataImpl) data).decode(rsslMsgEncoded, majVer, minVer, rsslDictionary);
	}

	// used only for JUNIT tests
	public static void setRsslData(Data data, com.thomsonreuters.upa.codec.Buffer rsslBufferEncoded, int majVer, int minVer,
			com.thomsonreuters.upa.codec.DataDictionary rsslDictionary, Object localFlSetDefDb)
	{
		((CollectionDataImpl) data).decode(rsslBufferEncoded, majVer, minVer, rsslDictionary, localFlSetDefDb);
	}
	
	// used only for JUNIT tests
	public static void setRsslData(Data data, Data dataEncoded, int majVer, int minVer,
			com.thomsonreuters.upa.codec.DataDictionary rsslDictionary, Object localFlSetDefDb)
	{
		((CollectionDataImpl) data).decode(((DataImpl)dataEncoded).encodedData(), majVer, minVer,  rsslDictionary, localFlSetDefDb);
	}

	public static void setRsslData(Msg msg, com.thomsonreuters.upa.codec.Buffer rsslBufferEncoded, int majVer, int minVer,
			com.thomsonreuters.upa.codec.DataDictionary rsslDictionary, Object localFlSetDefDb)
	{
		((MsgImpl) msg).decode(rsslBufferEncoded, majVer, minVer, rsslDictionary, localFlSetDefDb);
	}

	public static void setRsslData(Msg msg, Data dataEncoded, int majVer, int minVer,
			com.thomsonreuters.upa.codec.DataDictionary rsslDictionary, Object localFlSetDefDb)
	{
		((MsgImpl) msg).decode(((DataImpl)dataEncoded).encodedData(), majVer, minVer,  rsslDictionary, localFlSetDefDb);
	}
	
	// used only for JUNIT tests
	public static String getLastErrorText()
	{
		return _lastErrorText;
	}
	
	// used only for JUNIT tests
	public static void setRsslData(Buffer bufEncoded, Data dataEncoded)
	{
		((DataImpl)dataEncoded).encodedData().copy(bufEncoded);
	}
	
	// used only for JUNIT tests
	public static void setRsslData(RmtesBuffer rmtesBuffer, ByteBuffer dataEncoded)
	{
		((RmtesBufferImpl)rmtesBuffer).setRsslData(dataEncoded);
	}
	
	// used only for JUNIT tests
	public static String configGetConsumerName(OmmConsumerConfig consConfig)
	{
		return ((OmmConsumerConfigImpl) consConfig).configuredName();
	}
	
	// used only for JUINT tests
	public static int configVerifyChannelEncrypTypeAttribs(ChannelConfig chanCfg, String position,  OmmConsumerConfig consConfig, String channelName)
	{
		int result = 0;
		_lastErrorText = "";
		EncryptedChannelConfig encCfg = (EncryptedChannelConfig) chanCfg;
		String strValue = configGetChanPort(consConfig, channelName);
		if(strValue.equals(encCfg.serviceName) == false)
		{
			_lastErrorText = "Port mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig port='";
			_lastErrorText += strValue;
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] port='";
			_lastErrorText += encCfg.serviceName;
			_lastErrorText += "' for ";
			return 5;
		}
		strValue = configGetChanHost(consConfig, channelName);
		if(strValue.equals(encCfg.hostName) == false)
		{
			_lastErrorText = "HostName mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig host='";
			_lastErrorText += strValue;
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] host='";
			_lastErrorText += encCfg.hostName;
			_lastErrorText += "' for ";
			return 6;	
		}	
		Boolean boolValue = JUnitTestConnect.configGetBooleanValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelTcpNodelay);
		if(boolValue != encCfg.tcpNodelay)
		{
			_lastErrorText = "TcpNodelay mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig tcpNodelay ='";
			_lastErrorText += (boolValue ? "1" : "0");
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] tcpNodelay ='";
			_lastErrorText += (encCfg.tcpNodelay ? "1" : "0");
			_lastErrorText += "' for ";
			return 7;
		}	
		
		strValue = configGetStringValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelObjectName);
		if(strValue.equals(encCfg.objectName) == false)
		{
			_lastErrorText = "ObjectName mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig objectName='";
			_lastErrorText += strValue;
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] objectName='";
			_lastErrorText += encCfg.objectName;
			_lastErrorText += "' for ";
			return 8;	
		}	
	
		return result;			
	}

	// used only for JUINT tests
	public static int configVerifyChannelSocketTypeAttribs(ChannelConfig chanCfg, String position,  OmmConsumerConfig consConfig, String channelName)
	{
		int result = 0;
		_lastErrorText = "";
		SocketChannelConfig socCfg = (SocketChannelConfig) chanCfg;
		String strValue = configGetChanPort(consConfig, channelName);
		if(strValue.equals(socCfg.serviceName) == false)
		{
			_lastErrorText = "Port mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig port='";
			_lastErrorText += strValue;
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] port='";
			_lastErrorText += socCfg.serviceName;
			_lastErrorText += "' for ";
			return 5;
		}
		strValue = configGetChanHost(consConfig, channelName);
		if(strValue.equals(socCfg.hostName) == false)
		{
			_lastErrorText = "HostName mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig host='";
			_lastErrorText += strValue;
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] host='";
			_lastErrorText += socCfg.hostName;
			_lastErrorText += "' for ";
			return 6;	
		}	
		Boolean boolValue = JUnitTestConnect.configGetBooleanValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelTcpNodelay);
		if(boolValue != socCfg.tcpNodelay)
		{
			_lastErrorText = "TcpNodelay mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig tcpNodelay ='";
			_lastErrorText += (boolValue ? "1" : "0");
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] tcpNodelay ='";
			_lastErrorText += (socCfg.tcpNodelay ? "1" : "0");
			_lastErrorText += "' for ";
			return 7;
		}		
		return result;
	}

	// used only for JUINT tests
	public static int configVerifyChannelCommonAttribs(ChannelConfig chanCfg, String position,  OmmConsumerConfig consConfig, String channelName, ChannelConfig lastChanCfg)
	{
		int result = 0;
		int intValue = JUnitTestConnect.configGetIntValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelCompressionType);
		if(intValue != chanCfg.compressionType)
		{
			_lastErrorText = "CompressionType mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig CompressionType='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] CompressionType='";
			_lastErrorText += Integer.toString(chanCfg.compressionType);
			_lastErrorText += "' for ";
			return 9;
		}
		
		int intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelGuaranteedOutputBuffers);
		if(intLongValue != chanCfg.guaranteedOutputBuffers)
		{
			_lastErrorText = "GuaranteedOutputBuffers mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig guaranteedOutputBuffers ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] guaranteedOutputBuffers ='";
			_lastErrorText += Integer.toString(chanCfg.guaranteedOutputBuffers);
			_lastErrorText += "' for ";
			return 10;
		}
		
		intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelNumInputBuffers);
		if(intLongValue != chanCfg.numInputBuffers)
		{
			_lastErrorText = "NumInputBuffers mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig numInputBuffers ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] numInputBuffers ='";
			_lastErrorText += Integer.toString(chanCfg.numInputBuffers);
			_lastErrorText += "' for ";
			return 11;
		}

		intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelSysRecvBufSize);
		if(intLongValue != chanCfg.sysRecvBufSize)
		{
			_lastErrorText = "SysRecvBufSize mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig sysRecvBufSize ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] sysRecvBufSize ='";
			_lastErrorText += Integer.toString(chanCfg.sysRecvBufSize);
			_lastErrorText += "' for ";
			return 12;
		}

		intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelSysSendBufSize);
		if(intLongValue != chanCfg.sysSendBufSize)
		{
			_lastErrorText = "SysSendBufSize mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig sysSendBufSize ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] sysSendBufSize ='";
			_lastErrorText += Integer.toString(chanCfg.sysSendBufSize);
			_lastErrorText += "' for ";
			return 13;
		}
		
		intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelCompressionThreshold);
		if(intLongValue != chanCfg.compressionThreshold)
		{
			_lastErrorText = "CompressionThreshold mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig compressionThreshold ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] compressionThreshold ='";
			_lastErrorText += Integer.toString(chanCfg.compressionThreshold);
			_lastErrorText += "' for ";
			return 14;
		}

		intLongValue = JUnitTestConnect.configGetIntLongValue(consConfig, channelName, JUnitTestConnect.ConfigGroupTypeChannel, JUnitTestConnect.ChannelConnectionPingTimeout);
		if(intLongValue != chanCfg.connectionPingTimeout)
		{
			_lastErrorText = "ConnectionPingTimeout mismatch in '";
			_lastErrorText += channelName;
			_lastErrorText += "' FileConfig connectionPingTimeout ='";
			_lastErrorText += Integer.toString(intValue);
			_lastErrorText += "' Internal Active ChannelSet[";
			_lastErrorText += position;
			_lastErrorText += "] connectionPingTimeout ='";
			_lastErrorText += Integer.toString(chanCfg.connectionPingTimeout);
			_lastErrorText += "' for ";
			return 15;
		}

	
		return result;
	}
	
	// used only for JUNIT tests
	public static int configVerifyConsChannelSetAttribs(OmmConsumer consumer, OmmConsumerConfig consConfig, String consumerName )
	{
		_lastErrorText = "";
		int result = 0;
		OmmConsumerImpl consImpl = ( OmmConsumerImpl ) consumer;
		
		String channelName = configGetChannelName(consConfig, consumerName);
		if(channelName == null)
		{
			_lastErrorText = "Channel is null for ";
			_lastErrorText += consImpl.consumerName();
			result = 1;
			return 1;
		}
		
		String [] channels  = channelName.split(",");
		if(channels.length != consImpl.activeConfig().channelConfigSet.size())
		{
			_lastErrorText = "Channel set size is != number of channels in the file config channelSet for ";
			_lastErrorText += consImpl.consumerName();
			return 2;
		}
		String channName = null;
		String position = null;
		ChannelConfig lastChanCfg = consImpl.activeConfig().channelConfigSet.get( channels.length - 1);
		for (int i = 0; i < channels.length; i++)
		{
			ChannelConfig chanCfg = consImpl.activeConfig().channelConfigSet.get(i);
			channName = channels[i];
			position = Integer.toString(i);
			int channelConnType = configGetChannelType(consConfig, channName);
			if( channName.equals(chanCfg.name) == false )
			{
				_lastErrorText = "ChannelName mismatch: FileConfig name='";
				_lastErrorText += channName;
				_lastErrorText += "' Internal Active ChannelSet[";
				_lastErrorText += position;
				_lastErrorText += "] name='";
				_lastErrorText += chanCfg.name;
				_lastErrorText += "' for ";
				_lastErrorText += consImpl.consumerName();
				return 3;
			}
			if( channelConnType != chanCfg.rsslConnectionType )
			{
				_lastErrorText = "ConnectionType mismatch in '";
				_lastErrorText += channName;
				_lastErrorText += "' FileConfig ConnectionType='";
				_lastErrorText += Integer.toString(channelConnType);
				_lastErrorText += "' Internal Active ChannelSet[";
				_lastErrorText += position;
				_lastErrorText += "] ConnectionType='";
				_lastErrorText += Integer.toString(chanCfg.rsslConnectionType);
				_lastErrorText += "' for ";
				_lastErrorText += consImpl.consumerName();
				return 4;
			}
			switch( channelConnType )
			{
			case com.thomsonreuters.upa.transport.ConnectionTypes.SOCKET:
				{
					result = configVerifyChannelSocketTypeAttribs(chanCfg, position, consConfig, channName);
					break;
				}
			case com.thomsonreuters.upa.transport.ConnectionTypes.ENCRYPTED:
				{
					result = configVerifyChannelEncrypTypeAttribs(chanCfg, position, consConfig, channName);
					break;
				}			
			default:
				break;
			}
			if(result != 0)
			{
				_lastErrorText += consImpl.consumerName();
				break;
			}
			else
			{
				result = configVerifyChannelCommonAttribs(chanCfg, position, consConfig, channName, lastChanCfg);
				if(result != 0)
				{
					_lastErrorText += consImpl.consumerName();
					break;
				}
			}
		}		

		return result;
	}
	
	// used only for JUNIT tests
	public static String configGetChannelName(OmmConsumerConfig consConfig, String consumerName)
	{
		return ((OmmConsumerConfigImpl) consConfig).channelName(consumerName);
	}
	
	// used only for JUNIT tests
	public static String configGetDictionaryName(OmmConsumerConfig consConfig, String consumerName)
	{
		return ((OmmConsumerConfigImpl) consConfig).dictionaryName(consumerName);
	}
	
	// used only for JUNIT tests
	public static int configGetChannelType(OmmConsumerConfig consConfig, String channelName)
	{
		OmmConsumerConfigImpl configImpl = ( (OmmConsumerConfigImpl ) consConfig);
		ConfigAttributes attributes = configImpl.xmlConfig().getChannelAttributes(channelName);
		ConfigElement ce = null;
		int connectionType = ConnectionTypes.SOCKET;
	
		if (configImpl.getUserSpecifiedHostname() != null)
			connectionType = ConnectionTypes.SOCKET;
		else
		{
			if (attributes != null) 
			{
				ce = attributes.getPrimitiveValue(ConfigManager.ChannelType);
				if (ce != null)
					connectionType = ce.intValue();
			}
		}
		return connectionType;
	}	
	
	// used only for JUNIT tests
	public static String configGetChanHost(OmmConsumerConfig consConfig, String channelName)
	{
		OmmConsumerConfigImpl configImpl = ( (OmmConsumerConfigImpl ) consConfig);
		ConfigAttributes attributes = configImpl.xmlConfig().getChannelAttributes(channelName);
		ConfigElement ce = null;
		String host =  configImpl.getUserSpecifiedHostname();;
		if (host == null)
		{
			if (attributes != null && (ce = attributes.getPrimitiveValue(ConfigManager.ChannelHost)) != null)
				host = ce.asciiValue();
		}
	
		return host;
	}	
	
	// used only for JUNIT tests
	public static String configGetChanPort(OmmConsumerConfig consConfig, String channelName)
	{
		OmmConsumerConfigImpl configImpl = ( (OmmConsumerConfigImpl ) consConfig);
		ConfigAttributes attributes = configImpl.xmlConfig().getChannelAttributes(channelName);
		ConfigElement ce = null;
		String port =  configImpl.getUserSpecifiedPort();;
		if (port == null)
		{
			if (attributes != null && (ce = attributes.getPrimitiveValue(ConfigManager.ChannelPort)) != null)
				port = ce.asciiValue();
		}

		return port;
	}	
	
	// used only for JUNIT tests
	public static int configGetIntLongValue(OmmConsumerConfig consConfig, String name, int type, int configParam)
	{
		ConfigAttributes attributes = null;
		if(type == ConfigGroupTypeConsumer)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getConsumerAttributes(name);
		else if (type == ConfigGroupTypeChannel)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getChannelAttributes(name);
		else if (type == ConfigGroupTypeDictionary)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getDictionaryAttributes(name);
	
		ConfigElement ce = null;
		int maxInt = Integer.MAX_VALUE;
		if (attributes != null)
		{
			if ((ce = attributes.getPrimitiveValue(configParam)) != null)
				return (ce.intLongValue() > maxInt ? maxInt : ce.intLongValue());
		}
	
		return 0;
	}	
	
	// used only for JUNIT tests
	public static int configGetIntValue(OmmConsumerConfig consConfig, String name, int type, int configParam)
	{
		ConfigAttributes attributes = null;
		if(type == ConfigGroupTypeConsumer)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getConsumerAttributes(name);
		else if (type == ConfigGroupTypeChannel)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getChannelAttributes(name);
		else if (type == ConfigGroupTypeDictionary)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getDictionaryAttributes(name);
	
		ConfigElement ce = null;
		if (attributes != null)
		{
			if ((ce = attributes.getPrimitiveValue(configParam)) != null)
				return (ce.intValue());
		}
	
		return 0;
	}	

	// used only for JUNIT tests
	public static String configGetStringValue(OmmConsumerConfig consConfig, String name, int type, int configParam)
	{
		ConfigAttributes attributes = null;
		if(type == ConfigGroupTypeConsumer)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getConsumerAttributes(name);
		else if (type == ConfigGroupTypeChannel)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getChannelAttributes(name);
		else if (type == ConfigGroupTypeDictionary)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getDictionaryAttributes(name);
	
		ConfigElement ce = null;
		String configParamValue =  null;
		if (attributes != null )
		{
			ce = attributes.getPrimitiveValue(configParam);
			if(ce != null)
				configParamValue = ce.asciiValue();
		}
		return configParamValue;
	}	

	// used only for JUNIT tests
	public static Boolean configGetBooleanValue(OmmConsumerConfig consConfig, String name, int type, int configParam)
	{
		ConfigAttributes attributes = null;
		if(type == ConfigGroupTypeConsumer)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getConsumerAttributes(name);
		else if (type == ConfigGroupTypeChannel)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getChannelAttributes(name);
		else if (type == ConfigGroupTypeDictionary)
			attributes = ((OmmConsumerConfigImpl) consConfig).xmlConfig().getDictionaryAttributes(name);
	
		ConfigElement ce = null;
		Boolean configParamValue = false;
		if (attributes != null) 
		{
			ce = attributes.getPrimitiveValue(configParam);

			if (ce != null)
				configParamValue = ce.booleanValue();
		}
		return configParamValue;
	}	
}
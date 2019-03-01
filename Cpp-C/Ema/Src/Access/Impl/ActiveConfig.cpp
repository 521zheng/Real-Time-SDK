/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#include "ActiveConfig.h"
#include "EmaConfigImpl.h"

using namespace thomsonreuters::ema::access;

DictionaryConfig::DictionaryConfig() :
	dictionaryName(),
	rdmfieldDictionaryFileName(),
	enumtypeDefFileName(),
	rdmFieldDictionaryItemName(),
	enumTypeDefItemName(),
	dictionaryType( DEFAULT_DICTIONARY_TYPE )
{
}

DictionaryConfig::~DictionaryConfig()
{
}

void DictionaryConfig::clear()
{
	dictionaryName.clear();
	rdmfieldDictionaryFileName.clear();
	enumtypeDefFileName.clear();
	rdmFieldDictionaryItemName.clear();
	enumTypeDefItemName.clear();
	dictionaryType = DEFAULT_DICTIONARY_TYPE;
}

ServiceDictionaryConfig::ServiceDictionaryConfig() :
	serviceId(0)
{
}

ServiceDictionaryConfig::~ServiceDictionaryConfig()
{
	clear();
}

void ServiceDictionaryConfig::clear()
{
	serviceId = 0;

	DictionaryConfig* dictionaryConfig = dictionaryUsedList.pop_back();

	while (dictionaryConfig)
	{
		delete dictionaryConfig;
		dictionaryConfig = dictionaryUsedList.pop_back();
	}

	dictionaryConfig = dictionaryProvidedList.pop_back();

	while (dictionaryConfig)
	{
		delete dictionaryConfig;
		dictionaryConfig = dictionaryProvidedList.pop_back();
	}
}

void ServiceDictionaryConfig::addDictionaryUsed(DictionaryConfig* dictionaryUsed)
{
	dictionaryUsedList.push_back(dictionaryUsed);
}

void ServiceDictionaryConfig::addDictionaryProvided(DictionaryConfig* dictionaryProvided)
{
	dictionaryProvidedList.push_back(dictionaryProvided);
}

DictionaryConfig* ServiceDictionaryConfig::findDictionary(const EmaString& dictionaryName, bool isDictProvided)
{
	if (isDictProvided)
	{
		DictionaryConfig* name = dictionaryProvidedList.front();
		int size = dictionaryProvidedList.size();
		bool foundDefaultName = false;
		while (name && size-- > 0)
		{
			if (name->dictionaryName == dictionaryName)
				return name;
			else
				name = name->next();
		}
		return 0;
	}
	else
	{
		DictionaryConfig* name = dictionaryUsedList.front();
		int size = dictionaryUsedList.size();
		bool foundDefaultName = false;
		while (name && size-- > 0)
		{
			if (name->dictionaryName == dictionaryName)
				return name;
			else
				name = name->next();
		}
		return 0;
	}
}

EmaList<DictionaryConfig*>& ServiceDictionaryConfig::getDictionaryUsedList()
{
	return dictionaryUsedList;
}

EmaList<DictionaryConfig*>& ServiceDictionaryConfig::getDictionaryProvidedList()
{
	return dictionaryProvidedList;
}

LoggerConfig::LoggerConfig() :
	loggerName(),
	loggerFileName(),
	minLoggerSeverity( DEFAULT_LOGGER_SEVERITY ),
	loggerType( OmmLoggerClient::FileEnum),
	includeDateInLoggerOutput( DEFAULT_INCLUDE_DATE_IN_LOGGER_OUTPUT )
{
}

LoggerConfig::~LoggerConfig()
{
}

void LoggerConfig::clear()
{
	loggerName.clear();
	loggerFileName.clear();
	minLoggerSeverity = DEFAULT_LOGGER_SEVERITY;
	loggerType = OmmLoggerClient::FileEnum;
}

BaseConfig::BaseConfig() :
	configuredName(),
	instanceName(),
	itemCountHint(DEFAULT_ITEM_COUNT_HINT),
	serviceCountHint(DEFAULT_SERVICE_COUNT_HINT),
	dispatchTimeoutApiThread(DEFAULT_DISPATCH_TIMEOUT_API_THREAD),
	maxDispatchCountApiThread(DEFAULT_MAX_DISPATCH_COUNT_API_THREAD),
	maxDispatchCountUserThread(DEFAULT_MAX_DISPATCH_COUNT_USER_THREAD),
	requestTimeout(DEFAULT_REQUEST_TIMEOUT),
	xmlTraceMaxFileSize(DEFAULT_XML_TRACE_MAX_FILE_SIZE),
	xmlTraceToFile(DEFAULT_XML_TRACE_TO_FILE),
	xmlTraceToStdout(DEFAULT_XML_TRACE_TO_STDOUT),
	xmlTraceToMultipleFiles(DEFAULT_XML_TRACE_TO_MULTIPLE_FILE),
	xmlTraceWrite(DEFAULT_XML_TRACE_WRITE),
	xmlTraceRead(DEFAULT_XML_TRACE_READ),
	xmlTracePing(DEFAULT_XML_TRACE_PING),
	xmlTraceHex(DEFAULT_XML_TRACE_HEX),
	xmlTraceFileName(DEFAULT_XML_TRACE_FILE_NAME),
	loggerConfig(),
	catchUnhandledException(DEFAULT_HANDLE_EXCEPTION),
	parameterConfigGroup(1), // This variable is set for handling deprecation cases.
	libSslName(),
	libCryptoName(),
	traceStr()
{
}

BaseConfig::~BaseConfig()
{
}

void BaseConfig::clear()
{
	configuredName.clear();
	instanceName.clear();
	itemCountHint = DEFAULT_ITEM_COUNT_HINT;
	serviceCountHint = DEFAULT_SERVICE_COUNT_HINT;
	dispatchTimeoutApiThread = DEFAULT_DISPATCH_TIMEOUT_API_THREAD;
	maxDispatchCountApiThread = DEFAULT_MAX_DISPATCH_COUNT_API_THREAD;
	maxDispatchCountUserThread = DEFAULT_MAX_DISPATCH_COUNT_USER_THREAD;
	requestTimeout = DEFAULT_REQUEST_TIMEOUT;
	xmlTraceMaxFileSize = DEFAULT_XML_TRACE_MAX_FILE_SIZE;
	xmlTraceToFile = DEFAULT_XML_TRACE_TO_FILE;
	xmlTraceToStdout = DEFAULT_XML_TRACE_TO_STDOUT;
	xmlTraceToMultipleFiles = DEFAULT_XML_TRACE_TO_MULTIPLE_FILE;
	xmlTraceWrite = DEFAULT_XML_TRACE_WRITE;
	xmlTraceRead = DEFAULT_XML_TRACE_READ;
	xmlTracePing = DEFAULT_XML_TRACE_PING;
	xmlTraceHex = DEFAULT_XML_TRACE_HEX;
	xmlTraceFileName = DEFAULT_XML_TRACE_FILE_NAME;
	loggerConfig.clear();
	libSslName.clear();
	libCryptoName.clear();
	traceStr.clear();
}

EmaString BaseConfig::configTrace()
{
	traceStr.clear();
	traceStr.append("\n\t configuredName: ").append(configuredName)
		.append("\n\t instanceName: ").append(instanceName)
		.append("\n\t itemCountHint: ").append(itemCountHint)
		.append("\n\t serviceCountHint: ").append(serviceCountHint)
		.append("\n\t dispatchTimeoutApiThread: ").append(dispatchTimeoutApiThread)
		.append("\n\t maxDispatchCountApiThread: ").append(maxDispatchCountApiThread)
		.append("\n\t maxDispatchCountUserThread : ").append(maxDispatchCountUserThread)
		.append("\n\t requestTimeout : ").append(requestTimeout)
		.append("\n\t xmlTraceMaxFileSize : ").append(xmlTraceMaxFileSize)
		.append("\n\t xmlTraceToFile : ").append(xmlTraceToFile)
		.append("\n\t xmlTraceToStdout : ").append(xmlTraceToStdout)
		.append("\n\t xmlTraceToMultipleFiles : ").append(xmlTraceToMultipleFiles)
		.append("\n\t xmlTraceWrite : ").append(xmlTraceWrite)
		.append("\n\t xmlTraceRead : ").append(xmlTraceRead)
		.append("\n\t xmlTracePing : ").append(xmlTracePing)
		.append("\n\t xmlTraceHex : ").append(xmlTraceHex)
		.append("\n\t xmlTraceFileName : ").append(xmlTraceFileName)
		.append("\n\t libSslName : ").append(libSslName)
		.append("\n\t libCryptoName : ").append(libCryptoName);

	return traceStr;
}

void BaseConfig::setItemCountHint(UInt64 value)
{
	if (value <= 0) {}
	else if (value > 0xFFFFFFFF)
		itemCountHint = 0xFFFFFFFF;
	else
		itemCountHint = (UInt32)value;
}

void BaseConfig::setServiceCountHint(UInt64 value)
{
	if (value <= 0) {}
	else if (value > 0xFFFFFFFF)
		serviceCountHint = 0xFFFFFFFF;
	else
		serviceCountHint = (UInt32)value;
}

void BaseConfig::setRequestTimeout(UInt64 value)
{
	if (value <= 0) {}
	else if (value > 0xFFFFFFFF)
		requestTimeout = 0xFFFFFFFF;
	else
		requestTimeout = (UInt32)value;
}

void BaseConfig::setCatchUnhandledException(UInt64 value)
{
	if (value > 0)
		catchUnhandledException = true;
	else
		catchUnhandledException = false;
}

void BaseConfig::setMaxDispatchCountApiThread(UInt64 value)
{
	if (value <= 0) {}
	else if (value > 0xFFFFFFFF)
		maxDispatchCountApiThread = 0xFFFFFFFF;
	else
		maxDispatchCountApiThread = (UInt32)value;
}

void BaseConfig::setMaxDispatchCountUserThread(UInt64 value)
{
	if (value <= 0) {}
	else if (value > 0xFFFFFFFF)
		maxDispatchCountUserThread = 0xFFFFFFFF;
	else
		maxDispatchCountUserThread = (UInt32)value;
}

ActiveConfig::ActiveConfig( const EmaString& defaultServiceName ) :
	obeyOpenWindow( DEFAULT_OBEY_OPEN_WINDOW ),
	postAckTimeout( DEFAULT_POST_ACK_TIMEOUT ),
	maxOutstandingPosts( DEFAULT_MAX_OUTSTANDING_POSTS ),
	loginRequestTimeOut( DEFAULT_LOGIN_REQUEST_TIMEOUT ),
	directoryRequestTimeOut( DEFAULT_DIRECTORY_REQUEST_TIMEOUT ),
	dictionaryRequestTimeOut( DEFAULT_DICTIONARY_REQUEST_TIMEOUT ),
	reconnectAttemptLimit(DEFAULT_RECONNECT_ATTEMPT_LIMIT),
	reconnectMinDelay(DEFAULT_RECONNECT_MIN_DELAY),
	reconnectMaxDelay(DEFAULT_RECONNECT_MAX_DELAY),
	msgKeyInUpdates(DEFAULT_MSGKEYINUPDATES),
	pipePort(DEFAULT_PIPE_PORT),
	pRsslRDMLoginReq( 0 ),
	pRsslDirectoryRequestMsg( 0 ),
	pRsslRdmFldRequestMsg( 0 ),
	pRsslEnumDefRequestMsg( 0 ),
	pDirectoryRefreshMsg( 0 ),
	_defaultServiceName( defaultServiceName ),
	dictionaryConfig()
{
}

ActiveConfig::~ActiveConfig()
{
	clearChannelSet();
}

EmaString ActiveConfig::configTrace()
{
	BaseConfig::configTrace();
	traceStr.append("\n\t pipePort: ").append(pipePort)
		.append("\n\t obeyOpenWindow: ").append(obeyOpenWindow)
		.append("\n\t postAckTimeout: ").append(postAckTimeout)
		.append("\n\t maxOutstandingPosts: ").append(maxOutstandingPosts)
		.append("\n\t reconnectAttemptLimit: ").append(reconnectAttemptLimit)
		.append("\n\t reconnectMinDelay : ").append(reconnectMinDelay)
		.append("\n\t reconnectMaxDelay : ").append(reconnectMaxDelay)
		.append("\n\t msgKeyInUpdates : ").append(msgKeyInUpdates)
		.append("\n\t directoryRequestTimeOut : ").append(directoryRequestTimeOut)
		.append("\n\t dictionaryRequestTimeOut : ").append(dictionaryRequestTimeOut)
		.append("\n\t loginRequestTimeOut : ").append(loginRequestTimeOut);
	return traceStr;
}

void ActiveConfig::clearChannelSet()
{
	if ( configChannelSet.size() == 0 )
		return;
	for ( unsigned int i = 0; i < configChannelSet.size(); ++i )
	{
		if ( configChannelSet[i] != NULL )
		{
			delete configChannelSet[i];
			configChannelSet[i] = NULL;
		}
	}

	configChannelSet.clear();
}

void ActiveConfig::clear()
{
	pipePort = DEFAULT_PIPE_PORT;
	obeyOpenWindow = DEFAULT_OBEY_OPEN_WINDOW;
	postAckTimeout = DEFAULT_POST_ACK_TIMEOUT;
	maxOutstandingPosts = DEFAULT_MAX_OUTSTANDING_POSTS;
	reconnectAttemptLimit = DEFAULT_RECONNECT_ATTEMPT_LIMIT;
	reconnectMinDelay = DEFAULT_RECONNECT_MIN_DELAY;
	reconnectMaxDelay = DEFAULT_RECONNECT_MAX_DELAY;
	msgKeyInUpdates = DEFAULT_MSGKEYINUPDATES;
	directoryRequestTimeOut = DEFAULT_DIRECTORY_REQUEST_TIMEOUT;
	dictionaryRequestTimeOut = DEFAULT_DICTIONARY_REQUEST_TIMEOUT;
	loginRequestTimeOut = DEFAULT_LOGIN_REQUEST_TIMEOUT;
	dictionaryConfig.clear();
	pRsslRDMLoginReq = 0;
	pRsslDirectoryRequestMsg = 0;
	pRsslRdmFldRequestMsg = 0;
	pRsslEnumDefRequestMsg = 0;

	if ( pDirectoryRefreshMsg )
		delete pDirectoryRefreshMsg;
	pDirectoryRefreshMsg = 0;

}

void ActiveConfig::setObeyOpenWindow( UInt64 value )
{
	if ( value <= 0 )
		obeyOpenWindow = 0;
	else
		obeyOpenWindow = 1;
}

void ActiveConfig::setPostAckTimeout( UInt64 value )
{
	if ( value <= 0 ) {}
	else if ( value > 0xFFFFFFFF )
		postAckTimeout = 0xFFFFFFFF;
	else
		postAckTimeout = ( UInt32 )value;
}

void ActiveConfig::setLoginRequestTimeOut( UInt64 value )
{
	if ( value > 0xFFFFFFFF )
		loginRequestTimeOut = 0xFFFFFFFF;
	else
		loginRequestTimeOut = ( UInt32 ) value;
}

void ActiveConfig::setDirectoryRequestTimeOut( UInt64 value )
{
	if ( value > 0xFFFFFFFF )
		directoryRequestTimeOut = 0xFFFFFFFF;
	else
		directoryRequestTimeOut = ( UInt32 ) value;
}

void ActiveConfig::setDictionaryRequestTimeOut( UInt64 value )
{
	if ( value > 0xFFFFFFFF )
		dictionaryRequestTimeOut = 0xFFFFFFFF;
	else
		dictionaryRequestTimeOut = ( UInt32 ) value;
}

void ActiveConfig::setMaxOutstandingPosts( UInt64 value )
{
	if ( value <= 0 ) {}
	else if ( value > 0xFFFFFFFF )
		maxOutstandingPosts = 0xFFFFFFFF;
	else
		maxOutstandingPosts = ( UInt32 )value;
}

void ActiveConfig::setReconnectAttemptLimit(Int64 value)
{
	if (value >= 0)
	{
		reconnectAttemptLimit = value > 0x7FFFFFFF ? 0x7FFFFFFF : (Int32)value;
	}
}
void ActiveConfig::setReconnectMinDelay(Int64 value)
{
	if (value > 0)
	{
		reconnectMinDelay = value > 0x7FFFFFFF ? 0x7FFFFFFF : (Int32)value;
	}
}

void ActiveConfig::setReconnectMaxDelay(Int64 value)
{
	if (value > 0)
	{
		reconnectMaxDelay = value > 0x7FFFFFFF ? 0x7FFFFFFF : (Int32)value;
	}
}

ChannelConfig* ActiveConfig::findChannelConfig( const Channel* pChannel )
{
	ChannelConfig* retChannelCfg = 0;
	for ( unsigned int i = 0; i < configChannelSet.size(); ++i )
	{
		if ( configChannelSet[i]->pChannel ==  pChannel )
		{
			retChannelCfg = configChannelSet[i];
			break;
		}
	}
	return retChannelCfg;
}

bool ActiveConfig::findChannelConfig( EmaVector< ChannelConfig* >& cfgChannelSet, const EmaString& channelName, unsigned int& pos )
{
	bool channelFound = false;
	if ( cfgChannelSet.size() > 0 )
	{
		for ( pos = 0; pos < cfgChannelSet.size(); ++pos )
		{
			if ( cfgChannelSet[pos]->name ==  channelName )
			{
				channelFound = true;
				break;
			}
		}
	}
	return channelFound;
}

ActiveServerConfig::ActiveServerConfig(const EmaString& defaultServiceName) :
	pipePort(DEFAULT_SERVER_PIPE_PORT),
	acceptMessageWithoutBeingLogin(DEFAULT_ACCEPT_MSG_WITHOUT_BEING_LOGIN),
	acceptMessageWithoutAcceptingRequests(DEFAULT_ACCEPT_MSG_WITHOUT_ACCEPTING_REQUESTS),
	acceptDirMessageWithoutMinFilters(DEFAULT_ACCEPT_DIR_MSG_WITHOUT_MIN_FILTERS),
	acceptMessageWithoutQosInRange(DEFAULT_ACCEPT_MSG_WITHOUT_QOS_IN_RANGE),
	acceptMessageSameKeyButDiffStream(DEFAULT_ACCEPT_MSG_SAMEKEY_BUT_DIFF_STREAM),
	acceptMessageThatChangesService(DEFAULT_ACCEPT_MSG_THAT_CHANGES_SERVICE),
	_defaultServiceName(defaultServiceName),
	pDirectoryRefreshMsg(0)
{
	pServerConfig = new SocketServerConfig(defaultServiceName);
}

ActiveServerConfig::~ActiveServerConfig()
{
	ServiceDictionaryConfig* serviceDictionaryConfig = _serviceDictionaryConfigList.pop_back();

	while (serviceDictionaryConfig)
	{
		delete serviceDictionaryConfig;
		serviceDictionaryConfig = _serviceDictionaryConfigList.pop_back();
	}

	if (pServerConfig)
		delete pServerConfig;
}

void ActiveServerConfig::clear()
{
	pipePort = DEFAULT_SERVER_PIPE_PORT;
	acceptMessageWithoutBeingLogin = DEFAULT_ACCEPT_MSG_WITHOUT_BEING_LOGIN;
	acceptMessageWithoutAcceptingRequests = DEFAULT_ACCEPT_MSG_WITHOUT_ACCEPTING_REQUESTS;
	acceptDirMessageWithoutMinFilters = DEFAULT_ACCEPT_DIR_MSG_WITHOUT_MIN_FILTERS;
	acceptMessageWithoutQosInRange = DEFAULT_ACCEPT_MSG_WITHOUT_QOS_IN_RANGE;
	acceptMessageSameKeyButDiffStream = DEFAULT_ACCEPT_MSG_SAMEKEY_BUT_DIFF_STREAM;
	acceptMessageThatChangesService = DEFAULT_ACCEPT_MSG_THAT_CHANGES_SERVICE;

	if (pDirectoryRefreshMsg)
		delete pDirectoryRefreshMsg;
	pDirectoryRefreshMsg = 0;
}

EmaString ActiveServerConfig::configTrace()
{
	BaseConfig::configTrace();
	traceStr.append("\n\t pipePort: ").append(pipePort)
		.append("\n\t acceptMessageWithoutBeingLogin: ").append(acceptMessageWithoutBeingLogin)
		.append("\n\t acceptMessageWithoutAcceptingRequests: ").append(acceptMessageWithoutAcceptingRequests)
		.append("\n\t acceptDirMessageWithoutMinFilters: ").append(acceptDirMessageWithoutMinFilters)
		.append("\n\t acceptMessageWithoutQosInRange: ").append(acceptMessageWithoutQosInRange)
		.append("\n\t acceptMessageSameKeyButDiffStream: ").append(acceptMessageSameKeyButDiffStream)
		.append("\n\t acceptMessageThatChangesService: ").append(acceptMessageThatChangesService);

	return traceStr;
}

ServiceDictionaryConfig*	ActiveServerConfig::getServiceDictionaryConfig(UInt16 serviceId)
{
	ServiceDictionaryConfig** serviceDictionaryConfigPtr = _serviceDictionaryConfigHash.find(serviceId);

	return serviceDictionaryConfigPtr ? *serviceDictionaryConfigPtr : 0;
}

void ActiveServerConfig::addServiceDictionaryConfig(ServiceDictionaryConfig* serviceDictionaryConfig)
{
	_serviceDictionaryConfigHash.insert(serviceDictionaryConfig->serviceId, serviceDictionaryConfig);
	_serviceDictionaryConfigList.push_back(serviceDictionaryConfig);
}

void ActiveServerConfig::removeServiceDictionaryConfig(ServiceDictionaryConfig* serviceDictionaryConfig)
{
	_serviceDictionaryConfigHash.erase(serviceDictionaryConfig->serviceId);
	_serviceDictionaryConfigList.remove(serviceDictionaryConfig);

	delete serviceDictionaryConfig;
}

void ActiveServerConfig::setServiceDictionaryConfigList(EmaList<ServiceDictionaryConfig*>& serviceDictionaryConfigList)
{
	ServiceDictionaryConfig* serviceDictionaryConfig = serviceDictionaryConfigList.pop_back();

	while (serviceDictionaryConfig)
	{
		addServiceDictionaryConfig(serviceDictionaryConfig);

		serviceDictionaryConfig = serviceDictionaryConfigList.pop_back();
	}
}

const EmaList<ServiceDictionaryConfig*>& ActiveServerConfig::getServiceDictionaryConfigList()
{
	return _serviceDictionaryConfigList;
}

size_t ActiveServerConfig::UInt16rHasher::operator()(const UInt16& value) const
{
	return value;
}

bool ActiveServerConfig::UInt16Equal_To::operator()(const UInt16& x, const UInt16& y) const
{
	return x == y ? true : false;
}

ChannelConfig::ChannelConfig()
{
}

ChannelConfig::ChannelConfig( RsslConnectionTypes type ) :
	name(),
	interfaceName( DEFAULT_INTERFACE_NAME ),
	compressionType( DEFAULT_COMPRESSION_TYPE ),
	compressionThreshold( DEFAULT_COMPRESSION_THRESHOLD ),
	connectionType( type ),
	connectionPingTimeout( DEFAULT_CONNECTION_PINGTIMEOUT ),
	guaranteedOutputBuffers( DEFAULT_GUARANTEED_OUTPUT_BUFFERS ),
	numInputBuffers( DEFAULT_NUM_INPUT_BUFFERS ),
	sysSendBufSize( DEFAULT_SYS_SEND_BUFFER_SIZE ),
	sysRecvBufSize( DEFAULT_SYS_RECEIVE_BUFFER_SIZE ),
	highWaterMark( DEFAULT_HIGH_WATER_MARK ),
	pChannel( 0 )
{
}

void ChannelConfig::clear()
{
	name.clear();
	interfaceName = DEFAULT_INTERFACE_NAME;
	compressionType = DEFAULT_COMPRESSION_TYPE;
	compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;
	connectionPingTimeout = DEFAULT_CONNECTION_PINGTIMEOUT;
	guaranteedOutputBuffers = DEFAULT_GUARANTEED_OUTPUT_BUFFERS;
	numInputBuffers = DEFAULT_NUM_INPUT_BUFFERS;
	sysSendBufSize = DEFAULT_SYS_SEND_BUFFER_SIZE;
	sysRecvBufSize = DEFAULT_SYS_RECEIVE_BUFFER_SIZE;
	highWaterMark = DEFAULT_HIGH_WATER_MARK;
	pChannel = 0;
}

ChannelConfig::~ChannelConfig()
{
}

void ChannelConfig::setGuaranteedOutputBuffers( UInt64 value )
{
	if ( value <= 0 ) {}
	else if ( value > 0xFFFFFFFF )
		guaranteedOutputBuffers = 0xFFFFFFFF;
	else
		guaranteedOutputBuffers = ( UInt32 )value;
}

void ChannelConfig::setNumInputBuffers( UInt64 value )
{
	if ( value == 0 ) {}
	else
	{
		numInputBuffers = value > 0xFFFFFFFF ? 0xFFFFFFFF : ( UInt32 )value;
	}
}

ServerConfig::ServerConfig( RsslConnectionTypes type ) :
	name(),
	interfaceName(DEFAULT_INTERFACE_NAME),
	compressionType(DEFAULT_COMPRESSION_TYPE),
	compressionThreshold(DEFAULT_COMPRESSION_THRESHOLD),
	connectionType(type),
	connectionPingTimeout(DEFAULT_CONNECTION_PINGTIMEOUT),
	connectionMinPingTimeout(DEFAULT_CONNECTION_MINPINGTIMEOUT),
	guaranteedOutputBuffers(DEFAULT_PROVIDER_GUARANTEED_OUTPUT_BUFFERS),
	numInputBuffers(DEFAULT_NUM_INPUT_BUFFERS),
	sysSendBufSize(DEFAULT_PROVIDER_SYS_SEND_BUFFER_SIZE),
	sysRecvBufSize(DEFAULT_PROVIDER_SYS_RECEIVE_BUFFER_SIZE),
	highWaterMark(DEFAULT_HIGH_WATER_MARK)
{

}

ServerConfig::~ServerConfig()
{

}

void ServerConfig::clear()
{
}

void ServerConfig::setGuaranteedOutputBuffers(UInt64 value)
{
	if (value != 0)
	{
		guaranteedOutputBuffers = value > 0xFFFFFFFF ? 0xFFFFFFFF : (UInt32)value;
	}
}

void ServerConfig::setNumInputBuffers(UInt64 value)
{
	if (value != 0)
	{
		numInputBuffers = value > 0xFFFFFFFF ? 0xFFFFFFFF : (UInt32)value;
	}
}

SocketChannelConfig::SocketChannelConfig(const EmaString& defaultHostName, const EmaString& defaultServiceName, RsslConnectionTypes connType) :
	ChannelConfig(connType),
	hostName(defaultHostName),
	serviceName(defaultServiceName),
	defaultHostName(defaultHostName),
	defaultServiceName(defaultServiceName),
	tcpNodelay(DEFAULT_TCP_NODELAY),
	objectName(DEFAULT_OBJECT_NAME),
	sslCAStore(DEFAULT_SSL_CA_STORE),
	encryptedConnectionType(RSSL_CONN_TYPE_INIT),
	securityProtocol(RSSL_ENC_TLSV1_2),
	enableSessionMgnt(RSSL_FALSE),
	location(DEFAULT_EDP_RT_LOCATION)
{
}

SocketChannelConfig::~SocketChannelConfig()
{
}

void SocketChannelConfig::clear()
{
	ChannelConfig::clear();

	hostName = defaultHostName;
	serviceName = defaultServiceName;
	tcpNodelay = DEFAULT_TCP_NODELAY;
	objectName = DEFAULT_OBJECT_NAME;
	securityProtocol = RSSL_ENC_TLSV1_2;
	enableSessionMgnt = RSSL_FALSE;
	location = DEFAULT_EDP_RT_LOCATION;
}

ChannelConfig::ChannelType SocketChannelConfig::getType() const
{
	return ChannelConfig::SocketChannelEnum;
}

SocketServerConfig::SocketServerConfig(const EmaString& defaultServiceName) :
ServerConfig(RSSL_CONN_TYPE_SOCKET),
serviceName(defaultServiceName),
tcpNodelay(DEFAULT_TCP_NODELAY)
{
}

SocketServerConfig::~SocketServerConfig()
{
}

void SocketServerConfig::clear()
{
	tcpNodelay = DEFAULT_TCP_NODELAY;
}

ServerConfig::ServerType SocketServerConfig::getType() const
{
	return ServerConfig::SocketChannelEnum;
}

ReliableMcastChannelConfig::ReliableMcastChannelConfig() :
	ChannelConfig( RSSL_CONN_TYPE_RELIABLE_MCAST ),
	recvAddress( DEFAULT_CONS_MCAST_CFGSTRING ),
	recvServiceName( DEFAULT_CONS_MCAST_CFGSTRING ),
	unicastServiceName( DEFAULT_CONS_MCAST_CFGSTRING ),
	sendAddress( DEFAULT_CONS_MCAST_CFGSTRING ),
	sendServiceName( DEFAULT_CONS_MCAST_CFGSTRING ),
	hsmInterface( DEFAULT_CONS_MCAST_CFGSTRING ),
	tcpControlPort( DEFAULT_CONS_MCAST_CFGSTRING ),
	hsmMultAddress( DEFAULT_CONS_MCAST_CFGSTRING ),
	hsmPort( DEFAULT_CONS_MCAST_CFGSTRING ),
	hsmInterval( 0 ),
	packetTTL( DEFAULT_PACKET_TTL ),
	ndata( DEFAULT_NDATA ),
	nmissing( DEFAULT_NMISSING ),
	nrreq( DEFAULT_NREQ ),
	pktPoolLimitHigh( DEFAULT_PKT_POOLLIMIT_HIGH ),
	pktPoolLimitLow( DEFAULT_PKT_POOLLIMIT_LOW ),
	tdata( DEFAULT_TDATA ),
	trreq( DEFAULT_TRREQ ),
	twait( DEFAULT_TWAIT ),
	tbchold( DEFAULT_TBCHOLD ),
	tpphold( DEFAULT_TPPHOLD ),
	userQLimit( DEFAULT_USER_QLIMIT ),
	disconnectOnGap( RSSL_FALSE )
{
}

ReliableMcastChannelConfig::~ReliableMcastChannelConfig()
{
}

void ReliableMcastChannelConfig::setPacketTTL( UInt64 value )
{
	if ( value > 255 )
		packetTTL = 255;
	else if ( value < DEFAULT_PACKET_TTL )
		packetTTL = DEFAULT_PACKET_TTL;
	else
		packetTTL = ( RsslUInt8 ) value;
}

void ReliableMcastChannelConfig::setHsmInterval( UInt64 value )
{
	if ( value > 0 )
		hsmInterval = value > 0xFFFF ? 0xFFFF : ( UInt16 )value;
}

void ReliableMcastChannelConfig::setNdata( UInt64 value )
{
	if ( value > 0xFFFFF )
		ndata = 0xFFFFF;
	else if ( value < DEFAULT_NDATA )
		ndata = DEFAULT_NDATA;
	else
		ndata = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setNmissing( UInt64 value )
{
	if ( value > 0xFFFF )
		nmissing = 0xFFFF;
	else if ( value < DEFAULT_NMISSING )
		nmissing = DEFAULT_NMISSING;
	else
		nmissing = ( RsslUInt16 ) value;
}

void ReliableMcastChannelConfig::setNrreq( UInt64 value )
{
	if ( value > 0xFFFFF )
		nrreq = 0xFFFFF;
	else if ( value < DEFAULT_NREQ )
		nrreq = DEFAULT_NREQ;
	else
		nrreq = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setTdata( UInt64 value )
{
	if ( value > 0xFFFFF )
		tdata = 0xFFFFF;
	else if ( value < DEFAULT_TDATA )
		tdata = DEFAULT_TDATA;
	else
		tdata = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setTrreq( UInt64 value )
{
	if ( value > 0xFFFFF )
		trreq = 0xFFFFF;
	else if ( value < DEFAULT_TRREQ )
		trreq = DEFAULT_TRREQ;
	else
		trreq = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setPktPoolLimitHigh( UInt64 value )
{
	if ( value > 0xFFFFF )
		pktPoolLimitHigh = 0xFFFFF;
	else if ( value < DEFAULT_PKT_POOLLIMIT_HIGH )
		pktPoolLimitHigh = DEFAULT_PKT_POOLLIMIT_HIGH;
	else
		pktPoolLimitHigh = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setPktPoolLimitLow( UInt64 value )
{
	if ( value > 0xFFFFF )
		pktPoolLimitLow = 0xFFFFF;
	else if ( value < DEFAULT_PKT_POOLLIMIT_LOW )
		pktPoolLimitLow = DEFAULT_PKT_POOLLIMIT_LOW;
	else
		pktPoolLimitLow = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setTwait( UInt64 value )
{
	if ( value > 0xFFFFF )
		twait = 0xFFFFF;
	else if ( value < DEFAULT_TWAIT )
		twait = DEFAULT_TWAIT;
	else
		twait = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setTbchold( UInt64 value )
{
	if ( value > 0xFFFFF )
		tbchold = 0xFFFFF;
	else if ( value < DEFAULT_TBCHOLD )
		tbchold = DEFAULT_TBCHOLD;
	else
		tbchold = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setTpphold( UInt64 value )
{
	if ( value > 0xFFFFF )
		tpphold = 0xFFFFF;
	else if ( value < DEFAULT_TPPHOLD )
		tpphold = DEFAULT_TPPHOLD;
	else
		tpphold = ( RsslUInt32 ) value;
}

void ReliableMcastChannelConfig::setUserQLimit( UInt64 value )
{
	if ( value > 0xFFFF )
		userQLimit = 0xFFFF;
	else if ( value < DEFAULT_USER_QLIMIT )
		userQLimit = DEFAULT_USER_QLIMIT;
	else
		userQLimit = ( RsslUInt32 ) value;
}


void ReliableMcastChannelConfig::clear()
{
	ChannelConfig::clear();
	recvAddress = DEFAULT_CONS_MCAST_CFGSTRING;
	recvServiceName = DEFAULT_CONS_MCAST_CFGSTRING;
	unicastServiceName = DEFAULT_CONS_MCAST_CFGSTRING;
	sendAddress = DEFAULT_CONS_MCAST_CFGSTRING;
	sendServiceName = DEFAULT_CONS_MCAST_CFGSTRING;
	hsmInterface = DEFAULT_CONS_MCAST_CFGSTRING;
	tcpControlPort = DEFAULT_CONS_MCAST_CFGSTRING;
	hsmMultAddress = DEFAULT_CONS_MCAST_CFGSTRING;
	hsmPort = DEFAULT_CONS_MCAST_CFGSTRING;
	hsmInterval = 0;
	packetTTL	= DEFAULT_PACKET_TTL;
	ndata = DEFAULT_NDATA;
	nmissing = DEFAULT_NMISSING;
	nrreq = DEFAULT_NREQ;
	pktPoolLimitHigh = DEFAULT_PKT_POOLLIMIT_HIGH;
	pktPoolLimitLow = DEFAULT_PKT_POOLLIMIT_LOW;
	tdata = DEFAULT_TDATA;
	trreq = DEFAULT_TRREQ;
	twait = DEFAULT_TWAIT;
	tbchold = DEFAULT_TBCHOLD;
	tpphold = DEFAULT_TPPHOLD;
	userQLimit = DEFAULT_USER_QLIMIT;
	disconnectOnGap = RSSL_FALSE;
}

ChannelConfig::ChannelType ReliableMcastChannelConfig::getType() const
{
	return ChannelConfig::ReliableMcastChannelEnum;
}

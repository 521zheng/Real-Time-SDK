/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#include "OmmBaseImpl.h"
#include "LoginCallbackClient.h"
#include "ChannelCallbackClient.h"
#include "DirectoryCallbackClient.h"

#include "OmmInaccessibleLogFileException.h"
#include "OmmInvalidHandleException.h"
#include "OmmSystemException.h"
#include "ExceptionTranslator.h"

#include "GetTime.h"

#ifdef WIN32
#include <windows.h>
#else
#include <sys/time.h>
#endif

#include <new>

#define	EMA_BIG_STR_BUFF_SIZE (1024*4)

using namespace thomsonreuters::ema::access;

#ifdef USING_POLL
int OmmBaseImpl::addFd( int fd, short events = POLLIN )
{
	if ( _eventFdsCount == _eventFdsCapacity )
	{
		_eventFdsCapacity *= 2;
		pollfd* tmp( new pollfd[ _eventFdsCapacity ] );
		for ( int i = 0; i < _eventFdsCount; ++i )
			tmp[ i ] = _eventFds[ i ];
		delete [] _eventFds;
		_eventFds = tmp;
	}
	_eventFds[ _eventFdsCount ].fd = fd;
	_eventFds[ _eventFdsCount ].events = events;

	return _eventFdsCount++;
}

void OmmBaseImpl::removeFd( int fd )
{
	_pipeReadEventFdsIdx = -1;
	for ( int i = 0; i < _eventFdsCount; ++i )
		if ( _eventFds[ i ].fd == fd )
		{
			if ( i < _eventFdsCount - 1 )
				_eventFds[ i ] = _eventFds[ _eventFdsCount - 1 ];
			--_eventFdsCount;
			break;
		}
}
#endif

OmmBaseImpl::OmmBaseImpl( ActiveConfig& activeConfig ) :
	_activeConfig( activeConfig ),
	_userLock(),
	_pipeLock(),
	_reactorDispatchErrorInfo(),
	_state( NotInitializedEnum ),
	_pRsslReactor( 0 ),
	_pChannelCallbackClient( 0 ),
	_pLoginCallbackClient( 0 ),
	_pDirectoryCallbackClient( 0 ),
	_pDictionaryCallbackClient( 0 ),
	_pItemCallbackClient( 0 ),
	_pLoggerClient( 0 ),
	_pipe(),
	_pipeWriteCount( 0 ),
	_atExit( false ),
	_eventTimedOut( false ),
	_bMsgDispatched( false ),
	_bEventReceived( false ),
	_pErrorClientHandler( 0 ),
	_theTimeOuts()
{
	clearRsslErrorInfo( &_reactorDispatchErrorInfo );
}

OmmBaseImpl::OmmBaseImpl( ActiveConfig& activeConfig, OmmConsumerErrorClient& client ) :
	_activeConfig( activeConfig ),
	_userLock(),
	_pipeLock(),
	_reactorDispatchErrorInfo(),
	_state( NotInitializedEnum ),
	_pRsslReactor( 0 ),
	_pChannelCallbackClient( 0 ),
	_pLoginCallbackClient( 0 ),
	_pDirectoryCallbackClient( 0 ),
	_pDictionaryCallbackClient( 0 ),
	_pItemCallbackClient( 0 ),
	_pLoggerClient( 0 ),
	_pipe(),
	_pipeWriteCount( 0 ),
	_atExit( false ),
	_eventTimedOut( false ),
	_bMsgDispatched( false ),
	_bEventReceived( false ),
	_pErrorClientHandler( 0 ),
	_theTimeOuts()
{
	try
	{
		_pErrorClientHandler = new ErrorClientHandler( client );
	}
	catch ( std::bad_alloc )
	{
		client.onMemoryExhaustion( "Failed to allocate memory in OmmBaseImpl( ActiveConfig& , OmmConsumerErrorClient& )" );
	}

	clearRsslErrorInfo( &_reactorDispatchErrorInfo );
}

OmmBaseImpl::OmmBaseImpl( ActiveConfig& activeConfig, OmmProviderErrorClient& client ) :
	_activeConfig( activeConfig ),
	_userLock(),
	_pipeLock(),
	_reactorDispatchErrorInfo(),
	_state( NotInitializedEnum ),
	_pRsslReactor( 0 ),
	_pChannelCallbackClient( 0 ),
	_pLoginCallbackClient( 0 ),
	_pDirectoryCallbackClient( 0 ),
	_pDictionaryCallbackClient( 0 ),
	_pItemCallbackClient( 0 ),
	_pLoggerClient( 0 ),
	_pipe(),
	_pipeWriteCount( 0 ),
	_atExit( false ),
	_eventTimedOut( false ),
	_bMsgDispatched( false ),
	_bEventReceived( false ),
	_pErrorClientHandler( 0 ),
	_theTimeOuts()
{
	try
	{
		_pErrorClientHandler = new ErrorClientHandler( client );
	}
	catch ( std::bad_alloc )
	{
		client.onMemoryExhaustion( "Failed to allocate memory in OmmBaseImpl( ActiveConfig& , OmmNiProviderErrorClient& )" );
	}

	clearRsslErrorInfo( &_reactorDispatchErrorInfo );
}

OmmBaseImpl::~OmmBaseImpl()
{
	if ( _pErrorClientHandler )
		delete _pErrorClientHandler;
}

void OmmBaseImpl::readConfig( EmaConfigImpl* pConfigImpl )
{
	UInt64 id = OmmBaseImplMap<OmmBaseImpl>::add( this );

	_activeConfig.configuredName = pConfigImpl->getConfiguredName();
	_activeConfig.instanceName = _activeConfig.configuredName;
	_activeConfig.instanceName.append( "_" ).append( id );

	const UInt32 maxUInt32( 0xFFFFFFFF );
	UInt64 tmp;
	EmaString instanceNodeName( pConfigImpl->getInstanceNodeName() );
	instanceNodeName.append( _activeConfig.configuredName ).append( "|" );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "ItemCountHint", tmp ) )
		_activeConfig.itemCountHint = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "ServiceCountHint", tmp ) )
		_activeConfig.serviceCountHint = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "RequestTimeout", tmp ) )
		_activeConfig.requestTimeout = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	if ( pConfigImpl->get< UInt64 >( instanceNodeName + "LoginRequestTimeOut", tmp ) )
		_activeConfig.loginRequestTimeOut = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	pConfigImpl->get<Int64>( instanceNodeName + "DispatchTimeoutApiThread", _activeConfig.dispatchTimeoutApiThread );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "CatchUnhandledException", tmp ) )
		_activeConfig.catchUnhandledException = static_cast<UInt32>( tmp > 0 ? true : false );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "MaxDispatchCountApiThread", tmp ) )
		_activeConfig.maxDispatchCountApiThread = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	if ( pConfigImpl->get<UInt64>( instanceNodeName + "MaxDispatchCountUserThread", tmp ) )
		_activeConfig.maxDispatchCountUserThread = static_cast<UInt32>( tmp > maxUInt32 ? maxUInt32 : tmp );

	pConfigImpl->get<Int64>( instanceNodeName + "PipePort", _activeConfig.pipePort );

	pConfigImpl->getLoggerName( _activeConfig.configuredName, _activeConfig.loggerConfig.loggerName );

	_activeConfig.loggerConfig.minLoggerSeverity = OmmLoggerClient::SuccessEnum;
	_activeConfig.loggerConfig.loggerFileName = "emaLog";
	_activeConfig.loggerConfig.loggerType = OmmLoggerClient::FileEnum;
	_activeConfig.loggerConfig.includeDateInLoggerOutput = false;

	if ( _activeConfig.loggerConfig.loggerName.length() )
	{
		EmaString loggerNodeName( "LoggerGroup|LoggerList|Logger." );
		loggerNodeName.append( _activeConfig.loggerConfig.loggerName ).append( "|" );

		EmaString name;
		if ( !pConfigImpl->get< EmaString >( loggerNodeName + "Name", name ) )
		{
			EmaString errorMsg( "no configuration exists for consumer logger [" );
			errorMsg.append( loggerNodeName ).append( "]; will use logger defaults" );
			pConfigImpl->appendConfigError( errorMsg, OmmLoggerClient::ErrorEnum );
		}

		pConfigImpl->get<OmmLoggerClient::LoggerType>( loggerNodeName + "LoggerType", _activeConfig.loggerConfig.loggerType );

		if ( _activeConfig.loggerConfig.loggerType == OmmLoggerClient::FileEnum )
			pConfigImpl->get<EmaString>( loggerNodeName + "FileName", _activeConfig.loggerConfig.loggerFileName );

		pConfigImpl->get<OmmLoggerClient::Severity>( loggerNodeName + "LoggerSeverity", _activeConfig.loggerConfig.minLoggerSeverity );

		UInt64 idilo( 0 );
		if ( pConfigImpl->get< UInt64 >( loggerNodeName + "IncludeDateInLoggerOutput", idilo ) )
			_activeConfig.loggerConfig.includeDateInLoggerOutput = idilo == 1 ? true : false ;
	}
	else
		_activeConfig.loggerConfig.loggerName.set( "Logger" );

	if ( ProgrammaticConfigure* ppc = pConfigImpl->getProgrammaticConfigure() )
	{
		ppc->retrieveLoggerConfig( _activeConfig.loggerConfig.loggerName , _activeConfig );
	}

	EmaString channelSet;
	EmaString channelName;

	pConfigImpl->getChannelName( _activeConfig.configuredName, channelName );

	if ( channelName.trimWhitespace().empty() )
	{
		EmaString nodeName( "ConsumerGroup|ConsumerList|Consumer." );
		nodeName.append( _activeConfig.configuredName );
		nodeName.append( "|ChannelSet" );

		if ( pConfigImpl->get<EmaString>( nodeName, channelSet ) )
		{
			char* pToken = NULL;
			pToken = strtok( const_cast<char*>( channelSet.c_str() ), "," );
			do
			{
				if ( pToken )
				{
					channelName = pToken;
					ChannelConfig* newChannelConfig = readChannelConfig( pConfigImpl, ( channelName.trimWhitespace() ) );
					_activeConfig.configChannelSet.push_back( newChannelConfig );

				}

				pToken = strtok( NULL, "," );
			}
			while ( pToken != NULL );
		}
		else
		{
			useDefaultConfigValues( EmaString( "Channel" ), pConfigImpl->getUserSpecifiedHostname(), pConfigImpl->getUserSpecifiedPort().userSpecifiedValue );
		}
	}
	else
	{
		ChannelConfig* newChannelConfig = readChannelConfig( pConfigImpl, ( channelName.trimWhitespace() ) );
		_activeConfig.configChannelSet.push_back( newChannelConfig );
	}

	if ( ProgrammaticConfigure* ppc  = pConfigImpl->getProgrammaticConfigure() )
	{
		ppc->retrieveCommonConfig( _activeConfig.configuredName, _activeConfig );
		bool isProgmaticCfgChannelName = ppc->getActiveChannelName( _activeConfig.configuredName, channelName.trimWhitespace() );
		bool isProgramatiCfgChannelset = ppc->getActiveChannelSet( _activeConfig.configuredName, channelSet.trimWhitespace() );
		unsigned int posInProgCfg  = 0;

		if ( isProgmaticCfgChannelName )
		{
			_activeConfig.clearChannelSet();
			ChannelConfig* fileChannelConfig = readChannelConfig( pConfigImpl, ( channelName.trimWhitespace() ) );
			ppc->retrieveChannelConfig( channelName.trimWhitespace(), _activeConfig, pConfigImpl->getUserSpecifiedHostname().length() > 0, fileChannelConfig );
			if ( !( ActiveConfig::findChannelConfig( _activeConfig.configChannelSet, channelName.trimWhitespace(), posInProgCfg ) ) )
				_activeConfig.configChannelSet.push_back( fileChannelConfig );
			else
			{
				if ( fileChannelConfig )
					delete fileChannelConfig;
			}
		}
		else if ( isProgramatiCfgChannelset )
		{
			_activeConfig.configChannelSet.clear();
			char* pToken = NULL;
			pToken = strtok( const_cast<char*>( channelSet.c_str() ), "," );
			while ( pToken != NULL )
			{
				channelName = pToken;
				ChannelConfig* fileChannelConfig = readChannelConfig( pConfigImpl, ( channelName.trimWhitespace() ) );
				ppc->retrieveChannelConfig( channelName.trimWhitespace(), _activeConfig, pConfigImpl->getUserSpecifiedHostname().length() > 0, fileChannelConfig );
				if ( !( ActiveConfig::findChannelConfig( _activeConfig.configChannelSet, channelName.trimWhitespace(), posInProgCfg ) ) )
					_activeConfig.configChannelSet.push_back( fileChannelConfig );
				else
				{
					if ( fileChannelConfig )
						delete fileChannelConfig;
				}

				pToken = strtok( NULL, "," );
			}
		}
	}

	if ( _activeConfig.configChannelSet.size() == 0 )
	{
		EmaString channelName( "Channel" );
		useDefaultConfigValues( channelName, pConfigImpl->getUserSpecifiedHostname(), pConfigImpl->getUserSpecifiedPort().userSpecifiedValue );
	}

	_activeConfig.pRsslRDMLoginReq = pConfigImpl->getLoginReq();

	catchUnhandledException( _activeConfig.catchUnhandledException );
}

void OmmBaseImpl::useDefaultConfigValues( const EmaString& channelName, const EmaString& host, const EmaString& port )
{
	SocketChannelConfig* newChannelConfig =  0;
	try
	{
		newChannelConfig = new SocketChannelConfig( getActiveConfig().defaultServiceName() );
		if ( host.length() )
			newChannelConfig->hostName = host;

		if ( port.length() )
			newChannelConfig->serviceName = port;

		newChannelConfig->name.set( channelName );
		_activeConfig.configChannelSet.push_back( newChannelConfig );
	}
	catch ( std::bad_alloc )
	{
		const char* temp( "Failed to allocate memory for SocketChannelConfig." );
		throwMeeException( temp );
		return;
	}
}

ChannelConfig* OmmBaseImpl::readChannelConfig( EmaConfigImpl* pConfigImpl, const EmaString&  channelName )
{
	ChannelConfig* newChannelConfig = NULL;
	UInt32 maxUInt32( 0xFFFFFFFF );
	EmaString channelNodeName( "ChannelGroup|ChannelList|Channel." );
	channelNodeName.append( channelName ).append( "|" );

	RsslConnectionTypes channelType;
	if ( !pConfigImpl->get<RsslConnectionTypes>( channelNodeName + "ChannelType", channelType ) ||
	     pConfigImpl->getUserSpecifiedHostname().length() > 0 )
		channelType = RSSL_CONN_TYPE_SOCKET;

	switch ( channelType )
	{
	case RSSL_CONN_TYPE_SOCKET:
	{
		SocketChannelConfig* socketChannelCfg = NULL;
		try
		{
		  socketChannelCfg = new SocketChannelConfig( getActiveConfig().defaultServiceName() );
			newChannelConfig = socketChannelCfg;
		}
		catch ( std::bad_alloc )
		{
			const char* temp( "Failed to allocate memory for SocketChannelConfig. (std::bad_alloc)" );
			throwMeeException( temp );
			return 0;
		}

		if ( !socketChannelCfg )
		{
			const char* temp = "Failed to allocate memory for SocketChannelConfig. (null ptr)";
			throwMeeException( temp );
			return 0;
		}

		EmaString  tmp = pConfigImpl->getUserSpecifiedHostname() ;
		if ( tmp.length() )
			socketChannelCfg->hostName = tmp;
		else
			pConfigImpl->get< EmaString >( channelNodeName + "Host", socketChannelCfg->hostName );

		PortSetViaFunctionCall psvfc( pConfigImpl->getUserSpecifiedPort() );
		if ( psvfc.userSet ) {
		  if ( psvfc.userSpecifiedValue.length() )
		    socketChannelCfg->serviceName = psvfc.userSpecifiedValue;
		  else
		    socketChannelCfg->serviceName = _activeConfig.defaultServiceName();
		}
		else
			pConfigImpl->get< EmaString >( channelNodeName + "Port", socketChannelCfg->serviceName );

		UInt64 tempUInt = 1;
		pConfigImpl->get<UInt64>( channelNodeName + "TcpNodelay", tempUInt );
		if ( tempUInt )
			socketChannelCfg->tcpNodelay = RSSL_TRUE;
		else
			socketChannelCfg->tcpNodelay = RSSL_FALSE;

		break;
	}
	case RSSL_CONN_TYPE_HTTP:
	{
		HttpChannelConfig* httpChannelCfg = NULL;
		try
		{
			httpChannelCfg = new HttpChannelConfig();
			newChannelConfig = httpChannelCfg;
		}
		catch ( std::bad_alloc )
		{
			const char* temp( "Failed to allocate memory for HttpChannelConfig. (std::bad_alloc)" );
			throwMeeException( temp );
			return 0;
		}

		if ( !httpChannelCfg )
		{
			const char* temp = "Failed to allocate memory for HttpChannelConfig. (null ptr)";
			throwMeeException( temp );
			return 0;
		}

		pConfigImpl->get<EmaString>( channelNodeName + "Host", httpChannelCfg->hostName );

		pConfigImpl->get<EmaString>( channelNodeName + "Port", httpChannelCfg->serviceName );

		UInt64 tempUInt = 1;
		pConfigImpl->get<UInt64>( channelNodeName + "TcpNodelay", tempUInt );
		if ( !tempUInt )
			httpChannelCfg->tcpNodelay = RSSL_FALSE;
		else
			httpChannelCfg->tcpNodelay = RSSL_TRUE;

		pConfigImpl->get<EmaString>( channelNodeName + "ObjectName", httpChannelCfg->objectName );

		break;
	}
	case RSSL_CONN_TYPE_ENCRYPTED:
	{
		EncryptedChannelConfig* encriptedChannelCfg = NULL;
		try
		{
			encriptedChannelCfg = new EncryptedChannelConfig();
			newChannelConfig = encriptedChannelCfg;
		}
		catch ( std::bad_alloc )
		{
			const char* temp( "Failed to allocate memory for EncryptedChannelConfig. (std::bad_alloc)" );
			throwMeeException( temp );
			return 0;
		}

		if ( !newChannelConfig )
		{
			const char* temp = "Failed to allocate memory for EncryptedChannelConfig. (null ptr)";
			throwMeeException( temp );
			return 0;
		}

		pConfigImpl->get<EmaString>( channelNodeName + "Host", encriptedChannelCfg->hostName );

		pConfigImpl->get<EmaString>( channelNodeName + "Port", encriptedChannelCfg->serviceName );

		UInt64 tempUInt = 1;
		pConfigImpl->get<UInt64>( channelNodeName + "TcpNodelay", tempUInt );
		if ( tempUInt )
			encriptedChannelCfg->tcpNodelay = RSSL_TRUE;
		else
			encriptedChannelCfg->tcpNodelay = RSSL_FALSE;

		pConfigImpl->get<EmaString>( channelNodeName + "ObjectName", encriptedChannelCfg->objectName );

		break;
	}
	case RSSL_CONN_TYPE_RELIABLE_MCAST:
	{
		ReliableMcastChannelConfig* relMcastChannelCfg = NULL;
		try
		{
			relMcastChannelCfg = new ReliableMcastChannelConfig();
			newChannelConfig = relMcastChannelCfg;
		}
		catch ( std::bad_alloc )
		{
			const char* temp( "Failed to allocate memory for ReliableMcastChannelConfig. (std::bad_alloc)" );
			throwMeeException( temp );
			return 0;
		}

		if ( !newChannelConfig )
		{
			const char* temp = "Failed to allocate memory for ReliableMcastChannelConfig. (null ptr)";
			throwMeeException( temp );
			return 0;
		}
		EmaString errorMsg;
		if ( !readReliableMcastConfig( pConfigImpl, channelNodeName, relMcastChannelCfg, errorMsg ) )
		{
			throwIceException( errorMsg );
			return 0;
		}
		break;
	}
	default:
	{
		EmaString temp( "Not supported channel type. Type = " );
		temp.append( ( UInt32 )channelType );
		throwIueException( temp );
		return 0;
	}
	}

	newChannelConfig->name = channelName;

	pConfigImpl->get<EmaString>( channelNodeName + "InterfaceName", newChannelConfig->interfaceName );

	UInt64 tempUInt = 0;
	if ( channelType != RSSL_CONN_TYPE_RELIABLE_MCAST )
	{
		pConfigImpl->get<RsslCompTypes>( channelNodeName + "CompressionType", newChannelConfig->compressionType );

		tempUInt = 0;
		if ( pConfigImpl->get<UInt64>( channelNodeName + "CompressionThreshold", tempUInt ) )
		{
			newChannelConfig->compressionThreshold = tempUInt > maxUInt32 ? maxUInt32 : ( UInt32 )tempUInt;
		}
	}

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "GuaranteedOutputBuffers", tempUInt ) )
		newChannelConfig->setGuaranteedOutputBuffers( tempUInt );

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "NumInputBuffers", tempUInt ) )
		newChannelConfig->setNumInputBuffers( tempUInt );

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "ConnectionPingTimeout", tempUInt ) )
		newChannelConfig->connectionPingTimeout = tempUInt > maxUInt32 ? maxUInt32 : ( UInt32 )tempUInt;

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "SysRecvBufSize", tempUInt ) )
		newChannelConfig->sysRecvBufSize = tempUInt > maxUInt32 ? maxUInt32 : ( UInt32 )tempUInt;

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "SysSendBufSize", tempUInt ) )
		newChannelConfig->sysSendBufSize = tempUInt > maxUInt32 ? maxUInt32 : ( UInt32 )tempUInt;

	tempUInt = 0;
	if ( pConfigImpl->get<UInt64>( channelNodeName + "HighWaterMark", tempUInt ) )
		newChannelConfig->highWaterMark = tempUInt > maxUInt32 ? maxUInt32 : (UInt32) tempUInt;

	Int64 tempInt = -1;
	if ( pConfigImpl->get<Int64>( channelNodeName + "ReconnectAttemptLimit", tempInt ) )
		newChannelConfig->setReconnectAttemptLimit( tempInt );

	tempInt = 0;
	if ( pConfigImpl->get<Int64>( channelNodeName + "ReconnectMinDelay", tempInt ) )
		newChannelConfig->setReconnectMinDelay( tempInt );

	tempInt = 0;
	if ( pConfigImpl->get<Int64>( channelNodeName + "ReconnectMaxDelay", tempInt ) )
		newChannelConfig->setReconnectMaxDelay( tempInt );

	pConfigImpl->get<EmaString>( channelNodeName + "XmlTraceFileName", newChannelConfig->xmlTraceFileName );

	tempInt = 0;
	pConfigImpl->get<Int64>( channelNodeName + "XmlTraceMaxFileSize", tempInt );
	if ( tempInt > 0 )
		newChannelConfig->xmlTraceMaxFileSize = tempInt;

	tempUInt = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceToFile", tempUInt );
	if ( tempUInt > 0 )
		newChannelConfig->xmlTraceToFile = true;

	tempUInt = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceToStdout", tempUInt );
	newChannelConfig->xmlTraceToStdout = tempUInt > 0 ? true : false;

	tempUInt = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceToMultipleFiles", tempUInt );
	if ( tempUInt > 0 )
		newChannelConfig->xmlTraceToMultipleFiles = true;

	tempUInt = 1;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceWrite", tempUInt );
	if ( tempUInt == 0 )
		newChannelConfig->xmlTraceWrite = false;

	tempUInt = 1;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceRead", tempUInt );
	if ( tempUInt == 0 )
		newChannelConfig->xmlTraceRead = false;

	tempUInt = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTracePing", tempUInt );
	newChannelConfig->xmlTracePing = tempUInt == 0 ? false : true;

	tempUInt = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "XmlTraceHex", tempUInt );
	newChannelConfig->xmlTraceHex = tempUInt == 0 ? false : true;

	tempUInt = 1;
	pConfigImpl->get<UInt64>( channelNodeName + "MsgKeyInUpdates", tempUInt );
	if ( tempUInt == 0 )
		newChannelConfig->msgKeyInUpdates = false;

	return newChannelConfig;
}

bool OmmBaseImpl::readReliableMcastConfig( EmaConfigImpl* pConfigImpl, const EmaString& channNodeName, ReliableMcastChannelConfig* relMcastChannelCfg, EmaString& errorText )
{
	EmaString channelNodeName( channNodeName );

	pConfigImpl->get<EmaString>( channelNodeName + "RecvAddress", relMcastChannelCfg->recvAddress );
	if ( relMcastChannelCfg->recvAddress.empty() )
	{
		errorText.clear();
		errorText.append( "Invalid Channel Configuration for ChannelType [RSSL_RELIABLE_MCAST]. Missing required parameter [RecvAddress]." );
		return false;
	}

	pConfigImpl->get<EmaString>( channelNodeName + "RecvPort", relMcastChannelCfg->recvServiceName );
	if ( relMcastChannelCfg->recvServiceName.empty() )
	{
		errorText.clear();
		errorText.append( "Invalid Channel Configuration for ChannelType [RSSL_RELIABLE_MCAST]. Missing required parameter [RecvPort]." );
		return false;
	}

	pConfigImpl->get<EmaString>( channelNodeName + "UnicastPort", relMcastChannelCfg->unicastServiceName );
	if ( relMcastChannelCfg->unicastServiceName.empty() )
	{
		errorText.clear();
		errorText.append( "Invalid Channel Configuration for ChannelType [RSSL_RELIABLE_MCAST]. Missing required parameter [UnicastPort]." );
		return false;
	}

	pConfigImpl->get<EmaString>( channelNodeName + "SendAddress", relMcastChannelCfg->sendAddress );
	if ( relMcastChannelCfg->sendAddress.empty() )
	{
		errorText.clear();
		errorText.append( "Invalid Channel Configuration for ChannelType [RSSL_RELIABLE_MCAST]. Missing required parameter [SendAddress]." );
		return false;
	}

	pConfigImpl->get<EmaString>( channelNodeName + "SendPort", relMcastChannelCfg->sendServiceName );
	if ( relMcastChannelCfg->sendServiceName.empty() )
	{
		errorText.clear();
		errorText.append( "Invalid Channel Configuration for ChannelType [RSSL_RELIABLE_MCAST]. Missing required parameter [SendPort]." );
		return false;
	}

	UInt64 tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "DisconnectOnGap", tempUIntval );
	relMcastChannelCfg->disconnectOnGap = ( tempUIntval ) ? true : false;

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "PacketTTL", tempUIntval );
	relMcastChannelCfg->setPacketTTL( tempUIntval );

	pConfigImpl->get<EmaString>( channelNodeName + "HsmInterface", relMcastChannelCfg->hsmInterface );
	pConfigImpl->get<EmaString>( channelNodeName + "HsmMultAddress", relMcastChannelCfg->hsmMultAddress );
	pConfigImpl->get<EmaString>( channelNodeName + "HsmPort", relMcastChannelCfg->hsmPort );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "HsmInterval", tempUIntval );
	relMcastChannelCfg->setHsmInterval( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "ndata", tempUIntval );
	relMcastChannelCfg->setNdata( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "nmissing", tempUIntval );
	relMcastChannelCfg->setNmissing( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "nrreq", tempUIntval );
	relMcastChannelCfg->setNrreq( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "tdata", tempUIntval );
	relMcastChannelCfg->setTdata( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "trreq", tempUIntval );
	relMcastChannelCfg->setTrreq( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "pktPoolLimitHigh", tempUIntval );
	relMcastChannelCfg->setPktPoolLimitHigh( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "pktPoolLimitLow", tempUIntval );
	relMcastChannelCfg->setPktPoolLimitLow( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "twait", tempUIntval );
	relMcastChannelCfg->setTwait( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "tbchold", tempUIntval );
	relMcastChannelCfg->setTbchold( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "tpphold", tempUIntval );
	relMcastChannelCfg->setTpphold( tempUIntval );

	tempUIntval = 0;
	pConfigImpl->get<UInt64>( channelNodeName + "userQLimit", tempUIntval );
	relMcastChannelCfg->setUserQLimit( tempUIntval );

	return true;
}

void OmmBaseImpl::initialize( EmaConfigImpl* configImpl )
{
	try
	{
		_userLock.lock();

		readConfig( configImpl );

		_pLoggerClient = OmmLoggerClient::create( _activeConfig.loggerConfig.loggerType, _activeConfig.loggerConfig.includeDateInLoggerOutput,
			_activeConfig.loggerConfig.minLoggerSeverity, _activeConfig.loggerConfig.loggerFileName );

		readCustomConfig(configImpl);

		configImpl->configErrors().log(_pLoggerClient, _activeConfig.loggerConfig.minLoggerSeverity);

		if ( !_pipe.create() )
		{
			EmaString temp( "Failed to create communication Pipe." );
			if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
			throwIueException( temp );
		}
		else if ( OmmLoggerClient::VerboseEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "Successfully initialized communication Pipe." );
			_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::VerboseEnum, temp );
		}

		RsslError rsslError;
		RsslRet retCode = rsslInitialize( RSSL_LOCK_GLOBAL_AND_CHANNEL, &rsslError );
		if ( retCode != RSSL_RET_SUCCESS )
		{
			EmaString temp( "rsslInitialize() failed while initializing OmmBaseImpl." );
			temp.append( " Internal sysError='" ).append( rsslError.sysError )
			.append( "' Error text='" ).append( rsslError.text ).append( "'. " );

			if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
			throwIueException( temp );
			return;
		}
		else if ( OmmLoggerClient::VerboseEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "Successfully initialized Rssl." );
			_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::VerboseEnum, temp );
		}

		_state = RsslInitilizedEnum;

		RsslCreateReactorOptions reactorOpts;
		RsslErrorInfo rsslErrorInfo;
		clearRsslErrorInfo( &rsslErrorInfo );

		rsslClearCreateReactorOptions( &reactorOpts );

		reactorOpts.userSpecPtr = ( void* )this;

		_pRsslReactor = rsslCreateReactor( &reactorOpts, &rsslErrorInfo );
		if ( !_pRsslReactor )
		{
			EmaString temp( "Failed to initialize OmmBaseImpl (rsslCreateReactor)." );
			temp.append( "' Error Id='" ).append( rsslErrorInfo.rsslError.rsslErrorId )
			.append( "' Internal sysError='" ).append( rsslErrorInfo.rsslError.sysError )
			.append( "' Error Location='" ).append( rsslErrorInfo.errorLocation )
			.append( "' Error Text='" ).append( rsslErrorInfo.rsslError.text ).append( "'. " );
			if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
			throwIueException( temp );
			return;
		}
		else if ( OmmLoggerClient::VerboseEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "Successfully created Reactor." );
			_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::VerboseEnum, temp );
		}

		_state = ReactorInitializedEnum;

#ifdef USING_SELECT
		FD_ZERO( &_readFds );
		FD_ZERO( &_exceptFds );

		FD_SET( _pipe.readFD(), &_readFds );
		FD_SET( _pipe.readFD(), &_exceptFds );
		FD_SET( _pRsslReactor->eventFd, &_readFds );
		FD_SET( _pRsslReactor->eventFd, &_exceptFds );
#else
		_eventFdsCapacity = 8;
		_eventFds = new pollfd[ _eventFdsCapacity ];
		_eventFdsCount = 0;
		_pipeReadEventFdsIdx = addFd( _pipe.readFD() );
		addFd( _pRsslReactor->eventFd );
#endif

		_pLoginCallbackClient = LoginCallbackClient::create( *this );
		_pLoginCallbackClient->initialize();

		createDictionaryCallbackClient( _pDictionaryCallbackClient, *this );

		createDirectoryCallbackClient( _pDirectoryCallbackClient, *this );

		_pItemCallbackClient = ItemCallbackClient::create( *this );
		_pItemCallbackClient->initialize();

		_pChannelCallbackClient = ChannelCallbackClient::create( *this, _pRsslReactor );
		_pChannelCallbackClient->initialize( _pLoginCallbackClient->getLoginRequest(), _pDirectoryCallbackClient->getDirectoryRequest() );

		UInt64 timeOutLengthInMicroSeconds = _activeConfig.loginRequestTimeOut * 1000;
		_eventTimedOut = false;
		TimeOut* loginWatcher( new TimeOut( *this, timeOutLengthInMicroSeconds, &OmmBaseImpl::terminateIf, reinterpret_cast< void* >( this ), true ) );
		while ( ! _atExit && ! _eventTimedOut &&
		        ( _state < LoginStreamOpenOkEnum ) &&
		        ( _state != RsslChannelUpStreamNotOpenEnum ) )
			rsslReactorDispatchLoop( _activeConfig.dispatchTimeoutApiThread, _activeConfig.maxDispatchCountApiThread, _bEventReceived );

		ChannelConfig* pChannelcfg = _activeConfig.findChannelConfig(_pLoginCallbackClient->getActiveChannel());
		if (!pChannelcfg)
			pChannelcfg = _activeConfig.configChannelSet[_activeConfig.configChannelSet.size()-1];

		if ( _eventTimedOut )
		{
			EmaString failureMsg( "login failed (timed out after waiting " );
			failureMsg.append( _activeConfig.loginRequestTimeOut ).append( " milliseconds) for " );
			if ( pChannelcfg->getType() == ChannelConfig::SocketChannelEnum )
			{
				SocketChannelConfig* channelConfig( reinterpret_cast< SocketChannelConfig* >( pChannelcfg ) );
				failureMsg.append( channelConfig->hostName ).append( ":" ).append( channelConfig->serviceName ).append( ")" );
			}
			else if ( pChannelcfg->getType() == ChannelConfig::HttpChannelEnum )
			{
				HttpChannelConfig* channelConfig( reinterpret_cast< HttpChannelConfig* >( pChannelcfg ) );
				failureMsg.append( channelConfig->hostName ).append( ":" ).append( channelConfig->serviceName ).append( ")" );
			}
			else if ( pChannelcfg->getType() == ChannelConfig::EncryptedChannelEnum )
			{
				EncryptedChannelConfig* channelConfig( reinterpret_cast< EncryptedChannelConfig* >( pChannelcfg ) );
				failureMsg.append( channelConfig->hostName ).append( ":" ).append( channelConfig->serviceName ).append( ")" );
			}

			throwIueException( failureMsg );
			return;
		}
		else if ( _state == RsslChannelUpStreamNotOpenEnum )
		{
			if ( timeOutLengthInMicroSeconds != 0 ) loginWatcher->cancel();
			throwIueException( getLoginCallbackClient().getLoginFailureMessage() );
			return;
		}
		else
		{
			if ( timeOutLengthInMicroSeconds != 0 ) loginWatcher->cancel();
			loginWatcher = 0;
		}

		if ( _atExit )
		{
			throwIueException( "Application or user initiated exit while waiting for login response." );
			return;
		}

		loadDirectory();
		loadDictionary();

		if ( isApiDispatching() ) start();
		
		_userLock.unlock();
	}
	catch ( const OmmException& ommException )
	{
		uninitialize( false, true );

		_userLock.unlock();

		if ( hasErrorClientHandler() )
			notifErrorClientHandler( ommException, getErrorClientHandler() );
		else
			throw;
	}
}

ErrorClientHandler& OmmBaseImpl::getErrorClientHandler()
{
	return *_pErrorClientHandler;
}

bool OmmBaseImpl::hasErrorClientHandler() const
{
	return _pErrorClientHandler != 0 ? true : false;
}

EmaList< TimeOut* >& OmmBaseImpl::getTimeOutList()
{
	return _theTimeOuts;
}

Mutex& OmmBaseImpl::getTimeOutMutex()
{
	return _timeOutLock;
}

void OmmBaseImpl::installTimeOut()
{
	pipeWrite();
}

bool OmmBaseImpl::isPipeWritten()
{
	MutexLocker lock( _pipeLock );

	return ( _pipeWriteCount > 0 ? true : false );
}

void OmmBaseImpl::pipeWrite()
{
	MutexLocker lock( _pipeLock );

	if ( ++_pipeWriteCount == 1 )
		_pipe.write( "0", 1 );
}

void OmmBaseImpl::pipeRead()
{
	MutexLocker lock( _pipeLock );

	if ( --_pipeWriteCount == 0 )
	{
		char temp[10];
		_pipe.read( temp, 1 );
	}
}

void OmmBaseImpl::cleanUp()
{
	uninitialize( true, false );
}

void OmmBaseImpl::uninitialize( bool caughtExcep, bool calledFromInit )
{
	OmmBaseImplMap<OmmBaseImpl>::remove(this);

	if ( !calledFromInit ) _userLock.lock();

	if ( _state == NotInitializedEnum )
	{
		if ( !calledFromInit ) _userLock.unlock();
		return;
	}

	if ( isApiDispatching() && !caughtExcep )
	{
		eventReceived();
		msgDispatched();
		pipeWrite();
		stop();
		wait();
	}

	if ( _pRsslReactor )
	{
		if ( _pLoginCallbackClient && !caughtExcep )
			rsslReactorDispatchLoop( 10000, _pLoginCallbackClient->sendLoginClose(), _bEventReceived );

		if ( _pChannelCallbackClient )
			_pChannelCallbackClient->closeChannels();

		RsslErrorInfo rsslErrorInfo;
		clearRsslErrorInfo( &rsslErrorInfo );

		if ( RSSL_RET_SUCCESS != rsslDestroyReactor( _pRsslReactor, &rsslErrorInfo ) )
		{
			if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
			{
				EmaString temp( "Failed to uninitialize OmmBaseImpl( rsslDestroyReactor )." );
				temp.append( "' Error Id='" ).append( rsslErrorInfo.rsslError.rsslErrorId )
				.append( "' Internal sysError='" ).append( rsslErrorInfo.rsslError.sysError )
				.append( "' Error Location='" ).append( rsslErrorInfo.errorLocation )
				.append( "' Error Text='" ).append( rsslErrorInfo.rsslError.text ).append( "'. " );

				_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
			}
		}

		_pRsslReactor = 0;
	}

	ItemCallbackClient::destroy( _pItemCallbackClient );

	DictionaryCallbackClient::destroy( _pDictionaryCallbackClient );

	DirectoryCallbackClient::destroy( _pDirectoryCallbackClient );

	LoginCallbackClient::destroy( _pLoginCallbackClient );

	ChannelCallbackClient::destroy( _pChannelCallbackClient );

	if ( RSSL_RET_SUCCESS != rsslUninitialize() )
	{
		if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "rsslUninitialize() failed while unintializing OmmConsumer." );
			_pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
		}
	}

#ifdef USING_SELECT
	FD_CLR( _pipe.readFD(), &_readFds );
	FD_CLR( _pipe.readFD(), &_exceptFds );
#else
	removeFd( _pipe.readFD() );
	_pipeReadEventFdsIdx = -1;
#endif
	_pipe.close();

	OmmLoggerClient::destroy( _pLoggerClient );

	_state = NotInitializedEnum;

	if ( !calledFromInit ) _userLock.unlock();

#ifdef USING_POLL
	delete[] _eventFds;
#endif
}

void OmmBaseImpl::setAtExit()
{
	_atExit = true;
	eventReceived();
	msgDispatched();
	pipeWrite();
}

Int64 OmmBaseImpl::rsslReactorDispatchLoop( Int64 timeOut, UInt32 count, bool& bMsgDispRcvd )
{
	bMsgDispRcvd = false;

	Int64 startTime = GetTime::getMicros();
	Int64 endTime = 0;
	Int64 nextTimer = 0;

	bool userTimeoutExists( TimeOut::getTimeOutInMicroSeconds( *this, nextTimer ) );
	if ( userTimeoutExists )
	{
		if ( timeOut >= 0 && timeOut < nextTimer )
			userTimeoutExists = false;
		else
			timeOut = nextTimer;
	}

	RsslReactorDispatchOptions dispatchOpts;
	dispatchOpts.pReactorChannel = NULL;
	dispatchOpts.maxMessages = count;

	RsslRet reactorRetCode = RSSL_RET_SUCCESS;

	UInt64 loopCount = 0;
	do
	{
		reactorRetCode = _pRsslReactor ? rsslReactorDispatch( _pRsslReactor, &dispatchOpts, &_reactorDispatchErrorInfo ) : RSSL_RET_SUCCESS;
		++loopCount;
	}
	while ( reactorRetCode > RSSL_RET_SUCCESS && !bMsgDispRcvd && loopCount < 5 );

	if ( reactorRetCode < RSSL_RET_SUCCESS )
	{
		if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "Call to rsslReactorDispatch() failed. Internal sysError='" );
			temp.append( _reactorDispatchErrorInfo.rsslError.sysError )
				.append( "' Error Id " ).append( _reactorDispatchErrorInfo.rsslError.rsslErrorId ).append( "' " )
				.append( "' Error Location='" ).append( _reactorDispatchErrorInfo.errorLocation ).append( "' " )
				.append( "' Error text='" ).append( _reactorDispatchErrorInfo.rsslError.text ).append( "'. " );

			_userLock.lock();
			if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
			_userLock.unlock();
		}

		return -2;
	}

	if ( bMsgDispRcvd )
		return 0;

	endTime = GetTime::getMicros();

	if ( ( timeOut >= 0 ) && ( endTime - startTime >= timeOut ) )
	{
		if ( userTimeoutExists )
			TimeOut::execute( *this );

		return bMsgDispRcvd ? 0 : -1;
	}

	if ( timeOut >= 0 )
	{
		timeOut -= endTime - startTime;
		if ( timeOut < 0 )
			timeOut = 0;
	}

	do
	{
		startTime = endTime;

		Int64 selectRetCode = 1;

#if defined( USING_SELECT )

		fd_set useReadFds = _readFds;
		fd_set useExceptFds = _exceptFds;

		struct timeval selectTime;
		if ( timeOut >= 0 )
		{
			selectTime.tv_sec = static_cast<long>( timeOut / 1000000 );
			selectTime.tv_usec = timeOut % 1000000;
			selectRetCode = select( FD_SETSIZE, &useReadFds, NULL, &useExceptFds, &selectTime );
		}
		else if ( timeOut < 0 )
			selectRetCode = select( FD_SETSIZE, &useReadFds, NULL, &useExceptFds, NULL );

		if ( selectRetCode > 0 && FD_ISSET( _pipe.readFD(), &useReadFds ) )
		{
			pipeRead();
			--selectRetCode;
		}

#elif defined( USING_PPOLL )

		struct timespec ppollTime;

		if ( timeOut >= 0 )
		{
			ppollTime.tv_sec = timeOut / static_cast<long long>( 1e6 );
			ppollTime.tv_nsec = timeOut % static_cast<long long>( 1e6 ) * static_cast<long long>( 1e3 );
			selectRetCode = ppoll( _eventFds, _eventFdsCount, &ppollTime, 0 );
		}
		else if ( timeOut < 0 )
			selectRetCode = ppoll( _eventFds, _eventFdsCount, 0, 0 );

		if ( selectRetCode > 0 )
		{
			if ( _pipeReadEventFdsIdx == -1 )
				for ( int i = 0; i < _eventFdsCount; ++i )
					if ( _eventFds[i].fd == _pipe.readFD() )
					{
						_pipeReadEventFdsIdx = i;
						break;
					}

			if ( _pipeReadEventFdsIdx != -1 )
				if ( _eventFds[_pipeReadEventFdsIdx].revents & POLLIN )
				{
					pipeRead();
					--selectRetCode;
				}
		}

#else
#error "No Implementation for Operating System That Does Not Implement ppoll"
#endif

		if ( selectRetCode > 0 )
		{
			loopCount = 0;
			do
			{
				reactorRetCode = _pRsslReactor ? rsslReactorDispatch( _pRsslReactor, &dispatchOpts, &_reactorDispatchErrorInfo ) : RSSL_RET_SUCCESS;
				++loopCount;
			}
			while ( reactorRetCode > RSSL_RET_SUCCESS && !bMsgDispRcvd && loopCount < 5 );

			if ( reactorRetCode < RSSL_RET_SUCCESS )
			{
				if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				{
					EmaString temp( "Call to rsslReactorDispatch() failed. Internal sysError='" );
					temp.append( _reactorDispatchErrorInfo.rsslError.sysError )
						.append( "' Error Id " ).append( _reactorDispatchErrorInfo.rsslError.rsslErrorId ).append( "' " )
						.append( "' Error Location='" ).append( _reactorDispatchErrorInfo.errorLocation ).append( "' " )
						.append( "' Error text='" ).append( _reactorDispatchErrorInfo.rsslError.text ).append( "'. " );

					_userLock.lock();
					if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
					_userLock.unlock();
				}

				return -2;
			}

			if ( bMsgDispRcvd ) return 0;

			TimeOut::execute( *this );

			if ( bMsgDispRcvd ) return 0;

			endTime = GetTime::getMicros();

			if ( timeOut >= 0 )
			{
				if ( endTime > startTime + timeOut ) return -1;

				timeOut -= ( endTime - startTime );
			}
		}
		else if ( selectRetCode == 0 )
		{
			TimeOut::execute( *this );

			if ( bMsgDispRcvd ) return 0;

			endTime = GetTime::getMicros();

			if ( timeOut >= 0 )
			{
				if ( endTime > startTime + timeOut ) return -1;

				timeOut -= ( endTime - startTime );
			}
		}
		else if ( selectRetCode < 0 )
		{
#ifdef WIN32
			Int64 lastError = WSAGetLastError();
			if ( lastError != WSAEINTR )
			{
				if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				{
					EmaString temp( "Internal Error. Call to select() failed. LastError='" );
					temp.append( lastError ).append( "'. " );

					_userLock.lock();
					if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
					_userLock.unlock();
				}
			}
#else
			if ( errno != EINTR )
			{
				if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
				{
					EmaString temp( "Internal Error. Call to select() failed. LastError='" );
					temp.append( errno ).append( "'. " );

					_userLock.lock();
					if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );
					_userLock.unlock();
				}
			}
#endif
			return -2;
		}
	} while ( true );
}

void OmmBaseImpl::setState( ImplState state )
{
	_state = state;
}

void OmmBaseImpl::closeChannel( RsslReactorChannel* pRsslReactorChannel )
{
	if ( !pRsslReactorChannel ) return;

	RsslErrorInfo rsslErrorInfo;
	clearRsslErrorInfo( &rsslErrorInfo );

	if ( pRsslReactorChannel->socketId != REACTOR_INVALID_SOCKET )
		removeSocket( pRsslReactorChannel->socketId );

	if ( rsslReactorCloseChannel( _pRsslReactor, pRsslReactorChannel, &rsslErrorInfo ) != RSSL_RET_SUCCESS )
	{
		if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		{
			EmaString temp( "Failed to close reactor channel (rsslReactorCloseChannel)." );
			temp.append( "' RsslChannel='" ).append( ( UInt64 )rsslErrorInfo.rsslError.channel )
			.append( "' Error Id='" ).append( rsslErrorInfo.rsslError.rsslErrorId )
			.append( "' Internal sysError='" ).append( rsslErrorInfo.rsslError.sysError )
			.append( "' Error Location='" ).append( rsslErrorInfo.errorLocation )
			.append( "' Error Text='" ).append( rsslErrorInfo.rsslError.text ).append( "'. " );

			_userLock.lock();

			if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, temp );

			_userLock.unlock();
		}
	}

	_pChannelCallbackClient->removeChannel( pRsslReactorChannel );
}

void OmmBaseImpl::handleIue( const EmaString& text )
{
	if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		getOmmLoggerClient().log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, text );

	if ( hasErrorClientHandler() )
		getErrorClientHandler().onInvalidUsage( text );
	else
		throwIueException( text );
}

void OmmBaseImpl::handleIue( const char* text )
{
	if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		getOmmLoggerClient().log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, text );

	if ( hasErrorClientHandler() )
		getErrorClientHandler().onInvalidUsage( text );
	else
		throwIueException( text );
}

void OmmBaseImpl::handleIhe( UInt64 handle, const EmaString& text )
{
	if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		getOmmLoggerClient().log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, text );

	if ( hasErrorClientHandler() )
		getErrorClientHandler().onInvalidHandle( handle, text );
	else
		throwIheException( handle, text );
}

void OmmBaseImpl::handleIhe( UInt64 handle, const char* text )
{
	if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		getOmmLoggerClient().log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, text );

	if ( hasErrorClientHandler() )
		getErrorClientHandler().onInvalidHandle( handle, text );
	else
		throwIheException( handle, text );
}

void OmmBaseImpl::handleMee( const char* text )
{
	if ( OmmLoggerClient::ErrorEnum >= _activeConfig.loggerConfig.minLoggerSeverity )
		getOmmLoggerClient().log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, text );

	if ( hasErrorClientHandler() )
		getErrorClientHandler().onMemoryExhaustion( text );
	else
		throwMeeException( text );
}

ItemCallbackClient& OmmBaseImpl::getItemCallbackClient()
{
	return *_pItemCallbackClient;
}

DictionaryCallbackClient& OmmBaseImpl::getDictionaryCallbackClient()
{
	return *_pDictionaryCallbackClient;
}

DirectoryCallbackClient& OmmBaseImpl::getDirectoryCallbackClient()
{
	return *_pDirectoryCallbackClient;
}

LoginCallbackClient& OmmBaseImpl::getLoginCallbackClient()
{
	return *_pLoginCallbackClient;
}

ChannelCallbackClient& OmmBaseImpl::getChannelCallbackClient()
{
	return *_pChannelCallbackClient;
}

OmmLoggerClient& OmmBaseImpl::getOmmLoggerClient()
{
	return *_pLoggerClient;
}

void OmmBaseImpl::reissue( const ReqMsg& reqMsg, UInt64 handle )
{
	_userLock.lock();

	if ( _pItemCallbackClient ) _pItemCallbackClient->reissue( reqMsg, handle );

	_userLock.unlock();
}

void OmmBaseImpl::unregister( UInt64 handle )
{
	_userLock.lock();

	if ( _pItemCallbackClient ) _pItemCallbackClient->unregister( handle );

	_userLock.unlock();
}

void OmmBaseImpl::submit( const GenericMsg& genericMsg, UInt64 handle )
{
	_userLock.lock();

	if ( _pItemCallbackClient ) _pItemCallbackClient->submit( genericMsg, handle );

	_userLock.unlock();
}

void OmmBaseImpl::submit( const PostMsg& postMsg, UInt64 handle )
{
	_userLock.lock();

	if ( _pItemCallbackClient ) _pItemCallbackClient->submit( postMsg, handle );

	_userLock.unlock();
}

ActiveConfig& OmmBaseImpl::getActiveConfig()
{
	return _activeConfig;
}

void OmmBaseImpl::run()
{
	while ( !Thread::isStopping() && !_atExit )
		rsslReactorDispatchLoop( _activeConfig.dispatchTimeoutApiThread, _activeConfig.maxDispatchCountApiThread, _bEventReceived );
}

int OmmBaseImpl::runLog( void* pExceptionStructure, const char* file, unsigned int line )
{
	char reportBuf[EMA_BIG_STR_BUFF_SIZE * 10];
	if ( retrieveExceptionContext( pExceptionStructure, file, line, reportBuf, EMA_BIG_STR_BUFF_SIZE * 10 ) > 0 )
	{
		_userLock.lock();
		if ( _pLoggerClient ) _pLoggerClient->log( _activeConfig.instanceName, OmmLoggerClient::ErrorEnum, reportBuf );
		_userLock.unlock();
	}

	return 1;
}

RsslReactorCallbackRet OmmBaseImpl::channelCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslReactorChannelEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->getChannelCallbackClient().processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

RsslReactorCallbackRet OmmBaseImpl::loginCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslRDMLoginMsgEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->getLoginCallbackClient().processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

RsslReactorCallbackRet OmmBaseImpl::directoryCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslRDMDirectoryMsgEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->getDirectoryCallbackClient().processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

RsslReactorCallbackRet OmmBaseImpl::dictionaryCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslRDMDictionaryMsgEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->getDictionaryCallbackClient().processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

RsslReactorCallbackRet OmmBaseImpl::itemCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslMsgEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->_pItemCallbackClient->processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

RsslReactorCallbackRet OmmBaseImpl::channelOpenCallback( RsslReactor* pRsslReactor, RsslReactorChannel* pRsslReactorChannel, RsslReactorChannelEvent* pEvent )
{
	static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->eventReceived();
	return static_cast<OmmBaseImpl*>( pRsslReactor->userSpecPtr )->getChannelCallbackClient().processCallback( pRsslReactor, pRsslReactorChannel, pEvent );
}

const EmaString& OmmBaseImpl::getInstanceName() const
{
	return _activeConfig.instanceName;
}

void OmmBaseImpl::msgDispatched( bool value )
{
	_bMsgDispatched = value;
}

void OmmBaseImpl::eventReceived( bool value )
{
	_bEventReceived = value;
}

void OmmBaseImpl::notifErrorClientHandler( const OmmException& ommException, ErrorClientHandler& errorClient )
{
	switch ( ommException.getExceptionType() )
	{
	case OmmException::OmmSystemExceptionEnum :
		errorClient.onSystemError( static_cast<const OmmSystemException&>( ommException ).getSystemExceptionCode(),
		                           static_cast<const OmmSystemException&>( ommException ).getSystemExceptionAddress(),
		                           ommException.getText() );
		break;
	case OmmException::OmmInvalidHandleExceptionEnum:
		errorClient.onInvalidHandle( static_cast<const OmmInvalidHandleException&>( ommException ).getHandle(),
		                             ommException.getText() );
		break;
	case OmmException::OmmInvalidUsageExceptionEnum:
		errorClient.onInvalidUsage( ommException.getText() );
		break;
	case OmmException::OmmInaccessibleLogFileExceptionEnum:
		errorClient.onInaccessibleLogFile( static_cast<const OmmInaccessibleLogFileException&>( ommException ).getFilename(),
		                                   ommException.getText() );
		break;
	case OmmException::OmmMemoryExhaustionExceptionEnum:
		errorClient.onMemoryExhaustion( ommException.getText() );
		break;
	}
}

void OmmBaseImpl::terminateIf( void* object )
{
	OmmBaseImpl* user = reinterpret_cast<OmmBaseImpl*>( object );
	user->_eventTimedOut = true;
}

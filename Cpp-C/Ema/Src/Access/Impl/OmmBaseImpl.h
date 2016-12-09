/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __thomsonreuters_ema_access_OmmBaseImpl_h
#define __thomsonreuters_ema_access_OmmBaseImpl_h

#ifdef WIN32
#define USING_SELECT
#else
#define USING_POLL
#define USING_PPOLL
#endif

#ifdef USING_PPOLL
#include <poll.h>
#endif

#include "rtr/rsslReactor.h"
#include "EmaList.h"
#include "EmaVector.h"
#include "Mutex.h"
#include "Thread.h"
#include "OmmLoggerClient.h"
#include "Pipe.h"
#include "TimeOut.h"
#include "ActiveConfig.h"
#include "ErrorClientHandler.h"
#include "OmmException.h"
#include "OmmBaseImplMap.h"

namespace thomsonreuters {

namespace ema {

namespace access {

class ChannelCallbackClient;
class LoginCallbackClient;
class DirectoryCallbackClient;
class DictionaryCallbackClient;
class ItemCallbackClient;
class OmmLoggerClient;
class TimeOut;
class TunnelStreamRequest;
class EmaConfigImpl;

class OmmBaseImpl : public OmmCommonImpl, public Thread, public TimeOutClient
{
public :

	static RsslReactorCallbackRet channelCallback( RsslReactor*, RsslReactorChannel*, RsslReactorChannelEvent* );

	static RsslReactorCallbackRet loginCallback( RsslReactor*, RsslReactorChannel*, RsslRDMLoginMsgEvent* );

	static RsslReactorCallbackRet directoryCallback( RsslReactor*, RsslReactorChannel*, RsslRDMDirectoryMsgEvent* );

	static RsslReactorCallbackRet dictionaryCallback( RsslReactor*, RsslReactorChannel*, RsslRDMDictionaryMsgEvent* );

	static RsslReactorCallbackRet itemCallback( RsslReactor*, RsslReactorChannel*, RsslMsgEvent* );

	static RsslReactorCallbackRet channelOpenCallback( RsslReactor*, RsslReactorChannel*, RsslReactorChannelEvent* );

	virtual const EmaString& getInstanceName() const;

	virtual void reissue( const ReqMsg&, UInt64 );

	virtual void submit( const GenericMsg&, UInt64 );

	virtual void submit( const PostMsg&, UInt64 handle = 0 );

	virtual void unregister( UInt64 handle );

	virtual void addSocket( RsslSocket ) = 0;

	virtual void removeSocket( RsslSocket ) = 0;

	void closeChannel( RsslReactorChannel* );

	enum ImplState
	{
		NotInitializedEnum = 0,
		RsslInitilizedEnum,
		ReactorInitializedEnum,
		RsslChannelDownEnum,
		RsslChannelUpEnum,
		RsslChannelUpStreamNotOpenEnum,
		LoginStreamOpenSuspectEnum,
		LoginStreamOpenOkEnum,
		LoginStreamClosedEnum,
		DirectoryStreamOpenSuspectEnum,
		DirectoryStreamOpenOkEnum
	};

	void setState( ImplState state );

	void msgDispatched( bool value = true );

	void eventReceived( bool value = true );

	ItemCallbackClient& getItemCallbackClient();

	DictionaryCallbackClient& getDictionaryCallbackClient();

	DirectoryCallbackClient& getDirectoryCallbackClient();

	LoginCallbackClient& getLoginCallbackClient();

	ChannelCallbackClient& getChannelCallbackClient();

	OmmLoggerClient& getOmmLoggerClient();

	ActiveConfig& getActiveConfig();

	ErrorClientHandler& getErrorClientHandler();

	bool hasErrorClientHandler() const;

	EmaList< TimeOut* >& getTimeOutList();

	Mutex& getTimeOutMutex();

	void installTimeOut();

	virtual void loadDictionary() = 0;

	virtual void reLoadDirectory() = 0;

	virtual void loadDirectory() = 0;

	virtual void setRsslReactorChannelRole( RsslReactorChannelRole& ) = 0;

	virtual void createDictionaryCallbackClient( DictionaryCallbackClient*&, OmmBaseImpl& ) = 0;

	virtual void createDirectoryCallbackClient( DirectoryCallbackClient*&, OmmBaseImpl& ) = 0;

	virtual void processChannelEvent( RsslReactorChannelEvent* ) = 0;

	void handleIue( const EmaString& );

	void handleIue( const char* );

	void handleIhe( UInt64 , const EmaString& );

	void handleIhe( UInt64 , const char* );

	void handleMee( const char* );

protected:

	friend class OmmBaseImplMap<OmmBaseImpl>;
	friend class LoginItem;
	friend class NiProviderLoginItem;

	OmmBaseImpl( ActiveConfig& );
	OmmBaseImpl( ActiveConfig&, OmmConsumerErrorClient& );
	OmmBaseImpl( ActiveConfig&, OmmProviderErrorClient& );
	virtual ~OmmBaseImpl();

	void initialize( EmaConfigImpl* );

	void uninitialize( bool caughtException, bool calledFromInit );

	void readConfig( EmaConfigImpl* );

	virtual void readCustomConfig( EmaConfigImpl* ) = 0;

	ChannelConfig* readChannelConfig( EmaConfigImpl*, const EmaString& );

	bool readReliableMcastConfig( EmaConfigImpl*, const EmaString&, ReliableMcastChannelConfig*, EmaString& );

	void useDefaultConfigValues( const EmaString&, const EmaString&, const EmaString& );

	void setAtExit();

	void run();

	void cleanUp();

	int runLog( void*, const char*, unsigned int );

	void pipeWrite();

	void pipeRead();

	bool isPipeWritten();

	// return values:
	// -2 -> error
	// -1 -> timeout expired ( nothing dispatched )
	//  0 -> message(s) was dispatched
	Int64 rsslReactorDispatchLoop( Int64 timeOut, UInt32 count, bool& bMsgDispRcvd );

	static void terminateIf( void* );

	static void notifErrorClientHandler( const OmmException&, ErrorClientHandler& );

	ActiveConfig&	_activeConfig;

#ifdef USING_SELECT
	fd_set			_readFds;
	fd_set			_exceptFds;
#endif

#ifdef USING_POLL
	pollfd*			_eventFds;
	nfds_t			_eventFdsCount;
	nfds_t			_eventFdsCapacity;
	int				_pipeReadEventFdsIdx;

	void removeFd( int );
	int addFd( int, short );
#endif
	Mutex						_userLock;
	Mutex						_pipeLock;
	Mutex						_timeOutLock;
	RsslErrorInfo				_reactorDispatchErrorInfo;
	ImplState					_state;
	RsslReactor*				_pRsslReactor;
	ChannelCallbackClient*		_pChannelCallbackClient;
	LoginCallbackClient*		_pLoginCallbackClient;
	DirectoryCallbackClient*	_pDirectoryCallbackClient;
	DictionaryCallbackClient*	_pDictionaryCallbackClient;
	ItemCallbackClient*			_pItemCallbackClient;
	OmmLoggerClient*			_pLoggerClient;
	Pipe						_pipe;
	UInt32						_pipeWriteCount;
	bool						_atExit;
	bool						_eventTimedOut;
	bool						_bMsgDispatched;
	bool						_bEventReceived;
	ErrorClientHandler*			_pErrorClientHandler;
	EmaList< TimeOut* >			_theTimeOuts;

private:

	OmmBaseImpl( const OmmBaseImpl& );
	OmmBaseImpl& operator=( const OmmBaseImpl& );
	virtual bool isApiDispatching() const = 0;
};

}

}

}

#endif // __thomsonreuters_ema_access_OmmBaseImpl_h

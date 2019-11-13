///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
///*|-----------------------------------------------------------------------------

#ifndef __ema_iprovider_h_
#define __ema_iprovider_h_

#include <iostream>

#ifdef WIN32
#include <windows.h>
#else
#include <sys/time.h>
#endif

#include "Ema.h"

void sleep( int millisecs )
{
#if defined WIN32
	::Sleep( ( DWORD )( millisecs ) );
#else
	struct timespec sleeptime;
	sleeptime.tv_sec = millisecs / 1000;
	sleeptime.tv_nsec = ( millisecs % 1000 ) * 1000000;
	nanosleep( &sleeptime, 0 );
#endif
}

class AppClient : public thomsonreuters::ema::access::OmmProviderClient
{
public:

	void processLoginRequest(const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent&);

	void processMarketPriceRequest(const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent&);

	void processInvalidItemRequest(const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent&);

protected:

	void onReqMsg( const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent& );

};
//API QA
// application defined error client class for receiving and processing of error notifications
class AppErrorClient : public thomsonreuters::ema::access::OmmProviderErrorClient
{
public:

	void onInvalidHandle(thomsonreuters::ema::access::UInt64, const thomsonreuters::ema::access::EmaString&);

	void onInaccessibleLogFile(const thomsonreuters::ema::access::EmaString&, const thomsonreuters::ema::access::EmaString&);

	void onSystemError(thomsonreuters::ema::access::Int64, void*, const thomsonreuters::ema::access::EmaString&);

	void onMemoryExhaustion(const thomsonreuters::ema::access::EmaString&);

	void onInvalidUsage(const thomsonreuters::ema::access::EmaString&, thomsonreuters::ema::access::Int32);
};
//END API QA
#endif // __ema_iprovider_h_

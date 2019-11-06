///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2018. All rights reserved.            --
///*|-----------------------------------------------------------------------------

#ifndef __ema_consumer_h_
#define __ema_consumer_h_

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
	::Sleep( (DWORD)(millisecs) );
#else
	struct timespec sleeptime;
	sleeptime.tv_sec = millisecs / 1000;
	sleeptime.tv_nsec = (millisecs % 1000) * 1000000;
	nanosleep( &sleeptime, 0 );
#endif
}

// application defined client class for receiving and processing of item messages
class AppClient : public thomsonreuters::ema::access::OmmConsumerClient
{
protected :

	void onRefreshMsg( const thomsonreuters::ema::access::RefreshMsg&, const thomsonreuters::ema::access::OmmConsumerEvent& );

	void onUpdateMsg( const thomsonreuters::ema::access::UpdateMsg&, const thomsonreuters::ema::access::OmmConsumerEvent& );

	void onStatusMsg( const thomsonreuters::ema::access::StatusMsg&, const thomsonreuters::ema::access::OmmConsumerEvent& );
};
//API QA
// application defined error client class for receiving and processing of error notifications
class AppErrorClient : public thomsonreuters::ema::access::OmmConsumerErrorClient
{
public:

	void onInvalidHandle(thomsonreuters::ema::access::UInt64, const thomsonreuters::ema::access::EmaString&);

	void onInaccessibleLogFile(const thomsonreuters::ema::access::EmaString&, const thomsonreuters::ema::access::EmaString&);

	void onSystemError(thomsonreuters::ema::access::Int64, void*, const thomsonreuters::ema::access::EmaString&);

	void onMemoryExhaustion(const thomsonreuters::ema::access::EmaString&);

	void onInvalidUsage(const thomsonreuters::ema::access::EmaString&, thomsonreuters::ema::access::Int32);
};
//END API QA
#endif // __ema_consumer_h_

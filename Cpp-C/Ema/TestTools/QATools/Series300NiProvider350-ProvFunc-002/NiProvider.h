///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
///*|-----------------------------------------------------------------------------

#ifndef __ema_niprovider_h_
#define __ema_niprovider_h_

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
//APIQA
class DataDictionaryCache
{
public:

	DataDictionaryCache() :
	_fldComplete( false ),
	_enumComplete( false )
	{

	}

	rtsdk::ema::rdm::DataDictionary _dataDictionary;
	bool 									 _fldComplete;
	bool 									 _enumComplete;
};

//END APIQA
class AppClient : public rtsdk::ema::access::OmmProviderClient
{
public:

    //APIQA
	void decode(const rtsdk::ema::access::Msg&, DataDictionaryCache*, bool complete = false);  

protected:

	void onRefreshMsg(const rtsdk::ema::access::RefreshMsg&, const rtsdk::ema::access::OmmProviderEvent&);

	void onStatusMsg(const rtsdk::ema::access::StatusMsg&, const rtsdk::ema::access::OmmProviderEvent&);
};


#endif // __ema_niprovider_h_

///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
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

class AppClient : public thomsonreuters::ema::access::OmmProviderClient
{
public :

	AppClient();
	virtual ~AppClient();

	bool isConnectionUp() const;

protected :

	void onRefreshMsg( const thomsonreuters::ema::access::RefreshMsg&, const thomsonreuters::ema::access::OmmProviderEvent& );
	void onStatusMsg( const thomsonreuters::ema::access::StatusMsg&, const thomsonreuters::ema::access::OmmProviderEvent& );

	bool  _bConnectionUp;
};

#endif // __ema_niprovider_h_

/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#include "Ema.h"

class AppClient : public thomsonreuters::ema::access::OmmProviderClient
{
public:

	void processLoginRequest(const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent&);
protected:
	void onReqMsg(const thomsonreuters::ema::access::ReqMsg&, const thomsonreuters::ema::access::OmmProviderEvent&);

};
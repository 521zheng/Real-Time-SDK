/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __thomsonreuters_ema_access_OmmOutOfRangeExceptionImpl_h
#define __thomsonreuters_ema_access_OmmOutOfRangeExceptionImpl_h

#include "OmmOutOfRangeException.h"

namespace thomsonreuters {

namespace ema {

namespace access {

class OmmOutOfRangeExceptionImpl : public OmmOutOfRangeException
{
public :
	
	static void throwException( const EmaString& );

	OmmOutOfRangeExceptionImpl();

	virtual ~OmmOutOfRangeExceptionImpl();

private :

	OmmOutOfRangeExceptionImpl( const OmmOutOfRangeExceptionImpl& );
	OmmOutOfRangeExceptionImpl& operator=( const OmmOutOfRangeExceptionImpl& );
};

}

}

}

#endif // __thomsonreuters_ema_access_OmmOutOfRangeExceptionImpl_h

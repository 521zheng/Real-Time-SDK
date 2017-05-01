/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2016. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __thomsonreuters_ema_access_OmmProviderImpl_h
#define __thomsonreuters_ema_access_OmmProviderImpl_h

#include "OmmBaseImpl.h"
#include "OmmProviderClient.h"
#include "OmmProviderConfig.h"

namespace thomsonreuters {

namespace ema {

namespace access {

typedef const EmaString* EmaStringPtr;

class OmmProvider;

class OmmProviderImpl
{
public :

	OmmProviderImpl(OmmProvider*);

	virtual ~OmmProviderImpl();

	virtual const EmaString& getInstanceName() const = 0;

	virtual OmmProviderConfig::ProviderRole getProviderRole() const = 0;

	virtual UInt64 registerClient(const ReqMsg&, OmmProviderClient&, void* closure = 0, UInt64 parentHandle = 0) = 0;

	virtual void reissue(const ReqMsg&, UInt64) = 0;

	virtual void submit(const GenericMsg&, UInt64) = 0;

	virtual void submit(const RefreshMsg&, UInt64) = 0;

	virtual void submit(const UpdateMsg&, UInt64) = 0;

	virtual void submit(const StatusMsg&, UInt64) = 0;

	virtual Int64 dispatch(Int64 timeOut = 0) = 0;

	virtual void unregister(UInt64) = 0;

	virtual void submit(const AckMsg&, UInt64) = 0;

protected:

	OmmProvider*      _pOmmProvider;

private:
	OmmProviderImpl();
	OmmProviderImpl(const OmmProviderImpl&);
	const OmmProviderImpl& operator=(const OmmProviderImpl&);

};

}

}

}

#endif // __thomsonreuters_ema_access_OmmProviderImpl_h

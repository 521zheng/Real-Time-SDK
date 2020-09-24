/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright (C) 2019 Refinitiv. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __rtsdk_ema_access_TunnelStreamLoginReqMsgImpl_h
#define __rtsdk_ema_access_TunnelStreamLoginReqMsgImpl_h

#include "ReqMsg.h"

#include "rtr/rsslMsgEncoders.h"

namespace rtsdk {

namespace ema {

namespace access {

class TunnelStreamLoginReqMsgImpl
{
public :

	TunnelStreamLoginReqMsgImpl();

	virtual ~TunnelStreamLoginReqMsgImpl();

	TunnelStreamLoginReqMsgImpl( const TunnelStreamLoginReqMsgImpl& );

	TunnelStreamLoginReqMsgImpl& operator=( const TunnelStreamLoginReqMsgImpl& );

	TunnelStreamLoginReqMsgImpl& setLoginReqMsg( const ReqMsg& );

	const ReqMsg& getLoginReqMsg();

	RsslBuffer* getRsslBuffer();

	RsslMsg* getRsslMsg();

private :

	ReqMsg			_loginReqMsg;

	RsslBuffer		_rsslBuffer;
	RsslMsg			_rsslMsg;
};

}

}

}

#endif // __rtsdk_ema_access_TunnelStreamLoginReqMsgImpl_h

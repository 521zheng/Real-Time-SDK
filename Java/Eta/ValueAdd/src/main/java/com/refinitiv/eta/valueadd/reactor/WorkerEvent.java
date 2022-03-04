/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright (C) 2019-2022 Refinitiv. All rights reserved.         --
 *|-----------------------------------------------------------------------------
 */

package com.refinitiv.eta.valueadd.reactor;

/* Internal event class used by the Reactor and Worker. */
class WorkerEvent extends ReactorEvent
{
	WorkerEventTypes _eventType;
	long _timeout;
	TunnelStream _tunnelStream;
	RestClient _restClient;
	ReactorTokenSession _tokenSession;

	WorkerEventTypes eventType()
    {
        return _eventType;
    }
    
    void eventType(WorkerEventTypes type)
    {
        _eventType = type;
    }
    
    long timeout()
    {
        return _timeout;
    }
    
    void timeout(long timeout)
    {
        _timeout = timeout;
    }    

    TunnelStream tunnelStream()
    {
        return _tunnelStream;
    }
    
    void tunnelStream(TunnelStream tunnelStream)
    {
        _tunnelStream = tunnelStream;
    }    

    void clear()
    {
        super.clear();
        _eventType = WorkerEventTypes.INIT;
        _timeout = 0;
        _tunnelStream = null;
        _restClient = null;
        _tokenSession = null;
    }
    
    @Override
    public void returnToPool()
    {
    	_tunnelStream = null;
        _restClient = null;
        _tokenSession = null;
    	
    	super.returnToPool();
    }
    
    /**
     * Returns a String representation of this object.
     * 
     * @return a String representation of this object
     */
    public String toString()
    {
        return super.toString() + ", "
                + (_reactorChannel == null ? "ReactorChannel null" : _reactorChannel.toString())
                + ", " + WorkerEventTypes.toString(_eventType);
    }
}

///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.upa.valueadd.reactor;

import static org.junit.Assert.*;

import com.thomsonreuters.upa.codec.Codec;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.transport.BindOptions;
import com.thomsonreuters.upa.transport.ConnectionTypes;
import com.thomsonreuters.upa.transport.Server;
import com.thomsonreuters.upa.transport.Transport;
import com.thomsonreuters.upa.transport.TransportFactory;
import com.thomsonreuters.upa.transport.TransportReturnCodes;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.MsgBase;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannel;

/** A component represents a consumer, provider, etc. on the network (note the Consumer and Provider subclasses). */

public abstract class TestReactorComponent {
	
    /** Reactor channel associated with this component, if connected. */
	ReactorChannel _reactorChannel;
	
	/** Whether the reactor channel associated with this component is up. */
	boolean _reactorChannelIsUp;
	
	/** ReactorRole associated with this component. */
	ReactorRole _reactorRole;
	
	/** Server associated with this component, if any. */
	Server _server;
	
	/** Reusable ReactorErrorInfo. */
	ReactorErrorInfo _errorInfo;
	
	/** Reactor associated with this component. */
	TestReactor _testReactor;
    
    int _defaultSessionLoginStreamId;
    boolean _defaultSessionLoginStreamIdIsSet;
    int _defaultSessionDirectoryStreamId;
    boolean _defaultSessionDirectoryStreamIdIsSet;
    
    static int _portToBind = 16123; /** A port to use when binding servers. Incremented with each bind. */
    
    /** Returns the port the next bind call will use. Useful for testing reconnection
     * to servers that won't be bound until later in a test. */
    static int nextServerPort()
    {
        return _portToBind;
    }

    /** Returns the port of the component's server. */
    int serverPort()
    {
        assertNotNull(_server);
        return _server.portNumber();
    }
	
	TestReactor testReactor()
	{
		return _testReactor;
	}
	
	void testReactor(TestReactor testReactor)
	{
		_testReactor = testReactor;
	}
	
	ReactorChannel channel()
	{
		return _reactorChannel;
	}
		
	protected TestReactorComponent(TestReactor testReactor)
	{

		_errorInfo = ReactorFactory.createReactorErrorInfo();
		_testReactor = testReactor;
		_testReactor.addComponent(this);
	}
	
	public ReactorRole reactorRole()
	{
		return _reactorRole;
	}
	
	public ReactorChannel reactorChannel()
	{
		return _reactorChannel;
	}
	
	public void reactorChannel(ReactorChannel reactorChannel)
	{
		_reactorChannel = reactorChannel;
	}
	
	public boolean reactorChannelIsUp()
	{
		return _reactorChannelIsUp;
	}
	
	public void reactorChannelIsUp(boolean reactorChannelIsUp)
	{
		_reactorChannelIsUp = reactorChannelIsUp;
	}
	
	/** Stores the login stream ID for this component. Used if a login stream is automatically setup as part of opening a session. */
	public void defaultSessionLoginStreamId(int defaultSessionLoginStreamId)
	{
		_defaultSessionLoginStreamIdIsSet = true;
		_defaultSessionLoginStreamId = defaultSessionLoginStreamId;
		_defaultSessionLoginStreamIdIsSet = true;
	}
	
	/** If a login stream was automatically opened as part of opening a session, returns the ID of that stream for this component. */
	public int defaultSessionLoginStreamId()
	{
		assertTrue(_defaultSessionLoginStreamIdIsSet);
		return _defaultSessionLoginStreamId;
	}
	
	/** Stores the directory stream ID for this component. Used if a directory stream is automatically setup as part of opening a session. */
	public void defaultSessionDirectoryStreamId(int defaultSessionDirectoryStreamId)
	{
		_defaultSessionDirectoryStreamIdIsSet = true;
		_defaultSessionDirectoryStreamId = defaultSessionDirectoryStreamId;
		_defaultSessionDirectoryStreamIdIsSet = true;
	}

	/** If a directory stream was automatically opened as part of opening a session, returns the ID of that stream for this component. */
	public int defaultSessionDirectoryStreamId()
	{
		assertTrue(_defaultSessionDirectoryStreamIdIsSet);
		return _defaultSessionDirectoryStreamId;
	}
	
	Server server()
	{
		return _server;
	}
	
	public void bind(ConsumerProviderSessionOptions opts)
	{
        if (opts.connectionType() != ConnectionTypes.RELIABLE_MCAST)
        {
        	BindOptions bindOpts = TransportFactory.createBindOptions();
            bindOpts.clear();
            bindOpts.majorVersion(Codec.majorVersion());
            bindOpts.minorVersion(Codec.minorVersion());
            bindOpts.serviceName(String.valueOf(_portToBind++));
            bindOpts.pingTimeout(opts.pingTimeout());
            bindOpts.minPingTimeout(opts.pingTimeout());
            _server = Transport.bind(bindOpts, _errorInfo.error());
            assertNotNull("bind failed: " + _errorInfo.error().errorId() + "(" + _errorInfo.error().text() + ")", 
            		_server);
        }
        
        _testReactor.registerComponentServer(this);
	}
	
	
    /** Sends a Msg to the component's channel. */
    int submit(Msg msg, ReactorSubmitOptions submitOptions)
    {
        int ret;

        ret = _reactorChannel.submit(msg, submitOptions, _errorInfo);
        
        assertTrue("submit failed: " + ret + "(" + _errorInfo.location() + "--" + _errorInfo.error().text() + ")",
                ret >= ReactorReturnCodes.SUCCESS);

        return ret;
    }
    
    /** Sends a Msg to the component's channel, and dispatches to ensure no events are received and any internal flush events are processed. */
    int submitAndDispatch(Msg msg, ReactorSubmitOptions submitOptions)
    {
        int ret = submit(msg, submitOptions);
        testReactor().dispatch(0);
        return ret;
    }
    

    /** Sends an RDM message to the component's channel. */
    int submit(MsgBase msg, ReactorSubmitOptions submitOptions)
    {
        int ret;

        ret = _reactorChannel.submit(msg, submitOptions, _errorInfo);
        
        assertTrue("submit failed: " + ret + "(" + _errorInfo.location() + "--" + _errorInfo.error().text() + ")",
                ret >= ReactorReturnCodes.SUCCESS);
        
        return ret;
    }
    
	/** Sends an RDM message to the component's channel, and dispatches to ensure no events are received and any internal flush events are processed. */
    int submitAndDispatch(MsgBase msg, ReactorSubmitOptions submitOptions)
    {
        int ret = submit(msg, submitOptions);
        testReactor().dispatch(0);
        return ret;
    }
	
	/** Disconnect a consumer and provider component and clean them up. */
	public static void closeSession(Consumer consumer, Provider provider)
	{
		/* Make sure there's nothing left in the dispatch queue. */
		consumer.testReactor().dispatch(0);
		provider.testReactor().dispatch(0);
		
		consumer.close();
		provider.close();
	}

    /** Closes the component's channel. */
    void closeChannel()
    {
        assertEquals(ReactorReturnCodes.SUCCESS, _reactorChannel.close(_errorInfo));
        _reactorChannelIsUp = false;
        _reactorChannel = null;
    }
	
	/** Close a component and remove it from its associated TestReactor. 
     * Closes any associated server and reactor channel. */
	void close()
	{
        assertNotNull(_testReactor);
		if (_server != null)
		{
			assertEquals(TransportReturnCodes.SUCCESS, _server.close(_errorInfo.error()));
			_server = null;
		}
		
		if (_reactorChannel != null)
            closeChannel();

        _testReactor.removeComponent(this);
        _testReactor = null;
	}
}

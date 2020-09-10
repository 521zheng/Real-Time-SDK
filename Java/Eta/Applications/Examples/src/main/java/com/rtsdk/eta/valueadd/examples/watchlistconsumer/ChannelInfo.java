package com.rtsdk.eta.valueadd.examples.watchlistconsumer;

import com.rtsdk.eta.codec.Buffer;
import com.rtsdk.eta.codec.CodecFactory;
import com.rtsdk.eta.codec.DataDictionary;
import com.rtsdk.eta.codec.DecodeIterator;
import com.rtsdk.eta.codec.Msg;
import com.rtsdk.eta.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.rtsdk.eta.valueadd.domainrep.rdm.directory.Service;
import com.rtsdk.eta.valueadd.domainrep.rdm.login.LoginMsgFactory;
import com.rtsdk.eta.valueadd.domainrep.rdm.login.LoginMsgType;
import com.rtsdk.eta.valueadd.domainrep.rdm.login.LoginRefresh;
import com.rtsdk.eta.valueadd.examples.common.ConnectionArg;
import com.rtsdk.eta.valueadd.reactor.ConsumerRole;
import com.rtsdk.eta.valueadd.reactor.ReactorChannel;
import com.rtsdk.eta.valueadd.reactor.ReactorConnectInfo;
import com.rtsdk.eta.valueadd.reactor.ReactorConnectOptions;
import com.rtsdk.eta.valueadd.reactor.ReactorFactory;
import com.rtsdk.eta.valueadd.reactor.TunnelStream;

/*
 * Contains information associated with each open channel
 * in the value add Consumer.
 */
class ChannelInfo
{
	ConnectionArg connectionArg;
    ReactorConnectOptions connectOptions = ReactorFactory.createReactorConnectOptions();
    ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
    ConsumerRole consumerRole = ReactorFactory.createConsumerRole();

    PostHandler postHandler = new PostHandler();
 
    DataDictionary dictionary;
    int fieldDictionaryStreamId = 0;
    int enumDictionaryStreamId = 0;
    boolean shouldOffStreamPost = false;
    boolean shouldOnStreamPost = false;
    boolean shouldEnableEncrypted = false;
    boolean shouldEnableHttp = false;
    Buffer postItemName = CodecFactory.createBuffer();
    
    DecodeIterator dIter = CodecFactory.createDecodeIterator();
    Msg responseMsg = CodecFactory.createMsg();

    LoginRefresh loginRefresh = (LoginRefresh)LoginMsgFactory.createMsg();
	boolean hasServiceInfo = false;
    Service serviceInfo = DirectoryMsgFactory.createService();
    ReactorChannel reactorChannel;
    
    boolean tunnelStreamOpenSent = false; // flag to track if we already made a tunnel stream open request
    
    // assume one tunnel stream per ReactorChannel
    TunnelStream tunnelStream;
    boolean hasTunnelStreamServiceInfo = false;
    Service tsServiceInfo = DirectoryMsgFactory.createService();
    boolean isTunnelStreamUp;
    
	long loginReissueTime; // represented by epoch time in milliseconds
	boolean canSendLoginReissue;

	ChannelInfo()
    {
    	connectOptions.connectionList().add(connectInfo);
    	loginRefresh.rdmMsgType(LoginMsgType.REFRESH);
    }
}

package com.thomsonreuters.upa.shared;

import com.thomsonreuters.upa.transport.Channel;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRTT;

public class LoginRttInfo {

    private LoginRTT loginRtt;
    private Channel channel;
    private boolean inUse;
    private long rttLastSendNanoTime;

    public LoginRttInfo() {
        loginRtt = (LoginRTT) LoginMsgFactory.createMsg();
        loginRtt.rdmMsgType(LoginMsgType.RTT);
        rttLastSendNanoTime(System.nanoTime());
    }

    public void clear() {
        loginRtt.clear();
        channel = null;
        inUse = false;
    }

    public LoginRTT loginRtt() {
        return loginRtt;
    }

    public Channel channel() {
        return channel;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public void channel(Channel channel) {
        this.channel = channel;
    }

    public void rttLastSendNanoTime(long rttLastSendNanoTime) {
        this.rttLastSendNanoTime = rttLastSendNanoTime;
    }

    public long rttLastSendNanoTime() {
        return this.rttLastSendNanoTime;
    }
}

///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2018. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import com.thomsonreuters.upa.codec.Codec;
import com.thomsonreuters.upa.transport.ConnectionTypes;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannel;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelInfo;
import com.thomsonreuters.upa.valueadd.reactor.ReactorErrorInfo;
import com.thomsonreuters.upa.valueadd.reactor.ReactorFactory;
import com.thomsonreuters.ema.access.ChannelInformation;

class ChannelInformationImpl implements ChannelInformation
{
	public ChannelInformationImpl()
	{
		clear();
	}

	public ChannelInformationImpl(String componentInfo, String hostname, String ipAddress, int state,
			int connectionType, int protocolType, int majorVersion, int minorVersion, int pingTimeout) {
		this._componentInfo = componentInfo;
		this._hostname = hostname;
		this._ipAddress = ipAddress;
		this._channelState = state;
		this._connectionType = connectionType;
		this._protocolType = protocolType;
		this._majorVersion = majorVersion;
		this._minorVersion = minorVersion;
		this._pingTimeout = pingTimeout;
	}

	public ChannelInformationImpl(ReactorChannel channel) {
		set(channel);
	}

	@Override
	public void clear() {
		_channelState = ChannelState.CLOSED;
		_connectionType = ConnectionType.UNIDENTIFIED;
		_protocolType = ProtocolType.UNKNOWN;
		_pingTimeout = _majorVersion = _minorVersion = 0;
		_ipAddress = _hostname = _componentInfo = null;
	}

	public void set(ReactorChannel reactorChannel) {
		clear();
		
		if (reactorChannel == null)
			return;

		if (reactorChannel.channel() == null ) {
			_componentInfo = "unavailable";
		}
		else {
			ReactorChannelInfo rci = ReactorFactory.createReactorChannelInfo();
			ReactorErrorInfo ei = ReactorFactory.createReactorErrorInfo();
			reactorChannel.info(rci, ei);

			if (rci.channelInfo() == null ||
				rci.channelInfo().componentInfo() == null ||
				rci.channelInfo().componentInfo().isEmpty())
				_componentInfo = "unavailable";
			else {
				_componentInfo = rci.channelInfo().componentInfo().get(0).componentVersion().toString();

				// the clientHostname and clientIP methods will return non-null values only for IProvider clients
				_hostname = rci.channelInfo().clientHostname();
				_ipAddress = rci.channelInfo().clientIP();
			}
		}

		// _hostname will be null for Consumer and NiProvider applications.
		if (_hostname == null) {
			_hostname = reactorChannel.hostname();
		}

		if (reactorChannel.channel() != null) {
			_connectionType = reactorChannel.channel().connectionType();
			_protocolType = reactorChannel.channel().protocolType();
			_majorVersion = reactorChannel.channel().majorVersion();
			_minorVersion = reactorChannel.channel().minorVersion();
			_pingTimeout = reactorChannel.channel().pingTimeout();
			_channelState = reactorChannel.channel().state();			
		}
		else {
			_connectionType = _protocolType = -1;
			_majorVersion = _minorVersion = _pingTimeout = 0;
		}
	}
	
	@Override
	public String toString() {
		_stringBuilder.setLength(0);
		_stringBuilder.append("hostname: " + _hostname + "\n\tIP address: " + _ipAddress
				+ "\n\tconnected component info: " + _componentInfo + "\n\tchannel state: ");
		
		switch (_channelState)
		{
			case ChannelState.CLOSED:
				_stringBuilder.append("closed");
				break;
			case ChannelState.INACTIVE:
				_stringBuilder.append("inactive");	
				break;
			case ChannelState.INITIALIZING:
				_stringBuilder.append("initializing");					
				break;
			case ChannelState.ACTIVE:
				_stringBuilder.append("active");					
				break;
			default:
				_stringBuilder.append(_channelState);
				break;
		}
		
		if(_connectionType == ConnectionType.UNIDENTIFIED)
			_stringBuilder.append( "\n\tconnection type: unknown" + "\n\tprotocol type: ");
		else
			_stringBuilder.append( "\n\tconnection type: " + ConnectionTypes.toString(_connectionType)
				+ "\n\tprotocol type: ");
		if (_protocolType == 0)
			_stringBuilder.append("Reuters wire format");
		else
			_stringBuilder.append("unknown wire format");
		_stringBuilder.append("\n\tmajor version: " + _majorVersion + "\n\tminor version: " + _minorVersion
				+ "\n\tping timeout: " + _pingTimeout);
		return _stringBuilder.toString();
	}

	@Override
	public String componentInformation() {
		return _componentInfo;
	}

	@Override
	public String hostname() {
		return _hostname;
	}

	@Override
	public String ipAddress() {
		return _ipAddress;
	}

	@Override
	public int connectionType() {
		switch (_connectionType) {
		case 0: return ConnectionTypes.SOCKET;
		case 1: return ConnectionTypes.ENCRYPTED;
		case 2: return ConnectionTypes.HTTP;
		case 3: return ConnectionTypes.UNIDIR_SHMEM;
		case 4: return ConnectionTypes.RELIABLE_MCAST;
		case 6: return ConnectionTypes.SEQUENCED_MCAST;
		default: return _connectionType;	
		}
	}

	@Override
	public int channelState() {
		return _channelState;
	}

	@Override
	public int protocolType() {
		if (_protocolType == Codec.RWF_PROTOCOL_TYPE)
			return Codec.RWF_PROTOCOL_TYPE;
		return _protocolType;
	}

	@Override
	public int majorVersion() {
		return _majorVersion;
	}

	@Override
	public int minorVersion() {
		return _minorVersion;
	}

	@Override
	public int pingTimeout() {
		return _pingTimeout;
	}

	@Override
	public void hostname(String hostname) {
		_hostname = hostname;
	}

	@Override
	public void ipAddress(String ipAddress) {
		_ipAddress = ipAddress;
	}

	@Override
	public void componentInfo(String componentInfo) {
		_componentInfo = componentInfo;
	}

	@Override
	public void channelState(int channelState) {
		_channelState = channelState;
	}

	@Override
	public void connectionType(int connectionType) {
		_connectionType = connectionType;
	}

	@Override
	public void protocolType(int protocolType) {
		_protocolType = protocolType;
	}

	@Override
	public void majorVersion(int majorVersion) {
		_majorVersion = majorVersion;
	}

	@Override
	public void minorVersion(int minorVersion) {
		_minorVersion = minorVersion;
	}

	@Override
	public void pingTimeout(int pingTimeout) {
		_pingTimeout = pingTimeout;

	}

	private int _channelState;
	private int _connectionType;
	private String _hostname;
	private String _ipAddress;
	private String _componentInfo;
	private int _protocolType;
	private int _majorVersion;
	private int _minorVersion;
	private int _pingTimeout;
	private StringBuilder _stringBuilder = new StringBuilder();
}

///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2016. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.ema.access.ConfigManager.ConfigAttributes;
import com.thomsonreuters.ema.access.ConfigManager.ConfigElement;
import com.thomsonreuters.ema.access.DirectoryServiceStore.ServiceIdInteger;
import com.thomsonreuters.ema.access.OmmException.ExceptionType;
import com.thomsonreuters.ema.access.OmmLoggerClient.Severity;
import com.thomsonreuters.ema.rdm.EmaRdm;
import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.Codec;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.RefreshMsgFlags;
import com.thomsonreuters.upa.transport.WritePriorities;
import com.thomsonreuters.upa.valueadd.common.VaNode;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRefresh;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service.ServiceGroup;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service.ServiceState;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelEvent;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelEventTypes;
import com.thomsonreuters.upa.valueadd.reactor.ReactorReturnCodes;

class OmmNiProviderImpl extends OmmBaseImpl<OmmProviderClient> implements OmmProvider, DirectoryServiceStoreClient {
	
	private OmmProviderErrorClient _providerErrorClient = null;
	private OmmNiProviderActiveConfig _activeConfig = null;
	private HashMap<LongObject, StreamInfo> _handleToStreamInfo = new HashMap<>();
	private boolean _bIsStreamIdZeroRefreshSubmitted = false;
	private ReqMsg loginRequest = EmaFactory.createReqMsg();
	private int _nextProviderStreamId;
	private List<IntObject> _reusedProviderStreamIds;
	private LongObject _longObject = new LongObject();
	private ItemWatchList	_itemWatchList;
	private OmmNiProviderDirectoryStore _ommNiProviderDirectoryStore;
	private OmmProviderClient _adminClient;
	private Object _adminClosure;
	private ChannelInfo _activeChannelInfo;
	
	private static final long MIN_LONG_VALUE = 1;
    private static final long MAX_LONG_VALUE = Long.MAX_VALUE;
    
	private static long _longId = Integer.MAX_VALUE;
    
	OmmNiProviderImpl(OmmProviderConfig config)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		_adminClient = null;
		_adminClosure = null;
		_activeChannelInfo = null;
		
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	OmmNiProviderImpl(OmmProviderConfig config, OmmProviderClient client)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		/* the client needs to be set before calling initialize, so the proper item callbacks are set */
		_adminClient = client;
		_adminClosure = null;
		_activeChannelInfo = null;
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	OmmNiProviderImpl(OmmProviderConfig config, OmmProviderClient client, Object closure)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		/* the client needs to be set before calling initialize, so the proper item callbacks are set */
		_adminClient = client;
		_adminClosure = closure;
		_activeChannelInfo = null;
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}

	OmmNiProviderImpl(OmmProviderConfig config, OmmProviderErrorClient client)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		_adminClient = null;
		_adminClosure = null;
		_activeChannelInfo = null;
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_providerErrorClient = client;
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	OmmNiProviderImpl(OmmProviderConfig config, OmmProviderClient adminClient, OmmProviderErrorClient errorClient)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		/* the client needs to be set before calling initialize, so the proper item callbacks are set */
		_adminClient = adminClient;
		_adminClosure = null;
		_activeChannelInfo = null;
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_providerErrorClient = errorClient;

		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	OmmNiProviderImpl(OmmProviderConfig config, OmmProviderClient adminClient, OmmProviderErrorClient errorClient, Object closure)
	{
		super();
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		/* the client needs to be set before calling initialize, so the proper item callbacks are set */
		_adminClient = adminClient;
		_adminClosure = closure;
		_activeChannelInfo = null;
		super.initialize(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_providerErrorClient = errorClient;
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	//only for unit test, internal use
	OmmNiProviderImpl(OmmProviderConfig config, boolean isForTest)
	{
		if (!isForTest)
			return;
		
		_activeConfig = new OmmNiProviderActiveConfig();
		
		_activeConfig.directoryAdminControl = ((OmmNiProviderConfigImpl)config).adminControlDirectory();
		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		_ommNiProviderDirectoryStore = new OmmNiProviderDirectoryStore(_objManager, this, _activeConfig);
		
		_ommNiProviderDirectoryStore.setClient(this);
		
		/* the client needs to be set before calling initialize, so the proper item callbacks are set */
		_adminClient = null;
		_adminClosure = null;
		_activeChannelInfo = null;
		super.initializeForTest(_activeConfig, (OmmNiProviderConfigImpl)config);
		
		_itemWatchList = new ItemWatchList(_itemCallbackClient);
		
		_providerErrorClient = null;
		
		_rsslSubmitOptions.writeArgs().priority(WritePriorities.HIGH);
		
		_nextProviderStreamId = 0;	
		_reusedProviderStreamIds = new ArrayList<IntObject>();
	}
	
	@Override
	public void uninitialize()
	{
		super.uninitialize();
	}

	@Override
	public String providerName() {
		return _activeConfig.instanceName;
	}
	
	@Override
	public long registerClient(ReqMsg reqMsg, OmmProviderClient client)
	{
		return registerClient(reqMsg, client, null);
	}
	
	@Override
	public long registerClient(ReqMsg reqMsg, OmmProviderClient client, Object closure)
	{
		userLock().lock();
		
		if ( reqMsg.domainType() != EmaRdm.MMT_LOGIN && reqMsg.domainType() != EmaRdm.MMT_DICTIONARY )
		{
			StringBuilder temp = strBuilder();
			temp.append("OMM Interactive provider supports registering LOGIN and DICTIONARY domain type only.");
			userLock().unlock();
			handleInvalidUsage(temp.toString());
			return 0;
		}
		
		long handle = super.registerClient(reqMsg, client, closure);
		
		if (handle != 0)
		{
			StreamInfo streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
	    	if (streamInfo == null)
	    	{
	    		streamInfo = new StreamInfo(StreamType.CONSUMING, _itemCallbackClient.getItem(handle).streamId(), 0, reqMsg.domainType());
	    		_objManager._streamInfoPool.updatePool(streamInfo);
	    	}
	    	else
	    	{
	    		streamInfo.clear();
	    		streamInfo.set(StreamType.CONSUMING , _itemCallbackClient.getItem(handle).streamId(), reqMsg.domainType());
	    	}
	    	
	    	streamInfo.handle(handle);
	    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
		}
		
		userLock().unlock();
		return handle;
	}
	
	public void reissue(ReqMsg reqMsg, long handle)
	{
		ReqMsgImpl reqMsgImpl = (ReqMsgImpl)reqMsg;
		
		if  ( reqMsgImpl.domainTypeSet() && ( reqMsg.domainType() != EmaRdm.MMT_LOGIN && reqMsg.domainType() != EmaRdm.MMT_DICTIONARY ) )
		{
			StringBuilder temp = strBuilder();
			temp.append("OMM Interactive provider supports reissuing LOGIN and DICTIONARY domain type only.");
			handleInvalidUsage(temp.toString());
			return;
		}
		
		super.reissue(reqMsg, handle);
	}
	
	@Override
	public long dispatch(long timeOut)
	{
		return super.dispatch(timeOut);
	}
	
	@Override
	public void unregister(long handle)
	{
		userLock().lock();
		
		StreamInfo streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
		
		if ( streamInfo == null )
		{
			userLock().unlock();
			return;
		}
		
		if ( streamInfo.streamType() != StreamType.CONSUMING )
		{
			userLock().unlock();
			handleInvalidHandle(handle, "Attempt to unregister a handle that was not registered.");
			return;
		}
		
		_handleToStreamInfo.remove(_longObject.value(handle));
		streamInfo.returnToPool();
		
		super.unregister(handle);
		
		userLock().unlock();
	}

	@Override
	public void submit(RefreshMsg refreshMsg, long handle)
	{
		boolean bHandleAdded = false;
		StreamInfo streamInfo = null;
		
		userLock().lock();
		
		if ( _channelCallbackClient == null )
		{
			userLock().unlock();
			return;
		}
		
		if(_activeChannelInfo == null)
		{
			userLock().unlock();
			handleInvalidUsage(strBuilder().append("No active channel to send message.").toString());
			return;
		}
		
		RefreshMsgImpl refreshMsgImpl = (RefreshMsgImpl)refreshMsg;
		
		if ( refreshMsgImpl.domainType() == EmaRdm.MMT_DIRECTORY)
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received RefreshMsg with SourceDirectory domain; Handle = ")
						.append(handle).append(", user assigned streamId = ").append(refreshMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
		
			if ( refreshMsgImpl.rsslMsg().containerType() != com.thomsonreuters.upa.codec.DataTypes.MAP )
			{
				userLock().unlock();
				handleInvalidUsage(strBuilder().append("Attempt to submit RefreshMsg with SourceDirectory domain using container with wrong data type. Expected container data type is Map. Passed in is ")
						.append(DataType.asString(refreshMsgImpl.payload().dataType())).toString());
				return;
			}
			
			if ( !_ommNiProviderDirectoryStore.decodeSourceDirectory(refreshMsgImpl._rsslMsg, strBuilder() ) )
			{
				userLock().unlock();
				handleInvalidUsage(_strBuilder.toString());
				return;
			}
			
			if ( !_ommNiProviderDirectoryStore.submitSourceDirectory(null, refreshMsgImpl._rsslMsg, strBuilder(), _activeConfig.recoverUserSubmitSourceDirectory) )
			{
				userLock().unlock();
				StringBuilder text = new StringBuilder();
				text.append("Attempt to submit invalid source directory domain message.").append(OmmLoggerClient.CR)
				.append("Reason = ").append(_strBuilder);
				handleInvalidUsage(text.toString());
				return;
			}
			
			int flags = refreshMsgImpl._rsslMsg.flags();
			flags &= ~RefreshMsgFlags.SOLICITED;
			refreshMsgImpl._rsslMsg.flags(flags);
			
			if ( _activeConfig.mergeSourceDirectoryStreams)
			{
				refreshMsgImpl._rsslMsg.streamId(0);
			}
			else
			{
				streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
				if (streamInfo != null)
				{
					refreshMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				}
				else
				{
					streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
			    	if (streamInfo == null)
			    	{
			    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    		_objManager._streamInfoPool.updatePool(streamInfo);
			    	}
			    	else
			    	{
			    		streamInfo.clear();
			    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    	}
			    	
			    	refreshMsgImpl._rsslMsg.streamId(streamInfo.streamId());
					_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
					
					bHandleAdded = true;
				}
			}
		}
		else
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received RefreshMsg with market domain; Handle = ")
					.append(handle).append(", user assigned streamId = ").append(refreshMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
			
			streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
			
			if ( streamInfo != null )
			{
				refreshMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				
				if ( ( refreshMsgImpl._rsslMsg.flags() & com.thomsonreuters.upa.codec.RefreshMsgFlags.HAS_MSG_KEY) != 0 )
				{
					refreshMsgImpl._rsslMsg.msgKey().serviceId(streamInfo.serviceId());
					refreshMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				}
			}
			else if ( refreshMsgImpl.hasServiceName())
			{
				String serviceName = refreshMsgImpl.serviceName();
				
				ServiceIdInteger serviceId = _ommNiProviderDirectoryStore.serviceId(serviceName);
				
				if ( serviceId == null )
				{
					userLock().unlock();
					
					strBuilder().append("Attempt to submit initial RefreshMsg with service name of ")
					.append(serviceName).append(" that was not included in the SourceDirectory. Dropping this RefreshMsg.");
					handleInvalidUsage(_strBuilder.toString());
					return;
				}
				
				refreshMsgImpl._rsslMsg.msgKey().serviceId(serviceId.value());
				refreshMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				
				int flags = refreshMsgImpl.rsslMsg().flags();
				flags &= ~RefreshMsgFlags.SOLICITED;
				refreshMsgImpl.rsslMsg().flags(flags);
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
		    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(), refreshMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(), refreshMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	refreshMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
				bHandleAdded = true;
				
			}
			else if ( refreshMsgImpl.hasServiceId())
			{
				int serviceId = refreshMsgImpl.serviceId();
				String serviceName = _ommNiProviderDirectoryStore.serviceName(serviceId);
				
				if ( serviceName == null )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit initial RefreshMsg with service id of ")
					.append(serviceId).append(" that was not included in the SourceDirectory. Dropping this RefreshMsg.");
					handleInvalidUsage(_strBuilder.toString());
				}
				
				int flags = refreshMsgImpl.rsslMsg().flags();
				flags &= ~RefreshMsgFlags.SOLICITED;
				refreshMsgImpl.rsslMsg().flags(flags);
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
					streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId(), serviceId, refreshMsgImpl.domainType());
					streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId, refreshMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	refreshMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
		    	bHandleAdded = true;
			}
			else
			{
				userLock().unlock();
				handleInvalidUsage("Attempt to submit initial RefreshMsg without service name or id. Dropping this RefreshMsg.");
				return;
			}
		}
		
		_rsslErrorInfo.clear();
		int ret;
		if (ReactorReturnCodes.SUCCESS > (ret = _activeChannelInfo.rsslReactorChannel().submit(refreshMsgImpl._rsslMsg, _rsslSubmitOptions, _rsslErrorInfo)))
	    {
			if (bHandleAdded)
			{
				_handleToStreamInfo.remove(_longObject.value(handle));
				streamInfo.returnToPool();
				returnProviderStreamId(refreshMsgImpl._rsslMsg.streamId());
			}
			
			if (loggerClient().isErrorEnabled())
        	{
				com.thomsonreuters.upa.transport.Error error = _rsslErrorInfo.error();
				
	        	strBuilder().append("Internal error: rsslChannel.submit() failed in OmmNiProviderImpl.submit(RefreshMsg)")
	        		.append("RsslChannel ").append(Integer.toHexString(error.channel() != null ? error.channel().hashCode() : 0)) 
	    			.append(OmmLoggerClient.CR)
	    			.append("Error Id ").append(error.errorId()).append(OmmLoggerClient.CR)
	    			.append("Internal sysError ").append(error.sysError()).append(OmmLoggerClient.CR)
	    			.append("Error Location ").append(_rsslErrorInfo.location()).append(OmmLoggerClient.CR)
	    			.append("Error Text ").append(error.text());
	        	
	        	loggerClient().error(formatLogMessage(instanceName() , _strBuilder.toString(), Severity.ERROR));
        	}
			
			userLock().unlock();
			strBuilder().append("Failed to submit RefreshMsg. Reason: ")
				.append(ReactorReturnCodes.toString(ret))
				.append(". Error text: ")
				.append(_rsslErrorInfo.error().text());
			
			handleInvalidUsage(_strBuilder.toString());
			return;
	    }
		
		if (refreshMsgImpl.state().streamState() == OmmState.StreamState.CLOSED || 
				refreshMsgImpl.state().streamState() == OmmState.StreamState.CLOSED_RECOVER || 
				refreshMsgImpl.state().streamState() == OmmState.StreamState.CLOSED_REDIRECTED ||
				( refreshMsgImpl.state().streamState() == OmmState.StreamState.NON_STREAMING && refreshMsgImpl.complete() ) )
		{
			_handleToStreamInfo.remove(_longObject.value(handle));
			streamInfo.returnToPool();
			returnProviderStreamId(refreshMsgImpl._rsslMsg.streamId());
		}
		
		if (refreshMsgImpl._rsslMsg.streamId() == 0)
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
		}
		
		userLock().unlock();
	}

	@Override
	public void submit(UpdateMsg updateMsg, long handle)
	{
		boolean bHandleAdded = false;
		StreamInfo streamInfo = null;
		
		userLock().lock();
		
		if ( _channelCallbackClient == null )
		{
			userLock().unlock();
			return;
		}
		
		if(_activeChannelInfo == null)
		{
			userLock().unlock();
			handleInvalidUsage(strBuilder().append("No active channel to send message.").toString());
			return;
		}
		
		UpdateMsgImpl updateMsgImpl = (UpdateMsgImpl)updateMsg;
		
		if ( updateMsg.domainType() == EmaRdm.MMT_DIRECTORY)
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received UpdateMsg with SourceDirectory domain; Handle = ")
						.append(handle).append(", user assigned streamId = ").append(updateMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
			
			if ( updateMsgImpl.rsslMsg().containerType() != com.thomsonreuters.upa.codec.DataTypes.MAP )
			{
				userLock().unlock();
				handleInvalidUsage(strBuilder().append("Attempt to submit UpdateMsg with SourceDirectory domain using container with wrong data type. Expected container data type is Map. Passed in is ")
						.append(  DataType.asString(updateMsgImpl.payload().dataType())).toString());
				return;
			}
			
			if ( !_ommNiProviderDirectoryStore.decodeSourceDirectory(updateMsgImpl._rsslMsg, strBuilder()) )
			{
				userLock().unlock();
				handleInvalidUsage(_strBuilder.toString());
				return;
			}
			
			if ( !_ommNiProviderDirectoryStore.submitSourceDirectory( null, updateMsgImpl._rsslMsg, strBuilder(), _activeConfig.recoverUserSubmitSourceDirectory) )
			{
				userLock().unlock();
				StringBuilder text = new StringBuilder();
				text.append("Attempt to submit invalid source directory domain message.").append(OmmLoggerClient.CR)
				.append("Reason = ").append(_strBuilder);
				handleInvalidUsage(text.toString());
				return;
			}
			
			if ( _activeConfig.mergeSourceDirectoryStreams)
			{
				if ( _activeConfig.refreshFirstRequired && !_bIsStreamIdZeroRefreshSubmitted )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit UpdateMsg with SourceDirectory while RefreshMsg was not submitted on this stream yet. Handle = ")
					.append(handle).append(".");
					handleInvalidHandle(handle, _strBuilder.toString());
					return;
				}
				
				updateMsgImpl._rsslMsg.streamId(0);
			}
			else
			{
				streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
				if (streamInfo != null)
				{
					updateMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				}
				else
				{
					streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
			    	if (streamInfo == null)
			    	{
			    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    		_objManager._streamInfoPool.updatePool(streamInfo);
			    	}
			    	else
			    	{
			    		streamInfo.clear();
			    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    	}
			    	
			    	updateMsgImpl._rsslMsg.streamId(streamInfo.streamId());
					_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
					
					bHandleAdded = true;
				}
			}
		}
		else
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received UpdateMsg with market domain; Handle = ")
						.append(handle).append(", user assigned streamId = ").append(updateMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
			
			streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
			
			if ( streamInfo != null )
			{
				updateMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				
				if ( ( updateMsgImpl._rsslMsg.flags() & com.thomsonreuters.upa.codec.UpdateMsgFlags.HAS_MSG_KEY) != 0 )
				{
					updateMsgImpl._rsslMsg.msgKey().serviceId(streamInfo.serviceId());
					updateMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				}
			}
			else if ( updateMsgImpl.hasServiceName())
			{
				String serviceName = updateMsgImpl.serviceName();
				
				ServiceIdInteger serviceId = _ommNiProviderDirectoryStore.serviceId(serviceName);
				
				if ( serviceId == null )
				{
					userLock().unlock();
					
					strBuilder().append("Attempt to submit initial UpdateMsg with service name of ")
					.append(serviceName).append(" that was not included in the SourceDirectory. Dropping this UpdateMsg.");
					handleInvalidUsage(_strBuilder.toString());
					return;
				}
				
				updateMsgImpl._rsslMsg.msgKey().serviceId(serviceId.value());
				updateMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
		    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(), updateMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(), updateMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	updateMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
				bHandleAdded = true;
				
			}
			else if ( updateMsgImpl.hasServiceId())
			{
				int serviceId = updateMsgImpl.serviceId();
				String serviceName = _ommNiProviderDirectoryStore.serviceName(serviceId);
				
				if ( serviceName == null )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit initial UpdateMsg with service id of ")
					.append(serviceId).append(" that was not included in the SourceDirectory. Dropping this UpdateMsg.");
					handleInvalidUsage(_strBuilder.toString());
					return;
				}
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
					streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId(), serviceId, updateMsgImpl.domainType());
					streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId, updateMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	updateMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
		    	bHandleAdded = true;
			}
			else
			{
				userLock().unlock();
				handleInvalidUsage("Attempt to submit initial UpdateMsg without service name or id. Dropping this UpdateMsg.");
				return;
			}
		}
		
		_rsslErrorInfo.clear();
		int ret;
		if (ReactorReturnCodes.SUCCESS > (ret = _activeChannelInfo.rsslReactorChannel().submit(updateMsgImpl._rsslMsg, _rsslSubmitOptions, _rsslErrorInfo)))
	    {
			if (bHandleAdded)
			{
				_handleToStreamInfo.remove(_longObject.value(handle));
				streamInfo.returnToPool();
				returnProviderStreamId(updateMsgImpl._rsslMsg.streamId());
			}
			
			if (loggerClient().isErrorEnabled())
        	{
				com.thomsonreuters.upa.transport.Error error = _rsslErrorInfo.error();
				
	        	strBuilder().append("Internal error: rsslChannel.submit() failed in OmmNiProviderImpl.submit(UpdateMsg)")
	        		.append("RsslChannel ").append(Integer.toHexString(error.channel() != null ? error.channel().hashCode() : 0)) 
	    			.append(OmmLoggerClient.CR)
	    			.append("Error Id ").append(error.errorId()).append(OmmLoggerClient.CR)
	    			.append("Internal sysError ").append(error.sysError()).append(OmmLoggerClient.CR)
	    			.append("Error Location ").append(_rsslErrorInfo.location()).append(OmmLoggerClient.CR)
	    			.append("Error Text ").append(error.text());
	        	
	        	loggerClient().error(formatLogMessage(instanceName() , _strBuilder.toString(), Severity.ERROR));
        	}
			
			userLock().unlock();
			strBuilder().append("Failed to submit UpdateMsg. Reason: ")
				.append(ReactorReturnCodes.toString(ret))
				.append(". Error text: ")
				.append(_rsslErrorInfo.error().text());
			
			handleInvalidUsage(_strBuilder.toString());
			return;
	    }
		
		userLock().unlock();
	}

	@Override
	public void submit(StatusMsg statusMsg, long handle)
	{
		boolean bHandleAdded = false;
		StreamInfo streamInfo = null;
		
        userLock().lock();
		
		if ( _channelCallbackClient == null )
		{
			userLock().unlock();
			return;
		}
		
		if(_activeChannelInfo == null)
		{
			userLock().unlock();
			handleInvalidUsage(strBuilder().append("No active channel to send message.").toString());
			return;
		}
		
		StatusMsgImpl statusMsgImpl = (StatusMsgImpl)statusMsg;
		
		if ( statusMsg.domainType() == EmaRdm.MMT_DIRECTORY)
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received StatusMsg with SourceDirectory domain; Handle = ")
					.append(handle).append(", user assigned streamId = ").append(statusMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
			
			if ( statusMsgImpl.rsslMsg().containerType() != com.thomsonreuters.upa.codec.DataTypes.MAP )
			{
				userLock().unlock();
				handleInvalidUsage(strBuilder().append("Attempt to submit StatusMsg with SourceDirectory domain using container with wrong data type. Expected container data type is Map. Passed in is ")
						.append(DataType.asString(statusMsgImpl.payload().dataType())).toString());
			}
			
			if ( !_ommNiProviderDirectoryStore.decodeSourceDirectory(statusMsgImpl._rsslMsg, strBuilder()) )
			{
				userLock().unlock();
				handleInvalidUsage(_strBuilder.toString());
				return;
			}
			
			if ( _activeConfig.mergeSourceDirectoryStreams)
			{
				if ( _activeConfig.refreshFirstRequired && !_bIsStreamIdZeroRefreshSubmitted )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit StatusMsg with SourceDirectory while RefreshMsg was not submitted on this stream yet. Handle = ")
					.append(handle).append(".");
					handleInvalidHandle(handle, _strBuilder.toString());
					return;
				}
				
				statusMsgImpl._rsslMsg.streamId(0);
			}
			else
			{
				streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
				if (streamInfo != null)
				{
					statusMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				}
				else
				{
					streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
			    	if (streamInfo == null)
			    	{
			    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    		_objManager._streamInfoPool.updatePool(streamInfo);
			    	}
			    	else
			    	{
			    		streamInfo.clear();
			    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId());
			    		streamInfo.handle(handle);
			    	}
			    	
			    	statusMsgImpl._rsslMsg.streamId(streamInfo.streamId());
					_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
					
					bHandleAdded = true;
				}
			}
		}
		else
		{
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received StatusMsg with market domain; Handle = ")
						.append(handle).append(", user assigned streamId = ").append(statusMsgImpl.streamId()).append(".").toString(), Severity.TRACE));
			}
			
			streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
			
			if ( streamInfo != null )
			{
				statusMsgImpl._rsslMsg.streamId(streamInfo.streamId());
				
				if ( ( statusMsgImpl._rsslMsg.flags() & com.thomsonreuters.upa.codec.RefreshMsgFlags.HAS_MSG_KEY) != 0 )
				{
					statusMsgImpl._rsslMsg.msgKey().serviceId(streamInfo.serviceId());
					statusMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				}
			}
			else if ( statusMsgImpl.hasServiceName())
			{
				String serviceName = statusMsgImpl.serviceName();
				
				ServiceIdInteger serviceId = _ommNiProviderDirectoryStore.serviceId(serviceName);
				
				if ( serviceId == null )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit initial StatusMsg with service name of ")
					.append(serviceName).append(" that was not included in the SourceDirectory. Dropping this StatusMsg.");
					handleInvalidUsage(_strBuilder.toString());
					return;
				}
				
				statusMsgImpl._rsslMsg.msgKey().serviceId(serviceId.value());
				statusMsgImpl._rsslMsg.msgKey().applyHasServiceId();
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
		    		streamInfo = new StreamInfo(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(),  statusMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId.value(), statusMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	statusMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
				bHandleAdded = true;
				
			}
			else if ( statusMsgImpl.hasServiceId())
			{
				int serviceId = statusMsgImpl.serviceId();
				String serviceName = _ommNiProviderDirectoryStore.serviceName(serviceId);
				
				if ( serviceName == null )
				{
					userLock().unlock();
					strBuilder().append("Attempt to submit initial StatusMsg with service id of ")
					.append(serviceId).append(" that was not included in the SourceDirectory. Dropping this StatusMsg.");
					handleInvalidUsage(_strBuilder.toString());
					return;
				}
				
				streamInfo = (StreamInfo)_objManager._streamInfoPool.poll();
		    	if (streamInfo == null)
		    	{
		    		streamInfo = new StreamInfo(StreamType.PROVIDING  ,nextProviderStreamId(),serviceId, statusMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    		_objManager._streamInfoPool.updatePool(streamInfo);
		    	}
		    	else
		    	{
		    		streamInfo.clear();
		    		streamInfo.set(StreamType.PROVIDING, nextProviderStreamId(),serviceId, statusMsgImpl.domainType());
		    		streamInfo.handle(handle);
		    	}
		    	
		    	statusMsgImpl._rsslMsg.streamId(streamInfo.streamId());
		    	_handleToStreamInfo.put(streamInfo.handle(), streamInfo);
				
		    	bHandleAdded = true;
			}
			else
			{
				userLock().unlock();
				handleInvalidUsage("Attempt to submit initial StatusMsg without service name or id. Dropping this StatusMsg.");
				return;
			}
		}
		
		_rsslErrorInfo.clear();
		int ret;
		if (ReactorReturnCodes.SUCCESS > (ret = _activeChannelInfo.rsslReactorChannel().submit(statusMsgImpl._rsslMsg, _rsslSubmitOptions, _rsslErrorInfo)))
	    {
			if (bHandleAdded)
			{
				_handleToStreamInfo.remove(_longObject.value(handle));
				streamInfo.returnToPool();
				returnProviderStreamId(statusMsgImpl._rsslMsg.streamId());
			}
			
			if (loggerClient().isErrorEnabled())
        	{
				com.thomsonreuters.upa.transport.Error error = _rsslErrorInfo.error();
				
	        	strBuilder().append("Internal error: rsslChannel.submit() failed in OmmNiProviderImpl.submit(StatusMsg)")
	        		.append("RsslChannel ").append(Integer.toHexString(error.channel() != null ? error.channel().hashCode() : 0)) 
	    			.append(OmmLoggerClient.CR)
	    			.append("Error Id ").append(error.errorId()).append(OmmLoggerClient.CR)
	    			.append("Internal sysError ").append(error.sysError()).append(OmmLoggerClient.CR)
	    			.append("Error Location ").append(_rsslErrorInfo.location()).append(OmmLoggerClient.CR)
	    			.append("Error Text ").append(error.text());
	        	
	        	loggerClient().error(formatLogMessage(instanceName() , _strBuilder.toString(), Severity.ERROR));
        	}
			
			userLock().unlock();
			strBuilder().append("Failed to submit StatusMsg. Reason: ")
				.append(ReactorReturnCodes.toString(ret))
				.append(". Error text: ")
				.append(_rsslErrorInfo.error().text());
			
			handleInvalidUsage(_strBuilder.toString());
			return;
	    }
		
		if (statusMsgImpl.state().streamState() == OmmState.StreamState.CLOSED || 
				statusMsgImpl.state().streamState() == OmmState.StreamState.CLOSED_RECOVER || 
						statusMsgImpl.state().streamState() == OmmState.StreamState.CLOSED_REDIRECTED)
		{
			_handleToStreamInfo.remove(_longObject.value(handle));
			streamInfo.returnToPool();
			returnProviderStreamId(statusMsgImpl._rsslMsg.streamId());
		}
		
		userLock().unlock();
	}
	
	@Override
	public void submit(GenericMsg genericMsg, long handle)
	{
		userLock().lock();
			
		if ( _channelCallbackClient == null )
		{
			userLock().unlock();
			return;
		}
		
		if(_activeChannelInfo == null)
		{
			userLock().unlock();
			handleInvalidUsage(strBuilder().append("No active channel to send message.").toString());
			return;
		}
		
		if ( loggerClient().isTraceEnabled() )
		{
			loggerClient().trace(formatLogMessage(instanceName() , strBuilder().append("Received GenericMsg; Handle = ")
					.append(handle).append(", user assigned streamId = ").append(genericMsg.streamId()).append(".").toString(), Severity.TRACE));
		}
		
		StreamInfo streamInfo = _handleToStreamInfo.get(_longObject.value(handle));
		
		if ( streamInfo != null )
		{
			((GenericMsgImpl)genericMsg).streamId(streamInfo.streamId());
			if (((GenericMsgImpl) genericMsg)._rsslMsg.domainType() == 0)
				((GenericMsgImpl) genericMsg)._rsslMsg.domainType(streamInfo.domainType());
		}
		else
		{
			userLock().unlock();
			strBuilder().append("Attempt to submit GenericMsg on stream that is not open yet. Handle = ")
			.append(handle).append(".");
			handleInvalidHandle(handle, _strBuilder.toString());
			return;
		}
		
		_rsslErrorInfo.clear();
		int ret;
		if (ReactorReturnCodes.SUCCESS > (ret = _activeChannelInfo.rsslReactorChannel().submit(((GenericMsgImpl)genericMsg)._rsslMsg, _rsslSubmitOptions, _rsslErrorInfo)))
	    {
			if (loggerClient().isErrorEnabled())
        	{
				com.thomsonreuters.upa.transport.Error error = _rsslErrorInfo.error();
				
	        	strBuilder().append("Internal error: rsslChannel.submit() failed in OmmNiProviderImpl.submit(GenericMsg)")
	        		.append("RsslChannel ").append(Integer.toHexString(error.channel() != null ? error.channel().hashCode() : 0)) 
	    			.append(OmmLoggerClient.CR)
	    			.append("Error Id ").append(error.errorId()).append(OmmLoggerClient.CR)
	    			.append("Internal sysError ").append(error.sysError()).append(OmmLoggerClient.CR)
	    			.append("Error Location ").append(_rsslErrorInfo.location()).append(OmmLoggerClient.CR)
	    			.append("Error Text ").append(error.text());
	        	
	        	loggerClient().error(formatLogMessage(instanceName() , _strBuilder.toString(), Severity.ERROR));
        	}
			
			userLock().unlock();
			strBuilder().append("Failed to submit GenericMsg. Reason: ")
				.append(ReactorReturnCodes.toString(ret))
				.append(". Error text: ")
				.append(_rsslErrorInfo.error().text());
				
			handleInvalidUsage(_strBuilder.toString());
			return;
	    }
		
		userLock().unlock();
	}

	@Override
	void notifyErrorClient(OmmException ommException) {
		switch (ommException.exceptionType())
		{
		case ExceptionType.OmmInvalidHandleException:
			_providerErrorClient.onInvalidHandle(((OmmInvalidHandleException) ommException).handle(), ommException.getMessage());
			break;
		case ExceptionType.OmmInvalidUsageException:
			_providerErrorClient.onInvalidUsage(ommException.getMessage());
			break;
		default:
			break;
		}
	}

	@Override
	public String formatLogMessage(String clientName, String temp, int level) {
		strBuilder().append("loggerMsg\n").append("    ClientName: ").append(clientName).append("\n")
        .append("    Severity: ").append(OmmLoggerClient.loggerSeverityAsString(level)).append("\n")
        .append("    Text:    ").append(temp).append("\n").append("loggerMsgEnd\n\n");

		return _strBuilder.toString();
	}

	@Override
	public String instanceName() 
	{
		return _activeConfig.instanceName;
	}

	@Override
	boolean hasErrorClient() {
		return _providerErrorClient != null ? true : false;
	}

	@Override
	void readCustomConfig(EmaConfigImpl config)
	{
		_activeConfig.dictionaryConfig = new DictionaryConfig(true);
		
		_activeConfig.dictionaryConfig.rdmfieldDictionaryFileName = "RDMFieldDictionary";
		_activeConfig.dictionaryConfig.enumtypeDefFileName = "enumtype.def";
		
		_ommNiProviderDirectoryStore.loadConfigDirectory(config);
		
		ConfigAttributes niProviderAttributes = getAttributes(config);
		
		if(niProviderAttributes != null)
		{
			ConfigElement element = (ConfigElement)niProviderAttributes.getElement(ConfigManager.NiProviderRefreshFirstRequired);
			
			if (element != null)
			{
				_activeConfig.refreshFirstRequired = element.intLongValue() > 0 ? true : false;
			}
			
			element = (ConfigElement)niProviderAttributes.getElement(ConfigManager.NiProviderMergeSourceDirectoryStreams);
			
			if (element != null)
			{
				_activeConfig.mergeSourceDirectoryStreams = element.intLongValue() > 0 ? true : false;
			}
			
			element = (ConfigElement)niProviderAttributes.getElement(ConfigManager.NiProviderRecoverUserSubmitSourceDirectory);
			
			if (element != null)
			{
				_activeConfig.recoverUserSubmitSourceDirectory = element.intLongValue() > 0 ? true : false;
			}
			
			element = (ConfigElement)niProviderAttributes.getElement(ConfigManager.NiProviderRemoveItemsOnDisconnect);
			
			if (element != null)
			{
				_activeConfig.removeItemsOnDisconnect = element.intLongValue() > 0 ? true : false;
			}
		}
		
		ProgrammaticConfigure pc = config.programmaticConfigure();
		if ( pc != null )
			pc.retrieveCustomConfig(_activeConfig.configuredName, _activeConfig);
	}

	@Override
	void processChannelEvent(ReactorChannelEvent reactorChannelEvent) {
		switch ( reactorChannelEvent.eventType() )
		{
		case ReactorChannelEventTypes.CHANNEL_DOWN:
		case ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING:
			userLock().lock();
			
			if ( _itemWatchList != null )
				_itemWatchList.processChannelEvent(reactorChannelEvent);
			
			if ( _activeConfig.removeItemsOnDisconnect )
				removeItems();
			
			_activeChannelInfo = null;
			userLock().unlock();
			break;
		default:
			break;
		}
		
	}
	
	void removeItems()
	{
		_bIsStreamIdZeroRefreshSubmitted = false;
		
		Set<Entry<LongObject, StreamInfo>> entrySet = _handleToStreamInfo.entrySet();
		
		for (Entry<LongObject, StreamInfo> entry : entrySet)
		{
			_objManager._streamInfoPool.add(entry.getValue());
		}
		
		_handleToStreamInfo.clear();
		
		_ommNiProviderDirectoryStore.clearMap();
	}

	@Override
	Logger createLoggerClient() {
		return LoggerFactory.getLogger(OmmNiProviderImpl.class);
	}

	@Override
	ConfigAttributes getAttributes(EmaConfigImpl config)
	{
		return config.xmlConfig().getNiProviderAttributes(_activeConfig.configuredName);
	}
	
	@Override
	Object getAttributeValue(EmaConfigImpl config, int attributeKey)
	{
		return config.xmlConfig().getNiProviderAttributeValue(_activeConfig.configuredName, attributeKey);
	}

	@Override
	void handleAdminDomains() {
		
		_loginCallbackClient = new LoginCallbackClientProvider(this);
		_loginCallbackClient.initialize();

		_itemCallbackClient = new ItemCallbackClientProvider(this);
		_itemCallbackClient.initialize();

		_channelCallbackClient = new ChannelCallbackClient<>(this,_rsslReactor);
		
		if(_adminClient != null)
		{
			/* RegisterClient does not require a fully encoded login message to set the callbacks */
			loginRequest.clear().domainType(EmaRdm.MMT_LOGIN);
			_itemCallbackClient.registerClient(loginRequest, _adminClient, _adminClosure, 0);
		}

		
		if ( _activeConfig.directoryAdminControl == OmmNiProviderConfig.AdminControl.API_CONTROL)
		{
			_channelCallbackClient.initializeNiProviderRole(_loginCallbackClient.rsslLoginRequest(), 
					DirectoryServiceStore.getDirectoryRefreshMsg(_ommNiProviderDirectoryStore.getApiControlDirectory(), true ));
		}
		else
		{
			if (loggerClient().isTraceEnabled())
			{
				loggerClient().trace(formatLogMessage(_activeConfig.instanceName, "DirectoryAdminControl = UserControl", Severity.TRACE));
			}
			
			_channelCallbackClient.initializeNiProviderRole(_loginCallbackClient.rsslLoginRequest(), null);
		}

		handleLoginReqTimeout();
	}

	@Override
	public void handleInvalidUsage(String text)
	{
		if ( hasErrorClient() )
			_providerErrorClient.onInvalidUsage(text);
		else
			throw (ommIUExcept().message(text.toString()));
		
	}

	@Override
	public void handleInvalidHandle(long handle, String text)
	{	
		if ( hasErrorClient() )
			_providerErrorClient.onInvalidHandle(handle, text);
		else
			throw (ommIHExcept().message(text, handle));
	}
	
	void remapServiceIdAndServcieName(DirectoryRefresh directoryRefresh)
	{
		List<Service> serviceList = directoryRefresh.serviceList();
		
		for(int index = 0; index < serviceList.size(); index++ )
		{
			_ommNiProviderDirectoryStore.addToMap(serviceList.get(index));
		}	
	}
	
	int submitDirectoryRefresh(DirectoryRefresh directoryRefresh)
	{
		int retCode = CodecReturnCodes.SUCCESS;
		
		Buffer encodedBuffer = CodecFactory.createBuffer();
		
		encodedBuffer.data(ByteBuffer.allocate(1024));
		
		EncodeIterator encodeIt = CodecFactory.createEncodeIterator();
		encodeIt.clear();
		
		StringBuilder errorText = new StringBuilder();
		
		if ( encodeIt.setBufferAndRWFVersion(encodedBuffer, Codec.majorVersion(),Codec.minorVersion()) != CodecReturnCodes.SUCCESS )
		{
			errorText.append("Internal error. Failed to set encode iterator buffer and version in OmmNiProviderImpl.reLoadConfigSourceDirectory().");
			handleInvalidUsage(errorText.toString());
		}
		
		while ( ( retCode = directoryRefresh.encode(encodeIt) ) == CodecReturnCodes.BUFFER_TOO_SMALL )
		{
			encodedBuffer = Utilities.realignBuffer(encodeIt, encodedBuffer.capacity() * 2);
		}
		
		if ( retCode != CodecReturnCodes.SUCCESS )
		{
			errorText.append("Internal error. Failed to encode buffer from DirectoryRefresh in OmmNiProviderImpl.reLoadConfigSourceDirectory().").append(" Reason = ");
			errorText.append( CodecReturnCodes.toString(retCode) ).append(".");
			handleInvalidUsage(errorText.toString());
		}
		
		DecodeIterator decodeIt = CodecFactory.createDecodeIterator();
		decodeIt.clear();
		
		if ( decodeIt.setBufferAndRWFVersion(encodedBuffer, Codec.majorVersion(), Codec.minorVersion()) != CodecReturnCodes.SUCCESS )
		{
			errorText.append("Internal error. Failed to set decode iterator buffer and version in OmmNiProviderImpl.reLoadConfigSourceDirectory().");
			handleInvalidUsage(errorText.toString());
		}
	
		com.thomsonreuters.upa.codec.RefreshMsg rsslRefreshMsg = (com.thomsonreuters.upa.codec.RefreshMsg)CodecFactory.createMsg();
		rsslRefreshMsg.clear();
		
		if ( rsslRefreshMsg.decode(decodeIt) != CodecReturnCodes.SUCCESS )
		{
			errorText.append("Internal error. Failed to decode message in OmmNiProviderImpl.reLoadConfigSourceDirectory().");
			handleInvalidUsage(errorText.toString());
		}
		
		int flags = rsslRefreshMsg.flags();
		flags &= ~RefreshMsgFlags.SOLICITED;
		rsslRefreshMsg.flags(flags);
		
		if( _activeConfig.removeItemsOnDisconnect )
		{
			remapServiceIdAndServcieName(directoryRefresh);
		}
		
		if(_activeChannelInfo == null)
		{
			errorText.append("No active channel to send message.");
			handleInvalidUsage(errorText.toString());
			return CodecReturnCodes.FAILURE;
		}
		
		_rsslErrorInfo.clear();
		if (ReactorReturnCodes.SUCCESS > (retCode = _activeChannelInfo.rsslReactorChannel().submit(rsslRefreshMsg, _rsslSubmitOptions, _rsslErrorInfo)))
	    {			
			StringBuilder temp = strBuilder();
			if (loggerClient().isErrorEnabled())
        	{
				com.thomsonreuters.upa.transport.Error error = _rsslErrorInfo.error();
				
	        	temp.append("Internal error: rsslChannel.submit() failed in OmmNiProviderImpl.reLoadConfigSourceDirectory().")
	        		.append("RsslChannel ").append(Integer.toHexString(error.channel() != null ? error.channel().hashCode() : 0)) 
	    			.append(OmmLoggerClient.CR)
	    			.append("Error Id ").append(error.errorId()).append(OmmLoggerClient.CR)
	    			.append("Internal sysError ").append(error.sysError()).append(OmmLoggerClient.CR)
	    			.append("Error Location ").append(_rsslErrorInfo.location()).append(OmmLoggerClient.CR)
	    			.append("Error Text ").append(error.text());
	        	
	        	loggerClient().error(formatLogMessage(instanceName() , temp.toString(), Severity.ERROR));
	        	
	        	temp.setLength(0);
        	}
			
			temp.append("Failed to submit RefreshMsg. Reason: ")
				.append(ReactorReturnCodes.toString(retCode))
				.append(". Error text: ")
				.append(_rsslErrorInfo.error().text());
				
			handleInvalidUsage(temp.toString());
	    }
		
		return retCode;
	}
	
	void reLoadConfigSourceDirectory()
	{
		if ( _activeConfig.directoryAdminControl != OmmNiProviderConfig.AdminControl.API_CONTROL )
			return;
		
		DirectoryRefresh directoryRefresh = _ommNiProviderDirectoryStore.getApiControlDirectory().getDirectoryRefresh();
		
		if ( directoryRefresh.serviceList().size() == 0 )
			return;
		
		if ( loggerClient().isTraceEnabled() )
		{
			loggerClient().trace( formatLogMessage(_activeConfig.instanceName, "Reload of configured source directories.", Severity.TRACE) );
		}
		
		if( _activeConfig.removeItemsOnDisconnect )
		{
			remapServiceIdAndServcieName(directoryRefresh);
		}
		
		_bIsStreamIdZeroRefreshSubmitted = true;
		
		if ( loggerClient().isTraceEnabled() )
		{
			loggerClient().trace( formatLogMessage(_activeConfig.instanceName, "Configured source directoies were sent out on the wire after reconnect.", Severity.TRACE) );
		}
	}
	
	void reLoadUserSubmitSourceDirectory()
	{
		if ( !_activeConfig.recoverUserSubmitSourceDirectory )
			return;
		
		DirectoryRefresh directoryRefresh = DirectoryServiceStore.getDirectoryRefreshMsg(_ommNiProviderDirectoryStore.getDirectoryCache(), false );
		
		if ( directoryRefresh.serviceList().size() == 0 )
			return;
		
		if ( loggerClient().isTraceEnabled() )
		{
			loggerClient().trace( formatLogMessage(_activeConfig.instanceName, "Reload of user submitted source directories.", Severity.TRACE) );
		}
		
		if ( submitDirectoryRefresh(directoryRefresh) == ReactorReturnCodes.SUCCESS )
		{
			_bIsStreamIdZeroRefreshSubmitted = true;
			
			if ( loggerClient().isTraceEnabled() )
			{
				loggerClient().trace( formatLogMessage(_activeConfig.instanceName, "User submitted source directoies were sent out on the wire after reconnect.", Severity.TRACE) );
			}
		}
	}
	
	@Override
	void reLoadDirectory()
	{
		reLoadConfigSourceDirectory();
		reLoadUserSubmitSourceDirectory();
	}
	
	@Override
	public void submit(AckMsg ackMsg, long handle)
	{
		StringBuilder text = strBuilder();
		
		if (loggerClient().isErrorEnabled())
    	{
			text.append("Non interactive provider role does not support submitting AckMsg on handle =  ")
			.append(handle);
			
			loggerClient().error(formatLogMessage(instanceName() , text.toString(), Severity.ERROR));
			
			text.setLength(0);
    	}
		
		text.append("Failed to submit AckMsg. Reason: ")
		.append("Non interactive provider role does not support submitting AckMsg on handle =  ")
		.append(handle);
		
		handleInvalidUsage(text.toString());
	}

	@Override
	public int providerRole()
	{
		return OmmProviderConfig.ProviderRole.NON_INTERACTIVE;
	}
	
	int nextProviderStreamId()
	{		
		if ( _reusedProviderStreamIds.size() == 0 )
		{
			if ( _nextProviderStreamId == Integer.MIN_VALUE )
			{
				StringBuilder temp = strBuilder();
				temp.append("Unable to obtain next available stream id for submitting item.");
				handleInvalidUsage(temp.toString());
			}
			
			return --_nextProviderStreamId;
		}
		else
		{
			IntObject streamId = _reusedProviderStreamIds.remove(0);
			if (streamId != null)
			{
				int retValue = streamId.value();
				streamId.returnToPool();
				return retValue;
			}
			else
				return --_nextProviderStreamId;
		}				
	}
			
	void returnProviderStreamId(int streamId)
	{ 
		_reusedProviderStreamIds.add(_objManager.createIntObject().value(streamId));
	}
	
	void setActiveRsslReactorChannel(ChannelInfo activeChannelInfo)
	{
		_activeChannelInfo = activeChannelInfo;
	}
	
	void unsetActiveRsslReactorChannel(ChannelInfo cancelChannelInfo)
	{
		if (_activeChannelInfo == cancelChannelInfo)
			_activeChannelInfo = null;
	}

	static class StreamType
	{
		final static int CONSUMING = 1;
		final static int PROVIDING = 2;
	}
	
	class StreamInfo extends VaNode
	{
		private int _streamId;
		private int _serviceId;
		private int _domainType;
		private int _streamType;
		private LongObject _handle;
		
		StreamInfo(int streamType, int streamId)
		{
			_streamType = streamType;
			_streamId = streamId;
			_serviceId = 0;
			_handle = new LongObject();
		}
		
		StreamInfo(int streamType, int streamId, int domainType)
		{
			_streamType = streamType;
			_streamId = streamId;
			_serviceId = 0;
			_domainType = domainType;
			_handle = new LongObject();
		}
		
		StreamInfo(int streamType, int streamId, int serviceId, int domainType)
		{
			_streamType = streamType;
			_streamId = streamId;
			_serviceId = serviceId;
			_domainType = domainType;
			_handle = new LongObject();
		}
		
		void set(int streamType, int streamId)
		{
			_streamType = streamType;
			_streamId = streamId;
		}
		
		void set(int streamType, int streamId, int serviceId)
		{
			_streamType = streamType;
			_streamId = streamId;
			_serviceId = serviceId;
		}
		
		void set(int streamType, int streamId, int serviceId, int domainType)
		{
			_streamType = streamType;
			_streamId = streamId;
			_serviceId = serviceId;
			_domainType = domainType;
		}
		
		void handle(long handle)
		{
			_handle.value(handle);
		}
		
		void clear()
		{
			_streamId = 0;
			_serviceId = 0;
			_domainType = 0;
			_streamType = 0;
		}
		
		StreamInfo(StreamInfo other)
		{
			_streamId = other._streamId;
			_serviceId = other._serviceId;
			_domainType = other._domainType;
			_streamType = other._streamType;
		}
		
		int streamId()
		{
			return _streamId;
		}
		
		int serviceId()
		{
			return _serviceId;
		}

		int domainType()
		{
			return _domainType;
		}
		
		int streamType()
		{
			return _streamType;
		}
		
		LongObject handle()
		{
			return _handle;
		}
	}
	
	DirectoryServiceStore directoryServiceStore()
	{
		return _ommNiProviderDirectoryStore;
	}

	@Override
	public int implType() {
		return OmmCommonImpl.ImplementationType.NIPROVIDER;
	}

	@Override
	public long nextLongId() {
		
		long id = _longId;
		
		while( _handleToStreamInfo.containsKey(_longObject.value(id)) )
		{
			id = ++_longId;
			
			if ( _longId == MAX_LONG_VALUE )
			{
				_longId = MIN_LONG_VALUE;
			}
		}
		
		return id;
	}

	ItemWatchList itemWatchList() {
		return _itemWatchList;
	}
	
	int requestTimeout() {
		return _activeConfig.requestTimeout;
	}

	@Override
	public void onServiceDelete(ClientSession clientSession, int serviceId) {
		_itemWatchList.processServiceDelete(clientSession, serviceId);
	}

	@Override
	public void onServiceStateChange(ClientSession clientSession, int serviceId, ServiceState serviceState) {
	}

	@Override
	public void onServiceGroupChange(ClientSession clientSession, int serviceId, List<ServiceGroup> serviceGroupList) {
	}
}

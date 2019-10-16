/*
 * Copyright 2019 snowaver.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.mashroom.squirrel.module.call.manager;

import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.call.model.CallRoomStatus;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XMemTableCache;
import  io.netty.channel.Channel;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  lombok.NonNull;

@NoArgsConstructor( access = AccessLevel.PRIVATE )

public  class      CallManager  implements  Plugin
{
	private  ScheduledThreadPoolExecutor  callTimeoutChecker = new  ScheduledThreadPoolExecutor( new  Integer(System.getProperty("call.timeout.checker.poolSize","2")) );
	
	public  void  process( long  roomId,Channel  channel,long  contactId,Packet  packet )
	{
		//  sdp  and  candidate  packet  exchanging  is  very  frequent  and  they  are  controlled  by  the  client,   so  route  it  without  call  state  verifying.
		if( packet instanceof SDPPacket || packet instanceof CandidatePacket  /* both  peers  are  exchanging  sdp  and  candidate  informations  after  the  call  is  connected. */ )
		{
			/* if( callRoomStatus.getInteger("STATE") == 2 && (callRoomStatus.getLong("CALLER_ID") == contactId  && callRoomStatus.getLong("CALLEE_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() || callRoomStatus.getLong("CALLER_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() && callRoomStatus.getLong("CALLEE_ID") == contactId) ) */
			{
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
		}
		
		CallRoomStatus  callRoomStatus = this.callRoomStatusCache.lookupOne( CallRoomStatus.class,"SELECT  ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{Math.min(contactId,channel.attr(ConnectPacket.CLIENT_ID).get())+":"+Math.max(contactId,channel.attr(ConnectPacket.CLIENT_ID).get()),roomId} );
		
		if( callRoomStatus ==   null )
		{
			channel.write( new  CloseCallPacket(contactId,roomId,callRoomStatus == null ? CloseCallReason.ROOM_NOT_FOUND : CloseCallReason.STATE_ERROR) );
			
			return;
		}
		if( packet instanceof    CallPacket      )
		{
			if( callRoomStatus.getState() == 0 && this.callRoomStatusCache.update("UPDATE  CALL_ROOM_STATUS  SET  STATE = ?  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{1,callRoomStatus.getId(),roomId}) )
			{
				channel.attr( CallPacket.CALL_ROOM_ID ).set( roomId );
				
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
		}
		if( packet instanceof    CallAckPacket   )
		{
			if( callRoomStatus.getState() == 1 && ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode() == CallAckPacket.ACK_AGREE && callRoomStatusCache.update("UPDATE  CALL_ROOM_STATUS  SET  STATE = ?  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{2,callRoomStatus.getId(),roomId}) )
			{
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
			else
			if( callRoomStatus.getState() == 1 && ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode()   == CallAckPacket.ACK_AGREE )
			{
				this.callRoomStatusCache.update( "DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{callRoomStatus.getId(),roomId} );
			
				PacketRoute.INSTANCE.route(contactId,new  CloseCallPacket(channel.attr(ConnectPacket.CLIENT_ID).get(),roomId,CloseCallReason.DECLINE) );
			}
		}
		else
		if( packet instanceof    CloseCallPacket )
		{
			if( callRoomStatus.getState() == 1 && this.callRoomStatusCache.update("DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{callRoomStatus.getId(),roomId}) )
			{
				PacketRoute.INSTANCE.route( contactId,ObjectUtils.cast(packet,CloseCallPacket.class).setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()).setReason(CloseCallReason.CANCEL ) );
			}
			else
			if( callRoomStatus.getState() == 2 && this.callRoomStatusCache.update("DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{callRoomStatus.getId(),roomId}) )
			{
				PacketRoute.INSTANCE.route( contactId,ObjectUtils.cast(packet,CloseCallPacket.class).setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()).setReason(CloseCallReason.BY_USER) );
			}
		}
	}
	
	public  void  initialize( Object ...  params )   throws  Exception
	{
		this.callRoomStatusCache = CacheFactory.getOrCreateMemTableCache( "CALL_ROOM_STATUS_CACHE" );
	}
	
	public  void  initialize()   throws  Exception
	{
		this.initialize( null );
	}
	
	public  void  stop()
	{
		callTimeoutChecker.shutdown();
	}
	
	public  final  static  CallManager  INSTANCE = new  CallManager();
	
	private   XMemTableCache  callRoomStatusCache;
	
	public  void  scheduleTerminate(String  id,long  roomId,long  timeout,TimeUnit  timeoutTimeUnit )
	{
		callTimeoutChecker.schedule(() -> terminate(roomId,CloseCallReason.TIMEOUT),timeout  , timeoutTimeUnit );
	}
	
	public  void  terminate( long  roomId,@NonNull CloseCallReason  closeCallReason )
	{
		if( closeCallReason != CloseCallReason.TIMEOUT &&      closeCallReason != CloseCallReason.NETWORK_ERROR )
		{
			throw  new  IllegalArgumentException( "SQUIRREL-SERVER:  ** CALL  MANAGER **  only  CloseCallReason.TIMEOUT  and  CloseCallReason.NETWORK_ERROR  are  acceptable." );
		}
		
		CallRoomStatus  callRoomStatus = this.callRoomStatusCache.lookupOne(    CallRoomStatus.class,"SELECT  ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON  FROM  CALL_ROOM_STATUS  WHERE  CALL_ROOM_ID = ?",new  Object[]{roomId} );
		
		if( callRoomStatus != null && (callRoomStatus.getState()==2 || closeCallReason == CloseCallReason.TIMEOUT && callRoomStatus.getState() != 2) )  //  call  is  not  accepted  or  rejected, close.
		{
			this.callRoomStatusCache.update( "DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",new  Object[]{roomId} );
			
			PacketRoute.INSTANCE.route( callRoomStatus.getCallerId(),new  CloseCallPacket(callRoomStatus.getCalleeId(),callRoomStatus.getCallRoomId(),closeCallReason) );  PacketRoute.INSTANCE.route(  callRoomStatus.getCalleeId(),new  CloseCallPacket(callRoomStatus.getCallerId(),callRoomStatus.getCallRoomId(),closeCallReason) );
		}
	}
}

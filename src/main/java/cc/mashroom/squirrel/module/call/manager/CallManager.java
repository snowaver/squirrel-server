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

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XCache;
import  io.netty.channel.Channel;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )

public  class      CallManager  implements  Plugin
{
	public  void  process( long  roomId,Channel  channel,long  contactId,Packet  packet )
	{
		Map<String,Object>  callRoomStatus = callRoomStatusCache.getOne( "SELECT  ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON  FROM  CALL_ROOM_STATUS  WHERE  CALL_ROOM_ID = ?",new  Object[]{roomId} );
		
		if( callRoomStatus == null || !(callRoomStatus.getLong( "CALLER_ID") == contactId && callRoomStatus.getLong("CALLEE_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() || callRoomStatus.getLong("CALLER_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() && callRoomStatus.getLong("CALLEE_ID") == contactId) )
		{
			channel.write( new  CloseCallPacket(contactId,roomId,callRoomStatus == null ? CloseCallReason.ROOM_NOT_FOUND : CloseCallReason.STATE_ERROR) );
			
			return;
		}
		
		if( packet instanceof    CallPacket      )
		{
			if( callRoomStatus.getInteger("STATE") == 0 && callRoomStatusCache.update("UPDATE  CALL_ROOM_STATUS  SET  STATE = ?  WHERE  CALL_ROOM_ID = ?",new  Object[]{1 , roomId}) )
			{
				channel.attr( CallPacket.CALL_ROOM_ID ).set( roomId );
				
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
		}
		else
		if( packet instanceof    CallAckPacket   )
		{
			if( callRoomStatus.getInteger("STATE") == 1 && ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode() == CallAckPacket.ACK_ACCEPT && callRoomStatusCache.update("UPDATE  CALL_ROOM_STATUS  SET  STATE = ?  WHERE  CALL_ROOM_ID = ?",new  Object[]{2,roomId}) )
			{
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
			else
			if( callRoomStatus.getInteger("STATE") == 1 && ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode()    == CallAckPacket.ACK_REJECT )
			{
				callRoomStatusCache.update( "UPDATE  CALL_ROOM_STATUS  SET  STATE = ?,CLOSE_REASON = ?,CLOSED_BY = ?  WHERE  CALL_ROOM_ID = ?",new  Object[]{3,CloseCallReason.REJECT.getValue(),channel.attr(ConnectPacket.CLIENT_ID).get(),roomId} );
			
				PacketRoute.INSTANCE.route( contactId,new  CloseCallPacket(channel.attr(ConnectPacket.CLIENT_ID).get(), roomId, CloseCallReason.REJECT) );
			}
		}
		else
		if( packet instanceof    CloseCallPacket )
		{
			if( callRoomStatus.getInteger("STATE") == 1 && callRoomStatusCache.update("DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?",new  Object[]{callRoomStatus.getString("ID")}) )
			{
				PacketRoute.INSTANCE.route( contactId,ObjectUtils.cast(packet,CloseCallPacket.class).setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()).setReason(CloseCallReason.CANCEL) );
			}
			else
			if( callRoomStatus.getInteger("STATE") == 2 && callRoomStatusCache.update("DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?",new  Object[]{callRoomStatus.getString("ID")}) )
			{
				PacketRoute.INSTANCE.route( contactId,ObjectUtils.cast(packet,CloseCallPacket.class).setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()).setReason(CloseCallReason.CLOSE_ACTIVELY) );
			}
			//  history  logs  can  be  add  here.
			channel.writeAndFlush( new  QosReceiptPacket<>(contactId , packet.getId()) );
		}
		else
		if( packet instanceof SDPPacket || packet instanceof CandidatePacket /* both  peers  are  exchanging  sdp  and  candidate  informations  after  the  call  is  connected. */ )
		{
			if( callRoomStatus.getInteger("STATE") == 2 /* && (callRoomStatus.getLong("CALLER_ID") == contactId && callRoomStatus.getLong("CALLEE_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() || callRoomStatus.getLong("CALLER_ID") == channel.attr(ConnectPacket.CLIENT_ID).get() && callRoomStatus.getLong("CALLEE_ID") == contactId) */ )
			{
				PacketRoute.INSTANCE.route( contactId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
			}
		}
	}
	
	public  final  static  CallManager  INSTANCE = new  CallManager();
	
	private  XCache<?,?>  callRoomStatusCache;
	
	public  void  initialize()   throws  Exception
	{
		this.callRoomStatusCache  = CacheFactory.createCache( "CALL_ROOM_MEMBER_CACHE" );
	}
	
	public  void  stop()
	{
		
	}
}

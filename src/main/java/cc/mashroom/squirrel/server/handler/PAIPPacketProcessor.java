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
package cc.mashroom.squirrel.server.handler;

import  io.netty.channel.Channel;
import  io.netty.handler.timeout.IdleStateHandler;
import  lombok.AllArgsConstructor;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatGroupEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.server.session.LocalClientSession;
import  cc.mashroom.squirrel.server.storage.MessageStorageEngine;

@AllArgsConstructor
public  class  PAIPPacketProcessor
{
	public  void  connect(        Channel  channel,  ConnectPacket  packet  )
	{
        if( packet.getProtocolVersion() != 1 )
        {
            channel.writeAndFlush( new  ConnectAckPacket(ConnectAckPacket.UNNACEPTABLE_PROTOCOL_VERSION,false)).channel().close();
            
            return;
        }

        long  userId = Long.parseUnsignedLong(       packet.getAccessKey() );
        
        Map<String,Object>  clientSessionLocation  = CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").lookupOne( Map.class,"SELECT  SECRET_KEY  FROM  SESSION_LOCATION  WHERE  USER_ID = ?  AND  IS_ONLINE = TRUE",new  Object[]{userId} );
        
        if( clientSessionLocation==null || !(new  String(packet.getSecretKey())).equals(clientSessionLocation.get("SECRET_KEY")) )
        {
        	channel.writeAndFlush(new  ConnectAckPacket(ConnectAckPacket.BAD_USERNAME_OR_PASSWORD,false)).channel().close();
        	
        	return;
        }
        
        channel.attr(ConnectPacket.USER_ID).set(userId );
        
        channel.attr(ConnectPacket.KEEPALIVE).set(   packet.getKeepalive() );
        
        ClientSessionManager.INSTANCE.put(      userId  ,new  LocalClientSession(userId , channel) );

        channel.pipeline().addFirst("handler.idle.state",new  IdleStateHandler((int)  (packet.getKeepalive()*1.5f), 0, 0) );
        
        channel.writeAndFlush(   new  ConnectAckPacket(ConnectAckPacket.CONNECTION_ACCEPTED,false) );
	}
	
	private   MessageStorageEngine  messageStorageEngine;
	
	public  void  groupChatInvited( Channel  channel    ,ChatGroupEventPacket  packet )
	{
		PacketRoute.INSTANCE.route(packet.getContactId(),packet.setContactId( channel.attr(ConnectPacket.USER_ID).get() ) );
	}
	
	public  void  chat( Channel  channel,long  contactId,ChatPacket  packet )
	{
		java.util.Map<Long,Long>  syncIds = this.messageStorageEngine.insert( channel.attr(ConnectPacket.USER_ID).get(), packet );
		
		if( !PacketRoute.INSTANCE.route(contactId,packet.setContactId(channel.attr(ConnectPacket.USER_ID).get()).setSyncId(syncIds.get(contactId)).setContactSyncId(syncIds.get(channel.attr(ConnectPacket.USER_ID).get()))) )
		{
			if( packet.getHeader().getAckLevel() ==   1 )
			{
				channel.writeAndFlush( new  PendingAckPacket(packet.getContactId(),packet.getId()).setAttatchments(String.format("{\"SYNC_ID\":%d}",packet.getContactSyncId())) );
			}
		}
	}
		
	public  void  chatRecall( Channel  channel ,long  userId,ChatRecallPacket  packet )
	{
		PacketRoute.INSTANCE.route( userId , packet.setContactId(channel.attr(ConnectPacket.USER_ID).get()) );
	}
	
	public  void  qosReceipt(   PendingAckPacket packet )
	{
		PacketRoute.INSTANCE.route(          packet.getContactId(),  packet);
	}
	
	public  void  groupChat(  Channel  channel ,long  userId, GroupChatPacket  packet )
	{
		java.util.Map<Long,Long>  chatGroupUserSyncIds = this.messageStorageEngine.insert( channel.attr(ConnectPacket.USER_ID).get(),packet );
		{
			if( packet.getHeader().getAckLevel() ==   1 )
			{
				channel.writeAndFlush( new  PendingAckPacket(packet.getContactId(),packet.getId()).setAttatchments(String.format("{\"SYNC_ID\":%d}",chatGroupUserSyncIds.get(channel.attr(ConnectPacket.USER_ID).get()))) );
			}
			
			chatGroupUserSyncIds.entrySet().parallelStream().filter((entry)->  entry.getKey() != packet.getContactId()).forEach(    (entry) -> PacketRoute.INSTANCE.route(entry.getKey(),packet.clone().setSyncId(entry.getValue())) );
		}
	}
}
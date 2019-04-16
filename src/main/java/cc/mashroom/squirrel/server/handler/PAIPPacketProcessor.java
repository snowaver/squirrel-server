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
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.user.manager.OfflineMessageManager;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.server.session.LocalClientSession;

public  class  PAIPPacketProcessor
{
	public  void  connect(        Channel  channel, ConnectPacket  packet )
	{
        if( packet.getProtocolVersion() != 1 )
        {
            channel.writeAndFlush( new  ConnectAckPacket(ConnectAckPacket.UNNACEPTABLE_PROTOCOL_VERSION,false)).channel().close();
            
            return;
        }

        long  clientId= Long.valueOf( packet.getAccessKey() );
        
        Map<String,Object>  clientSessionLocation = CacheFactory.createCache("SESSION_LOCATION_CACHE").getOne( "SELECT  SECRET_KEY  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{clientId} );
        
        if( clientSessionLocation==null || !(new  String(packet.getSecretKey())).equals(clientSessionLocation.get("SECRET_KEY")) )
        {
        	channel.writeAndFlush( new  ConnectAckPacket(ConnectAckPacket.BAD_USERNAME_OR_PASSWORD     ,false)).channel().close();
        	
        	return;
        }
        //  the  old  session  should  be  removed  to  avoid  delayed  session  inactive  event  from  closing  the  new  created  session  managed  by  session  manager.
        ClientSession  oldSession  = ClientSessionManager.INSTANCE.remove( clientId );
        //  close  the  old  session  if  the  secret  key  is  different  from  the  old  one.  (NOTE:  a  client  holds  a  unique  secret  key,  so  should  not  close  the  session  with  the  same  secret  key)
        if( oldSession != null && clientSessionLocation != null && !(new  String(packet.getSecretKey())).equals(clientSessionLocation.get("SECRET_KEY")) )
        {
        	try
        	{
				oldSession.close( DisconnectAckPacket.REMOTE_LOGIN_ERROR );
			}
        	catch(Exception  e )
        	{
				
			}
        }
        
        channel.attr(ConnectPacket.KEEPALIVE).set( packet.getKeepalive() );
        
        channel.attr(ConnectPacket.CLIENT_ID).set( clientId );
        
        ClientSessionManager.INSTANCE.put(  clientId , new  LocalClientSession( clientId , channel ) );

        channel.pipeline().addFirst( "handler.idle.state",new  IdleStateHandler(0, 0, Math.round(packet.getKeepalive() * 1.5f)) );
        
        channel.writeAndFlush(     new  ConnectAckPacket(ConnectAckPacket.CONNECTION_ACCEPTED,false) );
	}
	
	public  void  chat(Channel  channel,long  clientId,ChatPacket  packet )
	{
		if( !PacketRoute.INSTANCE.route(clientId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get())) )
		{
			OfflineMessageManager.INSTANCE.store( channel.attr(ConnectPacket.CLIENT_ID).get() , packet.setContactId( clientId ) );

			if( /*packet instanceof Receiptable &&*/packet.getHeader().getQos() == 1 )
			{
				channel.writeAndFlush( new  QosReceiptPacket(packet.getContactId() , packet.getId()) );
			}
		}
	}
	
	public  void  chatRetract( Channel  channel , long  clientId , ChatRetractPacket  packet )
	{
		if( !PacketRoute.INSTANCE.route(clientId,packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get())) )
		{
			
		}
	}
	
	public  void  groupChat(Channel  channel,GroupChatPacket packet )
	{
		ChatGroupUserManager.INSTANCE.getChatGroupUserIds(packet.getGroupId()).forEach( (chatGroupUserId) -> {if( packet.getContactId() != chatGroupUserId ){ PacketRoute.INSTANCE.route(chatGroupUserId,packet); }} );
	}
	
	public  void  qosReceipt( QosReceiptPacket  packet )
	{
		PacketRoute.INSTANCE.route( packet.getContactId() , packet );
	}
	
	public  void  groupChatInvited( Channel  channel   ,GroupChatEventPacket  packet )
	{
		PacketRoute.INSTANCE.route(      packet.getContactId(),packet.setContactId(channel.attr(ConnectPacket.CLIENT_ID).get()) );
	}
}
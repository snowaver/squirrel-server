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

import  cc.mashroom.squirrel.paip.message.connect.ConnectAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.server.session.LocalClientSession;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  io.netty.channel.Channel;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  io.netty.handler.timeout.IdleStateHandler;
import  lombok.extern.slf4j.Slf4j;

@Slf4j
public  class  PAIPAuthorityAndSessionHandlerAdapter  extends  ChannelInboundHandlerAdapter
{
	@Override
	public  void  channelRead( ChannelHandlerContext  context,Object  packet )
	{
		if( packet instanceof  ConnectPacket )
		{
			authenticate( context.channel(),ObjectUtils.cast(packet) );
		}
		else
		if( !context.channel().hasAttr(ConnectPacket.USER_ID) )
		{
			context.close();
		}
	}
	@Override
	public  void  channelInactive( ChannelHandlerContext  context     )
	{
		Long  userId =    context.channel().attr(ConnectPacket.USER_ID).get();ClientSession  session= userId == null ? null : ClientSessionManager.INSTANCE.remove( userId );
		
		if( session!= null )
		{
		session.close( -1 );
		}
	}
	
	public  void  authenticate(Channel  channel,ConnectPacket  packet )
	{		
        if( packet.getProtocolVersion() != 1 )
        {
            channel.writeAndFlush(new  ConnectAckPacket(packet.getId(),ConnectAckPacket.UNNACEPTABLE_PROTOCOL_VERSION,false)).channel().close();  return;
        }
        
		long  userId = Long.parseUnsignedLong( packet.getAccessKey() );

        Map<String,Object>  clientSessionLocation = CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").lookupOne( Map.class,"SELECT  USER_ID,SECRET_KEY  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{userId} );
        
        if( clientSessionLocation == null || !(new  String(packet.getSecretKey())).equals(clientSessionLocation.getString("SECRET_KEY")) )
        {
        	channel.writeAndFlush(new  ConnectAckPacket(packet.getId(),ConnectAckPacket.BAD_USERNAME_OR_PASSWORD     ,false)).channel().close();  return;
        }
        //  the  old  session  should  be  removed  to  avoid  delayed  session  inactive  event  from  closing  the  new  created  session.
        ClientSession  session=ClientSessionManager.INSTANCE.remove( userId );
        //  close  the  old  session  if  the  secret  key  is  different  from  the  old  one.  (NOTE:  a  client  holds  a  unique  secret  key,  so  should  not  close  the  session  with  the  same  secret  key)
        if( session!= null )
        {
        	session.close(  DisconnectAckPacket.REASON_REMOTE_SIGNIN );
        }
        
        ClientSessionManager.INSTANCE.put(userId,new  LocalClientSession(userId,channel) );
        
        channel.attr(ConnectPacket.USER_ID).set( userId );

        channel.pipeline().addFirst( "handler.idle.state",new  IdleStateHandler((int)  (packet.getKeepalive()*1.5f),0,0) );
        
        channel.writeAndFlush( new  ConnectAckPacket(packet.getId(),ConnectAckPacket.CONNECTION_ACCEPTED,false) );
	}
}

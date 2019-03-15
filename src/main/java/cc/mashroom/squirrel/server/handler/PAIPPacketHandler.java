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

import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  io.netty.handler.codec.CorruptedFrameException;
import  lombok.AllArgsConstructor;

import org.joda.time.DateTime;

import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatInvitedPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;

@AllArgsConstructor

public  class  PAIPPacketHandler   extends  ChannelInboundHandlerAdapter
{
	protected  PAIPPacketProcessor    processor;
	
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  throwable )  throws  Exception
	{
		throwable.printStackTrace();
	}
	
	public  void  channelInactive( ChannelHandlerContext  context )  throws  Exception
	{
		Long  clientId =      context.channel().attr( ConnectPacket.CLIENT_ID ).get();
		
		System.err.println(DateTime.now().toString("yyyy-MM-dd HH:mm:ss")+"  CHANNEL.INACTIVE:\tCLIENT.ID="+clientId );
		
		if( clientId != null )
		{
			ClientSession  session = ClientSessionManager.INSTANCE.remove( clientId );
			
			if(    session != null )
			{
				/*
				System.err.println( String.format("the  connection  channel  is  inactive  now  so  close  the  session  (client  id:  %d)",clientId) );
				*/
				session.close( -1 );
			}
		}
	}
	
	public  void  channelRead( ChannelHandlerContext  context,Object  packet )  throws  Exception
	{
		System.err.println(     DateTime.now().toString("yyyy-MM-dd HH:mm:ss")+"  CHANNEL.READ:\t"+packet.toString() );
		
		if( packet instanceof ConnectPacket    )
		{
			this.processor.connect(    context.channel(),ObjectUtils.cast(packet , ConnectPacket.class ) );
		}
		else
		if( packet instanceof DisconnectPacket )
		{
			this.processor.disconnect( context.channel(),ObjectUtils.cast(packet,DisconnectPacket.class) );
		}
		else
		if( packet instanceof PingPacket )
		{
			context.channel().writeAndFlush(     new  PingAckPacket() );
		}
		else
		if( packet instanceof ChatPacket )
		{
			this.processor.chat( context.channel(),ObjectUtils.cast(packet,ChatPacket.class).getContactId(),ObjectUtils.cast(packet,ChatPacket.class) );
		}
		else
		if( packet instanceof QosReceiptPacket )
		{
			processor.qosReceipt( ObjectUtils.cast(packet , QosReceiptPacket.class) );
		}
		else
		if( packet instanceof CallPacket )
		{
			this.processor.call( context.channel(),ObjectUtils.cast(packet,CallPacket.class).getContactId(),ObjectUtils.cast(packet,CallPacket.class) );
		}
		else
		if( packet instanceof CallAckPacket    )
		{
			processor.callAck( context.channel(),ObjectUtils.cast(packet, CallAckPacket.class).getContactId(),ObjectUtils.cast(packet, CallAckPacket.class) );
		}
		else
		if( packet instanceof SDPPacket  )
		{
			this.processor.sdp( context.channel() ,ObjectUtils.cast(packet,SDPPacket.class).getContactId(), ObjectUtils.cast(packet ,SDPPacket.class) );
		}
		else
		if( packet instanceof CandidatePacket  )
		{
			this.processor.candidate( context.channel(),ObjectUtils.cast(packet,CandidatePacket.class).getContactId(),ObjectUtils.cast(packet,CandidatePacket.class) );
		}
		else
		if( packet instanceof CloseCallPacket  )
		{
			this.processor.closeCall( context.channel(),ObjectUtils.cast(packet,CloseCallPacket.class).getContactId(),ObjectUtils.cast(packet,CloseCallPacket.class) );
		}
		else
		if( packet instanceof GroupChatInvitedPacket )
		{
			this.processor.groupChatInvited( context.channel(),ObjectUtils.cast(packet,GroupChatInvitedPacket.class) );
		}
		else
		if( packet instanceof GroupChatPacket  )
		{
			this.processor.groupChat( context.channel() , ObjectUtils.cast(packet,GroupChatPacket.class) );
		}
		else
		if( packet instanceof ChatRetractPacket)
		{
			processor.chatRetract(context.channel(),ObjectUtils.cast(packet,ChatRetractPacket.class).getContactId(),ObjectUtils.cast(packet,ChatRetractPacket.class) );
		}
		else
		{
			throw  new  CorruptedFrameException( "SQUIRREL-SERVER:  ** PAIP  PACKET  HANDLER **  can  not  process  the  packet" );
		}
	}
}
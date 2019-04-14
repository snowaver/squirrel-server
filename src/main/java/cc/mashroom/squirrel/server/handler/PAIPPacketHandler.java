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
import  lombok.Setter;
import  lombok.SneakyThrows;

import  java.util.ArrayList;
import  java.util.List;

import  org.joda.time.DateTime;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.squirrel.paip.message.call.AbstractCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRetractPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.QosReceiptPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;

public  class  PAIPPacketHandler   extends  ChannelInboundHandlerAdapter
{
	@SneakyThrows
	public  PAIPPacketHandler(    PAIPPacketProcessor  packetProcessor )  //throws  InstantiationException,IllegalAccessException,ClassNotFoundException
	{
		this.setProcessor(    packetProcessor );
		
		for( String  processorClassName:System.getProperty("squirrel.server.externalProcessorClasses","").split("," ) )
		{
			if( StringUtils.isNotBlank(processorClassName) )
			{
				this.externalProcessors.add( ObjectUtils.cast(Class.forName(processorClassName),new  TypeReference<Class<? extends PAIPPacketExternalProcessor>>(){}).newInstance() );
			}
		}
	}
	
	protected  List<PAIPPacketExternalProcessor>  externalProcessors   = new  ArrayList<PAIPPacketExternalProcessor>();
	@Setter
	protected  PAIPPacketProcessor    processor;
	
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  throwable )  throws  Exception
	{
		throwable.printStackTrace();
	}
	
	public  void  channelInactive( ChannelHandlerContext  context )  throws  Exception
	{
		Long  clientId =      context.channel().attr( ConnectPacket.CLIENT_ID ).get();
		
		System.out.println(DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.LEFT:\tCLIENT.ID="+clientId );
		
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
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+packet.toString() );
		
		if( packet instanceof ConnectPacket    )
		{
			this.processor.connect(    context.channel(),ObjectUtils.cast(packet,   ConnectPacket.class) );
		}
		else
		/*
		if( packet instanceof DisconnectPacket )
		{
			this.processor.disconnect( context.channel(),ObjectUtils.cast(packet,DisconnectPacket.class) );
		}
		else
		*/
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
		if( packet instanceof CallPacket || packet instanceof CallAckPacket || packet instanceof SDPPacket || packet instanceof CandidatePacket || packet instanceof CloseCallPacket )
		{
			CallManager.INSTANCE.process( ObjectUtils.cast(packet,AbstractCallPacket.class).getRoomId(),context.channel(),ObjectUtils.cast(packet,AbstractCallPacket.class).getContactId(),ObjectUtils.cast(packet,AbstractCallPacket.class) );
		}
		else
		if( packet instanceof GroupChatEventPacket    )
		{
			this.processor.groupChatInvited( context.channel(),ObjectUtils.cast(packet,GroupChatEventPacket.class) );
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
			for( PAIPPacketExternalProcessor  externalProcessor : externalProcessors )
			{
				if( externalProcessor.process(ObjectUtils.cast(packet)))
				{
					return;
				}
			}
			
			throw  new  CorruptedFrameException( "SQUIRREL-SERVER:  ** PAIP  PACKET  HANDLER **  can  not  process  the  packet." );
		}
	}
}
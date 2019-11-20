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

import  java.util.ArrayList;
import  java.util.Arrays;
import  java.util.List;

import  org.joda.time.DateTime;

import  com.fasterxml.jackson.core.type.TypeReference;
import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.squirrel.paip.message.call.RoomPacket;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CandidatePacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.SDPPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatGroupEventPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.remote.RemoteCallable;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;

public  class    PAIPPacketInboundHandlerAdapter       extends  ChannelInboundHandlerAdapter
{
	public  PAIPPacketInboundHandlerAdapter(      PAIPPacketProcessor  processor )  //  throws  InstantiationException,IllegalAccessException,ClassNotFoundException
	{
		this.processor= processor;
		
		Lists.newArrayList(System.getProperty("squirrel.acceptor.externalProcessorClasses","").split(",")).stream().filter((processorClassName) -> StringUtils.isNotBlank(processorClassName)).forEach( (processorClassName) -> addExternalPacketProcessor(processorClassName) );
	}
	
	protected  List<PAIPPacketExternalProcessor>  externalProcessors =  new  ArrayList <PAIPPacketExternalProcessor>();
	@Setter
	protected  PAIPPacketProcessor    processor;
	
	protected  void  addExternalPacketProcessor(  String  processorClassName )
	{
		try
		{
			this.externalProcessors.add( ObjectUtils.cast(Class.forName(processorClassName),new  TypeReference<Class<? extends PAIPPacketExternalProcessor>>(){}).newInstance() );
		}
		catch(Throwable  e )
		{
			throw  new  IllegalStateException( String.format("SQUIRREL-SERVER:  ** PAIP  PACKET  HANDLER **  can  not  create  packet  external  processor  instance  for  class  %s.",processorClassName),e );
		}
	}
	
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  ukerror )    throws  Exception
	{
		ukerror.printStackTrace();
	}
	
	public  void  channelRead( ChannelHandlerContext  context,Object  packet )  throws Exception
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+packet.toString() );
		
		if( packet instanceof Route )
		{
			PAIPPacketRouter.INSTANCE.route( ObjectUtils.cast(packet,Route.class).getUserId(),ObjectUtils.cast(packet,Route.class).getPacket() );
		}
		else
		if( packet instanceof CallPacket || packet instanceof CallAckPacket ||  packet instanceof SDPPacket || packet instanceof CandidatePacket || packet instanceof CloseCallPacket )
		{
			CallManager.INSTANCE.process( ObjectUtils.cast(packet,RoomPacket.class).getRoomId(),context.channel(),ObjectUtils.cast(packet,RoomPacket.class).getContactId(),ObjectUtils.cast(packet,RoomPacket.class) );
		}
		else
		{
			if( !this.externalProcessors.stream().anyMatch((externalProcessor)  -> externalProcessor.process(ObjectUtils.cast(packet))) )
			{
				throw  new  CorruptedFrameException( "SQUIRREL-SERVER:  ** PAIP  PACKET  HANDLER **  the  packet  can  not  be  processed,  so  an  external  processor  is  required  for  the  packet." );
			}
		}
	}
}
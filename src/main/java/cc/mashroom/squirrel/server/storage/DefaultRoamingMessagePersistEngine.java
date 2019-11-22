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
package cc.mashroom.squirrel.server.storage;

import  java.io.FileNotFoundException;
import  java.sql.Timestamp;
import  java.util.List;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;
import  java.util.stream.Collectors;

import  org.apache.curator.shaded.com.google.common.collect.LinkedListMultimap;
import  org.springframework.context.annotation.DependsOn;
import  org.springframework.stereotype.Service;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.model.ChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.ChatMessage;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.server.handler.Route;
import  cc.mashroom.util.ObjectUtils;
import  io.netty.buffer.ByteBuf;
import  io.netty.buffer.Unpooled;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.SneakyThrows;

@Service
@DependsOn( value="PLUGIN_MANAGER" )
public  class  DefaultRoamingMessagePersistEngine<P extends Packet<P>>  implements  Plugin,RoamingMessagePersistEngine<P>,Runnable
{
	private  ThreadPoolExecutor  persistenceThreadPool= new  ThreadPoolExecutor( 8,8,2,TimeUnit.SECONDS,new  LinkedBlockingQueue<Runnable>(),new  DefaultThreadFactory("MESSAGE_STORAGE_PERSISTENCE_THREAD_POOL") );
		
	private  Thread  rolloverLooper= new  Thread( this, "MESSAGE_STORAGE_ROLLOVER_LOOPER" );
	
	private  RoamingMessageRollingFile     rollingFile= new  RoamingMessageRollingFile(   );
	
	private  LinkedListMultimap<String,Route<P>>  filePackets = LinkedListMultimap.create();
	@Override
	public  Route  <P>  prepersist( Route  <P>  route )
	{
		try
		{
			ByteBuf  byteBuf = route.getPacket().write( Unpooled.buffer(8).writeLongLE(route.getUserId()) );this.rollingFile.append( byteBuf.array() );  byteBuf.release();  this.filePackets.put( this.rollingFile.getFile().getName()+":"+route.getPacket().getPacketType().getValue(),route );  return  route;
		}
		catch( Exception  unknowne )
		{
			throw  new  RuntimeException(   unknowne );
		}
	}
	@SneakyThrows( value={InterruptedException.class} )
	@Override
	public  void  run ()
	{
		for( ;;Thread.sleep( 1000) )
		try
		{
			persist( rollingFile.getFile().getName() );
			
		this.rollingFile.rollover();
		}
		catch( FileNotFoundException  fnfex )
		{
			fnfex.printStackTrace();
		}
	}
	@Override
	public  <T>  List<T>  lookup( Class   <T>clazz,long  userId ,long  messageOffsetSyncId )
	{
		if( clazz == ChatGroupMessage.class )
		{
			return  ObjectUtils.cast( ChatGroupMessageRepository.DAO.lookup(ChatGroupMessage.class,"SELECT  ID,GROUP_ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,messageOffsetSyncId}).stream().map((message) -> message.setIsLocal(false).setCreateTime(new  Timestamp(message.getId()))).collect(Collectors.toList()),new  TypeReference<List<T>>(){} );
		}
		else
		{
			return  ObjectUtils.cast( ChatMessageRepository.DAO.lookup(ChatMessage.class,"SELECT  ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,messageOffsetSyncId}).stream().map((message) -> message.setIsLocal(false).setCreateTime(new  Timestamp(message.getId()))).collect(Collectors.toList()),new  TypeReference<List<T>>(){} );
		}
	}
	@Override
	public  void  initialize( Object  ...  parameters )
	{
		this.rolloverLooper.start();
	}
	@Override
	public  void  stop()
	{
	this.rolloverLooper.interrupt();
	}
	
	public  void  persist(    String  rollingFileName )
	{
		ChatMessageRepository.DAO.insert( ObjectUtils.cast(this.filePackets.get(rollingFileName+":"+PAIPPacketType.CHAT.getValue()),new  TypeReference<List<Route<ChatPacket>>>(){}) );
		
		this.filePackets.removeAll( rollingFileName+":"  + PAIPPacketType.CHAT.getValue() );
		
		ChatGroupMessageRepository.DAO.insert( ObjectUtils.cast(this.filePackets.get(rollingFileName+":"+PAIPPacketType.GROUP_CHAT.getValue()),new  TypeReference<List<Route<GroupChatPacket>>>(){}) );
	
		filePackets.removeAll( rollingFileName+":" + PAIPPacketType.GROUP_CHAT.getValue() );
	}
}
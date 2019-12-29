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

import  java.sql.Timestamp;
import  java.util.List;
import  java.util.concurrent.LinkedBlockingQueue;
import  java.util.concurrent.ThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.atomic.AtomicLong;
import  java.util.stream.Collectors;

import  org.apache.curator.shaded.com.google.common.collect.LinkedListMultimap;
import  org.joda.time.DateTime;
import  org.springframework.context.annotation.DependsOn;
import  org.springframework.stereotype.Service;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.model.ChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.ChatMessage;
import  cc.mashroom.squirrel.module.user.model.OoIData;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.server.handler.Route;
import  cc.mashroom.util.ObjectUtils;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.SneakyThrows;

@Service
@DependsOn( value="PLUGIN_MANAGER" )
public  class    DefaultRoamingMessagePersistAndRouteEngine<P extends Packet<P>>  implements  Plugin,RoamingMessagePersistAndRouteEngine<P>,Runnable
{
	private  ThreadPoolExecutor  persistenceThreadPool= new  ThreadPoolExecutor( 8,8,2,TimeUnit.SECONDS,new  LinkedBlockingQueue<Runnable>(),new  DefaultThreadFactory("MESSAGE_STORAGE_PERSISTENCE_THREAD_POOL") );
		
	private  Thread rolloverLooper = new  Thread( this, "MESSAGE_STORAGE_ROLLOVER_LOOPER" );
	
	private  AtomicLong  bucketKey = new  AtomicLong();
	
	private  LinkedListMultimap<String,Route<P>>  routeBuckets= LinkedListMultimap.create();
	@Override
	public  Route  <P>  prepersist( Route  <P>  route )
	{
		synchronized  (  bucketKey )
		{
			routeBuckets.put((route.getPacket() instanceof ChatPacket ? "S" : "G") + "_" + this.bucketKey.get(),route );
		return    route;
		}
	}
	@SneakyThrows( value={InterruptedException.class} )
	@Override
	public  void  run ()
	{
		for( ;;Thread.sleep( 100 ) )
		synchronized  (  bucketKey )
		{
			this.persistenceThreadPool.execute( () -> persist( this.bucketKey.getAndSet(DateTime.now().getMillis()) ) );
		}
	}
	@Override
	public  OoIData  lookup( long  userId,long  chatMessageOffsetSyncId,long  groupChatMessageOffsetSyncId )
	{
		return  new  OoIData().setOfflineChatMessages(ChatMessageRepository.DAO.lookup(ChatMessage.class,"SELECT  ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,chatMessageOffsetSyncId}).stream().map((message) -> message.setIsLocal(false).setCreateTime(new  Timestamp(message.getId()))).collect(Collectors.toList())).setOfflineGroupChatMessages( ChatGroupMessageRepository.DAO.lookup(ChatGroupMessage.class,"SELECT  ID,GROUP_ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,groupChatMessageOffsetSyncId}).stream().map((message) -> message.setIsLocal(false).setCreateTime(new  Timestamp(message.getId()))).collect(Collectors.toList()) );
	}
	@Override
	public  void  stop()
	{
		this.persistenceThreadPool.shutdown();
		
	this.rolloverLooper.interrupt();
	}
	@Override
	public  void  initialize( Object  ...  parameters )
	{
		this.rolloverLooper.start();
	}
	
	public  void  persist(      long  bucket )
	{
		ChatGroupMessageRepository.DAO.insertAndRoute( ObjectUtils.cast(this.routeBuckets.removeAll("G_"+bucket),new  TypeReference<List<Route<GroupChatPacket>>>(){}) );
		
		ChatMessageRepository.DAO.insertAndRoute( ObjectUtils.cast(this.routeBuckets.removeAll("S_"+bucket),new  TypeReference<List<Route<ChatPacket>>>(){}) );
	}
}
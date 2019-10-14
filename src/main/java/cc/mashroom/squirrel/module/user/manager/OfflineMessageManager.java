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
package cc.mashroom.squirrel.module.user.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.plugin.Plugin;
import cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import cc.mashroom.squirrel.module.chat.group.manager.ChatManager;
import  cc.mashroom.squirrel.module.user.repository.OfflineChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineGroupChatMessageRepository;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.util.ObjectUtils;
import cc.mashroom.xcache.CacheFactory;
import cc.mashroom.xcache.atomic.XAtomicLong;
import cc.mashroom.xcache.util.SafeCacher;
import  lombok.NoArgsConstructor;
import  lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor

public  class  OfflineMessageManager  implements  Plugin
{
	public  final  static  OfflineMessageManager  INSTANCE   = new  OfflineMessageManager();
	
	public  void  stop()
	{
		
	}
	
	private  XAtomicLong  getGroupChatMessageSyncId(long    userId )
	{
		XAtomicLong  groupChatMessageSyncId = CacheFactory.atomicLong( "SQUIRREL.GROUP_CHAT_MESSAGE.SYNC_ID("+userId+")" );
		
		SafeCacher.compareAndSet( groupChatMessageSyncId,0,() -> OfflineGroupChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+OfflineGroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );  return  groupChatMessageSyncId;
	}
	
	private  XAtomicLong  getChatMessageSyncId(     long    userId )
	{
		XAtomicLong  chatMessageSyncId = CacheFactory.atomicLong( "SQUIRREL.CHAT_MESSAGE.SYNC_ID("+userId+")" );
		
		SafeCacher.compareAndSet( chatMessageSyncId,0,() -> OfflineChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+OfflineChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );  return  chatMessageSyncId;
	}
	
	public  boolean  store(long  userId,Packet  packet )
	{
		try
		{
			if( packet instanceof ChatPacket )
			{
				ChatPacket  chatPacket = ObjectUtils.cast( packet );
				
				Object[][]  params = { {chatPacket.getId(),ChatManager.INSTANCE.getChatMessageSyncId(userId),chatPacket.getContactId(),userId,chatPacket.getMd5(),new  String(chatPacket.getContent()),chatPacket.getContentType().getValue()},{chatPacket.getId(),ChatManager.INSTANCE.getChatMessageSyncId(chatPacket.getContactId()),userId,chatPacket.getContactId(),chatPacket.getMd5(),new  String(chatPacket.getContent()),chatPacket.getContentType().getValue()} };

				return  ConnectionUtils.batchUpdatedCount(OfflineChatMessageRepository.DAO.update("INSERT  INTO  "+OfflineChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?,?)",params)) == 2;
			}
			else
			if( packet instanceof      GroupChatPacket )
			{
				GroupChatPacket  groupChatPacket = ObjectUtils.cast( packet );
				
//				Object[][]  params = new  Object[][];
				
//				ChatGroupManager.INSTANCE.getChatGroupUserIds(groupChatPacket.getGroupId()).forEach();
				
				return  OfflineGroupChatMessageRepository.DAO.update("INSERT  INTO  "+OfflineGroupChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,GROUP_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[]{groupChatPacket.getId(),groupChatPacket.getGroupId(),groupChatPacket.getContactId(),userId,groupChatPacket.getMd5(),new  String(groupChatPacket.getContent()),groupChatPacket.getContentType().getValue()}) >= 1;
			}
		}
		catch( Throwable  e )
		{
			log.error(    e.getMessage(), e );
		}
		
		return    false;
	}
	
	public  void  initialize( Object  ...  parameters  )
	{
		
	}
}
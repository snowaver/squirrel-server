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

import  java.util.List;
import  java.util.Set;

import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatManager;
import  cc.mashroom.squirrel.module.user.model.ChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.ChatMessage;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.atomic.XAtomicLong;
import  cc.mashroom.xcache.util.SafeCacher;

public  class  DefaultMessageStorageEngine
{
	private  XAtomicLong  getGroupChatMessageSyncId(  long  userId )
	{
		return  SafeCacher.compareAndSetQuietly( CacheFactory.atomicLong("SQUIRREL.GROUP_CHAT_MESSAGE.SYNC_ID("+userId+")"),0,() -> ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );
	}
		
	private  XAtomicLong  getChatMessageSyncId( long  userId )
	{
		return  SafeCacher.compareAndSetQuietly( CacheFactory.atomicLong("SQUIRREL.CHAT_MESSAGE.SYNC_ID("+userId+")"),0,() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );
	}
	
	public  boolean  insert( long  userId,ChatPacket  chatPacket   )
	{
		return  ConnectionUtils.batchUpdatedCount( ChatMessageRepository.DAO.update("INSERT  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?,?)",new  Object[][]{{chatPacket.getId(),ChatManager.INSTANCE.getChatMessageSyncId(userId),chatPacket.getContactId(),userId,chatPacket.getMd5(),new  String(chatPacket.getContent()),chatPacket.getContentType().getValue()},{chatPacket.getId(),ChatManager.INSTANCE.getChatMessageSyncId(chatPacket.getContactId()),userId,chatPacket.getContactId(),chatPacket.getMd5(),new  String(chatPacket.getContent()),chatPacket.getContentType().getValue()}}) ) == 2;
	}
	
	public  boolean  insert( long  userId,GroupChatPacket  groupChatPacket )
	{
		Set<Long>  chatGroupUserIds = ChatGroupManager.INSTANCE.getChatGroupUserIds( groupChatPacket.getGroupId() );
		
		return  ConnectionUtils.batchUpdatedCount( ChatGroupMessageRepository.DAO.update("INSERT  INTO  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,GROUP_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?,?,?)",(Object[][])  chatGroupUserIds.parallelStream().map((chatGroupUserId) -> new  Object[]{groupChatPacket.getId(),getGroupChatMessageSyncId(chatGroupUserId).incrementAndGet(),groupChatPacket.getGroupId(),groupChatPacket.getContactId(),chatGroupUserId,groupChatPacket.getMd5(),new  String(groupChatPacket.getContent()),groupChatPacket.getContentType().getValue()}).toArray()) ) == chatGroupUserIds.size();
	}
	
	public  List<ChatMessage>  lookupChatMessage( long  userId,long  syncOffsetId )
	{
		return  ChatMessageRepository.DAO.lookup( ChatMessage.class,"SELECT  *  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,syncOffsetId} );
	}
	
	public  List<ChatGroupMessage>  lookupGroupChatMessage( long  userId,long  syncOffsetId )
	{
		return  ChatGroupMessageRepository.DAO.lookup( ChatGroupMessage.class,"SELECT  *  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,syncOffsetId} );
	}
}
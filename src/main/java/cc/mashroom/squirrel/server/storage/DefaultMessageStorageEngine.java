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
import  java.util.Map;
import  java.util.stream.Collectors;

import  org.springframework.stereotype.Service;
import  org.springframework.util.Assert;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.util.ConnectionUtils;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import  cc.mashroom.squirrel.module.user.model.ChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.ChatMessage;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.xcache.atomic.XAtomicLong;
import  cc.mashroom.xcache.util.SafeCacher;

@Service
public  class  DefaultMessageStorageEngine  implements MessageStorageEngine
{
	public  Map<Long,Long>  insert( long  userId,ChatPacket  packet )
	{
		Map<Long,Long>  syncIds = Lists.newArrayList(userId,packet.getContactId()).stream().collect( Collectors.toMap((id) -> id,(id) -> getChatMessageSyncId(id).incrementAndGet()) );
		
		Assert.isTrue( ConnectionUtils.batchUpdatedCount(ChatMessageRepository.DAO.update("INSERT  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?)",new  Object[][]{{packet.getId(),syncIds.get(userId),packet.getContactId(),userId,packet.getMd5(),new  String(packet.getContent()),packet.getContentType().getValue(),TransportState.SENT.getValue()},{packet.getId(),syncIds.get(packet.getContactId()),userId,packet.getContactId(),packet.getMd5(),new  String(packet.getContent()),packet.getContentType().getValue(),TransportState.RECEIVED.getValue()}})) == syncIds.size(),"SQUIRREL-SERVER:  ** DEFAULT  MESSAGE  STORAGE  ENGINE **  error  while  storing  chat  messages." );
		
		return  syncIds;
	}
	
	protected  XAtomicLong  getChatMessageSyncId(      long  userId )
	{
		return  SafeCacher.createAndSetIfAbsent( "SQUIRREL.CHAT_MESSAGE.SYNC_ID("+userId+")",() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );
	}
	
	protected  XAtomicLong  getGroupChatMessageSyncId( long  userId )
	{
		return  SafeCacher.createAndSetIfAbsent( "SQUIRREL.CHAT_GROUP_MESSAGE.SYNC_ID("+userId+")",() -> ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );
	}
	
	public  Map<Long,Long>  insert( long  userId ,GroupChatPacket  packet )
	{
		Map<Long,Long>  chatGroupUserSyncIds = ChatGroupManager.INSTANCE.getChatGroupUserIds(packet.getGroupId()).parallelStream().collect( Collectors.toMap((chatGroupUserId) -> chatGroupUserId,(chatGroupUserId) -> getGroupChatMessageSyncId(chatGroupUserId).incrementAndGet()) );
		
		Assert.isTrue( ConnectionUtils.batchUpdatedCount(ChatGroupMessageRepository.DAO.update("INSERT  INTO  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,GROUP_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?,?,?)",(Object[][])  chatGroupUserSyncIds.entrySet().parallelStream().map((entry) -> new  Object[]{packet.getId(),entry.getValue(),packet.getGroupId(),packet.getContactId(),entry.getKey(),packet.getMd5(),new  String(packet.getContent()),packet.getContentType().getValue(),(entry.getKey() == packet.getContactId() ? TransportState.SENT : TransportState.RECEIVED).getValue()}).collect(Collectors.toList()).toArray(new  Object[chatGroupUserSyncIds.size()][]))) == chatGroupUserSyncIds.size(),"SQUIRREL-SERVER:  ** DEFAULT  MESSAGE  STORAGE  ENGINE **  error  while  storing  chat  group  messages." );

		return  chatGroupUserSyncIds;
	}
	
	public  List<ChatGroupMessage>  lookupChatGroupMessage(  long  userId,long  chatGroupMessageSyncOffsetId )
	{
		return  ChatGroupMessageRepository.DAO.lookup(ChatGroupMessage.class,"SELECT  ID,GROUP_ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,chatGroupMessageSyncOffsetId}).stream().map((offlineChatMessage) -> offlineChatMessage.setCreateTime(new  Timestamp(offlineChatMessage.getId()))).collect( Collectors.toList() );
	}
	
	public  List<ChatMessage>  lookupChatMessage( long  userId,long  chatMessageSyncOffsetId )
	{
		return  ChatMessageRepository.DAO.lookup(ChatMessage.class,"SELECT  ID,SYNC_ID,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?",new  Object[]{userId,chatMessageSyncOffsetId}).stream().map((offlineGroupChatMessage) -> offlineGroupChatMessage.setCreateTime(new  Timestamp(offlineGroupChatMessage.getId()))).collect(Collectors.toList());
	}
}
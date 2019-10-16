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
package cc.mashroom.squirrel.module.chat.group.manager;

import  java.util.Set;
import  java.util.concurrent.TimeUnit;
import  java.util.stream.Collectors;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupSyncRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XKeyValueCache;
import  cc.mashroom.xcache.atomic.XAtomicLong;
import  cc.mashroom.xcache.util.SafeCacher;
import  lombok.Getter;

public  class          ChatGroupManager  implements  Plugin
{
	public  void  stop()
	{
		
	}
	
	public  final  static  ChatGroupManager  INSTANCE = new  ChatGroupManager();
	@Getter
	private  XKeyValueCache  <Long,Set<Long>>   chatGroupUserIdsCache;
	
	public  void  initialize( Object  ...  params )  throws  Exception
	{
		this.chatGroupUserIdsCache = CacheFactory.getOrCreateKeyValueCache( "SQUIRREL.CHAT_GROUP.USER_IDS_CACHE" );
	}
	
	public  XAtomicLong  getChatGroupSyncId( long  userId )
	{
		return  SafeCacher.compareAndSetQuietly( CacheFactory.atomicLong("SQUIRREL.CHAT_GROUP.SYNC_ID("+userId+")"),0,() -> ChatGroupSyncRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupSyncRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );
	}
	
	public  Set<Long>  getChatGroupUserIds( final  long  chatGroupId )
	{
		Set<Long>   chatGroupUserIds = chatGroupUserIdsCache.get( chatGroupId );
		
		return  chatGroupUserIds != null ? chatGroupUserIds : SafeCacher.cache( this.chatGroupUserIdsCache.getLock(chatGroupId),2,TimeUnit.SECONDS,chatGroupUserIdsCache,chatGroupId,() -> ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?",new  Object[]{chatGroupId}).stream().map(ChatGroupUser::getContactId).collect(Collectors.toSet()) );
	}
}
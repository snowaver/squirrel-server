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

import  java.util.HashSet;
import  java.util.Set;
import  java.util.stream.Collectors;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XKeyValueCache;

public  class      ChatGroupUserManager  implements  Plugin
{
	public  final  static  ChatGroupUserManager  INSTANCE =new  ChatGroupUserManager();
	
	public  void  stop()
	{
		
	}
	
	public  void  initialize( Object  ...  params )  throws  Exception
	{
		this.chatGroupUserIdsCache = CacheFactory.getOrCreateKeyValueCache( "CHATGROUP_USER_IDS_CACHE" );
	}
	
	private  XKeyValueCache<Long,Set<Long>>     chatGroupUserIdsCache;
	
	public  Set<Long>  getChatGroupUserIds( final  long  chatGroupId )
	{
		Set<Long>  chatGroupUserIds    = this.chatGroupUserIdsCache.get( chatGroupId );
		
		if( chatGroupUserIds == null )
		{
			synchronized(     String.valueOf( chatGroupId ).intern() )
			{
				chatGroupUserIds       = this.chatGroupUserIdsCache.get( chatGroupId );
				
				if( chatGroupUserIds    == null )
				{
					chatGroupUserIds= ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?",new  Object[]{chatGroupId}).stream().map(ChatGroupUser::getContactId).collect( Collectors.toSet() );
					
					this.chatGroupUserIdsCache.put(     chatGroupId,chatGroupUserIds );
				}
			}
		}
		
		return      new  HashSet<Long>( chatGroupUserIds );
	}
}
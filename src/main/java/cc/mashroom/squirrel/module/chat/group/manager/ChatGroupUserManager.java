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

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XCache;

public  class      ChatGroupUserManager  implements  Plugin
{
	public  final  static  ChatGroupUserManager  INSTANCE = new  ChatGroupUserManager();
	
	public  void  stop()
	{
		
	}
	
	public  void  initialize()  throws  Exception
	{
		chatGroupUserIdsCache = CacheFactory.createCache(  "CHATGROUP_USER_IDS_CACHE" );
	}
	
	private  XCache<Long,Set<Long>>  chatGroupUserIdsCache;
	
	public  Set<Long>  getChatGroupUserIds( final  long  chatGroupId )
	{
		Set<Long>  chatGroupUserIds = chatGroupUserIdsCache.get( chatGroupId );
		
		if( chatGroupUserIds == null )
		{
			synchronized(     String.valueOf( chatGroupId ).intern() )
			{
				chatGroupUserIds = chatGroupUserIdsCache.get(    chatGroupId );
				
				if( chatGroupUserIds    == null )
				{
					chatGroupUserIds= new  HashSet<Long>();
					
					for( ChatGroupUser  chatGroupUser : ChatGroupUser.dao.search("SELECT  CONTACT_ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?",new  Object[]{chatGroupId}) )
					{
						chatGroupUserIds.add(   chatGroupUser.getLong( "CONTACT_ID" ) );
					}
					/*
					CacheFactory.createCache("CHATGROUP_CACHE").update( "INSERT  INTO  CHATGROUP  (CHATGROUP_ID,USER_IDS)  VALUES  (?,?)",new  Object[]{chatGroupId,chatGroupUserIds.toArray(new  Long[chatGroupUserIds.size()])} );  return  chatGroupUserIds;
					*/
					chatGroupUserIdsCache.put( chatGroupId, chatGroupUserIds );
				}
			}
		}
		
		return      new  HashSet<Long>( chatGroupUserIds );
	}
}
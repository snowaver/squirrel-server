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

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.atomic.XAtomicLong;
import  cc.mashroom.xcache.util.SafeCacher;

public  class       ChatManager  implements  Plugin
{
	public  void  stop()
	{
		
	}

	public  void  initialize( Object  ...  params )   throws  Exception
	{
		
	}
	
	public  final  static   ChatManager  INSTANCE = new  ChatManager();
		
	public  XAtomicLong  getGroupChatMessageSyncId(      long  userId )
	{
		XAtomicLong  groupChatMessageSyncId = CacheFactory.atomicLong( "SQUIRREL.GROUP_CHAT_MESSAGE.SYNC_ID("+userId+")" );
		
		SafeCacher.compareAndSet( groupChatMessageSyncId,0,() -> ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );  return  groupChatMessageSyncId;
	}
	
	public  XAtomicLong  getChatMessageSyncId( long  userId )
	{
		XAtomicLong  chatMessageSyncId = CacheFactory.atomicLong( "SQUIRREL.CHAT_MESSAGE.SYNC_ID("+userId+")" );
		
		SafeCacher.compareAndSet( chatMessageSyncId,0,() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId}) );  return  chatMessageSyncId;
	}
}
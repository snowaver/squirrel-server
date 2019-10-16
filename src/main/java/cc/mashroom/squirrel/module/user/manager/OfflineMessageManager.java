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
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
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
	public  final  static  OfflineMessageManager  INSTANCE  = new  OfflineMessageManager();
	
	public  void  stop()
	{
		
	}
	

	
	public  boolean  store(long  userId,Packet  packet )
	{
		try
		{

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
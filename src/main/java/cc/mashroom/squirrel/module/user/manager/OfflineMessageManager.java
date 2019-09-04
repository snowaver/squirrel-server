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

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.repository.OfflineChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineGroupChatMessageRepository;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.util.ObjectUtils;
import  lombok.NoArgsConstructor;
import  lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor

public  class  OfflineMessageManager  implements  Plugin
{
	public  final  static  OfflineMessageManager  INSTANCE = new  OfflineMessageManager();
	
	public  void  stop()
	{
		
	}
	
	public  boolean  store(long  userId,Packet  packet )
	{
		try
		{
			if( packet instanceof ChatPacket )
			{
				ChatPacket  chatPacket = ObjectUtils.cast( packet );
				
				return  OfflineChatMessageRepository.DAO.update("INSERT  INTO  "+OfflineChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?)",new  Object[]{chatPacket.getId(),chatPacket.getContactId(),userId,chatPacket.getMd5(),new  String(chatPacket.getContent()),chatPacket.getContentType().getValue()}) >= 1;
			}
			else
			if( packet instanceof      GroupChatPacket )
			{
				GroupChatPacket  groupChatPacket = ObjectUtils.cast( packet );
				
				return  OfflineGroupChatMessageRepository.DAO.update("INSERT  INTO  "+OfflineGroupChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,GROUP_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{groupChatPacket.getId(),groupChatPacket.getGroupId(),groupChatPacket.getContactId(),userId,groupChatPacket.getMd5(),new  String(groupChatPacket.getContent()),groupChatPacket.getContentType().getValue()}) >= 1;
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
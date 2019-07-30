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

import  java.sql.Timestamp;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.repository.OfflineMessageRepository;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
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
	
	public  void  initialize( Object  ...  parameters  )
	{
		
	}
	
	public  boolean  store( long  senderId,ChatPacket  packet )
	{
		try
		{
			return  OfflineMessageRepository.DAO.update("INSERT  INTO  "+OfflineMessageRepository.DAO.getDataSourceBind().table()+"  (CONTACT_ID,RECEIVER_ID,MD5,CONTENT,CONTENT_TYPE,CREATE_TIME)  VALUES  (?,?,?,?,?,?)",new  Object[]{senderId,packet.getContactId(),packet.getMd5(),new  String(packet.getContent()),packet.getContentType().getValue(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis())}) >= 1;
		}
		catch( Throwable  e )
		{
			log.error( e.getMessage(),e );
		}
		
		return    false;
	}
}
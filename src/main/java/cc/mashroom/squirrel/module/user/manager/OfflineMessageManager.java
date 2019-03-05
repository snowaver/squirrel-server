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
import  org.slf4j.LoggerFactory;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.squirrel.module.user.model.OfflineMessage;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  lombok.NoArgsConstructor;

@NoArgsConstructor

public  class  OfflineMessageManager  implements  Plugin
{
	private  final  static  org.slf4j.Logger  logger = LoggerFactory.getLogger( OfflineMessageManager.class );
	
	public  final  static  OfflineMessageManager  INSTANCE = new  OfflineMessageManager();
	
	public  boolean  store( long  senderId,ChatPacket  packet )
	{
		try
		{
			return  OfflineMessage.dao.update("INSERT  INTO  "+OfflineMessage.dao.getDataSourceBind().table()+"  (CONTACT_ID,RECEIVER_ID,MD5,CONTENT,CONTENT_TYPE,CREATE_TIME)  VALUES  (?,?,?,?,?,?)",new  Object[]{senderId,packet.getContactId(),packet.getMd5(),new  String(packet.getContent()),packet.getContentType().getValue(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis())}) >= 1;
		}
		catch(  Exception  e )
		{
			logger.error( e.getMessage(),e );
		}
		
		return  false;
	}
	
	public  void  initialize()
	{
		
	}
	
	public  void  stop()
	{
		
	}
}
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
package cc.mashroom.squirrel.module.user.service;

import  java.sql.Timestamp;
import  java.util.stream.Collectors;

import  org.joda.time.DateTime;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.repository.ContactRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineGroupChatMessageRepository;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  OfflineServiceImpl  implements  OfflineService
{
	@Connection( dataSource = @DataSource(type="db",name="squirrel") )
	
	public  ResponseEntity<String>  lookup( int  action,long  userId,Map<String,Map<String,Object>>  extras )
	{
		Map<String,Object>  response  = new  HashMap<String,Object>();
		
		response.addEntry( "CONTACTS",ContactRepository.DAO.lookup(Map.class,"SELECT  CONTACT_ID  AS  ID,CONTACT_USERNAME  AS  USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  LAST_MODIFY_TIME > ?  ORDER  BY  ID  ASC",new  Object[]{userId,new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CONTACTS").getOrDefault("LATEST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}) );
		
		response.addEntry( "OFFLINE_CHAT_MESSAGES",OfflineChatMessageRepository.DAO.lookup(Map.class,"SELECT  ID,USER_ID  AS  CONTACT_ID,MD5,CONTENT_TYPE,CONTENT  FROM  "+OfflineChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{userId,Long.parseLong(extras.get("OFFLINE_CHAT_MESSAGES").get("LATEST_RECEIVED_ID").toString())}).stream().filter((chatMessage) -> {chatMessage.addEntry("CREATE_TIME",new  Timestamp(chatMessage.getLong("ID"))).addEntry("TRANSPORT_STATE",TransportState.RECEIVED.getValue());  return  true;}).collect(Collectors.toList()) );
		
		response.addEntry( "OFFLINE_GROUP_CHAT_MESSAGES",OfflineGroupChatMessageRepository.DAO.lookup(Map.class,"SELECT  ID,GROUP_ID,USER_ID  AS  CONTACT_ID,MD5,CONTENT_TYPE,CONTENT  FROM  "+OfflineGroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  AND  IS_DELETED = FALSE)  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{userId,Long.parseLong(extras.get("OFFLINE_GROUP_CHAT_MESSAGES").get("LATEST_RECEIVED_ID").toString())}).stream().filter((groupChatMessage) -> {groupChatMessage.addEntry("CREATE_TIME",new  Timestamp(groupChatMessage.getLong("ID"))).addEntry("TRANSPORT_STATE",TransportState.RECEIVED.getValue());  return  true;}).collect(Collectors.toList()) );
		
		response.addEntry( "CHAT_GROUP_USERS", ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?  ORDER  BY  LAST_MODIFY_TIME  ASC",new  Object[]{userId,new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUP_USERS").getOrDefault("LATEST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}) );
		
		return  ResponseEntity.status(200).body( JsonUtils.toJson(response.addEntry("CHAT_GROUPS",ChatGroupRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?  ORDER  BY  ID  ASC",new  Object[]{userId,new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUPS").getOrDefault("LATEST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}))) );
	}
}
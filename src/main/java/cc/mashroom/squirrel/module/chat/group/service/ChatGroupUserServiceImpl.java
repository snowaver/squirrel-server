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
package cc.mashroom.squirrel.module.chat.group.service;

import  java.sql.Timestamp;
import  java.util.ArrayList;
import  java.util.LinkedList;
import  java.util.List;

import  org.apache.commons.lang3.StringUtils;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.util.Reference;

@Service
public  class  ChatGroupUserServiceImpl  implements  ChatGroupUserService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  add(    long  inviterId,long  chatGroupId,List<Long>  inviteeIds )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<Long,String>  nicknamesMapper  = new  HashMap<Long,String>();
		
		List<Reference<Object>>  ids=new  ArrayList<Reference<Object>>();
				
		Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>();
		
		UserRepository.DAO.lookup(Map.class,"SELECT  ID,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  ("+StringUtils.rightPad("?",(inviteeIds.size()-1)*2,",?")+")",inviteeIds.toArray(new  Long[inviteeIds.size()])).forEach( (user) -> nicknamesMapper.addEntry(user.getLong("ID"),user.getString("NICKNAME")) );
		
		List<Object[]>  addChatGroupUserBatchParameters = new  LinkedList<Object[]>();
		
		inviteeIds.forEach( (contactId) -> addChatGroupUserBatchParameters.add( new  Object[]{now,contactId,now,contactId,chatGroupId,contactId,nicknamesMapper.getString(contactId),chatGroupId,contactId} ) );
		
		ChatGroupUserRepository.DAO.insert( ids,"INSERT  INTO  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD)  SELECT  ?,?,?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?)",addChatGroupUserBatchParameters.toArray(new  Object[0][]) );
		
		CacheFactory.getOrCreateKeyValueCache("CHATGROUP_USER_IDS_CACHE").remove(chatGroupId );
		
		List<Map<String,Object>>  addedChatGroupUsers  = new  LinkedList<Map<String,Object>>();
		
		for( Reference< Object >  id : ids )
		{
			if( id != null && id.get() != null && Long.parseLong( id.get().toString())   >= 1 )
			{
				addedChatGroupUsers.add( new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("IS_DELETED",false).addEntry("CREATE_TIME",now).addEntry("CREATE_BY",inviterId).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",inviterId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",inviteeIds.get(ids.indexOf(id))).addEntry("VCARD",nicknamesMapper.getString(inviteeIds.get(ids.indexOf(id)))) );
			}
		}
		
		return  ResponseEntity.ok( response.addEntry("CHAT_GROUP_USERS",addedChatGroupUsers).addEntry("CHAT_GROUPS",ChatGroupRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId})) );
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  update(long  updatorId,long  chatGroupId,long  chatGroupUserId,String  newVcard )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroupUserRepository.DAO.update("UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  VCARD = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{newVcard,now,chatGroupUserId,chatGroupId,updatorId}) <= 0 )
		{
			throw  new  IllegalArgumentException(    "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  chat  group  user  was  not  found." );
		}
		
		Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",updatorId).addEntry("VCARD",newVcard).addEntry("LAST_MODIFY_TIME",now)));
		
		response.addEntry( "CHAT_GROUP_ALL_USERS",ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId}) );
		
		return  ResponseEntity.status(200).body( response.addEntry("CHAT_GROUPS",new  ArrayList<Map<String,Object>>()) );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  secede( long  secederId,long  chatGroupId, long  chatGroupUserId )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUPS",new  ArrayList<Map<String,Object>>()).addEntry( "CHAT_GROUP_ALL_USERS",new  ArrayList<Map<String,Object>>() );
		
		if( ChatGroupUserRepository.DAO.update("UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{now,chatGroupUserId,chatGroupId,secederId}) <= 0 )
		{
			throw  new  IllegalArgumentException(    "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  chat  group  user  was  not  found." );
		}
		
		if( ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  (SELECT  COUNT(*)  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE) <= 0",new  Object[]{now,chatGroupId,chatGroupId}) >= 1 )
		{
			response.addEntry( "CHAT_GROUPS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID", chatGroupId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now)) );
		}
		else
		{
			response.addEntry( "CHAT_GROUP_ALL_USERS",ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId}) );
		}
		
		return  ResponseEntity.status(200).body( response.addEntry("CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",secederId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now))) );
	}
}
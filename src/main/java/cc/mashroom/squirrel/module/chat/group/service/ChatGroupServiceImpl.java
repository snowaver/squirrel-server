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
import  java.util.List;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.util.Reference;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;

@Service
public  class  ChatGroupServiceImpl     implements  ChatGroupService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  add( long  userId,String  name )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Reference<Object> id=new  Reference<Object>();
		
		if( ChatGroupRepository.DAO.insert(id,"INSERT  INTO  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME)  SELECT  ?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  CREATE_BY = ?  AND  NAME = ?  AND  IS_DELETED = FALSE)",new  Object[]{now,userId,now,userId,name,userId,name}) >= 1 )
		{
			Reference<Object>  chatGroupUserId   = new  Reference<Object>();
			
			String  nickname = UserRepository.DAO.lookupOne(Map.class,"SELECT  NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{userId}).getString( "NICKNAME" );
			
			ChatGroupUserRepository.DAO.insert( chatGroupUserId,"INSERT  INTO  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD)  SELECT  ?,?,?,?,?,?,?  FROM  DUAL",new  Object[]{now,userId,now,userId,id.get(),userId,nickname} );
			
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUPS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("IS_DELETED",false).addEntry("CREATE_TIME",now).addEntry("CREATE_BY",userId).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",userId).addEntry("NAME",name))).addEntry("CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId.get()).addEntry("IS_DELETED",false).addEntry("CREATE_TIME",now).addEntry("CREATE_BY",userId).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",userId).addEntry("CHAT_GROUP_ID",id.get()).addEntry("CONTACT_ID",userId).addEntry("VCARD",nickname)))) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  update(  long  updaterId,long  chatGroupId,String  name )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>();
		
		if( ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  NAME = ?,LAST_MODIFY_TIME = ?,LAST_MODIFY_BY = ?  WHERE  ID = ?",new  Object[]{name,now,updaterId,chatGroupId}) >= 1 )
		{
			response.addEntry( "CHAT_GROUP_ALL_USERS",ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId}) );
			
			return  ResponseEntity.ok( new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUPS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupId).addEntry("NAME",name).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",updaterId))).addEntry("CHAT_GROUP_USERS",new  ArrayList<>()) );
		}
		
		return  ResponseEntity.status(601).body( response.addEntry(     "CHAT_GROUP_ALL_USERS", new  ArrayList<>()) );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  remove( long  id )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{true,id}) >= 1 )
		{
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUPS",new  HashMap<String,Object>().addEntry("ID",id).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now)).addEntry("CHAT_GROUP_USERS",new  ArrayList<>())) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel") )
	
	public  ResponseEntity<String>  lookup( int  action, String  keyword,Map<String,Map<String,Object>>  extras )
	{
		Map<String,Object>  response = new  HashMap<String,Object>().addEntry( "CHAT_GROUP_USERS",ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?  ORDER  BY  LAST_MODIFY_TIME  ASC",new  Object[]{Long.valueOf(keyword),new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUP_USERS").getOrDefault("LAST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}) );
		
		return  ResponseEntity.status(200).body( JsonUtils.toJson(response.addEntry("CHAT_GROUPS",ChatGroupRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?",new  Object[]{Long.valueOf(keyword),new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUPS").getOrDefault("LAST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}))) );
	}
}
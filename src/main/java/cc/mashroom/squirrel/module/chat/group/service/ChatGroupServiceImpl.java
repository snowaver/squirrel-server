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

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.util.Reference;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.user.model.User;
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
		
		if( ChatGroup.dao.insert(id,"INSERT  INTO  "+ChatGroup.dao.getDataSourceBind().table()+"  (NAME,CREATE_BY,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  CREATE_BY = ?  AND  NAME = ?)",new  Object[]{name,userId,now,now,userId,name}) >= 1 )
		{
			Reference<Object>  chatGroupUserId = new  Reference<Object>();
			
			ChatGroupUser.dao.insert( chatGroupUserId,"INSERT  INTO  "+ChatGroupUser.dao.getDataSourceBind().table()+"  (CHAT_GROUP_ID,CONTACT_ID,VCARD,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,(SELECT  NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID = ?),?,?  FROM  DUAL",new  Object[]{id.get(),userId,userId,now,now} );
			
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("CHAT_GROUPS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("NAME",name))).addEntry("CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId.get()).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("CHAT_GROUP_ID",id.get()).addEntry("CONTACT_ID",userId)))) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  update(  long  id,String  name )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroup.dao.update("UPDATE  "+ChatGroup.dao.getDataSourceBind().table()+"  SET  NAME = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{name,now,now}) >= 1 )
		{
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("id" , id).addEntry("lastModifyTime" , now)) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  remove( long  id )
	{
		return  ResponseEntity.status(ChatGroup.dao.update("DELETE  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{id}) >= 1 ? 200 : 601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel") )
	
	public  ResponseEntity<String>  search( int  action, String  keyword,java.util.Map<String,java.util.Map<String,Object>>  extras )
	{
		Map<String,Object>  response = new  HashMap<String,Object>().addEntry( "CHAT_GROUP_USERS",ChatGroupUser.dao.search("SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?",new  Object[]{Long.valueOf(keyword),new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUP_USERS").getOrDefault("LAST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}) );
		
		return  ResponseEntity.status(200).body( JsonUtils.toJson(response.addEntry("CHAT_GROUPS",ChatGroup.dao.search("SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,NAME  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  AND  LAST_MODIFY_TIME > ?  ORDER  BY  ID  ASC",new  Object[]{Long.valueOf(keyword),new  Timestamp(DateTime.parse(ObjectUtils.cast(extras.get("CHAT_GROUPS").getOrDefault("LAST_MODIFY_TIME","2000-01-01T00:00:00.000Z"),String.class)).getMillis())}))) );
	}
}
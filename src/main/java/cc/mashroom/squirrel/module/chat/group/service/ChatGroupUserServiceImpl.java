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
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.Reference;

@Service
public  class  ChatGroupUserServiceImpl  implements  ChatGroupUserService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<List<Map<String,Object>>>  add( long  inviterId,long  chatGroupId,List<Long>  inviteeIds )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<Long,String>  nicknamesMapper  = new  HashMap<Long,String>();
		
		List<Reference<Object>>  ids     = new  LinkedList<Reference<Object>>();
		
		List<Object[]>  params = new  LinkedList<Object[]>();
		
		List<Map<String,Object>>  response    = new  LinkedList<Map<String,Object>>();
		
		User.dao.search("SELECT  ID,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID  IN  ("+StringUtils.rightPad("?",(inviteeIds.size()-1)*2,",?")+")",inviteeIds.toArray(new  Long[inviteeIds.size()])).forEach( (user) -> nicknamesMapper.addEntry(user.getLong("ID"),user.getString("NICKNAME")) );
		
		for(  Long  contactId : inviteeIds )
		{
			params.add( new  Object[]{chatGroupId , contactId , nicknamesMapper.getString(contactId) , now , now , chatGroupId , contactId} );
		}
		
		ChatGroupUser.dao.insert( ids,"INSERT  INTO  "+ChatGroupUser.dao.getDataSourceBind().table()+"  (CHAT_GROUP_ID,CONTACT_ID,VCARD,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?)",params.toArray(new  Object[0][]) );
		
		CacheFactory.createCache( "CHATGROUP_USER_IDS_CACHE").remove(   chatGroupId );
		
		for(  Reference<Object >  id : ids )
		{
			if( id.get() != null && Long.parseLong( id.get().toString() ) >= 1 )
			{
				/*
				oldIds.add( ObjectUtils.cast(id.get(),BigInteger.class).longValue() );
				*/
				response.add( new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("VCARD",nicknamesMapper.getString(inviteeIds.get(ids.indexOf(id)))).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("CONTACT_ID",inviteeIds.get(ids.indexOf(id))).addEntry("CHAT_GROUP_ID",chatGroupId) );
			}
		}
		
		return  ResponseEntity.ok(response);
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  update( long  updaterId,long  chatGroupId,String  newvcard )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroupUser.dao.update("UPDATE  "+ChatGroupUser.dao.getDataSourceBind().table()+"  SET  VCARD = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{newvcard,now,now}) >= 1 )
		{
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("ID",chatGroupId).addEntry("LAST_MODIFY_TIME",now)) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  secede( long  secedeUserId,long  chatGroupId,long  chatGroupUserId )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<String,Object>  response     = new  HashMap<String,Object>();
		
		response.addEntry( "CHAT_GROUPS",new  ArrayList<Map<String,Object>>() );
		
		ChatGroupUser.dao.update("UPDATE  "+ChatGroupUser.dao.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{now,chatGroupUserId,chatGroupId,secedeUserId});
	
		response.addEntry( "CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",secedeUserId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now)) );
		
		if( ChatGroup.dao.update("UPDATE  "+ChatGroup.dao.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  (SELECT  COUNT(*)  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE) <= 0",new  Object[]{now,chatGroupId,chatGroupId}) >= 1 )
		{
			response.addEntry( "CHAT_GROUPS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now)) );
		}
		
		return  ResponseEntity.status( 200 ).body(     JsonUtils.toJson( response ) );
	}
}
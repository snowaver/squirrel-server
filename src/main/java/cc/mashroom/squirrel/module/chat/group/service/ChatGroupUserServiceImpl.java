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
import  java.util.Set;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.locks.Lock;
import  java.util.stream.Collectors;

import  org.apache.commons.lang3.StringUtils;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.XKeyValueCache;
import  cc.mashroom.util.Reference;

@Service
public  class  ChatGroupUserServiceImpl       implements ChatGroupUserService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  add(  long  inviterId, long  chatGroupId,List<Long>   inviteeIds )
	{
		XKeyValueCache<Long,Set<Long>>       chatGroupUserIdsCache =  ChatGroupUserManager.INSTANCE.getChatGroupUserIdsCache();
		
		Lock  locker = chatGroupUserIdsCache.getLock( chatGroupId );
		
		try
		{
			if( ! locker.tryLock(2 , TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException(  "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			Map<Long,String>  nicknamesMapper  = new  HashMap<Long,String>();
			
			List<Reference<Object>>  ids=new  ArrayList<Reference<Object>>();
					
			Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>();
			
			UserRepository.DAO.lookup(Map.class,"SELECT  ID,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  ("+StringUtils.rightPad("?",(inviteeIds.size()-1)*2+1,",?")+")",inviteeIds.toArray(new  Long[inviteeIds.size()])).forEach( (user) -> nicknamesMapper.addEntry(user.getLong("ID"),user.getString("NICKNAME")) );
			
			List<Object[]>  addChatGroupUserBatchParameters = inviteeIds.stream().map((contactId)-> new  Object[]{now,contactId,now,contactId,chatGroupId,contactId,nicknamesMapper.getString(contactId),chatGroupId,contactId}).collect( Collectors.toList() );
			
			ChatGroupUserRepository.DAO.insert( ids,"INSERT  INTO  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD)  SELECT  ?,?,?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",addChatGroupUserBatchParameters.toArray(new  Object[0][]) );
			
			List<Map>  chatGroupUsers = ChatGroupUserRepository.DAO.lookup( Map.class,"SELECT  *  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId} );
			
			for( Reference< Object >  id : ids )
			{
				if( id != null && id.get() != null  && Long.parseLong(id.get().toString())   >= 1 )
				{
					chatGroupUsers.add( new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("IS_DELETED",false).addEntry("CREATE_TIME",now).addEntry("CREATE_BY",inviterId).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",inviterId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",inviteeIds.get(ids.indexOf(id))).addEntry("VCARD",nicknamesMapper.getString(inviteeIds.get(ids.indexOf(id)))) );
				}
			}
			
			chatGroupUserIdsCache.remove( chatGroupId );
			
			return  ResponseEntity.ok(response.addEntry("CHAT_GROUP_USERS",chatGroupUsers).addEntry("CHAT_GROUPS",ChatGroupRepository.DAO.lookup(Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId})) );
		}
		catch( Exception e )
		{
			throw  new  RuntimeException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  see  the  exception  stack  please", e );
		}
		finally
		{
			locker.unlock();
		}
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  update(long  updatorId,long  chatGroupId,long  chatGroupUserId,String  newVcard )
	{
		{
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			if( ChatGroupUserRepository.DAO.update("UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  VCARD = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{newVcard,now,chatGroupUserId,chatGroupId,updatorId}) <= 0 )
			{
				throw  new  IllegalArgumentException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  chat  group  user  was  not  found." );
			}
			
			Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUP_USERS",Lists.newArrayList(new  HashMap<String,Object>().addEntry("ID",chatGroupUserId).addEntry("CHAT_GROUP_ID",chatGroupId).addEntry("CONTACT_ID",updatorId).addEntry("VCARD",newVcard).addEntry("LAST_MODIFY_TIME",now)));
			
			response.addEntry( "CHAT_GROUP_ALL_USERS",ChatGroupUserRepository.DAO.lookup(Map.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId}) );
			
			return  ResponseEntity.status(200).body(  response.addEntry("CHAT_GROUPS", new  ArrayList<Map<String,Object>>()) );
		}
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  remove( long  removerId ,long  chatGroupId,long  chatGroupUserId )
	{
		XKeyValueCache<Long,Set<Long>>       chatGroupUserIdsCache =  ChatGroupUserManager.INSTANCE.getChatGroupUserIdsCache();
		
		Lock  locker = chatGroupUserIdsCache.getLock( chatGroupId );
		
		try
		{
			if( ! locker.tryLock(2 , TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException(  "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUPS",new  ArrayList<Map<String,Object>>()).addEntry( "CHAT_GROUP_ALL_USERS",new  ArrayList<Map<String,Object>>() );
			
			Map<String,Object>  chatGroupUser= ChatGroupUserRepository.DAO.lookupOne( Map.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?",new  Object[]{chatGroupUserId,chatGroupId} );
			
			Map<String,Object>  chatGroup = ChatGroupRepository.DAO.lookupOne( Map.class,"SELECT  IS_DELETED,CREATE_TIME,CREATE_BY,NAME,(SELECT  COUNT(*)  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE  AND  ID <> ?)  AS  CHAT_GROUP_USER_COUNT  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId,chatGroupUserId,chatGroupId} );
			
			if( chatGroup.getLong("CREATE_BY") != removerId && chatGroupUser.getLong("CONTACT_ID") != removerId )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  can  not  remove  the  member  for  authority.  the  creator  can  remove  anyone  but  the  non-creator  can  only  quit  the  group." );
			}
			
			response.addEntry( "CHAT_GROUP_ALL_USERS",chatGroupUserIdsCache.get(chatGroupId).stream().map((contactId) -> new  HashMap<String,Object>().addEntry("CONTACT_ID",contactId)).collect(Collectors.toList()) );
			//  dismiss  the  chat  group  when  the  creator  left.
			if( chatGroup != null && chatGroup.getLong("CREATE_BY") == removerId && chatGroupUser.getLong("CONTACT_ID") == removerId && ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_BY = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{removerId,now,chatGroupId})  >= 1  )
			{
				response.addEntry( "CHAT_GROUPS",Lists.newArrayList(chatGroup.addEntry("ID",chatGroupId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now).addEntry("LAST_MODIFY_BY",removerId)) );
			}
			else
			if( chatGroupUser != null && ChatGroupUserRepository.DAO.update("UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_BY = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{removerId,now,chatGroupUserId}) >= 0 )
			{
				response.addEntry( "CHAT_GROUP_USERS",Lists.newArrayList(chatGroupUser.addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_BY",removerId).addEntry("LAST_MODIFY_TIME",now)) );
			}
			
			chatGroupUserIdsCache.remove( chatGroupId );  return  ResponseEntity.status(200   ).body( response );
		}
		catch( Exception e )
		{
			throw  new  RuntimeException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  see  the  exception  stack  please", e );
		}
		finally
		{
			locker.unlock();
		}
	}
}
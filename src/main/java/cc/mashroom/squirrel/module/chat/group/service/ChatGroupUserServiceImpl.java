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
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupSync;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.model.OoIData;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupSyncRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.XKeyValueCache;
import  cc.mashroom.util.Reference;

@Service
public  class  ChatGroupUserServiceImpl      implements   ChatGroupUserService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<OoIData>  add(long  inviterId,long  chatGroupId,List<Long>  inviteeIds )
	{
		XKeyValueCache<Long,Set<Long>>   chatGroupUserIdsCache = ChatGroupUserManager.INSTANCE.getChatGroupUserIdsCache();
		
		Lock  locker = chatGroupUserIdsCache.getLock( chatGroupId );
		
		try
		{
			if( ! locker.tryLock(2, TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException(  "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			Map<Long,String>  nicknamesMapper  =  new  HashMap<Long,String>();
			
			List<Reference<Object>>  ids= new  ArrayList<Reference<Object>>();
			/*	
			Map<String,List<? extends Map>>  response = new  HashMap<String,List<? extends Map>>();
			*/
			UserRepository.DAO.lookup(User.class,"SELECT  ID,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  ("+StringUtils.rightPad("?",(inviteeIds.size()-1)*2+1,",?")+")",inviteeIds.toArray(new  Long[inviteeIds.size()])).forEach( (user) -> nicknamesMapper.addEntry(user.getId(),user.getNickname()) );
			
			List<Object[]>  addChatGroupUserBatchParameters = inviteeIds.stream().map((contactId)-> new  Object[]{now,contactId,now,contactId,chatGroupId,contactId,nicknamesMapper.getString(contactId),chatGroupId,contactId}).collect( Collectors.toList() );
			
			ChatGroup  chatGroup = ChatGroupRepository.DAO.lookupOne( ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId} );
			
			if( chatGroup.getLastModifyTime().getTime() >=     now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  time  line  error,  check  system  time  please." );
			}
			
			ChatGroupRepository.DAO.update( "UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  LAST_MODIFY_BY = ?  AND  LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{inviterId,now,chatGroupId} );
			
			OoIData  ooiData = new  OoIData().setChatGroups(Lists.newArrayList(chatGroup.setLastModifyBy(inviterId).setLastModifyTime(now)) );
			
			ChatGroupUserRepository.DAO.insert( ids,"INSERT  INTO  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD)  SELECT  ?,?,?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",addChatGroupUserBatchParameters.toArray(new  Object[0][]) );
			//  users  who  may  already  exist  in  the  chat  group  should  be  removed  from  the  invitee  list,  and  no  further  chat  group  event  will  be  sent  to  them.
			inviteeIds.retainAll(ids.stream().filter((id) -> id.get() != null).map((id) -> (long)  addChatGroupUserBatchParameters.get(ids.indexOf(id))[1]).collect(Collectors.toList()) );
			
			ooiData.setChatGroupUsers( ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  *  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE" , new  Object[]{chatGroupId}) );
			
			ooiData.setChatGroupSyncs( ooiData.getChatGroupUsers().stream().map((chatGroupUser) -> new  ChatGroupSync(ChatGroupUserManager.INSTANCE.getChatGroupSyncId(chatGroupUser.getContactId()).incrementAndGet(),chatGroupUser.getContactId(),chatGroupId,now,inviteeIds.contains(chatGroupUser.getContactId()) ? 4 : 5)).collect(Collectors.toList()) );
			
			ChatGroupSyncRepository.DAO.insert( ooiData.getChatGroupSyncs() );
			
			chatGroupUserIdsCache.remove(chatGroupId );       return  ResponseEntity.ok( ooiData );
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
	
	public  ResponseEntity<OoIData>  update( long  updatorId,long  chatGroupId  , long  chatGroupUserId,String  newVcard )
	{
		{
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			ChatGroup  chatGroup = ChatGroupRepository.DAO.lookupOne( ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId} );
			
			if( chatGroup.getLastModifyTime().getTime() >=     now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  time  line  error,  check  system  time  please." );
			}
			
			ChatGroupUser  chatGroupUser = ChatGroupUserRepository.DAO.lookupOne( ChatGroupUser.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{chatGroupUserId,chatGroupId,updatorId} );
			
			if( chatGroup==null|| chatGroupUser==null )
			{
				throw  new  IllegalArgumentException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  chat  group  or  chat  group  user  was  not  found." );
			}
			
			ChatGroupRepository.DAO.update( "UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  LAST_MODIFY_BY = ?  AND  LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{updatorId,now,chatGroupId} );
			
			OoIData  ooiData = new  OoIData().setChatGroups(Lists.newArrayList(chatGroup.setLastModifyBy(updatorId).setLastModifyTime(now)) );
			
			ChatGroupUserRepository.DAO.update( "UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  VCARD = ?,LAST_MODIFY_BY = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{newVcard,updatorId,now,chatGroupUserId,chatGroupId,updatorId} );
			
			ooiData.setChatGroupUsers(Lists.newArrayList(chatGroupUser.setLastModifyBy(updatorId).setLastModifyTime(now).setVcard(newVcard))).setChatGroupSyncs( ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).stream().map((contactId) -> new  ChatGroupSync(ChatGroupUserManager.INSTANCE.getChatGroupSyncId(contactId).incrementAndGet(),contactId,chatGroupId,now,6)).collect(  Collectors.toList()) );
			
			ChatGroupSyncRepository.DAO.insert( ooiData.getChatGroupSyncs() );       return  ResponseEntity.ok( ooiData );
		}
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<OoIData>  remove( long removerId,long chatGroupId,long chatGroupUserId )
	{
		XKeyValueCache<Long,Set<Long>>   chatGroupUserIdsCache = ChatGroupUserManager.INSTANCE.getChatGroupUserIdsCache();
		
		Lock  locker = chatGroupUserIdsCache.getLock( chatGroupId );
		
		try
		{
			if( ! locker.tryLock(2, TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException(  "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			ChatGroup  chatGroup = ChatGroupRepository.DAO.lookupOne( ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?" ,new  Object[]{chatGroupId} );
			
			if( chatGroup.getLastModifyTime().getTime() >=     now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  time  line  error,  check  system  time  please." );
			}
			
			ChatGroupUser  chatGroupUser= ChatGroupUserRepository.DAO.lookupOne( ChatGroupUser.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CHAT_GROUP_ID = ?",new  Object[]{chatGroupUserId,chatGroupId} );
			
			if( chatGroup.getCreateBy() != removerId && chatGroupUser.getContactId() != removerId )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  can  not  remove  the  member  for  authority.  the  creator  can  remove  anyone  but  the  non-creator  can  only  quit  the  group." );
			}
			OoIData  ooiData          = new  OoIData();
			//  dismiss  the  chat  group  when  the  creator  left.
			if( chatGroup != null && chatGroup.getCreateBy() == removerId && chatGroupUser.getContactId() == removerId && ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_BY = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{removerId,now,chatGroupId}) >= 1 )
			{
				ooiData.setChatGroups(Lists.newArrayList(chatGroup.setCheckPointTime(chatGroup.getLastModifyTime()).setIsDeleted(true).setLastModifyBy(removerId).setLastModifyTime(now))).setChatGroupUsers( Lists.newArrayList() );
			}
			else
			if( chatGroupUser != null && ChatGroupUserRepository.DAO.update("UPDATE  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_BY = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{removerId,now,chatGroupUserId}) >= 0 )
			{
				ChatGroupRepository.DAO.update( "UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  LAST_MODIFY_BY = ?  AND  LAST_MODIFY_TIME = ?  WHERE  ID = ?",     new  Object[]{removerId,now,chatGroupId} );
				
				ooiData.setChatGroups(Lists.newArrayList(chatGroup.setCheckPointTime(chatGroup.getLastModifyTime()).setLastModifyBy(removerId).setLastModifyTime(now))).setChatGroupUsers( Lists.newArrayList(chatGroupUser.setIsDeleted(true).setLastModifyBy(removerId).setLastModifyTime(now)) );
			}
			
			ooiData.setChatGroupSyncs( ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).stream().map((contactId) -> new  ChatGroupSync(ChatGroupUserManager.INSTANCE.getChatGroupSyncId(contactId).incrementAndGet(),contactId,chatGroupId,now,chatGroup.getCreateBy() == removerId && chatGroupUser.getContactId() == removerId ? 3 : 7)  ).collect(Collectors.toList()) );
			
			ChatGroupSyncRepository.DAO.insert( ooiData.getChatGroupSyncs() );       return  ResponseEntity.ok( ooiData );
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
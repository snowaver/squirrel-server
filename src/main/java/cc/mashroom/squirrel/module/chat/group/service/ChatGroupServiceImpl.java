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
import java.util.stream.Collectors;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.util.Reference;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupSync;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.model.OoIData;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupSyncRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;

@Service
public  class  ChatGroupServiceImpl    implements  ChatGroupService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<OoIData>  add( long  createrId  ,String  name )
	{
		Reference<Object>  idRef = new  Reference<Object>();
		
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroupRepository.DAO.insert(idRef,"INSERT  INTO  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME)  SELECT  ?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  CREATE_BY = ?  AND  NAME = ?  AND  IS_DELETED = FALSE)",new  Object[]{now,createrId,now,createrId,name,createrId,name}) != 1 )
		{
			return  ResponseEntity.status(601).body( null );
		}
		
		Reference<Object>  chatGroupUserIdRef = new   Reference<Object>();
		
		String  nickname = UserRepository.DAO.lookupOne( String.class,"SELECT  NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",  new  Object[]  {createrId} );
		
		OoIData  ooiData = new  OoIData().setChatGroups( Lists.newArrayList(new  ChatGroup(Long.parseLong(idRef.get().toString()),false,now,createrId,now,createrId,now,name)) );
		
		ChatGroupUserRepository.DAO.insert( chatGroupUserIdRef,"INSERT  INTO  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now,createrId,now,createrId,idRef.get(),createrId,nickname} );
		
		ooiData.setChatGroupUsers( Lists.newArrayList(new  ChatGroupUser((Long)  chatGroupUserIdRef.get(),false,now,createrId,now,createrId,(Long)  idRef.get(),createrId,nickname)) );
		
		ChatGroupSync  chatGroupSync = new  ChatGroupSync( ChatGroupUserManager.INSTANCE.nextSynchronousId(createrId),createrId,  Long.parseLong(idRef.get().toString()),now,1 );
		
		ooiData.setChatGroupSyncs( Lists.newArrayList(chatGroupSync   ) );
		
		ooiData.setChatGroupSyncId(    chatGroupSync.getSyncId() );
		
		ChatGroupSyncRepository.DAO.insert( ooiData.getChatGroupSyncs() );  return  ResponseEntity.ok( ooiData );
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<OoIData>  update(  long  updaterId,long  chatGroupId,String  name )
	{
		ChatGroup  chatGroup = ChatGroupRepository.DAO.lookupOne( ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroupId} );
		
		if( chatGroup.getCreateBy()  != updaterId )
		{
			throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  only  the  chat  group  owner  can  modify  the  group  name." );
		}
		
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( chatGroup.getLastModifyTime().getTime() >=     now.getTime() )
		{
			throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  time  line  error,  check  system  time  please." );
		}
		
		if( ChatGroupRepository.DAO.update("UPDATE  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  SET  NAME = ?,LAST_MODIFY_TIME = ?,LAST_MODIFY_BY = ?  WHERE  ID = ?",new  Object[]{name,now,updaterId,chatGroupId}) != 1 )
		{
			return  ResponseEntity.status(601).body( null );
		}
		
		OoIData  ooiData = new  OoIData().setChatGroups(Lists.newArrayList(chatGroup.setLastModifyBy(updaterId).setLastModifyTime(now).setName(name))).setChatGroupUsers( Lists.newArrayList() );
		
		ooiData.setChatGroupSyncs( ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).stream().map((contactId) -> new  ChatGroupSync(ChatGroupUserManager.INSTANCE.nextSynchronousId(contactId),updaterId,chatGroupId,now,2)).collect(Collectors.toList()) );
		
		ChatGroupSyncRepository.DAO.insert( ooiData.getChatGroupSyncs() );  return  ResponseEntity.ok( ooiData );
	}
}
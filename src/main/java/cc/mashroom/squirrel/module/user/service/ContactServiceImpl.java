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
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.locks.Lock;

import  org.apache.curator.shaded.com.google.common.collect.Lists;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.user.manager.ContactManager;
import  cc.mashroom.squirrel.module.user.repository.ContactRepository;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  ContactServiceImpl  implements  ContactService
{
	private  Map<Integer,Integer>  subscribeeStatus = new  ConcurrentHashMap<Integer,Integer>().addEntry(2, 1).addEntry(4, 3).addEntry(6, 5).addEntry(8, 7 );
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,Object>>  update( long  userId,long  contactId,String  remark,String  group )
	{
		Lock  locker = ContactManager.INSTANCE.getUpdateLockerCache().getLock( Math.min(userId, contactId)+ ":"+ Math.max(userId,contactId) );
		
		try
		{
			if( !locker.tryLock(2,TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			if( Lists.newArrayList(userId).stream().map((id) -> ContactRepository.DAO.lookupOne(Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{id})).mapToLong((latestModifyTime) -> latestModifyTime == null ? 0L : latestModifyTime.getTime()).max().getAsLong() >= now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  time  line  error,  check  system  time  please."  );
			}
			
			ContactRepository.DAO.update( "UPDATE  "+ContactRepository.DAO.getDataSourceBind().table()+"  SET  REMARK = ?,GROUP_NAME = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{remark,group,now,userId,contactId} );
			
			return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("ID", contactId).addEntry("REMARK",remark).addEntry("GROUP_NAME",group).addEntry("LAST_MODIFY_TIME",now) );
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
	
	public  ResponseEntity<Map<String,Object>>  unsubscribe( long  unsubscriberId,long  unsubscribeeId )
	{
		Lock  locker = ContactManager.INSTANCE.getUpdateLockerCache().getLock( Math.min(unsubscriberId , unsubscribeeId)    + ":" + Math.max(unsubscriberId , unsubscribeeId) );
		
		try
		{
			if( !locker.tryLock(2,TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			if( Lists.newArrayList(unsubscriberId,unsubscribeeId).stream().map((id) -> ContactRepository.DAO.lookupOne(Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{id})).mapToLong((latestModifyTime) -> latestModifyTime == null ? 0L : latestModifyTime.getTime()).max().getAsLong() >= now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  time  line  error,  check  system  time  please."  );
			}
			
			ContactRepository.DAO.update( "UPDATE  "+ContactRepository.DAO.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{now,unsubscriberId,unsubscribeeId} );
			
			return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("ID",unsubscribeeId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now) );
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
	
	public  ResponseEntity<Map<String,Object>>  subscribe( long  subscriberId,long  subscribeeId,String  remark,String  group  )
	{
		Lock  locker = ContactManager.INSTANCE.getUpdateLockerCache().getLock( Math.min(subscriberId,subscribeeId)+":"+Math.max(subscriberId,subscribeeId) );
		
		try
		{
			if( !locker.tryLock(2,TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			if( Lists.newArrayList(subscriberId  ,  subscribeeId).stream().map((id) -> ContactRepository.DAO.lookupOne(Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{id})).mapToLong((latestModifyTime) -> latestModifyTime == null ? 0L : latestModifyTime.getTime()).max().getAsLong() >= now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  time  line  error,  check  system  time  please."  );
			}
			
			Map<Long,Map<String,Object>>  subscribingProfiles = new  HashMap<Long,Map<String,Object>>();
			
			UserRepository.DAO.lookup(Map.class,"SELECT  ID,USERNAME,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  (?,?)",new  Object[]{subscriberId,subscribeeId}).forEach( (user) -> subscribingProfiles.put(user.getLong("ID"),user) );
			
			if( ContactRepository.DAO.update("INSERT  INTO  "+ContactRepository.DAO.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,GROUP_NAME,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?,1,0,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",new  Object[]{subscriberId,subscribeeId,subscribingProfiles.get(subscribeeId).getString("USERNAME"),remark,group,now,now,subscriberId,subscribeeId}) <= 0 )
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  the  contact  subscription  is  exist." );
			}
			
			if( ContactRepository.DAO.update("INSERT  INTO  "+ContactRepository.DAO.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,GROUP_NAME,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?,2,0,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",new  Object[]{subscribeeId,subscriberId,subscribingProfiles.get(subscriberId).getString("USERNAME"),subscribingProfiles.get(subscriberId).getString("NICKNAME"),null,now,now,subscribeeId,subscriberId}) <= 0 )
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  the  contact  subscription  is  exist." );
			}
			
			Map<String,Object>  subscriberProfile = new  HashMap<String,Object>().addEntry("ID",subscriberId).addEntry("USERNAME",subscribingProfiles.get(subscriberId).getString("USERNAME")).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("SUBSCRIBE_STATUS",2).addEntry("REMARK",subscribingProfiles.get(subscriberId).getString("NICKNAME")).addEntry("GROUP_NAME",null).addEntry( "IS_DELETED",false  );
			
			return  ResponseEntity.status(200).body(new  HashMap<String,Object>().addEntry("SUBSCRIBER_PROFILE",subscriberProfile).addEntry("ID",subscribeeId).addEntry("USERNAME",subscribingProfiles.get(subscribeeId).getString("USERNAME")).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("SUBSCRIBE_STATUS",1).addEntry("REMARK",remark    ).addEntry("GROUP_NAME",group).addEntry("IS_DELETED",false) );
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
	
	public  ResponseEntity<Map<String,Object>>  changeSubscribeStatus( int  status,long  subscriberId,long  subscribeeId,String  remark,String  group )
	{
		if( status    != 8 )
		{
			throw  new  IllegalArgumentException( "SQUIRREL-SERVER:  ** CONTACT  SERVICE  IMPL **  only  8  ( ACCEPT )  is  acceptable."    );
		}
		
		Lock  locker = ContactManager.INSTANCE.getUpdateLockerCache().getLock( Math.min(subscriberId,subscribeeId)+":"+Math.max(subscriberId,subscribeeId) );
		
		try
		{
			if( !locker.tryLock(2,TimeUnit.SECONDS) )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  the  lock  is  not  acquired  before  the  waiting  time  (2  seconds)  elapsed,  give  up." );
			}
			
			Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
			
			if( Lists.newArrayList(subscriberId  ,  subscribeeId).stream().map((id) -> ContactRepository.DAO.lookupOne(Timestamp.class,"SELECT  MAX(LAST_MODIFY_TIME)  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{id})).mapToLong((latestModifyTime) -> latestModifyTime == null ? 0L : latestModifyTime.getTime()).max().getAsLong() >= now.getTime() )
			{
				throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  SERVICE  IMPL **  time  line  error,  check  system  time  please."  );
			}
			
			ContactRepository.DAO.update( "UPDATE  "+ContactRepository.DAO.getDataSourceBind().table()+"  SET  SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{subscribeeStatus.get(status),now,subscriberId, subscribeeId} );
			
			Map<String,Object>  subscribeeProfile = new  HashMap<String,Object>().addEntry("ID",subscribeeId).addEntry("SUBSCRIBE_STATUS",subscribeeStatus.get(status)).addEntry( "LAST_MODIFY_TIME",now );
			
			ContactRepository.DAO.update( "UPDATE  "+ContactRepository.DAO.getDataSourceBind().table()+"  SET  REMARK = ?,SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?,GROUP_NAME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{remark,status,now,group,subscribeeId,subscriberId} );
			
			return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("SUBSCRIBEE_PROFILE",subscribeeProfile).addEntry("ID",subscriberId).addEntry("REMARK",remark).addEntry("SUBSCRIBE_STATUS",status).addEntry("LAST_MODIFY_TIME",now).addEntry("GROUP_NAME",group ) );
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
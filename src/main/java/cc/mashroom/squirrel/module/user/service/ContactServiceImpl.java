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

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.user.model.Contact;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  ContactServiceImpl  implements  ContactService
{
	private  Map<Integer,Integer>  subscribeeStatus = new  ConcurrentHashMap<Integer,Integer>().addEntry(1,0).addEntry(3,2).addEntry(5,4).addEntry( 7,6 );
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,Object>>  update( long  userId,long  contactId,String  remark,String  group )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  REMARK = ?,GROUP_NAME = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{remark,group,now,userId,contactId} );
		
		return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("ID", contactId).addEntry("GROUP_NAME",group   ).addEntry("LAST_MODIFY_TIME",now) );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,Object>>  unsubscribe( long  unsubscriberId,long  unsubscribeeId )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{now,unsubscriberId,unsubscribeeId} );
		
		return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("ID",unsubscribeeId).addEntry("IS_DELETED",true).addEntry("LAST_MODIFY_TIME",now) );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,Object>>  subscribe( long  subscriberId,long  subscribeeId,String  remark,String  group )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact.dao.update("UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",   new  Object[]{now,subscriberId,subscribeeId} );
		
		Contact.dao.update("UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  IS_DELETED = TRUE,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",   new  Object[]{now,subscribeeId,subscriberId} );
		
		Map<Long,User>  subscribingProfileMapper = new  HashMap<Long,User>();
		
		User.dao.search("SELECT  ID,USERNAME,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID  IN  (?,?)",new  Object[]{subscriberId,subscribeeId}).forEach( (user) -> subscribingProfileMapper.put(user.getLong("ID"),user) );
		
		if( Contact.dao.update("INSERT  INTO  "+Contact.dao.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,GROUP_NAME,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?,0,0,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",new  Object[]{subscriberId,subscribeeId,subscribingProfileMapper.get(subscribeeId).getString("USERNAME"),remark,group,now,now,subscriberId,subscribeeId}) <= 0 )
		{
			throw  new  IllegalStateException(  "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  the  contact  relationship  exist  error." );
		}
		
		if( Contact.dao.update("INSERT  INTO  "+Contact.dao.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,GROUP_NAME,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?,1,0,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE)",new  Object[]{subscribeeId,subscriberId,subscribingProfileMapper.get(subscriberId).getString("USERNAME"),subscribingProfileMapper.get(subscriberId).getString("NICKNAME"),null,now,now,subscribeeId,subscriberId}) <= 0 )
		{
			throw  new  IllegalStateException(  "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  the  contact  relationship  exist  error." );
		}
		
		Map<String,Object>  subscriberProfile = new  HashMap<String,Object>().addEntry("ID",subscriberId).addEntry("USERNAME",subscribingProfileMapper.get(subscriberId).getString("USERNAME")).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("SUBSCRIBE_STATUS",1).addEntry("REMARK",subscribingProfileMapper.get(subscriberId).getString("NICKNAME")).addEntry("GROUP_NAME",null).addEntry( "IS_DELETED",false );
		
		return  ResponseEntity.status(200).body(new  HashMap<String,Object>().addEntry("SUBSCRIBER_PROFILE",subscriberProfile).addEntry("ID",subscribeeId).addEntry("USERNAME",subscribingProfileMapper.get(subscribeeId).getString("USERNAME")).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("SUBSCRIBE_STATUS",0).addEntry("REMARK",remark).addEntry("GROUP_NAME",group).addEntry("IS_DELETED",false) );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<Map<String,Object>>  changeSubscribeStatus( int  status, long  subscriberId, long  subscribeeId, String  remark,String  group )
	{
		if( status     != 7 )
		{
			throw  new  IllegalArgumentException(    "SQUIRREL-SERVER:  ** CONTACT  SERVICE  IMPL **  only  7  ( ACCEPT )  is  acceptable." );
		}
		
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{subscribeeStatus.get(status), now, subscriberId, subscribeeId} );
		
		Map<String,Object>  subscribeeProfile = new  HashMap<String,Object>().addEntry("ID",subscribeeId).addEntry("SUBSCRIBE_STATUS",subscribeeStatus.get(status)).addEntry( "LAST_MODIFY_TIME",now );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  REMARK = ?,SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?,GROUP_NAME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{remark,status,now,group,subscribeeId,subscriberId} );
		
		return  ResponseEntity.status(200).body( new  HashMap<String,Object>().addEntry("SUBSCRIBEE_PROFILE",subscribeeProfile).addEntry("ID",subscriberId).addEntry("REMARK",remark).addEntry("SUBSCRIBE_STATUS",status).addEntry("LAST_MODIFY_TIME",now).addEntry("GROUP_NAME",group ) );
	}
}
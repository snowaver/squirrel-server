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
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  ContactServiceImpl  implements  ContactService
{
	private  Map<Integer,Integer>  subscribeeStatus = new  ConcurrentHashMap<Integer,Integer>().addEntry(1,0).addEntry(3,2).addEntry(5,4).addEntry( 7,6 );
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  update( long  userId,long  contactId,String  remark,String  group )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		return  ResponseEntity.status(Contact.dao.update("UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  REMARK = ?,GROUP_NAME = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{remark,group,now,userId,contactId}) >= 1 ? 200 : 601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  subscribe( long  subscriberId,long  subscribeeId,String  remark,String  group )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( Contact.dao.update("INSERT  INTO  "+Contact.dao.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,GROUP_NAME,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,USERNAME,?,?,0,0,?,?  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  NOT  EXISTS  (SELECT  ID  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?)",new  Object[]{subscriberId,subscribeeId,remark,group,now,now,subscribeeId,subscriberId,subscribeeId}) <= 0 )
		{
			throw  new  IllegalStateException(       "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  contact  relationship  exist  error." );
		}
		
		if( Contact.dao.update("INSERT  INTO  "+Contact.dao.getDataSourceBind().table()+"  (USER_ID,CONTACT_ID,CONTACT_USERNAME,REMARK,SUBSCRIBE_STATUS,IS_DELETED,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,USERNAME,NICKNAME,1,0,?,?  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  NOT  EXISTS  (SELECT  ID  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  CONTACT_ID = ?)",new  Object[]{subscribeeId,subscriberId,now,now,subscriberId,subscribeeId,subscriberId}) <= 0 )
		{
			throw  new  IllegalStateException(       "SQUIRREL-CLIENT:  ** CONTACT  SERVICE  IMPL **  contact  relationship  exist  error." );
		}
		
		ClientSession  session    = ClientSessionManager.INSTANCE.get( subscribeeId );
		
		if( session != null )
		{
			session.deliver( new  SubscribePacket(subscriberId,new  User().addEntries(User.dao.getOne("SELECT  USERNAME,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{subscriberId})).addEntry("GROUP",group)) );
		}
		
		return  ResponseEntity.status(200).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  changeSubscribeStatus( int  status, long  subscriberId, long  subscribeeId, String  remark,String  group )
	{
		if( status     != 7 )
		{
			throw  new  IllegalArgumentException(    "SQUIRREL-SERVER:  ** CONTACT  SERVICE  IMPL **  only  7  ( accept )  is  acceptable." );
		}
		
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{subscribeeStatus.get(status), now, subscriberId, subscribeeId} );
		
		Contact.dao.update( "UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  REMARK = ?,SUBSCRIBE_STATUS = ?,LAST_MODIFY_TIME = ?,GROUP_NAME = ?  WHERE  USER_ID = ?  AND  CONTACT_ID = ?",new  Object[]{remark,status,now,group,subscribeeId,subscriberId} );
		
		ClientSession  session    = ClientSessionManager.INSTANCE.get( subscriberId );
		
		if( session != null )
		{
			session.deliver( new  SubscribeAckPacket(subscribeeId,SubscribeAckPacket.ACK_ACCEPT,new  User().addEntries(User.dao.getOne("SELECT  USERNAME,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{subscribeeId})).addEntry("GROUP",group)) );
		}
		
		return  ResponseEntity.status(200).body( "" );
	}
}
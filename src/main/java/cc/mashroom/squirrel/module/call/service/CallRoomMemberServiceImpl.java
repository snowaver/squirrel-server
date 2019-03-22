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
package cc.mashroom.squirrel.module.call.service;

import  java.math.BigInteger;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.xcache.CacheFactory;

@Service
public  class  CallRoomMemberServiceImpl  implements  CallRoomMemberService
{
	@Connection( dataSource=@DataSource(type="ch"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  add( Long  roomId,long  callerId, long  calleeId )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ObjectUtils.cast(CacheFactory.createCache("CALL_ROOM_MEMBER_CACHE").getOne("SELECT  COUNT(*)  AS  COUNT  FROM  CALL_ROOM_MEMBER  WHERE  CALLER_ID  IN  (?,?)  OR  CALLEE_ID  IN  (?,?)",new  Object[]{callerId,calleeId,callerId,calleeId}).get("COUNT"),BigInteger.class).intValue() <= 0 )
		{
			long  newRoomId=CacheFactory.getNextSequence( "CALL_ROOM_ID" );
			
			if( CacheFactory.createCache("CALL_ROOM_MEMBER_CACHE").update("INSERT  INTO  CALL_ROOM_MEMBER  (CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID)  VALUES  (?,?,?,?,?)",new  Object[]{now,callerId,calleeId,-1,newRoomId}) )
			{
				return  ResponseEntity.status( 200 ).body( String.valueOf( roomId ) );
			}
		}
		
		return  ResponseEntity.status(601).body( "" );
	}

	@Connection( dataSource=@DataSource(type="ch"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  remove( long  id )
	{
		return  ResponseEntity.status(ChatGroup.dao.update("DELETE  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{id}) >= 1 ? 200 : 601).body( "" );
	}
}
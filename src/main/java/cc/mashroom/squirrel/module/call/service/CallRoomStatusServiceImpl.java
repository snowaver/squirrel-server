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

import  java.sql.Timestamp;
import  java.util.concurrent.TimeUnit;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.xcache.CacheFactory;

@Service
public  class  CallRoomStatusServiceImpl  implements  CallRoomStatusService
{
	public  ResponseEntity<String>  add( Long  roomId,long  callerId,long  calleeId,int  contentType )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		String  id = Math.min( callerId,calleeId )+":"+ Math.max( callerId,calleeId );
		
		long  newRoomId= CacheFactory.getNextSequence( "SQUIRREL.CALL.ROOM_ID",null );
		
		if( CacheFactory.getOrCreateMemTableCache("CALL_ROOM_STATUS_CACHE").update("INSERT  INTO  CALL_ROOM_STATUS  (ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON)  SELECT  ?,?,?,?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  CALL_ROOM_STATUS  WHERE  CALLER_ID  IN  (?,?)  OR  CALLEE_ID  IN  (?,?))",new  Object[]{id,now,callerId,calleeId,0,newRoomId,contentType,-1,callerId,calleeId,callerId,calleeId}) )
		{
			CallManager.INSTANCE.scheduleTerminate( id,newRoomId,Integer.parseInt(System.getProperty("call.timeout.seconds","20")),TimeUnit.SECONDS );
			
			return  ResponseEntity.status( 200 ).body(  String.valueOf( newRoomId ) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}
}
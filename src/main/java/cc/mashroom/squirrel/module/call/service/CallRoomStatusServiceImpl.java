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

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.xcache.CacheFactory;

@Service
public  class  CallRoomStatusServiceImpl  implements  CallRoomStatusService
{
	public  ResponseEntity<String>  add( Long  roomId,long  callerId,long  calleeId,int  contentType )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		String  id = (Math.min(callerId,calleeId)+"/"+Math.max(callerId,calleeId)).toUpperCase();
		
		long  newRoomId   = CacheFactory.getNextSequence( "CALL_ROOM_ID" );
		
		if( CacheFactory.createCache("CALL_ROOM_STATUS_CACHE").update("INSERT  INTO  CALL_ROOM_STATUS  (ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{id,now,callerId,calleeId,0,newRoomId,contentType,-1}) )
		{
			return  ResponseEntity.status( 200 ).body(  String.valueOf( newRoomId ) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}

	public  ResponseEntity<String>  remove( long  id )
	{
		return  ResponseEntity.status(601).body( "" );
	}
}
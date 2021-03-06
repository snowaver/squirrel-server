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
package cc.mashroom.squirrel.server.session;

import  java.sql.Timestamp;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XMemTableCache;
import  lombok.Getter;
import  lombok.NoArgsConstructor;

@NoArgsConstructor
public  class  ClientSessionManager  implements  Plugin
{
	@Getter
	private  XMemTableCache       sessionLocationCache;
	
	private  Map<Long,ClientSession>  localClientSessionCache=  new  ConcurrentHashMap<Long,ClientSession>();
	
	public  final  static  ClientSessionManager  INSTANCE= new  ClientSessionManager();
	
	public  void  initialize( Object  ...  parameters )
	{
		this.sessionLocationCache = CacheFactory.getOrCreateMemTableCache( "SESSION_LOCATION_CACHE" );
	}

	public  ClientSession  remove(       long  userId )
	{
		this.sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  IS_ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{false,userId} );		
		
		return  this.localClientSessionCache.remove( userId );
	}
	
	public  void  put( LocalClientSession    session  )
	{
		this.sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  CLUSTER_NODE_ID = ?,ACCESS_TIME = ?,IS_ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{CacheFactory.getLocalNodeId(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis()),true,session.getUserId()} );

		this.localClientSessionCache.put( session.getUserId(),session );
	}
	
	public  ClientSession  get( long  userId )
	{
		return  this.localClientSessionCache.computeIfAbsent( userId,(key) -> {SessionLocation  sessionLocation = sessionLocationCache.lookupOne( SessionLocation.class,"SELECT  CLUSTER_NODE_ID  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{userId} );  return  sessionLocation == null || sessionLocation.getClusterNodeId() == null || CacheFactory.getLocalNodeId().equals(sessionLocation.getClusterNodeId()) ? null : new  ClusteredClientSession( userId,sessionLocation.getClusterNodeId() );} );
	}
	
	public  void  stop()
	{
		this.localClientSessionCache.entrySet().forEach( (entry)-> entry.getValue().close(    DisconnectAckPacket.REASON_UNKNOWN_ERROR ) );
	}
}
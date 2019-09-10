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

import  java.io.IOException;
import  java.sql.Timestamp;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XMemTableCache;
import  lombok.NoArgsConstructor;

@NoArgsConstructor
public  class  ClientSessionManager  implements  Plugin
{
	private  Map<Long,ClientSession>  localClientSessionCache = new  ConcurrentHashMap<Long,ClientSession>();

	private  XMemTableCache       sessionLocationCache;
	
	public  final  static  ClientSessionManager  INSTANCE  = new  ClientSessionManager();
	
	public  void  initialize( Object  ...  parameters )
	{
		this.sessionLocationCache = CacheFactory.getOrCreateMemTableCache( "SESSION_LOCATION_CACHE" );
	}

	public  ClientSession  remove(     long  clientId )
	{
		this.sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  IS_ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{false,clientId} );		
		//  leave  the  secret  key  available  for  reconnecting .
		return  this.localClientSessionCache.remove(    clientId );
	}
	
	public  void  put( long  clientId,LocalClientSession  session )
	{
		this.sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  CLUSTER_NODE_ID = ?,ACCESS_TIME = ?,IS_ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{ServerInfo.INSTANCE.getLocalNodeId(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis()),true,clientId} );

		localClientSessionCache.put(clientId,session );
	}
	
	public  ClientSession  get( long  clientId )
	{
		return  this.localClientSessionCache.computeIfAbsent( clientId,(key) -> {SessionLocation  sessionLocation = sessionLocationCache.lookupOne( SessionLocation.class,"SELECT  CLUSTER_NODE_ID  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{clientId} );  return  sessionLocation == null || sessionLocation.getClusterNodeId() == null || ServerInfo.INSTANCE.getLocalNodeId().equals(sessionLocation.getClusterNodeId()) ? null : new  ClusteredClientSession( clientId,sessionLocation.getClusterNodeId() );} );
	}
	
	public  void  stop()
	{
		this.localClientSessionCache.entrySet().forEach( (entry) -> { try{ entry.getValue().close(DisconnectAckPacket.REASON_UNKNOWN_ERROR ); }catch( IOException  e ){} } );
	}
}
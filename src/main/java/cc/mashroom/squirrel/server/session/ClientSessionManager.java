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
import  cc.mashroom.xcache.XCache;
import  lombok.NoArgsConstructor;

@NoArgsConstructor
public  class  ClientSessionManager  implements  Plugin
{
	private  Map<Long,ClientSession>  localClientSessionCache = new  ConcurrentHashMap<Long,ClientSession>();

	private  XCache<Long,String>  sessionLocationCache;
	
	public  final  static  ClientSessionManager  INSTANCE  = new  ClientSessionManager();
	
	public  void  initialize()
	{
		this.sessionLocationCache = CacheFactory.createCache( "SESSION_LOCATION_CACHE" );
	}

	public  ClientSession  remove(     long  clientId )
	{
		sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{false,clientId} );		
		//  leave  the  secret  key  available  for  reconnecting .
		return  this.localClientSessionCache.remove(    clientId );
	}
	
	public  void  put( long  clientId,LocalClientSession  session )
	{
		sessionLocationCache.update( "UPDATE  SESSION_LOCATION  SET  CLUSTER_NODE_ID = ?,ACCESS_TIME = ?,ONLINE = ?  WHERE  USER_ID = ?",new  Object[]{ServerInfo.INSTANCE.getLocalNodeId(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis()),true,clientId} );

		localClientSessionCache.put(clientId,session );
	}
	
	public  ClientSession  get( long  clientId )
	{
		return  localClientSessionCache.computeIfAbsent( clientId,(key) -> {Map<String,Object>  sessionLocation = sessionLocationCache.getOne( "SELECT  CLUSTER_NODE_ID  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{clientId} );  return  sessionLocation == null || sessionLocation.get("CLUSTER_NODE_ID") == null || ServerInfo.INSTANCE.getLocalNodeId().equals(sessionLocation.getString("CLUSTER_NODE_ID")) ? null : new  ClusteredClientSession( clientId,sessionLocation.getString("CLUSTER_NODE_ID") );} );
	}
	
	public  void  stop()
	{
		localClientSessionCache.entrySet().forEach( (entry) -> { try{ entry.getValue().close(DisconnectAckPacket.NETWORK_ERROR); }catch( IOException  e ){} } );
	}
}
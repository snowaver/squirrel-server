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
import  java.util.Arrays;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.RemoteCallable;
import  lombok.AllArgsConstructor;
import  lombok.Getter;

@AllArgsConstructor

public  class  ClusteredClientSession  implements  ClientSession
{
	@Getter
	private  long  clientId;
	@Getter
	private  String  clusterNodeId;
	
	public  void  deliver( Packet  packet )
	{
		if( clusterNodeId != null && !clusterNodeId.equalsIgnoreCase(ServerInfo.INSTANCE.getLocalNodeId()) )
		{
			CacheFactory.call( new  RemoteCallable<Boolean>(1,new  HashMap<String,Object>().addEntry("CLIENT_ID",clientId).addEntry("PACKET",packet)),Arrays.asList(clusterNodeId) );
		}
	}
	
	public  void  close( int  closeReason )  throws  IOException
	{
		if( clusterNodeId != null && !clusterNodeId.equalsIgnoreCase(ServerInfo.INSTANCE.getLocalNodeId()) )
		{
			CacheFactory.call( new  RemoteCallable<Boolean>(0,new  HashMap<String,Object>().addEntry("CLIENT_ID",clientId).addEntry("CLOSE_REASON",closeReason)),Arrays.asList(clusterNodeId) );
		}
	}
}
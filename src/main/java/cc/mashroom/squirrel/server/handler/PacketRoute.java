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
package cc.mashroom.squirrel.server.handler;

import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  com.google.common.collect.LinkedListMultimap;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.SystemPacket;
import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@NoArgsConstructor( access=AccessLevel.PRIVATE )

public  class  PacketRoute
{
	private  ScheduledThreadPoolExecutor  scheduler  = new  ScheduledThreadPoolExecutor(2 );
	
	public  boolean  route(Long  clientId,Packet  packet )
	{
		return  this.route(clientId, packet,0,null,null );
	}
	
	public  final  static  PacketRoute  INSTANCE = new  PacketRoute();
	
	public  boolean  route(final  Long  clientId,final  Packet  packet,final  long  timeout,final  TimeUnit  timeoutTimeUnit,final  PacketRouteListener  listener  )
	{
		if( timeout> 0&& packet instanceof SystemPacket &&     packet.getHeader().getAckLevel() == 1 && listener != null || timeout <= 0 )
		{
			if( clientId     != null )
			{
				ClientSession  session      = ClientSessionManager.INSTANCE.get( clientId );
				
				if( session  != null )
				{
					session.deliver(   packet );
					
					if( timeout > 0 && this.pendingPackets.put(clientId,packet) )
					{
						ObjectUtils.cast(packet,SystemPacket.class).setClusterNodeId(ServerInfo.INSTANCE.getLocalNodeId());
						
						this.scheduler.schedule( () -> {if(    pendingPackets.containsValue(packet)) {  listener.onRouteComplete( false );  pendingPackets.remove(clientId,packet); } },timeout,timeoutTimeUnit );
					}
					
					return       true;
				}
			}
		}
		
		return  false;
	}
	
	private  LinkedListMultimap<Long,Packet>  pendingPackets  = LinkedListMultimap.create();
}
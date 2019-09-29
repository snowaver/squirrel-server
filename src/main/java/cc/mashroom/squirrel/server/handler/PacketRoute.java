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

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.SystemPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  lombok.NonNull;

@NoArgsConstructor(       access =    AccessLevel.PRIVATE )

public          class  PacketRoute
{
	private  ScheduledThreadPoolExecutor scheduler = new  ScheduledThreadPoolExecutor( Integer.parseInt(System.getProperty("squirrel.route.scheduler.corePoolSize","2")) );

	private  ConcurrentHashMap<Long,SystemPacket<?>>  pendings = new  ConcurrentHashMap<Long,SystemPacket<?>>();
	
	public  boolean  route( Long  clientId,Packet  packet )
	{
		return  this.route( clientId, packet,0,null,null );
	}
	
	public  boolean  route( Long  clientId, Packet  packet,long  timeout,TimeUnit  timeoutTimeUnit,PacketRouteListener  listener )
	{
		if( clientId     != null )
		{
			ClientSession  session  = ClientSessionManager.INSTANCE.get(  clientId );
			
			if( session  != null )
			{
				if( timeout > 0 && packet instanceof SystemPacket && packet.getHeader().getAckLevel() == 1 && listener  != null && this.pendings.put(packet.getId(),ObjectUtils.cast(packet)) == null )
				{
					this.scheduler.schedule( () -> listener.onRouteComplete(packet,!this.pendings.remove(packet.getId(), packet)), timeout,timeoutTimeUnit );
				}
				
				session.deliver( packet );   return   true;
			}
		}
		
		return  false;
	}
	
	private  Map<Long,OrderedRouteQueue> orderedRouteQueues = new  ConcurrentHashMap<Long, OrderedRouteQueue>();
	
	public  final  static  PacketRoute  INSTANCE = new  PacketRoute();
	
	public  void  completeRoute( long  clientId, PendingAckPacket  pendingAckPacket )
	{
		SystemPacket  systemPacket = pendings.get(  pendingAckPacket.getPacketId() );
		
		if( systemPacket != null )
		{
			OrderedRouteQueue  routeQueue= this.orderedRouteQueues.get(   clientId );
			
			if(       routeQueue != null )
			{
				routeQueue.onRouteComplete( systemPacket , this.pendings.remove( pendingAckPacket.getPacketId(), systemPacket ) );
			}
		}
	}
	
	public  void  route( long  clientId,@NonNull  OrderedRouteDispose  routeDispose )
	{
		this.orderedRouteQueues.computeIfAbsent(clientId,(id) -> new  OrderedRouteQueue(id)).addRouteDispose(    routeDispose   );
	}
}
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

import com.google.common.collect.LinkedListMultimap;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.server.session.LocalClientSession;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@NoArgsConstructor( access=AccessLevel.PRIVATE )

public  class  PacketRoute
{
	public  boolean  route( Long clientId,Packet   packet  )
	{
		return  this.route( clientId,packet,0 );
	}
	
	private  ScheduledThreadPoolExecutor  scheduler= new  ScheduledThreadPoolExecutor( 2 );
	
	public  final  static  PacketRoute  INSTANCE = new  PacketRoute();
	
	public  boolean  route( final  Long  clientId,final  Packet  packet,final  long  resendIntervalSeconds )
	{
		if( clientId     != null )
		{
			ClientSession  session = ClientSessionManager.INSTANCE.get( clientId );
			
			if( session  != null )
			{
				session.deliver( packet , resendIntervalSeconds,TimeUnit.SECONDS );
				
				if( resendIntervalSeconds > 0 && session instanceof LocalClientSession && packet.getHeader().getAckLevel() == 1 && this.pendingPackets.put(clientId,packet) )
				{
					scheduler.schedule(()-> { if( this.pendingPackets.containsValue(packet) || this.pendingPackets.get(clientId) != null && this.pendingPackets.get(clientId).isEmpty() )  { route(clientId,pendingPackets.get(clientId).get(0),resendIntervalSeconds); } },resendIntervalSeconds,TimeUnit.SECONDS );
				}
				
				return       true;
			}
		}
		
		return  false;
	}
	
	private  LinkedListMultimap<Long,Packet>  pendingPackets = LinkedListMultimap.create();
}
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
package cc.mashroom.squirrel.server.remotetask;

import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.ObjectUtils;

public  class  RemoteEventProcessor  implements  cc.mashroom.xcache.RemoteEventProcessor
{
	public  <T>  T  process( int  eventType,Map<String,Object>  parameters )
	{
		switch( eventType )
		{
			case  0:
			{
				ClientSession  clientSession = ClientSessionManager.INSTANCE.get( parameters.getLong("USER_ID") );
				
				if( clientSession != null)  clientSession.close(parameters.getInteger("CLOSE_REASON") );
				
				return  (T)  Boolean.TRUE;
			}
			case   1:
			{
				return  (T)  Boolean.valueOf( PacketRoute.INSTANCE.route(parameters.getLong("USER_ID"),ObjectUtils.cast(parameters.get("PACKET"),Packet.class)) );
			}
			case   2:
			{
				PacketRoute.INSTANCE.completeRoute( parameters.getLong("USER_ID"),ObjectUtils.cast(parameters.get("PACKET"),PendingAckPacket.class) );
			}
		}
		
		return  null;
	}
}
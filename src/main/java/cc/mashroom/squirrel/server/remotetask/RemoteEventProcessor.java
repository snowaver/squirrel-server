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

import  java.io.IOException;

import  cc.mashroom.squirrel.paip.message.Packet;
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
				ClientSession  clientSession = ClientSessionManager.INSTANCE.get( parameters.getLong("CLIENT_ID") );
				
				if( clientSession != null )
				{
					try
					{
						clientSession.close( parameters.getInteger( "CLOSE_REASON" ) );
					}
					catch( IOException  e )
					{
						return  (T)  Boolean.FALSE;
					}
				}
				
				return  (T)  Boolean.FALSE;
			}
			case   1:
			{
				return  (T)  Boolean.valueOf( PacketRoute.INSTANCE.route(parameters.getLong("CLIENT_ID"),ObjectUtils.cast(parameters.get("PACKET"),Packet.class)) );
			}
		}
		
		return  null;
	}
}
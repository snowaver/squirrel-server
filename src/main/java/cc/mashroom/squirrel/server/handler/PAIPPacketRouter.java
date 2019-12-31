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

import  java.util.Optional;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE )

public  class      PAIPPacketRouter
{
	public  boolean  route( long  userId,Packet  <?>  packet )
	{
		if( userId>0 )
		{
			Optional<ClientSession>  sessionOptional  = Optional.ofNullable( ClientSessionManager.INSTANCE.get(userId) );  sessionOptional.ifPresent( (session) -> session.deliver(packet) );  return  sessionOptional.isPresent();
		}
		return  false;
	}
	
	public  boolean  route(Route<?>     route )
	{
		return  !route.isRoutable() ? false : route(route.getUserId(),route.getPacket() );
	}
	
	public  final  static  PAIPPacketRouter  INSTANCE = new  PAIPPacketRouter();
}
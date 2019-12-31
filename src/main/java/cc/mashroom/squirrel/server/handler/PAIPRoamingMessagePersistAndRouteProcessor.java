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

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.server.storage.RoamingMessagePersistAndRouteEngine;
import  cc.mashroom.util.ObjectUtils;
import  io.netty.channel.Channel;
import  lombok.AllArgsConstructor;

@AllArgsConstructor
public  class  PAIPRoamingMessagePersistAndRouteProcessor  <P extends Packet<P>>  implements  PAIPObjectProcessor<RouteGroup<P>>
{
	private  RoamingMessagePersistAndRouteEngine <P>  persistAndRouteEngine;
	@Override
	public  boolean  process( Channel  channel,RouteGroup  <P>  routeGroup )
	{
		return  true;
	}
	@Override
	public  boolean  isProcessable( Object  object )
	{
		return  object instanceof RouteGroup && (ObjectUtils.cast(object,RouteGroup.class).getOriginalPacket() instanceof ChatPacket || ObjectUtils.cast(object,RouteGroup.class).getOriginalPacket() instanceof GroupChatPacket);
	}
}
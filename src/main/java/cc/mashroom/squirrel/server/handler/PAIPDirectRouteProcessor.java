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
import  io.netty.channel.Channel;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@NoArgsConstructor(  access   = AccessLevel.PUBLIC )

public  class  PAIPDirectRouteProcessor<P extends Packet<P>>  implements  PAIPObjectProcessor<Route<P>>
{
	@Override
	public  boolean  process( Channel  channel,Route<P>  route )
	{
		PAIPPacketRouter.INSTANCE.route( route.getUserId(),route.getPacket() );  return  true;
	}
	@Override
	public  boolean  isProcessable( Object  object )
	{
		return  object instanceof Route;
	}
}
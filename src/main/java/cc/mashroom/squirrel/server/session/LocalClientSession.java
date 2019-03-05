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

import  io.netty.channel.Channel;
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;

@NoArgsConstructor
@AllArgsConstructor
public  class  LocalClientSession  implements  ClientSession
{
	@Getter
	private  long    clientId;
	@Getter
	private  Channel  channel;
	
	public  void  deliver(   Packet  packet )
	{
		this.channel.writeAndFlush( packet );
	}
	
	public  void  close(  int  reason )  throws  IOException
	{
		try
		{
			if( this.channel.hasAttr( CallPacket.CALL_ID ) )
			{
				PacketRoute.INSTANCE.route( channel.attr(CallPacket.CALL_CONTACT_ID).get(),new  CloseCallPacket(channel.attr(ConnectPacket.CLIENT_ID).get(),channel.attr(CallPacket.CALL_ID).get()) );
			}
			
			if( reason  >= 0 )
			{
				channel.writeAndFlush( new  DisconnectAckPacket(reason) );
			}
			
			channel.close(  );
		}
		finally
		{
			ClientSessionManager.INSTANCE.remove(clientId );
		}
	}
}
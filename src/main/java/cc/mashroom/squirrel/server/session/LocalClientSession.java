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

import  io.netty.channel.Channel;
import  lombok.AllArgsConstructor;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  lombok.extern.slf4j.Slf4j;

import  cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;


@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public  class  LocalClientSession  implements      ClientSession
{
	@Getter
	private  long  userId;
	@Getter
	private  Channel     channel;
	
	public  void  deliver(   Packet  packet )
	{
		this.channel.writeAndFlush(  packet);
	}
	
	public  void  close(   int  closeReason )
	{
		try
		{
			if( this.channel.hasAttr(CallPacket.CALL_ROOM_ID) && this.channel.attr(CallPacket.CALL_ROOM_ID).get() != null )  CallManager.INSTANCE.terminate( this.channel.attr(CallPacket.CALL_ROOM_ID).get(),CloseCallReason.NETWORK_ERROR );
			
			if( closeReason>= 0 )  this.channel.writeAndFlush( new  DisconnectAckPacket(closeReason) );
			
			this.channel.close().await(2000);
		}
		catch( InterruptedException  itptex )
		{
			
		}
		finally
		{
			ClientSessionManager.INSTANCE.remove( this.userId );
		}
	}
}
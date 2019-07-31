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
import  lombok.extern.slf4j.Slf4j;
import  cc.mashroom.squirrel.module.call.model.CallRoomStatus;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.xcache.CacheFactory;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public  class  LocalClientSession  implements  ClientSession
{
	@Getter
	private  long   clientId;
	@Getter
	private  Channel channel;
	
	public  void  deliver(   Packet  packet )
	{
		this.channel.writeAndFlush( packet );
	}
	
	public  void  close(  int  reason )  throws  IOException
	{
		try
		{
			if( channel.hasAttr(CallPacket.CALL_ROOM_ID) && channel.attr(CallPacket.CALL_ROOM_ID).get() != null )
			{
				CallRoomStatus  callRoomStatus = CacheFactory.getOrCreateMemTableCache("CALL_ROOM_STATUS_CACHE").lookupOne( CallRoomStatus.class,"SELECT  ID,CREATE_TIME,CALLER_ID,CALLEE_ID,STATE,CALL_ROOM_ID,CONTENT_TYPE,CLOSE_REASON  FROM  CALL_ROOM_STATUS  WHERE  CALL_ROOM_ID = ?  AND  (CALLER_ID = ?  OR  CALLEE_ID = ?)",new  Object[]{channel.attr(CallPacket.CALL_ROOM_ID).get(),clientId,clientId} );
				
				if( callRoomStatus  != null )
				{
					CacheFactory.getOrCreateMemTableCache("CALL_ROOM_STATUS_CACHE").update( "DELETE  FROM  CALL_ROOM_STATUS  WHERE  ID = ?  AND  CALL_ROOM_ID = ?",  new  Object[]{callRoomStatus.getId(),callRoomStatus.getCallRoomId()} );
					
					this.channel.writeAndFlush( new  CloseCallPacket(callRoomStatus.getCallerId() == clientId ? callRoomStatus.getCalleeId() : callRoomStatus.getCallerId(),callRoomStatus.getCallRoomId(),CloseCallReason.NETWORK_ERROR) );
					
					PacketRoute.INSTANCE.route( callRoomStatus.getCallerId() == clientId ? callRoomStatus.getCalleeId() : callRoomStatus.getCallerId(),new  CloseCallPacket(clientId,callRoomStatus.getCallRoomId(),CloseCallReason.NETWORK_ERROR) );
				}
			}
			if( reason >= 0 )
			{
				this.channel.writeAndFlush(    new  DisconnectAckPacket( reason ) );
			}
			this.channel.close();
		}
		catch(Exception  ex )
		{
			log.error( ex.getMessage(), ex );
			
			ex.printStackTrace();
		}
		finally
		{
			ClientSessionManager.INSTANCE.remove(clientId );
		}
	}
}
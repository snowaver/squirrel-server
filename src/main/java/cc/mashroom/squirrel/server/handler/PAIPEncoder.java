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

import  org.slf4j.LoggerFactory;

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.ChannelHandlerContext;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.call.CloseCallPacket;
import  cc.mashroom.util.ObjectUtils;

public  class  PAIPEncoder  extends  cc.mashroom.squirrel.paip.codec.PAIPEncoder
{
	private  final  static  org.slf4j.Logger  logger = LoggerFactory.getLogger( PAIPEncoder.class );
	
	protected  void  encode( ChannelHandlerContext  context,Packet  packet,ByteBuf  byteBuf )  throws  Exception
	{
		if( packet instanceof CallAckPacket && ObjectUtils.cast(packet,CallAckPacket.class).getResponseCode() != CallAckPacket.ACCEPT || packet instanceof CloseCallPacket )
		{
			context.channel().attr(CallPacket.CALL_ID).set( null );
			
			context.channel().attr(CallPacket.CALL_CONTACT_ID).set(      null );
		}
		else
		if( packet instanceof CallPacket      )
		{
			context.channel().attr(CallPacket.CALL_CONTACT_ID).compareAndSet( null,ObjectUtils.cast(packet,CallPacket.class).getContactId() );
			
			context.channel().attr(CallPacket.CALL_ID).compareAndSet( null,ObjectUtils.cast(packet,CallPacket.class).getCallId() );
		}
		
		super.encode( context,packet,byteBuf );
	}
}
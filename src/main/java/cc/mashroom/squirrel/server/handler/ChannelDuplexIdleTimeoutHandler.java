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

import  io.netty.channel.ChannelDuplexHandler;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.handler.timeout.IdleState;
import  io.netty.handler.timeout.IdleStateEvent;

import  org.joda.time.DateTime;

import  cc.mashroom.util.ObjectUtils;

public  class  ChannelDuplexIdleTimeoutHandler  extends  ChannelDuplexHandler
{
	public  void  userEventTriggered( ChannelHandlerContext  context,Object  event )  throws  Exception
	{
		if( event instanceof IdleStateEvent && ObjectUtils.cast(event,IdleStateEvent.class).state() == IdleState.READER_IDLE )
		{
			System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.IDLE:\tclose  the  channel  for  reader  idle." );
			
            context.fireChannelInactive().close();
        }
		else
		{
            super.userEventTriggered( context , event );
        }
	}
}
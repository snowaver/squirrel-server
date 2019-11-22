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

import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;

import  java.util.List;

import  org.joda.time.DateTime;

import  com.google.common.collect.Lists;

public  class      PAIPPacketInboundHandlerAdapter  extends  ChannelInboundHandlerAdapter
{
	public  PAIPPacketInboundHandlerAdapter( PAIPObjectProcessor...   processors )
	{
		this.processors = Lists.newArrayList( processors );
	}
	protected  List<PAIPObjectProcessor>  processors;
	@Override
	public  void  exceptionCaught( ChannelHandlerContext  context,Throwable  err )
	{
		err.printStackTrace();
	}
	@Override
	public  void  channelRead(     ChannelHandlerContext  context,Object  object )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  CHANNEL.READ:\t"+object.toString() );
		
		this.processors.stream().filter((processor) -> processor.isProcessable(object)).forEach( (processor) -> processor.process(context.channel(),object) );
	}
}
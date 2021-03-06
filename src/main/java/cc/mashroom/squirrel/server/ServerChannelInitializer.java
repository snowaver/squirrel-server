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
package cc.mashroom.squirrel.server;

import  io.netty.channel.socket.SocketChannel;
import  io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import  io.netty.handler.ssl.SslHandler;
import  lombok.AllArgsConstructor;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoderHandlerAdapter;
import  cc.mashroom.squirrel.paip.codec.PAIPEncoderHandlerAdapter;
import  cc.mashroom.squirrel.server.handler.ChannelDuplexIdleTimeoutHandler;
import  cc.mashroom.squirrel.server.handler.PAIPAuthorityHandlerAdapter;
import  cc.mashroom.squirrel.server.handler.PAIPCallManagerProcessor;
import  cc.mashroom.squirrel.server.handler.PAIPDirectRouteProcessor;
import  cc.mashroom.squirrel.server.handler.PAIPPacketInboundHandlerAdapter;
import cc.mashroom.squirrel.server.handler.PAIPPacketToRouteHandlerAdapter;
import  cc.mashroom.squirrel.server.handler.PAIPRoamingMessagePersistAndRouteProcessor;
import  cc.mashroom.util.SecureUtils;

import  javax.net.ssl.SSLEngine;

import  cc.mashroom.config.Config;

@AllArgsConstructor

public  class  ServerChannelInitializer  extends  io.netty.channel.ChannelInitializer  <SocketChannel>
{
	protected  PAIPRoamingMessagePersistAndRouteProcessor  roamingMessagePersistAndRouteProcessor;
	@Override
	protected  void  initChannel( SocketChannel  channel)  throws  Exception
	{
		SSLEngine  sslEngine = SecureUtils.getSSLContext(Config.server.getProperty("server.ssl.key-store-password"),Config.server.getProperty("server.ssl.key-store").replace("classpath:","/")).createSSLEngine();
		
		sslEngine.setUseClientMode(false);
		/*
		sslEngine.setNeedClientAuth(true);
		*/
		channel.pipeline().addLast("ssl",new  SslHandler(sslEngine)).addLast("idle_timeout",new  ChannelDuplexIdleTimeoutHandler()).addLast("length_based_decoder",new  LengthFieldBasedFrameDecoder(2*1024*1024,0,4,0,4)).addLast("decoder",new  PAIPDecoderHandlerAdapter()).addLast("encoder",new  PAIPEncoderHandlerAdapter()).addLast("authority",new  PAIPAuthorityHandlerAdapter()).addLast("packet_to_route",new  PAIPPacketToRouteHandlerAdapter()).addLast( "handler",new  PAIPPacketInboundHandlerAdapter(this.roamingMessagePersistAndRouteProcessor,new  PAIPCallManagerProcessor(),new  PAIPDirectRouteProcessor()) );
	}
}
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

import  cc.mashroom.squirrel.server.handler.PAIPRoamingMessagePersistAndRouteProcessor;
import  io.netty.bootstrap.ServerBootstrap;
import  io.netty.channel.ChannelOption;
import  io.netty.channel.MultithreadEventLoopGroup;
import  io.netty.channel.epoll.EpollEventLoopGroup;
import  io.netty.channel.epoll.EpollServerSocketChannel;
import  io.netty.channel.nio.NioEventLoopGroup;
import  io.netty.channel.socket.nio.NioServerSocketChannel;
import  lombok.SneakyThrows;

public  class  NettyAcceptor
{
	public  void  stop()
	{
		acceptEventGroup.shutdownGracefully();  handleEventGroup.shutdownGracefully();
	}
	
	private  MultithreadEventLoopGroup  acceptEventGroup;
	
	private  MultithreadEventLoopGroup  handleEventGroup;
	
	@SneakyThrows( value=  {InterruptedException.class} )
	public  NettyAcceptor  initialize( String  host,int  port,PAIPRoamingMessagePersistAndRouteProcessor  roamingMessagePersistAndRouteProcessor )
	{
		boolean  isLinuxSystem = System.getProperty("os.name").toLowerCase().contains( "linux" );
		
		new  ServerBootstrap().group( this.acceptEventGroup = isLinuxSystem ? new  EpollEventLoopGroup() : new  NioEventLoopGroup(),this.handleEventGroup = isLinuxSystem ? new  EpollEventLoopGroup() : new  NioEventLoopGroup()).channel(isLinuxSystem ? EpollServerSocketChannel.class : NioServerSocketChannel.class)/*.option(ChannelOption.SO_TIMEOUT,120)*/.option(ChannelOption.SO_BACKLOG,1024*1024).option(ChannelOption.SO_REUSEADDR,true).childHandler(new  ServerChannelInitializer(roamingMessagePersistAndRouteProcessor)).bind(host,port).sync();  return  this;
	}
}
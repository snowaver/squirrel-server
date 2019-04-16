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
package cc.mashroom.squirrel.common;

import  org.springframework.boot.context.event.ApplicationPreparedEvent;
import  org.springframework.boot.context.event.ApplicationReadyEvent;
import  org.springframework.context.ApplicationEvent;
import  org.springframework.context.ApplicationListener;
import  org.springframework.context.event.ContextStoppedEvent;

import  cc.mashroom.config.Config;
import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.plugin.PluginRegistry;
import  cc.mashroom.plugin.db.Db;
import  cc.mashroom.plugin.h2.H2CacheFactoryStrategy;
import  cc.mashroom.plugin.ignite.IgniteCacheFactoryStrategy;
import cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.server.NettyAcceptor;
import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.squirrel.server.remotetask.RemoteEventProcessor;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.xcache.CacheFactoryStrategy;
import  cc.mashroom.xcache.rpcs.RemoteEventHandler;

public  class       ComponentLifecycle  implements  ApplicationListener<ApplicationEvent>
{
	private  NettyAcceptor  socketAcceptor;
	
	public  void  onApplicationEvent( ApplicationEvent  event )
	{
		if( event instanceof ApplicationReadyEvent )
		{
			socketAcceptor = new  NettyAcceptor().initialize( Config.server.getProperty("host","0.0.0.0"),Config.server.getInt("port",8012) );
		}
		else
		if( event         instanceof ApplicationPreparedEvent )
		{
			RemoteEventHandler.getInstance().setProcessor( new  RemoteEventProcessor() );
			
			CacheFactoryStrategy  cacheFactoryStrategy = Config.server.getBoolean("cluster.enabled",false) ? new  IgniteCacheFactoryStrategy() : new  H2CacheFactoryStrategy();
			
			PluginRegistry.INSTANCE.register(new  Db()).register((Plugin)  cacheFactoryStrategy).register(ClientSessionManager.INSTANCE).register(ChatGroupUserManager.INSTANCE).register(CallManager.INSTANCE).initialize();
			
			ServerInfo.INSTANCE.setLocalNodeId(  cacheFactoryStrategy.getLocalNodeId() );
		}
		else
		if( event   instanceof ContextStoppedEvent )
		{
			if( socketAcceptor != null ) socketAcceptor.stop();
			
			PluginRegistry.INSTANCE.stop();
		}
	}
}
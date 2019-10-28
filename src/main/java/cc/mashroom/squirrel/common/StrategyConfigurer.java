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

import  org.intellij.lang.annotations.Identifier;
import  org.springframework.beans.factory.annotation.Value;
import  org.springframework.context.annotation.Bean;
import  org.springframework.context.annotation.Configuration;
import  org.springframework.context.annotation.DependsOn;
import  org.springframework.web.servlet.LocaleResolver;
import  org.springframework.web.servlet.i18n.CookieLocaleResolver;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.plugin.PluginManager;
import  cc.mashroom.plugin.db.Db;
import  cc.mashroom.plugin.h2.H2CacheFactoryStrategy;
import  cc.mashroom.plugin.ignite.IgniteCacheFactoryStrategy;
import  cc.mashroom.squirrel.module.call.manager.CallManager;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import  cc.mashroom.squirrel.module.user.manager.ContactManager;
import  cc.mashroom.squirrel.server.NettyAcceptor;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.squirrel.server.storage.MessageStorageEngine;
import  cc.mashroom.xcache.CacheFactoryStrategy;

@Configuration
public  class  StrategyConfigurer
{
	@Bean( name="COOKIE_LOCALE_RESOLVER" )
	
	public  LocaleResolver getLocaleResolver()
	{
		return    new  CookieLocaleResolver();
	}
	@Bean( name="NETTY_ACCEPTOR",destroyMethod="stop" )
	@DependsOn( value={"PLUGIN_MANAGER"} )
	public  NettyAcceptor  nettyAcceptor( @Value("${squirrel.acceptor.host:0.0.0.0}")  String  host  ,@Value("${squirrel.acceptor.port:8012}")  int  port,@Identifier  MessageStorageEngine  messageStorageEngine )
	{
		return  new  NettyAcceptor().initialize( host , port, messageStorageEngine );
	}
	@Bean( name="PLUGIN_MANAGER",destroyMethod="stop",initMethod="initialize" )
	public  PluginManager  pluginManager( @Value("${squirrel.cluster.enabled:false}")  boolean isCacheClusterEnabled,@Value("${squirrel.memory.policy:/memory-policy.ddl}")  String  cacheMemoryPolicyScriptFileResourcePath )
	{
		CacheFactoryStrategy  cacheFactoryStrategy= isCacheClusterEnabled ? new  IgniteCacheFactoryStrategy() : new  H2CacheFactoryStrategy();
		
		return  PluginManager.INSTANCE.register(new  Db()).register((Plugin)  cacheFactoryStrategy,cacheMemoryPolicyScriptFileResourcePath).register(ClientSessionManager.INSTANCE).register(ChatGroupManager.INSTANCE).register(CallManager.INSTANCE).register( ContactManager.INSTANCE );
	}
}
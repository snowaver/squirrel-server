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

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import  org.springframework.context.annotation.Bean;
import  org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import  org.springframework.web.servlet.LocaleResolver;
import  org.springframework.web.servlet.i18n.CookieLocaleResolver;

import cc.mashroom.config.Config;
import cc.mashroom.plugin.Plugin;
import cc.mashroom.plugin.db.Db;
import cc.mashroom.plugin.h2.H2CacheFactoryStrategy;
import cc.mashroom.plugin.ignite.IgniteCacheFactoryStrategy;
import cc.mashroom.squirrel.module.call.manager.CallManager;
import cc.mashroom.squirrel.server.NettyAcceptor;
import cc.mashroom.squirrel.server.ServerInfo;
import cc.mashroom.squirrel.server.session.ClientSessionManager;
import cc.mashroom.util.ObjectUtils;
import cc.mashroom.xcache.CacheFactory;
import cc.mashroom.xcache.CacheFactoryStrategy;
import cc.mashroom.xcache.XCacheStrategy;
import cc.mashroom.xcache.XKeyValueCache;

@Configuration
public  class  StrategyConfigurer
{
	@Bean( name="LOCALE_RESOLVER"   )
	public  LocaleResolver getLocaleResolver()
	{
		return    new  CookieLocaleResolver();
	}
	@Bean( name="DB"  ,destroyMethod= "stop" )
	public  Db  db()
	{
		return  new  Db();
	}
	@Bean( name="CACHE_FACTORY_STRATEGY",destroyMethod="stop" )
	public  CacheFactoryStrategy  cacheFactoryStrategy( @Value("${squirrel.cluster.enabled:false}")  boolean  isClusterEnabled,@Value("${squirrel.memory.policy:/memory-policy.ddl}")  String  scriptFileResourcePath )  throws  Exception
	{
		CacheFactoryStrategy  cacheFactoryStrategy = isClusterEnabled ? new  IgniteCacheFactoryStrategy() : new  H2CacheFactoryStrategy();
		
		ObjectUtils.cast(cacheFactoryStrategy,Plugin.class).initialize( scriptFileResourcePath );
		
		ServerInfo.INSTANCE.setLocalNodeId(cacheFactoryStrategy.getLocalNodeId() );
		
		return  cacheFactoryStrategy;
	}
	@Bean( name="CLIENT_SESSION_MANAGER",initMethod="initialize",destroyMethod="stop" )
	public  ClientSessionManager  clientSessionManager()
	{
		return  ClientSessionManager.INSTANCE;
	}
	@Bean( name="CALL_MANAGER",initMethod="initialize",destroyMethod="stop" )
	public  CallManager callManager()
	{
		return  CallManager.INSTANCE;
	}
	@Bean( name="NETTY_ACCEPTOR", destroyMethod="stop" )
	@Order( value=Integer.MAX_VALUE )
	public  NettyAcceptor  nettyAcceptor( @Value( "${squirrel.acceptor.host:0.0.0.0}" )  String  host,@Value("${squirrel.acceptor.port:8012}")  int  port )
	{
		return  new  NettyAcceptor().initialize( host , port );
	}
}
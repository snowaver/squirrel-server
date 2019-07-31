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
package cc.mashroom.squirrel.interceptor;

import  java.io.IOException;
import  java.util.Set;
import  java.util.concurrent.TimeUnit;

import  javax.servlet.Filter;
import  javax.servlet.FilterChain;
import  javax.servlet.ServletException;
import  javax.servlet.ServletRequest;
import  javax.servlet.ServletResponse;
import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;

import  org.apache.commons.lang.StringUtils;
import  org.springframework.stereotype.Component;

import  com.google.common.cache.CacheBuilder;
import  com.google.common.cache.CacheLoader;
import  com.google.common.cache.LoadingCache;
import  com.google.common.collect.Sets;

import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;

@Component

public  class  IdentityRecognitionFilter  extends  CacheLoader<String,Map<String,Object>>  implements  Filter
{
	public  final  static  LoadingCache<String,Map<String,Object>>  CACHE = CacheBuilder.newBuilder().initialCapacity(8).expireAfterAccess(24,TimeUnit.HOURS).expireAfterWrite(24,TimeUnit.HOURS).maximumSize(819200).build( new  IdentityRecognitionFilter() );
	
	public  final  static  Set<String>  EXCLUDE_URIS = Sets.newHashSet( "/user/signin","/system/balancingproxy","/monitoring/tosignin" );
	
	public  final  static  Set<String>  EXCLUDE_SUFFIXES = Sets.newHashSet( ".css",".js",".ico",".jpg",".png",".gif",".woff",".woff2",".ttf", ".properties" );
	/**
	 *  secret  key  responsed  on  'user/signin'  interface  should  be  attatched  to  the  request  by  header  or  parameters  named  'SECRET_KEY'.
	 */
	public  void  doFilter( ServletRequest  request,ServletResponse  response,FilterChain  chain )  throws  IOException, ServletException
	{
		String  requestUri= ((HttpServletRequest)  request).getRequestURI();
		
		if( "/user".equals(requestUri) && "POST".equalsIgnoreCase(  ((HttpServletRequest)  request).getMethod()) || (requestUri.indexOf(".") != -1 && EXCLUDE_SUFFIXES.contains(requestUri.substring(requestUri.lastIndexOf(".")).toLowerCase())) || EXCLUDE_URIS.contains(requestUri) || requestUri.matches("\\/user\\/[0-9]+\\/portrait") )
		{
			chain.doFilter( request, response );
		}
		else
		try
		{
			String  secretKey = ObjectUtils.cast(request,HttpServletRequest.class).getHeader( "SECRET_KEY" );
			
			if( StringUtils.isBlank(secretKey) )
			{
				secretKey = request.getParameter( "SECRET_KEY" );
			}
			
			if( StringUtils.isBlank(secretKey) )
			{
				throw  new  IllegalStateException(String.format("SQUIRREL-SERVER:  ** IDENTITY  RECOGNITION  INTERCEPTOR **  secret  key  was  not  found  both  in  headers  and  parameters  for  request  uri:  %s",requestUri) );
			}
			else
			{
				Map<String,Object>  sessionProfile = CACHE.get( secretKey );
				
				if( sessionProfile    == null ||  sessionProfile.isEmpty() )
				{
					throw  new  IllegalStateException( String.format("SQUIRREL-SERVER:  ** IDENTITY  RECOGNITION  INTERCEPTOR **  session  profile  (secret  key:  %s)  was  not  found  for  request  uri:  %s" , secretKey , requestUri) );
				}
				else
				{
					request.setAttribute("SESSION_PROFILE",sessionProfile );
				}
			}
			
			chain.doFilter( request, response );
		}
		catch( Throwable  e )
		{
			if( e instanceof IllegalStateException && ( "/monitoring/tomain".equals(requestUri ) || "/clustering/tolist".equals(requestUri ) || "/user/tolist".equals(requestUri) ) )
			{
				//  sending  redirect  to  '/monitoring/tosignin'  is  not  available  while  it  only  changes  the  iframe  if  iframe  used.  so  use  a  empty  html  that  includs  a  top  location  skipping  script  instead.
				response.getWriter().write( "<html><head><script  type='text/javascript'>top.location.href = '/monitoring/tosignin';</script></head><body></body></html>" );
			}
			else
			{
				e.printStackTrace();
				
				ObjectUtils.cast(response,HttpServletResponse.class).setStatus(    500 );
				
				response.getWriter().write(JsonUtils.toJson(new  HashMap<String,Object>().addEntry("STATUS","EM50001").addEntry("MESSAGE",e.getMessage())) );
			}
		}
	}
	
	public  Map<String,Object>  load( String  secretKey )  throws  Exception
	{
		Map<String,Object>  sessionProfile = CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").lookupOne( Map.class,"SELECT  USER_ID,CREATE_TIME,ACCESS_TIME,CLUSTER_NODE_ID,SECRET_KEY,IS_ONLINE  FROM  SESSION_LOCATION  WHERE  SECRET_KEY = ?",new  Object[]{ secretKey } );
		
		return  sessionProfile == null ? new  HashMap<String,Object>() :  sessionProfile;  //  Guava  cache  loader  should  return  a  non-null  value ,  so  create  a  new  empty  map  by  default  to  avoid  loading  exception.
	}
}
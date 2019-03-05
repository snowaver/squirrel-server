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

import  java.util.LinkedList;
import  java.util.List;
import  java.util.concurrent.atomic.AtomicLong;

import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;

import  org.joda.time.DateTime;
import  org.springframework.web.servlet.HandlerInterceptor;
import  org.springframework.web.servlet.ModelAndView;

import  cc.mashroom.util.StringUtils;

public  class  LoggingInterceptor  implements  HandlerInterceptor
{
	private  AtomicLong  counter = new  AtomicLong();
	
	public  boolean  preHandle( HttpServletRequest  request,HttpServletResponse  response,Object  object )  throws  Exception
	{
		request.setAttribute(   "request.counter",counter.incrementAndGet() );
		
		printtimeLine( request );
		
		return  true;
	}
	
	private  void  printtimeLine(   HttpServletRequest  request )
	{
		System.out.println( StringUtils.leftPad("=",39,"=")+" "+StringUtils.rightPad(String.valueOf(request.getAttribute("request.counter")),20," ")+DateTime.now().toString()+" "+StringUtils.leftPad("=",39,"=") );
	}
	
	public  void  postHandle( HttpServletRequest  request,HttpServletResponse  response,Object  object,ModelAndView  modelAndView )  throws  Exception
	{
		System.out.println( "URI:\t\t"+request.getRequestURI() );
		
		System.out.println( "METHOD:\t\t"+request.getMethod().toUpperCase() );
		
		List<String>  parameters     = new  LinkedList<String>();
		
		request.getParameterMap().entrySet().forEach( (entry) -> parameters.add( entry.getKey()+"="+(entry.getValue().length <= 0 ? "" : entry.getValue()[0]) ) );
		
		System.out.println( "PARAMETERS:\t"+StringUtils.join(parameters.toArray(),"&") );
	}

	public  void  afterCompletion(  HttpServletRequest  request,HttpServletResponse  response,Object  object,Exception  exception )  throws  Exception
	{
		System.out.println( "RESPONSE:\t" +response.getContentType()+ "/" +response.getStatus() );  printtimeLine( request );
	}
}
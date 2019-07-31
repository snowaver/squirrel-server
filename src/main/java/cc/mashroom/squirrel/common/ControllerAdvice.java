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

import  org.joda.time.DateTime;
import  org.springframework.core.MethodParameter;
import  org.springframework.http.MediaType;
import  org.springframework.http.converter.HttpMessageConverter;
import  org.springframework.http.server.ServerHttpRequest;
import  org.springframework.http.server.ServerHttpResponse;
import  org.springframework.http.server.ServletServerHttpRequest;
import  org.springframework.http.server.ServletServerHttpResponse;
import  org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import  cc.mashroom.util.StringUtils;

@org.springframework.web.bind.annotation.ControllerAdvice
public  class  ControllerAdvice  implements  ResponseBodyAdvice<Object>
{
	public  boolean  supports( MethodParameter  returnType,Class<? extends HttpMessageConverter<?>>  converterType )
	{
		return  true;
	}

	@Override
	public  Object  beforeBodyWrite( Object  body,MethodParameter  returnType,MediaType  selectedContentType,Class<? extends HttpMessageConverter<?>>  selectedConverterType,ServerHttpRequest  request,ServerHttpResponse  response )
	{
		System.out.println( DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS")+"  "+StringUtils.rightPad(String.valueOf(((ServletServerHttpRequest)  request).getServletRequest().getAttribute("REQUEST_COUNTER")),13," ")+"  "+selectedContentType+"/"+((ServletServerHttpResponse)  response).getServletResponse().getStatus()+"/"+body );
		
		return  body;
	}

}

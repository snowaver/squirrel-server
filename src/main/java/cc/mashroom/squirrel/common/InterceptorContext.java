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

import  org.springframework.context.annotation.Configuration;
import  org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import  org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import  org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import  cc.mashroom.squirrel.interceptor.LoggingInterceptor;

@Configuration
public  class  InterceptorContext  implements  WebMvcConfigurer
{
	public  void  addInterceptors( InterceptorRegistry  registry )
	{
		registry.addInterceptor(new  LocaleChangeInterceptor()).addPathPatterns( "/**" );
		
		registry.addInterceptor(new  LoggingInterceptor()).addPathPatterns( "/**" );
	}
}
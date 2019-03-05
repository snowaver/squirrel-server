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

import  org.springframework.context.annotation.Bean;
import  org.springframework.context.annotation.Configuration;
import  org.springframework.web.servlet.LocaleResolver;
import  org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public  class   StrategyConfigurer
{
	/*
	@Bean
	public  UndertowServletWebServerFactory  getUndertowServletWebServerFactory()
	{
		UndertowServletWebServerFactory  undertowServletWebServerFactory = new  UndertowServletWebServerFactory();
		
		undertowServletWebServerFactory.addBuilderCustomizers( (builder) -> builder.addHttpListener(8012,"0.0.0.0") );
		
		undertowServletWebServerFactory.addDeploymentInfoCustomizers( (customizer) -> customizer.addSecurityConstraint(new  SecurityConstraint().addWebResourceCollection(new  WebResourceCollection().addUrlPattern("/**")).setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL).setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT)).setConfidentialPortManager((exchange) -> 8011) );
		
		return  undertowServletWebServerFactory;
	}
	*/
	@Bean( name="localeResolver" )
	
	public  LocaleResolver   getLocaleResolver()
	{
		return  new  CookieLocaleResolver();
	}
}
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
package cc.mashroom.squirrel.module.monitoring.controller;

import  javax.servlet.http.HttpServletRequest;

import  org.springframework.ui.Model;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RestController;
import  org.springframework.web.servlet.ModelAndView;

import  cc.mashroom.squirrel.common.AbstractController;

@RequestMapping( "/monitoring" )
@RestController
public  class  MonitoringController  extends  AbstractController
{
	@RequestMapping( value=  "/tomain" )
	public  ModelAndView  tomain( Model  model,HttpServletRequest  request )
	{
		model.addAttribute( "sessionProfile",request.getAttribute("SESSION_PROFILE") );
		
		return  new  ModelAndView(   "monitoring/main" );
	}
	
	@RequestMapping( value="/tosignin" )
	public  ModelAndView  tosignin( Model  model )
	{
		return  new  ModelAndView( "monitoring/signin" );
	}
}
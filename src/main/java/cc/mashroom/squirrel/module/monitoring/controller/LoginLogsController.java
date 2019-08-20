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

import  javax.servlet.http.HttpServletResponse;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.monitoring.service.LoginLogsService;

@RequestMapping( "/loginlogs" )
@RestController
public  class  LoginLogsController    extends  AbstractController
{
	@Autowired
	private  LoginLogsService  service;
	
	@RequestMapping( value="/lookup",method={RequestMethod.GET} )
	public  ResponseEntity<String>  lookup( @RequestParam("page")  int  page,@RequestParam("pageSize")  int  pageSize,HttpServletResponse  response )
	{
		return  service.lookup( page,pageSize );
	}
}
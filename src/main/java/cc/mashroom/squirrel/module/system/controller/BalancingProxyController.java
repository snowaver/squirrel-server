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
package cc.mashroom.squirrel.module.system.controller;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.system.service.BalancingProxyService;

@RequestMapping( "/system" )
@RestController
public  class  BalancingProxyController  extends  AbstractController
{
	@Autowired
	private  BalancingProxyService  service;
	
	@RequestMapping( value="/balancingproxy",method={RequestMethod.GET} )
	public  ResponseEntity<String>  lookup( @RequestParam("action")  int  action,@RequestParam("keyword")  String  keyword )
	{
		return  service.lookup( action,keyword );
	}
}
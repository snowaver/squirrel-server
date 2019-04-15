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
package cc.mashroom.squirrel.module.user.controller;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.user.service.OfflineService;
import  cc.mashroom.util.HttpUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/offline" )
@RestController
public  class  OfflineController  extends  AbstractController
{
	@Autowired
	private  OfflineService  service;
	
	@RequestMapping( value="/search" , method={RequestMethod.GET} )
	public  ResponseEntity<String>  search( @RequestParam("action")  int  action,@RequestParam("keyword")  String  keyword,@RequestParam("extras")  String  extras,@RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile )
	{
		return  service.search( action,sessionProfile.getLong("USER_ID"),JsonUtils.fromJson(HttpUtils.decodeQuietly(extras,"UTF-8"),new  TypeReference<Map<String,Map<String,Object>>>(){}) );
	}
}
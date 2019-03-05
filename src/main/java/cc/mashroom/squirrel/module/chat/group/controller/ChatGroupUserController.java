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
package cc.mashroom.squirrel.module.chat.group.controller;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.chat.group.service.ChatGroupUserService;
import cc.mashroom.util.CollectionUtils;

@RequestMapping( "/chat/group/user" )
@RestController
public  class  ChatGroupUserController  extends  AbstractController
{
	@Autowired
	private  ChatGroupUserService  service;
	
	@RequestMapping( value="",method={RequestMethod.POST} )
	public  ResponseEntity<String>  add( @RequestParam("chatGroupId")  long  id,@RequestParam("contactIds")  String  contactIds )
	{
		return  service.add( id,Lists.newArrayList(CollectionUtils.toLongArray(contactIds.trim().split(","))) );
	}
}
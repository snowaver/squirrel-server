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

import  java.util.LinkedList;
import  java.util.List;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.chat.group.service.ChatGroupUserService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.CollectionUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/chat/group/user" )
@RestController
public  class  ChatGroupUserController  extends  AbstractController
{
	@Autowired
	private  ChatGroupUserService  service;
	
	@RequestMapping( value="",method={RequestMethod.POST} )
	public  ResponseEntity<String>  add( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId,@RequestParam("inviteeIds")  String  inviteeIdsSeperatedWithComma )
	{
		ResponseEntity<List<Map<String,Object>>>  responseEntity = service.add( sessionProfile.getLong("USER_ID"),chatGroupId,Lists.newArrayList(CollectionUtils.toLongArray(inviteeIdsSeperatedWithComma.trim().split(","))) );
		
		GroupChatEventPacket  groupChatEventPacket = new  GroupChatEventPacket( chatGroupId,GroupChatEventPacket.EVENT_MEMBER_ADDED,new  HashMap<String,Object>().addEntry("CHAT_GROUPS",new  LinkedList<Map<String,Object>>()).addEntry("CHAT_GROUP_USERS",responseEntity.getBody()) );
		
		ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).forEach( (userId) -> {if(userId != sessionProfile.getLong("USER_ID"))  PacketRoute.INSTANCE.route(userId,groupChatEventPacket);} );
		
		return  ResponseEntity.ok( JsonUtils.toJson(responseEntity.getBody()) );
	}
}
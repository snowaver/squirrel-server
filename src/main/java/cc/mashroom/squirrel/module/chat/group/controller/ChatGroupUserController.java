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

import  java.util.ArrayList;
import  java.util.List;
import  java.util.stream.Collectors;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  com.google.common.collect.Sets;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.chat.group.service.ChatGroupUserService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import cc.mashroom.squirrel.server.ServerInfo;
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
	
	@RequestMapping( value="",method={RequestMethod.POST  } )
	public  ResponseEntity<String>  add(    @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId") long  chatGroupId,@RequestParam("inviteeIds")   String  inviteeIdsSeperatedWithComma )
	{
		List<Long>  inviteeIds =         new  ArrayList<Long>( Sets.newHashSet( CollectionUtils.toLongArray(inviteeIdsSeperatedWithComma.trim().split(",")) ) );
		
		ResponseEntity<Map<String,List<? extends Map>>>   responseEntity=service.add( sessionProfile.getLong("USER_ID"),chatGroupId,inviteeIds );
		
		List<Map<String,Object>>  inviteeChatGroupUsers = responseEntity.getBody().get("CHAT_GROUP_USERS").stream().filter((chatGroupUser) -> inviteeIds.contains(chatGroupUser.getLong("CONTACT_ID"))).collect(Collectors.toList());
		
		for(  Map<String,Object>          chatGroupUser : responseEntity.getBody().get("CHAT_GROUP_USERS") )
		{
			if(       sessionProfile.getLong("USER_ID") != chatGroupUser.getLong("CONTACT_ID").longValue() )
			{
				if( inviteeIds.contains( chatGroupUser.getLong("CONTACT_ID") ) )
				{
					PacketRoute.INSTANCE.route( chatGroupUser.getLong("CONTACT_ID"),new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_MEMBER_ADDED,ServerInfo.INSTANCE.getLocalNodeId(),  responseEntity.getBody()) );
				}
				else
				{
					PacketRoute.INSTANCE.route( chatGroupUser.getLong("CONTACT_ID"),new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_MEMBER_ADDED,ServerInfo.INSTANCE.getLocalNodeId(),  new  HashMap<String,List<? extends Map>>().addEntry("CHAT_GROUP_USERS",inviteeChatGroupUsers).addEntry("CHAT_GROUPS",responseEntity.getBody().get("CHAT_GROUPS"))) );
				}
			}
		}
		
		return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<>().addEntry("CHAT_GROUP_USERS",inviteeChatGroupUsers).addEntry("CHAT_GROUPS",responseEntity.getBody().get("CHAT_GROUPS"))) );
	}
	
	@RequestMapping( value="",method={RequestMethod.PUT   } )
	public  ResponseEntity<String>  update( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId, @RequestParam("chatGroupUserId" )  long  chatGroupUserId,@RequestParam("vcard")  String  vcard )
	{
		ResponseEntity<Map<String,List<? extends Map>>>  responseEntity = service.update( sessionProfile.getLong("USER_ID"),chatGroupId,chatGroupUserId,vcard );
	
		List<? extends Map>  chatGroupAllUsers= responseEntity.getBody().remove(   "CHAT_GROUP_ALL_USERS" );
		
		GroupChatEventPacket  groupChatMemberUpdatedEventPacket = new  GroupChatEventPacket( chatGroupId,GroupChatEventPacket.EVENT_MEMBER_UPDATED,ServerInfo.INSTANCE.getLocalNodeId(),responseEntity.getBody() );
		
		chatGroupAllUsers.forEach( (chatGroupRemainingUser) -> {if(chatGroupRemainingUser.getLong("CONTACT_ID").longValue()!= sessionProfile.getLong("USER_ID"))  PacketRoute.INSTANCE.route(chatGroupRemainingUser.getLong("CONTACT_ID"),groupChatMemberUpdatedEventPacket);} );
		
		return  ResponseEntity.ok( JsonUtils.toJson(responseEntity.getBody()) );
	}
	
	@RequestMapping( value="",method={RequestMethod.DELETE} )
	public  ResponseEntity<String>  secede( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId, @RequestParam("chatGroupUserId" )  long  chatGroupUserId )
	{
		ResponseEntity<Map<String,List<? extends Map>>>  responseEntity =  this.service.remove( sessionProfile.getLong("USER_ID"),chatGroupId,chatGroupUserId );
		
		List<? extends Map>  chatGroupAllUsers= responseEntity.getBody().remove(   "CHAT_GROUP_ALL_USERS" );
		
		GroupChatEventPacket  groupChatMemberRemovedEventPacket = new  GroupChatEventPacket( chatGroupId,GroupChatEventPacket.EVENT_MEMBER_REMOVED,ServerInfo.INSTANCE.getLocalNodeId(),responseEntity.getBody() );
		
		chatGroupAllUsers.forEach( (chatGroupRemainingUser) -> PacketRoute.INSTANCE.route(chatGroupRemainingUser.getLong("CONTACT_ID") , groupChatMemberRemovedEventPacket) );
		
		return  ResponseEntity.ok( JsonUtils.toJson(responseEntity.getBody()) );
	}
}
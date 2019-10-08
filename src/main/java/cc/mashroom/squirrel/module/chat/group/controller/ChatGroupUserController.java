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

import  com.google.common.collect.Lists;
import  com.google.common.collect.Sets;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupSync;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.model.OoIData;
import  cc.mashroom.squirrel.module.chat.group.service.ChatGroupUserService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/chat/group/user" )
@RestController
public  class  ChatGroupUserController  extends  AbstractController
{
	@Autowired
	private  ChatGroupUserService  service;
	
	@RequestMapping( value="",method={RequestMethod.POST  } )
	public  ResponseEntity<OoIData>  add(    @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId,@RequestParam("inviteeIds")  String  inviteeIdsSeperatedWithComma )
	{
		List<Long>  inviteeIds = new  ArrayList<Long>( Sets.newHashSet(Lists.newArrayList(StringUtils.split(inviteeIdsSeperatedWithComma.trim(),",")).parallelStream().map( (inviteeId) -> Long.parseLong(inviteeId)).collect(Collectors.toList())) );
		
		ResponseEntity<OoIData>  responseEntity = service.add(  sessionProfile.getLong("USER_ID"),chatGroupId,inviteeIds );
		
		java.util.Map<Long,Long>  chatGroupSyncIds = responseEntity.getBody().getChatGroupSyncs().stream().collect( Collectors.toMap(ChatGroupSync::getUserId,ChatGroupSync::getSyncId) );
		
		List<ChatGroupUser>  inviteeChatGroupUsers = responseEntity.getBody().getChatGroupUsers().stream().filter((chatGroupUser) -> inviteeIds.contains(chatGroupUser.getContactId())).collect( Collectors.toList() );
		
		responseEntity.getBody().getChatGroupUsers().stream().filter((chatGroupUser) -> sessionProfile.getLong("USER_ID") != chatGroupUser.getContactId()).forEach( (chatGroupUser) -> PacketRoute.INSTANCE.route( chatGroupUser.getContactId(),new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_MEMBER_ADDED,ServerInfo.INSTANCE.getLocalNodeId(),JsonUtils.toJson(new  OoIData(responseEntity.getBody().getChatGroups(),inviteeIds.contains(chatGroupUser.getContactId()) ? responseEntity.getBody().getChatGroupUsers() : inviteeChatGroupUsers).setChatGroupSyncId((long)  chatGroupSyncIds.get(chatGroupUser.getContactId()))))) );

		return  ResponseEntity.ok( new  OoIData(responseEntity.getBody().getChatGroups(),inviteeChatGroupUsers).setChatGroupSyncId(chatGroupSyncIds.get(sessionProfile.getLong("USER_ID"))) );
	}
	
	@RequestMapping( value="",method={RequestMethod.PUT   } )
	public  ResponseEntity<OoIData>  update( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId,@RequestParam("chatGroupUserId" )  long  chatGroupUserId,@RequestParam("vcard")  String  vcard )
	{
		ResponseEntity<OoIData>  responseEntity = service.update( sessionProfile.getLong("USER_ID"),chatGroupId,chatGroupUserId,vcard );
		
		java.util.Map<Long,Long>  chatGroupSyncIds = responseEntity.getBody().getChatGroupSyncs().stream().collect( Collectors.toMap(ChatGroupSync::getUserId,ChatGroupSync::getSyncId) );
		
		ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).stream().filter((contactId) -> sessionProfile.getLong("USER_ID") != contactId).forEach( (contactId) -> PacketRoute.INSTANCE.route(contactId,new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_MEMBER_UPDATED,ServerInfo.INSTANCE.getLocalNodeId(),JsonUtils.toJson(new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers()).setChatGroupSyncId(chatGroupSyncIds.get(contactId))))) );
		
		return  ResponseEntity.ok( new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers() ).setChatGroupSyncId(       chatGroupSyncIds.get(sessionProfile.getLong("USER_ID"))));
	}
	
	@RequestMapping( value="",method={RequestMethod.DELETE} )
	public  ResponseEntity<OoIData>  secede( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId,@RequestParam("chatGroupUserId" )  long  chatGroupUserId )
	{
		ResponseEntity<OoIData>  responseEntity =  this.service.remove( sessionProfile.getLong("USER_ID"),chatGroupId,chatGroupUserId );
		
		java.util.Map<Long,Long>  chatGroupSyncIds = responseEntity.getBody().getChatGroupSyncs().stream().collect( Collectors.toMap(ChatGroupSync::getUserId,ChatGroupSync::getSyncId) );
		
		ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).stream().filter((contactId) -> sessionProfile.getLong("USER_ID") != contactId).forEach( (contactId) -> PacketRoute.INSTANCE.route(contactId,new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_MEMBER_REMOVED,ServerInfo.INSTANCE.getLocalNodeId(),JsonUtils.toJson(new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers()).setChatGroupSyncId(chatGroupSyncIds.get(contactId))))) );
		
		return  ResponseEntity.ok( new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers() ).setChatGroupSyncId(       chatGroupSyncIds.get(sessionProfile.getLong("USER_ID"))));
	}
}
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

import  java.util.stream.Collectors;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupUserManager;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupSync;
import  cc.mashroom.squirrel.module.chat.group.model.OoIData;
import  cc.mashroom.squirrel.module.chat.group.service.ChatGroupService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.server.ServerInfo;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping("/chat/group" )
@RestController
public  class  ChatGroupController  extends  AbstractController
{
	@Autowired
	private  ChatGroupService  service;
	
	@RequestMapping( method={RequestMethod.POST  } )
	public  ResponseEntity<OoIData>  add(    @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("name") String  name )
	{
		return  this.service.add( sessionProfile.getLong("USER_ID"),name );
	}
	
	@RequestMapping( method={RequestMethod.PUT   } )
	public  ResponseEntity<OoIData>  update( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("chatGroupId")  long  chatGroupId,@RequestParam("name")  String  name )
	{
		ResponseEntity<OoIData>  responseEntity = this.service.update( sessionProfile.getLong("USER_ID"),chatGroupId,name );
		
		java.util.Map<Long,Long>  chatGroupSyncIds = responseEntity.getBody().getChatGroupSyncs().stream().collect(  Collectors.toMap(ChatGroupSync::getUserId,ChatGroupSync::getSyncId) );
		
		ChatGroupUserManager.INSTANCE.getChatGroupUserIds(chatGroupId).parallelStream().filter((contactId) -> contactId != sessionProfile.getLong("USER_ID")).forEach( (contactId) -> PacketRoute.INSTANCE.route(contactId,new  GroupChatEventPacket(chatGroupId,GroupChatEventPacket.EVENT_GROUP_UPDATED,ServerInfo.INSTANCE.getLocalNodeId(),JsonUtils.toJson(new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers()).setChatGroupSyncId(chatGroupSyncIds.get(contactId))))) );
		
		return  ResponseEntity.ok( new  OoIData(responseEntity.getBody().getChatGroups(),responseEntity.getBody().getChatGroupUsers()).setChatGroupSyncId( chatGroupSyncIds.get(sessionProfile.getLong("USER_ID"))) );
	}
}
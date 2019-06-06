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

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.user.service.ContactService;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.squirrel.paip.message.subscribes.UnsubscribePacket;
import  cc.mashroom.squirrel.server.handler.PacketRoute;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/contact" )
@RestController
public  class  ContactController  extends  AbstractController
{
	@Autowired
	private  ContactService  service;
	
	@RequestMapping( value="/status",method={RequestMethod.POST  } )
	public  ResponseEntity<String>  subscribe( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("subscribeeId")  long  subscribeeId,@RequestParam("remark")  String  remark,@RequestParam("group")  String  group )
	{
		ResponseEntity<Map<String,Object>>  responseEntity = service.subscribe( sessionProfile.getLong("USER_ID"),subscribeeId,remark,group );
		
		PacketRoute.INSTANCE.route( subscribeeId,new  SubscribePacket(sessionProfile.getLong("USER_ID"),(Map<String,Object>)  responseEntity.getBody().remove("SUBSCRIBER_PROFILE")) );
		
		return  ResponseEntity.status(200).body( JsonUtils.toJson( responseEntity.getBody()  ) );
	}
	/**
	 *  changing  subscribe  status  is  only  for  accepting,  since  decline  is  not  available  according  to  the  control  flow  design.
	 */
	@RequestMapping( value="/status",method={RequestMethod.PUT   } )
	public  ResponseEntity<String>  changeSubscribeStatus(@RequestParam("status")  int  status,@RequestParam("subscriberId")  long  subscriberId,@RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("remark")  String  remark,@RequestParam("group")  String  group )
	{
		ResponseEntity<Map<String,Object>>  responseEntity = service.changeSubscribeStatus( status,subscriberId,sessionProfile.getLong("USER_ID"),remark,group );
		
		PacketRoute.INSTANCE.route( subscriberId,new  SubscribePacket(sessionProfile.getLong("USER_ID"),(Map<String,Object>)  responseEntity.getBody().remove("SUBSCRIBEE_PROFILE")) );
		
		return  ResponseEntity.status(200).body( JsonUtils.toJson( responseEntity.getBody()  ) );
	}
	/**
	 *  status:  200:  unsubscribed,  send  unsubscribe  packet  to  unsubscribee;  601:  failed.
	 */
	@RequestMapping( value="/status",method={RequestMethod.DELETE} )
	public  ResponseEntity<String>  unsubscribe( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("unsubscribeeId")  long  unsubscribeeId )
	{
		ResponseEntity<String>  response  = service.unsubscribe( sessionProfile.getLong("USER_ID"),unsubscribeeId );
		
		if( response.getStatusCodeValue()== 200 )
		{
			PacketRoute.INSTANCE.route(  unsubscribeeId,new  UnsubscribePacket(sessionProfile.getLong("USER_ID")) );
		}
		
		return  response;
	}
	
	@RequestMapping( method={RequestMethod.PUT} )
	public  ResponseEntity<String>  update( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("contactId")  long  contactId,@RequestParam("remark")  String  remark,@RequestParam("group")  String  group )
	{
		return  service.update( sessionProfile.getLong("USER_ID") , contactId , remark , group );
	}
}
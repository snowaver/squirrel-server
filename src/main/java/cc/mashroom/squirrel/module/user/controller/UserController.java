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

import  java.io.File;
import  java.io.IOException;

import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;

import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Controller;
import  org.springframework.ui.Model;
import  org.springframework.web.bind.WebDataBinder;
import  org.springframework.web.bind.annotation.InitBinder;
import  org.springframework.web.bind.annotation.PathVariable;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.ResponseBody;
import  org.springframework.web.multipart.MultipartFile;
import  org.springframework.web.servlet.ModelAndView;

import  com.fasterxml.jackson.core.type.TypeReference;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.config.Config;
import  cc.mashroom.squirrel.module.user.conversion.UserEditor;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.squirrel.module.user.service.UserService;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/user" )
@Controller
public  class  UserController  extends  AbstractController
{	
	@Autowired
	private  UserService  service;
	
	@RequestMapping( value="/{userId}/portrait", method={RequestMethod.GET} )
	@ResponseBody
	public  void  portrait( @PathVariable("userId")  long  userId,HttpServletResponse  response )  throws  IOException
	{
		render( response,new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),StringUtils.join(new  Object[]{"user",userId,"portrait.def"},"/")),null );
	}
	/**
	 *  status:  500:  unknown  error,  empty  response  body;  601:  bad  username  or  password,  empty  response  body;  602:  protocol  version  not  matched,  empty  response  body;  200:  success,  ID,  USERNAME,  NAME,  NICKNAME  AND  ROLETYPE  in  response  body.
	 */
	@RequestMapping( value="/signin", method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  signin( @RequestParam("username")  String  username,@RequestParam("password")  String  password,@RequestParam(name="protocolVersion",required=false,defaultValue="1")  int  protocolVersion,@RequestParam(name="roletype",required=false,defaultValue="0")  Integer  roletype,@RequestParam(name="longitude",required=false)  Double  longitude,@RequestParam(name="latitude",required=false)  Double  latitude,@RequestParam(name="mac",required=false)  String  mac,HttpServletRequest  request )
	{
		return  protocolVersion != ConnectPacket.CURRENT_PROTOCOL_VERSION ? ResponseEntity.status(602).body("") : service.signin( username,password,roletype,getRemoteAddress(request),longitude,latitude,mac );
	}

	@InitBinder
	public  void  binder(  WebDataBinder  binder )
	{
		binder.registerCustomEditor(User.class,new  UserEditor() );
	}
	
	@RequestMapping( method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  add( @RequestParam("user")  User  user , @RequestParam(name = "portrait",required = false)  MultipartFile  portrait )  throws  IOException
	{
		return  service.add( user,portrait );
	}
	
	@RequestMapping( value="/logout", method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  logout( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile )
	{
		return  service.logout(sessionProfile.getLong("USER_ID") );
	}
	
	@RequestMapping( value="/tolist" )
	public  ModelAndView  tolist( Model  model, HttpServletRequest  request )
	{
		model.addAttribute( "sessionProfile",request.getAttribute("SESSION_PROFILE") );
		
		return  new  ModelAndView(  "user/list" );
	}
	
	@RequestMapping( method={RequestMethod.GET } )
	@ResponseBody
	public  ResponseEntity<String>  get( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile,@RequestParam("userId")  long  userId )
	{
		return  service.get( userId );
	}
	
	@RequestMapping( value="/lookup", method={RequestMethod.GET } )
	@ResponseBody
	public  ResponseEntity<String>  lookup( @RequestParam("action")  int  action,@RequestParam("keyword")  String  keyword,@RequestParam("extras")  String  extras )
	{
		return  service.lookup( action,keyword,new  HashMap<String,Object>().addEntries(JsonUtils.fromJson(extras,new  TypeReference<Map<String,Object>>(){})) );
	}
}
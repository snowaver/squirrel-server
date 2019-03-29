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
import  org.springframework.web.bind.annotation.PathVariable;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.ResponseBody;
import  org.springframework.web.multipart.MultipartFile;
import  org.springframework.web.servlet.ModelAndView;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.config.Config;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.squirrel.module.user.service.UserService;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/user" )
@Controller
public  class  UserController  extends  AbstractController
{	
	@Autowired
	private  UserService  service;
	
	@RequestMapping( value= "/{userId}/portrait" )
	@ResponseBody
	public  void  portrait( @PathVariable("userId")  long  userId,HttpServletResponse  response )  throws  IOException
	{
		render( response,new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),StringUtils.join(new  Object[]{"user",userId,"portrait.def"},"/")),null );
	}
	
	@RequestMapping( value="/signin", method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  signin( @RequestParam("username")  String  username,@RequestParam("password")  String  password,@RequestParam(name="roletype",required=false,defaultValue="0")  Integer  roletype,@RequestParam(name="longitude",required=false)  Double  longitude,@RequestParam(name="latitude",required=false)  Double  latitude,@RequestParam(name="mac",required=false)  String  mac,HttpServletRequest  request )
	{
		return  service.signin( username,password,roletype,getRemoteAddress(request),longitude,latitude,mac );
	}

	@RequestMapping( method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  add( @RequestParam("user")  String  user , @RequestParam(name="portrait" , required=false)  MultipartFile  portrait )  throws  IOException
	{
		return  service.add( ObjectUtils.cast(new  User().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(user)),User.class),portrait );
	}
	
	@RequestMapping( value="/logout", method={RequestMethod.POST} )
	@ResponseBody
	public  ResponseEntity<String>  logout( @RequestAttribute("SESSION_PROFILE")  Map<String,Object>  sessionProfile )
	{
		return  service.logout(sessionProfile.getLong("USER_ID") );
	}
	
	@RequestMapping( value="/tolist" )
	public  ModelAndView  tolist( Model  model,HttpServletRequest  request )
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
	
	@RequestMapping( value="/search", method={RequestMethod.GET } )
	@ResponseBody
	public  ResponseEntity<String>  search( @RequestParam("action")  int  action,@RequestParam("keyword")  String  keyword,@RequestParam("extras")  String  extras )
	{
		return  service.search( action, keyword , new  HashMap<String,Object>().addEntries((java.util.Map<String,Object>)  JsonUtils.fromJson(extras)) );
	}
}
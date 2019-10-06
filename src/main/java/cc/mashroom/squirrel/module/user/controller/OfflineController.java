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
import  org.springframework.web.bind.WebDataBinder;
import  org.springframework.web.bind.annotation.InitBinder;
import  org.springframework.web.bind.annotation.RequestAttribute;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestParam;
import  org.springframework.web.bind.annotation.RestController;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.squirrel.module.user.conversion.OoiDataCheckpointsEditor;
import  cc.mashroom.squirrel.module.user.model.OoIData;
import  cc.mashroom.squirrel.module.user.model.OoiDataCheckpoints;
import  cc.mashroom.squirrel.module.user.service.OfflineService;
import  cc.mashroom.util.collection.map.Map;

@RequestMapping( "/offline" )
@RestController
public  class  OfflineController  extends  AbstractController
{
	@Autowired
	private  OfflineService  service;
	
	@RequestMapping(value="/lookup" )
	public  ResponseEntity<OoIData>  lookup( @RequestAttribute("SESSION_PROFILE" )  Map<String,Object>  sessionProfile,@RequestParam("checkpoints")  OoiDataCheckpoints  checkpoints )
	{
		return  service.lookup( 0,sessionProfile.getLong("USER_ID"),checkpoints );
	}
	
	@InitBinder
	public  void  binder( WebDataBinder  binder )
	{
		binder.registerCustomEditor( OoiDataCheckpoints.class,new  OoiDataCheckpointsEditor() );
	}
}
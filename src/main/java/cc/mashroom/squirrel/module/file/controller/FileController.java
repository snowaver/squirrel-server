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
package cc.mashroom.squirrel.module.file.controller;

import  java.io.File;
import  java.io.IOException;

import  javax.servlet.http.HttpServletResponse;

import  org.springframework.http.ResponseEntity;
import  org.springframework.web.bind.annotation.PathVariable;
import  org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RequestMethod;
import  org.springframework.web.bind.annotation.RestController;
import  org.springframework.web.multipart.MultipartFile;
import  org.springframework.web.multipart.MultipartHttpServletRequest;

import  cc.mashroom.squirrel.common.AbstractController;
import  cc.mashroom.config.Config;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.StringUtils;

@RequestMapping( "/file" )
@RestController
public  class  FileController  extends  AbstractController
{	
	@RequestMapping( method = { RequestMethod.POST } )
	public  ResponseEntity<String>  add( MultipartHttpServletRequest  request )  throws  IOException
	{
		for( MultipartFile  file : request.getFileMap().values() )
		{
			if( !file.isEmpty() )
			{
				FileUtils.writeByteArrayToFile( new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),"file/"+file.getOriginalFilename()),file.getBytes() );
			}
		}
		
		return  ResponseEntity.status(200).body( "" );
	}
	
	@RequestMapping( value="/{name}",method={RequestMethod.GET } )
	public  void  get( @PathVariable("name")  String  name,HttpServletResponse  response )  throws  IOException
	{
		render( response,new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),StringUtils.join(new  Object[]{"file",name},"/")),null );
	}
}
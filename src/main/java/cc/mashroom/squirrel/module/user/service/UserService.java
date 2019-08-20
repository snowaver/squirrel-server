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
package cc.mashroom.squirrel.module.user.service;

import  java.io.IOException;

import  org.springframework.http.ResponseEntity;
import  org.springframework.web.multipart.MultipartFile;

import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.util.collection.map.Map;

public  interface  UserService
{
	public  ResponseEntity<String>  lookup( int  action,String  keyword,Map<String,Object>  extras );
	
	public  ResponseEntity<String>  add( User  user,MultipartFile  file )  throws  IOException;
	
	public  ResponseEntity<String>  get(    long  userId );
	
	public  ResponseEntity<String>  signin( String  username,String  password,Integer  roletype,String  ip,Double  longitude,Double  latitude,String  mac );
	
	public  ResponseEntity<String>  logout( long  userId );
}
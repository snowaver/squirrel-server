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
package cc.mashroom.squirrel.module.chat.group.service;

import  org.springframework.http.ResponseEntity;

import  cc.mashroom.util.collection.map.Map;

public  interface  ChatGroupService
{
	public  ResponseEntity<String>  remove( long  id );
	
	public  ResponseEntity<String>  add( long  userId,String  name );
		
	public  ResponseEntity<String>  update(  long  id,String  name );
	
	public  ResponseEntity<String>  search( int  action,String  keyword,Map<String,Map<String,Object>>  extras );
}
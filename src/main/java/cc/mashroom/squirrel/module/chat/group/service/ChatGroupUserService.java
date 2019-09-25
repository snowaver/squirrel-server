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

import  java.util.List;

import  org.springframework.http.ResponseEntity;

import  cc.mashroom.util.collection.map.Map;

public  interface  ChatGroupUserService
{
	public  ResponseEntity<Map<String,List<? extends Map>>>  add( long  inviterId,long  chatGroupId,List  <Long>  inviteeIds );
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  update( long  updatorId,long  chatGroupId,long  chatGroupUserId,String  newVcard );
	
	public  ResponseEntity<Map<String,List<? extends Map>>>  remove( long  removerId,long  chatGroupId,long  chatGroupUserId );
}
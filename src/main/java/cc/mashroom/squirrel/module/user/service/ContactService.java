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

import  org.springframework.http.ResponseEntity;

import cc.mashroom.util.collection.map.Map;

public  interface  ContactService
{
	public  ResponseEntity<Map<String,Object>>  update( long  userId,long  contactId,String  remark,String  group );
	
	public  ResponseEntity<Map<String,Object>>  subscribe( long  subscriberId,long  subscribeeId,String  remark,String  group );
	
	public  ResponseEntity<Map<String,Object>>  changeSubscribeStatus( int  status,long  subscriberId,long  subscribeeId,String  remark,String  group );
	
	public  ResponseEntity<Map<String,Object>>  unsubscribe( long  unsubscriberId,long  unsubscribeeId );
}
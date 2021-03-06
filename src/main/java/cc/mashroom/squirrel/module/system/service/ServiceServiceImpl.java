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
package cc.mashroom.squirrel.module.system.service;

import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.system.repository.ServiceRepository;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  ServiceServiceImpl  implements  ServiceService
{
	@Connection( dataSource=@DataSource(name="squirrel",type="db") )
	
	public  ResponseEntity<String>  lookup( int  action,String  keyword )
	{
		switch( action )
		{
			case  0:
			{
				return  ResponseEntity.ok( JsonUtils.toJson(ServiceRepository.DAO.lookup(Map.class,"SELECT  ID,HOST,PORT,`SCHEMA`  FROM  "+ServiceRepository.DAO.getDataSourceBind().table())) );
			}
			case  1:
			{
				return  ResponseEntity.ok( JsonUtils.toJson(ServiceRepository.DAO.lookup(Map.class,"SELECT  ID,HOST,PORT,`SCHEMA`  FROM  "+ServiceRepository.DAO.getDataSourceBind().table()+"  WHERE  APPLICATION_ID = ?  ORDER  BY  HOST  ASC",new  Object[]{Integer.parseInt(keyword)})) );
			}
		}
		
		return  ResponseEntity.status(404).body( "" );
	}
}
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
package cc.mashroom.squirrel.module.monitoring.service;

import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.logs.repository.LoginLogsRepository;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  LoginLogsServiceImpl  implements  LoginLogsService
{
	@Connection(  dataSource=@DataSource(type="db",name="squirrel") )
	
	public  ResponseEntity<String>  lookup( int  page,int  pageSize )
	{
		return  ResponseEntity.ok( JsonUtils.toJson(LoginLogsRepository.DAO.lookup(Map.class,"SELECT  CREATE_TIME,USERNAME,STATE,IP,IP_LOCATION,ASTEXT(GEOMETRY)  AS  GEOMETRY,MAC  FROM  "+LoginLogsRepository.DAO.getDataSourceBind().table()+"  ORDER  BY  CREATE_TIME  DESC  LIMIT  "+pageSize*(page-1)+","+pageSize)) );
	}
}
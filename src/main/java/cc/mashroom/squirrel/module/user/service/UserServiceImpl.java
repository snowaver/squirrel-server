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

import  java.io.File;
import  java.io.IOException;
import  java.sql.Timestamp;
import  java.util.UUID;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;
import  org.springframework.web.multipart.MultipartFile;

import  cc.mashroom.config.Config;
import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.common.IPLocator;
import  cc.mashroom.squirrel.module.logs.repository.LoginLogsRepository;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.squirrel.module.user.repository.UserRepository;
import  cc.mashroom.squirrel.server.session.ClientSession;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  cc.mashroom.xcache.CacheFactory;
import  lombok.SneakyThrows;
import  cc.mashroom.util.DigestUtils;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

@Service
public  class  UserServiceImpl     implements  UserService
{
	@Connection( dataSource = @DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  add( User  user , MultipartFile  portrait )  throws  IOException
	{
		Reference<Object>  generatedUserId = new  Reference<Object>();
		
		if( UserRepository.DAO.insert(generatedUserId,"INSERT  INTO  "+UserRepository.DAO.getDataSourceBind().table()+"  (USERNAME,PASSWORD,NAME,NICKNAME,ROLETYPE)  SELECT  ?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  USERNAME = ?)",new  Object[]{user.getUsername(),new  String(DigestUtils.md5Hex(user.getPassword())).toUpperCase(),user.getName(),user.getNickname(),0,user.getUsername()}) >= 1 )
		{
			FileUtils.writeByteArrayToFile( new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),"user/"+generatedUserId.get()+"/portrait.def"),portrait == null ? FileUtils.readFileToByteArray(new  File(Config.use("server.properties").getProperty("squirrel.data.dir"),"user/portrait.def")) : portrait.getBytes() );
		
			return  ResponseEntity.status(200).body( "" );
		}
		else
		{
			return  ResponseEntity.status(601).body( "" );
		}
	}
	
	@SneakyThrows
	public  ResponseEntity<String>  logout( long  userId )
	{
		ClientSession  clientSession = ClientSessionManager.INSTANCE.get( userId );
		
		if( clientSession != null )
		{
			clientSession.close(0);
		}
		
		CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").update( "DELETE  FROM  SESSION_LOCATION  WHERE  USER_ID = ?",new  Object[]{userId} );
		
		return  ResponseEntity.status(   200 ).body( "" );
	}
	
	@Connection( dataSource = @DataSource(type="db",name="squirrel"),transactionIsolationLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  signin(   String  username,String  password,Integer  roletype,String  ip,Double  longitude,Double  latitude,String  mac )
	{
		User  user = UserRepository.DAO.lookupOne( User.class,"SELECT  ID,USERNAME,NAME,NICKNAME,ROLETYPE  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  USERNAME = ?  AND  PASSWORD = ?  AND  ROLETYPE = ?",new  Object[]{username,password,roletype} );
		
		String  secretKey= UUID.randomUUID().toString().toUpperCase();
		
		if( user != null )
		{
			CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").update( "MERGE  INTO  SESSION_LOCATION  (USER_ID,CREATE_TIME,CLUSTER_NODE_ID,SECRET_KEY)  VALUES  (?,?,?,?)",new  Object[]{user.getId(),new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis()),null,secretKey} );
		}
		
		LoginLogsRepository.DAO.insert( new  Reference<Object>(),"INSERT  INTO  "+LoginLogsRepository.DAO.getDataSourceBind().table()+"  (CREATE_TIME,USERNAME,STATE,IP,IP_LOCATION,GEOMETRY,MAC)  VALUES  (?,?,?,?,?,"+(longitude == null || latitude == null ? "?" : "ST_GEOMFROMTEXT(?)")+",?)",new  Object[]{new  Timestamp(DateTime.now(DateTimeZone.UTC).getMillis()),username,user == null ? 0 : 1,ip,IPLocator.locate(ip),longitude == null || latitude == null ? null : "POINT("+longitude+" "+latitude+")",mac} );
		
		return  user == null ? ResponseEntity.status(601).body("") : ResponseEntity.ok(JsonUtils.toJson(new  HashMap<String,Object>().addEntry("ID",user.getId()).addEntry("USERNAME",user.getUsername()).addEntry("NAME",user.getName()).addEntry("NICKNAME",user.getNickname()).addEntry("ROLETYPE",user.getRoleType()).addEntry("SECRET_KEY",secretKey)));
	}
		
	@Connection( dataSource=@DataSource(type="db" , name="squirrel") )
	
	public  ResponseEntity<String>  get(    long  userId )
	{
		Map<String,Object>  user = UserRepository.DAO.lookupOne( Map.class,"SELECT  ID,USERNAME,NAME,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{userId} );
		
		return  user == null ? ResponseEntity.status(601).body("") : ResponseEntity.ok( JsonUtils.toJson(user) );
	}
	
	@Connection( dataSource=@DataSource(type="db" , name="squirrel") )
	
	public  ResponseEntity<String>  search( int  action,String  keyword,Map<String,Object>  extras )
	{
		switch( action )
		{
			case  0:
			{
				return  ResponseEntity.ok( JsonUtils.toJson(UserRepository.DAO.lookup(Map.class,"SELECT  ID,USERNAME,NAME,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  USERNAME = ?",new  Object[]{keyword})) );
			}
			case  1:
			{
				return  ResponseEntity.ok( JsonUtils.toJson(UserRepository.DAO.lookup(Map.class,"SELECT  ID,USERNAME,NAME,NICKNAME  FROM  "+UserRepository.DAO.getDataSourceBind().table()+"  WHERE  ROLETYPE = ?",new  Object[]{0})) );
			}
		}
		
		throw  new  IllegalStateException(  "SQUIRREL-SERVER:  ** USER  SERVICE  IMPL **  action  not  found." );
	}
}
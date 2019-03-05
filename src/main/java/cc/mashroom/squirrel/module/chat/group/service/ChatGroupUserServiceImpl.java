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

import  java.sql.Timestamp;
import  java.util.LinkedList;
import  java.util.List;

import  org.apache.commons.lang3.StringUtils;
import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.user.model.User;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.Reference;

@Service
public  class  ChatGroupUserServiceImpl  implements  ChatGroupUserService
{
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  add(  long  chatGroupId , List<Long>  contactIds )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		Map<Long,String>  nicknamesMapper  = new  HashMap<Long,String>();
		
		List<Reference<Object>>  ids     = new  LinkedList<Reference<Object>>();
		
		List<Object[]>  params    = new  LinkedList<Object[]>();
		
		List<Map<String,Object>>  response    = new  LinkedList<Map<String,Object>>();
		
		User.dao.search("SELECT  ID,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  WHERE  ID  IN  ("+StringUtils.rightPad("?",(contactIds.size()-1)*2,",?")+")",contactIds.toArray(new  Long[contactIds.size()])).forEach( (user) -> nicknamesMapper.addEntry(user.getLong("ID"),user.getString("NICKNAME")) );
		
		for( Long  contactId : contactIds )
		{
			params.add( new  Object[]{chatGroupId, contactId, nicknamesMapper.getString(contactId), now,now,chatGroupId,contactId} );
		}
		
		ChatGroupUser.dao.insert( ids,"INSERT  INTO  "+ChatGroupUser.dao.getDataSourceBind().table()+"  (CHAT_GROUP_ID,CONTACT_ID,VCARD,CREATE_TIME,LAST_MODIFY_TIME)  SELECT  ?,?,?,?,?  FROM  DUAL  WHERE  NOT  EXISTS  (SELECT  ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?)",params.toArray(new  Object[0][]) );
		
		CacheFactory.createCache( "CHATGROUP_USER_IDS_CACHE" ).remove(  chatGroupId );
		//  updateing  cache  is  not  suggested  while  considering  data  consistence  between  cache  and  database.
		/*
		Set<Long>  chatGroupUserIds = chatGroupUserIdsCache.get(  chatGroupId );
		
		if( chatGroupUserIdsCache.put( chatGroupId,new  HashSet<Long>(Sets.union(chatGroupUserIds == null ? new  HashSet<Long>() : new  HashSet<Long>(chatGroupUserIds),new  HashSet<Long>(contactIds))) ) )
		{
			throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  GROUP  USER  SERVICE  IMPL **  chatgroup  user  ids  cache  update  error." );
		}
		*/
		for( Reference<Object>  id  : ids )
		{
			if( id.get() != null && Long.parseLong( id.get().toString() ) >= 1 )
			{
				/*
				oldIds.add( ObjectUtils.cast(id.get(),BigInteger.class).longValue() );
				*/
				response.add( new  HashMap<String,Object>().addEntry("ID",id.get()).addEntry("VCARD",nicknamesMapper.getString(contactIds.get(ids.indexOf(id)))).addEntry("CREATE_TIME",now).addEntry("LAST_MODIFY_TIME",now).addEntry("CONTACT_ID",contactIds.get(ids.indexOf(id))).addEntry("CHAT_GROUP_ID",chatGroupId) );
			}
		}
		
		return  ResponseEntity.ok( JsonUtils.toJson(response) );
	}

	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  update( long  id , String  newvcard )
	{
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
		
		if( ChatGroupUser.dao.update("UPDATE  "+ChatGroupUser.dao.getDataSourceBind().table()+"  SET  VCARD = ?,LAST_MODIFY_TIME = ?  WHERE  ID = ?",new  Object[]{newvcard , now , now}) >= 1 )
		{
			return  ResponseEntity.ok( JsonUtils.toJson(new  HashMap<String,Object>().addEntry("id",id ).addEntry("lastModifyTime",now)) );
		}
		
		return  ResponseEntity.status(601).body( "" );
	}
	
	@Connection( dataSource=@DataSource(type="db",name="squirrel"),transactionLevel=java.sql.Connection.TRANSACTION_REPEATABLE_READ )
	
	public  ResponseEntity<String>  remove( long  id )
	{
		return  ResponseEntity.status(ChatGroupUser.dao.update("DELETE  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{id}) >= 1 ? 200 : 601).body( "" );
	}
}
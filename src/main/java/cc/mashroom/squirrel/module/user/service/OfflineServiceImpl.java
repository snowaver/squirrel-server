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

import  java.sql.Timestamp;
import  java.util.List;
import  java.util.stream.Collectors;

import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  com.google.common.collect.Lists;

import  cc.mashroom.db.annotation.DataSource;
import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupSync;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupSyncRepository;
import  cc.mashroom.squirrel.module.chat.group.repository.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.user.model.Contact;
import  cc.mashroom.squirrel.module.user.model.OfflineChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.OfflineChatMessage;
import  cc.mashroom.squirrel.module.user.model.OoIData;
import  cc.mashroom.squirrel.module.user.model.OoiDataCheckpoints;
import  cc.mashroom.squirrel.module.user.repository.ContactRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineChatMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.OfflineGroupChatMessageRepository;
import  cc.mashroom.squirrel.paip.message.TransportState;

@Service
public  class  OfflineServiceImpl  implements  OfflineService
{
	@Connection( dataSource = @DataSource(type="db", name="squirrel") )
	
	public  ResponseEntity<OoIData>  lookup( int  action,long  userId,OoiDataCheckpoints  checkpoints )
	{
		OoIData  ooiData = new  OoIData().setContacts( checkpoints.getContactCheckpoint() == null ? Lists.newArrayList() : ContactRepository.DAO.lookup(Contact.class,"SELECT  CONTACT_ID  AS  ID,CONTACT_USERNAME  AS  USERNAME,CREATE_TIME,LAST_MODIFY_TIME,SUBSCRIBE_STATUS,REMARK,GROUP_NAME,IS_DELETED  FROM  "+ContactRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  LAST_MODIFY_TIME > ?  ORDER  BY  ID  ASC",new  Object[]{userId,new  Timestamp(checkpoints.getContactCheckpoint())}) );
		
		if(     checkpoints.getChatGroupCheckpoint() !=null )
		{
			if( checkpoints.getChatGroupCheckpoint() ==   0 )
			{
				ooiData.setChatGroups( ChatGroupRepository.DAO.lookup(ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  ORDER  BY  ID  ASC",new  Object[]{userId}) );
				
				ooiData.setChatGroupUsers( ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?)  ORDER  BY  LAST_MODIFY_TIME  ASC",new  Object[]{userId}) );
			
				ooiData.setChatGroupSyncId(0L);
			}
			else
			if( checkpoints.getChatGroupCheckpoint() >    0 )
			{
				ooiData.setChatGroups( Lists.newArrayList() ).setChatGroupUsers(Lists.newArrayList() );
				
				List<ChatGroupSync>   chatGroupSyncs = ChatGroupSyncRepository.DAO.lookup( ChatGroupSync.class,"SELECT  *  FROM  "+ChatGroupSyncRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?  AND  SYNC_ID > ?  ORDER  BY  SYNC_ID  DESC",new  Object[]{userId,checkpoints.getChatGroupCheckpoint()} );
				
				ooiData.setChatGroupSyncId(0L);
				
				if( !     chatGroupSyncs.isEmpty() )
				{
					for(ChatGroupSync  chatGroupSync : chatGroupSyncs )
					{
						ooiData.getChatGroups().addAll( ChatGroupRepository.DAO.lookup(ChatGroup.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  LAST_MODIFY_TIME = ?",new  Object[]{chatGroupSync.getChatGroupId(),chatGroupSync.getLastModifyTime()}) );
						
						ooiData.getChatGroupUsers().addAll( ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  ID,IS_DELETED,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CHAT_GROUP_ID,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  LAST_MODIFY_TIME = ?",new  Object[]{chatGroupSync.getChatGroupId(),chatGroupSync.getLastModifyTime()}) );
					}
					
					ooiData.setChatGroupSyncId( chatGroupSyncs.get(0).getSyncId() );
				}
			}
		}
		
		ooiData.setOfflineChatMessages( checkpoints.getChatMessageCheckpoint() == null ? Lists.newArrayList() : OfflineChatMessageRepository.DAO.lookup(OfflineChatMessage.class,"SELECT  ID,USER_ID  AS  CONTACT_ID,MD5,CONTENT_TYPE,CONTENT  FROM  "+OfflineChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{userId,new  Timestamp(checkpoints.getChatMessageCheckpoint())}).stream().map((offlineChatMessage) -> offlineChatMessage.setCreateTime(new  Timestamp(offlineChatMessage.getId())).setTransportState(TransportState.RECEIVED.getValue())).collect(Collectors.toList()) );
		
		ooiData.setOfflineGroupChatMessages( checkpoints.getGroupChatMessageCheckpoint() == null ? Lists.newArrayList() : OfflineGroupChatMessageRepository.DAO.lookup(OfflineChatGroupMessage.class,"SELECT  ID,GROUP_ID,USER_ID  AS  CONTACT_ID,MD5,CONTENT_TYPE,CONTENT  FROM  "+OfflineGroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID  IN  (SELECT  CHAT_GROUP_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  AND  IS_DELETED = FALSE)  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{userId,new  Timestamp(checkpoints.getGroupChatMessageCheckpoint())}).stream().map((offlineGroupChatMessage) -> offlineGroupChatMessage.setCreateTime(new  Timestamp(offlineGroupChatMessage.getId())).setTransportState(TransportState.RECEIVED.getValue())).collect(Collectors.toList()) );  return  ResponseEntity.ok( ooiData );
	}
}
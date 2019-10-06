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
package cc.mashroom.squirrel.module.user.model;

import  java.util.List;

import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.squirrel.module.chat.group.model.ChatGroup;
import  cc.mashroom.squirrel.module.chat.group.model.ChatGroupUser;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@NoArgsConstructor
@AllArgsConstructor
public  class  OoIData
{
	@JsonProperty( value="CHAT_GROUPS"   )
	private  List<ChatGroup>   chatGroups;
	@JsonProperty( value="CHAT_GROUP_USERS"  )
	private  List<ChatGroupUser>   chatGroupUsers;
	@JsonProperty( value="CHAT_GROUP_SYNC_ID")
	private  Long    chatGroupSyncId;
	@JsonProperty( value="CONTACTS" )
	private  List<Contact>  contacts;
	@JsonProperty( value="OFFLINE_CHAT_MESSAGES" )
	private  List<OfflineChatMessage>  offlineChatMessages;
	@JsonProperty( value="OFFLINE_GROUP_CHAT_MESSAGES" )
	private  List<OfflineChatGroupMessage>  offlineGroupChatMessages;
}
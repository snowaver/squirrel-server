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
package cc.mashroom.squirrel.server.storage;

import  java.util.List;
import  java.util.Map;

import  cc.mashroom.squirrel.module.user.model.ChatGroupMessage;
import  cc.mashroom.squirrel.module.user.model.ChatMessage;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;

public  interface  MessageStorageEngine
{
	public  Map<Long,Long>  insert( long  userId,ChatPacket  chatPacket );
	
	public  List<ChatMessage>  lookupChatMessage( long  userId,long  syncOffsetId );

	public  Map<Long,Long>  insert( long  userId,GroupChatPacket  groupChatPacket );
	
	public  List<ChatGroupMessage>  lookupChatGroupMessage( long  userId ,long  syncOffsetId );
}
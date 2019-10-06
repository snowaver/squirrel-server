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
package cc.mashroom.squirrel.module.chat.group.model;

import  java.sql.Timestamp;

import  cc.mashroom.db.annotation.Column;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
@NoArgsConstructor
@AllArgsConstructor
public  class   ChatGroupSync
{
	@Column( name="SYNC_ID" )
	private  Long  syncId;
	@Column( name="USER_ID" )
	private  Long  userId;
	@Column( name="CHAT_GROUP_ID" )
	private  Long      chatGroupId;
	@Column( name="LAST_MODIFY_TIME"  )
	private  Timestamp  lastModifyTime;
	@Column( name="TYPE" )
	private  Integer    type;
}
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

import  com.fasterxml.jackson.annotation.JsonProperty;

import  lombok.Data;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
public  class  OoiDataCheckpoints
{
	@JsonProperty( value="CHAT_MESSAGE_CHECK_POINT" )
	private  Long  chatMessageCheckpoint;
	@JsonProperty( value="GROUP_CHAT_MESSAGE_CHECK_POINT" )
	private  Long  groupChatMessageCheckpoint;
	@JsonProperty( value="CONTACT_CHECK_POINT" )
	private  Long      contactCheckpoint;
	@JsonProperty( value=  "CHAT_GROUP_CHECK_POINT" )
	private  Long  chatGroupCheckpoint;
}
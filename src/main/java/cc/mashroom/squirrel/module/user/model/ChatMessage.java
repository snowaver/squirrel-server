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

import  java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import  com.fasterxml.jackson.annotation.JsonProperty;

import  cc.mashroom.db.annotation.Column;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=  true )
@NoArgsConstructor
@AllArgsConstructor
public  class ChatMessage
{
	@JsonProperty( value="ID"  )
	@Column( name="ID"   )
	private  Long  id;
	@JsonFormat( pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" )
	@JsonProperty( value="CREATE_TIME" )
	@Column( name="CREATE_TIME")
	private  Timestamp  createTime;
	@JsonProperty( value="CONTACT_ID"  )
	@Column( name="CONTACT_ID" )
	private  Long  contactId;
	@JsonProperty( value="USER_ID")
	@Column( name="USER_ID" )
	private  Long  userId;
	@JsonProperty( value="MD5" )
	@Column( name="MD5"  )
	private  String   md5;
	@JsonProperty( value="CONTENT_TYPE")
	@Column( name="CONTENT_TYPE"  )
	private  Integer   contentType;
	@JsonProperty( value="CONTENT")
	@Column( name="CONTENT" )
	private  String  content;
	@JsonProperty( value="TRANSPORT_STATE" )
	private  Integer     transportState;
}
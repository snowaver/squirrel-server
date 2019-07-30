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

import  cc.mashroom.db.annotation.Column;
import  lombok.Data;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain=true )
public  class  User
{
	@Column( name="ID" )
	private  Long  id;
	@Column( name="USERNAME" )
	private  String  username;
	@Column( name="PASSWORD" )
	private  String  password;
	@Column( name="NAME" )
	private  String  name;
	@Column( name="NICKNAME" )
	private  String  nickname;
	@Column( name="ROLETYPE" )
	private  Integer roleType;
}
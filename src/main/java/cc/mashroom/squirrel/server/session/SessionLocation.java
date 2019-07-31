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
package cc.mashroom.squirrel.server.session;

import  java.sql.Timestamp;

import  cc.mashroom.db.annotation.Column;
import  lombok.Data;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain = true )
public  class   SessionLocation
{
	@Column( name="USER_ID"   )
	private  Long  userId;
	@Column( name="CREATE_TIME"   )
	private  Timestamp  createTime;
	@Column( name="ACCESS_TIME"   )
	private  Timestamp  accessTime;
	@Column( name="CLUSTER_NODE_ID" )
	private  String  clusterNodeId;
	@Column( name="SECRET_KEY"    )
	private  String  secretKey;
	@Column( name="IS_ONLINE" )
	private  Boolean  isOnline;
}
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
package cc.mashroom.squirrel.module.user.manager;

import  cc.mashroom.plugin.Plugin;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XKeyValueCache;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor

public  class  ContactManager  implements  Plugin
{
	public  final  static  ContactManager  INSTANCE = new  ContactManager();
	@Getter
	private XKeyValueCache<String,Object>  updateLockerCache; 
	
	public  void  initialize( Object  ...  parameters )
	{
		this.updateLockerCache = CacheFactory.getOrCreateKeyValueCache( "SQUIRREL.CONTACT.UPDATE_LOCKER_CACHE" );
	}
	
	public  void  stop()
	{
		
	}
}
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

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.server.handler.Route;

public  interface  RoamingMessagePersistEngine<P extends Packet<P>>
{
	public  Route<P>  prepersist( Route<P>  route );
	
	public  <T>  List<T>  lookup( Class<T>  clazz,long  userId,long  messageOffsetSyncId );
}
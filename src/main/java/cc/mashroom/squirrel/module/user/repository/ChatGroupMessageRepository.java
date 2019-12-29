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
package cc.mashroom.squirrel.module.user.repository;

import  java.util.List;
import  java.util.concurrent.atomic.AtomicInteger;

import  org.apache.commons.lang.StringUtils;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import cc.mashroom.squirrel.server.handler.PAIPPacketRouter;
import  cc.mashroom.squirrel.server.handler.Route;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(  name="squirrel" , table="chat_group_message" )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  ChatGroupMessageRepository  extends  GenericRepository
{
	public  final   static  ChatGroupMessageRepository  DAO= new  ChatGroupMessageRepository();
	
	public  void  insertAndRoute( List  <Route<GroupChatPacket>>  routes )
	{
		AtomicInteger  i = new  AtomicInteger();
		
		Object[]  params = new  Object[ routes.size()*  9 ];
		
		routes.forEach( (route) -> { params[i.getAndIncrement()] = route.getPacket().getId();  params[i.getAndIncrement()] = route.getPacket().getSyncId();  params[i.getAndIncrement()] = route.getPacket().getGroupId();  params[i.getAndIncrement()] = route.getPacket().getContactId();  params[i.getAndIncrement()] = route.getUserId();  params[i.getAndIncrement()] = route.getPacket().getMd5();  params[i.getAndIncrement()] = new  String(route.getPacket().getContent());  params[i.getAndIncrement()] = route.getPacket().getContentType().getValue();  params[i.getAndIncrement()] = route.getPacket().getContactId() == route.getUserId() ? TransportState.SENT.getValue() : TransportState.RECEIVED.getValue(); } );
		
		if( super.update("INSERT  INTO  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,GROUP_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE,TRANSPORT_STATE)  VALUES  "+StringUtils.rightPad("(?,?,?,?,?,?,?,?,?)",routes.size()*",(?,?,?,?,?,?,?,?,?)".length()-1,",(?,?,?,?,?,?,?,?,?)"),params) == routes.size() )  routes.forEach( (route) -> PAIPPacketRouter.INSTANCE.route(route.getUserId(),route.getPacket()) );
	}
}
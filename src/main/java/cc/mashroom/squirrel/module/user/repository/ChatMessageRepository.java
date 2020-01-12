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

import  java.util.LinkedList;
import  java.util.List;
import  java.util.Optional;

import  org.apache.commons.lang.StringUtils;
import  org.apache.curator.shaded.com.google.common.collect.Lists;

import  cc.mashroom.db.GenericRepository;
import  cc.mashroom.db.annotation.DataSourceBind;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.server.handler.PAIPPacketRouter;
import  cc.mashroom.squirrel.server.handler.RouteGroup;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;

@DataSourceBind(  name="squirrel" ,  table="chat_message" )
@NoArgsConstructor(  access=AccessLevel.PRIVATE )
public  class  ChatMessageRepository  extends  GenericRepository
{
	public  final static    ChatMessageRepository  DAO= new  ChatMessageRepository();
	
	public  void  insertAndRoute( List<RouteGroup  <ChatPacket>>  routeGroups )
	{
		if( routeGroups.isEmpty()  )  return;
		
		List<Object>  params = routeGroups.parallelStream().flatMap(  (routeGroup) -> routeGroup.getRoutes().stream()).collect( () -> new  LinkedList<Object>(),(p,route) -> p.addAll(Lists.newArrayList(route.getPacket().getId(),route.getPacket().getSyncId(),route.getPacket().getContactId(),route.getUserId(),route.getPacket().getMd5(),new  String(route.getPacket().getContent()),route.getPacket().getContentType().getValue(),route.getPacket().getContactId() == route.getUserId() ? TransportState.SENT.getValue() : TransportState.RECEIVED.getValue())),(pf,ps) -> {} );
		
		if( ChatMessageRepository.DAO.update("INSERT  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,SYNC_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE,TRANSPORT_STATE)  VALUES  "+StringUtils.rightPad("(?,?,?,?,?,?,?,?)",params.size()/8*",(?,?,?,?,?,?,?,?)".length()-1,",(?,?,?,?,?,?,?,?)"),params.toArray(new  Object[params.size()])) != params.size()/8 )
		{
			throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** CHAT  MESSAGE  REPOSITORY **  error  while  persisting  messages." );
		}
		routeGroups.parallelStream().forEach(  (routeGroup ) -> Optional.ofNullable(routeGroup.getOnPersistedListener()).ifPresent( (listener) -> listener.onPersisted()) );
		
		routeGroups.parallelStream().flatMap(  (routeGroup ) -> routeGroup.getRoutes().stream()).filter((route) -> route.isRoutable()).forEach( (route) -> route.getOnRoutedListener().onRouted(PAIPPacketRouter.INSTANCE.route(route.getUserId(),route.getPacket())) );
	}
}
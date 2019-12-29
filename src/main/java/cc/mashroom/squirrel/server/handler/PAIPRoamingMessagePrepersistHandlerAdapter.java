package cc.mashroom.squirrel.server.handler;

import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.server.storage.RoamingMessagePersistAndRouteEngine;
import  cc.mashroom.util.ObjectUtils;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelInboundHandlerAdapter;
import  lombok.AllArgsConstructor;

@AllArgsConstructor
public  class  PAIPRoamingMessagePrepersistHandlerAdapter   extends  ChannelInboundHandlerAdapter
{
	@Override
	public  void  channelRead(ChannelHandlerContext  context,Object  object )
	{
		if( object instanceof Route && (ObjectUtils.cast(object,Route.class).getPacket() instanceof ChatPacket || ObjectUtils.cast(object,Route.class).getPacket() instanceof GroupChatPacket) )
		{
			if( ObjectUtils.cast(object,Route.class).getPacket() instanceof ChatPacket )
			{
				persistEngine.prepersist( new  Route(ObjectUtils.cast(ObjectUtils.cast(object,Route.class).getPacket(),ChatPacket.class).getContactId(),ObjectUtils.cast(ObjectUtils.cast(object,Route.class).getPacket(),ChatPacket.class).clone().setSyncId(ObjectUtils.cast(ObjectUtils.cast(object,Route.class).getPacket(),ChatPacket.class).getContactId()).setContactSyncId(ObjectUtils.cast(ObjectUtils.cast(object,Route.class).getPacket(),ChatPacket.class).getSyncId())) );
			}
			
			persistEngine.prepersist( ObjectUtils.cast(object,Route.class) );
		}
	}
	
	private RoamingMessagePersistAndRouteEngine    persistEngine;
}

package cc.mashroom.squirrel.server.handler;

import  java.util.List;
import  java.util.stream.Collectors;

import  com.google.common.collect.Lists;

import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.xcache.util.SafeCacher;
import  io.netty.channel.ChannelHandlerContext;
import  io.netty.channel.ChannelHandler.Sharable;
import  io.netty.handler.codec.MessageToMessageDecoder;
import  lombok.AllArgsConstructor;

@Sharable
@AllArgsConstructor
public  class    PAIPPacketToRouteHandlerAdapter  extends  MessageToMessageDecoder<Packet <?>>
{
	@Override
	protected  void  decode(   ChannelHandlerContext  context, Packet<?>  packet ,List<Object>  objectList )
	{
		long  userId = context.channel().attr(ConnectPacket.USER_ID).get();
		
		if( packet instanceof GroupChatPacket  )
		{
			objectList.add( new  RouteGroup(packet,ChatGroupManager.INSTANCE.getChatGroupUserIds(ObjectUtils.cast(packet,GroupChatPacket.class).getGroupId()).parallelStream().map((chatGroupUserId) -> new  Route(chatGroupUserId != userId,chatGroupUserId,ObjectUtils.cast(packet,GroupChatPacket.class).clone().setSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_GROUP_MESSAGE.SYNC_ID("+chatGroupUserId+")",() -> ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{chatGroupUserId})).incrementAndGet()))).collect(Collectors.toList())).setOnPersistedListener(() -> context.channel().writeAndFlush(new  PendingAckPacket(0,packet.getId()))) );
		}
		else
		if( packet instanceof PingPacket )
		{
			context.channel().writeAndFlush( new  PingAckPacket(packet.getId()) );
		}
		else
		if( packet instanceof ChatRecallPacket )
		{
			objectList.add( new  Route(true,ObjectUtils.cast(packet,ChatRecallPacket.class).getContactId(),ObjectUtils.cast(packet,ChatRecallPacket.class).setContactId(userId)) );
		}
		else
		if( packet instanceof ChatPacket )
		{
			Route  route  = new  Route(true,ObjectUtils.cast(packet,ChatPacket.class).getContactId(),ObjectUtils.cast(packet,ChatPacket.class).clone().setContactId(userId).setSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_MESSAGE.SYNC_ID("+ObjectUtils.cast(packet,ChatPacket.class).getContactId()+")",() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{ObjectUtils.cast(packet,ChatPacket.class).getContactId()})).incrementAndGet()).setContactSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_MESSAGE.SYNC_ID("+userId+")",() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{userId})).incrementAndGet())).setOnRoutedListener( (success) -> {if( !success )  context.channel().writeAndFlush(new  PendingAckPacket(ObjectUtils.cast(packet,ChatPacket.class).getContactId(),packet.getId()));} );
			
			objectList.add( new  RouteGroup(packet,Lists.newArrayList(route,new  Route(false,userId,ObjectUtils.cast(packet,ChatPacket.class).clone().setSyncId(ObjectUtils.cast(route.getPacket(),ChatPacket.class).getContactSyncId()).setContactSyncId(ObjectUtils.cast(route.getPacket(),ChatPacket.class).getSyncId())))) );
		}
		else
		if( packet instanceof PendingAckPacket )
		{
			objectList.add( new  Route(true,ObjectUtils.cast(packet,PendingAckPacket.class).getContactId(),ObjectUtils.cast(packet,PendingAckPacket.class).setContactId(userId)) );
		}
		else
		{
			objectList.add( /* ORIGI */packet );
		}
	}
}

package cc.mashroom.squirrel.server.handler;

import  java.util.List;
import  java.util.stream.Collectors;

import  cc.mashroom.squirrel.module.chat.group.manager.ChatGroupManager;
import  cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.module.user.repository.ChatMessageRepository;
import  cc.mashroom.squirrel.paip.codec.PAIPDecoderHandlerAdapter;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatRecallPacket;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.squirrel.paip.message.connect.ConnectPacket;
import  cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingAckPacket;
import  cc.mashroom.squirrel.paip.message.connect.PingPacket;
import  cc.mashroom.squirrel.server.storage.MessageStorageEngine;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.xcache.util.SafeCacher;
import  io.netty.buffer.ByteBuf;
import  io.netty.channel.ChannelHandlerContext;
import  lombok.AllArgsConstructor;

@AllArgsConstructor
public  class  PAIPDecoderAndMessageRoutePrepersistHandlerAdapter      extends  PAIPDecoderHandlerAdapter
{
	protected  MessageStorageEngine  storageEng;
	
	@Override
	protected  void  decode(   ChannelHandlerContext  context,ByteBuf  byteBuf,List<Object>  objectList )
	{
		Packet<?>  packet = super.chain.decode( context.channel(),byteBuf.markReaderIndex().resetReaderIndex() );
		
		if( packet instanceof GroupChatPacket  )
		{
			objectList.addAll( ChatGroupManager.INSTANCE.getChatGroupUserIds(ObjectUtils.cast(packet,GroupChatPacket.class).getGroupId()).parallelStream().map((chatGroupUserId) -> this.storageEng.prepersist(new  Route(chatGroupUserId,ObjectUtils.cast(packet,GroupChatPacket.class).clone().setSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_GROUP_MESSAGE.SYNC_ID("+chatGroupUserId+")",() -> ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{chatGroupUserId})).incrementAndGet())))).collect(Collectors.toList()) );
		}
		else
		if( packet instanceof PingPacket )
		{
			context.channel().writeAndFlush( new  PingAckPacket(packet.getId()) );
		}
		else
		if( packet instanceof ChatRecallPacket )
		{
			objectList.add( new  Route(ObjectUtils.cast(packet,ChatRecallPacket.class).getContactId(),ObjectUtils.cast(packet,ChatRecallPacket.class).setContactId(context.channel().attr(ConnectPacket.USER_ID).get())) );
		}
		else
		if( packet instanceof ChatPacket )
		{
			Route<ChatPacket>  route = this.storageEng.prepersist( new  Route(context.channel().attr(ConnectPacket.USER_ID).get(),ObjectUtils.cast(packet,ChatPacket.class).clone().setSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_MESSAGE.SYNC_ID("+context.channel().attr(ConnectPacket.USER_ID).get()+")",() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{context.channel().attr(ConnectPacket.USER_ID).get()})).incrementAndGet())) );
			
			objectList.add( this.storageEng.prepersist(new  Route(ObjectUtils.cast(packet,ChatPacket.class).getContactId(),ObjectUtils.cast(packet,ChatPacket.class).clone().setContactId(context.channel().attr(ConnectPacket.USER_ID).get()).setSyncId(SafeCacher.createAndSetIfAbsent("SQUIRREL.CHAT_MESSAGE.SYNC_ID("+ObjectUtils.cast(packet,ChatPacket.class).getContactId()+")",() -> ChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  MAX(SYNC_ID)  AS  MAX_SYNC_ID  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  USER_ID = ?",new  Object[]{ObjectUtils.cast(packet,ChatPacket.class).getContactId()})).incrementAndGet()).setContactSyncId(route.getPacket().getSyncId()))) );
		}
		else
		if( packet instanceof PendingAckPacket )
		{
			objectList.add( new  Route(ObjectUtils.cast(packet,PendingAckPacket.class).getContactId(),ObjectUtils.cast(packet,PendingAckPacket.class).setContactId(context.channel().attr(ConnectPacket.USER_ID).get())) );
		}
		else
		{
			objectList.add( /* ORIGI */packet );
		}
	}
}

package cc.mashroom.squirrel.server.handler;

import  java.util.List;

import  cc.mashroom.squirrel.paip.message.Packet;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.experimental.Accessors;

@Data
@Accessors(  chain = true )
@AllArgsConstructor
public  class  RouteGroup<P  extends Packet<P>>
{
	private  P  originalPacket;
	private  List<Route<P>>  routes;
}

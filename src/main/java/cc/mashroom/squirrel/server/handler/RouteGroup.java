package cc.mashroom.squirrel.server.handler;

import  java.util.List;

import  cc.mashroom.squirrel.paip.message.Packet;
import  lombok.Data;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors(  chain = true )
@RequiredArgsConstructor
public  class  RouteGroup<P  extends Packet<P>>
{
	@NonNull
	private  P   originalPacket;
	@NonNull
	private  List<Route<P>>  routes;
	
	private  OnPersistedListener  onPersistedListener;
	
	public   RouteGroup<P >  addRoute(  Route  route )
	{
		this.routes.add(route );  return  this;
	}
	
	public   interface  OnPersistedListener{ public  void  onPersisted(); }
}

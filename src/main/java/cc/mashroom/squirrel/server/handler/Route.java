package cc.mashroom.squirrel.server.handler;

import  cc.mashroom.squirrel.paip.message.Packet;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.NonNull;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain  =true )
@AllArgsConstructor
public  class  Route<P extends Packet<P>>
{
	private  long  userId;
	@NonNull
	private  P  packet;
}

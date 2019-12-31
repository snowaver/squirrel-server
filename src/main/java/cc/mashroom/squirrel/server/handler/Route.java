package cc.mashroom.squirrel.server.handler;

import  cc.mashroom.squirrel.paip.message.Packet;
import lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Data;
import lombok.NoArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain  =true )
@NoArgsConstructor( access=AccessLevel.PRIVATE )
@AllArgsConstructor
public  class  Route<P extends Packet<P>>
{
	private  boolean  isRoutable;
	private  long  userId;
	private  P  packet;
}

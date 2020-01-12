package cc.mashroom.squirrel.server.handler;

import  cc.mashroom.squirrel.paip.message.Packet;
import  lombok.Data;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;
import  lombok.experimental.Accessors;

@Data
@Accessors( chain  =true )
@RequiredArgsConstructor
public  class  Route<P extends Packet<P>>
{
	@NonNull
	private  boolean  isRoutable;
	@NonNull
	private  long  userId;
	@NonNull
	private  P  packet;
	
	public   final  static  OnRoutedListener  NOOP_ON_ROUTED_LISTENER = new  OnRoutedListener(){ public  void  onRouted( boolean  success ){} };
	
	private  OnRoutedListener  onRoutedListener = NOOP_ON_ROUTED_LISTENER;
	
	public   interface  OnRoutedListener{ public  void  onRouted( boolean  success ); }
}

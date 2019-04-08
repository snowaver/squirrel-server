package cc.mashroom.squirrel.server.handler;

import  cc.mashroom.squirrel.paip.message.Packet;

public  interface  PAIPPacketExternalProcessor
{
	public  boolean  process(Packet  packet );
}

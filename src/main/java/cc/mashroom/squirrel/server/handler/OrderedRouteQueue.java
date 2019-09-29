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
package cc.mashroom.squirrel.server.handler;

import  java.util.concurrent.LinkedBlockingQueue;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.server.session.ClientSessionManager;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

public  class   OrderedRouteQueue      implements  PacketRouteListener
{
	@Override
	public  void  onRouteComplete( Packet  packet,boolean  isRouteSuccessfully )
	{
		OrderedRouteDispose  dispose = this.queue.peek();
		
		if( dispose.getPacket() !=packet )
		{
			throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** ORDERED  ROUTE  QUEUE **  the  complete  packet  should  be  equals  to  the  packet  in  the  peek  dispose." );
		}
		
		if( isRouteSuccessfully )
		{
			queue.poll();
			
			this.route();
		}
		else
		if( dispose.getCurrentRetryCount()<dispose.getMaxRetryCount())
		{
			dispose.setCurrentRetryCount(  dispose.getCurrentRetryCount() + 1 );
			
			this.route();
		}
		else
		{
			try
			{
				ClientSessionManager.INSTANCE.get(this.clientId).close( DisconnectAckPacket.REASON_UNKNOWN_ERROR );
			}
			catch( Exception  e )
			{
				
			}
		}
	}
	
	protected LinkedBlockingQueue   <OrderedRouteDispose>  queue = new  LinkedBlockingQueue<OrderedRouteDispose>();
	@NonNull
	protected Long      clientId;
	
	public  void  route()
	{
		OrderedRouteDispose  dispose = this.queue.peek();
		
		if( dispose == null || !PacketRoute.INSTANCE.route(this.clientId, dispose.getPacket(),dispose.getTimeout(), dispose.getTimeoutTimeUnit(),this) )
		{
			onRouteComplete( dispose.getPacket(),false );
		}
	}
	
	public  synchronized  void  addRouteDispose( OrderedRouteDispose   dispose )
	{
		if( queue.add(dispose)  )
		{
			if( this.queue.size()   == 1 )
			{
			this.route();
			}
			return;
		}
		
		throw  new  IllegalStateException( "SQUIRREL-SERVER:  ** ORDERED  ROUTE  QUEUE **  add  dispose  error." );
	}
}
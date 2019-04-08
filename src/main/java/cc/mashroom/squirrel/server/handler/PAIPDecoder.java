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

import  java.util.List;

import  io.netty.buffer.ByteBuf;
import  io.netty.channel.ChannelHandlerContext;

public  class  PAIPDecoder  extends  cc.mashroom.squirrel.paip.codec.PAIPDecoder
{
	public  PAIPDecoder()  throws  InstantiationException,IllegalAccessException,ClassNotFoundException
	{
		super();
	}

	protected  void  decode( ChannelHandlerContext  context,ByteBuf  byteBuf,List<Object>  objectList )  throws  Exception
	{
		super.decode( context,byteBuf,objectList );
	}
}
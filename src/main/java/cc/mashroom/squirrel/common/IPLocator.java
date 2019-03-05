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
package cc.mashroom.squirrel.common;

import  java.io.IOException;

import  com.github.jarod.qqwry.QQWry;

public  class  IPLocator
{
	public  static  String  locate( String  ip )
	{
		return   qqwry.findIP(ip).getMainInfo();
	}
	
	private  static  QQWry  qqwry;
	
	static
	{
		try
		{
			qqwry  = new  QQWry();
		}
		catch( IOException  e )
		{
			throw  new  IllegalStateException( e.getMessage(),e );
		}
	}
}
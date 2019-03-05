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

import  java.io.File;
import  java.io.IOException;
import  java.io.OutputStream;

import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;

import  org.slf4j.LoggerFactory;

import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.StringUtils;

public  abstract  class  AbstractController
{
	private  final  org.slf4j.Logger  logger = LoggerFactory.getLogger( AbstractController.class );
	
	protected  void  allowOriginAccessControl( HttpServletResponse  response )
	{
		response.setHeader( "Access-Control-Allow-Origin","*" );
		
		response.setHeader( "Access-Control-Allow-Methods",  "GET,POST,PUT,DELETE,PATCH,OPTIONS" );
	}
	
	protected  void  render( HttpServletResponse  response,File  file,String  fileName )  throws  IOException
	{
		try( OutputStream  os = response.getOutputStream() )
		{
			if( file.exists() )
			{
				response.setContentType(  "application/octet-stream" );
				
		        response.addHeader( "Content-Disposition","attachment;fileName="+(StringUtils.isBlank(fileName) ? file.getName() : fileName) );
				
		        os.write( FileUtils.readFileToByteArray(file) );
				
				os.flush();
			}
			else
			{
				response.setStatus( 404 );
			}
		}
	}
	
    protected  String  getRemoteAddress(  HttpServletRequest  request )
    {
    	String  ip = request.getHeader( "X-Forwarded-For" );
        
        if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
        {
        	if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
            {  
            	ip = request.getHeader( "Proxy-Client-IP" );
            }
            
            if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
            {  
            	ip = request.getHeader( "WL-Proxy-Client-IP" );
            }
            
            if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
            {
            	ip = request.getHeader( "HTTP_CLIENT_IP"  );
            }
            
            if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
            {
            	ip = request.getHeader( "HTTP_X_FORWARDED_FOR" );
            }
            
            if( StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) )
            {
            	ip = request.getRemoteAddr();
            }  
        }
        else
        if( ip.length() > 15 )
        {
        	for( String  child : ip.split(",") )
            {
            	if( !"unknown".equalsIgnoreCase(ip) )
            	{
            		return  child;
            	}
            }
        }
        
        return  request.getRemoteAddr();
    }
}
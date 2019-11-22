package cc.mashroom.squirrel.server.storage;

import  java.io.File;
import  java.io.FileNotFoundException;
import  java.io.FileOutputStream;
import  java.io.IOException;
import  java.io.OutputStream;
import  java.util.concurrent.locks.ReentrantLock;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.util.IOUtils;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  RoamingMessageRollingFile  implements       AutoCloseable
{
	private  ReentrantLock  relocker   =  new  ReentrantLock();
	@Accessors( chain=true )
	@Setter( value=AccessLevel.PRIVATE )
	@Getter
	private  File  file;
	@Accessors( chain=true )
	@Setter( value=AccessLevel.PRIVATE )
	private  OutputStream  outputStream;
	
	@SuppressWarnings(    "deprecation")
	@Override
	public  void     close()
	{
		try
		{
			relocker.lock();  IOUtils.closeQuietly( this.outputStream );
		}
		finally{this.relocker.unlock();}
	}
	public  File  append( byte[]  btar )  throws    IOException
	{
		try
		{
			relocker.lock();  this.outputStream.write(  btar );  this.outputStream.flush();  return  this.file;
		}
		finally{this.relocker.unlock();}
	}
	@SuppressWarnings(    "deprecation")
	public  void  rollover()      throws  FileNotFoundException
	{
		try
		{
			relocker.lock();  IOUtils.closeQuietly( this.outputStream );  this.setFile(new  File("./"+DateTime.now(DateTimeZone.UTC).toString("yyyyMMddHHmmssSSS"))).setOutputStream( new  FileOutputStream(this.file,true) );
		}
		finally{this.relocker.unlock();}
	}
}

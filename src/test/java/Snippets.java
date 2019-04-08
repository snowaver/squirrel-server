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
import  java.io.File;
import  java.io.IOException;
import  java.io.InputStream;
import  java.net.URI;
import  java.nio.file.FileVisitResult;
import  java.nio.file.FileVisitor;
import  java.nio.file.Files;
import  java.nio.file.attribute.BasicFileAttributes;
import  java.util.Collections;
import  java.util.List;

import  org.apache.commons.io.IOUtils;
import  org.apache.hadoop.conf.Configuration;
import  org.apache.hadoop.fs.FSDataInputStream;
import  org.apache.hadoop.fs.FileSystem;
import  org.apache.hadoop.fs.Path;

import  cc.mashroom.util.FileUtils;

public  class  Snippets
{
	public  static  void  main( String[]  args )  throws  IOException
	{
		/*
		new  File("d:/.gitkeep").createNewFile();
		*/
//		addCopyrightHeader();
		/*
		Configuration  configuration = new  Configuration();
		
		configuration.set( "fs.defaultFS" , "hdfs://hdns" );
		
		try( FileSystem  fs = FileSystem.get(URI.create("hdfs://172.27.0.3:9820"),configuration) )
		{
			try( FSDataInputStream  fsis = fs.open(new  Path("/programs/mashroom.txt")) )
			{
				System.err.println( IOUtils.toString(fsis,"UTF-8") );
			}
		}
		*/
		
		List<String>  values = FileUtils.readLines( new  File("d:/Workspace/Android/squirrel-peanut/app/src/main/res/values-en/strings.xml"),"UTF-8" );
		
		Collections.sort( values );
		
		values.forEach((value) -> System.err.println(value.trim()) );
	}
	
//	public  static  void  addCopyrightHeader()    throws  IOException
//	{
//		try( InputStream  inputs = Snippets.class.getResourceAsStream("/copyright.txt") )
//		{
//			String  copyrightHeader=IOUtils.toString(inputs,"UTF-8");
//			
//			Files.walkFileTree
//			(
//				new  File( "d:/workspace/squirrel-server" ).toPath(),
//				
//				new  FileVisitor<Path>()
//				{	
//					public  FileVisitResult  visitFile( Path  file,BasicFileAttributes  attrs )  throws  IOException
//					{
//						if(     file.toFile().getName().toLowerCase().endsWith(".java") )
//						{
//							String  javaFileContent = FileUtils.readFileToString( file.toFile(),"UTF-8" );
//							//  last  copyright  content  should  be  removed  before  adding  a  new  copyright  content,  then  replace  the  java  file  content.
//							FileUtils.writeStringToFile( file.toFile(),copyrightHeader+"\r\n"+(javaFileContent.startsWith("/*") ? javaFileContent.substring(javaFileContent.indexOf("*/")+2).trim() : javaFileContent.trim()),"UTF-8" );
//						}
//						
//						return  FileVisitResult.CONTINUE;
//					}
//	
//					public  FileVisitResult  visitFileFailed(     Path  file,IOException  exc )  throws  IOException
//					{
//						return  FileVisitResult.CONTINUE;
//					}
//	
//					public  FileVisitResult  postVisitDirectory(  Path  dir, IOException  exc )  throws  IOException
//					{
//						return  FileVisitResult.CONTINUE;
//					}
//					
//					public  FileVisitResult  preVisitDirectory(   Path  dir,BasicFileAttributes  attrs )  throws  IOException
//					{
//						return  FileVisitResult.CONTINUE;
//					}
//				}
//			);
//		}
//	}
}
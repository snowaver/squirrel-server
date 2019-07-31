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
import  java.nio.file.FileVisitResult;
import  java.nio.file.FileVisitor;
import  java.nio.file.Files;
import  java.nio.file.Path;
import  java.nio.file.attribute.BasicFileAttributes;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.concurrent.BackgroundInitializer;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.BackgroundPreinitializer;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor;
import org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.context.FileEncodingApplicationListener;
import org.springframework.boot.context.config.AnsiOutputApplicationListener;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.config.DelegatingApplicationContextInitializer;
import org.springframework.boot.context.config.DelegatingApplicationListener;
import org.springframework.boot.context.event.EventPublishingRunListener;
import org.springframework.boot.context.logging.ClasspathLoggingApplicationListener;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener;
import org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource.StubPropertySource;

import cc.mashroom.squirrel.module.system.controller.BalancingProxyController;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.IOUtils;

public  class  Snippets
{
	public  static  void  main( String[]  args )  throws  Exception
	{
		System.err.println( Lists.newArrayList(BalancingProxyController.class.getSuperclass().getDeclaredMethods()) );
		
		Class<?>  configurationclasspostprocessorClass    = ConfigurationClassPostProcessor.class;
		
		Class<?>  requestmappinghandleradapterClass = RequestMappingHandlerAdapter.class;
		
		Class<?>  beanfactoryClass= DefaultListableBeanFactory.class;
		
		Class<?>  dispatcherservletClass   = DispatcherServlet.class;
		
		Class<?>  eventpublishingrunListener = EventPublishingRunListener.class;
		
		List<Class<?>>  initializers = Lists.newArrayList(DelegatingApplicationContextInitializer.class, /*SharedMetadataReaderFactoryContextInitializer.class,*/ ContextIdApplicationContextInitializer.class, ConfigurationWarningsApplicationContextInitializer.class, ServerPortInfoApplicationContextInitializer.class, ConditionEvaluationReportLoggingListener.class);
		
		List<Class<?>>  applicationenvironmentpreparedeventListeners = Lists.newArrayList(ConfigFileApplicationListener.class,AnsiOutputApplicationListener.class,LoggingApplicationListener.class,ClasspathLoggingApplicationListener.class,LoggingApplicationListener.class,BackgroundPreinitializer.class,DelegatingApplicationListener.class,FileEncodingApplicationListener.class);
		
		List<Class<?>>  applicationcontextinitializedeventListeners  = Lists.newArrayList(BackgroundPreinitializer.class, DelegatingApplicationListener.class);
		
		List<Class<?>>  applicationpreparedeventlisteners = Lists.newArrayList(ConfigFileApplicationListener.class, LoggingApplicationListener.class, BackgroundPreinitializer.class, DelegatingApplicationListener.class);
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
	}
	
	public  static  void  addCopyrightHeader(  String  sourcefolder ) throws  IOException
	{
		try( InputStream  inputs = Snippets.class.getResourceAsStream("/copyright.txt") )
		{
			String  copyrightHeader=IOUtils.toString(inputs,"UTF-8");
			
			Files.walkFileTree
			(
				new  File(sourcefolder).toPath() , new  FileVisitor<Path>()
				{
					public  FileVisitResult  visitFile( Path  file,BasicFileAttributes  attrs )  throws  IOException
					{
						if(     file.toFile().getName().toLowerCase().endsWith(".java") )
						{
							String  javaFileContent = FileUtils.readFileToString( file.toFile(),"UTF-8" );
							//  last  copyright  content  should  be  removed  before  adding  a  new  copyright  content,  then  replace  the  java  file  content.
							FileUtils.writeStringToFile( file.toFile(),copyrightHeader+"\r\n"+(javaFileContent.startsWith("/*") ? javaFileContent.substring(javaFileContent.indexOf("*/")+2).trim() : javaFileContent.trim()),"UTF-8" );
						}
						
						return  FileVisitResult.CONTINUE;
					}
	
					public  FileVisitResult  visitFileFailed(     Path  file,IOException  exc )  throws  IOException
					{
						return  FileVisitResult.CONTINUE;
					}
	
					public  FileVisitResult  postVisitDirectory(  Path  dir, IOException  exc )  throws  IOException
					{
						return  FileVisitResult.CONTINUE;
					}
					
					public  FileVisitResult  preVisitDirectory(   Path  dir,BasicFileAttributes  attrs )  throws  IOException
					{
						return  FileVisitResult.CONTINUE;
					}
				}
			);
		}
	}
}
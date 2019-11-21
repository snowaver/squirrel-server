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
import java.io.FileOutputStream;
import  java.io.IOException;
import  java.io.InputStream;
import java.io.PrintStream;
import  java.nio.file.FileVisitResult;
import  java.nio.file.FileVisitor;
import  java.nio.file.Files;
import  java.nio.file.Path;
import  java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import  java.util.Deque;
import  java.util.LinkedList;
import  java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.math.RandomUtils;
import  org.apache.commons.lang3.concurrent.BackgroundInitializer;
import  org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import  org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import  org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import  org.apache.curator.shaded.com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import  org.springframework.beans.factory.support.DefaultListableBeanFactory;
import  org.springframework.boot.SpringApplication;
import  org.springframework.boot.autoconfigure.BackgroundPreinitializer;
import  org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import  org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor;
import  org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer;
import  org.springframework.boot.context.ContextIdApplicationContextInitializer;
import  org.springframework.boot.context.FileEncodingApplicationListener;
import  org.springframework.boot.context.config.AnsiOutputApplicationListener;
import  org.springframework.boot.context.config.ConfigFileApplicationListener;
import  org.springframework.boot.context.config.DelegatingApplicationContextInitializer;
import  org.springframework.boot.context.config.DelegatingApplicationListener;
import  org.springframework.boot.context.event.EventPublishingRunListener;
import  org.springframework.boot.context.logging.ClasspathLoggingApplicationListener;
import  org.springframework.boot.context.logging.LoggingApplicationListener;
import  org.springframework.boot.env.PropertiesPropertySourceLoader;
import  org.springframework.boot.env.RandomValuePropertySource;
import  org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor;
import  org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import  org.springframework.boot.env.YamlPropertySourceLoader;
import  org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener;
import  org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer;
import  org.springframework.context.annotation.ConfigurationClassPostProcessor;
import  org.springframework.core.env.SimpleCommandLinePropertySource;
import  org.springframework.web.servlet.DispatcherServlet;
import  org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import  org.springframework.core.env.ConfigurableEnvironment;
import  org.springframework.core.env.MapPropertySource;
import  org.springframework.core.env.PropertySource.StubPropertySource;
import org.springframework.util.StringUtils;

import cc.mashroom.plugin.db.Db;
import cc.mashroom.plugin.h2.H2CacheFactoryStrategy;
import cc.mashroom.plugin.ignite.IgniteCacheFactoryStrategy;
import cc.mashroom.squirrel.module.user.repository.ChatGroupMessageRepository;
import cc.mashroom.util.CollectionUtils;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.IOUtils;
import cc.mashroom.util.collection.map.Map;
import cc.mashroom.xcache.CacheFactory;
import cc.mashroom.xcache.XMemTableCache;

public  class  Snippets
{
	public  void  print() throws ClassNotFoundException
	{
		try {
			Class.forName("cn.com.abc.A");
		} catch (Exception e) {
			throw new RuntimeException(DateTime.now().toString(),e);
		}
	}
	
	public  static  void  main( String  []  args )  throws  Exception
	{
/*//		new  IgniteCacheFactoryStrategy().initialize( "/memory-policy.ddl" );
		ThreadPoolExecutor  pool = new  ThreadPoolExecutor( 4,4,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>() );
		int count = 100000;
		CountDownLatch  cdl = new  CountDownLatch( count );
		Timestamp  now = new  Timestamp( DateTime.now(DateTimeZone.UTC).getMillis() );
//		XMemTableCache  cache = CacheFactory.getOrCreateMemTableCache("CHAT_GROUP_MESSAGE_CACHE");
		String uuid = UUID.randomUUID().toString().toUpperCase().replace("-", "");
		new Db().initialize();
		AtomicLong recordCount = new AtomicLong();
		AtomicLong syncId = new AtomicLong();
		AtomicLong userId = new AtomicLong();
		List<Runnable> tasks = Lists.newLinkedList();
		for( int  i = 1;i <= count;i = i+1 )
		{
			int size = RandomUtils.nextInt(256)+1;
			recordCount.addAndGet(size);
			Object[]  params = new  Object[size*9];
			String[]  placeholders = new  String[ size ];
			for( int  j = 0;j < size;j = j+1 )
			{
				
				params[j*9+0] = now.getTime();
				params[j*9+1] = now.getTime();
				params[j*9+2] = syncId.incrementAndGet();
				params[j*9+3] = userId.incrementAndGet();
				params[j*9+4] = 2L;
				params[j*9+5] = uuid;
				params[j*9+6] = "";
				params[j*9+7] = 3;
				params[j*9+8] = 6;
				
				placeholders[j] = "(?,?,?,?,?,?,?,?,?)";
				
				
				params[j*6+0] = userId.incrementAndGet();
				params[j*6+1] = now;
				params[j*6+2] = now;
				params[j*6+3] = uuid;
				params[j*6+4] = uuid;
				params[j*6+5] = false;
				
				placeholders[j] = "(?,?,?,?,?,?)";
				
			}
//			String  sql = "INSERT  INTO  SESSION_LOCATION  (USER_ID,CREATE_TIME,ACCESS_TIME,SECRET_KEY,CLUSTER_NODE_ID,IS_ONLINE)  VALUES  "+cc.mashroom.util.StringUtils.join(placeholders,",");
			String  sql = "INSERT  INTO  chat_group_message  (ID,GROUP_ID,SYNC_ID,CONTACT_ID,USER_ID,MD5,CONTENT,CONTENT_TYPE,TRANSPORT_STATE)  VALUES  "+cc.mashroom.util.StringUtils.join(placeholders,",");
//			System.err.println(sql);
//			System.err.println(Lists.newArrayList(params));
			tasks.add(() -> {
				try {
					ChatGroupMessageRepository.DAO.insert(new LinkedList<>(),sql,params);
//					cache.update(sql,params);
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					cdl.countDown();
					long cdcount=cdl.getCount();
					if(cdcount%100000 == 0) {
						System.err.println(DateTime.now()+":\t"+cdcount);
					}
				}
			});
		}
		System.err.println("total record count: "+recordCount.get());
		DateTime startTime = DateTime.now();
		System.err.println( startTime );
		tasks.forEach((task) -> pool.submit(task));
		cdl.await();
		DateTime endTime = DateTime.now();
		System.err.println( endTime+" elapse: "+(endTime.getMillis()-startTime.getMillis())+"ms, "+((double) count*1000/(endTime.getMillis()-startTime.getMillis()))+" (batch/s), total records: "+recordCount );
		
		Class<?>  configurationclasspostprocessorClass    = ConfigurationClassPostProcessor.class;
		
		Class<?>  requestmappinghandleradapterClass = RequestMappingHandlerAdapter.class;
		
		Class<?>  beanfactoryClass= DefaultListableBeanFactory.class;
		
		Class<?>  dispatcherservletClass   = DispatcherServlet.class;
		
		Class<?>  eventpublishingrunListener = EventPublishingRunListener.class;
		
		List<Class<?>>  initializers = Lists.newArrayList(DelegatingApplicationContextInitializer.class, SharedMetadataReaderFactoryContextInitializer.class, ContextIdApplicationContextInitializer.class, ConfigurationWarningsApplicationContextInitializer.class, ServerPortInfoApplicationContextInitializer.class, ConditionEvaluationReportLoggingListener.class);
		
		List<Class<?>>  applicationenvironmentpreparedeventListeners = Lists.newArrayList(ConfigFileApplicationListener.class,AnsiOutputApplicationListener.class,LoggingApplicationListener.class,ClasspathLoggingApplicationListener.class,LoggingApplicationListener.class,BackgroundPreinitializer.class,DelegatingApplicationListener.class,FileEncodingApplicationListener.class);
		
		List<Class<?>>  applicationcontextinitializedeventListeners  = Lists.newArrayList(BackgroundPreinitializer.class, DelegatingApplicationListener.class);
		
		List<Class<?>>  applicationpreparedeventlisteners = Lists.newArrayList(ConfigFileApplicationListener.class, LoggingApplicationListener.class, BackgroundPreinitializer.class, DelegatingApplicationListener.class);
		*/
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
		
//        MemcachedClient  memcachedClient = new  XMemcachedClientBuilder(AddrUtil.getAddresses("127.0.0.1:8014")).build();
//        
//        memcachedClient.set( "a",0,"sljdfld" );
//        
//        memcachedClient.shutdown();
		
/*		MemCachedClient cachedClient = new MemCachedClient();  
        
        // 初始化SockIOPool，管理memcached的连接池  
        SockIOPool pool = SockIOPool.getInstance();  
  
        // 设置缓存服务器列表，当使用分布式缓存的时，可以指定多个缓存服务器。（这里应该设置为多个不同的服务器）  
        String[] servers = { "127.0.0.1:8014" 
        // 也可以使用域名 "server3.mydomain.com:1624"  
        };  
  
        pool.setServers(servers);  
        pool.setFailover(false);  
        pool.setInitConn(10); // 设置初始连接  
        pool.setMinConn(5);// 设置最小连接  
        pool.setMaxConn(250); // 设置最大连接  
        pool.setMaxIdle(1000 * 60 * 60 * 3); // 设置每个连接最大空闲时间3个小时  
        pool.setMaintSleep(30);  
        pool.setNagle(false);  
        pool.setSocketTO(3000);  
        pool.setAliveCheck(false);  
        pool.initialize(); 
        
        cachedClient.set("a", "sldfjdlj");
        Thread.sleep(60*10*1000);
        pool.shutDown();*/
		
//		JedisPoolConfig config = new JedisPoolConfig(); 
//        config.setMaxIdle(5); 
//        config.setTestOnBorrow(false); 
//        
//        JedisPool jedisPool = new JedisPool(config,"127.0.0.1",8014);
//        Jedis jedis = jedisPool.getResource();
//        System.err.println(jedis.set("name", "{\"name\":\"join\"}"));
//        jedisPool.close();
		
//		throw new IllegalStateException();
		try
		{
			new  Snippets().print();
		}
		catch( Exception  e )
		{
			try( FileOutputStream  os = new  FileOutputStream("d:/program/apache-tomcat-8.5.2902/logs/catalina.2019-11-18.log",true) )
			{
				e.printStackTrace( new  PrintStream(os) );
			}
		}
		
		String  s = "java.lang.RuntimeException: java.lang.ClassNotFoundException: cn.com.abc.A\r\n" + 
				"	at Snippets.print(Snippets.java:96)\r\n" + 
				"	at Snippets.main(Snippets.java:250)\r\n" + 
				"Caused by: java.lang.ClassNotFoundException: cn.com.abc.A\r\n" + 
				"	at java.net.URLClassLoader.findClass(URLClassLoader.java:381)\r\n" + 
				"	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)\r\n" + 
				"	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:338)\r\n" + 
				"	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)\r\n" + 
				"	at java.lang.Class.forName0(Native Method)\r\n" + 
				"	at java.lang.Class.forName(Class.java:264)\r\n" + 
				"	at Snippets.print(Snippets.java:94)\r\n" + 
				"	... 1 more\r\n";
		System.err.println("	...".matches("^(\\s+(at|\\.{3}))|^(Caused by:)"));
		System.err.println("	...".matches("^\\s\\.{3}\\b"));
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
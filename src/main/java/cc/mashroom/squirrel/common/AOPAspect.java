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

import  org.aspectj.lang.ProceedingJoinPoint;
import  org.aspectj.lang.annotation.Around;
import  org.aspectj.lang.annotation.Aspect;
import  org.aspectj.lang.annotation.Pointcut;
import  org.aspectj.lang.reflect.MethodSignature;
import  org.springframework.stereotype.Component;

import  cc.mashroom.db.annotation.Connection;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.util.ObjectUtils;

@Aspect
@Component
public  class  AOPAspect
{
	@Pointcut( "@annotation(cc.mashroom.db.annotation.Connection)" )
    public  void  transactionPointcut()
	{
		
    }
	
	@Around(  "transactionPointcut()" )
	public  Object  transactionAround( ProceedingJoinPoint  proceedingJoinPoint )  throws  Throwable
	{
		MethodSignature  signature = ObjectUtils.cast( proceedingJoinPoint.getSignature(),MethodSignature.class );
		
		Connection  connection = signature.getMethod().getAnnotation( Connection.class );
		
		return  Db.tx( connection.dataSource().name(),connection.transactionLevel(),(sqlConnection) -> proceedingJoinPoint.proceed() );
	}
}
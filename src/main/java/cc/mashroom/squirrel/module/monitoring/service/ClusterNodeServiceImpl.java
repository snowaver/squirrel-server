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
package cc.mashroom.squirrel.module.monitoring.service;

import  java.util.List;

import  org.springframework.http.ResponseEntity;
import  org.springframework.stereotype.Service;

import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.xcache.CacheFactory;
import  cc.mashroom.xcache.XClusterNode;

@Service
public  class  ClusterNodeServiceImpl  implements  ClusterNodeService
{
	public  ResponseEntity<String>  getAll()
	{
		Map<String,Object>  onlineCounts = new  HashMap<String, Object>();
		
		CacheFactory.getOrCreateMemTableCache("SESSION_LOCATION_CACHE").lookup(Map.class,"SELECT  CLUSTER_NODE_ID,COUNT(USER_ID)  AS  ONLINE_COUNT  FROM  SESSION_LOCATION  WHERE  ONLINE = ?  GROUP  BY  CLUSTER_NODE_ID",new  Object[]{true}).forEach( (onlineCount) -> onlineCounts.put(onlineCount.getString("CLUSTER_NODE_ID"),onlineCount.get("ONLINE_COUNT")) );
		
		List<XClusterNode>  clusterNodes = CacheFactory.getClusterNodes();
		
		clusterNodes.forEach( (clusterNode) -> clusterNode.getMetrics().addEntry("ONLINE_COUNT",onlineCounts.get(clusterNode.getId().toString())) );
		
		return  ResponseEntity.ok( JsonUtils.toJson(clusterNodes ) );
	}
}
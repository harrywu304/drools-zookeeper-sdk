/**
 * 
 */
package org.pub.droolszk.helper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.pub.droolszk.api.RuleService;
import org.pub.droolszk.api.RuleServiceFactory;
import org.pub.droolszk.api.Ruleset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhx 2017年9月28日 上午9:39:09
 * 基于ZooKeeper的规则辅助器
 */
public class ZkRuleHelper {  

	private static Logger logger = LoggerFactory.getLogger(ZkRuleHelper.class); 
	
	/**
	 * ZooKeeper host and port, eg. 127.0.0.1:2181
	 */
	private String connectString;
	
	/**
	 * Ruleset info context path
	 */
	private String contextPath;
	
	private CuratorFramework  zkclient = null ;  

	/**
	 * ZkRuleHelper构造方法
	 * @param connectString ZooKeeper连接字符串，如：127.0.0.1:2181
	 * @param contextPath 规则集节点所在路径的上下文，注意不要以/开头，如：demoapp/testruleset <br>
	 *                    testruleset为规则集名字，data为当前版本号，子节点为规则集所包含的规则文件
	 */
	public ZkRuleHelper(String connectString, String contextPath) {
		super();
		this.connectString = connectString;
		this.contextPath = contextPath;
		zkclient = getZkCient();
	}
	
	/**
	 * 根据规则集名称获取规则服务
	 * @param rulesetName 规则集名称
	 * @return 规范服务
	 */
	public RuleService getRuleService(String rulesetName){
		RuleService rt = null;
		//尝试根据rulesetName直接获取RuleService
		rt = RuleServiceFactory.getRuleService(rulesetName);
		if(rt == null) {
			//根据rulesetName获取ruleset信息
			Ruleset ruleset = getRulesetInfo(rulesetName);
			if(ruleset != null) {
				//创建ruleservice
				rt = RuleServiceFactory.createRuleService(ruleset);
				//监听Zookeeper中的ruleset节点
				try {
					watchRuleset(rulesetName);
				} catch (Exception e) {
					logger.error("watchRuleset fail just after created ruleservice", e);
				}
			}
		}

		return rt;
	}
	
	/**
	 * 获取Curator ZooKeeper Client
	 * @return CuratorFramework
	 */
	private CuratorFramework getZkCient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(6000)
                .connectionTimeoutMs(3000)
                .namespace(contextPath)
                .build();
        client.start();
        logger.info("zookeeper connected {} {}",connectString,contextPath);
        return client; 
	}   
	
	/**
	 * 从ZooKeeper获取规则集内容和Meta信息
	 * @param rulesetName 规则集名称
	 * @return
	 * @throws Exception
	 */
	private Ruleset getRulesetInfo(String rulesetName) {
		Ruleset rt = null;
		String rulesetPath = "/"+rulesetName;
		try {
	        //get ruleset version
	        byte[] versionBytes = zkclient.getData().forPath(rulesetPath);
	        String version = new String(versionBytes);
	        
	        //get rules file list
	        List<byte[]> rules = new ArrayList<byte[]>();
	        List<String> rulefiles = zkclient.getChildren().forPath(rulesetPath);
	        for(String rulefileName:rulefiles) {
	        		//get rule file content
	        		byte[] ruleBytes = zkclient.getData().forPath(rulesetPath+"/"+rulefileName);
	        		logger.debug("ruleset[{}] file[{}] content:{}",rulesetName,rulefileName, new String(ruleBytes));
	        		rules.add(ruleBytes);
	        }
	        
	        rt = new Ruleset(rulesetName, version, rules);
	        logger.info("got ruleset [{}|{}] info from zookeeper", rulesetName, version);
		}catch(Exception e) {
			logger.error("got ruleset from zookeeper throw exception", e);
		}
        return rt;
	}
	
	protected void addRuleFile(String rulesetName, String ruleFilename, byte[] ruleBytes) throws Exception {
		String ruleFilePath = "/"+rulesetName+"/"+ruleFilename;
		zkclient.create().forPath(ruleFilePath, ruleBytes);
	}
	
	protected void updateRuleFile(String rulesetName, String ruleFilename, byte[] ruleBytes) throws Exception {
		String ruleFilePath = "/"+rulesetName+"/"+ruleFilename;
		zkclient.setData().forPath(ruleFilePath, ruleBytes);
	}
	
	protected void deleteRuleFile(String rulesetName, String ruleFilename) throws Exception {
		String ruleFilePath = "/"+rulesetName+"/"+ruleFilename;
		zkclient.delete().forPath(ruleFilePath);
	}

	protected void createRuleset(String rulesetName, String version) throws Exception {
		String ruleFilePath = "/"+rulesetName;
		zkclient.create().forPath(ruleFilePath, version.getBytes());
	}
	
	/**
	 * 监听Ruleset Zookeeper节点，节点更新则动态刷新运行时RuleService
	 * @param rulesetName 规则集名称
	 * @throws Exception 运行出错
	 */
	protected void watchRuleset(String rulesetName) throws Exception {
		String rulesetPath = "/"+rulesetName;
        //monitor node data change
        final NodeCache nodeCache = new NodeCache(zkclient, rulesetPath);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
            		logger.info("ruleset [{}] trigged update, got new data:{}", rulesetName, new String(nodeCache.getCurrentData().getData()));
                //update ruleset at runtime
            		RuleService rs = getRuleService(rulesetName);
            		if(rs != null) {
            			logger.info("begin to get new ruleset info");
	            		Ruleset newSet = getRulesetInfo(rulesetName);
	            		logger.info("end got new ruleset info");
	            		if(newSet != null) {
	            			logger.info("begin to update ruleset {}", rulesetName);
	            			rs.updateRuleset(newSet);
	            			logger.info("end to update ruleset {}", newSet);
	            		}
            		}
            }
        });
	}
	

}

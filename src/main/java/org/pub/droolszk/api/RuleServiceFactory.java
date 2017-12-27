/**
 * 
 */
package org.pub.droolszk.api;

import org.pub.droolszk.impl.drools.DroolsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author wuhuoxin 2017年9月12日 下午3:51:13
 *
 * 规则服务工厂
 */
public class RuleServiceFactory { 
	
	private static Logger logger = LoggerFactory.getLogger(RuleServiceFactory.class); 
	
	/**
	 * rules map for singleton
	 */
	private static ConcurrentHashMap<String, RuleService> rsMap = new ConcurrentHashMap<String, RuleService>();
	
	/**
	 * 根据规则集合的名字获取规则服务的单例 <br>
	 * 注意：同名规则集只能创建一次
	 * @param ruleset 规则集信息
	 * @return 规则服务
	 * @exception RuntimeException 同名规则集服务已存在，创建失败
	 */
	public static RuleService createRuleService(Ruleset ruleset){
		String rulesetName = ruleset.getRulesetName();
		if(rsMap.get(rulesetName) != null){
			throw new RuntimeException("Fail!! RuleService " + rulesetName + " existed");
		}
		logger.info("begin createRuleService {} {}", rulesetName, ruleset.getVersion());
		synchronized(RuleServiceFactory.class){
			if(rsMap.get(rulesetName) == null){
				RuleService rs = new DroolsImpl(ruleset);
				rsMap.put(rulesetName, rs);
				logger.info("end createRuleService {} {}, success!!", rulesetName, ruleset.getVersion());
			}
		}
		return rsMap.get(rulesetName);
	}
	
	/**
	 * 获取已创建的RuleService
	 * @param rulesetName 规则集名称
	 * @return RuleService
	 */
	public static RuleService getRuleService(String rulesetName) {
		return rsMap.get(rulesetName);
	}
	

}

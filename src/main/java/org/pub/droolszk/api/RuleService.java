/**
 * 
 */
package org.pub.droolszk.api;

/**
 * @author wuhuoxin 2017年9月4日 上午11:53:55
 * 规则服务
 */
public interface RuleService {
	
	/**
	 * 传递Fact对象并执行规则，执行结果通过传入的Fact对象返回
	 * @param facts Fact对象列表
	 * @exception RuntimeException 规则集正在更新时禁止执行规则
	 */
	public void execute(Object[] facts);
	
	/**
	 * 获取运行时规则集合信息
	 * @return 运行时规则集合信息
	 */
	public Ruleset getRulesetInfo();
	
	/**
	 * 动态刷新规则集合
	 * @param newRuleset 新规则集合信息，rulesetName必须前后一致
	 * @exception RuntimeException 规则集名称不一致，更新失败
	 */
	public void updateRuleset(Ruleset newRuleset);

}

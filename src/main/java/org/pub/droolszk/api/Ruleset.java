/**
 * 
 */
package org.pub.droolszk.api;

import java.util.List;

/**
 * @author wuhx 2017年9月26日 下午5:24:05
 * 规则集信息
 */
public class Ruleset {
	
	/**
	 * 规则集名称
	 */
	private String rulesetName;
	
	/**
	 * 版本
	 */
	private String version;
	
	/**
	 * 规则文件内容列表
	 */
	private List<byte[]> rules;
	
	/**
	 * @param rulesetName
	 * @param version
	 * @param rules
	 */
	public Ruleset(String rulesetName, String version, List<byte[]> rules) {
		super();
		this.rulesetName = rulesetName;
		this.version = version;
		this.rules = rules;
	}

	/**
	 * @return the rulesetName
	 */
	public String getRulesetName() {
		return rulesetName;
	}

	/**
	 * @param rulesetName the rulesetName to set
	 */
	public void setRulesetName(String rulesetName) {
		this.rulesetName = rulesetName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the rules
	 */
	public List<byte[]> getRules() {
		return rules;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<byte[]> rules) {
		this.rules = rules;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Ruleset [rulesetName=" + rulesetName + ", version=" + version + "]";
	}

}

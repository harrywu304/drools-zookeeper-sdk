/**
 * 
 */
package org.pub.droolszk.helper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.pub.droolszk.api.RuleService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wuhx Oct 9, 2017 10:21:13 AM
 *
 */
public class ZkRuleHelperTest {
	
	private static ZkRuleHelper zrHelper = null;
	private static String rulesetName = "set001";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String connectString = "host73.dev:2181";
		String contextPath = "dzk/zktest";
		zrHelper = new ZkRuleHelper(connectString, contextPath);
		//zrHelper.createRuleset(rulesetName, "1.0");
		//addRuleFileNode();
	}


	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	//@Test
	public void testGetRuleService() throws Exception {


		RuleService rs = zrHelper.getRuleService(rulesetName);
		
		Map<String, Object> map = new HashMap();
		map.put("id", 456);
		map.put("name", "harry");
		rs.execute(new Object[] { map });
		
		//保持主线程不退出，这样在broker断开的情况下consumer就不会退出
        Object lock = new Object();
        synchronized (lock) {
            try {
				lock.wait();
			} catch (InterruptedException e) {
			}
        }
	}
	
	//@Test
	public void testWatchRuleset() throws Exception {
		RuleService rs = zrHelper.getRuleService(rulesetName);
		zrHelper.watchRuleset(rulesetName);
		
		//保持主线程不退出，这样在broker断开的情况下consumer就不会退出
        Object lock = new Object();
        synchronized (lock) {
            try {
				lock.wait();
			} catch (InterruptedException e) {
			}
        }
	}
	
	private static void addRuleFileNode() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("package rules.grading").append("\r\n")
		.append("import java.util.Map;").append("\r\n")
		.append("rule \"test rule\"").append("\r\n")
		.append("when").append("\r\n")
		.append("$map:Map()").append("\r\n")
		.append("then").append("\r\n")
		.append("System.out.println(\"map:new \" + $map.get(\"name\"));").append("\r\n")
		.append("end");
		
		String rulesetName = "set001";
		String ruleFilename = "rfile1";
		
		zrHelper.addRuleFile(rulesetName, ruleFilename, sb.toString().getBytes());
	}
	

}

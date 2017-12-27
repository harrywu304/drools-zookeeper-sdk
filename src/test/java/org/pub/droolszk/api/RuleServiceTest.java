package org.pub.droolszk.api;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for RuleServiceTest
 */
public class RuleServiceTest {
	
	private static RuleService abcRs;
	
	@BeforeClass
	public static void init() {
		Ruleset ruleset = new Ruleset("abc","1.0", getRules());
		abcRs = RuleServiceFactory.createRuleService(ruleset);		
	}

	@Test
	public void testExecute() {
		Map<String, Object> map = new HashMap();
		map.put("id", 456);
		map.put("name", "harry");
		abcRs.execute(new Object[] { map });
	}
	
	@Test
	public void testUpdateRuleset() throws InterruptedException {
		Map<String, Object> map = new HashMap();
		map.put("id", 456);
		map.put("name", "harry");
		abcRs.execute(new Object[] { map });
		
		Ruleset newRuleset = new Ruleset("abc","2.0", getNewRules());
		abcRs.updateRuleset(newRuleset);
		abcRs.execute(new Object[] { map });
	}
	
	/**
	 * 测试规则服务正在更新规则集合时执行Task的情况
	 */
	//@Test
	public void testExecuteWhenUpdating() {
		Map<String, Object> map = new HashMap();
		map.put("id", 456);
		map.put("name", "harry");
		
		Thread t1 = new Thread() {
			public void run() {
				System.out.println("begin execute");
				abcRs.execute(new Object[] { map });	
				System.out.println("end execute");
			}
		};
		Thread t2 = new Thread() {
			public void run() {
				Ruleset newRuleset = new Ruleset("abc","2.0", getNewRules());
				abcRs.updateRuleset(newRuleset);
			}
		};	
		t2.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t1.start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static List<byte[]> getRules(){
		StringBuilder sb = new StringBuilder();
		sb.append("package rules.grading").append("\r\n")
		.append("import java.util.Map;").append("\r\n")
		.append("rule \"test rule\"").append("\r\n")
		.append("when").append("\r\n")
		.append("$map:Map()").append("\r\n")
		.append("then").append("\r\n")
		.append("Thread.sleep(3000);").append("\r\n")
		.append("System.out.println(\"map:\" + $map.get(\"name\"));").append("\r\n")
		.append("end");
				
		List<byte[]> rt = new ArrayList<byte[]>();
		rt.add(sb.toString().getBytes());	
		return rt;
	}
	
	private static List<byte[]> getNewRules(){
		StringBuilder sb = new StringBuilder();
		sb.append("package rules.grading").append("\r\n")
		.append("import java.util.Map;").append("\r\n")
		.append("rule \"test rule\"").append("\r\n")
		.append("when").append("\r\n")
		.append("$map:Map()").append("\r\n")
		.append("then").append("\r\n")
		.append("System.out.println(\"map:new \" + $map.get(\"name\"));").append("\r\n")
		.append("end");
				
		List<byte[]> rt = new ArrayList<byte[]>();
		rt.add(sb.toString().getBytes());	
		return rt;
	}

}

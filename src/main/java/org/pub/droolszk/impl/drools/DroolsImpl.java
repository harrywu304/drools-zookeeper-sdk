/**
 * 
 */
package org.pub.droolszk.impl.drools;

import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.pub.droolszk.api.RuleService;
import org.pub.droolszk.api.Ruleset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author wuhuoxin 2017年9月4日 下午2:14:10
 * RuleService的Drools实现
 */
public class DroolsImpl implements RuleService {
	
	private static Logger logger = LoggerFactory.getLogger(DroolsImpl.class); 
	
	/**
	 * 规则集合名字
	 */
	private Ruleset ruleset;
	
	/**
	 * Kbase
	 */
	private InternalKnowledgeBase kbase;
	
	/**
	 * 无状态KieSession
	 */
	private StatelessKieSession statefulKSession;
	
	/**
	 * kbase是否正在升级的标识
	 */
	private boolean updating = false;
	
	/**
	 * 创建Drools RuleService对象
	 * @param ruleset 规则集信息
	 */
	public DroolsImpl(Ruleset ruleset) {
		super();
		this.ruleset = ruleset;
		//初始化对象
		init();
	}
	
	private void init(){
		kbase = getInternalKnowledgeBase();
		statefulKSession = getStatelessKieSession();
	}
	
	/**
	 * init InternalKnowledgeBase
	 * @return
	 */
	private InternalKnowledgeBase getInternalKnowledgeBase(){
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		//add rules resources to kbuilder
		List<byte[]> rules = ruleset.getRules();
		for(byte[] ruleFileContent:rules){
			Resource res = ResourceFactory.newByteArrayResource(ruleFileContent);
			kbuilder.add(res,ResourceType.DRL);
		}

		//create kbase
		KieBaseConfiguration kbConf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		//kbConf.setProperty("org.drools.sequential", "true");
		InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(kbConf);	
		
		//init kbase
		Collection<KiePackage> kpackage = kbuilder.getKnowledgePackages();
		kbase.addPackages(kpackage);
		logger.info("init InternalKnowledgeBase for {} {} success.", ruleset.getRulesetName(), ruleset.getVersion());
		return kbase;
	}
	
	/**
	 * initiate StatelessKieSession
	 * @return
	 */
	private StatelessKieSession getStatelessKieSession(){
		StatelessKieSession statefulKSession=kbase.newStatelessKieSession();
		logger.info("init StatelessKieSession for {} {}  success.", ruleset.getRulesetName(), ruleset.getVersion());
		return statefulKSession;
	}
	
	/* (non-Javadoc)
	 * @see RuleService#execute(java.lang.Object[])
	 */
	@Override
	public void execute(Object[] facts) {
		//check if kbase updating
		if(updating) {
			throw new RuntimeException("Execute forbidden!! Ruleset is updating, try again later");
		}
		statefulKSession.execute(Arrays.asList(facts));
	}

	@Override
	public synchronized void updateRuleset(Ruleset newRuleset){
		//check if rulesetName consistent
		if(!ruleset.getRulesetName().equals(newRuleset.getRulesetName())){
			throw new RuntimeException("Update forbidden!! Not the same Ruleset");
		}
		
		logger.info("begin updateRuleset {} {}", newRuleset.getRulesetName(), newRuleset.getVersion());
		//mark ruleservice locked, set updating true, then no new task can execute
		updating = true;
		
		Map<String, InternalKnowledgePackage> packages = kbase.getPackagesMap();
		Set<String> packageNameSet = new HashSet<String>();
		packageNameSet.addAll(packages.keySet());
		logger.debug("packageNameSet:"+packageNameSet);
		
		//clear package when no task running 
		for(String packageName:packageNameSet){
			kbase.removeKiePackage(packageName);
		}
		
		//build new packages by new rules
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		//add rules resources to kbuilder
		List<byte[]> rules = newRuleset.getRules();
		for(byte[] ruleFileContent:rules){
			Resource res = ResourceFactory.newByteArrayResource(ruleFileContent);
			kbuilder.add(res,ResourceType.DRL);
		}
		
		//add new packages to kbase
		Collection<KiePackage> newPkgs = kbuilder.getKnowledgePackages();	
		kbase.addPackages(newPkgs);
		
		//update ruleset info
		ruleset = newRuleset;
		//unlock ruleservice
		updating = false;
		logger.info("updateRuleset for {} {} success", ruleset.getRulesetName(), ruleset.getVersion());
	}

	/* (non-Javadoc)
	 * @see RuleService#getRulesetInfo()
	 */
	@Override
	public Ruleset getRulesetInfo() {
		return ruleset;
	}
	
}

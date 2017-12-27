# drools-zookeeper-sdk
drools integrated with zookeeper, provide rule central management and runtime rules dynamic refresh

## 关于drools-zookeeper-sdk
开发语言：纯Java \
特性：
- 封装了Drools规则引擎， 提供了简便的调用API；
- 支持不重启动态刷新规则引擎；
- 支持Zookeeper集成，规则在Zookeeper集中管理，规则集版本变更后主动触发客户端规则引擎动态刷新；
- 支持多规则集配置；


## 应用场景

- 场景1：单机环境，规则存储在数据库中，规则更新后，程序手动触发规则引擎动态刷新；
- 场景2: 集群环境，规则集成存储在Zookeeper，规则通过Zookeeper来集中管理，规则集版本变更后主动触发客户端集群的规则引擎动态刷新；

## Quick start

- 场景1直接使用RuleService即可，不依赖于Zookeeper， 具体调用可参考RuleServiceTest的代码；
- 场景2需要ZkRuleHelper，具体调用可参考ZkRuleHelperTest；

## 联系我
- 微信: harrywu304
- Email: harrywu304@qq.com
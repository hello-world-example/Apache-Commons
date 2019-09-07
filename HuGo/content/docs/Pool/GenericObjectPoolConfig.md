---
bookToc: false
---



# GenericObjectPoolConfig



| 属性                           | 类型    | 默认值                                               | 作用                                                         |
| ------------------------------ | ------- | ---------------------------------------------------- | ------------------------------------------------------------ |
| maxTotal                       | int     | 8                                                    | 池中最多可用的实例个数                                       |
| maxIdle                        | int     | 8                                                    | 池中最多可容纳的实例（instances）个数                        |
| minIdle                        | int     | 0                                                    | 池中最少需要容纳的实例（instances）个数                      |
| lifo                           | boolean | TRUE                                                 | 池中实例的操作是否按照LIFO（后进先出）的原则                 |
| fairness                       | boolean | FALSE                                                | 租借池化对象的客户端按照FIFO进行                             |
| maxWaitMillis                  | long    | -1                                                   | 调用borrowObject方法时，需要等待的最长时间                   |
| minEvictableIdleTimeMillis     | long    | 1800000                                              | 池中对象处于空闲状态开始到被回收的最短时间                   |
| softMinEvictableIdleTimeMillis | long    | 3                                                    | 池中对象处于空闲状态开始到被回收的最短时间                   |
| numTestsPerEvictionRun         | int     | 3                                                    | 池中处于空闲状态的对象每次被检测是否回收时 最多只检测3个处于空闲状态的对象,比如该值设置为3,此时池中有5个闲置对象,那么每次只会检查前三个闲置对象 |
| evictionPolicyClassName        | String  | org.apache.commons.pool2. impl.DefaultEvictionPolicy | 回收策略                                                     |
| testOnCreate                   | boolean | FALSE                                                | 调用borrowObject方法时，依据此标识判断是否 需要对返回的结果进行校验，如果校验失败会删 除当前实例，并尝试再次获取 |
| testOnBorrow                   | boolean | FALSE                                                | 调用borrowObject方法时，依据此标识判断是否 需要对返回的结果进行校验，如果校验失败会 删除当前实例，并尝试再次获取 |
| testOnReturn                   | boolean | FALSE                                                | 调用returnObject方法时，依据此标识判断是否 需要对返回的结果进行校验 |
| testWhileIdle                  | boolean | FALSE                                                | 闲置实例校验标识，如果校验失败会删除当前实例                 |
| timeBetweenEvictionRunsMillis  | long    | -1                                                   | 闲置实例校验器启动的时间间隔，单位是毫秒                     |
| blockWhenExhausted             | boolean | TRUE                                                 | 当池中对象都被借出后，客户端来租借对象， 此时是否进行阻塞还是非阻塞，默认阻塞 |
| jmxEnabled                     | boolean | TRUE                                                 | 开启JMX开关                                                  |
| jmxNamePrefix                  | String  | pool                                                 | JMX前缀                                                      |
| jmxNameBase                    | String  | null                                                 | JMX根名字                                                    |

 
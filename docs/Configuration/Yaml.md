# 操作 YAML 文件



## 新增依赖

```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>1.24</version>
</dependency>
```



## my.yaml

```yaml
spring:
  application:
    name: spring-${person.name}

person:
  name: Kail
  age: 27
```





## 代码示例

```java
URL myYaml = ClassLoader.getSystemResource("my.yaml");

Configurations configurations = new Configurations();

YAMLConfiguration configuration = configurations.fileBased(YAMLConfiguration.class, myYaml);

String string = configuration.getString("spring.application.name");
```


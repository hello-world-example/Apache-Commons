# 快速开始指南



## 读取 properties 文件

`database.properties` 是一个配置文件的示例：

```properties
database.host = db.acme.com
database.port = 8199
database.user = admin
database.password = ???
database.timeout = 60000

zk.addresses = host1,host2,host3
```

读取这个文件最简单的方式是通过 `Configurations` 帮助类。 这个类提供了一系列从不同源创建配置对象的方法。读取 properties 代码如下：

```java
Configurations configs = new Configurations();

URL databaseRes = ClassLoader.getSystemResource("database.properties");

Configuration config = configs.properties(databaseRes);

String dbHost = config.getString("database.host");

int dbPort = config.getInt("database.port");

List<String> addresses = config.getList(String.class, "zk.addresses");

Iterator<String> database = config.getKeys("database");
```



## 读取 XML 文件

`paths.xml` 文件内容如下:

```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>
<configuration>
  <processing stage="qa">
    <paths>
      <path>/data/path1</path>
      <path>/data/otherpath</path>
      <path>/var/log</path>
    </paths>
  </processing>
</configuration>
```

读取 xml 配置文件的方式与 `properties` 文件类似：

```java
Configurations configs = new Configurations();

URL pathsRes = ClassLoader.getSystemResource("paths.xml");

XMLConfiguration xmlConfiguration = configs.xml(pathsRes);

// qa
String stage = xmlConfiguration.getString("processing[@stage]");

// size 3
List<String> paths = xmlConfiguration.getList(String.class, "processing.paths.path");

// /data/otherpath
String secondPath = xmlConfiguration.getString("processing.paths.path(1)");
```



> [Hierarchical Configurations](http://commons.apache.org/proper/commons-configuration/userguide/howto_hierarchical.html)



## 更新配置

```java
Configurations configs = new Configurations();

// ..../target/classes/database.properties
URL databaseRes = ClassLoader.getSystemResource("database.properties");

FileBasedConfigurationBuilder<PropertiesConfiguration> configurationBuilder = configs.propertiesBuilder(databaseRes);

PropertiesConfiguration config = configurationBuilder.getConfiguration();

config.setProperty("database.password", 1723);

config.addProperty("zk.addresses", "new-zk2");

configurationBuilder.save();
```



> [Configuration Builders](http://commons.apache.org/proper/commons-configuration/userguide/howto_builders.html#Configuration_Builders)
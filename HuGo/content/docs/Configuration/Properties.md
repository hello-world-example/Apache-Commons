# Properties 配置



## 属性引用

```properties
database.host = db.acme.com
database.port = 8199

database.address = ${database.host}:${database.port}
```

使用方式一样

```java
// db.acme.com:8199
properties.getString("database.address")
```



## 包含其他配置文件

**database.properties**

```properties
database.timeout = 60000

include = database_db1.properties
include = database_db2.properties
```

**database_db1.properties**

```properties
database.db1.host = db.acme.com1
database.db1.port = 81991
database.db1.user = admin1
database.db1.password = ???1
```

**database_db2.properties**

```properties
database.db2.host = db.acme.com2
database.db2.port = 81992
database.db2.user = admin2
database.db2.password = ???2
```



```java
// db.acme.com1
properties.getString("database.db1.host");

// db.acme.com2
properties.getString("database.db2.host");
```



## 特殊编码

```properties
key = \u4e2d\u6587\u5b57\u7b26

config.dirs = C:\\Temp\\,D:\\data\\
```

```java
// 中文字符
properties.getString("key");

// [C:\Temp\,D:\data\]
properties.getList(String.class, "config.dirs")
```

> [Unicode编码转换](https://tool.chinaz.com/tools/unicode.aspx)



## Read More

- [Properties files](http://commons.apache.org/proper/commons-configuration/userguide/howto_properties.html)
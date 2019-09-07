---
bookToc: false
---

# Runtime Dependencies



| Component                                                   | Dependencies                                |
| :---------------------------------------------------------- | :------------------------------------------ |
| Core                                                        | commons-lang <br />commons-logging          |
| Configuration builders                                      | **commons-beanutils**                       |
| PropertyListConfiguration<br />XMLPropertyListConfiguration | commons-codec                               |
| JSONConfiguration                                           | com.fasterxml.jackson.core:jackson-databind |
| **YAMLConfiguration**                                       | org.yaml:snakeyaml                          |
| ConfigurationDynaBean                                       | commons-beanutils                           |
| XPathExpressionEngine                                       | commons-jxpath                              |
| CatalogResolver                                             | xml-resolver                                |
| Web configurations                                          | servlet-api                                 |
| ExprLookup                                                  | commons-jexl                                |
| VFSFileSystem<br />VFSFileChangedReloadingStrategy          | commons-vfs                                 |
| ConfigPropertySource                                        | spring-core                                 |



## Maven 依赖

### Core

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-configuration2</artifactId>
    <version>2.5</version>
</dependency>

<dependency>
    <groupId>commons-beanutils</groupId>
    <artifactId>commons-beanutils</artifactId>
    <version>1.9.3</version>
</dependency>
```

### Yaml

```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>1.24</version>
</dependency>
```


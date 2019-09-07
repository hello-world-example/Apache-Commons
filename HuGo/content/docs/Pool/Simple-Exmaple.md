# 入门示例

## A Simple Pool Client

假设您正在编写  `java.io.Reader`  工具包，并提供将  `Reader` 转换成 `String` 的方法。以下是在没有使用 `ObjectPool` 的情况下实现的  `ReaderUtil` 的代码：

```java
import java.io.Reader; 
import java.io.IOException; 
 
public class ReaderUtil { 
  
    public ReaderUtil() { 
    } 
 
    public String readToString(Reader in) throws IOException { 
        StringBuffer buf = new StringBuffer(); 
        try { 
            for(int c = in.read(); c != -1; c = in.read()) { 
                buf.append((char)c); 
            } 
            return buf.toString(); 
        } finally { 
            try { 
                in.close(); 
            } catch(Exception e) { 
                // ignored 
            } 
        } 
    } 
}
```

以下是使用 Pool 的代码示例

```java
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.pool2.ObjectPool;

public class ReaderUtil {
    
    private ObjectPool<StringBuffer> pool;
    
    public ReaderUtil(ObjectPool<StringBuffer> pool) {
        this.pool = pool;
    }

    public String readToString(Reader in) throws IOException {
        StringBuffer buf = null;
        try {
            // ❤❤❤ 从 Pool 中获取一个对象实例 ❤❤❤
            buf = pool.borrowObject();
          
            for (int c = in.read(); c != -1; c = in.read()) {
                buf.append((char) c);
            }
            return buf.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to borrow buffer from pool" + e.toString());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // ignored
            }
            try {
                if (null != buf) {
                  	// ❤❤❤ 归还对象到 Pool 中 ❤❤❤
                    pool.returnObject(buf);
                }
            } catch (Exception e) {
                // ignored
            }
        }
    }
}
```



## PooledObjectFactory

实现 `PoolableObjectFactory` 的最简单的方式是继承 [`BasePooledObjectFactory`](http://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/BasePooledObjectFactory.html).

```java
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class StringBufferFactory extends BasePooledObjectFactory<StringBuffer> {

    // BasePooledObjectFactory 抽象方法
    @Override
    public StringBuffer create() {
        return new StringBuffer();
    }

    // BasePooledObjectFactory 抽象方法
    @Override
    public PooledObject<StringBuffer> wrap(StringBuffer buffer) {
        return new DefaultPooledObject<StringBuffer>(buffer);
    }

    /**
     * 当对象被归还到 pool 变为不活跃时，清除 buffer
     */
    @Override
    public void passivateObject(PooledObject<StringBuffer> pooledObject) {
        pooledObject.getObject().setLength(0);
    }

    // 对于其他方法，BasePooledObjectFactory 中的默认空的实现就足够了
}
```

We can, for example, use this factory with the `GenericObjectPool` to instantiate our `ReaderUtil` as follows:

```java
ReaderUtil readerUtil = new ReaderUtil(new GenericObjectPool<StringBuffer>(new StringBufferFactory()));
```



## Read More

- [官方 入门示例](http://commons.apache.org/proper/commons-pool/examples.html)


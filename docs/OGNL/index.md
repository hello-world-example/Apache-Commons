# Ognl

OGNL 是 `Object-Graph Navigation Language` 的缩写，它是一种功能强大的表达式语言（Expression Language，简称为 `EL`），通过它简单一致的表达式语法，可以存取对象的任意属性，调用对象的方法，遍历整个对象的结构图，实现字段类型转化等功能

## Root

> - `Object Ognl.getValue(String expression, Object root)`
> - `setValue(String expression, Object root, Object value)`

| 表达式                                                     | 描述                        |      |
| ---------------------------------------------------------- | --------------------------- | ---- |
| `"person.name"` = `"#root.person.name"`                    | 获取对象属性值              |      |
| `"username + ',' + sex"`                                   | 同时获取多个值并用 逗号分割 |      |
| `Ognl.getValue("length()", "12345")`                       | 访问对象方法                |      |
| `Ognl.getValue(Ognl.parseExpression("length()"), "12345")` | 访问对象方法                |      |
| `Ognl.getValue("substring(0,1)", "12345")`                 | 访问对象方法                |      |

## Context

> - `Object Ognl.getValue(String expression, Map context, Object root)`
> - `setValue(String expression, Map context, Object root, Object value)`
>
> 访问 Context 表达式前面需要加上 `#`，root 也属于 Context，使用 `#root` 访问，也可以省略
```java
Map<String, Person> context = new HashMap<String, Person>();
context.put("p1", p1);
context.put("p2", p2);
```

| 表达式                                             | 描述                      |      |
| -------------------------------------------------- | ------------------------- | ---- |
| `Ognl.getValue("#p1.name", context, new Object())` | 获取 Context 中的对象属性 |      |

## 赋值

| 表达式                                | 描述 |      |
| ------------------------------------- | ---- | ---- |
| `"#u.username = '暗之幻影'"`          |      |      |
| `"#u.username = '风',#p.name = '云'"` |      |      |

## 静态方法

| 表达式                         | 描述         |      |
| ------------------------------ | ------------ | ---- |
| `"@System@out.println(#this)"` | 打印对象本身 |      |





| 表达式 | 描述 |      |
| ------ | ---- | ---- |
|        |      |      |





| 表达式 | 描述 |      |
| ------ | ---- | ---- |
|        |      |      |





| 表达式 | 描述 |      |
| ------ | ---- | ---- |
|        |      |      |



## 注意事项

### MemberAccess implementation must be provided!

`ognl:ognl:3.2.x` 后，以下代码会报错，`MemberAccess` 的实现需要手动设置

```java
Ognl.getValue("length()", "12345")
```

> [MemberAccess implementation must be provided! --Ognl3.2.6的异常解决方案](https://blog.csdn.net/LX928525166/article/details/82699572)



## Read More

- [OGNL设计及使用不当造成的远程代码执行漏洞](https://wooyun.js.org/drops/OGNL设计及使用不当造成的远程代码执行漏洞.html))
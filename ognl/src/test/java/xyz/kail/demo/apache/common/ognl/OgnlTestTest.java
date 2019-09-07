package xyz.kail.demo.apache.common.ognl;

import ognl.Ognl;
import org.junit.Test;

import java.util.*;

public class OgnlTestTest {


    // ***************** root对象的概念 ******************* //
    @Test
    public void testOgnl_01() throws Exception {
        User user = new User();
        user.setUsername("暗之幻影");
        user.setSex("男");
        //相当于调用user.getUsername()方法
        String value = (String) Ognl.getValue("username + ',' + sex", user);
        System.out.println(value);
    }

    @Test
    public void testOgnl_02() throws Exception {
        User user = new User();
        Person p = new Person();
        p.setName("暗之幻影");
        user.setPerson(p);

        //相当于调用user.getPerson().getName()方法
        String value = (String) Ognl.getValue("person.name", user);
        System.out.println(value);
    }

    @Test
    public void testOgnl_03() throws Exception {
        User user = new User();
        Person p = new Person();
        p.setName("暗之幻影");
        user.setPerson(p);

        //可以使用#root来引用根对象，相当于调用user.getPerson().getName()方法
        String value = (String) Ognl.getValue("#root.person.name", user);
        System.out.println(value);
    }

    // *********************** context的概念 **********************//

    @Test
    public void testOgnl_04() throws Exception {
        Person p1 = new Person();
        Person p2 = new Person();
        p1.setName("风");
        p2.setName("云");

        Map<String, Person> context = new HashMap<String, Person>();
        context.put("p1", p1);
        context.put("p2", p2);

        // 需要加上#才表示到context中取数据，否则表示从根目录下取值
        String value = (String) Ognl.getValue("#p1.name + ',' + #p2.name", context, new Object());
        System.out.println(value);
    }

    @Test
    public void testOgnl_05() throws Exception {
        Person p1 = new Person();
        Person p2 = new Person();
        p1.setName("风");
        p2.setName("云");

        Map<String, Person> context = new HashMap<String, Person>();
        context.put("p1", p1);
        context.put("p2", p2);

        User root = new User();
        root.setUsername("雨");

        String value = (String) Ognl.getValue("#p1.name + ',' + #p2.name + ',' + username", context, root);
        System.out.println(value);
    }

    // ******************* OGNL赋值操作 ************************//

    @Test
    public void testOgnl_06() throws Exception {
        User user = new User();

        //给root对象的属性赋值，相当于调用user.setUsername()
        Ognl.setValue("username", user, "暗之幻影");

        System.out.println(user.getUsername());
    }

    @Test
    public void testOgnl_07() throws Exception {
        User user = new User();

        Map<String, User> context = new HashMap<String, User>();
        context.put("u", user);

        //给context中的对象属性赋值，相当于调用user.setUsername()
        Ognl.setValue("#u.username", context, new Object(), "暗之幻影");

        System.out.println(user.getUsername());
    }

    @Test
    public void testOgnl_08() throws Exception {
        User user = new User();

        Map<String, User> context = new HashMap<String, User>();
        context.put("u", user);

        //给context中的对象属性赋值，相当于调用user.setUsername()
        //在表达式中使用=赋值操作符来赋值
        Ognl.getValue("#u.username = '暗之幻影'", context, new Object());

        System.out.println(user.getUsername());
    }

    @Test
    public void testOgnl_09() throws Exception {
        User user = new User();
        Person p = new Person();

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("u", user);
        context.put("p", p);

        //给context中的对象属性赋值，相当于调用user.setUsername()
        //在表达式中使用=赋值操作符来赋值
        Ognl.getValue("#u.username = '风',#p.name = '云'", context, new Object());

        System.out.println(user.getUsername() + "," + p.getName());
    }


    //****************** 使用OGNL调用对象的方法 **********************//

    @Test
    public void testOgnl_10() throws Exception {
        User user = new User();
        user.setUsername("暗之幻影");

        String value = (String) Ognl.getValue("getUsername()", user);
        System.out.println(value);
    }

    @Test
    public void testOgnl_11() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        System.out.println(user.getUsername());
    }

    // ******************* OGNL调用类变量、静态方法 *************************//

    @Test
    public void testOgnl_12() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        Ognl.getValue("@System@out.println(username)", user);
    }

    @Test
    public void testOgnl_13() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        Ognl.getValue("@System@out.println(@org.darkness.struts2.test.Utils@toLowerCase(username))", user);
    }

    // ********************* OGNL中的this表达式 **********************//

    @Test
    public void testOgnl_14() throws Exception {
        Object root = new Object();
        Map context = new HashMap();

        List values = new ArrayList();
        for (int i = 0; i < 11; i++) {
            values.add(i);
        }
        context.put("values", values);

        Ognl.getValue("@System@out.println(#values.size.(#this > 10 ? \"大于10\" : '不大于10'))", context, root);

        System.out.println(Ognl.getValue("#this.size().(#this > 2 ? \"大于2\" : '不大于2')", Arrays.asList(1, 2, 3)));
        System.out.println(Ognl.getValue("size().(#this > 2 ? \"大于2\" : '不大于2')", Arrays.asList(1, 2, 3)));

    }

    @Test
    public void testOgnl_15() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('testOgnl_15')", user);
        Ognl.getValue("@System@out.println(#this.username)", user);
        Ognl.getValue("@System@out.println(username)", user);

        System.out.println(Ognl.getValue("#this.username", user));
        System.out.println(Ognl.getValue("username", user));
    }

    @Test
    public void testOgnl_16() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('AbCd')", user);
        Ognl.getValue("@System@out.println(username.(#this.toLowerCase()))", user);
        Ognl.getValue("@System@out.println(username.toLowerCase())", user);

        System.out.println(Ognl.getValue("username.(#this.toLowerCase())", user));
        System.out.println(Ognl.getValue("username.toLowerCase()", user));
    }

    // ******************* 如何把表达式的解释结果作为另外一个表达式,OGNL中括号的使用 **********************//

    @Test
    public void testOgnl_17() throws Exception {
        Object root = new Object();
        Map context = new HashMap();
        User u = new User();
        u.setUsername("暗之幻影");
        context.put("u", u);
        context.put("fact", "username");

        /**
         * 1、首先把#fact表达式进行解释，得到一个值：username
         * 2、解释括号中的表达式#u，其结果是user对象
         * 3、把括号中表达式的结果作为root对象，解释在第一步中得到的结果（即把第一步的结果看成另外一个表达式）
         */
        String value = (String) Ognl.getValue("#fact(#u)", context, root);
        System.out.println(value);
    }

    @Test
    public void testOgnl_18() throws Exception {
        Person person = new Person();
        Map context = new HashMap();
        User u = new User();
        u.setUsername("暗之幻影");
        context.put("u", u);

        /**
         * 相当于调用person这个根对象的fact方法，并把#u的解释结果作为参数传入此方法
         */
        String value = (String) Ognl.getValue("fact(#u)", context, person);
        System.out.println(value); //输出是 "暗之幻影,"
    }

    @Test
    public void testOgnl_19() throws Exception {
        Person person = new Person();
        Map context = new HashMap();
        User u = new User();
        u.setUsername("暗之幻影");
        context.put("u", u);

        /**
         * 1、先计算表达式fact，得到结果是：username
         * 2、解释括号中的表达式#u，其结果是user对象
         * 3、把括号中表达式的结果作为root对象，解释在第一步中得到的结果（即把第一步的结果看成另外一个表达式）
         */
        String value = (String) Ognl.getValue("(fact)(#u)", context, person);
        System.out.println(value); //输出"暗之幻影"
    }

    // ********************* OGNL访问集合元素 **************************//

    @Test
    public void testOgnl_20() throws Exception {
        Object root = new Object();
        Map context = new HashMap();

        //用OGNL创建List对象
        List listvalue = (List) Ognl.getValue("{'item1', 'item2', 'item3'}", new Object());
        System.out.println(listvalue);
        context.put("listvalue", listvalue);

        //用OGNL创建数组
        int[] intarray = (int[]) Ognl.getValue("new int[]{23,45,67}", root);
        context.put("intarray", intarray);

        //用OGNL创建Map对象
        Map mapvalue = (Map) Ognl.getValue("#{'key1':#listvalue,'key2':#intarray}", context, root);
        System.out.println(mapvalue);
        context.put("mapvalue", mapvalue);

        Ognl.getValue("@System@out.println(#listvalue[0])", context, root);
        Ognl.getValue("@System@out.println(#intarray[1])", context, root);
        Ognl.getValue("@System@out.println(#mapvalue['key1'][2])", context, root);
        Ognl.getValue("@System@out.println(#mapvalue.key1[0])", context, root);
        Ognl.getValue("@System@out.println(#mapvalue.key1[0])", context, root);
    }
}

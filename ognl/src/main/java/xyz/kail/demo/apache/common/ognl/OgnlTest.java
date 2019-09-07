package xyz.kail.demo.apache.common.ognl;

import ognl.Ognl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OgnlTest {

    public static void main(String[] args) throws Exception {
//        new OgnlTest().testOgnl_08();
        System.out.println(Ognl.getValue("@String.format('%s + %s = %s', 1, 1, 2)", new Object()));


    }


    // ******************* OGNL调用类变量、静态方法 *************************//
    public void testOgnl_12() throws Exception {
        User user = new User();

        Ognl.getValue("@System@out.println(username)", user);
    }

    public void testOgnl_13() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        Ognl.getValue("@System@out.println(@org.darkness.struts2.test.Utils@toLowerCase(username))", user);
    }

    // ********************* OGNL中的this表达式 **********************//     
    public void testOgnl_14() throws Exception {
        Object root = new Object();
        Map context = new HashMap();

        List values = new ArrayList();
        for (int i = 0; i < 11; i++) {
            values.add(i);
        }
        context.put("values", values);

        Ognl.getValue("@System@out.println(#values.size.(#this > 10 ? \"大于10\" : '不大于10'))", context, root);

    }

    public void testOgnl_15() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        Ognl.getValue("@System@out.println(#this.username)", user);
    }

    public void testOgnl_16() throws Exception {
        User user = new User();

        Ognl.getValue("setUsername('暗之幻影')", user);
        Ognl.getValue("@System@out.println(username.(#this.toLowerCase()))", user);
    }

    // ******************* 如何把表达式的解释结果作为另外一个表达式,OGNL中括号的使用 **********************//     
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
    public void testOgnl_20() throws Exception {
        Object root = new Object();
        Map context = new HashMap();

        //用OGNL创建List对象     
        List listvalue = (List) Ognl.getValue("{123,'暗之幻影','风之水影'}", context, root);
        context.put("listvalue", listvalue);

        //用OGNL创建数组     
        int[] intarray = (int[]) Ognl.getValue("new int[]{23,45,67}", context, root);
        context.put("intarray", intarray);

        //用OGNL创建Map对象     
        Map mapvalue = (Map) Ognl.getValue("#{'listvalue':#listvalue,'intarray':#intarray}", context, root);
        context.put("mapvalue", mapvalue);

        Ognl.getValue("@System@out.println(#listvalue[0])", context, root);
        Ognl.getValue("@System@out.println(#intarray[1])", context, root);
        Ognl.getValue("@System@out.println(#mapvalue['intarray'][2])", context, root);
        Ognl.getValue("@System@out.println(#mapvalue.intarray[0])", context, root);
    }
}  

package com.leggasai.rpc.client.proxy.javassist;


import com.leggasai.rpc.client.proxy.invoke.MethodInvoke;
import javassist.*;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JavassistProxyGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);

    private static ConcurrentHashMap<Class<?>, Object> proxyInstanceCache = new ConcurrentHashMap<>();

    public static Object newProxyInstance(ClassLoader classLoader, Class<?> targetClass, MethodInvoke invoker)
            throws Exception {

        if(proxyInstanceCache.containsKey(targetClass)){
            return proxyInstanceCache.get(targetClass);
        }

        ClassPool pool = ClassPool.getDefault();

        //生成代理类的全限定名
        String qualifiedName = generateClassName(targetClass);
        // 创建代理类
        CtClass proxy = pool.makeClass(qualifiedName);

        //接口方法列表
        CtField mf = CtField.make("public static java.lang.reflect.Method[] methods;", proxy);
        proxy.addField(mf);

        CtField hf = CtField.make("private " + MethodInvoke.class.getName() + " handler;", proxy);
        proxy.addField(hf);

        CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get(MethodInvoke.class.getName())}, proxy);
        constructor.setBody("this.handler=$1;");
        constructor.setModifiers(Modifier.PUBLIC);
        proxy.addConstructor(constructor);

        proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
        Set<Class<?>> interfaces1 = new LinkedHashSet<>();
        if (targetClass.isInterface()){
            interfaces1.add(targetClass);
        }
        Class<?> originalClazz = targetClass;
        while (targetClass != null) {
            Collections.addAll(interfaces1, targetClass.getInterfaces());
            targetClass = targetClass.getSuperclass();
        }

        List<Class<?>> interfaces = new ArrayList<>(interfaces1);
        // 获取被代理类的所有接口


        List<Method> methods = new ArrayList<>();
        for (Class cls : interfaces) {
            CtClass ctClass = pool.get(cls.getName());
            proxy.addInterface(ctClass);

            Method[] arr = cls.getDeclaredMethods();
            for (Method method : arr) {
                int ix = methods.size();
                Class<?> rt = method.getReturnType();
                Class<?>[] pts = method.getParameterTypes();

                StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                for(int j=0;j<pts.length;j++) {
                    code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
                }
                code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);");
                if(!Void.TYPE.equals(rt) )
                    code.append(" return ").append(asArgument(rt, "ret")).append(";");

                StringBuilder sb = new StringBuilder(1024);
                sb.append(modifier(method.getModifiers())).append(' ').append(getParameterType(rt)).append(' ').append(method.getName());
                sb.append('(');
                for(int i=0;i<pts.length;i++)
                {
                    if( i > 0 )
                        sb.append(',');
                    sb.append(getParameterType(pts[i]));
                    sb.append(" arg").append(i);
                }
                sb.append(')');

                Class<?>[] ets = method.getExceptionTypes();    //方法抛出异常
                if( ets != null && ets.length > 0 )
                {
                    sb.append(" throws ");
                    for(int i=0;i<ets.length;i++)
                    {
                        if( i > 0 )
                            sb.append(',');
                        sb.append(getParameterType(ets[i]));
                    }
                }
                sb.append('{').append(code.toString()).append('}');

                CtMethod ctMethod = CtMethod.make(sb.toString(), proxy);
                proxy.addMethod(ctMethod);

                methods.add(method);
            }
        }

        proxy.setModifiers(Modifier.PUBLIC);

        Class<?> proxyClass = proxy.toClass(classLoader, null);
        proxyClass.getField("methods").set(null, methods.toArray(new Method[0]));

        Object instance = proxyClass.getConstructor(MethodInvoke.class).newInstance(invoker);

        Object old = proxyInstanceCache.putIfAbsent(originalClazz, instance);

        if(old!=null){
            instance = old;
        }
        return instance;
    }

    private static String modifier(int mod) {
        if( Modifier.isPublic(mod) ) return "public";
        if( Modifier.isProtected(mod) ) return "protected";
        if( Modifier.isPrivate(mod) ) return "private";
        return "";
    }

    /**
     * 数组类型返回 String[]
     * @param c
     * @return
     */
    public static String getParameterType(Class<?> c) {
        if(c.isArray()) {   //数组类型
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            } while( c.isArray() );

            return c.getName() + sb.toString();
        }
        return c.getName();
    }

    private static String asArgument(Class<?> cl, String name) {
        if( cl.isPrimitive() ) {
            if( Boolean.TYPE == cl )
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            if( Byte.TYPE == cl )
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            if( Character.TYPE == cl )
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            if( Double.TYPE == cl )
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            if( Float.TYPE == cl )
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            if( Integer.TYPE == cl )
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            if( Long.TYPE == cl )
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            if( Short.TYPE == cl )
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            throw new RuntimeException(name+" is unknown primitive type.");
        }
        return "(" + getParameterType(cl) + ")"+name;
    }

    private static String generateClassName(Class<?> type){
        return String.format("%s$Proxy%d", type.getName(), counter.getAndIncrement());
    }
}

package com.stduy.lib_core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ButterKniffer {

    private static HashMap<Class, Object> binderList = new HashMap<>();

    public static <T> void bindView(T host) {
        String canonicalName = host.getClass().getCanonicalName();
        try {
            Class<?> aClass = Class.forName(canonicalName + "$ViewBinder");
            Object o = aClass.newInstance();
            Method bind = aClass.getDeclaredMethod("bind", host.getClass());
            bind.invoke(o, host);
            binderList.put(host.getClass(), o);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void unbind(Object host) {
        Object o = binderList.remove(host.getClass());
        try {
            Method unbind = o.getClass().getDeclaredMethod("unbind", host.getClass());
            unbind.invoke(o, host);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
package io.github.splotycode.mosaik.runtime.startup;

import io.github.splotycode.mosaik.util.ExceptionUtil;

import java.lang.reflect.InvocationTargetException;

public class StartUpInvoke {

    private static Class clazz;

    static {
        try {
            clazz = Class.forName("io.github.splotycode.mosaik.startup.Main");
        } catch (ClassNotFoundException e) {
            ExceptionUtil.throwRuntime(e);
        }
    }

    public static void inokeIfNotInitialised() {
        try {
            clazz.getMethod("mainIfNotInitialised").invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ExceptionUtil.throwRuntime(e);
        }
    }

    public static void inokeIfNotInitialised(String... args) {
        try {
            clazz.getMethod("mainIfNotInitialised", String[].class).invoke(null, new Object[] {args});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ExceptionUtil.throwRuntime(e);
        }
    }

    public static void invokeTestSuite() {
        inokeIfNotInitialised("-mosaik.appname", "tests", "-debug:log_file");
    }
}

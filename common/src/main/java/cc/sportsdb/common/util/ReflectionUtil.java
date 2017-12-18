package cc.sportsdb.common.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public abstract class ReflectionUtil {

    private ReflectionUtil() {

    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object instance, String name) {
        Object[] objects = new Object[1];
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            field.setAccessible(true);
            objects[0] = field.get(instance);
        }, (field) -> field.getName().equals(name));
        return (T) objects[0];
    }



    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object[] args) {
        Method method = ReflectionUtils.findMethod(instance.getClass(), methodName, paramTypes);
        method.setAccessible(true);
        return (T) ReflectionUtils.invokeMethod(method, instance, args);
    }
}

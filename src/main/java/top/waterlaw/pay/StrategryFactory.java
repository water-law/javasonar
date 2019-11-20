package top.waterlaw.pay;

import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Set;

public class StrategryFactory {
    //- 饿汉模式
    private static StrategryFactory strategryFactory = new StrategryFactory();
    // String "XXX.class"
    public static HashMap<Integer, String> source_map = new HashMap<Integer, String>();

    static {
        // 反射扫描出包下所有的类
        Reflections reflections = new Reflections("top.waterlaw.pay.impl");
        // 取出带有 Pay 注解的类
        Set<Class<?>> classList = reflections.getTypesAnnotatedWith(Pay.class);
        for (Class clazz: classList) {
            Pay t = (Pay) clazz.getAnnotation(Pay.class);
            source_map.put(t.value(), clazz.getCanonicalName());
        }
    }

    private StrategryFactory() {}

    public Stratygy create(int type) throws Exception {
        String clazz = source_map.get(type);
        Class<?> clazz_ = Class.forName(clazz);
        return (Stratygy)clazz_.newInstance();
    }

    public static StrategryFactory getInstance() {
        return strategryFactory;
    }
}

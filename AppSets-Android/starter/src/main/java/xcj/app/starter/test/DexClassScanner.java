package xcj.app.starter.test;

import static xcj.app.starter.android.util.PurpleLoggerKt.PurpleLogger;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

public class DexClassScanner {

    public static String TAG = "DexClassScanner";

    private static BaseDexClassLoader mClassLoader;
    private static SoftReference<Set<String>> mAllClassNamesBuffer;

    public static @Nullable List<Class<?>> collectClassByAnnotation(
            Context context,
            String packageName,
            Class<? extends Annotation> annotation) {
        DexClassScanner.setClassLoader(context.getApplicationContext());
        List<Class<?>> allClassByAnnotation = DexClassScanner.getAllClassByAnnotation(annotation, packageName);
        DexClassScanner.setClassLoader((Context) null);
        return allClassByAnnotation;
    }

    public static <C> @Nullable List<Class<C>> collectClassByInterface(
            Context context,
            String packageName,
            Class<C> interfaceclass) {
        DexClassScanner.setClassLoader(context.getApplicationContext());
        List<Class<C>> allClassByInterface = DexClassScanner.getAllClassByInterface(interfaceclass, packageName);
        DexClassScanner.setClassLoader((Context) null);
        return allClassByInterface;
    }

    /**
     * 默认情况下有可能ClassLoader获取失败
     * 调用该函数确保ClassLoader能获取到
     */
    public static void setClassLoader(Context context) {
        if (context == null) {
            mClassLoader = null;
            return;
        }
        if (mClassLoader == null) {
            setDexClassLoader((BaseDexClassLoader) context.getClassLoader());
        }
    }

    public static void setDexClassLoader(BaseDexClassLoader classLoader) {
        if (classLoader == null) {
            mClassLoader = null;
            return;
        }
        mClassLoader = classLoader;
    }

    /**
     * @return 返回与接口同包名的所有实现类
     */
    public static <C> @Nullable List<Class<C>> getAllClassByInterface(Class<C> interfaceClass) {
        Package pkg = interfaceClass.getPackage();
        String pkgName = pkg != null ? pkg.getName() : "";
        return getAllClassByInterface(interfaceClass, pkgName);
    }

    /**
     * @return 返回与接口同包名的所有实现类
     */
    public static @Nullable List<Class<?>> getAllClassByAnnotation(Class<? extends Annotation> annotationClass, String packageName) {
        if (!annotationClass.isAnnotation()) {
            PurpleLogger.getCurrent().e(TAG, "annotationClass must be a Annotation!", null, null);
            return null;
        }
        List<Class<?>> returnClassList = new ArrayList<>();
        List<Class<?>> allClass = getClasses(packageName);
        for (Class<?> c : allClass) {
            if (c == null || c == annotationClass) {
                continue;
            }
            Annotation annotation = c.getDeclaredAnnotation(annotationClass);
            if (annotation != null) {
                returnClassList.add(c);
            }
        }
        return returnClassList;
    }

    /**
     * @param packageName 指定返回的类的包名前缀,设为空则不限制
     * @return 返回这个interfaceClass的所有实现类及子接口, 不包括interfaceClass本身
     */

    public static <C> @Nullable List<Class<C>> getAllClassByInterface(Class<C> interfaceClass, String packageName) {
        if (!interfaceClass.isInterface()) {
            PurpleLogger.getCurrent().e(TAG, "interfaceClass must be a Interface!", null, null);
            return null;
        }
        List<Class<C>> returnClassList = new ArrayList<>();
        List<Class<?>> allClass = getClasses(packageName);
        //排除本身
        allClass.remove(interfaceClass);

        for (Class<?> c : allClass) {
            if (interfaceClass.isAssignableFrom(c)) {
                returnClassList.add((Class<C>) c);
            }
        }
        return returnClassList;
    }

    /**
     * @param packageName 指定返回的包名前缀,设为空则不限制
     * @return 返回以包名前缀与参数相同的类
     */
    public static List<Class<?>> getClasses(String packageName) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        Set<String> allClassesName = getAllClassesName(packageName);
        for (String str : allClassesName) {
            if (str.isEmpty() || !str.startsWith(packageName)) {
                continue;
            }
            try {
                Class<?> aClass = getClass(str);
                if (aClass == null) {
                    continue;
                }
                classes.add(aClass);
            } catch (ClassNotFoundException e) {
                PurpleLogger.getCurrent().d(TAG, "getClasses, exception:" + e.getMessage(), null, null);
            }
        }
        return classes;
    }

    /**
     * @param packageName 指定返回的包名前缀,设为空则不限制
     * @return 返回以包名前缀与参数packageName相同的类的全地址(包名 + 类名)
     */
    private static Set<String> getAllClassesName(String packageName) {
        if (mAllClassNamesBuffer != null) {
            Set<String> ref = mAllClassNamesBuffer.get();
            if (ref != null) {
                return ref;
            }
        }
        Set<String> classNames = new LinkedHashSet<>();
        try {
            Field f_pathList = getField("pathList", BaseDexClassLoader.class);
            Field f_dexElements = getField("dexElements", getClass("dalvik.system.DexPathList"));
            Field f_dexFile = getField("dexFile", getClass("dalvik.system.DexPathList$Element"));

            Object pathList = getObjectFromField(f_pathList, mClassLoader);
            Object[] list = (Object[]) getObjectFromField(f_dexElements, pathList);

            if (list == null) {
                return classNames;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //DexFile在API26版本被弃用
                for (Object o : list) {
                    DexFile d = (DexFile) getObjectFromField(f_dexFile, o);
                    if (d != null) {
                        Enumeration<String> enumeration = d.entries();
                        while (enumeration.hasMoreElements()) {
                            String className = enumeration.nextElement();
                            if (packageName.isEmpty() || className.startsWith(packageName)) {
                                classNames.add(className);
                            }
                        }
                    }
                }

            } else {
                Class<?> c_DexFile = getClass("dalvik.system.DexFile");
                Field f_mCookie = getField("mCookie", c_DexFile);
                Method m_getClassNameList = getMethod("getClassNameList", c_DexFile, Object.class);

                try {
                    if (m_getClassNameList == null) {
                        return classNames;
                    }
                    for (Object o : list) {
                        Object o_DexFile = getObjectFromField(f_dexFile, o);
                        Object o_mCookie = getObjectFromField(f_mCookie, o_DexFile);
                        if (o_mCookie == null) {
                            continue;
                        }
                        String[] classList = (String[]) m_getClassNameList.invoke(o_DexFile, o_mCookie);
                        if (classList != null) {
                            Collections.addAll(classNames, classList);
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    PurpleLogger.getCurrent().d(TAG, "getAllClassesName, exception:" + e.getMessage(), null, null);
                }
            }
        } catch (ClassNotFoundException e) {
            PurpleLogger.getCurrent().d(TAG, "getAllClassesName, exception:" + e.getMessage(), null, null);
        }
        mAllClassNamesBuffer = new SoftReference<>(classNames);
        return classNames;
    }

    public static Field getField(String field, Class<?> Class) {
        try {
            return Class.getDeclaredField(field);
        } catch (Throwable e) {
            PurpleLogger.getCurrent().d(TAG, "getField, exception:" + e.getMessage(), null, null);
        }
        return null;
    }

    public static Method getMethod(String method, Class<?> Class, Class<?>... parameterTypes) {
        try {
            Method res = Class.getDeclaredMethod(method, parameterTypes);
            res.setAccessible(true);
            return res;
        } catch (Throwable e) {
            PurpleLogger.getCurrent().d(TAG, "getMethod, exception:" + e.getMessage(), null, null);
        }
        return null;
    }

    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return mClassLoader.loadClass(className);
    }

    public static Object getObjectFromField(Field field, Object arg) {
        try {
            field.setAccessible(true);
            return field.get(arg);
        } catch (Throwable e) {
            PurpleLogger.getCurrent().d(TAG, "getObjectFromField, exception:" + e.getMessage(), null, null);
        }
        return null;
    }
}

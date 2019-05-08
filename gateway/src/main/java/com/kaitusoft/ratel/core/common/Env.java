package com.kaitusoft.ratel.core.common;

import com.kaitusoft.ratel.core.handler.extend.DemoAuth;
import com.kaitusoft.ratel.core.handler.extend.DemoPostHandler;
import com.kaitusoft.ratel.core.handler.extend.DemoPreHandler;
import com.kaitusoft.ratel.core.model.ExtendInstance;
import com.kaitusoft.ratel.core.model.po.Group;
import com.kaitusoft.ratel.handler.AbstractAuthProcessor;
import com.kaitusoft.ratel.handler.AbstractPostHandler;
import com.kaitusoft.ratel.handler.AbstractPreHandler;
import com.kaitusoft.ratel.util.ResourceUtil;
import com.kaitusoft.ratel.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/31
 *          <p>
 *          write description here
 */

public class Env {

    private static final Logger logger = LoggerFactory.getLogger(Env.class);

    public static Set<Group> groups = new HashSet<>();

    public static Set<ExtendInstance> auths = new HashSet<>();

    public static Set<ExtendInstance> preHandlers = new HashSet<>();

    public static Set<ExtendInstance> postHandlers = new HashSet<>();


    static {
        AbstractAuthProcessor defaultAuth = new DemoAuth();
        ExtendInstance auth = new ExtendInstance();
        auth.setInstance(defaultAuth.getClass().getCanonicalName());
        auth.setUsage(defaultAuth.usage());
        auths.add(auth);

        AbstractPreHandler demoPreHandler = new DemoPreHandler();
        ExtendInstance pre = new ExtendInstance();
        pre.setInstance(demoPreHandler.getClass().getCanonicalName());
        pre.setUsage(demoPreHandler.usage());
        preHandlers.add(pre);

        AbstractPostHandler demoPostHandler = new DemoPostHandler();
        ExtendInstance post = new ExtendInstance();
        post.setInstance(demoPostHandler.getClass().getCanonicalName());
        post.setUsage(demoPostHandler.usage());
        postHandlers.add(post);

    }

    public static void loadCustomInstance() throws Exception {

        Collection<Class<?>> allClasses = getAllExtendClass();
        logger.debug("got {} classes", allClasses.size());
        loadExtendInstance(allClasses);
    }

    private static Collection<Class<?>> getAllExtendClass() throws Exception {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        String homeDir = System.getProperty("app.home");
        if(StringUtils.isEmpty(homeDir))
            homeDir = current.getResource("").getPath();
        File home = new File(homeDir);
        String extendDir = homeDir + File.separator + "ext";
        File extend = new File(extendDir);
        if(!extend.exists() || !extend.isDirectory()){
            extend = home;
        }

        if (!home.exists())
            return new ArrayList<>(0);

        logger.info("find extend instance in dir:{}", extend.getPath());

        Set<File> files = ResourceUtil.findFile(extend, ".jar", true);
        logger.debug("got {} jars", files.size());

        ClassLoader myClassLoader = createClassLoader(files, current);
//        if (myClassLoader != null)

        Collection<Class<?>> allClasses = ResourceUtil.getClassesFromJars(files, myClassLoader);

        Set<File> classFiles = ResourceUtil.findFile(home, ".class", true);
        for(File classFile : classFiles){
            try {
                allClasses.add(ResourceUtil.loadFileAsClass(myClassLoader, classFile.getPath(), extend));
            } catch (Throwable throwable) {
                logger.warn("load class error:{}", classFile.getPath(), throwable);
            }
        }

        return allClasses;
    }

    private static Collection<Class<?>> getAllLoadedClasses() throws NoSuchFieldException, IllegalAccessException {
        Field f = ClassLoader.class.getDeclaredField("classes");
        f.setAccessible(true);
        Vector classes=(Vector)f.get(Thread.currentThread().getContextClassLoader());
        return classes;
    }

    private static void loadExtendInstance(Collection<Class<?>> allClasses) {
        List<Class<?>> extendAuths = ResourceUtil.getSubClasses(AbstractAuthProcessor.class, allClasses);
        extendAuths.forEach((clazz) -> {
            boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
            if (isAbstract)
                return;
            try {
                AbstractAuthProcessor handler = (AbstractAuthProcessor) clazz.newInstance();
                ExtendInstance extend = new ExtendInstance();
                extend.setInstance(handler.getClass().getCanonicalName());
                extend.setUsage(handler.usage());
                auths.add(extend);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        List<Class<?>> extendPreHandlers = ResourceUtil.getSubClasses(AbstractPreHandler.class, allClasses);
        extendPreHandlers.forEach((clazz) -> {
            boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
            if (isAbstract)
                return;
            try {
                AbstractPreHandler handler = (AbstractPreHandler) clazz.newInstance();
                ExtendInstance extend = new ExtendInstance();
                extend.setInstance(handler.getClass().getCanonicalName());
                extend.setUsage(handler.usage());
                preHandlers.add(extend);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        List<Class<?>> extendPostHandlers = ResourceUtil.getSubClasses(AbstractPostHandler.class, allClasses);
        extendPostHandlers.forEach((clazz) -> {
            boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
            if (isAbstract)
                return;
            try {
                AbstractPostHandler handler = (AbstractPostHandler) clazz.newInstance();
                ExtendInstance extend = new ExtendInstance();
                extend.setInstance(handler.getClass().getCanonicalName());
                extend.setUsage(handler.usage());
                postHandlers.add(extend);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }


//    private static ClassLoader configExtendClassLoader() {
//        ClassLoader current = Thread.currentThread().getContextClassLoader();
//        String homeDir = current.getResource("").getPath();
//        File home = new File(homeDir);
//        home = home.getParentFile();
//        if (!home.exists())
//            return null;
//
//        Set<File> files = ResourceUtil.findFile(home, ".jar", true);
//        logger.debug("got {} jar files in dir {} and its' children dirs", files.size(), home);
//        ClassLoader extendClassLoader = createClassLoader(files, current);
//        return extendClassLoader;
//    }

    private static ClassLoader createClassLoader(Set<File> files, ClassLoader parent) {
        Set<URL> set = new HashSet<>();

        files.forEach((file) -> {
            try {
                set.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        final URL[] array = set.toArray(new URL[set.size()]);
        ;
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            if (parent == null)
                return new URLClassLoader(array);
            else
                return new URLClassLoader(array, parent);
        });
    }

//    private static URL buildClassLoaderUrl(File file) throws MalformedURLException {
//        String path = "";
//        String filePath = file.getPath();
//        String fileName = file.getName().toLowerCase();
//        logger.debug("file : {}" ,filePath);
//        if(fileName.endsWith(".jar")) {
////            path = "file://" + filePath + "!/";
//            return file.toURI().toURL();
//        } else if(fileName.endsWith(".class")) {
////            path = "class://" + filePath;
//            return file.toURI().toURL();
//        }
//        URL classUrl = new URL(path);
//        return classUrl;
////        return file.toURI().toURL();
//    }


    public static boolean isCommander(){
        return !StringUtils.isEmpty(System.getProperty("config.console"));
    }


    public static boolean isCluster(){
        return !StringUtils.isEmpty(System.getProperty("config.cluster"));
    }
}

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

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/31
 *          <p>
 *          write description here
 */

public class Env {

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

    public static void loadCustomInstance() {
        ClassLoader myClassLoader = configExtendClassLoader();
        if (myClassLoader != null)
            loadExtendInstance(myClassLoader);
    }

    private static void loadExtendInstance(ClassLoader extendClassLoader) {
        List<Class<?>> extendAuths = ResourceUtil.getSubClasses(extendClassLoader, AbstractAuthProcessor.class);
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

        List<Class<?>> extendPreHandlers = ResourceUtil.getSubClasses(extendClassLoader, AbstractPreHandler.class);
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

        List<Class<?>> extendPostHandlers = ResourceUtil.getSubClasses(extendClassLoader, AbstractPostHandler.class);
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


    private static ClassLoader configExtendClassLoader() {
        String homeDir = System.getProperty("user.dir");
        File home = new File(homeDir);
        if (!home.exists())
            return null;

        Set<File> files = ResourceUtil.findFile(home, ".jar", true);

        ClassLoader extendClassLoader = createClassLoader(files, Thread.currentThread().getContextClassLoader());
        return extendClassLoader;
    }

    private static ClassLoader createClassLoader(Set<File> files, ClassLoader parent) {
        Set<URL> set = new HashSet<>();

        files.forEach((file) -> {
            try {
                set.add(buildClassLoaderUrl(file));
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

    private static URL buildClassLoaderUrl(File file) throws MalformedURLException {
        String url = "file:" + file.getPath();
        URL classUrl = new URL(url);
        return classUrl;
    }


    public static boolean isCommander(){
        return !StringUtils.isEmpty(System.getProperty("config.console"));
    }


    public static boolean isCluster(){
        return !StringUtils.isEmpty(System.getProperty("config.cluster"));
    }
}

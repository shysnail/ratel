package com.kaitusoft.ratel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
public class ResourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);
    /**
     * 获得根目录如果在jar中运行获得相对路径,反则返回当前线程运行的根目录
     *
     * @param fileName
     * @return
     */
    public static String getResource(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("文件名字不能为空");
        }
        URL path = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (path != null && path.getPath().contains(".jar!")) {
            return fileName;
        } else {
            String result = path == null ? "" : path.getPath();
            return result;
        }
    }

    public static <T> T instanceClass(String name, Class<T> type) throws Exception {
        Class clazz = Class.forName(name);
        T t = (T) clazz.newInstance();
        return t;
    }

    public static boolean inJar() {
        return ResourceUtil.class.getResource("").getPath().contains(".jar!");
    }

    public static boolean inJar(Class clazz) {
        return clazz.getResource("").getPath().contains(".jar!");
    }

    public static boolean inJar(String fileName){
        return Thread.currentThread().getContextClassLoader().getResource(fileName).getPath().contains(".jar!");
    }

    public static String getPath(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if(url == null)
            return "";

        if (url.getPath().contains(".jar!"))
            return fileName;

        return url.getPath();

    }

    /**
     * find all ?
     *
     * @param ext
     * @return
     */
    public static Set<File> findFile(File dir, String ext, boolean recursion) {

        Set<File> found = new HashSet<>();
        findTypeFilesInDir(dir, ext, found, recursion);

        return found;
    }

    private static void findTypeFilesInDir(File dir, String ext, Set<File> found, boolean recursion) {
        File[] subFiles = dir.listFiles();
        for (File subFile : subFiles) {
            if (!subFile.isDirectory()) {
                if (subFile.getName().toLowerCase().endsWith(ext))
                    found.add(subFile);

                continue;
            }

            if (!recursion)
                continue;


            findTypeFilesInDir(subFile, ext, found, recursion);
        }

    }


    public static List<Class<?>> getSubClasses(Class baseClass, Collection<Class<?>> classes){
        List<Class<?>> list = new ArrayList<>();
        try {
            for(Class clazz : classes){

                if (baseClass.isAssignableFrom(clazz)) {
                    if (!baseClass.equals(clazz)) {
                        // 自身并不加进去
                        list.add(clazz);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Class<?>> getClassesFromJars(Collection<File> jarFiles, ClassLoader classLoader) throws Exception{
        List<Class<?>> classes = new LinkedList<>();
        for(File jar : jarFiles){
            classes.addAll(getClasssFromJarFile(classLoader, jar.getPath(), null));
        }
        return classes;
    }


    /**
     * 从jar文件中读取指定目录下面的所有的class文件
     *
     * @param jarPath  jar文件存放的位置
     * @param packagePre 指定的包前缀
     * @return 所有的的class的对象
     */
    protected static List<Class<?>> getClasssFromJarFile(ClassLoader classLoader, String jarPath, String packagePre) throws Exception {
        ClassLoader useClassLoader = classLoader;
        if(useClassLoader == null)
            useClassLoader = Thread.currentThread().getContextClassLoader();

        List<Class<?>> clazzs = new ArrayList<Class<?>>();

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);

            List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

            Enumeration<JarEntry> ee = jarFile.entries();
            while (ee.hasMoreElements()) {
                JarEntry entry = (JarEntry) ee.nextElement();
                // 过滤我们出满足我们需求的东西
                String entryName = entry.getName();
                if (entryName.endsWith(".class") && (StringUtils.isEmpty(packagePre) || entryName.startsWith(packagePre))) {
                    jarEntryList.add(entry);
                }
            }
            for (JarEntry entry : jarEntryList) {
                try {
                    if(logger.isDebugEnabled() && entry.getName().indexOf("FactoryBean") >= 0)
                        logger.error("jar:{}, class:{}", jarPath, entry.getName());
                    if(entry.getName().startsWith("org/hyperic"))
                        continue;
                    clazzs.add(loadFileAsClass(useClassLoader, entry.getName()));
                } catch (Throwable t) {
//                    logger.debug("load class error, class:{}, jar:{}", entry.getName(), jarPath, t);
                }
            }
        } catch (IOException e1) {
            logger.error("load class error, jar:{}", jarPath, e1);
            throw new Exception("jar:" + jarPath);
        } finally {
            if (null != jarFile) {
                try {
                    jarFile.close();
                } catch (Exception e) {
                }

            }
        }
        return clazzs;
    }


    public static Class<?> loadFileAsClass(ClassLoader classLoader, String fileName) throws ClassNotFoundException, Throwable {
        return loadFileAsClass(classLoader, fileName, null);
    }

    /**
     *
     * @param classLoader
     * @param fileName 文件名
     * @param prefix -- 路径前缀，需要剔除
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadFileAsClass(ClassLoader classLoader, String fileName, File prefix) throws ClassNotFoundException, Throwable {
        if(prefix != null)
            fileName = fileName.substring(prefix.getPath().length() + 1);
        fileName = fileName.substring(0, fileName.length() - 6);
        String className = fileName.replace('/', '.');
        return classLoader.loadClass(className);
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException, Throwable {
        return classLoader.loadClass(className);
    }

//
//
//    @SuppressWarnings("rawtypes")
//    private static ArrayList<Class> getAllClass(ClassLoader classLoader, String packagename) {
//        ArrayList<Class> list = new ArrayList<>();
//        if (classLoader == null)
//            classLoader = Thread.currentThread().getContextClassLoader();
//
//
//        String path = "/";
//        if (!StringUtils.isEmpty(packagename))
//            path = packagename.replace('.', File.separatorChar);
//        try {
//            List<File> fileList = new ArrayList<>();
//            /**
//             * 这里面的路径使用的是相对路径
//             * 如果大家在测试的时候获取不到，请理清目前工程所在的路径
//             * 使用相对路径更加稳定！
//             * 另外，路径中切不可包含空格、特殊字符等！
//             * 本人在测试过程中由于空格，吃了大亏！！！
//             */
//            Enumeration<URL> enumeration = classLoader.getResources("");
//            while (enumeration.hasMoreElements()) {
//                URL url = enumeration.nextElement();
//                fileList.add(new File(url.getFile()));
//            }
//            for (int i = 0; i < fileList.size(); i++) {
//                list.addAll(findClass(fileList.get(i), packagename));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    private static List<Class> findClass(File file, String packagename) {
//        List<Class> list = new LinkedList<>();
//        if (!file.exists()) {
//            return list;
//        }
//        // 返回一个抽象路径名数组，这些路径名表示此抽象路径名表示的目录中的文件。
//        File[] files = file.listFiles();
//        for (File file2 : files) {
//            if (file2.isDirectory()) {
//                if (!file2.getName().contains(".")) {
//                    List<Class> list2 = findClass(file2, packagename + "." + file2.getName());
//                    list.addAll(list2);
//                }
//            } else if (file2.getName().endsWith(".class")) {
//                try {
//                    // 保存的类文件不需要后缀.class
//                    list.add(Class.forName(packagename + '.' + file2.getName().substring(0, file2.getName().length() - 6)));
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return list;
//    }


    /**
     * find one
     * @param fileName
     * @return
     */
    public static String findFileInClasspath(String fileName) throws FileNotFoundException {
        String[] files  = findFileInClasspath(fileName, false);
        if(files == null || files.length == 0)
            throw new FileNotFoundException(fileName + "not found");

        return files[0];
    }


    public static String[] getClasspathDirectories(){
        String classpath = "";//System.getenv("CLASSPATH") + ";";
//        classpath += System.getProperty("java.class.path") + ";";
        classpath += System.getProperty("user.dir") + ";";

        String[] directories = classpath.split(";");
        Set<String> dirs = new HashSet<>();
        for(String dir : directories){
            if("null".equalsIgnoreCase(dir) || StringUtils.isEmpty(dir))
                continue;
            File f = new File(dir);
            if(f.isDirectory())
                dirs.add(dir);
        }
        String[] result = new String[dirs.size()];
        dirs.toArray(result);
        return result;
    }

    /**
     * find all ?
     * @param fileName
     * @return
     */
    public static String[] findFileInClasspath(String fileName, boolean findAll) throws FileNotFoundException{

        String[] dirs = getClasspathDirectories();

        Set<String> found = new HashSet<>();
        for(String dir : dirs){
            File dirFile = new File(dir);
            findInDir(dirFile, fileName, found, findAll);
            if(!findAll && found.size() > 0)
                break;
        }

        if(found.size() == 0)
            throw new FileNotFoundException(fileName + "not found");

        String[] result = new String[found.size()];
        found.toArray(result);

        return result;
    }

    private static void findInDir(File dir, String fileName, Set<String> found, boolean findAll){
        if(found.size() > 0 && !findAll)
            return;
        File target = new File(dir.getPath() + File.separator + fileName);
        if(target.exists() && target.isFile()) {
            found.add(target.getPath());
            if(!findAll)
                return;
        }

        File[] subFiles = dir.listFiles();
        for(File subFile : subFiles){
            if(!subFile.isDirectory())
                continue;

            findInDir(subFile, fileName, found, findAll);
        }

    }

}

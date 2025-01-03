
package com.ycw.core.internal.loader;

import com.google.common.collect.Sets;
import com.ycw.core.internal.db.Mysql;
import com.ycw.core.internal.db.entity.DBEntity;
import com.ycw.core.internal.db.entity.JdbcRepository;
import com.ycw.core.internal.loader.annotation.ClassFilter;
import com.ycw.core.internal.loader.annotation.ProjectFilter;
import com.ycw.core.internal.loader.service.AbstractService;
import com.ycw.core.util.AnnotationUtil;
import com.ycw.core.util.ClassUtils;
import com.ycw.core.util.CollectionUtils;
import com.ycw.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <类扫描器实现类>
 * <p>
 * ps: 扫描指定包下所有继承AbstractService的业务接口
 *
 * @author <yangcaiwang>
 * @version <1.0>
 */
public class Scanner {

    private static final Logger log = LoggerFactory.getLogger(Scanner.class);

    public static Set<Class<?>> classes = Sets.newHashSet();

    private static Scanner scanner = new Scanner();

    public static Scanner getInstance() {
        return scanner;
    }

    public <T> void scan(String... packPaths) {
        for (String path : packPaths) {
            scan(classes, path, null, clz -> {
                try {
                    boolean b1 = !Modifier.isInterface(clz.getModifiers());
                    boolean b2 = !Modifier.isAbstract(clz.getModifiers());
                    boolean b3 = ClassUtils.isAssignableFrom(AbstractService.class, clz);
                    boolean b = (b1 && b2 && b3);
                    if (!b) {
                        log.debug("break searched class [{}],[{},{},{}]", clz, b1, b2, b3);
                    }
                    return b;
                } catch (Exception e) {
                    log.error(e.getMessage() + ":" + clz, e);
                    return false;
                }
            });
        }
        classes.forEach(clz -> {
            try {
                if (ClassUtils.isAssignableFrom(JdbcRepository.class, clz)) {
                    Class<? extends DBEntity> entityType = (Class<? extends DBEntity>) ClassUtils.getTypeArguments(clz)[0];
                    if (!Mysql.getInstance().containsAliasName(entityType)) {
                        log.warn("======================= [{}]没有配置数据库信息，不会初始化数据仓库 =======================", entityType);
                        return;
                    }
                }
                if (ClassUtils.isAssignableFrom(AbstractService.class, clz)) {
                    Constructor<T> constructor = (Constructor<T>) clz.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    T service = (T) constructor.newInstance();
                    log.info("======================= new AbstractService [{}] =======================", service);
                }
            } catch (Exception e) {
                log.error(e.getMessage() + ":" + clz, e);
            }
        });
    }

    /**
     * 扫描项目,同时也会扫描所有引用的项目
     *
     * @param classes     存放扫描到的class
     * @param rootPackage 开始扫描的包目录
     * @param classFilter class过滤器
     */
    public void scan(Set<Class<?>> classes, String rootPackage, ClassFilter classFilter) {
        scan(classes, rootPackage, null, classFilter);
    }

    public void scan(Set<Class<?>> classes, String rootPackage, ProjectFilter projectFilter, ClassFilter classFilter) {
        scan(classes, rootPackage, projectFilter, ClassLoader.getSystemClassLoader(), classFilter);
    }

    /**
     * 扫描项目,同时也会扫描所有引用的项目
     *
     * @param classes       存放扫描到的class
     * @param rootPackage   开始扫描的包目录
     * @param projectFilter 项目过滤器
     * @param classFilter   class过滤器
     */
    public void scan(Set<Class<?>> classes, String rootPackage, ProjectFilter projectFilter, ClassLoader loader, ClassFilter classFilter) {
        if (rootPackage == null)
            rootPackage = "";
        rootPackage = rootPackage.replace('/', '.');
        String rootPackageDir = rootPackage.replace('.', '/');
        Enumeration<URL> enumeration;
        try {
            enumeration = loader.getResources(rootPackageDir);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StringUtils.CHARSET_NAME);
                    File file = new File(filePath);
                    if (projectFilter == null || projectFilter.accept(file)) {
                        log.debug("search file [{}]", file.getAbsoluteFile());
                        doFile(rootPackageDir, classes, file, classFilter);
                    }
                } else if ("jar".equals(protocol)) {
                    JarURLConnection jarURLConnection = ((JarURLConnection) url.openConnection());
                    JarFile jar = jarURLConnection.getJarFile();
                    if (projectFilter == null || projectFilter.accept(jar)) {
                        log.debug("search jar [{}]", jar.getName());
                        doJar(rootPackage, classes, jar, classFilter);
                    }
                }

            }
            log.debug("searched all classes [{}]", classes.size());
        } catch (Exception e) {
            log.error(e.getMessage() + ":" + rootPackage, e);
        }
    }

    private void doFile(String packageDir, Set<Class<?>> classes, File file, ClassFilter classFilter) {
        if (file.isFile() && file.getName().endsWith(".class")) {
            doClassFile(packageDir, classes, file, classFilter);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (!CollectionUtils.isEmpty(files)) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".class")) {
                        doClassFile(packageDir, classes, f, classFilter);
                    } else if (f.isDirectory()) {
                        String nextPackageDir = packageDir.isEmpty() ? f.getName() : packageDir + '/' + f.getName();
                        doFile(nextPackageDir, classes, f, classFilter);
                    }
                }
            }
        }
    }

    private void doClassFile(String packageDir, Set<Class<?>> classes, File file, ClassFilter classFilter) {
        Class<?> clz = null;
        try {
            String classSimpleName = file.getName().substring(0, file.getName().length() - 6);
            String packageName = packageDir.replace('/', '.');
            String className = packageName + '.' + classSimpleName;
            clz = Class.forName(className);
            if (AnnotationUtil.isAnnotation(clz, Deprecated.class))
                return;
            if (classFilter != null) {
                if (classFilter.accept(clz)) {
                    classes.add(clz);
                }
            } else {
                classes.add(clz);
            }
        } catch (Exception e) {
            log.error(e.getMessage() + ":" + packageDir, e);
        }
    }

    private void doJar(String rootPackage, Set<Class<?>> classes, JarFile jarFile, ClassFilter classFilter) {
        Enumeration<JarEntry> enumeration = jarFile.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry entry = enumeration.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && name.endsWith(".class")) {
                String classDir = name.substring(0, name.length() - 6);
                String className = classDir.replace('/', '.');
                if (rootPackage.isEmpty() || className.startsWith(rootPackage)) {
                    Class<?> clz = null;
                    try {
                        log.debug("search jar class [{}]", className);
                        clz = Class.forName(className);
                        if (AnnotationUtil.isAnnotation(clz, Deprecated.class))
                            continue;
                        if (classFilter != null) {
                            if (classFilter.accept(clz)) {
                                classes.add(clz);
                            }
                        } else {
                            classes.add(clz);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage() + ":" + entry + "," + name, e);
                    }
                }
            }
        }
    }
}

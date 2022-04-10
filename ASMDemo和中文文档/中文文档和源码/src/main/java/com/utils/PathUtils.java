package com.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class PathUtils {

    // 获取当前类的所在工程路径;
    public static String getCurrentPath1() {
        File f = new File(PathUtils.class.getResource("/").getPath());
        return f.getPath();
    }

    // 获取当前类的绝对路径；
    public static String getCurrentClassPath(Class claz) {
        File f = new File(claz.getResource("").getPath());
        return f.getPath();
    }

    // 获取当前类的所在工程路径;
    public static String getCurrentProjectPath() {
        File directory = new File("");// 参数为空
        // getCanonicalPath()返回的就是标准的将符号完全解析的路径
        String courseFile = "";
        try {
            courseFile = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courseFile;
    }

    // 获取当前类的所在工程路径;
    // file:/D:/eclipseJavaWorkspace/eclipse202006Workspace/Hello/bin/
    public static String getCurrentPath4() {
        URL path = Thread.currentThread().getContextClassLoader().getResource("");
        return path.toString();
    }

    public static String getCurrentPath5() {
        return System.getProperty("java.class.path");
    }

    public static String getCurrentPath6() {
        return System.getProperty("user.dir");
    }

    public static String getCurrentPath7() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();// /D:/eclipseJavaWorkspace/eclipse202006Workspace/Hello/bin/
        String p = new File(path).getAbsolutePath();// D:\eclipseJavaWorkspace\eclipse202006Workspace\Hello\bin
        return p;
    }

}
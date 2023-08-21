package edu.zjut.yzj.the_snake_king_contest.botrunningsystem.utils;

import java.io.File;
import java.io.IOException;

public class MyFileUtils {
    /**
     * 判断文件是否存在，不存在就创建
     * @param file
     */
    public static void createFile(File file) {
        if (file.exists()) {
//            System.out.println("File exists");
        } else {
//            System.out.println("File not exists, create it ...");
            //getParentFile() 获取上级目录（包含文件名时无法直接创建目录的）
            if (!file.getParentFile().exists()) {
//                System.out.println("not exists");
                //创建上级目录
                file.getParentFile().mkdirs();
            }
            try {
                //在上级目录里创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

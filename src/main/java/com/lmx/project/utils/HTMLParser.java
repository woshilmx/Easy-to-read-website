package com.lmx.project.utils;


import java.io.*;


/**
 * 用于消除html文档中的user-select属性
 */

public class HTMLParser {

    public static void main(String[] args) throws IOException {


        String parentPath = "E:\\javaProject2023\\Boke\\resourse\\设计模式之美";
        File[] files = new File(parentPath).listFiles();
        for (File file : files) {
//            if (file.getName().startsWith("00")) {
//                continue;
//            }
            System.out.println(file.getName() + "开始处理");
            String path = file.getAbsolutePath();
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while ((line = reader.readLine()) != null) {
                if (line.contains("user-select")) {

                    stringBuilder.append("/*").append(line).append("*/").append("\n");

                } else {
                    stringBuilder.append(line).append("\n");
                }
            }


            reader.close();

//        写入文件
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        }
//        String path = "E:\\javaProject2023\\Boke\\resourse\\设计模式之美\\01丨为什么说每个程序员都要尽早地学习并掌握设计模式相关知识？.html";

    }


}

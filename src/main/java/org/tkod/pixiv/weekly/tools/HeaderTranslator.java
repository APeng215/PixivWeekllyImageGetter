package org.tkod.pixiv.weekly.tools;

import java.util.Scanner;

/**
 * 用来转换https报文首部,为java中设置HttpsConnection属性的形式
 */
public class HeaderTranslator {
    public static void main(String[] args){
        //输入从浏览器 直接摘取的 https 首部文本
        var s = new Scanner(System.in);
        while(s.hasNextLine()){
            var line = s.nextLine();
            var index = line.indexOf(':');
            line = line.replaceAll("\"","\\\\\"");
            System.out.println("connection.setRequestProperty(\"" +
                    line.substring(0,index) +
                    "\",\"" +
                    line.substring(index) +
                    "\");");

        }

    }
}

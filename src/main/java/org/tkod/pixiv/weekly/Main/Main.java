package org.tkod.pixiv.weekly.Main;

import org.tkod.pixiv.weekly.Imagesgetter.ImagesGetter;
import org.tkod.pixiv.weekly.PageGetter.PageGetter;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Main {

    public static Proxy getProxy() {
        return proxy;
    }
    static private Proxy proxy;
    static public String MODE = "";

    /**
     * 更改此项以调整默认Cookies
     */
    static public String COOKIES;
    /**
     * 更改此项以调整是否使用内置Cookies
     */
    final static private boolean COOKIES_NEEDED = true;

    public static void main(String[] args) {
        //设置代理
        autoSetProxy();
        //创建 图片文件夹
        var dir = new File("Images");
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("无法创建文件目录");
            System.exit(-1);
        }
        try {//等待系统创建文件夹
            Thread.sleep(200);
        } catch (InterruptedException ignored){}
        //获取 执行次数
        int numToGet = -1;
        //文本提示
        //类型获取
        var modeNeeded = true;
        while (modeNeeded){
            modeNeeded = false;
            System.out.println(
                    """
                            ***************************************
                            *                                     *
                            *         欢迎使用Pixiv周榜爬虫          *
                            *                                     *
                            ***************************************
    
                            
                            1 normal
                            2 R18
                            请选择爬虫模式:
                            """
            );
            var modeInput = new Scanner(System.in).nextInt();
            if(modeInput == 2){
                Main.MODE = "R18";
                if(COOKIES_NEEDED){//向用户询问具有R18权限的cookies
                    setCookies();
                }
                PageGetter.headUrlStr = "https://www.pixiv.net/ranking.php?mode=weekly_r18&date=";
            }
            else if(!(modeInput == 1)){
                System.out.println("请不要输入无关字符串");
                modeNeeded = true;
            }
        }
        //获取起始日期
        Calendar calendar = null;
        var dateNeeded = true;
        while (dateNeeded){
            //获取系统日期
            calendar = Calendar.getInstance();
            try{
                //文本提示
                var scanner = new Scanner(System.in);
                System.out.println("请输入需要下载的周榜的起始日期(不包含)：");
                System.out.print("年份：");
                var year = scanner.nextInt();
                calendar.set(Calendar.YEAR,year);
                System.out.print("月：");
                var month = scanner.nextInt();
                calendar.set(Calendar.MONTH,month-1);
                System.out.print("日：");
                var day = scanner.nextInt();
                calendar.set(Calendar.DAY_OF_MONTH,day);
                dateNeeded = false;
            }catch (Exception e){
                System.out.println("请输入正确的数字!!!!\n");
            }
        }
        //数目获取
        while(numToGet < 0){
            System.out.println("\n请输入需要获取过往几周的周榜图片：");
            try{
                numToGet = new Scanner(System.in).nextInt();
                if(numToGet > 100){
                    numToGet = -1;
                    System.out.println("数据量过大，请不要超过100周 qwq\n");
                }
            }catch (Exception e){
                System.out.println("请不要给我一些奇怪的数据啊喂 (#`O′)\n");
            }
        }
        //主线程
        var MainThread = Thread.currentThread();
        //创建 容量为 3 的 线程池
        var pool = Executors.newFixedThreadPool(3);
        //添加下载线程
        while (numToGet-- != 0){
            //向前 1 周
            calendar.add(Calendar.WEEK_OF_YEAR,-1);
            //获得日期字符串
            final var date =
                    calendar.get(Calendar.YEAR) +
                    String.format("%02d",calendar.get(Calendar.MONTH) + 1) +
                    String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
            //创建 周榜文件夹
            var dirW = new File("Images\\" + date + Main.MODE);
            if(!dirW.exists() && !dirW.mkdir()){
                System.out.println("无法创建文件目录");
                System.exit(-1);
            }
            //添加线程
            pool.execute(()->{
                //周榜页面 爬取 页面关键字
                var pageGetter = new PageGetter();
                pageGetter.setDate(date);
                var pageUrlKeywords = pageGetter.getPageUrlStrKeywords();
                //生成 页面链接
                List<String> pageUrls = new ArrayList<>();
                for(var pageUrlKeyword:pageUrlKeywords){
                    pageUrls.add("https://i.pximg.net/img-original/img/" + pageUrlKeyword + "_p0");
                }
                //爬取 周榜 图片
                var imageGetter= new ImagesGetter();
                imageGetter.setUrlStrs(pageUrls);
                imageGetter.setDate(date);
                imageGetter.downloadNow();
                try{
                    //尝试激活主线程
                    synchronized (MainThread){
                        MainThread.notify();
                    }
                }catch (Exception ignored){}
            });
        }
        //预定线程结束
        pool.shutdown();
        //检测程序结束
        while(true){
            try {
                //休眠主线程
                synchronized (MainThread){
                    Thread.currentThread().wait();
                    MainThread.wait(2000);
                }
                //检测线程池
                if(pool.isTerminated()){
                    System.out.println("程序已退出!!");
                    System.exit(0);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void setCookies() {
        //获取具有R18权限的Cookies
        //文本提示
        var scanner = new Scanner(System.in);
        System.out.println("\n请输入具有R18权限的cookies(可以 登录www.pixiv.net——F12开发者工具台——应用——cookies查看 ，也 通过查看访问R18内容时，发送的cookies 来查看)：\n");
        COOKIES = scanner.next();
    }

    public static void autoSetProxy() {
        //获取系统代理
        System.setProperty("java.net.useSystemProxies","true");
        var proxy = ProxySelector.getDefault().select(URI.create("https://www.pixiv.net/")).get(0);
        if(proxy.address() != null){
            Main.proxy = proxy;
        } else {
            // 没有设置代理
            System.out.println("警告：未检测到系统代理！！！");
//            System.exit(-1);
        }
    }

}

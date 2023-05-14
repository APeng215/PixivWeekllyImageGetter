package org.tkod.pixiv.weekly.Main;

import org.tkod.pixiv.weekly.Imagesgetter.ImagesGetter;
import org.tkod.pixiv.weekly.PageGetter.PageGetter;

import java.io.File;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
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
    final static String str = "https://i.pximg.net/img-original/img/2023/05/04/00/18/34/107784754_p0.png";

//    "https://www.pixiv.net/ranking.php?mode=weekly&date=20230510"
    //https://www.pixiv.net/ranking.php?mode=weekly&date=20230510&p=2&format=json
//    https://www.pixiv.net/ranking.php?mode=weekly&date=20230514&p=1&format=json

    public static void main(String[] args) {
        //设置代理
        autoSetProxy();
        //创建 图片文件夹
        var dir = new File("Images");
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("无法创建文件目录");
            System.exit(-1);
        }
        //获取 执行次数
        int numToGet = -1;
        //文本提示
        while(numToGet < 0){
            System.out.println(
                    """
                            ***************************************
                            *                                     *
                            *         欢迎使用Pixiv周榜爬虫          *
                            *                                     *
                            ***************************************


                            请输入需要获取过往几周的周榜图片："""
            );
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
        //获取系统日期
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        //创建 容量为 3 的 线程池
        var pool = Executors.newFixedThreadPool(3);
        //添加下载线程
        while (numToGet-- != 0){
            //向前 1 周
            calendar.add(Calendar.WEEK_OF_YEAR,-1);
            //获得日期字符串
            final var date =
                    String.valueOf(calendar.get(Calendar.YEAR)) +
                    String.format("%02d",calendar.get(Calendar.MONTH) + 1) +
                    String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
            //创建 周榜文件夹
            var dirW = new File("Images\\" + date);
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
            });
        }
        //预定 线程池的终止
        pool.shutdown();
        while(true){
            try {
                Thread.sleep(1000);
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
    public static void autoSetProxy() {
        //获取系统代理
        System.setProperty("java.net.useSystemProxies","true");
        var proxy = ProxySelector.getDefault().select(URI.create("https://www.pixiv.net/")).get(0);
        if(proxy.address() != null){
            Main.proxy = proxy;
        } else {
            // 没有设置代理
            System.out.println("未检测到系统代理，程序将无法访问Pixiv！！！");
            System.exit(-1);
        }
    }

}

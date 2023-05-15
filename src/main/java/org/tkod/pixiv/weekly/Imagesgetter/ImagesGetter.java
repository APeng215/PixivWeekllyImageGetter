package org.tkod.pixiv.weekly.Imagesgetter;

import org.tkod.pixiv.weekly.Main.Main;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

    public class ImagesGetter {
    private List<String> urlStrs;

    public void setDate(String date) {
        this.date = date;
    }

    private String date;
    public void setUrlStrs(List<String> urlStrs) {
        this.urlStrs = urlStrs;
    }

    public void downloadNow(){
        if(urlStrs == null){
            System.out.println("未初始化：图片链接！！！");
            return;
        }
        for(var urlStr : urlStrs) {
            try {
                downloadImage(urlStr);
            } catch (Exception e) {
                try {
                    downloadImage(urlStr);
                } catch (Exception ex) {
                    try {
                        downloadImage(urlStr);
                    } catch (Exception exc) {
                        try {
                            downloadImage(urlStr);
                        } catch (Exception exception) {
                            System.out.println("下载失败:" + urlStr);
                        }
                    }
                }
            }
        }
    }

        private void downloadImage(String urlStr) throws Exception{
            //图片名称
            var fileName = urlStr.substring(urlStr.lastIndexOf('/'));
            URL url;
            HttpsURLConnection connection;
            InputStream input;
            try {//对于png
                //创建连接
                url = new URL(urlStr + ".png");
                connection = (HttpsURLConnection) url.openConnection(Main.getProxy());
                //设置GET报文的Header
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
                connection.setRequestProperty("referer", "https://www.pixiv.net/");
                //创建链接
                connection.connect();
                //图片输入流
                input = connection.getInputStream();
                //更新文件名
                fileName += ".png";
            } catch (FileNotFoundException e) {//对于jpg
                //创建连接
                url = new URL(urlStr + ".jpg");
                connection = (HttpsURLConnection) url.openConnection(Main.getProxy());
                //设置GET报文的Header
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
                connection.setRequestProperty("referer", "https://www.pixiv.net/");
                //创建链接
                connection.connect();
                //图片输入流
                input = connection.getInputStream();
                //更新文件名
                fileName += ".jpg";
            }
            //文件输出流
            var pngFileOutputStream = new FileOutputStream(new File("Images\\" + date + Main.MODE, fileName));
            //开始下载
            var bytes = input.readAllBytes();
            input.close();
            connection.disconnect();
            //保存文件
            pngFileOutputStream.write(bytes);
            pngFileOutputStream.flush();
            pngFileOutputStream.close();
            System.out.println("已下载： " + fileName);
        }

    }

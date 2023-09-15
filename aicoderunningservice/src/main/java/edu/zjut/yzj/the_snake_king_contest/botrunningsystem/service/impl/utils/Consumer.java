package edu.zjut.yzj.the_snake_king_contest.botrunningsystem.service.impl.utils;

import edu.zjut.yzj.the_snake_king_contest.botrunningsystem.utils.MyFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

@Component
public class Consumer extends Thread {
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot) {
        this.bot = bot;
        this.start();   //Consumer线程线程启动

        try {
            this.join(timeout);  // 主线程最多等待Consumer线程timeout这么多时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();  // todo 这里其实不一定能真的中断执行bot代码的线程
        }
    }

    private String addUid(String code, String uid) {  // 在code中的Bot类名后添加uid
        int k = code.indexOf(" implements Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);
    }

//        @Override
//    public void run() {
//        UUID uuid = UUID.randomUUID();
//        String uid = uuid.toString().substring(0, 8);
//
//        //第一个参数 类名
//        //第二个参数 整段代码
//        Supplier<Integer> botInterface = Reflect.compile(
//                "com.kob.botrunningsystem.utils.Bot" + uid,
//                addUid(bot.getBotCode(), uid)
//        ).create().get();
//
//        //每个用户单独一个目录
//        String filePath="/apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt";
//
//        File file = new File(filePath);
//        //如果这个文件不存在则级联创建这个文件
//        FileUtils.createFile(file);
//
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        //把当前的局面信息写到 "/apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt"这个文件中
//        try (PrintWriter fout=new PrintWriter(file)){
//            fout.println(bot.getInput());
//            fout.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//
//
//        //AI代码的真正运行在这一步
//        //获得AI代码的运行结果，也就是0123这四个数字其中之一
//        Integer direction = botInterface.get();
//        System.out.println("move-direction: " + bot.getUserId() + " " + direction);
//
//        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
//        data.add("user_id", bot.getUserId().toString());
//        data.add("direction", direction.toString());
//
//        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
//    }
    private static boolean containerIsExist(String containerName)  {
        boolean isExist = false;
        String cmd = "docker ps -a | grep " + containerName;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(
                    new String[]{"/bin/sh", "-c", cmd}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader buff = new BufferedReader(isr);

        String line;
        try {
            if((line=buff.readLine())!=null){
                isExist = true;
                buff.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isExist;
    }

    private static boolean containerIsActive(String containerName) {
        boolean isActive = false;
        String cmd = "docker ps | grep " + containerName;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(
                    new String[]{"/bin/sh", "-c", cmd}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader buff = new BufferedReader(isr);

        String line;
        try {
            if((line=buff.readLine())!=null){
                isActive = true;
                buff.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isActive;
    }

    /**
     * 如果容器已经存在，则重启即可，如果重启尚未存在，则启动容器，并设置好正确的容器名称
     * @param containerName
     */
    public  void startOrRestartContainer(String containerName) {
        Runtime runtime = Runtime.getRuntime();
        if (containerIsExist(containerName)) {
            System.out.println("容器已经存在，重启即可");
            try {
                runtime.exec("docker restart " + containerName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("容器尚未存在，需要创建容器");

            //
            String cmd = "docker run -d --name bot" + bot.getUserId() + " -m 512M --memory-reservation 300M --cpus=1 -v /apps/kob/gameinfo/users/" + bot.getUserId() + ":/app myjdk8:01";
            try {
                runtime.exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer get() {
        String path = "/apps/kob/gameinfo/users/" + bot.getUserId() + "/result.txt";
        File file = new File(path);
        String res = "0";
        try {
            Scanner sc = new Scanner(file);
            res = sc.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(res);
    }


    /**
     * Consumer线程要干的事情：
     * 1.把bot代码写入到指定位置的文件
     * 2.拼接出容器的名称，如果容器不存在，则创建容器，如果容器存在，则重启容器
     * 3.每隔100ms，轮询检查容器是否stop,如果容器停止了，则读取对应位置的文件中的结果
     */
    @Override
    public void run() {
        //consumer线程要往这个文件写入当前的局面信息，供AI代码读取
        String outputPath = "/apps/kob/gameinfo/users/" + bot.getUserId() + "/gameinfo.txt";
        File outputFile = new File(outputPath);
        //如果这个文件不存在则级联创建这个文件
        if (!outputFile.exists()){
            System.out.println(outputPath+"文件不存在,马上创建！");
        }

        MyFileUtils.createFile(outputFile);
        try {   //确保文件创建成功
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //把当前的局面信息写到 /apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt"这个文件中
        try (PrintWriter fout = new PrintWriter(outputFile)) {
            fout.println(bot.getInput());
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        String userCodeFilePath = "/apps/kob/gameinfo/users/" + bot.getUserId() + "/Bot.java";
        File codeFile = new File(userCodeFilePath);
        //判断用户的java源代码是否已经存在
        boolean exists = codeFile.exists();
        if(!exists){    //如果不存在，则要往用户的目录下创建用户代码的java源文件
            MyFileUtils.createFile(codeFile);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //往用户目录写入java源文件
            try (PrintWriter fout = new PrintWriter(codeFile)) {
                fout.println(bot.getBotCode());
                fout.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //源文件都不存在，字节码当然也不存在，需要编译生成
            //在用户所属目录下编译用户的java源代码生成字节码
            String[] cmds = {"/bin/sh", "-c",
                    "cd /apps/kob/gameinfo/users/" + bot.getUserId() + " && javac Bot.java"};
            Process pro = null;
            try {
                pro = Runtime.getRuntime().exec(cmds);
                pro.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //即将启动或重启的容器名称
        String containerName = "bot" + bot.getUserId();
        //如果容器已经存在则重启容器,否则就启动一个全新容器
        startOrRestartContainer(containerName);
        //等容器启动
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //检查目标文件是否就绪
        String resFilePath = "/apps/kob/gameinfo/users/" + bot.getUserId() + "/result.txt";
        File resFile = new File(resFilePath);
        long lastRecentlyModifiedTimestamp=0L;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //读取结果文件的最后修改时间
            long curRecentlyModifiedTimestamp= resFile.lastModified();
            System.out.println("res文件的最后修改时间是：" + new Date(curRecentlyModifiedTimestamp));
            if (curRecentlyModifiedTimestamp > lastRecentlyModifiedTimestamp) {
                //说明文件更新了，可以从中读取结果
                System.out.println("结果文件有更新，可以去读取用户代码运行的结果了");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                String resStr = null;
                try {
//                    resStr = FileUtils.readFileToString(resFile);
                    Scanner sc = new Scanner(resFile);
                    resStr = sc.next();
                    System.out.println("sc.next()的结果是： "+resStr);
                } catch (IOException  e) {
                    e.printStackTrace();
                }

                System.out.println("读取到的结果是："+resStr);
                Integer direction = Integer.parseInt(resStr);
                System.out.println("下一步的方向是："+direction);

                //发送结果
                MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
                data.add("user_id", bot.getUserId().toString());
                data.add("direction", direction.toString());
                restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
                break;//一定要终止循环
            }
            lastRecentlyModifiedTimestamp=curRecentlyModifiedTimestamp;
        }
    }
}

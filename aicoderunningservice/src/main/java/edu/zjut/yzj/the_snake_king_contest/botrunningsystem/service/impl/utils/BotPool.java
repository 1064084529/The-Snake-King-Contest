package edu.zjut.yzj.the_snake_king_contest.botrunningsystem.service.impl.utils;

import edu.zjut.yzj.the_snake_king_contest.botrunningsystem.utils.MyFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;



@Component
public class BotPool extends Thread {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();
    private final static ExecutorService pool = Executors.newCachedThreadPool();
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        BotPool.restTemplate = restTemplate;
    }

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

    public  void startOrRestartContainer(String containerName,Bot bot) {
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

    /**
     * 真正往AI代码运行队列中添加AI代码运行任务的方法
     * @param userId userId
     * @param botCode 需要执行的AI代码
     * @param input 当前对战的局面信息
     */
    public void addBot(Integer userId, String botCode, String input) {
        //todo 这里后面可以换成用BlockingQueue实现,从而可以省掉互斥锁和条件变量
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll();  //条件满足，通知消费者线程取走任务并执行
        } finally {
            lock.unlock();
        }
    }

    /**
     * 动态编译运行java代码
     * @param bot 一个AI代码运行任务
     */
    private void consume(Bot bot) {
        //todo 这里可以用线程池去实现
        Runnable task = new Runnable() {
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
                    Thread.sleep(100);
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
                        Thread.sleep(100);
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

                startOrRestartContainer(containerName,bot);

                //等容器启动
                try {
                    Thread.sleep(300);
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
                    if (curRecentlyModifiedTimestamp > lastRecentlyModifiedTimestamp+10) {
                        //说明文件更新了，可以从中读取结果
                        System.out.println("结果文件有更新，可以去读取用户代码运行的结果了");
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        String resStr = null;
                        try {
                            Scanner sc = new Scanner(resFile);
                            resStr = sc.next();
                            System.out.println("sc.next()的结果是： "+resStr);
                        } catch (IOException  e) {
                            e.printStackTrace();
                        }

                        System.out.println("读取到的结果是："+resStr);

                        //发送结果
                        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
                        data.add("user_id", bot.getUserId().toString());
                        data.add("direction", resStr);
                        //删除结果文件
                        resFile.delete();
                        //确保删除成功
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
                        break;//一定要终止循环
                    }
                    lastRecentlyModifiedTimestamp=curRecentlyModifiedTimestamp;
                }
            }
        };

        pool.submit(task);

//        Consumer consumer = new Consumer();
//        consumer.startTimeout(5000, bot); //启动新线程去执行，5s没执行完直接放弃
    }


    @Override
    public void run() {
        while (true) {
            lock.lock();
            if (bots.isEmpty()) {
                try {
                    //任务队列是空的，只能等
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove();
                lock.unlock();

                consume(bot);  // 比较耗时，可能会执行几秒钟
            }
        }
    }
}

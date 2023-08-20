package edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl.utils;

import edu.zjut.yzj.ai_battle_platform.botrunningsystem.utils.FileUtils;
import edu.zjut.yzj.ai_battle_platform.botrunningsystem.utils.RuntimeUtils;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.UUID;
import java.util.function.Supplier;

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


//    @Override
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

    /**
     * Consumer线程要干的事情：
     * 1.把bot代码写入到指定位置的文件
     * 2.拼接出容器的名称，如果容器不存在，则创建容器，如果容器存在，则重启容器
     * 3.每隔100ms，轮询检查容器是否stop,如果容器停止了，则读取对应位置的文件中的结果
     */
    @Override
    public void run() {
        String filePath="/apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt";
        File file = new File(filePath);
        //如果这个文件不存在则级联创建这个文件
        FileUtils.createFile(file);

        //确保文件被级联创建完成
        try {
           Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String userCodeFilePath="/apps/kob/gameinfo/users/"+bot.getUserId()+"/Bot.java";
        File codeFile = new File(userCodeFilePath);
        FileUtils.createFile(codeFile);

        //把用户代码写入到文件中 文件名
        try (PrintWriter fout=new PrintWriter(codeFile)){
            fout.println(bot.getBotCode());
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        Runtime runtime = Runtime.getRuntime();
//        //编译这段java源文件，生成对应的字节码
//        String cdCommand = "cd /apps/kob/gameinfo/users/"+bot.getUserId();
//        try {
//            runtime.exec(cdCommand);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        runtime.exec("p")

        String[] cmds = {"/bin/sh", "-c",
                "cd /apps/kob/gameinfo/users/"+bot.getUserId()+" && javac Bot.java"};

        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec(cmds);
            pro.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        InputStream in = pro.getInputStream();
//
//        BufferedReader read = new BufferedReader(new InputStreamReader(in));
//
//        String line = null;
//
//        while (true) {
//            try {
//                if (!((line = read.readLine()) != null)) break;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(line);
//        }

    }
}

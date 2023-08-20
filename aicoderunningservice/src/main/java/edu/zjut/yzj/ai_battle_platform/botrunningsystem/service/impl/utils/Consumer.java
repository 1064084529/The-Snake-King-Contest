package edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl.utils;

import edu.zjut.yzj.ai_battle_platform.botrunningsystem.utils.FileUtils;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);

        //第一个参数 类名
        //第二个参数 整段代码
        Supplier<Integer> botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid)
        ).create().get();

        //每个用户单独一个目录
        String filePath="/apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt";

        File file = new File(filePath);
        //如果这个文件不存在则级联创建这个文件
        FileUtils.createFile(file);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        //把当前的局面信息写到 "/apps/kob/gameinfo/users/"+bot.getUserId()+"/gameinfo.txt"这个文件中
        try (PrintWriter fout=new PrintWriter(file)){
            fout.println(bot.getInput());
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        //AI代码的真正运行在这一步
        //获得AI代码的运行结果，也就是0123这四个数字其中之一
        Integer direction = botInterface.get();
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}

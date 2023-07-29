package edu.zjut.yzj.ai_battle_platform.botrunningsystem;

import edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl.BotRunningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotRunningSystemApplication {
    public static void main(String[] args) {
        BotRunningServiceImpl.botPool.start();//启动一个消费者线程
        SpringApplication.run(BotRunningSystemApplication.class, args);
    }
}
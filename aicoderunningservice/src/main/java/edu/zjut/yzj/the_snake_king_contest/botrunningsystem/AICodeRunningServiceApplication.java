package edu.zjut.yzj.the_snake_king_contest.botrunningsystem;

import edu.zjut.yzj.the_snake_king_contest.botrunningsystem.service.impl.BotRunningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AICodeRunningServiceApplication {
    public static void main(String[] args) {
        BotRunningServiceImpl.botPool.start();//启动一个消费者线程
        SpringApplication.run(AICodeRunningServiceApplication.class, args);
    }
}
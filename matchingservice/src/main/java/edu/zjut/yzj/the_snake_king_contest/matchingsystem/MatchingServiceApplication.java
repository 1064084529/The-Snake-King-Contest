package edu.zjut.yzj.the_snake_king_contest.matchingsystem;

import edu.zjut.yzj.the_snake_king_contest.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingServiceApplication {
    public static void main(String[] args) {
        MatchingServiceImpl.matchingPool.start();  // 启动匹配线程
        SpringApplication.run(MatchingServiceApplication.class, args);
    }
}
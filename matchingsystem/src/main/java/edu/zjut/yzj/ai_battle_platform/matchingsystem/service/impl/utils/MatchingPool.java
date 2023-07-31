package edu.zjut.yzj.ai_battle_platform.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 匹配池，会单独启动一个线程扫描匹配池中的玩家进行匹配。
 */
@Component
public class MatchingPool extends Thread {

    /**
     * 匹配池中的所有玩家。多线程共享变量，存在线程安全问题。读写操作需要加互斥锁保护。
     */
    private static List<Player> players = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;
    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";
    private final HashSet<Integer> matchPoolUserIds = new HashSet<>();

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }


    /**
     * 往匹配池中添加玩家,如果匹配池中已经存在该名玩家，则添加失败
     * @param userId
     * @param rating
     * @param botId
     */
    public boolean addPlayer(Integer userId, Integer rating, Integer botId) {
        if (matchPoolUserIds.contains(userId)) return false;
        lock.lock();
        try {
            players.add(new Player(userId, rating, botId, 0));
            //往hash表中存入userId
            matchPoolUserIds.add(userId);
        } finally {
            lock.unlock();
        }

        return true;
    }

    /**
     * 匹配成功或者玩家取消匹配，从匹配池中移除玩家
     * @param userId
     */
    public void removePlayer(Integer userId) {
        lock.lock();
        try {
            System.out.println("移除玩家id: " + userId);
            matchPoolUserIds.remove(userId);
            List<Player> newPlayers = new ArrayList<>();
            for (Player player: players) {
                if (!player.getUserId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将所有匹配池中的玩家的等待时间+1
     */
    private void increaseWaitingTime() {  // 将所有当前玩家的等待时间加1
        for (Player player: players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    /**
     * 判断两名玩家是否匹配
     * @param a
     * @param b
     * @return
     */
    private boolean checkMatched(Player a, Player b) {  // 判断两名玩家是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta <= waitingTime * 10;
    }

    private void sendResult(Player a, Player b) {  // 返回匹配结果
        System.out.println("send result: " + a + " " + b);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("a_bot_id", a.getBotId().toString());
        data.add("b_id", b.getUserId().toString());
        data.add("b_bot_id", b.getBotId().toString());

        restTemplate.postForObject(startGameUrl, data, String.class);

    }

    /**
     * 尝试匹配所有玩家
     */
    private void matchPlayers() {  // 尝试匹配所有玩家
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i ++ ) {
            if (used[i]) continue;
            for (int j = i + 1; j < players.size(); j ++ ) {
                if (used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if (checkMatched(a, b)) {
                    used[i] = used[j] = true;
                    //匹配成功，把玩家从hash表中移除
                    matchPoolUserIds.remove(a.getUserId());
                    matchPoolUserIds.remove(b.getUserId());
                    sendResult(a, b);
                    break;
                }
            }
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i ++ ) {
            if (!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println("匹配池中的玩家有：");
                System.out.println(players);
                System.out.println("玩家id的集合："+matchPoolUserIds);

                lock.lock();
                try {
                    increaseWaitingTime();
                    matchPlayers();
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}

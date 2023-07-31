package edu.zjut.yzj.ai_battle_platform.matchingsystem.service.impl;

import edu.zjut.yzj.ai_battle_platform.matchingsystem.service.MatchingService;
import edu.zjut.yzj.ai_battle_platform.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
    public final static MatchingPool matchingPool = new MatchingPool();

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add player: " + userId + " " + rating);
        if(matchingPool.addPlayer(userId, rating, botId))
            return "add player success";
        else
            return "该玩家已在匹配中，请勿重复添加";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove player: " + userId);
        matchingPool.removePlayer(userId);
        return "remove player success";
    }
}

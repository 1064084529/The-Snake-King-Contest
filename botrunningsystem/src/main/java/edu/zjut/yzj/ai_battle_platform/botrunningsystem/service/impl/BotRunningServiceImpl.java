package edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl;

import edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.BotRunningService;
import edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    public final static BotPool botPool = new BotPool();

    /**
     * 往AI代码运行队列中添加一个AI代码的task
     * @param userId
     * @param botCode
     * @param input
     * @return
     */
    @Override
    public String addBot(Integer userId, String botCode, String input) {
        System.out.println("add bot: " + userId + " " + botCode + " " + input);
        botPool.addBot(userId, botCode, input);
        return "add bot success";
    }
}

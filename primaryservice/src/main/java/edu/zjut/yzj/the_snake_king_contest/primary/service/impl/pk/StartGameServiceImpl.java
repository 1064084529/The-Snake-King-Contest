package edu.zjut.yzj.the_snake_king_contest.primary.service.impl.pk;

import edu.zjut.yzj.the_snake_king_contest.primary.consumer.WebSocketServer;
import edu.zjut.yzj.the_snake_king_contest.primary.service.pk.StartGameService;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl implements StartGameService {

    /**
     * 内部会new一个Game类实例，并启动一个线程去单独处理这场对战
     * @param aId
     * @param aBotId
     * @param bId
     * @param bBotid
     * @return
     */
    @Override
    public String startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotid) {
        System.out.println("start game: " + aId + " " + bId);
        WebSocketServer.startGame(aId, aBotId, bId, bBotid);
        return "start game success";
    }
}

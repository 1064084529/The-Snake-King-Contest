package edu.zjut.yzj.the_snake_king_contest.primary.service.impl.pk;

import edu.zjut.yzj.the_snake_king_contest.primary.consumer.WebSocketServer;
import edu.zjut.yzj.the_snake_king_contest.primary.consumer.utils.Game;
import edu.zjut.yzj.the_snake_king_contest.primary.service.pk.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move: " + userId + " " + direction + " ");
        if (WebSocketServer.users.get(userId) != null) {
            Game game = WebSocketServer.users.get(userId).game;
            if (game != null) {
                if (game.getPlayerA().getId().equals(userId)) {
                    game.setNextStepA(direction);
                } else if (game.getPlayerB().getId().equals(userId)) {
                    game.setNextStepB(direction);
                }
            }
        }

        return "receive bot move success";
    }
}

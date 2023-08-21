package edu.zjut.yzj.the_snake_king_contest.primary.consumer;

import com.alibaba.fastjson.JSONObject;
import edu.zjut.yzj.the_snake_king_contest.primary.consumer.utils.Game;
import edu.zjut.yzj.the_snake_king_contest.primary.consumer.utils.JwtAuthentication;
import edu.zjut.yzj.the_snake_king_contest.primary.mapper.BotMapper;
import edu.zjut.yzj.the_snake_king_contest.primary.mapper.RecordMapper;
import edu.zjut.yzj.the_snake_king_contest.primary.mapper.UserMapper;
import edu.zjut.yzj.the_snake_king_contest.primary.pojo.Bot;
import edu.zjut.yzj.the_snake_king_contest.primary.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每一个客户端与服务端websocket连接的抽象
 */
@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    /**
     * 把所有正在匹配池、游戏中的玩家维护到这个hash表
     */
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    //这个链接，具体指WebSocketServer类实例对应的用户是谁
    private User user;

    //后端向前端发送信息，得依靠这个session
    private Session session = null;

    public static RecordMapper recordMapper;
    private static BotMapper botMapper;
    public static RestTemplate restTemplate;

    //每一个链接，有一个Game实例。同一场对战中的两名玩家，他们的WebSocketServer实例中的game对象是同一个
    public Game game = null;

    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerurl = "http://127.0.0.1:3001/player/remove/";

    public static UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }


    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        //从jwt中解析出userID,能解析出来就说明这个userID合法。
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if (this.user != null) {
            WebSocketServer.users.put(userId, this);
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("disconnected!");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
        //todo 如果匹配池中有当前玩家，则把当前玩家从匹配池中移除，这样可以防止自己与自己匹配
        stopMatching();
    }

    /**
     * 开始游戏的方法，每场对战创建一个Game类实例并启动一个线程去处理本场对战。并在游戏正式开始前，把对手信息等返回给前端
     * @param aId A玩家id
     * @param aBotId A玩家botID
     * @param bId B玩家id
     * @param bBotId B玩家botID
     */
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);

        Game game = new Game(13, 14, 20, a.getId(), botA, b.getId(), botB);

        game.createMap();
        if (users.get(a.getId()) != null)
            users.get(a.getId()).game = game;
        if (users.get(b.getId()) != null)
            users.get(b.getId()).game = game;

        //开启一个新线程，去处理这场对战
        game.start();

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        //给A玩家的浏览器发送相关消息
        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (users.get(a.getId()) != null)
            users.get(a.getId()).sendMessage(respA.toJSONString());

        //给B玩家的浏览器发送相关消息
        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (users.get(b.getId()) != null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
    }

    /**
     *
     * @param botId 用户的botID
     */
    private void startMatching(Integer botId) {
        System.out.println("start matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    /**
     * 取消匹配的方法,会把
     */
    private void stopMatching() {
        System.out.println("stop matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerurl, data, String.class);
    }

    /**
     * onMessage方法接收到move事件时调用的方法，其内部会判断是玩家亲自操作还是AI代码运行服务给出的操作。只有当玩家亲自出马的时候
     * 才会真正设置Game中NextStepA和nextStepB变量的值
     * @param direction
     */
    private void move(int direction) {
        System.out.println("move " + direction);
        //这里需要判断当前websocket连接对应的玩家是本场游戏中的玩家A还是玩家B
        if (game.getPlayerA().getId().equals(user.getId())) {
            if (game.getPlayerA().getBotId().equals(-1))  // 亲自出马
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if (game.getPlayerB().getBotId().equals(-1))  // 亲自出马
                game.setNextStepB(direction);
        }
    }

    /**
     * 接收前端开始匹配或者游戏中操作指令的方法。
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {  // 当做路由
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }


    /**
     * 向当前链接绑定的客户端发送消息
     * @param message
     */
    public void sendMessage(String message) {
        //todo 这里应该没必要上锁
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
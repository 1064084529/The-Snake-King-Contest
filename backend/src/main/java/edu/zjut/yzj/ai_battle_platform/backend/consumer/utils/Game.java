package edu.zjut.yzj.ai_battle_platform.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import edu.zjut.yzj.ai_battle_platform.backend.consumer.WebSocketServer;
import edu.zjut.yzj.ai_battle_platform.backend.pojo.Bot;
import edu.zjut.yzj.ai_battle_platform.backend.pojo.Record;
import edu.zjut.yzj.ai_battle_platform.backend.pojo.User;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 每场对战会创建一个Game类实例，包含本场对战的相关信息，包括对战双方信息、地图信息、对战的状态
 */
@Getter
public class Game extends Thread {

    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;

    private final int[][] g;
    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    private final Player playerA, playerB;

    /**
     * Game线程、执行onMessage方法中处理move时间的线程，会并发读写这个变量，因此对于这个变量的修改，需要加上互斥锁。
     */
    private Integer nextStepA,nextStepB = null;

    private final ReentrantLock lock = new ReentrantLock();

    private String status = "playing";  // playing -> finished
    private String loser = "";  // all: 平局，A: A输，B: B输
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";


    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA,
                Bot botA, Integer idB, Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";
        if (botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if (botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, botIdB, botCodeB, 1, cols - 2, new ArrayList<>());
    }

    /**
     * 创建地图的方法，其内部循环调用draw来真正创建地图。
     */
    public void createMap() {
        for (int i = 0; i < 1000; i ++ ) {
            if (draw())
                break;
        }
    }

    /**
     * 执行单次画地图的方法。
     * @return 画出来的地图是否具有连通性。
     */
    private boolean draw() {  // 画地图
        for (int i = 0; i < this.rows; i ++ ) {
            for (int j = 0; j < this.cols; j ++ ) {
                g[i][j] = 0;
            }
        }

        for (int r = 0; r < this.rows; r ++ ) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c ++ ) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i ++ ) {
            for (int j = 0; j < 1000; j ++ ) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1)
                    continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
                    continue;

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }
        //返回生成的地图是否联通
        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }


    /**
     * 设置A玩家的下一步操作
     * @param nextStepA
     */
    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    /**
     * dfs判断两点之间是否联通
     * @param sx 源点x
     * @param sy 源点y
     * @param tx 目标点x
     * @param ty 目标点y
     * @return
     */
    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i ++ ) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }


    /**
     * 将当前的局面信息，编码成字符串
     * @param player
     * @return
     */
    private String getInput(Player player) {  // 将当前的局面信息，编码成字符串
        Player me, you;
        if (playerA.getId().equals(player.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }
        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    /**
     * 向AI代码运行服务 发送任务运行
     * @param player
     */
    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) return;  // 亲自出马，不需要执行代码
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString()); //userID
        data.add("bot_code", player.getBotCode());  //AI代码
        data.add("input", getInput(player));    //当前的局面信息
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    /**
     * @description: 核心方法。判断对战双方本回合是否给出操作指令。
     * @author: yzj
     * @date: 2023/7/26 9:21
     * @param: []
     * @return: boolean
     **/
    private boolean twoPlayersNextStepIsReady() {
        try {
            //这里等待200ms是因为，前端蛇移动一个格子,需要200ms
            //如果后端向前端发送操作过快，可能会丢掉中间的一些移动指令
            //前端200ms才会前进一格
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendBotCode(playerA);
        sendBotCode(playerB);

        //最多等待5s
        for (int i = 0; i < 50; i ++ ) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @description: 检查玩家的蛇，新增加的cell，是否和这条蛇本身、对方的蛇、障碍物发生碰撞
     * @author: yzj
     * @date: 2023/7/26 11:53
     * @param: [cellsA, cellsB]
     * @return: boolean
     **/
    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);

        //碰撞到障碍物检测
        if (g[cell.x][cell.y] == 1) return false;

        //碰撞到自己的身体检测
        for (int i = 0; i < n - 1; i ++ ) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y)
                return false;
        }

        //碰撞到对手的蛇的身体
        for (int i = 0; i < n - 1; i ++ ) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y)
                return false;
        }

        return true;
    }

    /**
     * @description: 判断两名玩家的下一步操作是否合法
     * @author: yzj
     * @date: 2023/7/26 11:40
     * @param: []
     * @return: void
     **/
    private void judge() {
        //获取两条蛇身体的所有格子
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        //检查玩家A的这一步操作是否合法
        boolean validA = check_valid(cellsA, cellsB);
        //检查玩家B的这一步操作是否合法
        boolean validB = check_valid(cellsB, cellsA);

        if (!validA || !validB) {
            status = "finished";
            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    /**
     * @description: 向两名玩家发送消息的方法
     * @author: yzj
     * @date: 2023/7/26 10:35
     * @param: [message]
     * @return: void
     **/
    private void sendMessage2AllPlayers(String message) {
        if (WebSocketServer.users.get(playerA.getId()) != null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        if (WebSocketServer.users.get(playerB.getId()) != null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }

    /**
     * @description: 向两个客户端发送两名玩家的移动信息，
     * 发送完之后，把nextStepA和nextStepB清空。
     * @author: yzj
     * @date: 2023/7/26 10:32
     * @param: []
     * @return: void
     **/
    private void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendMessage2AllPlayers(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @description: 向两名玩法发送对战结果
     * @author: yzj
     * @date: 2023/7/26 10:34
     * @param: []
     * @return: void
     **/
    private void sendResult() {
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        //把本局对战记录存入数据库
        saveToDatabase();
        sendMessage2AllPlayers(resp.toJSONString());
    }

    /**
     * 把地图信息编码成01串
     * @return 地图信息的01串
     */
    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i ++ ) {
            for (int j = 0; j < cols; j ++ ) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    /**
     * 游戏结束，更新玩家的天梯积分
     * @param player
     * @param rating
     */
    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    /**
     * @description: 将本局对战的相关信息存入数据库，以便将来回放；并更新两名玩家的天梯积分
     * @author: yzj
     * @date: 2023/7/26 16:41
     * @param: []
     * @return: void
     **/
    private void saveToDatabase() {
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if ("A".equals(loser)) {
            ratingA -= 2;
            ratingB += 5;
        } else if ("B".equals(loser)) {
            ratingA += 5;
            ratingB -= 2;
        }

        updateUserRating(playerA, ratingA);
        updateUserRating(playerB, ratingB);

        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i ++ ) {  //一场游戏到不了1000回合
            if (twoPlayersNextStepIsReady()) {  // 是否获取了两条蛇的下一步操作
                judge();
                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {    //至少有一名玩家没有输入
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
}

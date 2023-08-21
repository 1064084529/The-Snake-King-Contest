package edu.zjut.yzj.the_snake_king_contest.primary.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer botId;  // -1表示亲自出马，否则表示用AI打
    private String botCode;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;

    /**
     * @description: 判断当前回合，蛇的身体是否应该变长
     * @author: yzj
     * @date: 2023/7/26 11:43
     * @param: [step]
     * @return: boolean
     **/
    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    /**
     * @description: 获得蛇身体的方法
     * @author: yzj
     * @date: 2023/7/26 11:49
     * @param: []
     * @return: java.util.List<com.kob.backend.consumer.utils.Cell>
     **/
    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int d: steps) {
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing( ++ step)) {
                res.remove(0);
            }
        }
        return res;
    }

    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for (int d: steps) {
            res.append(d);
        }
        return res.toString();
    }
}

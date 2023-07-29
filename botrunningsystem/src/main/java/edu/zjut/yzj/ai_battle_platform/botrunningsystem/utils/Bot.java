package edu.zjut.yzj.ai_battle_platform.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class Bot implements Supplier<Integer>{
    @Override
    public Integer get() {
        File file = new File("input.txt");

        try {
            //从文件中读取 当前的局面信息
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private boolean check_tail_increasing(int step) {  // 检验当前回合，蛇的长度是否增加
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    /**
     * 根据起始点和操作序列，计算出蛇的身体
     * @param sx
     * @param sy
     * @param steps
     * @return
     */
    public List<Cell> getCells(int sx, int sy, String steps) {
        steps = steps.substring(1, steps.length() - 1);
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i ++ ) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing( ++ step)) {
                res.remove(0);
            }
        }
        return res;
    }

    /**
     * 根据当前局面信息，决定下一步往哪走
     * @param input 当前的局面信息编码成的字符串
     * @return 0123
     */
    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        int[][] g = new int[13][14];
        for (int i = 0, k = 0; i < 13; i ++ ) {
            for (int j = 0; j < 14; j ++, k ++ ) {
                if (strs[0].charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }

        /**
         * 玩家A蛇的起点
         */
        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        /**
         * 玩家B蛇的起点
         */
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        //玩家A的蛇身
        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        //玩家B的蛇身
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        //地图上标注蛇的身体
        for (Cell c: aCells) g[c.x][c.y] = 1;
        for (Cell c: bCells) g[c.x][c.y] = 1;

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        for (int i = 0; i < 4; i ++ ) {
            //(x,y)这个坐标是新的蛇头坐标
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                return i;
            }
        }

        return 0;
    }


}

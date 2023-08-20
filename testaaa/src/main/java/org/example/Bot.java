package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class Bot implements Supplier<Integer>{

    public static void main(String[] args) {
        Bot bot = new Bot();
        bot.get();
    }

    @Override
    public Integer get() {
        File file = new File("/apps/kob/gameinfo/gameinfo.txt");

        try {
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

    private boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

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

    public Integer nextMove(String input) {
        System.out.println("读到的字符串是："+input);
        String[] strs = input.split("#");
        int[][] g = new int[13][14];
        for (int i = 0, k = 0; i < 13; i ++ ) {
            for (int j = 0; j < 14; j ++, k ++ ) {
                if (strs[0].charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        for (Cell c: aCells) g[c.x][c.y] = 1;
        for (Cell c: bCells) g[c.x][c.y] = 1;

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

        //把AI代码的运行结果写到 /apps/kob/gameinfo/res_direction.txt这个文件中
        File file = new File("/apps/kob/gameinfo/res_direction.txt");
        for (int i = 0; i < 4; i ++ ) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                System.out.println("direction: " + i);
                try (PrintWriter fout=new PrintWriter(file)){
                    fout.println(i);
                    fout.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return i;
            }
        }

        try (PrintWriter fout=new PrintWriter(file)){
            fout.println(0);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }


}

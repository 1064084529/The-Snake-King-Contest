package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Bot{
    public static Integer get() {
        File file = new File("/app/gameinfo.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    public static List<Cell> getCells(int sx, int sy, String steps) {
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

    public static Integer nextMove(String input) {
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
        for (int i = 0; i < 4; i ++ ) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                return i;
            }
        }

        return 0;
    }

    public static void createFile(File file){
        if (file.exists()) {
            System.out.println("File exists");
        } else {
            System.out.println("File not exists, create it ...");
            //getParentFile() 获取上级目录（包含文件名时无法直接创建目录的）
            if (!file.getParentFile().exists()) {
                System.out.println("not exists");
                //创建上级目录
                file.getParentFile().mkdirs();
            }
            try {
                //在上级目录里创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int res = get();
        String outPutFilePath="/app/result.txt";
        File resFile = new File(outPutFilePath);
        createFile(resFile);
        //确保文件被级联创建完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (PrintWriter fout=new PrintWriter(resFile)){
            fout.println(res);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
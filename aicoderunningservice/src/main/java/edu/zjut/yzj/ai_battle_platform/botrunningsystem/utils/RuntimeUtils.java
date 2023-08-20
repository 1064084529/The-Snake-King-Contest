package edu.zjut.yzj.ai_battle_platform.botrunningsystem.utils;

import java.io.*;

public class RuntimeUtils {
    public static String exec(String cmdStr){
        final StringBuilder str = new StringBuilder();
        final Runtime runtime = Runtime.getRuntime();
        if (runtime == null){
            return str.toString();
        }
        Process pro = null;
        BufferedReader input = null;
        PrintWriter writer = null;
        BufferedReader errorReader = null;
        try {
            pro = runtime.exec(cmdStr);
            input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            errorReader = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
            String line ;
            while ((line = input.readLine())!= null){
                str.append(line).append("\n");
            }
            while ((line = errorReader.readLine())!= null){
                System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }finally {
            try {
                if (errorReader != null){
                    errorReader.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            try {
                if (input != null){
                    input.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (writer != null){
                writer.close();
            }
            if (pro != null){
                pro.destroy();
            }

        }
        return str.toString();
    }
}

package edu.zjut.yzj.ai_battle_platform.backend.utils;

import java.util.Random;

public class VerificationCodeUtil {
    public static String code(int n) {
        //定义一个变量来接生成的字符串
        String code = "";
        Random r = new Random();
        //定义一个for循环来实现n次循环生成n个随机数并整合到一起组成验证码
        for (int i = 0; i < n; i++) {
            //生成一个随机字符：数字、英文大小写字母
//            int type = r.nextInt(3);//0 1 2 分别表示数字大小写字母
            int type = 0;
            switch (type) {
                case 0:
                    //数字
                    code += r.nextInt(10);//0-9
                    break;
                case 1:
                    //小写字母(a 97  z  97+25)
                    char ch = (char) (r.nextInt(26) + 97);
                    code += ch;//code = code +ch
                    break;
                case 2:
                    //大写字母(A 65  Z 65+25)
                    char ch1 = (char) (r.nextInt(26) + 65);
                    code += ch1;//code = code + ch1
                    break;
            }
        }
        //最后返回code值
        return code;
    }

}

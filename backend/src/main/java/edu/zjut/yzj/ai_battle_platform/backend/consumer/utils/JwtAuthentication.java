package edu.zjut.yzj.ai_battle_platform.backend.consumer.utils;

import edu.zjut.yzj.ai_battle_platform.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;

public class JwtAuthentication {
    /**
     *根据jwt获取用户id,能解析出userID就说明这个jwt合法
     * @param token
     * @return
     */
    public static Integer getUserId(String token) {
        int userId = -1;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return userId;
    }
}

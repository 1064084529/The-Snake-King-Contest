package edu.zjut.yzj.ai_battle_platform.backend.service.user.account;

import java.util.Map;

public interface LoginService {
    public Map<String, String> getToken(String username, String password,String phone,String code);

    public Map<String, String> sendCode(String phone);
}

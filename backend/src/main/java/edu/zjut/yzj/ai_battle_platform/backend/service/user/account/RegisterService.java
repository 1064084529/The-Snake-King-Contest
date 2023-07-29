package edu.zjut.yzj.ai_battle_platform.backend.service.user.account;

import java.util.Map;

public interface RegisterService {
    public Map<String, String> register(String username, String password, String confirmedPassword,String phone);
}

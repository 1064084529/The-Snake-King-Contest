package edu.zjut.yzj.the_snake_king_contest.primary.service.user.account;

import java.util.Map;

public interface LoginService {
    public Map<String, String> getToken(String username, String password,String phone,String code);

    public Map<String, String> sendCode(String phone);
}

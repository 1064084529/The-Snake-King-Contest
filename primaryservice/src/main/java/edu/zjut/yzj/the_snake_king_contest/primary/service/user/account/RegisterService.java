package edu.zjut.yzj.the_snake_king_contest.primary.service.user.account;

import java.util.Map;

public interface RegisterService {
    public Map<String, String> register(String username, String password, String confirmedPassword,String phone);
}

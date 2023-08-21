package edu.zjut.yzj.the_snake_king_contest.primary.service.impl.user.account;

import edu.zjut.yzj.the_snake_king_contest.primary.pojo.User;
import edu.zjut.yzj.the_snake_king_contest.primary.service.impl.utils.UserDetailsImpl;
import edu.zjut.yzj.the_snake_king_contest.primary.service.user.account.InfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
    @Override
    public Map<String, String> getinfo() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        map.put("photo", user.getPhoto());
        map.put("phone", user.getPhone());
        System.out.println("进入getinfo方法了)");
        return map;
    }
}

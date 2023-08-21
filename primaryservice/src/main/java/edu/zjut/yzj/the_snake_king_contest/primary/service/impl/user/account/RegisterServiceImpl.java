package edu.zjut.yzj.the_snake_king_contest.primary.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.zjut.yzj.the_snake_king_contest.primary.mapper.UserMapper;
import edu.zjut.yzj.the_snake_king_contest.primary.pojo.User;
import edu.zjut.yzj.the_snake_king_contest.primary.service.user.account.RegisterService;
import edu.zjut.yzj.the_snake_king_contest.primary.utils.MobileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword,String phone) {
        Map<String, String> map = new HashMap<>();
        if (username == null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if (password == null || confirmedPassword == null) {
            map.put("error_message", "密码不能为空");
            return map;
        }

        username = username.trim();
        if (username.length() == 0) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        if (password.length() == 0 || confirmedPassword.length() == 0) {
            map.put("error_message", "密码不能为空");
            return map;
        }

        if (username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }

        if (password.length() > 100 || confirmedPassword.length() > 100) {
            map.put("error_message", "密码长度不能大于100");
            return map;
        }

        if (!password.equals(confirmedPassword)) {
            map.put("error_message", "两次输入的密码不一致");
            return map;
        }

        if (!MobileUtil.isLegalMobileNumber(phone)) {
            map.put("error_message", "手机号不合法");
            return map;
        }


        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper1);
        if (!users.isEmpty()) {
            map.put("error_message", "用户名已存在");
            return map;
        }

        QueryWrapper<User> qw2 = new QueryWrapper<>();
        qw2.eq("phone", phone);
        List<User> users1 = userMapper.selectList(qw2);
        if (!users1.isEmpty()) {
            map.put("error_message", "手机号已存在");
            return map;
        }


        String encodedPassword = passwordEncoder.encode(password);
        String photo = "https://cdn.acwing.com/media/user/profile/photo/29061_lg_d028a5d048.jpg";
        User user = new User(null, username, encodedPassword,phone, photo, 1500);
        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}

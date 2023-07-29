package edu.zjut.yzj.ai_battle_platform.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.zjut.yzj.ai_battle_platform.backend.mapper.UserMapper;
import edu.zjut.yzj.ai_battle_platform.backend.pojo.User;
import edu.zjut.yzj.ai_battle_platform.backend.service.impl.utils.UserDetailsImpl;
import edu.zjut.yzj.ai_battle_platform.backend.service.user.account.LoginService;
import edu.zjut.yzj.ai_battle_platform.backend.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private MsgUtil msgUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public Map<String, String> getToken(String username, String password,String phone,String code) {

        String jwt = "";
        Map<String, String> map = new HashMap<>();
        //用户采用用户名+密码的方式登陆
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            Authentication authenticate = authenticationManager.authenticate(authenticationToken);  // 登录失败，会自动处理
            UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
            User user = loginUser.getUser();
            jwt= JwtUtil.createJWT(user.getId().toString());
        }else if(StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(code)) {
            //用户采用手机号+验证码的方式登陆
            //后端暂时不对手机号和验证码做校验了
            //1.去redis中查看是否存在该手机号为key
            String key="login_code:" + phone;
            boolean exist = redisUtil.hasKey(key);
            ;
            if (!exist) {
                map.put("error_message", "请发送验证码");
                return map;
            }else if (!redisUtil.get(key).equals(code)){
                map.put("error_message", "验证码错误！");
                return map;
            }
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("phone", phone);
            User user = userMapper.selectOne(qw);
            jwt= JwtUtil.createJWT(user.getId().toString());
            //验证码是一次性的，删除redis中的验证码
            redisUtil.del(key);
        }

        map.put("error_message", "success");
        map.put("token", jwt);

        return map;
    }

    @Override
    public Map<String, String> sendCode(String phone) {
        Map<String, String> map = new HashMap<>();
        //校验手机号是否符合手机号格式
        boolean success = MobileUtil.isLegalMobileNumber(phone);
        if (!success) {
            map.put("error_message","不是有效的手机号");
            return map;
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("phone",phone);
        List<User> users = userMapper.selectList(qw);

        if (users.isEmpty()) {
            map.put("error_message", "该手机号不存在");
            return map;
        }

        //如果redis中已经有该用户的验证码，并且ttl还大于4分钟，则拒绝发送新的验证码
        String key = "login_code:" + phone;
        boolean exist = redisUtil.hasKey(key);
        if(exist){
            long ttl = redisUtil.getExpire(key);
            if (ttl > 120L) {
                map.put("error_message", "请不要频繁发送验证码！");
                return map;
            }
        }

        //到这说明是一个正确的手机号，生成验证码
        String code = VerificationCodeUtil.code(6);
        //把验证码存入到redis中
        redisUtil.set(key, code, 300L);
        //todo 调用阿里云短信接口给用户发短信
        try {
            msgUtil.sendMsg(phone,code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        map.put("error_message", "success");
        return map;
    }
}

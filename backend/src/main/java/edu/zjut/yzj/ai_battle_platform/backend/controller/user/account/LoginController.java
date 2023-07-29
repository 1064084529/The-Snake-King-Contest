package edu.zjut.yzj.ai_battle_platform.backend.controller.user.account;

import edu.zjut.yzj.ai_battle_platform.backend.service.user.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/api/user/account/token/")
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        System.out.println("用户要登陆了！！");
        //用户名密码方式登陆
        String username = map.get("username");
        String password = map.get("password");
        //手机号验证码方式登陆
        String phone = map.get("phone");
        String code = map.get("code");
        return loginService.getToken(username, password, phone, code);
    }

    @PostMapping("/api/user/account/sendcode/")
    public Map<String, String> sendCode(@RequestParam Map<String, String> map) {
        String phone = map.get("phone");
        //查mysql看看有没有这个手机号
        System.out.println("要获取验证码的手机号是 " + phone);

        return loginService.sendCode(phone);
    }



}

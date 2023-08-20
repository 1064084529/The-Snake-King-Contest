package edu.zjut.yzj.ai_battle_platform.backend.controller.user.account;

import edu.zjut.yzj.ai_battle_platform.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterController {
    @Autowired
    private RegisterService registerService;

    @PostMapping("/api/user/account/register/")
    public Map<String, String> register(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        System.out.println("username= "+username);
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");
        String phone = map.get("phone");

        System.out.println("phone="+phone);

        return registerService.register(username, password, confirmedPassword,phone);
    }
}

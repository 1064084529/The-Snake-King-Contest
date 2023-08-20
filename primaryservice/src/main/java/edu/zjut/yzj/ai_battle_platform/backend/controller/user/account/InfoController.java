package edu.zjut.yzj.ai_battle_platform.backend.controller.user.account;

import edu.zjut.yzj.ai_battle_platform.backend.service.user.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {
    @Autowired
    private InfoService infoService;

    @GetMapping("/api/user/account/info/")
    public Map<String, String> getinfo() {
        return infoService.getinfo();
    }
}

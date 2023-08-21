package edu.zjut.yzj.the_snake_king_contest.primary.controller.user.bot;

import edu.zjut.yzj.the_snake_king_contest.primary.pojo.Bot;
import edu.zjut.yzj.the_snake_king_contest.primary.service.user.bot.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetListController {
    @Autowired
    private GetListService getListService;

    @GetMapping("/api/user/bot/getlist/")
    public List<Bot> getList() {
        return getListService.getList();
    }
}

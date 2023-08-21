package edu.zjut.yzj.the_snake_king_contest.primary.service.impl.user.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.zjut.yzj.the_snake_king_contest.primary.mapper.BotMapper;
import edu.zjut.yzj.the_snake_king_contest.primary.pojo.Bot;
import edu.zjut.yzj.the_snake_king_contest.primary.pojo.User;
import edu.zjut.yzj.the_snake_king_contest.primary.service.impl.utils.UserDetailsImpl;
import edu.zjut.yzj.the_snake_king_contest.primary.service.user.bot.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListServiceImpl implements GetListService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public List<Bot> getList() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        QueryWrapper<Bot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());

        return botMapper.selectList(queryWrapper);
    }
}

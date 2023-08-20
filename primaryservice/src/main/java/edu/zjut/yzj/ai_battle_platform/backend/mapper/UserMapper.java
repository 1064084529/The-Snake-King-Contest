package edu.zjut.yzj.ai_battle_platform.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.zjut.yzj.ai_battle_platform.backend.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

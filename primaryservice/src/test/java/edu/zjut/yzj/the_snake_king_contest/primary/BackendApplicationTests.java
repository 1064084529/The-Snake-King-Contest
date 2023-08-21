package edu.zjut.yzj.the_snake_king_contest.primary;

import edu.zjut.yzj.the_snake_king_contest.primary.utils.VerificationCodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;


@SpringBootTest(classes = {PrimaryServiceApplication.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {

    @Resource
    private RedisTemplate redisTemplate;

//    @Test
//    void contextLoads() {
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        System.out.println(passwordEncoder.encode("pyxc"));
//        System.out.println(passwordEncoder.encode("pb"));
//        System.out.println(passwordEncoder.encode("pc"));
//        System.out.println(passwordEncoder.encode("pe"));
//    }

    @Test
    void testCode(){
        String code = VerificationCodeUtil.code(6);
        System.out.println(code);
    }

    @Test
    void contextLoads() {
        //获取数据库的连接
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        redisTemplate.opsForValue().set("key1","myc");
        System.out.println(redisTemplate.opsForValue().get("key1"));

    }

}

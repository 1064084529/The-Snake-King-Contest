package edu.zjut.yzj.the_snake_king_contest.primary.service.ranklist;

import com.alibaba.fastjson.JSONObject;

public interface GetRanklistService {
    JSONObject getList(Integer page);
}

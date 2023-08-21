package edu.zjut.yzj.the_snake_king_contest.primary.service.record;

import com.alibaba.fastjson.JSONObject;

public interface GetRecordListService {
    JSONObject getList(Integer page);
}

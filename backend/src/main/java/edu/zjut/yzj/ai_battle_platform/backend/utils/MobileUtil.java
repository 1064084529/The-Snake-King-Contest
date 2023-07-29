package edu.zjut.yzj.ai_battle_platform.backend.utils;


import org.apache.commons.lang3.StringUtils;

public class MobileUtil {

    static final String MOBILE_RULE = "^1[3-9]\\d{9}$";

    public static boolean isLegalMobileNumber(String mobile) {
        if (StringUtils.isEmpty(mobile) || mobile.length() != 11) {
            return false;
        }
        return mobile.matches(MOBILE_RULE);
    }
}
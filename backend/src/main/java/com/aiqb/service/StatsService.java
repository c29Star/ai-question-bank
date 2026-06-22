package com.aiqb.service;

import java.util.Map;

public interface StatsService {
    /** 学生个人统计 */
    Map<String, Object> personal(Long userId);
}

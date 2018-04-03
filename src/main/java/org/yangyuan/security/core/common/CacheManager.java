package org.yangyuan.security.core.common;

/**
 * 缓存管理器定义
 * @author yangyuan
 * @date 2018年3月30日 
 * @param <T>
 * @param <E>
 */
public interface CacheManager<T, E> {
    /**
     * 强制缓存失效
     * @param subject
     */
    void invalid(Subject<T, E> subject);
}
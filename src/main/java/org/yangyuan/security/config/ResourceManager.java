package org.yangyuan.security.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.yangyuan.security.core.common.CacheManager;
import org.yangyuan.security.core.common.PrincipalFactory;
import org.yangyuan.security.core.common.SecurityAuthHandler;
import org.yangyuan.security.core.common.SecurityManager;
import org.yangyuan.security.dao.common.AuthSessionDao;
import org.yangyuan.security.dao.common.CacheSessionDao;
import org.yangyuan.security.dao.common.RedisResourceFactory;
import org.yangyuan.security.realm.common.JdbcRealmAdaptor;
import org.yangyuan.security.realm.common.Realm;
import org.yangyuan.security.realm.common.RemoteRealmAdaptor;
import org.yangyuan.security.util.SecurityConfigUtils;

/**
 * 统一资源管理器
 * @author yangyuan
 * @date 2018年3月30日
 */
public class ResourceManager {
    private static CommonResource commonResource;
    private static CookieResource cookieResource;
    private static CoreResource coreResource;
    private static DaoResource daoResource;
    private static SessionResource sessionResource;
    
    static {
        load();
    }
    
    private static void load(){
        RedisResourceFactory redisResourceFactory = getInstance(SecurityConfigUtils.cellString("common.redisResourceFactory"));
        commonResource =
        CommonResource.custom()
                        .redisResourceFactory(redisResourceFactory)
                        .build();
        
        cookieResource = 
        CookieResource.custom()
                        .name(SecurityConfigUtils.cellString("cookie.name"))
                        .domain(SecurityConfigUtils.cellString("cookie.domain"))
                        .path(SecurityConfigUtils.cellString("cookie.path"))
                        .httpOnly(SecurityConfigUtils.cellBoolean("cookie.http_only"))
                        .maxAge(SecurityConfigUtils.cellInt("cookie.max_age"))
                        .secure(SecurityConfigUtils.cellBoolean("cookie.secure"))
                        .build();
        
        sessionResource = 
        SessionResource.custom()
                        .expiresMilliseconds(SecurityConfigUtils.cellLong("session.expiresMilliseconds"))
                        .gcOpen(SecurityConfigUtils.cellBoolean("session.gc.open"))
                        .gcScript(SecurityConfigUtils.cellString("session.gc.script"))
                        .gcDelaySecond(SecurityConfigUtils.cellLong("session.gc.gcDelaySecond"))
                        .build();
        
        SecurityManager securityManager = getInstance(SecurityConfigUtils.cellString("core.securityManager"));
        PrincipalFactory principalFactory = getInstance(SecurityConfigUtils.cellString("core.principalFactory"));
        CacheManager<?, ?> cacheManager = getInstance(SecurityConfigUtils.cellString("core.cacheManager"));
        SecurityAuthHandler securityAuthHandler = getInstance(SecurityConfigUtils.cellString("core.securityAuthHandler"));
        coreResource = 
        CoreResource.custom()
                        .useClientSubjectLogin(SecurityConfigUtils.cellBoolean("core.useClientSubjectLogin"))
                        .securityManager(securityManager)
                        .principalFactory(principalFactory)
                        .cacheManager(cacheManager)
                        .securityAuthHandler(securityAuthHandler)
                        .build();
        
        CacheSessionDao<String, Object> ehcacheSessionDao = getInstance(SecurityConfigUtils.cellString("dao.ehcacheSessionDao"));
        CacheSessionDao<String, Object> redisSessionDao = getInstance(SecurityConfigUtils.cellString("dao.redisSessionDao"));
        Realm jdbcRealm = getInstance(SecurityConfigUtils.cellString("dao.jdbcRealm"));
        Realm remoteRealm = getInstance(SecurityConfigUtils.cellString("dao.remoteRealm"));
        AuthSessionDao jdbcSessionDao = getInstance(SecurityConfigUtils.cellString("dao.jdbcSessionDao"));
        AuthSessionDao remoteSessionDao = getInstance(SecurityConfigUtils.cellString("dao.remoteSessionDao"));
        JdbcRealmAdaptor jdbcRealmAdaptor = getInstance(SecurityConfigUtils.cellString("dao.jdbcRealmAdaptor"));
        RemoteRealmAdaptor remoteRealmAdaptor = getInstance(SecurityConfigUtils.cellString("dao.remoteRealmAdaptor"));
        daoResource = 
        DaoResource.custom()
                    .ehcacheSessionDao(ehcacheSessionDao)
                    .redisSessionDao(redisSessionDao)
                    .jdbcRealm(jdbcRealm)
                    .remoteRealm(remoteRealm)
                    .jdbcSessionDao(jdbcSessionDao)
                    .remoteSessionDao(remoteSessionDao)
                    .jdbcRealmAdaptor(jdbcRealmAdaptor)
                    .remoteRealmAdaptor(remoteRealmAdaptor)
                    .build();
    }
    
    /**
     * 公共资源
     * @return
     */
    public static CommonResource common(){
        return commonResource;
    }
    
    /**
     * cookie资源
     * @return
     */
    public static CookieResource cookie(){
        return cookieResource;
    }
    
    /**
     * 核心资源
     * @return
     */
    public static CoreResource core(){
        return coreResource;
    }
    
    /**
     * 数据访问资源
     * @return
     */
    public static DaoResource dao(){
        return daoResource;
    }
    
    /**
     * session资源
     * @return
     */
    public static SessionResource session(){
        return sessionResource;
    }
    
    /**
     * 获取bean实例
     * @param name bean完整类路径或spring ioc管理的bean名称
     * @return bean实例
     */
    @SuppressWarnings("unchecked")
    private static <T> T getInstance(String name){
        if(name.indexOf(".") > 0){
            return (T) getInstanceFromClass(name);
        }
        return (T) getInstanceFromSpring(name);
    }
    
    /**
     * 根据bean完整类路径，利用反射获取bean实例
     * @param name bean完整类路径
     * @return bean实例
     */
    @SuppressWarnings("unchecked")
    private static <T> T getInstanceFromClass(String name) {
        try {
            Class<?> cls =  Class.forName(name);
            return (T) cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 根据bean名称获取spring ioc 管理的 bean实例
     * @param name bean名称
     * @return bean实例
     */
    @SuppressWarnings("unchecked")
    private static <T> T getInstanceFromSpring(String name) {
        return (T) SpringIocHook.getBean(name);
    }
    
    /**
     * spring ioc 钩子
     * @author yangyuan
     * @date 2018年3月31日
     */
    @Component
    private static class SpringIocHook implements ApplicationContextAware {
        private static ApplicationContext applicationContext;
        
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            SpringIocHook.applicationContext = applicationContext;
        }
        
        /**
         * 根据bean名称获取spring ioc 管理的 bean实例
         * @param name bean名称
         * @return bean实例
         */
        @SuppressWarnings("unchecked")
        public static <T> T getBean(String name) {
            T t = (T) applicationContext.getBean(name);
            if(t == null){
                throw new RuntimeException("bean[" + name + "] not found");
            }
            return t;
        }
        
    }
    
    
}
package org.yangyuan.security.core;

/**
 * qq第三方登录令牌
 * @author yangyuan
 * @date 2018年4月3日
 */
public class QqRemoteToken extends RemoteToken{
    public QqRemoteToken(String accessToken) {
        super(accessToken);
    }

    @Override
    public int getPlanform() {
        return 1;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}

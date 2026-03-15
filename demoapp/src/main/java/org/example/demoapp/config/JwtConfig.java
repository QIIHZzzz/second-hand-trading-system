package org.example.demoapp.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")

public class JwtConfig {
    private String secret = "defaultSecretKeyChangeInProduction";
    private long expire = 86400000L;

    public String getSecret(){return secret;}
    public void setSecret(String secret){this.secret = secret;}

    public long getExpire(){return expire;}
    public void setExpire(long expire){this.expire = expire;}
}

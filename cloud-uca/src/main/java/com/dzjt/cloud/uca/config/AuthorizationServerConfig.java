package com.dzjt.cloud.uca.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${dzjt.oauth.authorization_type}")
    private String auth_type;

    /**
     * 用于密码模式
     */
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TokenStore tokenStore;

    @Autowired
    ClientDetailsService clientDetailsService;


    @Bean
    AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(this.tokenStore);
        tokenServices.setClientDetailsService(this.clientDetailsService);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setAccessTokenValiditySeconds(60 * 60 * 2);
        tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24 * 3);
        return tokenServices;
    }

    /**
     * 用于授权码模式
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "dzjt.oauth", name = "authorization_type", havingValue = "authorization_code")
    AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("permitAll()").allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        System.out.println("########################"+this.auth_type);
        clients.inMemory()
                .withClient("dzjt_web")
                .secret(new BCryptPasswordEncoder().encode("123"))
                .resourceIds("res1")
//                .authorizedGrantTypes("authorization_code","refresh_token")
                .authorizedGrantTypes("password","refresh_token")
//                .authorizedGrantTypes(this.auth_type, "refresh_token")//无效，不知道为啥
                .scopes("users", "orgs")
                .redirectUris("http://localhost:8081/index.html");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        if (this.auth_type == "authorization_code") {
            //用于授权码模式
            endpoints.authorizationCodeServices(this.authorizationCodeServices()).tokenServices(this.tokenServices());
        } else {
            // 用于密码模式
            endpoints.authenticationManager(this.authenticationManager).tokenServices(this.tokenServices());
        }
    }
}

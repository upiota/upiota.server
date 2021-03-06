package io.github.upiota.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import io.github.upiota.server.security.MyAuthenticationEntryPoint;
import io.github.upiota.server.security.MyAccessDeniedHandler;
import io.github.upiota.server.security.MyRedisTokenStore;
import io.github.upiota.server.security.MyWebResponseExceptionTranslator;


@Configuration
public class OAuth2ServerConfig {

    private static final String DEMO_RESOURCE_ID = "order";
    
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    	
    	@Autowired
     	private MyAuthenticationEntryPoint unauthorizedHandler;
    	
        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(DEMO_RESOURCE_ID).stateless(true).authenticationEntryPoint(unauthorizedHandler);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
        	http.antMatcher("/user/**").authorizeRequests().anyRequest().authenticated()
        	.and().exceptionHandling().accessDeniedHandler(new MyAccessDeniedHandler()).authenticationEntryPoint(unauthorizedHandler);
//        	http.authorizeRequests()
//        	.antMatchers(HttpMethod.OPTIONS).permitAll()
//        	.anyRequest().authenticated();
        }
    }


    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    	@Autowired
    	@Qualifier("authenticationManagerBean")
    	private AuthenticationManager authenticationManager;
        @Autowired
        private RedisConnectionFactory redisConnectionFactory;
        
        @Bean
    	public TokenStore tokenStore() {
    		return new MyRedisTokenStore(redisConnectionFactory);
    	}

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            //配置两个客户端,一个用于password认证一个用于client认证
            clients.inMemory()
            .withClient("client_1")
                    .resourceIds(DEMO_RESOURCE_ID)
                    .authorizedGrantTypes("client_credentials", "refresh_token")
                    .scopes("select")
                    .authorities("client")
                    .secret("123456")
                    .and().withClient("client_2")
                    .resourceIds(DEMO_RESOURCE_ID)
                    .authorizedGrantTypes("password", "refresh_token")
                    .scopes("select")
                    .authorities("client")
                    .secret(new BCryptPasswordEncoder().encode("123456"));
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        	endpoints//.exceptionTranslator(new MyWebResponseExceptionTranslator())
                    .tokenStore(tokenStore())
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            //允许表单认证
            oauthServer.allowFormAuthenticationForClients().accessDeniedHandler(new MyAccessDeniedHandler());
        }

    }

}

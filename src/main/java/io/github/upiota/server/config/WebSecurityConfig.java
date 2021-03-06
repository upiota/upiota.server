package io.github.upiota.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsUtils;

import io.github.upiota.server.security.MyAuthenticationEntryPoint;
import io.github.upiota.server.security.MyAccessDeniedHandler;

//@Configuration
//@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private MyAuthenticationEntryPoint unauthorizedHandler;

	@Autowired
	private MyAccessDeniedHandler accessDeniedHandler;

	// @Autowired
	// private MyFilterSecurityInterceptor myFilterSecurityInterceptor;

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Autowired
	public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/swagger-resources/**", "/webjars/**", "/v2/api-docs", "/swagger-ui.html", "/",
				"/*.html", "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js");
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity

				// we don't need CSRF because our token is invulnerable
				.csrf().disable()
				// don't create session
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

				.authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

				// allow anonymous resource requests
				// .antMatchers(
				// HttpMethod.GET,
				// "/", "/*.html", "/favicon.ico", "/**/*.html", "/**/*.css",
				// "/**/*.js").permitAll()
				.antMatchers("/auth/**").permitAll().anyRequest().authenticated();


		// httpSecurity.addFilterBefore(myFilterSecurityInterceptor,
		// FilterSecurityInterceptor.class);
		httpSecurity.exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
				.accessDeniedHandler(accessDeniedHandler);
		// disable page caching
		httpSecurity.headers().cacheControl();
	}
}
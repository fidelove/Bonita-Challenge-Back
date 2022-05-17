package com.bonitasoft.challenge.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
public class WebSecurityConfig {

	@Bean
	public Map<String, Long> sessionManager() {
		return new HashMap<>();
	}

//	@Autowired
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
//				.password("admin").roles("USER", "ADMIN", "READER", "WRITER").and().withUser("audit").password("audit")
//				.roles("USER", "ADMIN", "READER");
//	}
//
//	@Override
//	protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.authorizeRequests().antMatchers("/").permitAll();
//		httpSecurity.authorizeRequests().antMatchers("/user*").hasRole(RoleType.ADMIN.toString()).anyRequest()
//				.authenticated();
//	}
}

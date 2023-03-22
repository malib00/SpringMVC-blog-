package com.karpov.blog.config;

import com.karpov.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

	@Autowired
	private UserService userService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
		http
				.requiresChannel(channel ->
						channel.anyRequest().requiresSecure()) //redirect to HTTPS
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/", "/static/**", "/img/**", "/posts/**", "/register", "/activate/*", "/news", "/about").permitAll()
						.anyRequest().authenticated()
				)
				.userDetailsService(userService)
				.formLogin((form) -> form
						.loginPage("/login")
						.permitAll()
				)
				.rememberMe((remember) -> remember
						.rememberMeServices(rememberMeServices)
						.key("remember-me-key")
				)
				.logout((logout) -> logout.permitAll());

		return http.csrf().disable().build();
	}

	@Bean
	RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
		RememberMeTokenAlgorithm encodingAlgorithm = RememberMeTokenAlgorithm.SHA256;
		TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices("remember-me-key", userDetailsService, encodingAlgorithm);
		rememberMe.setMatchingAlgorithm(RememberMeTokenAlgorithm.MD5);
		return rememberMe;
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder(8);
	}
}

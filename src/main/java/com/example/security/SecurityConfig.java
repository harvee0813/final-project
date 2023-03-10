package com.example.security;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.security.service.CustomEmployeeDetailsService;
import com.example.security.service.CustomUserDetailsService;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private CustomUserDetailsService userDetailsService;
	@Autowired
	private CustomEmployeeDetailsService employeeDetailsService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http	
		.csrf().disable()
		.authorizeRequests()
		.antMatchers("/").permitAll()
		.antMatchers("/cus").permitAll()
		.antMatchers("/cus/register", "/cus/checkId","/cus/search-id","/cus/search-pw","/cus/registered").permitAll()
		.antMatchers("/emp/find-password", "/emp/checkSameEmpNo.json", "/emp/authentication-tel", "/emp/reset-login-form").permitAll() 
		.antMatchers("/emp/register", "/emp/registered").permitAll()
		.antMatchers("/emp/repair/**").permitAll()
		.antMatchers("/cus/login", "/emp/login", "/admin/login", "/emp/register/registeration", "/emp/register/empInsert", "/emp/register/getEmpNo.json", "/emp/register/inquiry").permitAll()
		.antMatchers("/logout").authenticated()
		.antMatchers("/cus/**").hasAnyRole("CUSTOMER", "ADMIN")
		.antMatchers("/reservation/**").hasAnyRole("CUSTOMER", "ADMIN")
		.antMatchers("/repair/**").hasAnyRole("CUSTOMER", "ADMIN")
		.antMatchers("/emp/**").hasAnyRole("??????", "??????", "?????????")
		.antMatchers("/erp/**").hasAnyRole("??????", "??????", "?????????")
		.antMatchers("/vacation/requestManage").hasRole("?????????")
		.antMatchers("/vacation/**").hasAnyRole("??????", "??????", "?????????")
		.antMatchers("/admin/**").hasRole("?????????")
		.anyRequest().authenticated()
		.and()
		.formLogin()
		.loginProcessingUrl("/login")
		.and()
		.logout()
		.logoutUrl("/logout")
		.logoutSuccessUrl("/cus")
		.and()
		.addFilter(authenticationFilter())
		.exceptionHandling()
		.accessDeniedHandler(accessDeniedHandler())
		.and()
		.headers().frameOptions().disable();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedHandler() {
			
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException) throws IOException, ServletException {

				response.sendRedirect("/access-denied");
			}
		};
	}
	
	@Bean
	public CustomAuthenticationFilter authenticationFilter() throws Exception {
		CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		filter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return filter;
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		Map<String, UserDetailsService> map = new HashMap<>();
		map.put("?????????", userDetailsService);
		map.put("??????", employeeDetailsService);
		map.put("?????????", employeeDetailsService);
		
		return new CustomAuthenticationProvider(map, passwordEncoder);
	}
	
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				CustomAuthenticationToken customAuthenticationToken = (CustomAuthenticationToken) authentication;
				
				String userType = customAuthenticationToken.getUserType();
				if ("?????????".equals(userType)) {
					response.sendRedirect("/cus");
				} else if ("??????".equals(userType)) {
					response.sendRedirect("/emp/register/registeration");
				} else if ("?????????".equals(userType)) {
					response.sendRedirect("/emp/register/registeration");

				}
			}			
		};
	}
	
	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new AuthenticationFailureHandler() {
			
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				
				String userType = request.getParameter("userType");
				if ("?????????".equals(userType)) {
					response.sendRedirect("/cus/login?error=" + exception.getMessage());
				} else if ("??????".equals(userType)) {
					response.sendRedirect("/emp/login?error=fail");
				} else if ("?????????".equals(userType)) {
					response.sendRedirect("/admin/login?error=fail");
				}
			}
		};

	}
	
}

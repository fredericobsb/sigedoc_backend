package com.sigedoc.sistemasigedoc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.sigedoc.sistemasigedoc.security.jwt.AuthEntryPointJwt;
import com.sigedoc.sistemasigedoc.security.jwt.AuthTokenFilter;
import com.sigedoc.sistemasigedoc.security.services.UserDetailsServiceImpl;


@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }
  
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
       
      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(passwordEncoder());
   
      return authProvider;
  }
  
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	  http.cors().and().csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests((authorize) -> authorize
              .antMatchers("/swagger-ui/**").permitAll()
              .antMatchers("/swagger-resources/**").permitAll()
              .antMatchers("/swagger-ui.html").permitAll()
              .antMatchers("/v2/api-docs/**").permitAll()
              .antMatchers("/api/auth/**").permitAll()
              .antMatchers("/api/test/**").permitAll()
              .anyRequest()
              .authenticated());
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();	  
  }
}

package com.finesse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.finesse.security.CustomUserDetailsService;
import com.finesse.security.JwtAuthenticationEntryPoint;
import com.finesse.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * AuthenticationProvider - Componente central da autenticação
     * Utiliza o CustomUserDetailsService para carregar usuários do banco
     * e o BCryptPasswordEncoder para validar senhas
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
                      
        // Define o encoder de senha (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());
        
        // Opcional: esconder se o usuário existe ou não nas mensagens de erro
        authProvider.setHideUserNotFoundExceptions(true);
        
        return authProvider;
    }

    /**
     * PasswordEncoder - BCrypt para criptografia de senhas
     * Strength padrão: 10 rounds
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager - Gerenciador central de autenticação
     * Usado pelo AuthController para autenticar usuários
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain - Configuração da cadeia de filtros de segurança
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configuração CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Desabilitar CSRF (não necessário para API REST stateless)
            .csrf(csrf -> csrf.disable())
            
            // Tratamento customizado de exceções de autenticação
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Configuração de autorização de requisições
            .authorizeHttpRequests(auth -> auth
                // Permitir OPTIONS para CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Rotas públicas de autenticação
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Swagger/OpenAPI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                
                // Rotas que exigem roles específicas
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Todas as outras rotas exigem autenticação
                .anyRequest().authenticated()
            )
            
            // Configuração de sessão - STATELESS (sem sessão no servidor)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Adicionar o AuthenticationProvider customizado
            .authenticationProvider(authenticationProvider())
            
            // Adicionar o filtro JWT antes do filtro padrão de autenticação
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

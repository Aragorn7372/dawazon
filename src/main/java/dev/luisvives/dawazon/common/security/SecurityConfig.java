package dev.luisvives.dawazon.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de Spring Security.
 * <p>
 * Define autenticación, autorización, login/logout y gestión de sesiones.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    /**
     * Servicio de detalles de usuario para autenticación.
     */
    final UserDetailsService userDetailsService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param userDetailsService Servicio de detalles de usuario.
     */
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Bean del codificador de contraseñas BCrypt.
     *
     * @return Codificador BCrypt.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación DAO.
     *
     * @return Proveedor de autenticación configurado.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean del administrador de autenticación.
     *
     * @param authConfig Configuración de autenticación.
     * @return Administrador de autenticación.
     * @throws Exception Sí hay error en la configuración.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura la cadena de filtros de seguridad.
     * <p>
     * Define:
     * - Rutas públicas (recursos estáticos, productos, auth)
     * - Configuración de login/logout personalizado
     * - Gestión de sesiones con máximo 1 sesión concurrente
     * </p>
     *
     * @param http Configurador de seguridad HTTP.
     * @return Cadena de filtros configurada.
     * @throws Exception Sí hay error en la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**", "/css/**", "/js/**", "/static/**").permitAll()
                        .requestMatchers("/*.png", "/*.jpg", "/*.jpeg", "/*.ico", "/*.svg").permitAll()

                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/", "/products", "/products/**", "/productos", "/productos/**").permitAll()
                        .requestMatchers("/auth/signin", "/auth/signup", "/auth/signin-post").permitAll()

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/signin")
                        .loginProcessingUrl("/auth/signin-post")
                        .usernameParameter("userName")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/signin?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false));

        return http.build();
    }
}
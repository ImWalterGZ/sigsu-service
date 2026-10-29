package mx.utch.sigsu.seguridad.service;

import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import mx.utch.sigsu.compartido.dto.ErrorResponseDto;
@Configuration
public class ConfiguracionSeguridad {
 @Bean PasswordEncoder contrasenas() { return new BCryptPasswordEncoder(); }
 @Bean TransactionTemplate transacciones(PlatformTransactionManager gestor) { return new TransactionTemplate(gestor); }
 @Bean SecurityFilterChain seguridad(HttpSecurity http,StringRedisTemplate redis,ObjectMapper json,@Value("${sigsu.idempotencia.ttl:24h}") Duration ttl) throws Exception {
  var filtro=new FiltroIdempotencia(redis,ttl,json);
  http.csrf(csrf->csrf.disable()).sessionManagement(sesion->sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(reglas->reglas
    .requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/actuator/health","/error").permitAll()
    .requestMatchers("/administracion/**").hasRole("ADMINISTRADOR")
    .requestMatchers("/bandeja","/bandeja/**","/solicitudes/*/tomar","/solicitudes/*/estado").hasRole("RESPONSABLE")
    .requestMatchers(HttpMethod.POST,"/solicitudes","/solicitudes/*/cancelar","/solicitudes/*/adjuntos").hasRole("ESTUDIANTE")
    .requestMatchers("/catalogo/**").authenticated().requestMatchers(HttpMethod.GET,"/solicitudes","/solicitudes/**").authenticated().anyRequest().denyAll())
   .httpBasic(basico->basico.authenticationEntryPoint((peticion,respuesta,error)->{
    respuesta.setHeader("WWW-Authenticate","Basic realm=\"SIGSU\""); respuesta.setStatus(401); respuesta.setContentType("application/json;charset=UTF-8"); respuesta.getOutputStream().write(json.writeValueAsBytes(new ErrorResponseDto(401,"credenciales requeridas o inválidas",null,List.of())));
   }))
   .exceptionHandling(errores->errores.accessDeniedHandler((peticion,respuesta,error)->{
    respuesta.setStatus(403); respuesta.setContentType("application/json;charset=UTF-8"); respuesta.getOutputStream().write(json.writeValueAsBytes(new ErrorResponseDto(403,"sin permiso para esta operación",null,List.of())));
   }))
   .addFilterAfter(filtro,AuthorizationFilter.class);
  return http.build();
 }
}
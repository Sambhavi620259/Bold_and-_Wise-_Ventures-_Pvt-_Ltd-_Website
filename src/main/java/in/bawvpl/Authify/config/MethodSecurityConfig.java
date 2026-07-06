package in.bawvpl.Authify.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

@Configuration
@RequiredArgsConstructor
public class MethodSecurityConfig {

    private final RoleHierarchy roleHierarchy;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {

        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();

        handler.setRoleHierarchy(roleHierarchy);

        return handler;
    }
}
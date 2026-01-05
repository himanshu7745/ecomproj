package com.example.ecomproj.auth.jwt;

import com.example.ecomproj.auth.security.RestAuthenticationEntryPoint;
import com.example.ecomproj.auth.service.JwtService;
import com.example.ecomproj.auth.service.UsersServices;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsersServices usersService;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    public JWTFilter(RestAuthenticationEntryPoint authenticationEntryPoint, JwtService jwtService, UsersServices usersService) {
        this.restAuthenticationEntryPoint = authenticationEntryPoint;
        this.jwtService = jwtService;
        this.usersService = usersService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token); // can throw ExpiredJwtException or other JwtException
            }

            if (username != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = usersService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {
                    var claims = jwtService.extractRoles(token);
                    var authorities = claims.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList();

                    var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Invalid or expired JWT: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
        } catch (Exception e) {
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Authentication failed: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
        }
    }
}

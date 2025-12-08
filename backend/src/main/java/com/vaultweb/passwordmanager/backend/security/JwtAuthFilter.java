package com.vaultweb.passwordmanager.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Minimalistic JWT authentication filter for Cloud Page backend.
 *
 * <p>This filter extracts the JWT from the "Authorization" header (Bearer scheme), validates it
 * using {@link JwtUtil}, and sets a simple authentication in the Spring Security context. No local
 * user database or password validation is required.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtUtil jwtUtil;

  public JwtAuthFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  /**
   * Filters each HTTP request to validate JWT tokens.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Extract JWT from the "Authorization" header if it starts with "Bearer ".
   *   <li>Validate the token using {@link JwtUtil}.
   *   <li>If valid, set an authenticated {@link UsernamePasswordAuthenticationToken} in the
   *       security context.
   * </ol>
   *
   * If the token is invalid or missing, a 401 Unauthorized response is returned.
   *
   * @param request the incoming HTTP request
   * @param response the HTTP response
   * @param filterChain the filter chain
   * @throws ServletException if a servlet error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (isCorsPreflightRequest(request)) {
      response.setStatus(HttpServletResponse.SC_OK);
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getServletPath();
    if (path.startsWith("/api/auth/")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.equals("/swagger-ui.html")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/webjars")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
      return;
    }

    String jwt = authHeader.substring(7);
    if (!jwtUtil.validateToken(jwt)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
      return;
    }

    String username = jwtUtil.extractUsername(jwt);
    Long userId = jwtUtil.extractUserId(jwt);
    if (userId == null) {
      LOGGER.warn("Token for subject {} does not contain required userId claim", username);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing userId claim in token");
      return;
    }

    AuthenticatedUser principal = new AuthenticatedUser(userId, username);
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            principal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    filterChain.doFilter(request, response);
  }

  private boolean isCorsPreflightRequest(HttpServletRequest request) {
    return HttpMethod.OPTIONS.matches(request.getMethod())
        && request.getHeader("Origin") != null
        && request.getHeader("Access-Control-Request-Method") != null;
  }
}

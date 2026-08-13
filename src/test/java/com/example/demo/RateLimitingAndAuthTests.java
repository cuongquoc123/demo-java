package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.FillterChains.RateLimitingFillter;
import com.example.demo.Service.JWTService;

class RateLimitingAndAuthTests {

    private RateLimitingFillter rateLimitingFilter;
    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFillter();
        jwtService = new JWTService();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUnauthenticatedRateLimitExceededAt11thRequest() throws Exception {
        String endpoint = "/api/auth/login";
        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", endpoint);
            request.setRemoteAddr("192.168.1.100");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            rateLimitingFilter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus(), "Request " + i + " should succeed");
        }

        // 11th request on the same endpoint should be rate-limited (HTTP 429)
        MockHttpServletRequest request = new MockHttpServletRequest("POST", endpoint);
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        rateLimitingFilter.doFilter(request, response, filterChain);
        assertEquals(429, response.getStatus(), "11th unauthenticated request should return 429");
        assertTrue(response.getContentAsString().contains("\"status\":429"), "Response JSON should contain status 429");
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/auth/login\""), "Response JSON should contain path");
    }

    @Test
    void testAuthenticatedRateLimitAllowsMoreThan10Requests() throws Exception {
        String username = "testuser";
        UserDetails userDetails = User.withUsername(username).password("password").authorities(Collections.emptyList()).build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String endpoint = "/api/demos/data";
        for (int i = 1; i <= 15; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", endpoint);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            rateLimitingFilter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus(), "Authenticated request " + i + " should succeed");
        }
    }

    @Test
    void testJWTGenerationAndValidation() {
        String token = jwtService.GeneratedToken("myuser");
        assertEquals("myuser", jwtService.ExtractUsername(token));

        UserDetails userDetails = User.withUsername("myuser").password("pass").authorities(Collections.emptyList()).build();
        assertTrue(jwtService.IsValidToken(token, userDetails));

        UserDetails differentUser = User.withUsername("otheruser").password("pass").authorities(Collections.emptyList()).build();
        assertFalse(jwtService.IsValidToken(token, differentUser));
    }

    @Test
    void testCustomAuthenticationEntryPointReturnsErrorRespone() throws Exception {
        com.example.demo.exception.CustomAuthenticationEntryPoint entryPoint = new com.example.demo.exception.CustomAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new org.springframework.security.authentication.BadCredentialsException("Unauthorized"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"status\":401"));
        assertTrue(response.getContentAsString().contains("\"error\":\"Unauthorized\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/protected\""));
    }

    @Test
    void testCustomAccessDeniedHandlerReturnsErrorRespone() throws Exception {
        com.example.demo.exception.CustomAccessDeniedHandler handler = new com.example.demo.exception.CustomAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new org.springframework.security.access.AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"status\":403"));
        assertTrue(response.getContentAsString().contains("\"error\":\"Forbidden\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/admin/users\""));
    }
}

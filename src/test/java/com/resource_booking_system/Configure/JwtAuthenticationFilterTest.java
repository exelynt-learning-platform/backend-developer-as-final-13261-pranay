package com.resource_booking_system.Configure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MyUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        userDetails = User.withUsername("john").password("password").authorities(Collections.singletonList(new SimpleGrantedAuthority("USER"))).build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldNotFilter_whenAuthPath_returnsTrue() {
        when(request.getServletPath()).thenReturn("/auth/login");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenSwaggerUi_returnsTrue() {
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenApiDocs_returnsTrue() {
        when(request.getServletPath()).thenReturn("/v3/api-docs/swagger-config");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenErrorPath_returnsTrue() {
        when(request.getServletPath()).thenReturn("/error");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_whenApiPath_returnsFalse() {
        when(request.getServletPath()).thenReturn("/api/reservations");
        assertFalse(filter.shouldNotFilter(request));
    }


    @Test
    void doFilterInternal_whenNoAuthorizationHeader_shouldContinueChain() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractAllClaims(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenHeaderNotBearer_shouldContinueChain() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractAllClaims(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenValidToken_shouldSetAuthentication() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtUtil.validateToken(claims, userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("john", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenAuthenticationAlreadySet_shouldSkipTokenValidation() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("existing", null, Collections.emptyList()));

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("john");

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenUsernameNull_shouldNotAuthenticate() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenInvalid_shouldNotAuthenticate() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtUtil.validateToken(claims, userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_whenTokenExpired_shouldCatchAndContinue() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer expired.token");
        when(jwtUtil.extractAllClaims("expired.token")).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenSignatureInvalid_shouldCatchAndContinue() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer bad.sig.token");
        when(jwtUtil.extractAllClaims("bad.sig.token")).thenThrow(new SignatureException("Invalid signature"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenMalformed_shouldCatchAndContinue() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.token");
        when(jwtUtil.extractAllClaims("malformed.token")).thenThrow(new MalformedJwtException("Malformed"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenUnsupported_shouldCatchAndContinue() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer unsupported.token");
        when(jwtUtil.extractAllClaims("unsupported.token")).thenThrow(new UnsupportedJwtException("Unsupported"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenIllegalArgument_shouldCatchAndContinue() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtUtil.extractAllClaims("")).thenThrow(new IllegalArgumentException("Empty token"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_whenUserNotFound_shouldNotAuthenticate() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("ghost");
        when(userDetailsService.loadUserByUsername("ghost"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_chainShouldBeInvokedExactlyOnce_onValidFlow() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractAllClaims("valid.jwt.token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtUtil.validateToken(claims, userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_chainShouldBeInvokedExactlyOnce_onException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer expired.token");
        when(jwtUtil.extractAllClaims("expired.token")).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
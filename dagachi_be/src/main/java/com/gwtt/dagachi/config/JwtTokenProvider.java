package com.gwtt.dagachi.config;

import com.gwtt.dagachi.adapter.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  private SecretKey key;
  private final UserDetailsService userDetailsService;

  @PostConstruct
  public void init() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    String nickname = userDetails.getNickname();
    String username = userDetails.getUsername();
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationTime);

    return Jwts.builder()
        .id(username)
        .issuedAt(now)
        .claim("nickname", nickname)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    String username =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getId();

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      System.out.println("검증되지 않은 토큰입니다. : " + e.getMessage());
    } catch (ExpiredJwtException e) {
      System.out.println("만료된 토큰입니다. : " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      System.out.println("지원하지 않는 토큰입니다. : " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.out.println("잘못된 토큰입니다. : " + e.getMessage());
    }
    return false;
  }
}

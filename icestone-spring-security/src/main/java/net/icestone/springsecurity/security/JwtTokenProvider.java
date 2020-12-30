package net.icestone.springsecurity.security;

import io.jsonwebtoken.*;
import net.icestone.springsecurity.models.AppUser;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.icestone.springsecurity.security.Token;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static net.icestone.springsecurity.security.SecurityConstants.EXPIRATION_TIME;
import static net.icestone.springsecurity.security.SecurityConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static net.icestone.springsecurity.security.SecurityConstants.SECRET;

@Component
public class JwtTokenProvider {

    //Generate token with Authentication
    public String generateToken(Authentication authentication){
        AppUser appUser = (AppUser)authentication.getPrincipal();
        Date now = new Date(System.currentTimeMillis());

        Date expiryDate = new Date(now.getTime()+EXPIRATION_TIME);

        String AppUserId = Long.toString(appUser.getId());

        Map<String,Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(appUser.getId())));
        claims.put("AppUsername", appUser.getUsername());
        claims.put("fullName", appUser.getFullName());

        return Jwts.builder()
                .setSubject(AppUserId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        }

    //Generate token with AppUser
    public String generateToken(AppUser appUser){
        Date now = new Date(System.currentTimeMillis());

        Date expiryDate = new Date(now.getTime()+EXPIRATION_TIME);

        String AppUserId = Long.toString(appUser.getId());

        Map<String,Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(appUser.getId())));
        claims.put("AppUsername", appUser.getUsername());
        claims.put("fullName", appUser.getFullName());

        return Jwts.builder()
                .setSubject(AppUserId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        }
    
    public Token generateRefreshToken(String subject) {
        Date now = new Date();
        System.out.println("now" + now.toString());
        //Long duration = now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME;
        //Date expiryDate = new Date(duration);
        Long duration = REFRESH_TOKEN_EXPIRATION_TIME;
        Date expiryDate = new Date(now.getTime()+REFRESH_TOKEN_EXPIRATION_TIME);
        System.out.println("expiryDate" + expiryDate.toString());
        
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        
        validateRefreshToken(token);
        
        return new Token(Token.TokenType.REFRESH, token, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }
    
    //Validate the token
    public boolean validateToken(String token){
    	
    	System.out.println("DF token:"+token);
    	
        try{
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        }catch (SignatureException ex){
            System.out.println("Invalid JWT Signature");
        }catch (MalformedJwtException ex){
            System.out.println("Invalid JWT Token");
        }catch (ExpiredJwtException ex){
            System.out.println("Expired JWT token");
        }catch (UnsupportedJwtException ex){
            System.out.println("Unsupported JWT token");
        }catch (IllegalArgumentException ex){
            System.out.println("JWT claims string is empty");
        }
        return false;
    }
    
    public boolean validateRefreshToken(String token) {
    	
    	System.out.println("validateRefreshToken:"+token);
    	
        try {
            Jwts.parser().setSigningKey(SECRET).parse(token);
            
            System.out.println("validateRefreshToken Validated ok");
            return true;
        } catch (SignatureException ex) {
            ex.printStackTrace();
        } catch (MalformedJwtException ex) {
            ex.printStackTrace();
        } catch (ExpiredJwtException ex) {
            ex.printStackTrace();
        } catch (UnsupportedJwtException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    //Get user Id from token

    public Long getUserIdFromJWT(String token){
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        String id = (String)claims.get("id");

        return Long.parseLong(id);
    }
    


    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public LocalDateTime getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }
    
}
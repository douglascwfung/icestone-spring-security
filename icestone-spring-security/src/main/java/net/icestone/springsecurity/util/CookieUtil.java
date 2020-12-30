package net.icestone.springsecurity.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    //@Value("${authentication-test.auth.accessTokenCookieName}")
    private String accessTokenCookieName = "accessToken";

    //@Value("${authentication-test.auth.refreshTokenCookieName}")
    private String refreshTokenCookieName = "refreshToken";

    public HttpCookie createAccessTokenCookie(String token, Long duration) {
        String encryptedToken = SecurityCipher.encrypt(token);
        return ResponseCookie.from(accessTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public HttpCookie createRefreshTokenCookie(String token, Long duration) {
    	
    	System.out.println("Refresh Token before encrypted:" + token);
    	
        String encryptedToken = SecurityCipher.encrypt(token);
        
        System.out.println("Refresh Token after encrypted:" + encryptedToken);
        
        
        return ResponseCookie.from(refreshTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessTokenCookieName, "").maxAge(0).httpOnly(true).path("/").build();
    }

}

package net.icestone.springsecurity.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.icestone.springsecurity.util.CookieUtil;

import net.icestone.springsecurity.security.Token;

import net.icestone.springsecurity.util.SecurityCipher;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.icestone.springsecurity.models.AppUser;
import net.icestone.springsecurity.payloads.JWTLoginSucessReponse;
import net.icestone.springsecurity.payloads.LoginRequest;
import net.icestone.springsecurity.security.JwtTokenProvider;
import net.icestone.springsecurity.services.MapValidationErrorService;
import net.icestone.springsecurity.services.AppUserService;
import net.icestone.springsecurity.validator.AppUserValidator;

import javax.validation.Valid;

import static net.icestone.springsecurity.security.SecurityConstants.TOKEN_PREFIX;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private MapValidationErrorService mapValidationErrorService;

    @Autowired
    private AppUserService userService;
    
    @Autowired
    private AppUserValidator userValidator;
    
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CookieUtil cookieUtil;
    
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
    		@CookieValue(name = "refreshToken", required = false) String refreshToken,
    		@Valid @RequestBody LoginRequest loginRequest, BindingResult result ){
    	
    	Token newRefreshToken;
    	HttpHeaders responseHeaders = new HttpHeaders();
    	
        String username = loginRequest.getUsername();
        //User user = userService.g  .findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email " + email));

    	
    	//String decryptedRefreshToken = SecurityCipher.decrypt(refreshToken);
    	//Boolean refreshTokenValid = tokenProvider.validateToken(refreshToken);
    	
    	newRefreshToken = tokenProvider.generateRefreshToken(username);
    	
    	addRefreshTokenCookie(responseHeaders, newRefreshToken);
    	
        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(result);
        if(errorMap != null) return errorMap;

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = TOKEN_PREFIX +  tokenProvider.generateToken(authentication);

        return ResponseEntity.ok().headers(responseHeaders).body(new JWTLoginSucessReponse(true, jwt));
    }

    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
    		@CookieValue(name = "refreshToken", required = false) String refreshToken,
    		@Valid @RequestBody LoginRequest loginRequest, BindingResult result ){
    	
    	System.out.println("refreshToken:"+refreshToken);
    	
    	String decryptedRefreshToken = SecurityCipher.decrypt(refreshToken);
    	
    	System.out.println("decryptedRefreshToken:" + decryptedRefreshToken);
    	
    	 Boolean refreshTokenValid = tokenProvider.validateRefreshToken(decryptedRefreshToken);
        
    	 System.out.println("refreshTokenValid:" + refreshTokenValid);
    	 
    	 if (!refreshTokenValid) {
             throw new IllegalArgumentException("Refresh Token is invalid!");
         }
    	 
    	 System.out.println("refreshTokenValid DF1:");
    	 
    	 String currentUser = tokenProvider.getUsernameFromToken(decryptedRefreshToken);
    	 
    	 System.out.println("refreshTokenValid DF2:");
    	
    	 AppUser appuser = userService.findByUsername(currentUser);
    	 
    	Token newRefreshToken;
    	HttpHeaders responseHeaders = new HttpHeaders();
    	
        //String username = loginRequest.getUsername();
        //User user = userService.g  .findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email " + email));

    	newRefreshToken = tokenProvider.generateRefreshToken(currentUser);
    	
    	addRefreshTokenCookie(responseHeaders, newRefreshToken);
    	
        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(result);
        if(errorMap != null) return errorMap;


        String jwt = TOKEN_PREFIX +  tokenProvider.generateToken(appuser);

        return ResponseEntity.ok().headers(responseHeaders).body(new JWTLoginSucessReponse(true, jwt));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AppUser user, BindingResult result){
        // Validate passwords match
        userValidator.validate(user,result);

        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(result);
        if(errorMap != null)return errorMap;

        AppUser newUser = userService.saveUser(user);

        return  new ResponseEntity<AppUser>(newUser, HttpStatus.CREATED);
    }
    
    
    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }
}
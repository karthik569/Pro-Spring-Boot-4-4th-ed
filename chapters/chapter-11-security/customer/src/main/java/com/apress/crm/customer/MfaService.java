package com.apress.crm.customer;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("mfa")
@Service
public class MfaService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecret() {
        return gAuth.createCredentials().getKey();
    }

    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
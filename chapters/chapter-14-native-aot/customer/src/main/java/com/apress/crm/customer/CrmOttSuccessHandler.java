package com.apress.crm.customer;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Profile("ott")
@Component
public class CrmOttSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken ott) throws IOException {
        // Generate the full Magic Link URL
        String redirectUri = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath() + "/login/ott")
                .queryParam("token", ott.getTokenValue())
                .build().toUriString();

        // Pro Tip: In production, send this via email using JavaMailSender
        System.out.println("Magic Link sent to " + ott.getUsername() + ": " + redirectUri);

        // For this demo, we redirect to a success page
        response.sendRedirect("/ott-sent");
    }
}
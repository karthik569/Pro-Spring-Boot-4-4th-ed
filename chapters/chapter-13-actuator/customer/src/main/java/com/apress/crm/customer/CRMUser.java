package com.apress.crm.customer;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "#this instanceof T(org.springframework.security.oauth2.core.oidc.user.OidcUser) ? attributes['email'] : name")
public @interface CRMUser {
}

package uk.co.bbr.web.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('BBR_MEMBER') or hasRole('BBR_PRO') or hasRole('BBR_ADMIN')")
public @interface IsBbrMember {
}

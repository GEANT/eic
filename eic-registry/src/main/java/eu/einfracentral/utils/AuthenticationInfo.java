package eu.einfracentral.utils;

import eu.einfracentral.exception.OIDCAuthenticationException;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthenticationInfo {

    private AuthenticationInfo() {
    }

    public static String getSub(Authentication auth) {
        return getOIDC(auth).getSub();
    }

    public static String getEmail(Authentication auth) {
        return getOIDC(auth).getUserInfo().getEmail();
    }

    public static String getName(Authentication auth) {
        return getOIDC(auth).getUserInfo().getName();
    }

    public static String getGivenName(Authentication auth) {
        return getOIDC(auth).getUserInfo().getGivenName();
    }

    public static String getFamilyName(Authentication auth) {
        return getOIDC(auth).getUserInfo().getFamilyName();
    }

    private static OIDCAuthenticationToken getOIDC(Authentication auth) {
        if (auth instanceof OIDCAuthenticationToken) {
            return ((OIDCAuthenticationToken) auth);
        } else {
            throw new OIDCAuthenticationException("Could not retrieve user details. Authentication is not an instance of OIDCAuthentication");
        }
    }
}

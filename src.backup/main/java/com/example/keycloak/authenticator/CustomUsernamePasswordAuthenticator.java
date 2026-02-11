package com.example.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;

public class CustomUsernamePasswordAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.challenge(
                context.form().createLoginUsernamePassword()
        );
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        MultivaluedMap<String, String> formData =
                context.getHttpRequest().getDecodedFormParameters();

        if (formData == null) {
            fail(context, AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        String username = formData.getFirst("username");
        String password = formData.getFirst("password");

        if (username == null || password == null ||
                username.isBlank() || password.isBlank()) {

            fail(context, AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        UserModel user = session.users().getUserByUsername(realm, username);

        if (user == null) {
            fail(context, AuthenticationFlowError.INVALID_USER);
            return;
        }

        if (!user.isEnabled()) {
            fail(context, AuthenticationFlowError.USER_DISABLED);
            return;
        }

        // ✅ UNIVERSAL SAFE PASSWORD VALIDATION
        CredentialInput credentialInput =
                UserCredentialModel.password(password);

        if (!(user instanceof CredentialInputValidator validator) ||
                !validator.isValid(realm, user, credentialInput)) {

            fail(context, AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        context.setUser(user);
        context.success();
    }

    private void fail(AuthenticationFlowContext context,
                      AuthenticationFlowError error) {

        context.failureChallenge(
                error,
                context.form().createLoginUsernamePassword()
        );
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session,
                                 RealmModel realm,
                                 UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session,
                                   RealmModel realm,
                                   UserModel user) {
    }

    @Override
    public void close() {
    }
}

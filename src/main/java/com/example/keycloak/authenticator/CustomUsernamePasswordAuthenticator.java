package com.example.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;


public class CustomUsernamePasswordAuthenticator implements Authenticator {

    private static final String LOGIN_FORM = "login.ftl";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // Show login form
        Response challenge = context.form().createForm(LOGIN_FORM);
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Get form data
        MultivaluedMap<String, String> formData =
                context.getHttpRequest().getDecodedFormParameters();

        String username = formData.getFirst(USERNAME_FIELD);
        String password = formData.getFirst(PASSWORD_FIELD);

        // Validate username
        if (username == null || username.trim().isEmpty()) {
            Response challenge = context.form()
                    .setError("missingUsernameMessage")
                    .createForm(LOGIN_FORM);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // Validate password
        if (password == null || password.trim().isEmpty()) {
            Response challenge = context.form()
                    .setError("missingPasswordMessage")
                    .createForm(LOGIN_FORM);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // Find user by username
        UserModel user = context.getSession().users()
                .getUserByUsername(context.getRealm(), username);

        if (user == null) {
            // User not found
            Response challenge = context.form()
                    .setError("invalidUserMessage")
                    .createForm(LOGIN_FORM);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        // Check if user is enabled
        if (!user.isEnabled()) {
            Response challenge = context.form()
                    .setError("accountDisabledMessage")
                    .createForm(LOGIN_FORM);
            context.failureChallenge(AuthenticationFlowError.USER_DISABLED, challenge);
            return;
        }

        // Validate password - use UserModel's credential manager
        boolean valid = user.credentialManager()
                .isValid(UserCredentialModel.password(password));

        if (!valid) {
            // Wrong password
            Response challenge = context.form()
                    .setError("invalidPasswordMessage")
                    .createForm(LOGIN_FORM);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        context.setUser(user);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
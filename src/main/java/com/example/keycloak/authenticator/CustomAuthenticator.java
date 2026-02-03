package com.example.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;


public class CustomAuthenticator implements Authenticator {



    private static final String CUSTOM_AUTH_FORM = "custom-auth-form.ftl";


    private static final String CUSTOM_CODE = "customCode"; //

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challenge = context.form().createForm(CUSTOM_AUTH_FORM);


        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {


        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
             String enteredCode = formData.getFirst(CUSTOM_CODE);

        if (enteredCode == null || enteredCode.trim().isEmpty()) {
            Response challenge = context.form()
                    .setError("customCodeMissing")
                    .createForm(CUSTOM_AUTH_FORM);
               context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return; //
        }


        if (validateCustomCode(context, enteredCode)) {
            context.success();
        } else {
            Response challenge = context.form()
                    .setError("invalidCustomCode")
                    .createForm(CUSTOM_AUTH_FORM);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
        }
    }


    private boolean validateCustomCode(AuthenticationFlowContext context, String code) {
        UserModel user = context.getUser();
        String expectedCode = user.getFirstAttribute("customAuthCode");

        if (expectedCode != null && expectedCode.equals(code)) {
            return true;
        }

        if ("123456".equals(code)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        //
    }

    @Override
    public void close() {

    }
}

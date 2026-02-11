package com.example.keycloak.requiredaction;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;

public class MobileNumberRequiredAction implements RequiredActionProvider {

    private static final String FORM_FIELD = "mobileNumber";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        UserModel user = context.getUser();

        if (user.getFirstAttribute("mobileNumber") == null) {
            user.addRequiredAction(
                    MobileNumberRequiredActionFactory.PROVIDER_ID
            );
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(
                context.form().createForm("mobile-number.ftl")
        );
    }

    @Override
    public void processAction(RequiredActionContext context) {

        MultivaluedMap<String, String> formData =
                context.getHttpRequest().getDecodedFormParameters();

        String mobileNumber = formData.getFirst(FORM_FIELD);

        if (mobileNumber == null || mobileNumber.isBlank()) {
            context.challenge(
                    context.form()
                            .setError("Mobile number is required")
                            .createForm("mobile-number.ftl")
            );
            return;
        }

        context.getUser().setSingleAttribute("mobileNumber", mobileNumber);
        context.success();
    }

    @Override
    public void close() {
    }
}

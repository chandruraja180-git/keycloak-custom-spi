package com.example.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import static org.mockito.Mockito.*;

class CustomUsernamePasswordAuthenticatorTest {

    private CustomUsernamePasswordAuthenticator authenticator;
    private AuthenticationFlowContext context;
    private UserModel user;
    private KeycloakSession session;
    private UserProvider users;
    private LoginFormsProvider form;
    private HttpRequest httpRequest;
    private RealmModel realm;
    private SubjectCredentialManager credentialManager;

    @BeforeEach
    void setUp() {
        authenticator = new CustomUsernamePasswordAuthenticator();

        // Mocks
        context = mock(AuthenticationFlowContext.class);
        user = mock(UserModel.class);
        session = mock(KeycloakSession.class);
        users = mock(UserProvider.class);
        form = mock(LoginFormsProvider.class);
        httpRequest = mock(HttpRequest.class);
        realm = mock(RealmModel.class);
        credentialManager = mock(SubjectCredentialManager.class);  // ADD THIS!

        // Mock context to return session, form, httpRequest, and realm
        when(context.getSession()).thenReturn(session);
        when(context.form()).thenReturn(form);
        when(context.getHttpRequest()).thenReturn(httpRequest);
        when(context.getRealm()).thenReturn(realm);  // ADD THIS!

        // Mock session to return user provider
        when(session.users()).thenReturn(users);

        // Mock form provider
        when(form.createForm(anyString())).thenReturn(mock(Response.class));
        when(form.setError(anyString())).thenReturn(form);

        // Default user behavior
        when(user.isEnabled()).thenReturn(true);
        when(user.credentialManager()).thenReturn(credentialManager);  // ADD THIS!
        when(users.getUserByUsername(any(), eq("testuser"))).thenReturn(user);
    }

    @Test
    void testSuccessfulAuthentication() {
        // Form data
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("username", "testuser");
        formData.add("password", "password123");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // Mock password validation - FIXED!
        when(credentialManager.isValid(any(UserCredentialModel.class))).thenReturn(true);

        authenticator.action(context);

        verify(context).setUser(user);
        verify(context).success();
    }

    @Test
    void testMissingUsername() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("username", "");
        formData.add("password", "password123");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        authenticator.action(context);

        verify(context).failureChallenge(eq(AuthenticationFlowError.INVALID_CREDENTIALS), any(Response.class));
    }

    @Test
    void testInvalidPassword() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("username", "testuser");
        formData.add("password", "wrongpassword");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // Mock password validation to return false - FIXED!
        when(credentialManager.isValid(any(UserCredentialModel.class))).thenReturn(false);

        authenticator.action(context);

        verify(context).failureChallenge(eq(AuthenticationFlowError.INVALID_CREDENTIALS), any(Response.class));
    }

    @Test
    void testDisabledUser() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("username", "testuser");
        formData.add("password", "password123");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(user.isEnabled()).thenReturn(false);

        authenticator.action(context);

        verify(context).failureChallenge(eq(AuthenticationFlowError.USER_DISABLED), any(Response.class));
    }
}
package com.example.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUsernamePasswordAuthenticatorTest {

    @Mock
    AuthenticationFlowContext context;

    @Mock
    KeycloakSession session;

    @Mock
    RealmModel realm;

    @Mock
    UserModel user;

    @InjectMocks
    CustomUsernamePasswordAuthenticator authenticator;

    @BeforeEach
    void setup() {
        when(context.getSession()).thenReturn(session);
        when(context.getRealm()).thenReturn(realm);
    }

    private void mockForm(String username, String password) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        if (username != null) formData.add("username", username);
        if (password != null) formData.add("password", password);

        var httpRequest = mock(org.keycloak.http.HttpRequest.class);
        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(context.getHttpRequest()).thenReturn(httpRequest);
    }

    @Test
    void testMissingUsername() {
        mockForm(null, "pass");

        authenticator.action(context);

        verify(context).failureChallenge(
                eq(AuthenticationFlowError.INVALID_CREDENTIALS),
                any(Response.class)
        );
    }

    @Test
    void testDisabledUser() {
        mockForm("user", "pass");

        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserByUsername(realm, "user")).thenReturn(user);
        when(user.isEnabled()).thenReturn(false);

        authenticator.action(context);

        verify(context).failureChallenge(
                eq(AuthenticationFlowError.USER_DISABLED),
                any(Response.class)
        );
    }

    @Test
    void testInvalidPassword() {
        mockForm("user", "wrong");

        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserByUsername(realm, "user")).thenReturn(user);
        when(user.isEnabled()).thenReturn(true);

        // ✅ Mock credentialManager from user
        var credentialManager = mock(UserCredentialManager.class);
        when(user.credentialManager()).thenReturn(credentialManager);
        when(credentialManager.isValid(any(), any())).thenReturn(false);

        authenticator.action(context);

        verify(context).failureChallenge(
                eq(AuthenticationFlowError.INVALID_CREDENTIALS),
                any(Response.class)
        );
    }

    @Test
    void testSuccessfulAuthentication() {
        mockForm("user", "pass");

        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserByUsername(realm, "user")).thenReturn(user);
        when(user.isEnabled()).thenReturn(true);

        var credentialManager = mock(UserCredentialManager.class);
        when(user.credentialManager()).thenReturn(credentialManager);
        when(credentialManager.isValid(any(), any())).thenRetur

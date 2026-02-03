# Keycloak Custom Authenticator - Deployment Guide
## For Keycloak 24.0.5

This guide will help you deploy and configure your custom authenticator in Keycloak.

---

## 📁 Project Structure

```
custom-authenticator/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/keycloak/authenticator/
│   │   │       ├── CustomAuthenticator.java
│   │   │       └── CustomAuthenticatorFactory.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── services/
│   │       │       └── org.keycloak.authentication.AuthenticatorFactory
│   │       └── theme/
│   │           └── base/
│   │               └── login/
│   │                   ├── custom-auth-form.ftl
│   │                   └── messages/
│   │                       └── messages_en.properties
```

---

## 🔧 Step 1: Set Up Your IDE Project

### Option A: Maven Project
1. Create a new Maven project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Copy the `pom.xml` file to your project root
3. Create the following directory structure:
   ```
   src/main/java/com/example/keycloak/authenticator/
   src/main/resources/META-INF/services/
   src/main/resources/theme/base/login/
   src/main/resources/theme/base/login/messages/
   ```

### Option B: Copy Files
1. Place `CustomAuthenticator.java` in: `src/main/java/com/example/keycloak/authenticator/`
2. Place `CustomAuthenticatorFactory.java` in: `src/main/java/com/example/keycloak/authenticator/`
3. Place `org.keycloak.authentication.AuthenticatorFactory` in: `src/main/resources/META-INF/services/`
4. Place `custom-auth-form.ftl` in: `src/main/resources/theme/base/login/`
5. Place `messages_en.properties` in: `src/main/resources/theme/base/login/messages/`

---

## 🏗️ Step 2: Build the Project

### Using Maven command line:
```bash
mvn clean package
```

### Using IDE:
- IntelliJ IDEA: Right-click on pom.xml → Maven → Reimport, then run Maven build
- Eclipse: Right-click on project → Run As → Maven install

This will create `custom-authenticator.jar` in the `target/` directory.

---

## 📦 Step 3: Deploy to Keycloak

### For Keycloak 24.0.5 (Quarkus distribution):

1. **Copy the JAR file:**
   ```bash
   cp target/custom-authenticator.jar /opt/keycloak/providers/
   ```
   
   Replace `/opt/keycloak/` with your actual Keycloak installation directory.

2. **Rebuild Keycloak (required for Quarkus version):**
   ```bash
   cd /opt/keycloak
   bin/kc.sh build
   ```

3. **Restart Keycloak:**
   ```bash
   bin/kc.sh start-dev
   # OR for production:
   bin/kc.sh start
   ```

---

## ⚙️ Step 4: Configure in Keycloak Admin Console

### 4.1 Log into Keycloak Admin Console
- URL: `http://localhost:8080/admin` (or your Keycloak URL)
- Use your admin credentials

### 4.2 Create a New Authentication Flow

1. Go to **Authentication** → **Flows** tab
2. Click **Create flow** button
3. Fill in:
   - **Name**: `Custom Browser Flow`
   - **Description**: `Browser flow with custom authenticator`
4. Click **Create**

### 4.3 Add Execution Steps

1. In your new flow, click **Add execution**
2. Add these executions in order:
   - **Cookie** (Alternative)
   - **Kerberos** (Disabled)
   - **Identity Provider Redirector** (Alternative)
   
3. Click **Add step** button
4. Select **Username Password Form** → Click **Add**
5. Set it to **Required**

6. Click **Add step** button again
7. Select **Custom Authentication Step** (this is your custom authenticator)
8. Set it to **Required**

### 4.4 Bind the Flow

1. Go to **Authentication** → **Bindings** tab
2. Select your **Custom Browser Flow** from the **Browser Flow** dropdown
3. Click **Save**

---

## 🧪 Step 5: Test the Authenticator

1. **Log out** from Keycloak admin console
2. Navigate to your application login (or `http://localhost:8080/realms/{your-realm}/account`)
3. Enter username and password
4. After successful password validation, you should see your custom authentication form
5. Enter the code: **123456** (default test code)
6. You should be logged in successfully

---

## 🔐 Customizing the Validation Logic

Edit the `validateCustomCode()` method in `CustomAuthenticator.java`:

### Example 1: Validate against user attribute
```java
private boolean validateCustomCode(AuthenticationFlowContext context, String code) {
    UserModel user = context.getUser();
    String expectedCode = user.getFirstAttribute("customAuthCode");
    return expectedCode != null && expectedCode.equals(code);
}
```

To set user attribute in Keycloak Admin:
1. Go to **Users** → Select user → **Attributes** tab
2. Add attribute: `customAuthCode` = `123456`

### Example 2: Generate and send OTP
```java
private boolean validateCustomCode(AuthenticationFlowContext context, String code) {
    UserModel user = context.getUser();
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    
    // Get or generate OTP
    String storedOtp = authSession.getAuthNote("CUSTOM_OTP");
    if (storedOtp == null) {
        storedOtp = generateOTP();
        authSession.setAuthNote("CUSTOM_OTP", storedOtp);
        sendOTPToUser(user, storedOtp); // Implement email/SMS sending
    }
    
    return storedOtp.equals(code);
}

private String generateOTP() {
    return String.format("%06d", new Random().nextInt(999999));
}
```

### Example 3: External API validation
```java
private boolean validateCustomCode(AuthenticationFlowContext context, String code) {
    UserModel user = context.getUser();
    
    // Call external API
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://your-api.com/validate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                String.format("{\"userId\":\"%s\",\"code\":\"%s\"}", 
                    user.getUsername(), code)))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    } catch (Exception e) {
        return false;
    }
}
```

---

## 🎨 Customizing the UI

Edit `custom-auth-form.ftl` to change the appearance:

```html
<!-- Add instructions -->
<div class="instruction-text">
    <p>Please enter the 6-digit code sent to your email</p>
</div>

<!-- Change input type -->
<input type="password" id="customCode" name="customCode" ... />

<!-- Add additional fields -->
<input type="text" id="deviceId" name="deviceId" ... />
```

Edit `messages_en.properties` for different languages:
- Create `messages_es.properties` for Spanish
- Create `messages_fr.properties` for French
- etc.

---

## 🐛 Troubleshooting

### Authenticator not appearing in Keycloak
1. Check that `org.keycloak.authentication.AuthenticatorFactory` file exists in `META-INF/services/`
2. Verify the file contains the correct factory class name
3. Ensure you ran `bin/kc.sh build` after copying the JAR
4. Check Keycloak logs: `logs/keycloak.log`

### Form not displaying
1. Verify `custom-auth-form.ftl` is in the JAR at `theme/base/login/`
2. Check browser console for errors
3. Ensure template name matches: `CUSTOM_AUTH_FORM = "custom-auth-form.ftl"`

### Build errors
1. Check Java version: Must be Java 17+
2. Verify Keycloak version in `pom.xml` matches your installation
3. Clear Maven cache: `mvn clean`

### Validation always fails
1. Add logging to `validateCustomCode()`:
   ```java
   System.out.println("Expected: " + expectedCode + ", Got: " + code);
   ```
2. Check Keycloak server logs for output
3. Verify user attributes are set correctly

---

## 📝 Additional Configuration Options

### Make authenticator conditional
```java
@Override
public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    // Only require for users with specific role
    return user.hasRole(realm.getRole("requires-custom-auth"));
}
```

### Add required action
```java
@Override
public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    // Force user to configure authenticator if not set
    if (user.getFirstAttribute("customAuthCode") == null) {
        user.addRequiredAction("CONFIGURE_CUSTOM_AUTH");
    }
}
```

---

## 📚 Resources

- Keycloak Documentation: https://www.keycloak.org/docs/24.0.5/
- Server Developer Guide: https://www.keycloak.org/docs/24.0.5/server_development/
- Authentication SPI: https://www.keycloak.org/docs/24.0.5/server_development/#_auth_spi

---

## ✅ Checklist

- [ ] Project structure created
- [ ] All files in correct locations
- [ ] Maven build successful (`mvn clean package`)
- [ ] JAR copied to `/opt/keycloak/providers/`
- [ ] Keycloak rebuilt (`bin/kc.sh build`)
- [ ] Keycloak restarted
- [ ] Custom flow created in admin console
- [ ] Executions added to flow
- [ ] Flow bound to browser authentication
- [ ] Test login successful

---

**Need help?** Check the troubleshooting section or review the Keycloak server logs at `/opt/keycloak/logs/keycloak.log`

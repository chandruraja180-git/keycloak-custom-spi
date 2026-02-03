<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true displayInfo=false; section>
    <#if section = "header">
        Additional Authentication Required
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form id="kc-custom-auth-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="customCode" class="${properties.kcLabelClass!}">
                            Enter your authentication code:
                        </label>
                        <input type="text" 
                               id="customCode" 
                               name="customCode" 
                               class="${properties.kcInputClass!}" 
                               autofocus 
                               autocomplete="off"
                               placeholder="Enter code here"
                        />
                        
                        <#if message?has_content && (message.type = 'error')>
                            <span class="${properties.kcInputErrorMessageClass!}">
                                <#if message.summary == 'customCodeMissing'>
                                    Please enter the authentication code.
                                <#elseif message.summary == 'invalidCustomCode'>
                                    Invalid authentication code. Please try again.
                                <#else>
                                    ${message.summary}
                                </#if>
                            </span>
                        </#if>
                    </div>

                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" 
                               type="submit" 
                               value="Submit"
                        />
                    </div>
                </form>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>

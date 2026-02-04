<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=true; section>
    <#if section == "header">
        ${msg("updateMobileNumberTitle")}
    <#elseif section == "form">

        <form id="kc-mobile-form"
              class="${properties.kcFormClass!}"
              action="${url.loginAction}"
              method="post">

            <div class="${properties.kcFormGroupClass!}">
                <label for="mobileNumber" class="${properties.kcLabelClass!}">
                    ${msg("mobileNumber")}
                </label>

                <input type="text"
                       id="mobileNumber"
                       name="mobileNumber"
                       class="${properties.kcInputClass!}"
                       value=""
                       required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <input type="submit"
                       class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!}"
                       value="${msg("doSubmit")}" />
            </div>

        </form>

    </#if>
</@layout.registrationLayout>

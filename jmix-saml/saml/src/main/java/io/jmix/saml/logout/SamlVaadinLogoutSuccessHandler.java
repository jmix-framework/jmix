/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.saml.logout;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.spring.security.UidlRedirectStrategy;
import io.jmix.core.common.util.Preconditions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.logout.HttpSessionLogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A {@link LogoutSuccessHandler} that issues a SAML 2.0 LogoutRequest to the asserting party and is aware of
 * Vaadin UIDL requests. It is used instead of {@link Saml2RelyingPartyInitiatedLogoutSuccessHandler} because:
 * <ul>
 *     <li>Vaadin {@code AuthenticationContext.logout()} invokes the logout success handler within a UIDL (XHR)
 *     request where a plain HTTP redirect does not navigate the browser. This handler uses
 *     {@link UidlRedirectStrategy} which performs a client-side page location change for UIDL requests and falls
 *     back to a regular redirect otherwise.</li>
 *     <li>{@code Saml2LogoutResponseFilter} invokes the logout success handler with {@code null} authentication
 *     after processing the LogoutResponse from the asserting party. This handler redirects to
 *     {@link #logoutSuccessUrl} in that case instead of responding with 401.</li>
 * </ul>
 */
public class SamlVaadinLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger log = getLogger(SamlVaadinLogoutSuccessHandler.class);

    protected final Saml2LogoutRequestResolver logoutRequestResolver;

    protected Saml2LogoutRequestRepository logoutRequestRepository = new HttpSessionLogoutRequestRepository();

    protected RedirectStrategy redirectStrategy = new UidlRedirectStrategy();

    protected String logoutSuccessUrl = "/";

    public SamlVaadinLogoutSuccessHandler(Saml2LogoutRequestResolver logoutRequestResolver) {
        Preconditions.checkNotNullArgument(logoutRequestResolver, "logoutRequestResolver cannot be null");
        this.logoutRequestResolver = logoutRequestResolver;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                @Nullable Authentication authentication) throws IOException {
        Saml2LogoutRequest logoutRequest = authentication == null
                ? null
                : logoutRequestResolver.resolve(request, authentication);
        if (logoutRequest == null) {
            // Either the logout has already been completed on the asserting party side (the LogoutResponse
            // callback runs with cleared authentication), or single logout is not available for the
            // registration. Finish the local logout with a redirect instead of an error.
            log.debug("No SAML logout request generated, redirecting to '{}'", logoutSuccessUrl);
            redirectStrategy.sendRedirect(request, response, logoutSuccessUrl);
            return;
        }
        logoutRequestRepository.saveLogoutRequest(logoutRequest, request, response);
        if (logoutRequest.getBinding() == Saml2MessageBinding.REDIRECT) {
            doRedirect(request, response, logoutRequest);
        } else {
            doPost(request, response, logoutRequest);
        }
    }

    /**
     * Sets the URL to redirect to when the SAML LogoutRequest cannot be generated or the single logout has
     * completed. The URL is resolved against the servlet context path for regular requests; for UIDL requests
     * it is passed to the browser as is, so use an absolute URL if the application is deployed under a
     * non-root context path.
     */
    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        Preconditions.checkNotNullArgument(logoutSuccessUrl, "logoutSuccessUrl cannot be null");
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    /**
     * Sets the {@link Saml2LogoutRequestRepository} for saving the SAML LogoutRequest. It must match the
     * repository used by {@code Saml2LogoutResponseFilter} to validate the LogoutResponse.
     */
    public void setLogoutRequestRepository(Saml2LogoutRequestRepository logoutRequestRepository) {
        Preconditions.checkNotNullArgument(logoutRequestRepository, "logoutRequestRepository cannot be null");
        this.logoutRequestRepository = logoutRequestRepository;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        Preconditions.checkNotNullArgument(redirectStrategy, "redirectStrategy cannot be null");
        this.redirectStrategy = redirectStrategy;
    }

    protected void doRedirect(HttpServletRequest request, HttpServletResponse response,
                              Saml2LogoutRequest logoutRequest) throws IOException {
        String location = logoutRequest.getLocation();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(location)
                .query(logoutRequest.getParametersQuery());
        redirectStrategy.sendRedirect(request, response, uriBuilder.build(true).toUriString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response,
                          Saml2LogoutRequest logoutRequest) throws IOException {
        if (isUidlRequest(request)) {
            // An auto-submitting HTML form cannot be rendered into a UIDL response, so the LogoutRequest
            // cannot be delivered to the asserting party from a Vaadin UI request.
            log.warn("SAML logout request for registration '{}' uses POST binding which cannot be delivered "
                            + "from a Vaadin UI request. The user is redirected to '{}' without notifying the "
                            + "identity provider. Enable 'jmix.saml.force-redirect-binding-logout' if the identity "
                            + "provider supports the redirect binding for single logout",
                    logoutRequest.getRelyingPartyRegistrationId(), logoutSuccessUrl);
            redirectStrategy.sendRedirect(request, response, logoutSuccessUrl);
            return;
        }
        String location = logoutRequest.getLocation();
        String saml = logoutRequest.getSamlRequest();
        String relayState = logoutRequest.getRelayState();
        String html = createSamlPostRequestFormData(location, saml, relayState);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.getWriter().write(html);
    }

    protected boolean isUidlRequest(HttpServletRequest request) {
        String servletMapping = request.getHttpServletMapping().getPattern();
        return HandlerHelper.isFrameworkInternalRequest(servletMapping, request);
    }

    /**
     * Renders the same auto-submitting form as
     * {@link Saml2RelyingPartyInitiatedLogoutSuccessHandler} does for the POST binding.
     */
    protected String createSamlPostRequestFormData(String location, String saml, @Nullable String relayState) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n").append("    <head>\n");
        html.append("        <meta http-equiv=\"Content-Security-Policy\" ")
                .append("content=\"script-src 'sha256-oZhLbc2kO8b8oaYLrUc7uye1MgVKMyLtPqWR4WtKF+c='\">\n");
        html.append("        <meta charset=\"utf-8\" />\n");
        html.append("    </head>\n");
        html.append("    <body>\n");
        html.append("        <noscript>\n");
        html.append("            <p>\n");
        html.append("                <strong>Note:</strong> Since your browser does not support JavaScript,\n");
        html.append("                you must press the Continue button once to proceed.\n");
        html.append("            </p>\n");
        html.append("        </noscript>\n");
        html.append("        \n");
        html.append("        <form action=\"");
        html.append(location);
        html.append("\" method=\"post\">\n");
        html.append("            <div>\n");
        html.append("                <input type=\"hidden\" name=\"SAMLRequest\" value=\"");
        html.append(HtmlUtils.htmlEscape(saml));
        html.append("\"/>\n");
        if (StringUtils.hasText(relayState)) {
            html.append("                <input type=\"hidden\" name=\"RelayState\" value=\"");
            html.append(HtmlUtils.htmlEscape(relayState));
            html.append("\"/>\n");
        }
        html.append("            </div>\n");
        html.append("            <noscript>\n");
        html.append("                <div>\n");
        html.append("                    <input type=\"submit\" value=\"Continue\"/>\n");
        html.append("                </div>\n");
        html.append("            </noscript>\n");
        html.append("        </form>\n");
        html.append("        \n");
        html.append("        <script>window.onload = function() { document.forms[0].submit(); }</script>\n");
        html.append("    </body>\n");
        html.append("</html>");
        return html.toString();
    }
}

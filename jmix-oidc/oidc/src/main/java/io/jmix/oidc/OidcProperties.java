package io.jmix.oidc;

import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.oidc")
public class OidcProperties {

    /**
     * Whether default OIDC configuration should be used. The property is used by OidcAutoConfiguration.
     */
    boolean useDefaultConfiguration;

    /**
     * Post logout redirect uri template for OIDC logout. The property is used by OidcAutoConfiguration.
     */
    String postLogoutRedirectUri;

    /**
     * DefaultClaimsRolesMapper configuration.
     */
    DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper;

    /**
     * JWT authentication converter configuration.
     */
    JwtAuthenticationConverterConfig jwtAuthenticationConverter;

    /**
     * Set of properties to configure Security filter chains
     */
    FilterChain filterChain;

    public OidcProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            @DefaultValue("{baseUrl}") String postLogoutRedirectUri,
            @DefaultValue DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper,
            @DefaultValue JwtAuthenticationConverterConfig jwtAuthenticationConverter,
            @DefaultValue FilterChain filterChain
    ) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
        this.defaultClaimsRolesMapper = defaultClaimsRolesMapper;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.filterChain = filterChain;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public DefaultClaimsRolesMapperConfig getDefaultClaimsRolesMapper() {
        return defaultClaimsRolesMapper;
    }

    public JwtAuthenticationConverterConfig getJwtAuthenticationConverter() {
        return jwtAuthenticationConverter;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public static class DefaultClaimsRolesMapperConfig {
        /**
         * Prefix that can be used to recognize a resource role from the authorization server
         */
        String resourceRolePrefix;

        /**
         * Prefix that can be used to recognize a row-level role from the authorization server
         */
        String rowLevelRolePrefix;

        /**
         * OidcUser claim name that stores a collection of roles names
         */
        String rolesClaimName;

        public DefaultClaimsRolesMapperConfig(
                @DefaultValue("roles") String rolesClaimName,
                @DefaultValue("") String resourceRolePrefix,
                @DefaultValue("") String rowLevelRolePrefix
        ) {
            this.resourceRolePrefix = resourceRolePrefix;
            this.rowLevelRolePrefix = rowLevelRolePrefix;
            this.rolesClaimName = rolesClaimName;
        }

        public String getResourceRolePrefix() {
            return resourceRolePrefix;
        }

        public String getRowLevelRolePrefix() {
            return rowLevelRolePrefix;
        }

        public String getRolesClaimName() {
            return rolesClaimName;
        }
    }

    public static class JwtAuthenticationConverterConfig {

        /**
         * JWT claim name that is used as a username of Jmix user
         */
        private String usernameClaim;

        public JwtAuthenticationConverterConfig(
                @DefaultValue("sub") String usernameClaim) {
            this.usernameClaim = usernameClaim;
        }

        public String getUsernameClaim() {
            return usernameClaim;
        }
    }

    public static class FilterChain {

        /**
         * Whether the forced API scope is enabled for Security filter chains provided via {@link #apiScopeSecurityFilterChainNames}.
         */
        boolean forceApiScopeEnabled;

        /**
         * Whether the forced UI scope is enabled for Security filter chains provided via {@link #uiScopeSecurityFilterChainNames}.
         */
        boolean forceUiScopeEnabled;

        /**
         * Represents a list of security filter chain names which should be customized
         * by {@link io.jmix.oidc.filter.OidcResourceServerSecurityFilterChainCustomizer}.
         *
         * @see #forceApiScopeEnabled
         */
        List<String> apiScopeSecurityFilterChainNames;

        /**
         * Represents a list of security filter chain names which should be customized
         * by {@link io.jmix.oidc.filter.OidcVaadinSecurityFilterChainCustomizer}.
         *
         * @see #forceUiScopeEnabled
         */
        List<String> uiScopeSecurityFilterChainNames;

        public FilterChain(@DefaultValue("true") boolean forceApiScopeEnabled,
                           @DefaultValue("true") boolean forceUiScopeEnabled,
                           List<String> apiScopeSecurityFilterChainNames,
                           List<String> uiScopeSecurityFilterChainNames) {
            this.forceApiScopeEnabled = forceApiScopeEnabled;
            this.apiScopeSecurityFilterChainNames = ListUtils.emptyIfNull(apiScopeSecurityFilterChainNames);
            this.forceUiScopeEnabled = forceUiScopeEnabled;
            this.uiScopeSecurityFilterChainNames = ListUtils.emptyIfNull(uiScopeSecurityFilterChainNames);
        }

        /**
         * @see #forceApiScopeEnabled
         */
        public boolean isForceApiScopeEnabled() {
            return forceApiScopeEnabled;
        }

        /**
         * @see #forceUiScopeEnabled
         */
        public boolean isForceUiScopeEnabled() {
            return forceUiScopeEnabled;
        }

        /**
         * @see #apiScopeSecurityFilterChainNames
         */
        public List<String> getApiScopeSecurityFilterChainNames() {
            return apiScopeSecurityFilterChainNames;
        }

        /**
         * @see #uiScopeSecurityFilterChainNames
         */
        public List<String> getUiScopeSecurityFilterChainNames() {
            return uiScopeSecurityFilterChainNames;
        }
    }
}
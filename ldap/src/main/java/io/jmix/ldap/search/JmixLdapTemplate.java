package io.jmix.ldap.search;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.util.Assert;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extension of Spring LDAP's LdapTemplate class which adds extra functionality required by Jmix.
 */
public class JmixLdapTemplate extends LdapTemplate {

    private SearchControls searchControls = new SearchControls();

    private static final boolean RETURN_OBJECT = true;

    public JmixLdapTemplate(ContextSource contextSource) {
        super(contextSource);
    }

    public Set<DirContextOperations> searchForMultipleEntries(String base, String filter, Object[] params) {
        return executeReadOnly((ctx) ->
                searchForMultipleEntriesInternal(ctx, this.searchControls, base, filter, params));
    }

    public static Set<DirContextOperations> searchForMultipleEntriesInternal(DirContext ctx, SearchControls searchControls,
                                                                             String base, String filter, Object[] params) throws NamingException {
        final DistinguishedName searchBaseDn = new DistinguishedName(base);
        final NamingEnumeration<SearchResult> resultsEnum = ctx.search(searchBaseDn, filter, params,
                buildControls(searchControls));
        Set<DirContextOperations> results = new HashSet<>();
        try {
            while (resultsEnum.hasMore()) {
                SearchResult searchResult = resultsEnum.next();
                DirContextAdapter dca = (DirContextAdapter) searchResult.getObject();
                Assert.notNull(dca, "No object returned by search, DirContext is not correctly configured");
                results.add(dca);
            }
        } catch (PartialResultException ex) {
            LdapUtils.closeEnumeration(resultsEnum);
        }
        if (results.size() == 0) {
            return Collections.emptySet();
        }
        return results;
    }

    private static SearchControls buildControls(SearchControls originalControls) {
        return new SearchControls(originalControls.getSearchScope(), originalControls.getCountLimit(),
                originalControls.getTimeLimit(), originalControls.getReturningAttributes(), RETURN_OBJECT,
                originalControls.getDerefLinkFlag());
    }

    public void setSearchControls(SearchControls searchControls) {
        this.searchControls = searchControls;
    }
}

package io.jmix.saml.converter;

import io.jmix.saml.user.DefaultJmixSamlUserDetails;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.*;
import org.opensaml.saml.saml2.core.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.provider.service.authentication.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SamlResponseAuthenticationConverter implements Converter<OpenSamlAuthenticationProvider.ResponseToken, Saml2Authentication> {

    @Override
    public Saml2Authentication convert(OpenSamlAuthenticationProvider.ResponseToken responseToken) {
        Response response = responseToken.getResponse();
        Saml2AuthenticationToken token = responseToken.getToken();
        Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
        if (assertion == null) {
            throw new IllegalStateException("SAML response doesn't contain assertions");
        }
        Subject subject = assertion.getSubject();
        String username = subject.getNameID().getValue();
        Map<String, List<Object>> attributes = getAssertionAttributes(assertion);
        DefaultSaml2AuthenticatedPrincipal delegatePrincipal = new DefaultSaml2AuthenticatedPrincipal(username, attributes);
        DefaultJmixSamlUserDetails principal = new DefaultJmixSamlUserDetails(delegatePrincipal, AuthorityUtils.createAuthorityList("ROLE_USER"));
        return new Saml2Authentication(principal,
                token.getSaml2Response(), AuthorityUtils.createAuthorityList("ROLE_USER"));
    }

    /**
     * Copied from {@link OpenSaml4AuthenticationProvider}
     */
    private Map<String, List<Object>> getAssertionAttributes(Assertion assertion) {
        Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                List<Object> attributeValues = new ArrayList<>();
                for (XMLObject xmlObject : attribute.getAttributeValues()) {
                    Object attributeValue = getXmlObjectValue(xmlObject);
                    if (attributeValue != null) {
                        attributeValues.add(attributeValue);
                    }
                }
                attributeMap.put(attribute.getName(), attributeValues);
            }
        }
        return attributeMap;
    }

    /**
     * Copied from {@link OpenSaml4AuthenticationProvider}
     */
    private Object getXmlObjectValue(XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        }
        if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        }
        if (xmlObject instanceof XSInteger) {
            return ((XSInteger) xmlObject).getValue();
        }
//        if (xmlObject instanceof XSURI) {
//            return ((XSURI) xmlObject).getURI();
//        }
        if (xmlObject instanceof XSBoolean) {
            XSBooleanValue xsBooleanValue = ((XSBoolean) xmlObject).getValue();
            return (xsBooleanValue != null) ? xsBooleanValue.getValue() : null;
        }
        if (xmlObject instanceof XSDateTime) {
            return ((XSDateTime) xmlObject).getValue();
        }
        return null;
    }

}

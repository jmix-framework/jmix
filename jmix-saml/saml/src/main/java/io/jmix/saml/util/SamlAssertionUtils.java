package io.jmix.saml.util;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.*;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class that provides some methods for working with SAML assertions.
 */
public class SamlAssertionUtils {

    /**
     * Extracts all attributes from the assertion.
     *
     * @param assertion SAML assertion
     * @return map of attribute names to attribute values
     */
    public static Map<String, List<Object>> getAssertionAttributes(Assertion assertion) {
        Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                String attributeName = attribute.getName();
                List<Object> attributeValues = attributeMap.computeIfAbsent(attributeName, k -> new ArrayList<>());
                for (XMLObject xmlObject : attribute.getAttributeValues()) {
                    Object attributeValue = getXmlObjectValue(xmlObject);
                    if (attributeValue != null) {
                        attributeValues.add(attributeValue);
                    }
                }
                attributeMap.put(attributeName, attributeValues);
            }
        }
        return attributeMap;
    }

    /**
     * Extracts session indexes from authentication statements of the assertion. Session indexes identify the user
     * session on the identity provider and must be sent back in the {@code LogoutRequest} during single logout.
     *
     * @param assertion SAML assertion
     * @return list of session indexes
     */
    public static List<String> getSessionIndexes(Assertion assertion) {
        List<String> sessionIndexes = new ArrayList<>();
        for (AuthnStatement authnStatement : assertion.getAuthnStatements()) {
            String sessionIndex = authnStatement.getSessionIndex();
            if (sessionIndex != null) {
                sessionIndexes.add(sessionIndex);
            }
        }
        return sessionIndexes;
    }

    /**
     * Returns username from the assertion. The username is taken from the plain {@code NameID} of the assertion
     * subject.
     *
     * @param assertion SAML assertion
     * @return username, or null if the assertion contains no subject, no plain {@code NameID} (e.g. the identity
     * provider releases an {@code EncryptedID}), or the {@code NameID} has no value
     */
    @Nullable
    public static String getUsername(Assertion assertion) {
        return Optional.ofNullable(getNameId(assertion))
                .map(NameID::getValue)
                .orElse(null);
    }

    /**
     * Returns the plain {@code NameID} of the assertion subject.
     *
     * @param assertion SAML assertion
     * @return NameID, or null if the assertion contains no subject or no plain {@code NameID}
     */
    @Nullable
    public static NameID getNameId(Assertion assertion) {
        return Optional.ofNullable(assertion.getSubject())
                .map(Subject::getNameID)
                .orElse(null);
    }

    /**
     * Returns the first value of the given assertion attribute.
     *
     * @param assertion     SAML assertion
     * @param attributeName name of the assertion attribute
     * @return first attribute value converted to string, or null if the attribute is absent or has no values
     */
    @Nullable
    public static String getFirstAttributeValue(Assertion assertion, String attributeName) {
        List<Object> values = getAssertionAttributes(assertion).get(attributeName);
        return (values == null || values.isEmpty()) ? null : values.get(0).toString();
    }

    @Nullable
    protected static Object getXmlObjectValue(XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        }
        if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        }
        if (xmlObject instanceof XSInteger) {
            return ((XSInteger) xmlObject).getValue();
        }
        if (xmlObject instanceof XSURI) {
            return ((XSURI) xmlObject).getURI();
        }
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

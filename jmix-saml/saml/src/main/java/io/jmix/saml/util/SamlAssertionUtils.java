package io.jmix.saml.util;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.*;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * Returns username from the assertion.
     *
     * @param assertion SAML assertion
     * @return username
     */
    @Nullable
    public static String getUsername(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
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

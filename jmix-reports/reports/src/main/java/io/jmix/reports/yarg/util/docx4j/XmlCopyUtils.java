/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.util.docx4j;

import jakarta.xml.bind.*;
import jakarta.xml.bind.util.JAXBSource;
import org.xlsx4j.sml.Cell;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.namespace.QName;

public class XmlCopyUtils {

    private final static String cellName;

    static {
        if (org.docx4j.XmlUtils.getTransformerFactory() != null) {
            cellName = Cell.class.getSimpleName();
        } else {
            cellName = Cell.class.getSimpleName();
        }
    }

    private XmlCopyUtils() {
    }

    public static Marshaller createMarshaller(JAXBContext context) {
        try {
            return context.createMarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Unmarshaller createUnmarshaller(JAXBContext context) {
        try {
            return context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Cell copyCell(Cell cell, Unmarshaller unmarshaller, Marshaller marshaller) {
        try {
            JAXBElement<Cell> contentObject = new JAXBElement<>(new QName(cellName), Cell.class, cell);
            JAXBSource source = new JAXBSource(marshaller, contentObject);
            source.setXMLReader(new PseudoXMLReader(marshaller, contentObject));
            JAXBElement<Cell> elem = unmarshaller.unmarshal(source, Cell.class);
            return elem.getValue();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class PseudoXMLReader implements XMLReader {
        private Marshaller marshaller;
        private Object contentObject;

        private LexicalHandler lexicalHandler;
        // we will store this value but never use it by ourselves.
        private EntityResolver entityResolver;
        // SAX allows ContentHandler to be changed during the parsing,
        // but JAXB doesn't. So this repeater will sit between those
        // two components.
        private XMLFilter repeater = new XMLFilterImpl();
        private DTDHandler dtdHandler;
        private ErrorHandler errorHandler;

        public PseudoXMLReader(Marshaller marshaller, Object contentObject) {
            this.marshaller = marshaller;
            this.contentObject = contentObject;
        }

        public boolean getFeature(String name) throws SAXNotRecognizedException {
            if (name.equals("http://xml.org/sax/features/namespaces")) {
                return true;
            }
            if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
                return false;
            }
            if (name.equals("http://xml.org/sax/features/string-interning")) {
                return true;
            }
            throw new SAXNotRecognizedException(name);
        }

        public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
            if (name.equals("http://xml.org/sax/features/namespaces") && value)
                return;
            if (name.equals("http://xml.org/sax/features/namespace-prefixes") && !value)
                return;
            if (name.equals("http://xml.org/sax/features/string-interning")) {
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        public Object getProperty(String name) throws SAXNotRecognizedException {
            if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
                return lexicalHandler;
            }
            throw new SAXNotRecognizedException(name);
        }

        public void setProperty(String name, Object value) throws SAXNotRecognizedException {
            if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
                this.lexicalHandler = (LexicalHandler) value;
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        public void setEntityResolver(EntityResolver resolver) {
            this.entityResolver = resolver;
        }

        public EntityResolver getEntityResolver() {
            return entityResolver;
        }

        public void setDTDHandler(DTDHandler handler) {
            this.dtdHandler = handler;
        }

        public DTDHandler getDTDHandler() {
            return dtdHandler;
        }

        public void setContentHandler(ContentHandler handler) {
            repeater.setContentHandler(handler);
        }

        public ContentHandler getContentHandler() {
            return repeater.getContentHandler();
        }

        public void setErrorHandler(ErrorHandler handler) {
            this.errorHandler = handler;
        }

        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        public void parse(InputSource input) throws SAXException {
            parse();
        }

        public void parse(String systemId) throws SAXException {
            parse();
        }

        public void parse() throws SAXException {
            // parses a content object by using the given marshaller
            // SAX events will be sent to the repeater, and the repeater
            // will further forward it to an appropriate component.
            try {
                marshaller.marshal(contentObject, (XMLFilterImpl) repeater);
            } catch (JAXBException e) {
                // wrap it to a SAXException
                SAXParseException se =
                        new SAXParseException(e.getMessage(),
                                null, null, -1, -1, e);

                // if the consumer sets an error handler, it is our responsibility
                // to notify it.
                if (errorHandler != null)
                    errorHandler.fatalError(se);

                // this is a fatal error. Even if the error handler
                // returns, we will abort anyway.
                throw se;
            }
        }
    }
}

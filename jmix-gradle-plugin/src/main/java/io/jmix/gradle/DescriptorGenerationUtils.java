/*
 * Copyright 2020 Haulmont.
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

package io.jmix.gradle;

import com.google.common.base.Strings;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.*;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.gradle.MetaModelUtil.*;

public class DescriptorGenerationUtils {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DescriptorGenerationUtils.class);

    private static final String ORM_XMLNS = "http://xmlns.jcp.org/xml/ns/persistence/orm";
    private static final String ORM_SCHEMA_LOCATION = ORM_XMLNS + " http://xmlns.jcp.org/xml/ns/persistence/orm_2_2.xsd";

    private static final String PERSISTENCE_XMLNS = "http://xmlns.jcp.org/xml/ns/persistence";
    private static final String PERSISTENCE_SCHEMA_LOCATION = PERSISTENCE_XMLNS + " http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd";

    private static final String PERSISTENCE_VER = "2.2";

    public static final String ONE_TO_ONE_ANNOTATION = "javax.persistence.OneToOne";
    public static final String ONE_TO_MANY_ANNOTATION = "javax.persistence.OneToMany";
    public static final String MANY_TO_ONE_ANNOTATION = "javax.persistence.ManyToOne";
    public static final String MANY_TO_MANY_ANNOTATION = "javax.persistence.ManyToMany";

    static final String CONVERTERS_LIST_PROPERTY = "io.jmix.enhancing.converters-list";

    public static File constructPersistenceXml(String persistenceFileName,
                                               String storeName,
                                               String ormRelativeFileName,
                                               Set<String> jpaEntitiesAndConverters,
                                               Set<String> converters) {
        File file = new File(persistenceFileName);
        file.getParentFile().mkdirs();

        Document doc = DocumentHelper.createDocument();
        Element rootEl = doc.addElement("persistence", PERSISTENCE_XMLNS);
        Namespace xsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootEl.add(xsi);
        rootEl.addAttribute(new QName("schemaLocation", xsi), PERSISTENCE_SCHEMA_LOCATION);
        rootEl.addAttribute("version", PERSISTENCE_VER);

        Element persistenceUnit = rootEl.addElement("persistence-unit");
        persistenceUnit.addAttribute("name", storeName);
        persistenceUnit.addElement("provider").addText("io.jmix.data.impl.JmixPersistenceProvider");
        persistenceUnit.addElement("mapping-file").addText(ormRelativeFileName);

        for (String name : jpaEntitiesAndConverters) {
            persistenceUnit.addElement("class").addText(name);
        }

        persistenceUnit.addElement("exclude-unlisted-classes");

        Element properties = persistenceUnit.addElement("properties");

        properties.addElement("property")
                .addAttribute("name", "eclipselink.weaving")
                .addAttribute("value", "static");

        properties.addElement("property")
                .addAttribute("name", CONVERTERS_LIST_PROPERTY)
                .addAttribute("value", String.join(";", converters));

        writeDocument(doc, file);

        return file;

    }


    public static File constructOrmXml(String fileName, Set<String> mappedStoreClasses, ClassPool classPool) throws NotFoundException, BadBytecode {

        List<CtClass> persistentClasses = new ArrayList<>();
        Map<CtClass, CtClass> extendedClasses = new HashMap<>();

        findExtendedClasses(mappedStoreClasses, classPool, persistentClasses, extendedClasses);


        Map<CtClass, List<Attr>> mappings = new LinkedHashMap<>();

        for (CtClass aClass : persistentClasses) {
            List<Attr> attrList = processClass(aClass, extendedClasses, classPool);
            if (!attrList.isEmpty())
                mappings.put(aClass, attrList);
        }

        LOG.debug("Found " + mappings.size() + " entities containing extended associations");
        return createOrmFile(mappings, fileName);

    }

    protected static void findExtendedClasses(Set<String> mappedStoreClasses, ClassPool classPool, List<CtClass> persistentClasses, Map<CtClass, CtClass> extendedClasses) throws NotFoundException {
        Map<CtClass, CtClass> foundExtensions = new HashMap<>();

        for (String className : mappedStoreClasses) {
            CtClass aClass = classPool.get(className);

            persistentClasses.add(aClass);

            String replacedEntity = findReplacedEntity(aClass);

            if (replacedEntity != null) {
                foundExtensions.put(classPool.get(replacedEntity), aClass);
            }
        }

        for (Map.Entry<CtClass, CtClass> mappingEntry : foundExtensions.entrySet()) {
            CtClass originalClass = mappingEntry.getKey();
            CtClass extClass = mappingEntry.getValue();
            CtClass lastExtClass = null;
            CtClass aClass = foundExtensions.get(extClass);
            while (aClass != null) {
                lastExtClass = aClass;
                aClass = foundExtensions.get(aClass);
            }
            if (lastExtClass != null) {
                extendedClasses.put(originalClass, lastExtClass);
            } else {
                extendedClasses.put(originalClass, extClass);
            }
        }
    }

    protected static File createOrmFile(Map<CtClass, List<Attr>> mappings, String fileName) {
        Document doc = createEmptyDocument();

        Element rootEl = doc.getRootElement();

        for (Map.Entry<CtClass, List<Attr>> entry : mappings.entrySet()) {
            if (isJpaMappedSuperclass(entry.getKey())) {
                Element entityEl = rootEl.addElement("mapped-superclass", ORM_XMLNS);
                entityEl.addAttribute("class", entry.getKey().getName());
                createAttributes(entry, entityEl);
            }
        }


        for (Map.Entry<CtClass, List<Attr>> entry : mappings.entrySet()) {
            if (isJpaEntity(entry.getKey())) {
                Element entityEl = rootEl.addElement("entity", ORM_XMLNS);
                entityEl.addAttribute("class", entry.getKey().getName());

                AnnotationsAttribute attribute = (AnnotationsAttribute) entry.getKey().getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
                Annotation annotation = attribute.getAnnotation(ENTITY_ANNOTATION_TYPE);
                StringMemberValue memberValue = (StringMemberValue) annotation.getMemberValue("name");
                entityEl.addAttribute("name",
                        memberValue != null ? memberValue.getValue() : entry.getKey().getSimpleName());


                createAttributes(entry, entityEl);
            }
        }

        for (Map.Entry<CtClass, List<Attr>> entry : mappings.entrySet()) {
            if (isJpaEmbeddable(entry.getKey())) {
                Element entityEl = rootEl.addElement("embeddable", ORM_XMLNS);
                entityEl.addAttribute("class", entry.getKey().getName());
                createAttributes(entry, entityEl);
            }
        }

        File ormFile = new File(fileName);

        ormFile.getParentFile().mkdirs();

        writeDocument(doc, ormFile);

        return ormFile;
    }

    private static void writeDocument(Document doc, File file) {
        LOG.info("Creating file " + file);
        try (OutputStream os = new FileOutputStream(file)) {
            OutputFormat format = OutputFormat.createPrettyPrint();
            new XMLWriter(os, format).write(doc);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + file.getName(), e);
        }
    }

    private static void createAttributes(Map.Entry<CtClass, List<Attr>> entry, Element entityEl) {
        Element attributesEl = entityEl.addElement("attributes", ORM_XMLNS);
        Collections.sort(entry.getValue(), Comparator.comparingInt(a -> a.type.order));
        for (Attr attr : entry.getValue()) {
            attr.toXml(attributesEl);
        }
    }

    private static Document createEmptyDocument() {
        Document doc = DocumentHelper.createDocument();
        Element rootEl = doc.addElement("entity-mappings", ORM_XMLNS);
        Namespace xsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootEl.add(xsi);
        rootEl.addAttribute(new QName("schemaLocation", xsi), ORM_SCHEMA_LOCATION);
        rootEl.addAttribute("version", PERSISTENCE_VER);
        return doc;
    }

    private static List<Attr> processClass(CtClass aClass, Map<CtClass, CtClass> extendedClasses, ClassPool classPool) throws NotFoundException, BadBytecode {
        List<Attr> list = new ArrayList<>();

        for (CtField field : aClass.getDeclaredFields()) {
            Attr.Type type = getAttrType(field);
            if (type != null) {
                CtClass fieldType = field.getType();
                CtClass extClass = null;
                if (isCollection(field)) {
                    String genericSignature = field.getGenericSignature();

                    SignatureAttribute.ClassType objectType = (SignatureAttribute.ClassType) SignatureAttribute.toFieldSignature(genericSignature);

                    if (objectType.getTypeArguments().length == 1) {
                        String argType = objectType.getTypeArguments()[0].getType().toString();
                        extClass = extendedClasses.get(classPool.get(argType));
                    }
                } else {
                    extClass = extendedClasses.get(fieldType);
                }
                if (extClass != null) {
                    Attr attr = new Attr(type, field, extClass.getName());
                    attr.type.getCascade(field);
                    list.add(attr);
                }
            }
        }

        return list;
    }

    @Nullable
    private static Attr.Type getAttrType(CtField field) {
        if (hasAnnotationOnField(field, ONE_TO_ONE_ANNOTATION))
            return Attr.Type.ONE_TO_ONE;
        else if (hasAnnotationOnField(field, ONE_TO_MANY_ANNOTATION))
            return Attr.Type.ONE_TO_MANY;
        else if (hasAnnotationOnField(field, MANY_TO_ONE_ANNOTATION))
            return Attr.Type.MANY_TO_ONE;
        else if (hasAnnotationOnField(field, MANY_TO_MANY_ANNOTATION))
            return Attr.Type.MANY_TO_MANY;
        else
            return null;
    }

    private static class Attr {

        protected enum Type {
            MANY_TO_ONE(1, "many-to-one", MANY_TO_ONE_ANNOTATION, "EAGER"),
            ONE_TO_MANY(2, "one-to-many", ONE_TO_MANY_ANNOTATION),
            ONE_TO_ONE(3, "one-to-one", ONE_TO_ONE_ANNOTATION, "EAGER"),
            MANY_TO_MANY(4, "many-to-many", MANY_TO_MANY_ANNOTATION);

            private int order;
            private String xml;
            private String annotationName;
            private String defaultFetch = "LAZY";

            Type(int order, String xml, String annotationName) {
                this.order = order;
                this.xml = xml;
                this.annotationName = annotationName;
            }

            Type(int order, String xml, String annotationName, String defaultFetch) {
                this(order, xml, annotationName);
                this.defaultFetch = defaultFetch;
            }

            protected String getFetch(CtField ctField) {
                Annotation annotation = getAnnotation(ctField);
                EnumMemberValue memberValue = (EnumMemberValue) annotation.getMemberValue("fetch");
                String fetchType = defaultFetch;
                if (memberValue != null && !Strings.isNullOrEmpty(memberValue.getValue())) {
                    fetchType = memberValue.getValue();
                }
                return fetchType;
            }

            protected String getMappedBy(CtField ctField) {
                StringMemberValue value = (StringMemberValue) getAnnotation(ctField).getMemberValue("mappedBy");
                return value != null ? value.getValue() : null;
            }

            protected List<String> getCascade(CtField ctField) {
                ArrayMemberValue value = (ArrayMemberValue) getAnnotation(ctField).getMemberValue("cascade");
                List<String> result = null;
                if (value != null) {
                    result = Arrays.stream(value.getValue()).map(v -> ((EnumMemberValue) v).getValue()).collect(Collectors.toList());
                }
                return result;
            }

            protected Annotation getAnnotation(CtField ctField) {
                return Attr.getAnnotation(ctField, annotationName);
            }
        }

        private final Type type;
        private final CtField field;
        private final String targetEntity;

        private Attr(Type type, CtField field, String targetEntity) {
            this.type = type;
            this.field = field;
            this.targetEntity = targetEntity;
        }

        private Element toXml(Element parentEl) {
            Element el = parentEl.addElement(type.xml, ORM_XMLNS);
            el.addAttribute("name", field.getName());
            el.addAttribute("target-entity", targetEntity);
            el.addAttribute("fetch", type.getFetch(field));
            String mappedBy = type.getMappedBy(field);
            if (!Strings.isNullOrEmpty(mappedBy))
                el.addAttribute("mapped-by", mappedBy);

            // either
            new JoinColumnHandler(getAnnotation(field, "javax.persistence.JoinColumn")).toXml(el);
            // or
            new OrderByHandler(getAnnotation(field, "javax.persistence.OrderBy")).toXml(el);
            new JoinTableHandler(getAnnotation(field, "javax.persistence.JoinTable")).toXml(el);
            new MapsIdHandler(getAnnotation(field, "javax.persistence.MapsId")).toXml(el);

            List<String> cascadeTypes = type.getCascade(field);
            if (cascadeTypes != null && cascadeTypes.size() > 0) {
                Element cascadeTypeEl = el.addElement("cascade", ORM_XMLNS);
                for (String cascadeType : cascadeTypes) {
                    cascadeTypeEl.addElement("cascade-" + cascadeType.toLowerCase());
                }
            }

            return el;
        }

        protected static Annotation getAnnotation(CtField ctField, String annotationName) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
            return annotationsAttribute.getAnnotation(annotationName);
        }
    }


    private static class AnnotationHandler {
        protected Annotation annotation;

        public AnnotationHandler(Annotation annotation) {
            this.annotation = annotation;
        }

        protected String getStringAttribute(String name) {
            return getStringAttribute(name, "");

        }

        protected String getStringAttribute(String name, String defaultValue) {
            StringMemberValue memberValue = (StringMemberValue) annotation.getMemberValue(name);
            return memberValue != null ? memberValue.getValue() : defaultValue;
        }

        protected boolean getBooleanAttribute(String name, Boolean defaultValue) {
            BooleanMemberValue memberValue = ((BooleanMemberValue) annotation.getMemberValue(name));
            return memberValue != null ? memberValue.getValue() : defaultValue;
        }

        protected List<Annotation> getAnnotationArrayAttribute(String name) {
            ArrayMemberValue memberValues = ((ArrayMemberValue) annotation.getMemberValue(name));
            List<Annotation> result = new ArrayList<>();
            if (memberValues != null) {
                for (MemberValue value : memberValues.getValue()) {
                    result.add(((AnnotationMemberValue) value).getValue());
                }
            }
            return result;
        }

    }

    private static class JoinColumnHandler extends AnnotationHandler {

        private JoinColumnHandler(Annotation annotation) {
            super(annotation);
        }

        protected void toXml(Element parentEl) {
            if (annotation == null)
                return;

            Element el = parentEl.addElement(getElementName());
            el.addAttribute("name", getStringAttribute("name"));

            String referencedColumnName = getStringAttribute("referencedColumnName");

            if (!Strings.isNullOrEmpty(referencedColumnName))
                el.addAttribute("referenced-column-name", referencedColumnName);

            if (getBooleanAttribute("unique", false))
                el.addAttribute("unique", "true");

            if (!getBooleanAttribute("nullable", true))
                el.addAttribute("nullable", "false");

            if (!getBooleanAttribute("insertable", true))
                el.addAttribute("insertable", "false");

            if (!getBooleanAttribute("updatable", true))
                el.addAttribute("updatable", "false");
        }

        protected String getElementName() {
            return "join-column";
        }
    }

    private static class InverseJoinColumnHandler extends JoinColumnHandler {

        private InverseJoinColumnHandler(Annotation annotation) {
            super(annotation);
        }

        @Override
        protected String getElementName() {
            return "inverse-join-column";
        }
    }

    private static class JoinTableHandler extends AnnotationHandler {

        private JoinTableHandler(Annotation annotation) {
            super(annotation);
        }

        private void toXml(Element parentEl) {
            if (annotation == null)
                return;

            Element el = parentEl.addElement("join-table");
            el.addAttribute("name", getStringAttribute("name"));
            for (Annotation joinColumnAnnot : getAnnotationArrayAttribute("joinColumns")) {
                new JoinColumnHandler(joinColumnAnnot).toXml(el);
            }
            for (Annotation joinColumnAnnot : getAnnotationArrayAttribute("inverseJoinColumns")) {
                new InverseJoinColumnHandler(joinColumnAnnot).toXml(el);
            }
        }
    }

    private static class MapsIdHandler extends AnnotationHandler {

        private MapsIdHandler(Annotation annotation) {
            super(annotation);
        }

        private void toXml(Element parentEl) {
            if (annotation == null)
                return;

            parentEl.addAttribute("maps-id", getStringAttribute("value"));
        }
    }

    private static class OrderByHandler extends AnnotationHandler {

        private OrderByHandler(Annotation annotation) {
            super(annotation);
        }

        private void toXml(Element parentEl) {
            if (annotation == null)
                return;

            Element el = parentEl.addElement("order-by");
            el.setText(getStringAttribute("value"));
        }
    }

}

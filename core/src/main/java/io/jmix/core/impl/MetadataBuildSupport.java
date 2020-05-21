/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import org.springframework.stereotype.Component;

@Component("jmix_MetadataBuildSupport")
public class MetadataBuildSupport {

//    @Inject
//    protected Stores stores;
//
//    @Inject
//    protected Environment environment;
//
//    public static class XmlAnnotation {
//        public final Object value;
//        public final Map<String, Object> attributes = new HashMap<>();
//
//        public XmlAnnotation(@Nullable Object value) {
//            this.value = value;
//        }
//    }
//
//    public static class XmlAnnotations {
//        public final String entityClass;
//        public final Map<String, XmlAnnotation> annotations = new HashMap<>();
//        public final List<XmlAnnotations> attributeAnnotations = new ArrayList<>();
//
//        public XmlAnnotations(String entityClass) {
//            this.entityClass = entityClass;
//        }
//    }
//
//    public static class XmlFile {
//        public final String name;
//        public final Element root;
//
//        public XmlFile(String name, Element root) {
//            this.name = name;
//            this.root = root;
//        }
//    }
//
//    private static final Logger log = LoggerFactory.getLogger(MetadataBuildSupport.class);
//
//    public static final String PERSISTENCE_CONFIG = "cuba.persistenceConfig";
//    public static final String METADATA_CONFIG = "jmix.core.metadataConfig";
//
//    @Inject
//    protected Resources resources;
//
//    @Inject
//    protected DatatypeRegistry datatypes;
//
//    private static final Pattern JAVA_CLASS_PATTERN = Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
//
//    public List<XmlFile> init() {
//        List<XmlFile> metadataXmlList = new ArrayList<>();
//        StringTokenizer metadataFilesTokenizer = new StringTokenizer(getMetadataConfig());
//        for (String fileName : metadataFilesTokenizer.getTokenArray()) {
//            metadataXmlList.add(new XmlFile(fileName, readXml(fileName)));
//        }
//        return metadataXmlList;
//    }
//
//    /**
//     * @param storeName data store name
//     * @return location of persistent entities descriptor or null if not defined
//     */
//    @Nullable
//    public String getPersistenceConfig(String storeName) {
//        String propName = PERSISTENCE_CONFIG;
//        if (!Stores.isMain(storeName))
//            propName = propName + "_" + storeName;
//
//        String config = AppContext.getProperty(propName);
//        if (StringUtils.isBlank(config)) {
//            log.trace("Property {} is not set, assuming {} is not a RdbmsStore", propName, storeName);
//            return null;
//        } else
//            return config;
//    }
//
//    /**
//     * @return location of metadata descriptor
//     */
//    public String getMetadataConfig() {
//        String config = environment.getProperty(METADATA_CONFIG);
//        if (StringUtils.isBlank(config))
//            throw new IllegalStateException(METADATA_CONFIG + " application property is not defined");
//        return config;
//    }
//
//    public Map<String, List<EntityClassInfo>> getEntityPackages(List<XmlFile> metadataXmlList) {
//        Map<String, List<EntityClassInfo>> packages = new LinkedHashMap<>();
//
//        loadFromMetadataConfig(packages, metadataXmlList);
//        stores.getAll().forEach(db -> loadFromPersistenceConfig(packages, db));
//
//        return packages;
//    }
//
//    protected void loadFromMetadataConfig(Map<String, List<EntityClassInfo>> packages, List<XmlFile> metadataXmlList) {
//        for (XmlFile xmlFile : metadataXmlList) {
//            for (Element element : xmlFile.root.elements("metadata-model")) {
//                String rootPackage = element.attributeValue("root-package");
//                if (StringUtils.isBlank(rootPackage))
//                    throw new IllegalStateException("metadata-model/@root-package is empty in " + xmlFile.name);
//
//                List<EntityClassInfo> classNames = packages.computeIfAbsent(rootPackage, k -> new ArrayList<>());
//
//                for (Element classEl : element.elements("class")) {
//                    classNames.add(new EntityClassInfo(classEl.attributeValue("store"), classEl.getText().trim(), false));
//                }
//            }
//        }
//    }
//
//    protected void loadFromPersistenceConfig(Map<String, List<EntityClassInfo>> packages, String db) {
//        String persistenceConfig = getPersistenceConfig(db);
//        if (persistenceConfig == null) {
//            return;
//        }
//        StringTokenizer persistenceFilesTokenizer = new StringTokenizer(persistenceConfig);
//        for (String fileName : persistenceFilesTokenizer.getTokenArray()) {
//            Element root = readXml(fileName);
//            Element puEl = root.element("persistence-unit");
//            if (puEl == null)
//                throw new IllegalStateException("File " + fileName + " has no persistence-unit element");
//
//            for (Element classEl : puEl.elements("class")) {
//                String className = classEl.getText().trim();
//                boolean included = false;
//
//                for (Map.Entry<String, List<EntityClassInfo>> entry : packages.entrySet()) {
//                    if (className.startsWith(entry.getKey() + ".")) {
//                        List<EntityClassInfo> classNames = entry.getValue();
//                        if (classNames == null) {
//                            classNames = new ArrayList<>();
//                            packages.put(entry.getKey(), classNames);
//                        }
//                        classNames.add(new EntityClassInfo(db, className, true));
//                        included = true;
//                        break;
//                    }
//                }
//
//                if (!included) {
//                    throw new IllegalStateException(
//                            String.format("Can not find a model for class %s. The class's package must be inside of some model's root package.",
//                                    className));
//                }
//            }
//        }
//    }
//
//    protected Element readXml(String path) {
//        try (InputStream stream = resources.getResourceAsStream(path)) {
//            if (stream == null) {
//                throw new IllegalStateException("Resource not found: " + path);
//            }
//
//            Document document = Dom4j.readDocument(stream);
//            return document.getRootElement();
//        } catch (IOException e) {
//            throw new IllegalStateException("Unable to read resource: " + path);
//        }
//    }
//
//    public List<XmlAnnotations> getEntityAnnotations(List<XmlFile> metadataXmlList) {
//        List<XmlAnnotations> result = new ArrayList<>();
//
//        for (XmlFile xmlFile : metadataXmlList) {
//            Element annotationsEl = xmlFile.root.element("annotations");
//            if (annotationsEl != null) {
//                for (Element entityEl : annotationsEl.elements("entity")) {
//                    String className = entityEl.attributeValue("class");
//                    XmlAnnotations entityAnnotations = new XmlAnnotations(className);
//                    for (Element annotEl : entityEl.elements("annotation")) {
//                        XmlAnnotation xmlAnnotation = new XmlAnnotation(inferMetaAnnotationType(annotEl.attributeValue("value")));
//                        for (Element attrEl : annotEl.elements("attribute")) {
//                            Object value = getXmlAnnotationAttributeValue(attrEl);
//                            xmlAnnotation.attributes.put(attrEl.attributeValue("name"), value);
//                        }
//                        entityAnnotations.annotations.put(annotEl.attributeValue("name"), xmlAnnotation);
//                    }
//                    for (Element propEl : entityEl.elements("property")) {
//                        XmlAnnotations attributeAnnotations = new XmlAnnotations(propEl.attributeValue("name"));
//                        for (Element annotEl : propEl.elements("annotation")) {
//                            XmlAnnotation xmlAnnotation = new XmlAnnotation(inferMetaAnnotationType(annotEl.attributeValue("value")));
//                            for (Element attrEl : annotEl.elements("attribute")) {
//                                Object value = getXmlAnnotationAttributeValue(attrEl);
//                                xmlAnnotation.attributes.put(attrEl.attributeValue("name"), value);
//                            }
//                            attributeAnnotations.annotations.put(annotEl.attributeValue("name"), xmlAnnotation);
//                        }
//                        entityAnnotations.attributeAnnotations.add(attributeAnnotations);
//                    }
//                    result.add(entityAnnotations);
//                }
//            }
//        }
//        return result;
//    }
//
//    @Nullable
//    protected Object getXmlAnnotationAttributeValue(Element attributeEl) {
//        String value = attributeEl.attributeValue("value");
//        String className = attributeEl.attributeValue("class");
//        String datatypeName = attributeEl.attributeValue("datatype");
//
//        List<Element> values = attributeEl.elements("value");
//        if (StringUtils.isNotBlank(value)) {
//            if (!values.isEmpty())
//                throw new IllegalStateException("Both 'value' attribute and 'value' element(s) are specified for attribute " + attributeEl.attributeValue("name"));
//            return getXmlAnnotationAttributeValue(value, className, datatypeName);
//        }
//        if (!values.isEmpty()) {
//            Object val0 = getXmlAnnotationAttributeValue(values.get(0).getTextTrim(), className, datatypeName);
//            Object array = Array.newInstance(val0.getClass(), values.size());
//            Array.set(array, 0, val0);
//            for (int i = 1; i < values.size(); i++) {
//                Object val = getXmlAnnotationAttributeValue(values.get(i).getTextTrim(), className, datatypeName);
//                Array.set(array, i, val);
//            }
//            return array;
//        }
//        return null;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Nullable
//    protected Object getXmlAnnotationAttributeValue(String value, @Nullable String className, @Nullable String datatypeName) {
//        if (className == null && datatypeName == null)
//            return inferMetaAnnotationType(value);
//        if (className != null) {
//            Class aClass = ReflectionHelper.getClass(className);
//            if (aClass.isEnum()) {
//                return Enum.valueOf(aClass, value);
//            } else {
//                throw new UnsupportedOperationException("Class " + className + "  is not Enum");
//            }
//        } else {
//            try {
//                return datatypes.get(datatypeName).parse(value);
//            } catch (ParseException e) {
//                throw new RuntimeException("Unable to parse XML meta-annotation value", e);
//            }
//        }
//    }
//
//    @Nullable
//    protected Object inferMetaAnnotationType(@Nullable String str) {
//        Object val;
//        if (str != null && (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")))
//            val = Boolean.valueOf(str);
//        else if (str != null && JAVA_CLASS_PATTERN.matcher(str).matches()) {
//            try {
//                val = ReflectionHelper.loadClass(str);
//            } catch (ClassNotFoundException e) {
//                val = str;
//            }
//        } else if (!"".equals(str) && StringUtils.isNumeric(str)) {
//            val = Integer.valueOf(str);
//        } else
//            val = str;
//        return val;
//    }
}
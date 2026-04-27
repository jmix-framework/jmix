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

package io.jmix.flowui.kit.meta.generator;

import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElseGet;

/**
 * Internal generator that builds Studio meta-annotation source code from Flow UI XSD schemas.
 * <p>
 * The generator resolves XSD element inheritance, referenced attributes, attribute groups and child elements,
 * infers the most suitable {@link StudioMetaKind}, converts XSD attribute definitions to
 * {@code @StudioProperty} declarations and reuses existing compatible {@code propertyGroups} when they are found.
 * It can either return the generated source for preview or write it to a new or existing {@code @StudioUiKit}
 * source file.
 * <p>
 * This class is intended for framework development tooling and is not part of the public runtime API.
 */
final class StudioMetaDescriptionGenerator {

    public static void main(String[] args) throws Exception {
        CLI.run(args);
    }

    private final Path schemaSearchRoot;
    private final XsdRegistry registry;
    private final StudioPropertyGroupsMatcher propertyGroupsMatcher;
    private final Map<String, String> preferredPropertyGroupJavaClasses;
    private final Map<String, List<String>> preferredPropertyGroupsByComplexType;

    StudioMetaDescriptionGenerator(Path schemaSearchRoot) {
        this(schemaSearchRoot, PREFERRED_PROPERTY_GROUP_JAVA_CLASSES, PREFERRED_PROPERTY_GROUPS_BY_COMPLEX_TYPE);
    }

    StudioMetaDescriptionGenerator(Path schemaSearchRoot,
                                   Map<String, String> preferredPropertyGroupJavaClasses) {
        this(schemaSearchRoot, preferredPropertyGroupJavaClasses, PREFERRED_PROPERTY_GROUPS_BY_COMPLEX_TYPE);
    }

    StudioMetaDescriptionGenerator(Path schemaSearchRoot,
                                   Map<String, String> preferredPropertyGroupJavaClasses,
                                   Map<String, List<String>> preferredPropertyGroupsByComplexType) {
        this.schemaSearchRoot = schemaSearchRoot.toAbsolutePath().normalize();
        this.registry = new XsdRegistry(this.schemaSearchRoot);
        this.propertyGroupsMatcher = new StudioPropertyGroupsMatcher(this.schemaSearchRoot);
        this.preferredPropertyGroupJavaClasses = Map.copyOf(preferredPropertyGroupJavaClasses);
        this.preferredPropertyGroupsByComplexType = Map.copyOf(preferredPropertyGroupsByComplexType);
    }

    static Path detectWorkspaceRoot(Path workingDirectory) {
        Path current = workingDirectory.toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("jmix"))
                    && Files.isDirectory(current.resolve("jmix-premium"))) {
                return current;
            }
            if (Files.exists(current.resolve("settings.gradle"))
                    && Files.isDirectory(current.resolve("jmix-flowui"))) {
                Path parent = current.getParent();
                if (parent != null && Files.isDirectory(parent.resolve("jmix-premium"))) {
                    return parent;
                }
                return current;
            }
            if (Files.exists(current.resolve("flowui-kit.gradle"))) {
                Path moduleParent = current.getParent();
                if (moduleParent != null) {
                    Path gradleRoot = moduleParent.getParent();
                    if (gradleRoot != null && Files.exists(gradleRoot.resolve("settings.gradle"))) {
                        Path workspaceRoot = gradleRoot.getParent();
                        return workspaceRoot != null ? workspaceRoot : gradleRoot;
                    }
                }
            }
            current = current.getParent();
        }
        return workingDirectory.toAbsolutePath().normalize();
    }

    List<Path> findKnownSchemas() {
        return registry.discoverSchemas();
    }

    List<StudioXsdElementCandidate> findElementCandidates(Path schemaPath, String elementIdentifier) {
        Objects.requireNonNull(elementIdentifier, "elementIdentifier");
        return registry.findCandidates(resolveAbsolutePath(schemaPath), elementIdentifier);
    }

    Path getDefaultOutputPath(StudioXsdElementCandidate candidate) {
        Path moduleRoot = findModuleRoot(candidate.schemaPath());
        if (moduleRoot == null) {
            throw new IllegalArgumentException("Cannot detect module root for " + candidate.schemaPath());
        }

        Path packagePath = Path.of(StudioComponent.class.getPackageName().replace('.', '/')).resolve("generated");
        String contextPrefix = "";
        if (GENERIC_METHOD_NAMES.contains(candidate.elementName())) {
            List<String> contextSource = !candidate.ancestorElements().isEmpty()
                    ? candidate.ancestorElements()
                    : candidate.contextNames().subList(0, Math.max(0, candidate.contextNames().size() - 1));
            if (!contextSource.isEmpty()) {
                contextPrefix = toPascalCase(contextSource.getLast());
            }
        }

        String fileName = "Studio" + contextPrefix + toPascalCase(candidate.elementName()) + "Generated.java";
        return moduleRoot.resolve("src/main/java").resolve(packagePath).resolve(fileName).normalize();
    }

    StudioMetaGenerationResult generate(StudioXsdElementCandidate candidate,
                                        StudioMetaKind forcedKind,
                                        @Nullable Path outputPath) {
        GeneratedMeta meta = createGeneratedMeta(candidate, forcedKind);
        Path resolvedOutputPath = outputPath != null
                ? resolveAbsolutePath(outputPath)
                : getDefaultOutputPath(candidate);
        String uniqueMethodName = ensureUniqueMethodName(resolvedOutputPath, meta.methodName());
        GeneratedMeta metaWithUniqueMethod = meta.withMethodName(uniqueMethodName);
        String source;
        if (Files.exists(resolvedOutputPath)) {
            source = buildUpdatedSourceForExistingFile(resolvedOutputPath, renderAnnotation(metaWithUniqueMethod));
        } else {
            source = buildJavaSource(resolvedOutputPath, metaWithUniqueMethod, candidate.schemaPath());
        }
        return new StudioMetaGenerationResult(resolvedOutputPath, source, metaWithUniqueMethod.kind(), metaWithUniqueMethod.headerTodos());
    }

    StudioMetaGenerationResult write(StudioXsdElementCandidate candidate,
                                     StudioMetaKind forcedKind,
                                     @Nullable Path outputPath) throws IOException {
        GeneratedMeta meta = createGeneratedMeta(candidate, forcedKind);
        Path resolvedOutputPath = outputPath != null
                ? resolveAbsolutePath(outputPath)
                : getDefaultOutputPath(candidate);
        String uniqueMethodName = ensureUniqueMethodName(resolvedOutputPath, meta.methodName());
        GeneratedMeta metaWithUniqueMethod = meta.withMethodName(uniqueMethodName);

        Files.createDirectories(resolvedOutputPath.getParent());
        if (Files.exists(resolvedOutputPath) && Files.size(resolvedOutputPath) > 0) {
            Files.writeString(resolvedOutputPath,
                    buildUpdatedSourceForExistingFile(resolvedOutputPath, renderAnnotation(metaWithUniqueMethod)),
                    StandardCharsets.UTF_8);
            String source = Files.readString(resolvedOutputPath, StandardCharsets.UTF_8);
            return new StudioMetaGenerationResult(resolvedOutputPath, source, metaWithUniqueMethod.kind(),
                    metaWithUniqueMethod.headerTodos());
        }

        String source = buildJavaSource(resolvedOutputPath, metaWithUniqueMethod, candidate.schemaPath());
        Files.writeString(resolvedOutputPath, source, StandardCharsets.UTF_8);
        return new StudioMetaGenerationResult(resolvedOutputPath, source, metaWithUniqueMethod.kind(),
                metaWithUniqueMethod.headerTodos());
    }

    private GeneratedMeta createGeneratedMeta(StudioXsdElementCandidate candidate, StudioMetaKind forcedKind) {
        DefinitionRef elementRef = resolveElementReference(candidate.document(), candidate.element());
        ResolvedType elementType = resolveElementType(elementRef);
        LinkedHashMap<String, AttributeInfo> attributes = collectAttributes(elementType.ref(), elementType.kind(),
                new HashSet<>(), new HashSet<>());
        ChildrenInfo childrenInfo = collectChildren(elementType.ref(), elementType.kind(), new HashSet<>(), new HashSet<>());
        KindInfo kindInfo = inferKind(candidate, elementType.ref(), elementType.kind(), attributes, forcedKind);

        List<PropertyInfo> properties = new ArrayList<>();
        for (AttributeInfo attributeInfo : attributes.values()) {
            properties.add(inferProperty(attributeInfo, kindInfo.kind()));
        }
        Path moduleRoot = findModuleRoot(candidate.schemaPath());
        PropertyGroupsResolution propertyResolution = resolvePropertyGroups(candidate, elementRef, elementType,
                properties, moduleRoot);
        List<String> propertyGroups = propertyResolution.propertyGroups();
        List<PropertyInfo> inlineProperties = propertyResolution.inlineProperties();

        String xmlns = candidate.document().targetNamespace().isEmpty() ? null : candidate.document().targetNamespace();
        String xmlnsAlias = inferXmlnsAlias(candidate.document());
        List<String> headerTodos = buildHeaderTodos(candidate, kindInfo.kind(), xmlns, xmlnsAlias, inlineProperties,
                propertyGroups);
        String methodName = buildMethodName(candidate, kindInfo.kind());

        return new GeneratedMeta(
                kindInfo.kind(),
                kindInfo.kindComment(),
                toPascalCase(candidate.elementName()),
                candidate.elementName(),
                FLOWUI_LAYOUT_NS.equals(candidate.document().targetNamespace()) ? null : xmlns,
                xmlnsAlias,
                headerTodos,
                propertyGroups,
                inlineProperties,
                childrenInfo.childElements(),
                childrenInfo.supportsAnyChildren(),
                methodName,
                kindInfo.kind() == StudioMetaKind.COMPONENT ? Div.class.getCanonicalName() : "void"
        );
    }

    private DefinitionRef resolveElementReference(SchemaDocument document, Element element) {
        String ref = element.getAttribute("ref");
        if (ref.isEmpty()) {
            return new DefinitionRef(document, element);
        }

        QNameRef qNameRef = registry.resolveQName(ref, document);
        DefinitionRef resolved = registry.findDefinition(registry.elementDefinitions, qNameRef);
        return resolved != null ? resolved : new DefinitionRef(document, element);
    }

    private ResolvedType resolveElementType(DefinitionRef elementRef) {
        assert elementRef.element() != null : "Element reference must have an element";

        Element inlineComplexType = firstChildElement(elementRef.element(), "complexType");
        if (inlineComplexType != null) {
            return new ResolvedType(new DefinitionRef(elementRef.document(), inlineComplexType), "complex");
        }

        Element inlineSimpleType = firstChildElement(elementRef.element(), "simpleType");
        if (inlineSimpleType != null) {
            return new ResolvedType(new DefinitionRef(elementRef.document(), inlineSimpleType), "simple");
        }

        String typeName = elementRef.element().getAttribute("type");
        if (typeName.isEmpty()) {
            return new ResolvedType(null, null);
        }

        QNameRef qNameRef = registry.resolveQName(typeName, elementRef.document());
        if (XS_NS.equals(qNameRef.namespace())) {
            return new ResolvedType(new DefinitionRef(elementRef.document(), null, qNameRef), "builtin");
        }

        DefinitionRef complexType = registry.findDefinition(registry.complexTypeDefinitions, qNameRef);
        if (complexType != null) {
            return new ResolvedType(complexType, "complex");
        }

        DefinitionRef simpleType = registry.findDefinition(registry.simpleTypeDefinitions, qNameRef);
        if (simpleType != null) {
            return new ResolvedType(simpleType, "simple");
        }

        return new ResolvedType(new DefinitionRef(elementRef.document(), null, qNameRef), "unknown");
    }

    private LinkedHashMap<String, AttributeInfo> collectAttributes(@Nullable DefinitionRef typeRef,
                                                                   @Nullable String typeKind,
                                                                   Set<VisitedNodeKey> visitedTypes,
                                                                   Set<VisitedNodeKey> visitedGroups) {
        LinkedHashMap<String, AttributeInfo> attributes = new LinkedHashMap<>();
        if (typeRef == null || typeRef.element() == null || !Set.of("complex", "simple").contains(typeKind)) {
            return attributes;
        }

        VisitedNodeKey visitedNodeKey = new VisitedNodeKey(typeRef.document().path(), System.identityHashCode(typeRef.element()));
        if (!visitedTypes.add(visitedNodeKey)) {
            return attributes;
        }

        Element baseContainer = typeRef.element();
        Element extension = findExtension(typeRef.element());
        Element restriction = findRestriction(typeRef.element());
        if (extension != null) {
            String baseName = extension.getAttribute("base");
            if (!baseName.isEmpty()) {
                QNameRef baseQName = registry.resolveQName(baseName, typeRef.document());
                DefinitionRef baseRef = registry.findDefinition(registry.complexTypeDefinitions, baseQName);
                if (baseRef != null) {
                    attributes.putAll(collectAttributes(baseRef, "complex", visitedTypes, visitedGroups));
                }
            }
            baseContainer = extension;
        } else if (restriction != null && "complex".equals(typeKind)) {
            String baseName = restriction.getAttribute("base");
            if (!baseName.isEmpty()) {
                QNameRef baseQName = registry.resolveQName(baseName, typeRef.document());
                DefinitionRef baseRef = registry.findDefinition(registry.complexTypeDefinitions, baseQName);
                if (baseRef != null) {
                    attributes.putAll(collectAttributes(baseRef, "complex", visitedTypes, visitedGroups));
                }
            }
            baseContainer = restriction;
        }

        for (Element child : childElements(baseContainer)) {
            String childLocalName = localName(child);
            if ("attribute".equals(childLocalName)) {
                AttributeInfo attribute = resolveAttribute(typeRef.document(), child);
                if (attribute != null) {
                    attributes.put(attribute.xmlAttribute(), attribute);
                }
            } else if ("attributeGroup".equals(childLocalName) && child.hasAttribute("ref")) {
                QNameRef groupQName = registry.resolveQName(child.getAttribute("ref"), typeRef.document());
                DefinitionRef groupRef = registry.findDefinition(registry.attributeGroupDefinitions, groupQName);
                if (groupRef == null || groupRef.element() == null) {
                    continue;
                }
                VisitedNodeKey groupKey = new VisitedNodeKey(groupRef.document().path(), System.identityHashCode(groupRef.element()));
                if (!visitedGroups.add(groupKey)) {
                    continue;
                }
                attributes.putAll(collectAttributeGroup(groupRef, visitedTypes, visitedGroups));
            }
        }

        return attributes;
    }

    private LinkedHashMap<String, AttributeInfo> collectAttributeGroup(DefinitionRef groupRef,
                                                                       Set<VisitedNodeKey> visitedTypes,
                                                                       Set<VisitedNodeKey> visitedGroups) {
        LinkedHashMap<String, AttributeInfo> attributes = new LinkedHashMap<>();
        if (groupRef.element() == null) {
            return attributes;
        }

        for (Element child : childElements(groupRef.element())) {
            String childLocalName = localName(child);
            if ("attribute".equals(childLocalName)) {
                AttributeInfo attribute = resolveAttribute(groupRef.document(), child);
                if (attribute != null) {
                    attributes.put(attribute.xmlAttribute(), attribute);
                }
            } else if ("attributeGroup".equals(childLocalName) && child.hasAttribute("ref")) {
                QNameRef groupQName = registry.resolveQName(child.getAttribute("ref"), groupRef.document());
                DefinitionRef nestedGroupRef = registry.findDefinition(registry.attributeGroupDefinitions, groupQName);
                if (nestedGroupRef == null || nestedGroupRef.element() == null) {
                    continue;
                }
                VisitedNodeKey nestedKey = new VisitedNodeKey(nestedGroupRef.document().path(),
                        System.identityHashCode(nestedGroupRef.element()));
                if (!visitedGroups.add(nestedKey)) {
                    continue;
                }
                attributes.putAll(collectAttributeGroup(nestedGroupRef, visitedTypes, visitedGroups));
            }
        }

        return attributes;
    }

    private @Nullable AttributeInfo resolveAttribute(SchemaDocument document, Element attributeElement) {
        Element targetElement = attributeElement;
        SchemaDocument targetDocument = document;
        if (attributeElement.hasAttribute("ref")) {
            QNameRef refQName = registry.resolveQName(attributeElement.getAttribute("ref"), document);
            DefinitionRef resolved = registry.findDefinition(registry.attributeDefinitions, refQName);
            if (resolved != null && resolved.element() != null) {
                targetElement = resolved.element();
                targetDocument = resolved.document();
            }
        }

        String name = attributeElement.getAttribute("name");
        if (name.isEmpty()) {
            name = targetElement.getAttribute("name");
        }
        if (name.isEmpty()) {
            return null;
        }

        Element inlineSimpleType = firstChildElement(targetElement, "simpleType");
        String typeName = attributeElement.getAttribute("type");
        if (typeName.isEmpty()) {
            typeName = targetElement.getAttribute("type");
        }
        QNameRef typeQName = typeName.isEmpty() ? null : registry.resolveQName(typeName, targetDocument);

        String defaultValue = firstNonEmptyOrNull(
                attributeElement.getAttribute("default"),
                attributeElement.getAttribute("fixed"),
                targetElement.getAttribute("default"),
                targetElement.getAttribute("fixed")
        );
        boolean required = "required".equals(firstNonEmptyOrNull(attributeElement.getAttribute("use"), targetElement.getAttribute("use")));

        return new AttributeInfo(name, required, defaultValue, typeQName, inlineSimpleType, targetDocument, List.of());
    }

    private ChildrenInfo collectChildren(@Nullable DefinitionRef typeRef,
                                         @Nullable String typeKind,
                                         Set<VisitedNodeKey> visitedTypes,
                                         Set<VisitedNodeKey> visitedGroups) {
        if (typeRef == null || typeRef.element() == null || !Set.of("complex", "simple").contains(typeKind)) {
            return new ChildrenInfo(List.of(), false);
        }

        VisitedNodeKey visitedNodeKey = new VisitedNodeKey(typeRef.document().path(), System.identityHashCode(typeRef.element()));
        if (!visitedTypes.add(visitedNodeKey)) {
            return new ChildrenInfo(List.of(), false);
        }

        Element extension = findExtension(typeRef.element());
        Element restriction = findRestriction(typeRef.element());
        Element baseContainer;
        baseContainer = requireNonNullElseGet(extension,
                () -> requireNonNullElseGet(restriction, typeRef::element));

        List<String> childNames = new ArrayList<>();
        boolean supportsAnyChildren = false;
        if (extension != null && extension.hasAttribute("base")) {
            QNameRef baseQName = registry.resolveQName(extension.getAttribute("base"), typeRef.document());
            DefinitionRef baseRef = registry.findDefinition(registry.complexTypeDefinitions, baseQName);
            if (baseRef != null) {
                ChildrenInfo baseChildren = collectChildren(baseRef, "complex", visitedTypes, visitedGroups);
                childNames.addAll(baseChildren.childElements());
                supportsAnyChildren = baseChildren.supportsAnyChildren();
            }
        }

        ChildrenInfo nestedChildren = collectChildrenFromContainer(baseContainer, typeRef.document(), visitedGroups);
        childNames.addAll(nestedChildren.childElements());
        supportsAnyChildren = supportsAnyChildren || nestedChildren.supportsAnyChildren();

        return new ChildrenInfo(new ArrayList<>(new LinkedHashSet<>(childNames)), supportsAnyChildren);
    }

    private ChildrenInfo collectChildrenFromContainer(Element container,
                                                      SchemaDocument document,
                                                      Set<VisitedNodeKey> visitedGroups) {
        List<String> childNames = new ArrayList<>();
        boolean supportsAnyChildren = false;

        for (Element child : childElements(container)) {
            String childLocalName = localName(child);
            switch (childLocalName) {
                case "element" -> {
                    String name = child.getAttribute("name");
                    if (!name.isEmpty()) {
                        childNames.add(name);
                    } else if (child.hasAttribute("ref")) {
                        QNameRef refQName = registry.resolveQName(child.getAttribute("ref"), document);
                        childNames.add(refQName.localName());
                    }
                }
                case "all", "choice", "sequence" -> {
                    ChildrenInfo nestedChildren = collectChildrenFromContainer(child, document, visitedGroups);
                    childNames.addAll(nestedChildren.childElements());
                    supportsAnyChildren = supportsAnyChildren || nestedChildren.supportsAnyChildren();
                }
                case "group" -> {
                    if (!child.hasAttribute("ref")) {
                        break;
                    }
                    QNameRef groupQName = registry.resolveQName(child.getAttribute("ref"), document);
                    DefinitionRef groupRef = registry.findDefinition(registry.groupDefinitions, groupQName);
                    if (groupRef == null || groupRef.element() == null) {
                        break;
                    }
                    VisitedNodeKey groupKey = new VisitedNodeKey(groupRef.document().path(),
                            System.identityHashCode(groupRef.element()));
                    if (!visitedGroups.add(groupKey)) {
                        break;
                    }
                    ChildrenInfo groupChildren = collectChildrenFromContainer(groupRef.element(), groupRef.document(), visitedGroups);
                    childNames.addAll(groupChildren.childElements());
                    supportsAnyChildren = supportsAnyChildren || groupChildren.supportsAnyChildren();
                }
                case "any" -> supportsAnyChildren = true;
                default -> {
                }
            }
        }

        return new ChildrenInfo(childNames, supportsAnyChildren);
    }

    private @Nullable Element findExtension(Element element) {
        Element complexContent = firstChildElement(element, "complexContent");
        if (complexContent != null) {
            Element extension = firstChildElement(complexContent, "extension");
            if (extension != null) {
                return extension;
            }
        }

        Element simpleContent = firstChildElement(element, "simpleContent");
        if (simpleContent != null) {
            return firstChildElement(simpleContent, "extension");
        }
        return null;
    }

    private @Nullable Element findRestriction(Element element) {
        Element complexContent = firstChildElement(element, "complexContent");
        if (complexContent != null) {
            Element restriction = firstChildElement(complexContent, "restriction");
            if (restriction != null) {
                return restriction;
            }
        }

        Element simpleContent = firstChildElement(element, "simpleContent");
        if (simpleContent != null) {
            Element restriction = firstChildElement(simpleContent, "restriction");
            if (restriction != null) {
                return restriction;
            }
        }

        return firstChildElement(element, "restriction");
    }

    private KindInfo inferKind(StudioXsdElementCandidate candidate,
                               @Nullable DefinitionRef typeRef,
                               @Nullable String typeKind,
                               Map<String, AttributeInfo> attributes,
                               StudioMetaKind forcedKind) {
        if (forcedKind != StudioMetaKind.AUTO) {
            return new KindInfo(forcedKind, "Meta annotation kind was chosen explicitly.");
        }

        if (containsContextName(candidate, "facets")) {
            return new KindInfo(StudioMetaKind.FACET,
                    "The element is declared inside a facets container, so @StudioFacet was selected.");
        }

        if (FLOWUI_DATA_NS.equals(candidate.document().targetNamespace())
                && !candidate.contextNames().isEmpty()
                && ("viewData".equals(candidate.contextNames().getFirst())
                || DATA_COMPONENT_ELEMENT_NAMES.contains(candidate.elementName())
                && candidate.contextNames().contains("viewData"))) {
            return new KindInfo(StudioMetaKind.DATA_COMPONENT,
                    "The element belongs to the data schema root, so @StudioDataComponent was selected.");
        }

        if (containsActionContext(candidate)
                || "action".equals(candidate.elementName()) && attributes.containsKey(StudioXmlAttributes.TYPE)) {
            return new KindInfo(StudioMetaKind.ACTION,
                    "The element is located inside an actions definition, so @StudioAction was selected.");
        }

        if (!candidate.ancestorElements().isEmpty()
                || candidate.nested() && !candidate.element().hasAttribute("type")) {
            return new KindInfo(StudioMetaKind.ELEMENT,
                    "The element is nested inside another XSD element, so @StudioElement was selected.");
        }

        if (extendsBaseComponent(typeRef, typeKind, new HashSet<>())) {
            return new KindInfo(StudioMetaKind.COMPONENT,
                    "The XSD type extends a component base type, so @StudioComponent was selected.");
        }

        return new KindInfo(StudioMetaKind.ELEMENT,
                "No component/data/facet/action markers were found, so @StudioElement was selected.");
    }

    private boolean extendsBaseComponent(@Nullable DefinitionRef typeRef,
                                         @Nullable String typeKind,
                                         Set<VisitedNodeKey> visitedTypes) {
        if (typeRef == null || typeRef.element() == null || !"complex".equals(typeKind)) {
            return false;
        }

        VisitedNodeKey visitedNodeKey = new VisitedNodeKey(typeRef.document().path(), System.identityHashCode(typeRef.element()));
        if (!visitedTypes.add(visitedNodeKey)) {
            return false;
        }

        Element extension = findExtension(typeRef.element());
        if (extension == null || !extension.hasAttribute("base")) {
            return false;
        }

        QNameRef baseQName = registry.resolveQName(extension.getAttribute("base"), typeRef.document());
        if (Set.of("baseComponent", "componentContainer").contains(baseQName.localName())) {
            return true;
        }

        DefinitionRef baseRef = registry.findDefinition(registry.complexTypeDefinitions, baseQName);
        return extendsBaseComponent(baseRef, "complex", visitedTypes);
    }

    private PropertyInfo inferProperty(AttributeInfo attributeInfo, StudioMetaKind kind) {
        String inferredType = "STRING";
        String category = inferCategory(attributeInfo.xmlAttribute());
        List<String> comments = new ArrayList<>(attributeInfo.comments());
        List<String> options = List.of();

        EnumValues enumValues = resolveEnumValues(attributeInfo);
        String specialType = inferSpecialType(attributeInfo, kind);
        String builtinType = inferBuiltinType(attributeInfo);
        if (specialType != null) {
            inferredType = specialType;
        } else if ("LOCALIZED_STRING".equals(builtinType)) {
            inferredType = builtinType;
        } else if (!enumValues.values().isEmpty()) {
            inferredType = enumValues.allowsFreeForm() ? "OPTIONS" : "ENUMERATION";
            options = enumValues.values();
            if (!enumValues.allowsFreeForm()) {
                comments.add("Fill `classFqn` manually if \"" + attributeInfo.xmlAttribute()
                        + "\" should point to a Java enum.");
            }
        } else if (builtinType != null) {
            inferredType = builtinType;
        }

        if ("STRING".equals(inferredType) && AMBIGUOUS_SPECIAL_NAMES.contains(attributeInfo.xmlAttribute())) {
            comments.add("The tool left \"" + attributeInfo.xmlAttribute()
                    + "\" as STRING because its exact StudioPropertyType depends on the Java model.");
        }

        return new PropertyInfo(attributeInfo.xmlAttribute(), inferredType, category, attributeInfo.required(),
                attributeInfo.defaultValue(), options, comments);
    }

    private StudioPropertySignature toPropertySignature(PropertyInfo propertyInfo) {
        return StudioPropertySignature.of(
                propertyInfo.xmlAttribute(),
                propertyInfo.studioType(),
                propertyInfo.category() != null ? propertyInfo.category() : null,
                propertyInfo.required(),
                propertyInfo.defaultValue() != null ? propertyInfo.defaultValue() : "",
                propertyInfo.options()
        );
    }

    private PropertyGroupsResolution resolvePropertyGroups(StudioXsdElementCandidate candidate,
                                                           DefinitionRef elementRef,
                                                           ResolvedType elementType,
                                                           List<PropertyInfo> properties,
                                                           @Nullable Path moduleRoot) {
        LinkedHashSet<String> resolvedGroups = new LinkedHashSet<>();
        String preferredJavaClass = resolvePreferredPropertyGroupJavaClass(elementRef, elementType);
        if (preferredJavaClass != null) {
            List<String> explicitPropertyGroups = propertyGroupsMatcher.findPropertyGroupsByClassFqn(
                    preferredJavaClass,
                    candidate.elementName(),
                    moduleRoot
            );
            if (!explicitPropertyGroups.isEmpty()) {
                resolvedGroups.addAll(explicitPropertyGroups);
            }
        }

        if (resolvedGroups.isEmpty()) {
            List<String> explicitPropertyGroups = resolvePreferredPropertyGroups(elementRef, elementType);
            if (!explicitPropertyGroups.isEmpty()) {
                resolvedGroups.addAll(explicitPropertyGroups);
            }
        }

        if (resolvedGroups.isEmpty()) {
            List<String> exactMetaPropertyGroups = propertyGroupsMatcher.findPropertyGroupsByXmlElement(
                    candidate.elementName(),
                    moduleRoot
            );
            if (!exactMetaPropertyGroups.isEmpty()) {
                resolvedGroups.addAll(exactMetaPropertyGroups);
            }
        }

        List<PropertyInfo> remainingProperties = filterCoveredProperties(properties, List.copyOf(resolvedGroups));
        if (!remainingProperties.isEmpty()) {
            List<StudioPropertySignature> remainingPropertySignatures = remainingProperties.stream()
                    .map(this::toPropertySignature)
                    .toList();
            resolvedGroups.addAll(propertyGroupsMatcher.findMatchingGroupFqns(remainingPropertySignatures, moduleRoot));
            remainingProperties = filterCoveredProperties(properties, List.copyOf(resolvedGroups));
        }

        return new PropertyGroupsResolution(List.copyOf(resolvedGroups), remainingProperties);
    }

    private @Nullable String resolvePreferredPropertyGroupJavaClass(DefinitionRef elementRef, ResolvedType elementType) {
        String complexTypeName = resolveComplexTypeName(elementRef, elementType);
        if (complexTypeName == null) {
            return null;
        }
        return preferredPropertyGroupJavaClasses.get(complexTypeName);
    }

    private List<String> resolvePreferredPropertyGroups(DefinitionRef elementRef, ResolvedType elementType) {
        String complexTypeName = resolveComplexTypeName(elementRef, elementType);
        if (complexTypeName == null) {
            return List.of();
        }
        return preferredPropertyGroupsByComplexType.getOrDefault(complexTypeName, List.of());
    }

    private @Nullable String resolveComplexTypeName(DefinitionRef elementRef, ResolvedType elementType) {
        if (!"complex".equals(elementType.kind()) || elementRef.element() == null) {
            return null;
        }

        String typeName = elementRef.element().getAttribute("type");
        if (!typeName.isEmpty()) {
            return registry.resolveQName(typeName, elementRef.document()).localName();
        }

        if (elementType.ref() != null && elementType.ref().qName() != null) {
            return elementType.ref().qName().localName();
        }

        if (elementType.ref() != null && elementType.ref().element() != null) {
            String namedType = elementType.ref().element().getAttribute("name");
            return namedType.isEmpty() ? null : namedType;
        }

        return null;
    }

    private @Nullable String inferSpecialType(AttributeInfo attributeInfo, StudioMetaKind kind) {
        String xmlAttribute = attributeInfo.xmlAttribute();
        if (SIZE_NAMES.contains(xmlAttribute)) {
            return "SIZE";
        }
        if (kind == StudioMetaKind.DATA_COMPONENT && StudioXmlAttributes.CLASS.equals(xmlAttribute)) {
            return "ENTITY_CLASS";
        }
        if (Set.of(StudioXmlAttributes.CLASS_NAMES, StudioXmlAttributes.THEME_NAMES).contains(xmlAttribute)) {
            return "VALUES_LIST";
        }
        if (StudioXmlAttributes.ICON.equals(xmlAttribute) || xmlAttribute.endsWith("Icon")) {
            return "ICON";
        }
        switch (xmlAttribute) {
            case StudioXmlAttributes.ID -> {
                return "COMPONENT_ID";
            }
            case StudioXmlAttributes.FETCH_PLAN -> {
                return "FETCH_PLAN";
            }
            case StudioXmlAttributes.QUERY -> {
                return "JPA_QUERY";
            }
            case StudioXmlAttributes.LOADER -> {
                return "DATA_LOADER_REF";
            }
            case StudioXmlAttributes.ACTION -> {
                return "ACTION_REF";
            }
            case StudioXmlAttributes.CLICK_SHORTCUT, StudioXmlAttributes.FOCUS_SHORTCUT -> {
                return "SHORTCUT_COMBINATION";
            }
            case StudioXmlAttributes.SHORTCUT_COMBINATION -> {
                return "SHORTCUT_COMBINATION";
            }
            case StudioXmlAttributes.DATATYPE -> {
                return "DATATYPE_ID";
            }
            case StudioXmlAttributes.STORE -> {
                return "STORE";
            }
        }
        return null;
    }

    private @Nullable String inferBuiltinType(AttributeInfo attributeInfo) {
        if (attributeInfo.inlineSimpleType() != null) {
            String inferred = inferSimpleType(attributeInfo.inlineSimpleType(), attributeInfo.sourceDocument());
            if (inferred != null) {
                return inferred;
            }
        }

        if (attributeInfo.typeQName() == null) {
            return null;
        }

        QNameRef typeQName = attributeInfo.typeQName();
        if (XS_NS.equals(typeQName.namespace())) {
            return BUILTIN_TYPE_MAP.get(typeQName.localName());
        }

        if ("resourceString".equals(typeQName.localName())) {
            return "LOCALIZED_STRING";
        }

        DefinitionRef simpleTypeRef = registry.findDefinition(registry.simpleTypeDefinitions, typeQName);
        if (simpleTypeRef != null && simpleTypeRef.element() != null) {
            String inferred = inferSimpleType(simpleTypeRef.element(), simpleTypeRef.document());
            if (inferred != null) {
                return inferred;
            }
        }

        if (typeQName.localName().endsWith("resourceString")) {
            return "LOCALIZED_STRING";
        }

        return null;
    }

    private @Nullable String inferSimpleType(Element simpleType, @Nullable SchemaDocument document) {
        Element restriction = findRestriction(simpleType);
        if (restriction != null && restriction.hasAttribute("base") && document != null) {
            QNameRef baseQName = registry.resolveQName(restriction.getAttribute("base"), document);
            if (XS_NS.equals(baseQName.namespace())) {
                return BUILTIN_TYPE_MAP.get(baseQName.localName());
            }

            DefinitionRef baseSimpleType = registry.findDefinition(registry.simpleTypeDefinitions, baseQName);
            if (baseSimpleType != null && baseSimpleType.element() != null) {
                return inferSimpleType(baseSimpleType.element(), baseSimpleType.document());
            }
        }

        if (firstChildElement(simpleType, "list") != null) {
            return "VALUES_LIST";
        }
        return null;
    }

    private EnumValues resolveEnumValues(AttributeInfo attributeInfo) {
        Element source = attributeInfo.inlineSimpleType();
        SchemaDocument sourceDocument = attributeInfo.sourceDocument();
        if (source == null && attributeInfo.typeQName() != null && !XS_NS.equals(attributeInfo.typeQName().namespace())) {
            DefinitionRef simpleTypeRef = registry.findDefinition(registry.simpleTypeDefinitions, attributeInfo.typeQName());
            if (simpleTypeRef != null && simpleTypeRef.element() != null) {
                source = simpleTypeRef.element();
                sourceDocument = simpleTypeRef.document();
            }
        }
        if (source == null) {
            return new EnumValues(List.of(), false);
        }
        return resolveEnumValues(source, sourceDocument, attributeInfo.xmlAttribute(), new HashSet<>());
    }

    private EnumValues resolveEnumValues(Element simpleType,
                                         @Nullable SchemaDocument document,
                                         String attributeName,
                                         Set<VisitedNodeKey> visitedTypes) {
        VisitedNodeKey visitedNodeKey = new VisitedNodeKey(
                document != null ? document.path() : Path.of(""),
                System.identityHashCode(simpleType)
        );
        if (!visitedTypes.add(visitedNodeKey)) {
            return new EnumValues(List.of(), false);
        }

        Element restriction = findRestriction(simpleType);
        if (restriction != null) {
            List<String> values = new ArrayList<>();
            for (Element enumeration : childElements(restriction, "enumeration")) {
                String value = enumeration.getAttribute("value");
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            if (!values.isEmpty()) {
                return new EnumValues(values, false);
            }
        }

        Element union = firstChildElement(simpleType, "union");
        if (union == null) {
            return new EnumValues(List.of(), false);
        }

        LinkedHashSet<String> values = new LinkedHashSet<>();
        boolean allowsFreeForm = false;
        String memberTypes = union.getAttribute("memberTypes");
        if (!memberTypes.isBlank() && document != null) {
            for (String memberType : memberTypes.trim().split("\\s+")) {
                QNameRef qNameRef = registry.resolveQName(memberType, document);
                if (XS_NS.equals(qNameRef.namespace()) && "string".equals(qNameRef.localName())) {
                    allowsFreeForm = true;
                    continue;
                }
                DefinitionRef nestedTypeRef = registry.findDefinition(registry.simpleTypeDefinitions, qNameRef);
                if (nestedTypeRef != null && nestedTypeRef.element() != null) {
                    EnumValues nestedValues = resolveEnumValues(nestedTypeRef.element(), nestedTypeRef.document(),
                            attributeName, visitedTypes);
                    values.addAll(nestedValues.values());
                    allowsFreeForm = allowsFreeForm || nestedValues.allowsFreeForm();
                }
            }
        }

        for (Element nestedSimpleType : childElements(union, "simpleType")) {
            EnumValues nestedValues = resolveEnumValues(nestedSimpleType, document, attributeName, visitedTypes);
            values.addAll(nestedValues.values());
            allowsFreeForm = allowsFreeForm || nestedValues.allowsFreeForm();
        }

        return new EnumValues(new ArrayList<>(values), allowsFreeForm);
    }

    private @Nullable String inferCategory(String attributeName) {
        if (SIZE_NAMES.contains(attributeName)) {
            return resolveCategoryConstantName(StudioProperty.Category.SIZE);
        }
        if (POSITION_NAMES.contains(attributeName)) {
            return resolveCategoryConstantName(StudioProperty.Category.POSITION);
        }
        if (LOOK_AND_FEEL_NAMES.contains(attributeName)
                || Set.of(StudioXmlAttributes.DISABLE_ON_CLICK, StudioXmlAttributes.ICON_AFTER_TEXT,
                StudioXmlAttributes.WHITE_SPACE).contains(attributeName)
                || attributeName.endsWith("Color")) {
            return resolveCategoryConstantName(StudioProperty.Category.LOOK_AND_FEEL);
        }
        if (VALIDATION_NAMES.contains(attributeName)) {
            return resolveCategoryConstantName(StudioProperty.Category.VALIDATION);
        }
        if (Set.of(StudioXmlAttributes.FETCH_PLAN, StudioXmlAttributes.LOADER, StudioXmlAttributes.QUERY)
                .contains(attributeName)) {
            return resolveCategoryConstantName(StudioProperty.Category.GENERAL);
        }
        if (Set.of(StudioXmlAttributes.ACTION, StudioXmlAttributes.ID, StudioXmlAttributes.SHORTCUT_COMBINATION,
                StudioXmlAttributes.TEXT, StudioXmlAttributes.DESCRIPTION, StudioXmlAttributes.ENABLED,
                StudioXmlAttributes.TITLE, StudioXmlAttributes.VISIBLE).contains(attributeName)) {
            return resolveCategoryConstantName(StudioProperty.Category.GENERAL);
        }
        return null;
    }

    private String resolveCategoryConstantName(String categoryConstantValue) {
        if (StudioProperty.Category.GENERAL.equals(categoryConstantValue)) {
            return "GENERAL";
        }
        if (StudioProperty.Category.DATA_BINDING.equals(categoryConstantValue)) {
            return "DATA_BINDING";
        }
        if (StudioProperty.Category.SIZE.equals(categoryConstantValue)) {
            return "SIZE";
        }
        if (StudioProperty.Category.POSITION.equals(categoryConstantValue)) {
            return "POSITION";
        }
        if (StudioProperty.Category.LOOK_AND_FEEL.equals(categoryConstantValue)) {
            return "LOOK_AND_FEEL";
        }
        if (StudioProperty.Category.VALIDATION.equals(categoryConstantValue)) {
            return "VALIDATION";
        }
        if (StudioProperty.Category.ADDITIONAL.equals(categoryConstantValue)) {
            return "ADDITIONAL";
        }
        return "OTHER";
    }

    private List<PropertyInfo> filterCoveredProperties(List<PropertyInfo> properties, List<String> propertyGroups) {
        if (propertyGroups.isEmpty()) {
            return List.copyOf(properties);
        }

        List<StudioPropertySignature> groupProperties = propertyGroupsMatcher.flattenProperties(propertyGroups);
        return properties.stream()
                .filter(property -> groupProperties.stream().noneMatch(groupProperty -> isCoveredByGroup(property, groupProperty)))
                .toList();
    }

    private boolean isCoveredByGroup(PropertyInfo property, StudioPropertySignature groupProperty) {
        return Objects.equals(groupProperty.xmlAttribute(), property.xmlAttribute())
                && Objects.equals(groupProperty.type(), property.studioType())
                && coveredCategoryMatches(groupProperty.category(), property.category())
                && groupProperty.required() == property.required()
                && coveredDefaultValueMatches(groupProperty.defaultValue(), property.defaultValue())
                && coveredOptionsMatch(groupProperty.options(), property.options());
    }

    private boolean coveredCategoryMatches(@Nullable String groupCategory, @Nullable String propertyCategory) {
        return Objects.equals(groupCategory, propertyCategory)
                || groupCategory == null
                || propertyCategory == null
                || "OTHER".equals(propertyCategory);
    }

    private boolean coveredDefaultValueMatches(String groupDefaultValue, @Nullable String propertyDefaultValue) {
        return propertyDefaultValue == null || Objects.equals(groupDefaultValue, propertyDefaultValue);
    }

    private boolean coveredOptionsMatch(List<String> groupOptions, List<String> propertyOptions) {
        return propertyOptions.isEmpty() || Objects.equals(groupOptions, propertyOptions);
    }

    private @Nullable String inferXmlnsAlias(SchemaDocument document) {
        if (FLOWUI_LAYOUT_NS.equals(document.targetNamespace())) {
            return "";
        }

        Path moduleRoot = findModuleRoot(document.path());
        if (moduleRoot == null) {
            return null;
        }

        Path srcDir = moduleRoot.resolve("src");
        if (!Files.isDirectory(srcDir)) {
            return null;
        }

        Pattern aliasPattern = Pattern.compile(XMLNS_ALIAS_PATTERN_TEMPLATE.pattern().formatted(Pattern.quote(document.targetNamespace())));
        Pattern defaultPattern = Pattern.compile(XMLNS_DEFAULT_PATTERN_TEMPLATE.pattern().formatted(Pattern.quote(document.targetNamespace())));

        Map<String, Integer> aliases = new HashMap<>();
        boolean defaultNamespaceFound = false;
        try {
            //noinspection resource
            Files.walk(srcDir)
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".xml"))
                    .forEach(path -> {
                        if (containsIgnoredDirectory(path)) {
                            return;
                        }
                        try {
                            String content = Files.readString(path, StandardCharsets.UTF_8);
                            Matcher aliasMatcher = aliasPattern.matcher(content);
                            while (aliasMatcher.find()) {
                                aliases.merge(aliasMatcher.group(1), 1, Integer::sum);
                            }
                            if (defaultPattern.matcher(content).find()) {
                                aliases.putIfAbsent("", 0);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw new IllegalStateException("Cannot scan XML files for namespace aliases", ioException);
            }
            throw e;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan XML files for namespace aliases", e);
        }

        if (aliases.containsKey("")) {
            defaultNamespaceFound = true;
        }
        aliases.remove("");

        if (!aliases.isEmpty()) {
            return aliases.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        if (defaultNamespaceFound || document.targetNamespace().startsWith("http://jmix.io/schema/flowui/")) {
            return "";
        }
        return null;
    }

    private List<String> buildHeaderTodos(StudioXsdElementCandidate candidate,
                                          StudioMetaKind kind,
                                          @Nullable String xmlns,
                                          @Nullable String xmlnsAlias,
                                          List<PropertyInfo> properties,
                                          List<String> propertyGroups) {
        List<String> todos = new ArrayList<>();
        switch (kind) {
            case COMPONENT ->
                    todos.add("Replace Object with the actual component class and fill `classFqn`, `category`, `icon` "
                            + "and `documentationLink`.");
            case ELEMENT ->
                    todos.add("Fill `classFqn`, `icon`, `target`/`unsupportedTarget` and `documentationLink` after "
                            + "checking the Java implementation.");
            case FACET ->
                    todos.add("Fill `classFqn`, `category`, `icon` and `documentationLink` after checking the Java "
                            + "implementation.");
            case DATA_COMPONENT ->
                    todos.add("Fill `classFqn`, `category`, `icon` and `documentationLink` after checking the "
                            + "Java implementation.");
            case ACTION ->
                    todos.add("Fill `type`, `description`, `classFqn`, `icon` and `documentationLink` after checking "
                            + "the Java implementation.");
            default -> {
            }
        }

        if (xmlns != null && xmlnsAlias == null && !FLOWUI_LAYOUT_NS.equals(xmlns)) {
            todos.add("The tool could not infer `xmlnsAlias`. Review XML samples in the module and set it manually if the "
                    + "namespace uses a prefix.");
        }
        if (!candidate.ancestorElements().isEmpty() && Set.of(StudioMetaKind.ELEMENT, StudioMetaKind.ACTION).contains(kind)) {
            todos.add("The selected XSD element is nested under `"
                    + String.join(" / ", candidate.ancestorElements())
                    + "`. Verify `target` and nesting semantics manually.");
        }
        boolean hasPropertyTodos = properties.stream().anyMatch(propertyInfo -> !propertyInfo.comments().isEmpty());
        if (hasPropertyTodos) {
            todos.add("Review properties with inline TODO comments. They require manual confirmation.");
        }
        if (propertyGroups.isEmpty()) {
            todos.add("No compatible existing `propertyGroups` match was found automatically. Extract a reusable group manually "
                    + "if this property set repeats elsewhere.");
        }
        return todos;
    }

    private String buildMethodName(StudioXsdElementCandidate candidate, StudioMetaKind kind) {
        String base = candidate.elementName();
        if (GENERIC_METHOD_NAMES.contains(candidate.elementName())) {
            List<String> contextSource = !candidate.ancestorElements().isEmpty()
                    ? candidate.ancestorElements()
                    : candidate.contextNames().subList(0, Math.max(0, candidate.contextNames().size() - 1));
            if (!contextSource.isEmpty()) {
                base = contextSource.getLast() + " " + candidate.elementName();
            }
        }

        String methodName = toCamelCase(base);
        if (kind == StudioMetaKind.ACTION && !methodName.endsWith("Action")) {
            methodName += "Action";
        }
        return methodName;
    }

    private String buildJavaSource(Path outputPath, GeneratedMeta meta, Path selectedXsd) {
        String packageName = detectPackageName(outputPath);
        String interfaceName = outputPath.getFileName().toString().replaceFirst("\\.java$", "");
        List<String> sourceLines = new ArrayList<>();
        sourceLines.add("""
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
                """.stripTrailing());
        sourceLines.add("");
        sourceLines.add("package " + packageName + ";");
        sourceLines.add("");
        sourceLines.add("import io.jmix.flowui.kit.meta.*;");
        sourceLines.add("");
        sourceLines.add("// Generated from " + relativizeIfPossible(selectedXsd) + ".");
        sourceLines.add("// Review all TODO comments before using the generated meta description in production.");
        sourceLines.add("@StudioUiKit");
        sourceLines.add("interface " + interfaceName + " {");
        sourceLines.add("");
        sourceLines.addAll(renderAnnotation(meta));
        sourceLines.add("}");
        sourceLines.add("");
        return String.join("\n", sourceLines);
    }

    private List<String> renderAnnotation(GeneratedMeta meta) {
        String annotationName = switch (meta.kind()) {
            case COMPONENT -> "StudioComponent";
            case ELEMENT -> "StudioElement";
            case ACTION -> "StudioAction";
            case FACET -> "StudioFacet";
            case DATA_COMPONENT -> "StudioDataComponent";
            default -> throw new IllegalStateException("Unexpected value: " + meta.kind());
        };

        List<String> lines = new ArrayList<>();
        if (meta.kindComment() != null) {
            lines.add("    // TODO " + meta.kindComment());
        }
        for (String todo : meta.headerTodos()) {
            lines.add("    // TODO " + todo);
        }
        if (!meta.childElements().isEmpty()) {
            lines.add("    // TODO XSD declares child elements: "
                    + String.join(", ", meta.childElements())
                    + ". Generate nested meta descriptions if needed.");
        }
        if (meta.supportsAnyChildren()) {
            lines.add("    // TODO The XSD also allows arbitrary nested elements via <xs:any>. Review available children manually.");
        }

        lines.add("    @" + annotationName + "(");
        List<List<String>> argumentBlocks = new ArrayList<>();
        argumentBlocks.add(List.of("            name = " + javaQuote(meta.name())));
        String xmlElementReference = StudioXmlElements.resolveConstantReference(meta.xmlElement());
        argumentBlocks.add(List.of("            xmlElement = "
                + (xmlElementReference != null ? xmlElementReference : javaQuote(meta.xmlElement()))));
        if (meta.xmlns() != null) {
            argumentBlocks.add(List.of("            xmlns = " + javaQuote(meta.xmlns())));
        }
        if (meta.xmlnsAlias() != null && !meta.xmlnsAlias().isEmpty()) {
            argumentBlocks.add(List.of("            xmlnsAlias = " + javaQuote(meta.xmlnsAlias())));
        }

        for (String todoAttribute : List.of("classFqn", "category", "icon", "documentationLink")) {
            argumentBlocks.add(List.of("            " + todoAttribute + " = " + javaQuote("TODO")));
        }

        if (!meta.propertyGroups().isEmpty()) {
            argumentBlocks.add(getPropertyGroupsBlock(meta));
        }

        List<String> propertiesBlock = new ArrayList<>();
        propertiesBlock.add("            properties = {");
        if (!meta.properties().isEmpty()) {
            for (PropertyInfo property : meta.properties()) {
                List<String> renderedProperty = renderProperty(property);
                for (int i = renderedProperty.size() - 1; i >= 0; i--) {
                    if (renderedProperty.get(i).stripLeading().startsWith("@StudioProperty")) {
                        renderedProperty.set(i, renderedProperty.get(i) + ",");
                        break;
                    }
                }
                propertiesBlock.addAll(renderedProperty);
            }
        } else {
            propertiesBlock.add("                    // TODO No XML attributes were discovered for this element.");
        }
        propertiesBlock.add("            }");
        argumentBlocks.add(propertiesBlock);

        for (int index = 0; index < argumentBlocks.size(); index++) {
            List<String> block = argumentBlocks.get(index);
            boolean last = index == argumentBlocks.size() - 1;
            if (block.size() == 1) {
                lines.add(block.getFirst() + (last ? "" : ","));
                continue;
            }
            lines.addAll(block.subList(0, block.size() - 1));
            lines.add(block.getLast() + (last ? "" : ","));
        }
        lines.add("    )");
        String returnType = meta.methodReturnType();
        if (!Void.class.getName().toLowerCase().equals(returnType)) {
            lines.add("    // TODO Define correct kit class that should be used in Studio preview\n" +
                    "      // or void if component should not be visible in preview");
        }
        lines.add("    " + returnType + " " + meta.methodName() + "();");
        return lines;
    }

    private List<String> getPropertyGroupsBlock(GeneratedMeta meta) {
        List<String> propertyGroupsBlock = new ArrayList<>();
        propertyGroupsBlock.add("            propertyGroups = {");
        for (String propertyGroup : meta.propertyGroups()) {
            propertyGroupsBlock.add("                    " + propertyGroup + ".class,");
        }
        int lastIndex = propertyGroupsBlock.size() - 1;
        propertyGroupsBlock.set(lastIndex, propertyGroupsBlock.get(lastIndex).replaceFirst(",$", ""));
        propertyGroupsBlock.add("            }");
        return propertyGroupsBlock;
    }

    private List<String> renderProperty(PropertyInfo propertyInfo) {
        List<String> lines = new ArrayList<>();
        for (String comment : propertyInfo.comments()) {
            lines.add("                    // TODO " + comment);
        }

        List<String> arguments = new ArrayList<>();
        String xmlAttributeReference = StudioXmlAttributes.resolveConstantReference(propertyInfo.xmlAttribute());
        arguments.add("xmlAttribute = "
                + (xmlAttributeReference != null ? xmlAttributeReference : javaQuote(propertyInfo.xmlAttribute())));
        if (propertyInfo.category() != null) {
            arguments.add("category = StudioProperty.Category." + propertyInfo.category());
        }
        arguments.add("type = StudioPropertyType." + propertyInfo.studioType());
        if (propertyInfo.required()) {
            arguments.add("required = true");
        }
        if (propertyInfo.defaultValue() != null) {
            arguments.add("defaultValue = " + javaQuote(propertyInfo.defaultValue()));
        }
        if (!propertyInfo.options().isEmpty()) {
            List<String> quotedOptions = propertyInfo.options().stream()
                    .map(StudioMetaDescriptionGenerator::javaQuote)
                    .toList();
            arguments.add("options = {" + String.join(", ", quotedOptions) + "}");
        }
        lines.add("                    @StudioProperty(" + String.join(", ", arguments) + ")");
        return lines;
    }

    private String buildUpdatedSourceForExistingFile(Path outputPath, List<String> generatedBlock) {
        try {
            String original = Files.readString(outputPath, StandardCharsets.UTF_8);
            return buildUpdatedSourceForExistingFile(original, outputPath, generatedBlock);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot update " + outputPath, e);
        }
    }

    private String buildUpdatedSourceForExistingFile(String original, Path outputPath, List<String> generatedBlock) {
        return appendGeneratedBlock(original, outputPath, generatedBlock);
    }

    private String appendGeneratedBlock(String original, Path outputPath, List<String> generatedBlock) {
        if (!original.contains("@StudioUiKit")) {
            throw new IllegalArgumentException(outputPath + " does not look like a Studio meta file. Create a new file instead.");
        }

        String updated = original;
        if (!updated.contains("import io.jmix.flowui.kit.meta.*;")) {
            Matcher packageMatcher = Pattern.compile("^package\\s+[^;]+;\\R", Pattern.MULTILINE).matcher(updated);
            if (packageMatcher.find()) {
                int insertIndex = packageMatcher.end();
                updated = updated.substring(0, insertIndex)
                        + "\nimport io.jmix.flowui.kit.meta.*;\n"
                        + updated.substring(insertIndex);
            }
        }

        int lastBraceIndex = updated.lastIndexOf('}');
        if (lastBraceIndex < 0) {
            throw new IllegalArgumentException("Cannot append generated block to " + outputPath
                    + ": interface closing brace was not found.");
        }

        String block = "\n" + String.join("\n", generatedBlock) + "\n";
        updated = updated.substring(0, lastBraceIndex).stripTrailing()
                + "\n\n"
                + block
                + updated.substring(lastBraceIndex);
        return updated;
    }

    private String ensureUniqueMethodName(Path outputPath, String methodName) {
        if (!Files.exists(outputPath)) {
            return methodName;
        }
        try {
            String content = Files.readString(outputPath, StandardCharsets.UTF_8);
            Pattern methodPattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(");
            if (methodPattern.matcher(content).find()) {
                return methodName + "Generated";
            }
            return methodName;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + outputPath + " to verify method name uniqueness", e);
        }
    }

    private Path resolveAbsolutePath(Path path) {
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return Path.of("").toAbsolutePath().resolve(path).normalize();
    }

    private static boolean containsActionContext(StudioXsdElementCandidate candidate) {
        return candidate.contextNames().stream().anyMatch(name ->
                name.endsWith("Actions")
                        || name.endsWith("Action")
                        || "viewActions".equals(name)
                        || "actions".equals(name)
        ) || candidate.ancestorElements().contains("actions");
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean containsContextName(StudioXsdElementCandidate candidate, String value) {
        return candidate.contextNames().contains(value) || candidate.ancestorElements().contains(value);
    }

    private static boolean containsIgnoredDirectory(Path path) {
        for (Path part : path) {
            if (IGNORED_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Path findModuleRoot(Path schemaPath) {
        Path current = schemaPath.toAbsolutePath().normalize();
        for (int i = 0; i < current.getNameCount(); i++) {
            if ("src".equals(current.getName(i).toString())) {
                return current.getRoot() != null
                        ? current.getRoot().resolve(current.subpath(0, i))
                        : current.subpath(0, i);
            }
        }
        return null;
    }

    private static Optional<String> readJavaPackage(Path javaFile) {
        try {
            for (String line : Files.readAllLines(javaFile, StandardCharsets.UTF_8)) {
                Matcher matcher = JAVA_PACKAGE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    return Optional.of(matcher.group(1));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + javaFile, e);
        }
        return Optional.empty();
    }

    private String detectPackageName(Path outputPath) {
        if (Files.exists(outputPath)) {
            Optional<String> existingPackage = readJavaPackage(outputPath);
            if (existingPackage.isPresent()) {
                return existingPackage.get();
            }
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < outputPath.getNameCount(); i++) {
            if ("java".equals(outputPath.getName(i).toString())) {
                for (int j = i + 1; j < outputPath.getNameCount() - 1; j++) {
                    parts.add(outputPath.getName(j).toString());
                }
                break;
            }
        }
        return parts.isEmpty() ? "io.jmix.generated" : String.join(".", parts);
    }

    private String relativizeIfPossible(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.startsWith(schemaSearchRoot)) {
            return schemaSearchRoot.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString();
    }

    private static @Nullable String firstNonEmptyOrNull(String... values) {
        for (String value : values) {
            if (!value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static String javaQuote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static String toPascalCase(String value) {
        StringBuilder result = new StringBuilder();
        for (String part : splitWords(value)) {
            if (part.isEmpty()) {
                continue;
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.toString();
    }

    private static String toCamelCase(String value) {
        List<String> parts = splitWords(value);
        if (parts.isEmpty()) {
            return "generatedMeta";
        }
        StringBuilder result = new StringBuilder();
        String firstPart = parts.getFirst();
        result.append(Character.toLowerCase(firstPart.charAt(0)));
        if (firstPart.length() > 1) {
            result.append(firstPart.substring(1));
        }
        for (int i = 1; i < parts.size(); i++) {
            String part = parts.get(i);
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        String candidate = result.toString();
        return switch (candidate) {
            case "class", "default", "enum", "public", "switch" -> candidate + "Value";
            default -> candidate;
        };
    }

    private static List<String> splitWords(String value) {
        String normalized = value
                .replace('-', ' ')
                .replace('_', ' ')
                .replace('.', ' ')
                .replace(':', ' ')
                .replace('/', ' ');
        normalized = normalized.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        normalized = normalized.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
        String[] parts = normalized.trim().split("\\s+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                result.add(part);
            }
        }
        return result;
    }

    private static String localName(Node node) {
        String localName = node.getLocalName();
        return localName != null ? localName : node.getNodeName();
    }

    private static @Nullable Element firstChildElement(Element element, String childLocalName) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && childLocalName.equals(localName(child))) {
                return (Element) child;
            }
        }
        return null;
    }

    private static List<Element> childElements(Element element) {
        List<Element> children = new ArrayList<>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) child);
            }
        }
        return children;
    }

    private static List<Element> childElements(Element element, String childLocalName) {
        List<Element> children = new ArrayList<>();
        for (Element child : childElements(element)) {
            if (childLocalName.equals(localName(child))) {
                children.add(child);
            }
        }
        return children;
    }

    private record DefinitionKey(String namespace, String localName) {
    }

    private record QNameRef(@Nullable String namespace, String localName) {
    }

    static final class SchemaDocument {
        private final Path path;
        private final String targetNamespace;
        private final Map<String, String> namespaceMap;
        private final Document document;
        private final Element root;
        private final Map<Element, Element> parentMap;

        private SchemaDocument(Path path,
                               String targetNamespace,
                               Map<String, String> namespaceMap,
                               Document document,
                               Element root,
                               Map<Element, Element> parentMap) {
            this.path = path;
            this.targetNamespace = targetNamespace;
            this.namespaceMap = namespaceMap;
            this.document = document;
            this.root = root;
            this.parentMap = parentMap;
        }

        Path path() {
            return path;
        }

        String targetNamespace() {
            return targetNamespace;
        }

        Map<String, String> namespaceMap() {
            return namespaceMap;
        }

        Document document() {
            return document;
        }

        Element root() {
            return root;
        }

        Map<Element, Element> parentMap() {
            return parentMap;
        }
    }

    private record DefinitionRef(SchemaDocument document, @Nullable Element element, @Nullable QNameRef qName) {
        private DefinitionRef(SchemaDocument document, @Nullable Element element) {
            this(document, element, null);
        }

    }

    private record ResolvedType(@Nullable DefinitionRef ref, @Nullable String kind) {
    }

    private record AttributeInfo(String xmlAttribute,
                                 boolean required,
                                 @Nullable String defaultValue,
                                 @Nullable QNameRef typeQName,
                                 @Nullable Element inlineSimpleType,
                                 SchemaDocument sourceDocument,
                                 List<String> comments) {
    }

    private record PropertyInfo(String xmlAttribute,
                                String studioType,
                                @Nullable String category,
                                boolean required,
                                @Nullable String defaultValue,
                                List<String> options,
                                List<String> comments) {
    }

    private record ChildrenInfo(List<String> childElements, boolean supportsAnyChildren) {
    }

    private record EnumValues(List<String> values, boolean allowsFreeForm) {
    }

    private record KindInfo(StudioMetaKind kind, @Nullable String kindComment) {
    }

    private record PropertyGroupsResolution(List<String> propertyGroups, List<PropertyInfo> inlineProperties) {
    }

    private record VisitedNodeKey(Path path, int identityHash) {
    }

    private record GeneratedMeta(StudioMetaKind kind,
                                 @Nullable String kindComment,
                                 String name,
                                 String xmlElement,
                                 @Nullable String xmlns,
                                 @Nullable String xmlnsAlias,
                                 List<String> headerTodos,
                                 List<String> propertyGroups,
                                 List<PropertyInfo> properties,
                                 List<String> childElements,
                                 boolean supportsAnyChildren,
                                 String methodName,
                                 String methodReturnType) {

        private GeneratedMeta withMethodName(String methodName) {
            return new GeneratedMeta(kind, kindComment, name, xmlElement, xmlns, xmlnsAlias, headerTodos,
                    propertyGroups, properties, childElements, supportsAnyChildren, methodName, methodReturnType);
        }
    }

    private static final class XsdRegistry {

        private final Path schemaSearchRoot;
        private final Map<Path, SchemaDocument> documents = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> elementDefinitions = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> complexTypeDefinitions = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> simpleTypeDefinitions = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> attributeGroupDefinitions = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> attributeDefinitions = new HashMap<>();
        private final Map<DefinitionKey, List<DefinitionRef>> groupDefinitions = new HashMap<>();

        private XsdRegistry(Path schemaSearchRoot) {
            this.schemaSearchRoot = schemaSearchRoot;
        }

        private List<Path> discoverSchemas() {
            List<Path> schemas = new ArrayList<>();
            FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(schemaSearchRoot) && IGNORED_DIRS.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".xsd")) {
                        schemas.add(file.toAbsolutePath().normalize());
                    }
                    return FileVisitResult.CONTINUE;
                }
            };

            try {
                Files.walkFileTree(schemaSearchRoot, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot discover XSD files under " + schemaSearchRoot, e);
            }

            schemas.sort(Comparator.naturalOrder());
            for (Path schema : schemas) {
                loadDocument(schema);
            }
            return schemas;
        }

        private List<StudioXsdElementCandidate> findCandidates(Path schemaPath, String elementIdentifier) {
            SchemaDocument document = loadDocument(schemaPath);
            List<StudioXsdElementCandidate> candidates = new ArrayList<>();
            for (Element element : allElements(document.document())) {
                if (!XS_NS.equals(element.getNamespaceURI()) || !"element".equals(localName(element))) {
                    continue;
                }
                if (!elementIdentifier.equalsIgnoreCase(element.getAttribute("name"))) {
                    continue;
                }

                List<String> ancestorElements = collectAncestorElements(document, element);
                List<String> contextNames = collectContextNames(document, element);
                boolean nested = document.parentMap().get(element) != document.root();
                String kind = nested ? "nested" : "global";
                String description = String.join("/", contextNames)
                        + " (" + kind + ", " + relativize(schemaSearchRoot, schemaPath) + ")";
                candidates.add(new StudioXsdElementCandidate(
                        document.path(),
                        element.getAttribute("name"),
                        nested,
                        ancestorElements,
                        contextNames,
                        description,
                        document,
                        element
                ));
            }
            return candidates;
        }

        private SchemaDocument loadDocument(Path schemaPath) {
            Path normalizedPath = schemaPath.toAbsolutePath().normalize();
            SchemaDocument existing = documents.get(normalizedPath);
            if (existing != null) {
                return existing;
            }

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                Document document = factory.newDocumentBuilder().parse(normalizedPath.toFile());
                Element root = document.getDocumentElement();
                Map<String, String> namespaceMap = extractNamespaces(root);
                Map<Element, Element> parentMap = buildParentMap(root);

                SchemaDocument schemaDocument = new SchemaDocument(
                        normalizedPath,
                        root.getAttribute("targetNamespace"),
                        namespaceMap,
                        document,
                        root,
                        parentMap
                );
                documents.put(normalizedPath, schemaDocument);
                indexDocument(schemaDocument);
                return schemaDocument;
            } catch (Exception e) {
                throw new IllegalStateException("Cannot parse XSD document " + normalizedPath, e);
            }
        }

        private void indexDocument(SchemaDocument document) {
            for (Element child : childElements(document.root())) {
                String name = child.getAttribute("name");
                if (name.isEmpty()) {
                    continue;
                }
                DefinitionRef definitionRef = new DefinitionRef(document, child);
                DefinitionKey definitionKey = new DefinitionKey(document.targetNamespace(), name);
                switch (localName(child)) {
                    case "element" ->
                            elementDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    case "complexType" ->
                            complexTypeDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    case "simpleType" ->
                            simpleTypeDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    case "attributeGroup" ->
                            attributeGroupDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    case "attribute" ->
                            attributeDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    case "group" ->
                            groupDefinitions.computeIfAbsent(definitionKey, key -> new ArrayList<>()).add(definitionRef);
                    default -> {
                    }
                }
            }
        }

        private QNameRef resolveQName(String qName, SchemaDocument document) {
            if (qName.contains(":")) {
                String[] split = qName.split(":", 2);
                return new QNameRef(document.namespaceMap().get(split[0]), split[1]);
            }
            if (BUILTIN_TYPE_MAP.containsKey(qName)) {
                return new QNameRef(XS_NS, qName);
            }
            return new QNameRef(document.targetNamespace(), qName);
        }

        private @Nullable DefinitionRef findDefinition(Map<DefinitionKey, List<DefinitionRef>> index, QNameRef qName) {
            assert qName.namespace() != null : "Namespace cannot be null for QNameRef: " + qName;
            List<DefinitionRef> definitions = index.get(new DefinitionKey(qName.namespace(), qName.localName()));
            return definitions == null || definitions.isEmpty() ? null : definitions.getFirst();
        }

        private Map<String, String> extractNamespaces(Element root) {
            Map<String, String> namespaces = new HashMap<>();
            NamedNodeMap attributes = root.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String attributeName = attribute.getName();
                if ("xmlns".equals(attributeName)) {
                    namespaces.put("", attribute.getValue());
                } else if (attributeName.startsWith("xmlns:")) {
                    namespaces.put(attributeName.substring("xmlns:".length()), attribute.getValue());
                }
            }
            return namespaces;
        }

        private Map<Element, Element> buildParentMap(Element root) {
            Map<Element, Element> parentMap = new IdentityHashMap<>();
            ArrayDeque<Element> stack = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                Element current = stack.pop();
                for (Element child : childElements(current)) {
                    parentMap.put(child, current);
                    stack.push(child);
                }
            }
            return parentMap;
        }

        private List<Element> allElements(Document document) {
            List<Element> elements = new ArrayList<>();
            ArrayDeque<Element> stack = new ArrayDeque<>();
            stack.push(document.getDocumentElement());
            while (!stack.isEmpty()) {
                Element current = stack.pop();
                elements.add(current);
                List<Element> children = childElements(current);
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
            return elements;
        }

        private List<String> collectAncestorElements(SchemaDocument document, Element element) {
            List<String> ancestorElements = new ArrayList<>();
            Element current = element;
            while (document.parentMap().containsKey(current)) {
                current = document.parentMap().get(current);
                if ("element".equals(localName(current)) && current.hasAttribute("name")) {
                    ancestorElements.add(current.getAttribute("name"));
                }
            }
            Collections.reverse(ancestorElements);
            return ancestorElements;
        }

        private List<String> collectContextNames(SchemaDocument document, Element element) {
            List<String> contextNames = new ArrayList<>();
            Element current = element;
            while (true) {
                if (Set.of("element", "complexType").contains(localName(current)) && current.hasAttribute("name")) {
                    contextNames.add(current.getAttribute("name"));
                }
                Element parent = document.parentMap().get(current);
                if (parent == null) {
                    break;
                }
                current = parent;
            }
            Collections.reverse(contextNames);
            return contextNames;
        }

        private String relativize(Path base, Path path) {
            Path normalizedPath = path.toAbsolutePath().normalize();
            if (normalizedPath.startsWith(base.toAbsolutePath().normalize())) {
                return base.toAbsolutePath().normalize().relativize(normalizedPath).toString().replace('\\', '/');
            }
            return normalizedPath.toString();
        }
    }

    static final class CLI {

        static void run(String[] args) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            CLI.CliArguments cliArguments = CLI.CliArguments.parse(args);
            Path workspaceRoot = resolveWorkspaceRoot(cliArguments, reader);
            StudioMetaDescriptionGenerator generator = new StudioMetaDescriptionGenerator(workspaceRoot);
            List<Path> knownSchemas = generator.findKnownSchemas();

            if (cliArguments.listSchemas()) {
                for (Path schema : knownSchemas) {
                    System.out.println(relativize(workspaceRoot, schema));
                }
                if (!cliArguments.hasGenerationArguments()) {
                    return;
                }
            }

            Path schemaPath = resolveSchemaPath(cliArguments, workspaceRoot, knownSchemas, reader);
            String elementIdentifier = cliArguments.element() != null
                    ? cliArguments.element()
                    : prompt(reader, "Enter XSD element identifier", null);
            StudioXsdElementCandidate candidate = chooseCandidate(generator, schemaPath, elementIdentifier, reader);
            StudioMetaKind kind = resolveKind(cliArguments, reader);
            Path outputPath = resolveOutputPath(cliArguments, generator, candidate, workspaceRoot, reader);

            StudioMetaGenerationResult result = generator.write(candidate, kind, outputPath);
            System.out.println("Generated meta description in " + result.outputPath());
            if (!result.todos().isEmpty()) {
                System.out.println("Review TODO comments before finalizing the file.");
            }
        }

        private static Path resolveWorkspaceRoot(CliArguments cliArguments, BufferedReader reader) throws IOException {
            if (cliArguments.root() != null) {
                return resolvePath(cliArguments.root());
            }

            Path detectedWorkspaceRoot = detectWorkspaceRoot(Path.of("").toAbsolutePath());
            if (!cliArguments.interactive()) {
                return detectedWorkspaceRoot;
            }

            String rawValue = prompt(reader, "Enter workspace root", detectedWorkspaceRoot.toString());
            return resolvePath(rawValue);
        }

        private static Path resolveSchemaPath(CliArguments cliArguments,
                                              Path workspaceRoot,
                                              List<Path> knownSchemas,
                                              BufferedReader reader) throws IOException {
            if (cliArguments.xsd() != null) {
                return resolvePath(cliArguments.xsd());
            }

            if (knownSchemas.isEmpty()) {
                return resolvePath(prompt(reader, "Enter XSD path", null));
            }

            System.out.println("Known XSD schemas:");
            for (int i = 0; i < knownSchemas.size(); i++) {
                System.out.printf("%2d. %s%n", i + 1, relativize(workspaceRoot, knownSchemas.get(i)));
            }

            while (true) {
                String rawValue = prompt(reader, "Choose a schema by number or enter a path", null);
                if (rawValue.matches("\\d+")) {
                    int index = Integer.parseInt(rawValue);
                    if (index >= 1 && index <= knownSchemas.size()) {
                        return knownSchemas.get(index - 1);
                    }
                }

                Path path = resolvePath(rawValue, workspaceRoot);
                if (Files.exists(path)) {
                    return path;
                }
                System.out.println("Invalid schema selection.");
            }
        }

        private static StudioXsdElementCandidate chooseCandidate(StudioMetaDescriptionGenerator generator,
                                                                 Path schemaPath,
                                                                 String elementIdentifier,
                                                                 BufferedReader reader) throws IOException {
            List<StudioXsdElementCandidate> candidates = generator.findElementCandidates(schemaPath, elementIdentifier);
            while (candidates.isEmpty()) {
                System.out.printf("No XSD element \"%s\" was found in %s.%n", elementIdentifier, schemaPath);
                elementIdentifier = prompt(reader, "Enter another element identifier", null);
                candidates = generator.findElementCandidates(schemaPath, elementIdentifier);
            }

            if (candidates.size() == 1) {
                return candidates.getFirst();
            }

            System.out.printf("Multiple XSD elements named \"%s\" were found:%n", elementIdentifier);
            for (int i = 0; i < candidates.size(); i++) {
                System.out.printf("%2d. %s%n", i + 1, candidates.get(i).description());
            }

            while (true) {
                String rawValue = prompt(reader, "Choose the exact element by number", null);
                if (rawValue.matches("\\d+")) {
                    int index = Integer.parseInt(rawValue);
                    if (index >= 1 && index <= candidates.size()) {
                        return candidates.get(index - 1);
                    }
                }
                System.out.println("Invalid element selection.");
            }
        }

        private static StudioMetaKind resolveKind(CliArguments cliArguments, BufferedReader reader) throws IOException {
            if (!cliArguments.interactive() || cliArguments.kindSpecified()) {
                return cliArguments.kind();
            }

            while (true) {
                String rawValue = prompt(reader,
                        "Enter meta kind (auto/component/element/action/facet/data-component)",
                        "auto");
                StudioMetaKind kind = CliArguments.tryParseKind(rawValue, null);
                if (kind != null) {
                    return kind;
                }
                System.out.println("Invalid meta kind.");
            }
        }

        private static Path resolveOutputPath(CliArguments cliArguments,
                                              StudioMetaDescriptionGenerator generator,
                                              StudioXsdElementCandidate candidate,
                                              Path workspaceRoot,
                                              BufferedReader reader) throws IOException {
            if (cliArguments.output() != null) {
                return resolvePath(cliArguments.output());
            }

            Path defaultOutputPath = generator.getDefaultOutputPath(candidate);
            if (!cliArguments.interactive()) {
                return defaultOutputPath;
            }

            String rawValue = prompt(reader, "Enter output path", relativize(workspaceRoot, defaultOutputPath));
            return resolvePath(rawValue, workspaceRoot);
        }

        private static String prompt(BufferedReader reader, String message, @Nullable String defaultValue) throws IOException {
            while (true) {
                String suffix = defaultValue != null ? " [" + defaultValue + "]" : "";
                System.out.print(message + suffix + ": ");
                String value = reader.readLine();
                if (value == null) {
                    throw new IOException("Input stream was closed.");
                }
                value = value.trim();
                if (!value.isEmpty()) {
                    return value;
                }
                if (defaultValue != null) {
                    return defaultValue;
                }
            }
        }

        private static Path resolvePath(String pathValue) {
            return resolvePath(pathValue, Path.of("").toAbsolutePath());
        }

        private static Path resolvePath(String pathValue, Path basePath) {
            Path path = Path.of(pathValue);
            if (!path.isAbsolute()) {
                return basePath.toAbsolutePath().resolve(path).normalize();
            }
            return path.normalize();
        }

        private static String relativize(Path base, Path path) {
            Path normalizedBase = base.toAbsolutePath().normalize();
            Path normalizedPath = path.toAbsolutePath().normalize();
            if (normalizedPath.startsWith(normalizedBase)) {
                return normalizedBase.relativize(normalizedPath).toString().replace('\\', '/');
            }
            return normalizedPath.toString();
        }

        private record CliArguments(@Nullable String xsd,
                                    @Nullable String element,
                                    @Nullable String output,
                                    @Nullable String root,
                                    StudioMetaKind kind,
                                    boolean kindSpecified,
                                    boolean listSchemas,
                                    boolean interactive) {

            private static CliArguments parse(String[] args) {
                String xsd = null;
                String element = null;
                String output = null;
                String root = null;
                StudioMetaKind kind = StudioMetaKind.AUTO;
                boolean kindSpecified = false;
                boolean listSchemas = false;
                boolean interactive = args.length == 0;

                for (int i = 0; i < args.length; i++) {
                    String current = args[i];
                    switch (current) {
                        case "--xsd" -> xsd = requireValue(args, ++i);
                        case "--element" -> element = requireValue(args, ++i);
                        case "--output" -> output = requireValue(args, ++i);
                        case "--root" -> root = requireValue(args, ++i);
                        case "--kind" -> {
                            String rawKind = requireValue(args, ++i);
                            kind = parseKind(rawKind);
                            kindSpecified = rawKind != null;
                        }
                        case "--list-schemas" -> listSchemas = true;
                        default -> throw new IllegalArgumentException("Unknown argument: " + current);
                    }
                }

                return new CliArguments(xsd, element, output, root, kind, kindSpecified, listSchemas, interactive);
            }

            private boolean hasGenerationArguments() {
                return xsd != null || element != null || output != null || kindSpecified;
            }

            @Nullable
            private static String requireValue(String[] args, int index) {
                if (index >= args.length) {
                    return null;
                } else {
                    return args[index];
                }
            }

            private static StudioMetaKind parseKind(@Nullable String rawKind) {
                return tryParseKind(rawKind, StudioMetaKind.AUTO);
            }

            @Nullable
            private static StudioMetaKind tryParseKind(@Nullable String rawKind, @Nullable StudioMetaKind defaultKind) {
                if (rawKind == null || rawKind.isBlank()) {
                    return defaultKind;
                }
                try {
                    return StudioMetaKind.valueOf(rawKind.strip().toUpperCase().replace('-', '_'));
                } catch (Exception e) {
                    return defaultKind;
                }
            }
        }
    }

    static final String XS_NS = "http://www.w3.org/2001/XMLSchema";
    static final String FLOWUI_LAYOUT_NS = "http://jmix.io/schema/flowui/layout";
    static final String FLOWUI_DATA_NS = "http://jmix.io/schema/flowui/data";

    private static final Set<String> IGNORED_DIRS = Set.of(".git", ".gradle", ".idea", "build", "out");
    private static final Set<String> GENERIC_METHOD_NAMES = Set.of("action", "column", "item", "loader", "property");
    private static final Set<String> LOOK_AND_FEEL_NAMES = Set.of(
            StudioXmlAttributes.CLASS_NAMES,
            StudioXmlAttributes.CSS,
            StudioXmlAttributes.ICON,
            StudioXmlAttributes.THEME_NAMES
    );
    private static final Set<String> SIZE_NAMES = Set.of(
            StudioXmlAttributes.HEIGHT,
            StudioXmlAttributes.MAX_HEIGHT,
            StudioXmlAttributes.MAX_WIDTH,
            StudioXmlAttributes.MIN_HEIGHT,
            StudioXmlAttributes.MIN_WIDTH,
            StudioXmlAttributes.WIDTH
    );
    private static final Set<String> POSITION_NAMES = Set.of(
            StudioXmlAttributes.ALIGN_SELF,
            StudioXmlAttributes.COLSPAN,
            StudioXmlAttributes.JUSTIFY_SELF,
            StudioXmlAttributes.ROWSPAN
    );
    private static final Set<String> VALIDATION_NAMES = Set.of(
            StudioXmlAttributes.ALLOWED_CHAR_PATTERN,
            StudioXmlAttributes.PATTERN,
            StudioXmlAttributes.ERROR_MESSAGE,
            StudioXmlAttributes.MAX,
            StudioXmlAttributes.MAX_LENGTH,
            StudioXmlAttributes.MIN,
            StudioXmlAttributes.MIN_LENGTH,
            StudioXmlAttributes.PREVENT_INVALID_INPUT,
            StudioXmlAttributes.REQUIRED,
            StudioXmlAttributes.REQUIRED_MESSAGE
    );
    private static final Set<String> AMBIGUOUS_SPECIAL_NAMES = Set.of(
            StudioXmlAttributes.CLASS,
            StudioXmlAttributes.COMPONENT,
            StudioXmlAttributes.META_CLASS,
            StudioXmlAttributes.PROPERTY,
            StudioXmlAttributes.CONTAINER,
            StudioXmlAttributes.DATA_CONTAINER,
            StudioXmlAttributes.ITEMS_CONTAINER
    );
    private static final Map<String, String> BUILTIN_TYPE_MAP = Map.ofEntries(
            Map.entry("boolean", "BOOLEAN"),
            Map.entry("byte", "INTEGER"),
            Map.entry("date", "STRING"),
            Map.entry("dateTime", "STRING"),
            Map.entry("decimal", "BIG_DECIMAL"),
            Map.entry("double", "DOUBLE"),
            Map.entry("duration", "STRING"),
            Map.entry("float", "FLOAT"),
            Map.entry("int", "INTEGER"),
            Map.entry("integer", "INTEGER"),
            Map.entry("language", "STRING"),
            Map.entry("long", "LONG"),
            Map.entry("NCName", "STRING"),
            Map.entry("negativeInteger", "INTEGER"),
            Map.entry("nonNegativeInteger", "INTEGER"),
            Map.entry("nonPositiveInteger", "INTEGER"),
            Map.entry("normalizedString", "STRING"),
            Map.entry("positiveInteger", "INTEGER"),
            Map.entry("short", "INTEGER"),
            Map.entry("string", "STRING"),
            Map.entry("time", "STRING"),
            Map.entry("token", "STRING"),
            Map.entry("unsignedInt", "LONG"),
            Map.entry("unsignedLong", "LONG"),
            Map.entry("unsignedShort", "INTEGER")
    );
    private static final Set<String> DATA_COMPONENT_ELEMENT_NAMES = Set.of(
            StudioXmlElements.INSTANCE, StudioXmlElements.COLLECTION,
            StudioXmlElements.KEY_VALUE_INSTANCE, StudioXmlElements.KEY_VALUE_COLLECTION
    );
    private static final Pattern JAVA_PACKAGE_PATTERN =
            Pattern.compile("^package\\s+([^;]+);$");
    private static final Pattern XMLNS_ALIAS_PATTERN_TEMPLATE =
            Pattern.compile("xmlns:([A-Za-z_][A-Za-z0-9_.-]*)=\"%s\"");
    private static final Pattern XMLNS_DEFAULT_PATTERN_TEMPLATE =
            Pattern.compile("xmlns=\"%s\"");

    private static final Map<String, String> PREFERRED_PROPERTY_GROUP_JAVA_CLASSES = Map.<String, String>ofEntries(
            Map.entry("collectionContainerType", "io.jmix.flowui.model.CollectionContainer"),
            Map.entry("instanceContainerType", "io.jmix.flowui.model.InstanceContainer"),
            Map.entry("instanceLoaderType", "io.jmix.flowui.model.InstanceLoader"),
            Map.entry("collectionLoaderType", "io.jmix.flowui.model.CollectionLoader"),
            Map.entry("keyValueInstanceContainerType", "io.jmix.flowui.model.KeyValueContainer"),
            Map.entry("keyValueInstanceLoaderType", "io.jmix.flowui.model.KeyValueInstanceLoader"),
            Map.entry("keyValueCollectionContainerType", "io.jmix.flowui.model.KeyValueCollectionContainer"),
            Map.entry("keyValueCollectionLoaderType", "io.jmix.flowui.model.KeyValueCollectionLoader"),
            Map.entry("onComponentValueChangedType", "io.jmix.flowui.facet.dataloadcoordinator.OnComponentValueChangedLoadTrigger"),
            Map.entry("onContainerItemChangedType", "io.jmix.flowui.facet.dataloadcoordinator.OnContainerItemChangedLoadTrigger"),
            Map.entry("detailsComponent", "io.jmix.flowui.component.details.JmixDetails"),
            Map.entry("hboxComponent", "com.vaadin.flow.component.orderedlayout.HorizontalLayout"),
            Map.entry("componentLayout", "com.vaadin.flow.component.orderedlayout.VerticalLayout"),
            Map.entry("flexLayoutComponent", "com.vaadin.flow.component.orderedlayout.FlexLayout"),
            Map.entry("scrollerContainer", "io.jmix.flowui.component.scroller.JmixScroller"),
            Map.entry("splitContainer", "io.jmix.flowui.component.splitlayout.JmixSplitLayout"),
            Map.entry("accordionContainer", "io.jmix.flowui.component.accordion.JmixAccordion"),
            Map.entry("formLayoutComponent", "io.jmix.flowui.component.formlayout.JmixFormLayout"),
            Map.entry("tabsContainer", "io.jmix.flowui.component.tabsheet.JmixTabs"),
            Map.entry("tabSheetComponent", "io.jmix.flowui.component.tabsheet.JmixTabSheet"),
            Map.entry("cardContainer", "io.jmix.flowui.component.card.JmixCard"),
            Map.entry("gridLayoutComponent", "io.jmix.flowui.component.gridlayout.GridLayout"),
            Map.entry("dropdownActionItemType", "io.jmix.flowui.kit.component.dropdownbutton.ActionItem"),
            Map.entry("dropdownComponentItemType", "io.jmix.flowui.kit.component.dropdownbutton.ComponentItem"),
            Map.entry("dropdownTextItemType", "io.jmix.flowui.kit.component.dropdownbutton.TextItem"),
            Map.entry("userMenuTextItemType", "io.jmix.flowui.kit.component.usermenu.TextUserMenuItem"),
            Map.entry("userMenuActionItemType", "io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem"),
            Map.entry("userMenuComponentItemType", "io.jmix.flowui.kit.component.usermenu.ComponentUserMenuItem"),
            Map.entry("userMenuViewItemType", "io.jmix.flowui.component.usermenu.ViewUserMenuItem"),
            Map.entry("numberRendererType", "com.vaadin.flow.data.renderer.NumberRenderer"),
            Map.entry("tooltipElement", "com.vaadin.flow.component.shared.Tooltip"),
            Map.entry("tabComponent", "com.vaadin.flow.component.tabs.Tab"),
            Map.entry("formItemType", "com.vaadin.flow.component.formlayout.FormLayout.FormItem"),
            Map.entry("dataGridColumnComponent", "io.jmix.flowui.component.grid.DataGridColumn"),
            Map.entry("dataGridEditorActionsColumn", "io.jmix.flowui.kit.component.grid.EditorActionsColumn"),
            Map.entry("dataGridComponent", "io.jmix.flowui.component.grid.DataGrid"),
            Map.entry("treeDataGridComponent", "io.jmix.flowui.component.grid.TreeDataGrid"),
            Map.entry("loginFormComponent", "io.jmix.flowui.component.loginform.JmixLoginForm"),
            Map.entry("loginOverlayComponent", "com.vaadin.flow.component.login.LoginOverlay"),
            Map.entry("jmixLoginFormType", "io.jmix.flowui.kit.component.loginform.JmixLoginI18n.JmixForm"),
            Map.entry("loginFormType", "com.vaadin.flow.component.login.LoginI18n.Form"),
            Map.entry("loginErrorMessageType", "com.vaadin.flow.component.login.LoginI18n.ErrorMessage"),
            Map.entry("simplePaginationComponent", "io.jmix.flowui.component.pagination.SimplePagination"),
            Map.entry("uploadComponent", "io.jmix.flowui.component.upload.JmixUpload"),
            Map.entry("fileUploadFieldComponent", "io.jmix.flowui.component.upload.FileUploadField"),
            Map.entry("fileStorageUploadFieldComponent", "io.jmix.flowui.component.upload.FileStorageUploadField"),
            Map.entry("genericFilterComponent", "io.jmix.flowui.component.genericfilter.GenericFilter"),
            Map.entry("propertyFilterComponent", "io.jmix.flowui.component.propertyfilter.PropertyFilter"),
            Map.entry("jpqlFilterComponent", "io.jmix.flowui.component.jpqlfilter.JpqlFilter"),
            Map.entry("groupFilterComponent", "io.jmix.flowui.component.logicalfilter.GroupFilter"),
            Map.entry("paginationUrlQueryParametersType", "io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder"),
            Map.entry("genericFilterUrlQueryParametersType", "io.jmix.flowui.facet.queryparameters.GenericFilterQueryParametersBinder"),
            Map.entry("propertyFilterUrlQueryParametersType", "io.jmix.flowui.facet.queryparameters.PropertyFilterQueryParametersBinder"),
            Map.entry("dataGridFilterUrlQueryParametersType", "io.jmix.flowui.facet.urlqueryparameters.DataGridFilterUrlQueryParametersBinder"),
            Map.entry("codeEditorComponent", "io.jmix.flowui.component.codeeditor.CodeEditor"),
            Map.entry("virtualListComponent", "io.jmix.flowui.component.virtuallist.JmixVirtualList"),
            Map.entry("twinColumnComponent", "io.jmix.flowui.component.twincolumn.TwinColumn"),
            Map.entry("gridColumnVisibilityComponent", "io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility"),
            Map.entry("menuFilterFieldComponent", "io.jmix.flowui.component.menufilterfield.MenuFilterField"),
            Map.entry("richTextEditorComponent", "io.jmix.flowui.component.richtexteditor.RichTextEditor"),
            Map.entry("horizontalMenuComponent", "io.jmix.flowui.component.horizontalmenu.HorizontalMenu"),
            Map.entry("workAreaContainer", "io.jmix.tabbedmode.component.workarea.WorkArea"),
            Map.entry("mainTabSheetComponent", "io.jmix.tabbedmode.component.tabsheet.MainTabSheet")
    );

    private static final Map<String, List<String>> PREFERRED_PROPERTY_GROUPS_BY_COMPLEX_TYPE = Map.ofEntries(
            Map.entry("messageValidatorType", List.of(
                    StudioPropertyGroups.Message.class.getCanonicalName()
            )),
            Map.entry("customValidatorType", List.of(
                    StudioPropertyGroups.Bean.class.getCanonicalName(),
                    StudioPropertyGroups.Message.class.getCanonicalName()
            )),
            Map.entry("dateValidatorType", List.of(
                    StudioPropertyGroups.MessageAndCheckSeconds.class.getCanonicalName()
            )),
            Map.entry("digitsValidatorType", List.of(
                    StudioPropertyGroups.Message.class.getCanonicalName(),
                    StudioPropertyGroups.RequiredInteger.class.getCanonicalName()
            )),
            Map.entry("maxMinValidatorType", List.of(
                    StudioPropertyGroups.MessageAndRequiredIntegerValue.class.getCanonicalName()
            )),
            Map.entry("sizeValidatorType", List.of(
                    StudioPropertyGroups.Message.class.getCanonicalName(),
                    StudioPropertyGroups.IntegerMin.class.getCanonicalName(),
                    StudioPropertyGroups.IntegerMax.class.getCanonicalName()
            )),
            Map.entry("regexpValidatorType", List.of(
                    StudioPropertyGroups.Message.class.getCanonicalName()
            ))
    );
}

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

import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StudioPropertyGroupsMatcher {

    private static final Set<String> IGNORED_DIRS = Set.of(".git", ".gradle", ".idea", "build", "out");
    private static final String STUDIO_PROPERTY_GROUP_ANNOTATION = "@StudioPropertyGroup";
    private static final String STUDIO_PROPERTY_ANNOTATION = "@StudioProperty";
    private static final List<String> STUDIO_META_ANNOTATIONS = List.of(
            "@StudioFacet",
            "@StudioComponent",
            "@StudioElement",
            "@StudioAction",
            "@StudioDataComponent"
    );
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("\\bpackage\\s+([A-Za-z_][A-Za-z0-9_.]*)\\s*;");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\bimport\\s+([A-Za-z_][A-Za-z0-9_.*]*)\\s*;");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile(
            "(?:public\\s+)?(?:static\\s+)?interface\\s+([A-Za-z_][A-Za-z0-9_]*)"
                    + "(?:\\s+extends\\s+([^{]+))?\\s*\\{"
    );

    private final Path workspaceRoot;
    private final List<Path> scanRoots;

    private @Nullable Map<String, GroupDefinition> groupDefinitions;
    private @Nullable Map<String, List<StudioPropertySignature>> flattenedProperties;
    private @Nullable Map<String, List<MetaDefinition>> metaDefinitionsByClassFqn;
    private @Nullable Map<String, List<MetaDefinition>> metaDefinitionsByXmlElement;
    private @Nullable Map<String, List<String>> outerGroupClassesBySimpleName;

    StudioPropertyGroupsMatcher(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
        this.scanRoots = detectScanRoots(this.workspaceRoot);
    }

    List<String> findMatchingGroupFqns(List<StudioPropertySignature> generatedProperties,
                                       @Nullable Path preferredModuleRoot) {
        if (generatedProperties.isEmpty()) {
            return List.of();
        }

        Map<String, GroupDefinition> definitions = getGroupDefinitions();
        Map<String, List<StudioPropertySignature>> flattened = getFlattenedProperties();
        List<GroupCandidate> candidates = new ArrayList<>();

        for (GroupDefinition definition : definitions.values()) {
            List<StudioPropertySignature> groupProperties = flattened.get(definition.fqn());
            if (groupProperties == null || groupProperties.isEmpty()) {
                continue;
            }

            BitSet coverage = findCoverage(groupProperties, generatedProperties);
            if (coverage == null || coverage.isEmpty()) {
                continue;
            }

            candidates.add(new GroupCandidate(
                    definition.fqn(),
                    definition.moduleRoot(),
                    !definition.parentGroupFqns().isEmpty(),
                    groupProperties.size(),
                    coverage
            ));
        }

        return selectGroups(candidates, preferredModuleRoot);
    }

    private Map<String, GroupDefinition> getGroupDefinitions() {
        if (groupDefinitions == null) {
            groupDefinitions = loadGroupDefinitions();
        }
        return groupDefinitions;
    }

    private Map<String, List<StudioPropertySignature>> getFlattenedProperties() {
        if (flattenedProperties == null) {
            flattenedProperties = flattenGroups(getGroupDefinitions());
        }
        return flattenedProperties;
    }

    private Map<String, List<MetaDefinition>> getMetaDefinitionsByClassFqn() {
        if (metaDefinitionsByClassFqn == null) {
            metaDefinitionsByClassFqn = loadMetaDefinitionsByClassFqn();
        }
        return metaDefinitionsByClassFqn;
    }

    private Map<String, List<MetaDefinition>> getMetaDefinitionsByXmlElement() {
        if (metaDefinitionsByXmlElement == null) {
            Map<String, List<MetaDefinition>> definitionsByXmlElement = new LinkedHashMap<>();
            for (List<MetaDefinition> definitions : getMetaDefinitionsByClassFqn().values()) {
                for (MetaDefinition definition : definitions) {
                    definitionsByXmlElement.computeIfAbsent(definition.xmlElement(), key -> new ArrayList<>())
                            .add(definition);
                }
            }
            metaDefinitionsByXmlElement = definitionsByXmlElement;
        }
        return metaDefinitionsByXmlElement;
    }

    private Map<String, List<String>> getOuterGroupClassesBySimpleName() {
        if (outerGroupClassesBySimpleName == null) {
            Map<String, LinkedHashSet<String>> outerClasses = new LinkedHashMap<>();
            for (String groupFqn : getGroupDefinitions().keySet()) {
                int nestedSeparatorIndex = groupFqn.lastIndexOf('.');
                if (nestedSeparatorIndex < 0) {
                    continue;
                }

                String outerClassFqn = groupFqn.substring(0, nestedSeparatorIndex);
                String simpleOuterClassName = outerClassFqn.substring(outerClassFqn.lastIndexOf('.') + 1);
                outerClasses.computeIfAbsent(simpleOuterClassName, key -> new LinkedHashSet<>()).add(outerClassFqn);
            }
            outerGroupClassesBySimpleName = outerClasses.entrySet().stream()
                    .collect(LinkedHashMap::new,
                            (result, entry) -> result.put(entry.getKey(), List.copyOf(entry.getValue())),
                            LinkedHashMap::putAll);
        }
        return outerGroupClassesBySimpleName;
    }

    List<StudioPropertySignature> flattenProperties(List<String> groupFqns) {
        if (groupFqns.isEmpty()) {
            return List.of();
        }

        Map<String, List<StudioPropertySignature>> flattened = getFlattenedProperties();
        List<StudioPropertySignature> properties = new ArrayList<>();
        for (String groupFqn : groupFqns) {
            properties.addAll(flattened.getOrDefault(groupFqn, List.of()));
        }
        return List.copyOf(properties);
    }

    List<String> findPropertyGroupsByClassFqn(String classFqn,
                                              @Nullable String xmlElement,
                                              @Nullable Path preferredModuleRoot) {
        if (classFqn.isBlank()) {
            return List.of();
        }

        List<MetaDefinition> definitions = getMetaDefinitionsByClassFqn().getOrDefault(classFqn, List.of());
        if (definitions.isEmpty()) {
            return List.of();
        }

        List<MetaDefinition> candidates = definitions;
        if (xmlElement != null && !xmlElement.isBlank()) {
            List<MetaDefinition> xmlElementMatches = definitions.stream()
                    .filter(definition -> xmlElement.equals(definition.xmlElement()))
                    .toList();
            if (!xmlElementMatches.isEmpty()) {
                candidates = xmlElementMatches;
            }
        }

        return candidates.stream()
                .sorted((left, right) -> compareMetaDefinitions(left, right, preferredModuleRoot))
                .map(MetaDefinition::propertyGroups)
                .filter(propertyGroups -> !propertyGroups.isEmpty())
                .findFirst()
                .orElse(List.of());
    }

    List<String> findPropertyGroupsByXmlElement(String xmlElement,
                                                @Nullable Path preferredModuleRoot) {
        if (xmlElement.isBlank()) {
            return List.of();
        }

        List<MetaDefinition> definitions = getMetaDefinitionsByXmlElement().getOrDefault(xmlElement, List.of()).stream()
                .filter(definition -> !definition.propertyGroups().isEmpty())
                .sorted((left, right) -> compareMetaDefinitions(left, right, preferredModuleRoot))
                .toList();
        if (definitions.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<List<String>> distinctPropertyGroups = new LinkedHashSet<>();
        for (MetaDefinition definition : definitions) {
            distinctPropertyGroups.add(definition.propertyGroups());
            if (distinctPropertyGroups.size() > 1) {
                return List.of();
            }
        }

        return definitions.getFirst().propertyGroups();
    }

    private Map<String, GroupDefinition> loadGroupDefinitions() {
        Map<String, GroupDefinition> definitions = new LinkedHashMap<>();

        for (Path scanRoot : scanRoots) {
            if (!Files.isDirectory(scanRoot)) {
                continue;
            }

            FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(scanRoot) && IGNORED_DIRS.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toString().endsWith(".java")) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (!isMainJavaSource(file)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String source = Files.readString(file, StandardCharsets.UTF_8);
                    if (!source.contains(STUDIO_PROPERTY_GROUP_ANNOTATION)) {
                        return FileVisitResult.CONTINUE;
                    }

                    parseGroupDefinitions(file, source).forEach(definition -> definitions.put(definition.fqn(), definition));
                    return FileVisitResult.CONTINUE;
                }
            };

            try {
                Files.walkFileTree(scanRoot, visitor);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot scan property group definitions under " + scanRoot, e);
            }
        }

        return definitions;
    }

    private Map<String, List<MetaDefinition>> loadMetaDefinitionsByClassFqn() {
        Map<String, List<MetaDefinition>> definitionsByClassFqn = new LinkedHashMap<>();
        Map<String, List<String>> outerGroupClasses = getOuterGroupClassesBySimpleName();

        for (Path scanRoot : scanRoots) {
            if (!Files.isDirectory(scanRoot)) {
                continue;
            }

            FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(scanRoot) && IGNORED_DIRS.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toString().endsWith(".java") || !isMainJavaSource(file)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String source = Files.readString(file, StandardCharsets.UTF_8);
                    if (!source.contains("@StudioUiKit") || !containsMetaAnnotation(source)) {
                        return FileVisitResult.CONTINUE;
                    }

                    for (MetaDefinition definition : parseMetaDefinitions(file, source, outerGroupClasses)) {
                        definitionsByClassFqn.computeIfAbsent(definition.classFqn(), key -> new ArrayList<>())
                                .add(definition);
                    }
                    return FileVisitResult.CONTINUE;
                }
            };

            try {
                Files.walkFileTree(scanRoot, visitor);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot scan meta definitions under " + scanRoot, e);
            }
        }

        return definitionsByClassFqn;
    }

    private List<GroupDefinition> parseGroupDefinitions(Path sourceFile, String source) {
        String strippedSource = stripComments(source);
        String packageName = parsePackageName(strippedSource);
        if (packageName == null) {
            return List.of();
        }

        String outerClassName = sourceFile.getFileName().toString().replaceFirst("\\.java$", "");
        ImportContext importContext = parseImports(strippedSource);
        Set<String> localGroupNames = parseInterfaceNames(strippedSource);
        List<GroupDefinition> definitions = new ArrayList<>();

        int currentIndex = 0;
        while (true) {
            int annotationIndex = strippedSource.indexOf(STUDIO_PROPERTY_GROUP_ANNOTATION, currentIndex);
            if (annotationIndex < 0) {
                break;
            }

            AnnotationParseResult annotation = parseAnnotation(strippedSource, annotationIndex, STUDIO_PROPERTY_GROUP_ANNOTATION);
            Matcher matcher = INTERFACE_PATTERN.matcher(strippedSource);
            matcher.region(annotation.endIndex(), strippedSource.length());
            if (!matcher.find()) {
                break;
            }

            String interfaceName = matcher.group(1);
            String extendsClause = matcher.group(2);
            List<String> parentGroups = parseParentGroups(extendsClause, packageName, outerClassName, importContext,
                    localGroupNames);
            List<StudioPropertySignature> directProperties = parseGroupProperties(annotation.content());

            definitions.add(new GroupDefinition(
                    packageName + "." + outerClassName + "." + interfaceName,
                    sourceFile,
                    detectModuleRoot(sourceFile),
                    parentGroups,
                    directProperties
            ));

            currentIndex = matcher.end();
        }

        return definitions;
    }

    private List<MetaDefinition> parseMetaDefinitions(Path sourceFile,
                                                      String source,
                                                      Map<String, List<String>> outerGroupClassesBySimpleName) {
        String strippedSource = stripComments(source);
        String packageName = parsePackageName(strippedSource);
        if (packageName == null) {
            return List.of();
        }

        String outerClassName = sourceFile.getFileName().toString().replaceFirst("\\.java$", "");
        ImportContext importContext = parseImports(strippedSource);
        Set<String> localGroupNames = parseInterfaceNames(strippedSource);
        List<MetaDefinition> definitions = new ArrayList<>();
        int currentIndex = 0;

        while (true) {
            MetaAnnotationMatch match = findNextMetaAnnotation(strippedSource, currentIndex);
            if (match == null) {
                break;
            }

            AnnotationParseResult annotation = parseAnnotation(strippedSource, match.annotationIndex(), match.annotationName());
            Map<String, String> arguments = parseNamedArguments(annotation.content());
            String classFqn = unquoteJavaString(arguments.getOrDefault("classFqn", ""));
            if (!classFqn.isBlank()) {
                definitions.add(new MetaDefinition(
                        classFqn,
                        unquoteJavaString(arguments.getOrDefault("xmlElement", "")),
                        parseClassArray(arguments.get("propertyGroups"), packageName, outerClassName, importContext,
                                localGroupNames, outerGroupClassesBySimpleName),
                        detectModuleRoot(sourceFile),
                        sourceFile
                ));
            }

            currentIndex = annotation.endIndex();
        }

        return List.copyOf(definitions);
    }

    private Map<String, List<StudioPropertySignature>> flattenGroups(Map<String, GroupDefinition> definitions) {
        Map<String, List<StudioPropertySignature>> flattened = new LinkedHashMap<>();
        for (GroupDefinition definition : definitions.values()) {
            List<StudioPropertySignature> properties = flattenGroup(definition.fqn(), definitions, flattened, new HashSet<>());
            if (properties != null) {
                flattened.put(definition.fqn(), properties);
            }
        }
        return flattened;
    }

    private @Nullable List<StudioPropertySignature> flattenGroup(String fqn,
                                                                 Map<String, GroupDefinition> definitions,
                                                                 Map<String, List<StudioPropertySignature>> cache,
                                                                 Set<String> visiting) {
        List<StudioPropertySignature> cached = cache.get(fqn);
        if (cached != null) {
            return cached;
        }

        GroupDefinition definition = definitions.get(fqn);
        if (definition == null || !visiting.add(fqn)) {
            return null;
        }

        List<StudioPropertySignature> properties = new ArrayList<>();
        for (String parentFqn : definition.parentGroupFqns()) {
            List<StudioPropertySignature> parentProperties = flattenGroup(parentFqn, definitions, cache, visiting);
            if (parentProperties == null) {
                visiting.remove(fqn);
                return null;
            }
            properties.addAll(parentProperties);
        }
        properties.addAll(definition.directProperties());
        visiting.remove(fqn);

        List<StudioPropertySignature> flattened = List.copyOf(properties);
        cache.put(fqn, flattened);
        return flattened;
    }

    private @Nullable BitSet findCoverage(List<StudioPropertySignature> groupProperties,
                                          List<StudioPropertySignature> generatedProperties) {
        boolean[] matchedGeneratedProperties = new boolean[generatedProperties.size()];
        BitSet coverage = new BitSet(generatedProperties.size());
        for (StudioPropertySignature groupProperty : groupProperties) {
            int matchedIndex = findMatchedPropertyIndex(groupProperty, generatedProperties, matchedGeneratedProperties);
            if (matchedIndex < 0) {
                return null;
            }
            matchedGeneratedProperties[matchedIndex] = true;
            coverage.set(matchedIndex);
        }
        return coverage;
    }

    private int findMatchedPropertyIndex(StudioPropertySignature groupProperty,
                                         List<StudioPropertySignature> generatedProperties,
                                         boolean[] matchedGeneratedProperties) {
        for (int i = 0; i < generatedProperties.size(); i++) {
            if (!matchedGeneratedProperties[i]
                    && matchesGroupProperty(groupProperty, generatedProperties.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean matchesGroupProperty(StudioPropertySignature groupProperty,
                                         StudioPropertySignature generatedProperty) {
        if (groupProperty.equals(generatedProperty)) {
            return true;
        }

        return Objects.equals(groupProperty.xmlAttribute(), generatedProperty.xmlAttribute())
                && Objects.equals(groupProperty.type(), generatedProperty.type())
                && Objects.equals(groupProperty.classFqn(), generatedProperty.classFqn())
                && groupCategoryMatches(groupProperty.category(), generatedProperty.category())
                && groupProperty.required() == generatedProperty.required()
                && Objects.equals(groupProperty.defaultValue(), generatedProperty.defaultValue())
                && Objects.equals(groupProperty.defaultValueRef(), generatedProperty.defaultValueRef())
                && Objects.equals(groupProperty.initialValue(), generatedProperty.initialValue())
                && Objects.equals(groupProperty.options(), generatedProperty.options())
                && Objects.equals(groupProperty.setMethod(), generatedProperty.setMethod())
                && Objects.equals(groupProperty.setParameterFqn(), generatedProperty.setParameterFqn())
                && Objects.equals(groupProperty.addMethod(), generatedProperty.addMethod())
                && Objects.equals(groupProperty.addParameterFqn(), generatedProperty.addParameterFqn())
                && Objects.equals(groupProperty.removeMethod(), generatedProperty.removeMethod())
                && Objects.equals(groupProperty.removeParameterFqn(), generatedProperty.removeParameterFqn())
                && Objects.equals(groupProperty.typeParameter(), generatedProperty.typeParameter())
                && groupProperty.useAsInjectionType() == generatedProperty.useAsInjectionType()
                && Objects.equals(groupProperty.componentRefTags(), generatedProperty.componentRefTags())
                && Objects.equals(groupProperty.cdataWrapperTag(), generatedProperty.cdataWrapperTag());
    }

    private boolean groupCategoryMatches(@Nullable String groupCategory, @Nullable String generatedCategory) {
        return Objects.equals(groupCategory, generatedCategory)
                || groupCategory == null && generatedCategory != null;
    }

    private List<String> selectGroups(List<GroupCandidate> candidates, @Nullable Path preferredModuleRoot) {
        List<String> selectedGroups = new ArrayList<>();
        BitSet coveredIndexes = new BitSet();

        while (true) {
            GroupCandidate bestCandidate = null;
            int bestUncoveredCount = 0;

            for (GroupCandidate candidate : candidates) {
                int uncoveredCount = uncoveredCount(candidate.coverage(), coveredIndexes);
                if (uncoveredCount == 0) {
                    continue;
                }

                if (bestCandidate == null
                        || isBetterCandidate(candidate, uncoveredCount, bestCandidate, bestUncoveredCount,
                        preferredModuleRoot)) {
                    bestCandidate = candidate;
                    bestUncoveredCount = uncoveredCount;
                }
            }

            if (bestCandidate == null) {
                break;
            }

            selectedGroups.add(bestCandidate.fqn());
            coveredIndexes.or(bestCandidate.coverage());
        }

        return List.copyOf(selectedGroups);
    }

    private boolean isBetterCandidate(GroupCandidate candidate,
                                      int uncoveredCount,
                                      GroupCandidate currentBest,
                                      int currentBestUncoveredCount,
                                      @Nullable Path preferredModuleRoot) {
        if (uncoveredCount != currentBestUncoveredCount) {
            return uncoveredCount > currentBestUncoveredCount;
        }
        if (candidate.propertyCount() != currentBest.propertyCount()) {
            return candidate.propertyCount() > currentBest.propertyCount();
        }
        if (candidate.composite() != currentBest.composite()) {
            return candidate.composite();
        }

        int candidateRank = moduleRank(candidate.moduleRoot(), preferredModuleRoot);
        int bestRank = moduleRank(currentBest.moduleRoot(), preferredModuleRoot);
        if (candidateRank != bestRank) {
            return candidateRank < bestRank;
        }

        return candidate.fqn().compareTo(currentBest.fqn()) < 0;
    }

    private int compareMetaDefinitions(MetaDefinition left,
                                       MetaDefinition right,
                                       @Nullable Path preferredModuleRoot) {
        int leftRank = moduleRank(left.moduleRoot(), preferredModuleRoot);
        int rightRank = moduleRank(right.moduleRoot(), preferredModuleRoot);
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }

        int xmlElementComparison = left.xmlElement().compareTo(right.xmlElement());
        if (xmlElementComparison != 0) {
            return xmlElementComparison;
        }

        return left.sourceFile().toString().compareTo(right.sourceFile().toString());
    }

    private int moduleRank(Path candidateModuleRoot, @Nullable Path preferredModuleRoot) {
        if (preferredModuleRoot != null && candidateModuleRoot.startsWith(preferredModuleRoot)) {
            return 0;
        }
        Path flowuiKitModule = workspaceRoot.resolve("jmix/jmix-flowui/flowui-kit").normalize();
        if (candidateModuleRoot.startsWith(flowuiKitModule)) {
            return 1;
        }
        return 2;
    }

    private int uncoveredCount(BitSet candidateCoverage, BitSet coveredIndexes) {
        BitSet uncovered = (BitSet) candidateCoverage.clone();
        uncovered.andNot(coveredIndexes);
        return uncovered.cardinality();
    }

    private List<String> parseParentGroups(@Nullable String extendsClause,
                                           String packageName,
                                           String outerClassName,
                                           ImportContext importContext,
                                           Set<String> localGroupNames) {
        if (extendsClause == null || extendsClause.isBlank()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String reference : splitTopLevel(extendsClause, ',')) {
            String resolvedReference = resolveGroupReference(reference.strip(), packageName, outerClassName,
                    importContext, localGroupNames);
            if (resolvedReference != null) {
                result.add(resolvedReference);
            }
        }
        return List.copyOf(result);
    }

    private List<String> parseClassArray(@Nullable String value,
                                         String packageName,
                                         String outerClassName,
                                         ImportContext importContext,
                                         Set<String> localGroupNames,
                                         Map<String, List<String>> outerGroupClassesBySimpleName) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String strippedValue = value.strip();
        String inner = strippedValue;
        if (strippedValue.startsWith("{") && strippedValue.endsWith("}")) {
            inner = strippedValue.substring(1, strippedValue.length() - 1).strip();
        }
        if (inner.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String reference : splitTopLevel(inner, ',')) {
            String resolvedReference = resolveGroupReference(reference.replace(".class", "").strip(),
                    packageName, outerClassName, importContext, localGroupNames, outerGroupClassesBySimpleName);
            if (resolvedReference != null) {
                result.add(resolvedReference);
            }
        }
        return List.copyOf(result);
    }

    private @Nullable String resolveGroupReference(String reference,
                                                   String packageName,
                                                   String outerClassName,
                                                   ImportContext importContext,
                                                   Set<String> localGroupNames) {
        return resolveGroupReference(reference, packageName, outerClassName, importContext, localGroupNames, Map.of());
    }

    private @Nullable String resolveGroupReference(String reference,
                                                   String packageName,
                                                   String outerClassName,
                                                   ImportContext importContext,
                                                   Set<String> localGroupNames,
                                                   Map<String, List<String>> outerGroupClassesBySimpleName) {
        if (reference.isBlank()) {
            return null;
        }

        String sanitizedReference = reference.replace(".class", "").strip();
        if (!sanitizedReference.contains(".")) {
            if (localGroupNames.contains(sanitizedReference)) {
                return packageName + "." + outerClassName + "." + sanitizedReference;
            }
            String explicitImport = importContext.explicitImports().get(sanitizedReference);
            if (explicitImport != null) {
                return explicitImport;
            }
            if (!importContext.wildcardImports().isEmpty()) {
                return importContext.wildcardImports().get(0) + "." + sanitizedReference;
            }
            return packageName + "." + outerClassName + "." + sanitizedReference;
        }

        String firstSegment = sanitizedReference.substring(0, sanitizedReference.indexOf('.'));
        if (!firstSegment.isEmpty() && Character.isLowerCase(firstSegment.charAt(0))) {
            return sanitizedReference;
        }

        if (outerClassName.equals(firstSegment)) {
            return packageName + "." + sanitizedReference;
        }

        String importedOuterClass = importContext.explicitImports().get(firstSegment);
        if (importedOuterClass != null) {
            return importedOuterClass + sanitizedReference.substring(firstSegment.length());
        }

        String indexedSamePackageOuterClass = findIndexedOuterClassFqn(outerGroupClassesBySimpleName, firstSegment,
                packageName);
        if (indexedSamePackageOuterClass != null) {
            return indexedSamePackageOuterClass + sanitizedReference.substring(firstSegment.length());
        }

        String indexedWildcardOuterClass = findIndexedOuterClassFqn(outerGroupClassesBySimpleName, firstSegment,
                importContext.wildcardImports());
        if (indexedWildcardOuterClass != null) {
            return indexedWildcardOuterClass + sanitizedReference.substring(firstSegment.length());
        }

        for (String wildcardImport : importContext.wildcardImports()) {
            return wildcardImport + "." + sanitizedReference;
        }

        return packageName + "." + sanitizedReference;
    }

    private @Nullable String findIndexedOuterClassFqn(Map<String, List<String>> outerGroupClassesBySimpleName,
                                                      String simpleOuterClassName,
                                                      String packageName) {
        return outerGroupClassesBySimpleName.getOrDefault(simpleOuterClassName, List.of()).stream()
                .filter(fqn -> fqn.startsWith(packageName + "."))
                .findFirst()
                .orElse(null);
    }

    private @Nullable String findIndexedOuterClassFqn(Map<String, List<String>> outerGroupClassesBySimpleName,
                                                      String simpleOuterClassName,
                                                      List<String> candidatePackages) {
        return outerGroupClassesBySimpleName.getOrDefault(simpleOuterClassName, List.of()).stream()
                .filter(fqn -> candidatePackages.stream().anyMatch(candidatePackage -> fqn.startsWith(candidatePackage + ".")))
                .findFirst()
                .orElse(null);
    }

    private Set<String> parseInterfaceNames(String source) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = INTERFACE_PATTERN.matcher(source);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return Set.copyOf(result);
    }

    private List<StudioPropertySignature> parseGroupProperties(String annotationContent) {
        if (annotationContent.isBlank()) {
            return List.of();
        }

        List<StudioPropertySignature> properties = new ArrayList<>();
        int currentIndex = 0;
        while (true) {
            int propertyIndex = annotationContent.indexOf(STUDIO_PROPERTY_ANNOTATION, currentIndex);
            if (propertyIndex < 0) {
                break;
            }

            AnnotationParseResult propertyAnnotation = parseAnnotation(annotationContent, propertyIndex,
                    STUDIO_PROPERTY_ANNOTATION);
            properties.add(parsePropertySignature(propertyAnnotation.content()));
            currentIndex = propertyAnnotation.endIndex();
        }

        return List.copyOf(properties);
    }

    private StudioPropertySignature parsePropertySignature(String propertyAnnotationContent) {
        Map<String, String> values = parseNamedArguments(propertyAnnotationContent);

        String xmlAttribute = unquoteJavaString(values.get("xmlAttribute"));
        String type = lastSegment(values.get("type"));
        String category = values.containsKey("category")
                ? lastSegment(values.get("category"))
                : null;
        boolean required = Boolean.parseBoolean(values.getOrDefault("required", "false"));
        String defaultValue = unquoteJavaString(values.getOrDefault("defaultValue", ""));
        String defaultValueRef = unquoteJavaString(values.getOrDefault("defaultValueRef", ""));
        String initialValue = unquoteJavaString(values.getOrDefault("initialValue", ""));
        String classFqn = unquoteJavaString(values.getOrDefault("classFqn", ""));
        List<String> options = parseStringArray(values.get("options"));
        String setMethod = unquoteJavaString(values.getOrDefault("setMethod", ""));
        String setParameterFqn = unquoteJavaString(values.getOrDefault("setParameterFqn", ""));
        String addMethod = unquoteJavaString(values.getOrDefault("addMethod", ""));
        String addParameterFqn = unquoteJavaString(values.getOrDefault("addParameterFqn", ""));
        String removeMethod = unquoteJavaString(values.getOrDefault("removeMethod", ""));
        String removeParameterFqn = unquoteJavaString(values.getOrDefault("removeParameterFqn", ""));
        String typeParameter = unquoteJavaString(values.getOrDefault("typeParameter", ""));
        boolean useAsInjectionType = Boolean.parseBoolean(values.getOrDefault("useAsInjectionType", "false"));
        List<String> componentRefTags = parseStringArray(values.get("componentRefTags"));
        String cdataWrapperTag = unquoteJavaString(values.getOrDefault("cdataWrapperTag", ""));

        return new StudioPropertySignature(
                xmlAttribute,
                type,
                classFqn,
                category,
                required,
                defaultValue,
                defaultValueRef,
                initialValue,
                options,
                setMethod,
                setParameterFqn,
                addMethod,
                addParameterFqn,
                removeMethod,
                removeParameterFqn,
                typeParameter,
                useAsInjectionType,
                componentRefTags,
                cdataWrapperTag
        );
    }

    private Map<String, String> parseNamedArguments(String annotationContent) {
        Map<String, String> arguments = new LinkedHashMap<>();
        for (String part : splitTopLevel(annotationContent, ',')) {
            if (part.isBlank()) {
                continue;
            }

            int equalsIndex = findTopLevelEquals(part);
            if (equalsIndex < 0) {
                continue;
            }

            String name = part.substring(0, equalsIndex).strip();
            String value = part.substring(equalsIndex + 1).strip();
            arguments.put(name, value);
        }
        return arguments;
    }

    private int findTopLevelEquals(String value) {
        boolean inString = false;
        boolean escaped = false;
        int parenthesesDepth = 0;
        int bracesDepth = 0;

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }
            if (current == '(') {
                parenthesesDepth++;
                continue;
            }
            if (current == ')') {
                parenthesesDepth--;
                continue;
            }
            if (current == '{') {
                bracesDepth++;
                continue;
            }
            if (current == '}') {
                bracesDepth--;
                continue;
            }
            if (current == '=' && parenthesesDepth == 0 && bracesDepth == 0) {
                return i;
            }
        }
        return -1;
    }

    private List<String> parseStringArray(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String strippedValue = value.strip();
        if (!strippedValue.startsWith("{") || !strippedValue.endsWith("}")) {
            return List.of(unquoteJavaString(strippedValue));
        }

        String inner = strippedValue.substring(1, strippedValue.length() - 1).strip();
        if (inner.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String item : splitTopLevel(inner, ',')) {
            if (!item.isBlank()) {
                result.add(unquoteJavaString(item.strip()));
            }
        }
        return List.copyOf(result);
    }

    private ImportContext parseImports(String source) {
        Map<String, String> explicitImports = new LinkedHashMap<>();
        List<String> wildcardImports = new ArrayList<>();

        Matcher matcher = IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            String importedValue = matcher.group(1);
            if (importedValue.endsWith(".*")) {
                wildcardImports.add(importedValue.substring(0, importedValue.length() - 2));
                continue;
            }

            String simpleName = importedValue.substring(importedValue.lastIndexOf('.') + 1);
            explicitImports.put(simpleName, importedValue);
        }

        return new ImportContext(explicitImports, List.copyOf(wildcardImports));
    }

    private @Nullable String parsePackageName(String source) {
        Matcher matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    private AnnotationParseResult parseAnnotation(String source, int annotationIndex, String annotationName) {
        int currentIndex = annotationIndex + annotationName.length();
        while (currentIndex < source.length() && Character.isWhitespace(source.charAt(currentIndex))) {
            currentIndex++;
        }

        if (currentIndex >= source.length() || source.charAt(currentIndex) != '(') {
            return new AnnotationParseResult("", currentIndex);
        }

        int endIndex = findMatchingBracket(source, currentIndex, '(', ')');
        return new AnnotationParseResult(source.substring(currentIndex + 1, endIndex), endIndex + 1);
    }

    private @Nullable MetaAnnotationMatch findNextMetaAnnotation(String source, int startIndex) {
        MetaAnnotationMatch bestMatch = null;
        for (String annotationName : STUDIO_META_ANNOTATIONS) {
            int annotationIndex = source.indexOf(annotationName, startIndex);
            if (annotationIndex < 0) {
                continue;
            }

            if (bestMatch == null || annotationIndex < bestMatch.annotationIndex()) {
                bestMatch = new MetaAnnotationMatch(annotationName, annotationIndex);
            }
        }
        return bestMatch;
    }

    private int findMatchingBracket(String source, int startIndex, char open, char close) {
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;

        for (int i = startIndex; i < source.length(); i++) {
            char current = source.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }
            if (current == open) {
                depth++;
                continue;
            }
            if (current == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        throw new IllegalArgumentException("Cannot find matching bracket in property group source.");
    }

    private List<String> splitTopLevel(String value, char delimiter) {
        List<String> parts = new ArrayList<>();
        boolean inString = false;
        boolean escaped = false;
        int parenthesesDepth = 0;
        int bracesDepth = 0;
        int startIndex = 0;

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }
            if (current == '(') {
                parenthesesDepth++;
                continue;
            }
            if (current == ')') {
                parenthesesDepth--;
                continue;
            }
            if (current == '{') {
                bracesDepth++;
                continue;
            }
            if (current == '}') {
                bracesDepth--;
                continue;
            }
            if (current == delimiter && parenthesesDepth == 0 && bracesDepth == 0) {
                parts.add(value.substring(startIndex, i));
                startIndex = i + 1;
            }
        }

        parts.add(value.substring(startIndex));
        return parts;
    }

    private String stripComments(String source) {
        StringBuilder result = new StringBuilder(source.length());
        boolean inString = false;
        boolean escaped = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (current == '\n') {
                    inLineComment = false;
                    result.append(current);
                }
                continue;
            }

            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                } else if (current == '\n') {
                    result.append('\n');
                }
                continue;
            }

            if (inString) {
                result.append(current);
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"' ) {
                inString = true;
                result.append(current);
                continue;
            }
            if (current == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (current == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }

            result.append(current);
        }

        return result.toString();
    }

    private boolean containsMetaAnnotation(String source) {
        for (String annotationName : STUDIO_META_ANNOTATIONS) {
            if (source.contains(annotationName)) {
                return true;
            }
        }
        return false;
    }

    private String lastSegment(@Nullable String value) {
        String nonNullValue = Objects.requireNonNullElse(value, "");
        int separatorIndex = nonNullValue.lastIndexOf('.');
        return separatorIndex < 0 ? nonNullValue.strip() : nonNullValue.substring(separatorIndex + 1).strip();
    }

    private String unquoteJavaString(String value) {
        String strippedValue = value.strip();
        if (strippedValue.length() >= 2 && strippedValue.startsWith("\"") && strippedValue.endsWith("\"")) {
            strippedValue = strippedValue.substring(1, strippedValue.length() - 1);
        }

        String constantValue = StudioXmlAttributes.resolveConstantValue(strippedValue);
        if (constantValue != null) {
            return constantValue;
        }
        constantValue = StudioXmlElements.resolveConstantValue(strippedValue);
        if (constantValue != null) {
            return constantValue;
        }

        return strippedValue
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private List<Path> detectScanRoots(Path root) {
        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        Path jmixRoot = root.resolve("jmix");
        Path premiumRoot = root.resolve("jmix-premium");
        if (Files.isDirectory(jmixRoot)) {
            paths.add(jmixRoot.normalize());
        }
        if (Files.isDirectory(premiumRoot)) {
            paths.add(premiumRoot.normalize());
        }
        if (paths.isEmpty()) {
            paths.add(root);
        }
        return List.copyOf(paths);
    }

    private boolean isMainJavaSource(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        for (int i = 0; i <= normalized.getNameCount() - 3; i++) {
            if ("src".equals(normalized.getName(i).toString())
                    && "main".equals(normalized.getName(i + 1).toString())
                    && "java".equals(normalized.getName(i + 2).toString())) {
                return true;
            }
        }
        return false;
    }

    private Path detectModuleRoot(Path sourceFile) {
        Path normalized = sourceFile.toAbsolutePath().normalize();
        for (int i = 0; i < normalized.getNameCount(); i++) {
            if ("src".equals(normalized.getName(i).toString())) {
                return normalized.getRoot() != null
                        ? normalized.getRoot().resolve(normalized.subpath(0, i))
                        : normalized.subpath(0, i);
            }
        }
        return normalized.getParent();
    }

    private record ImportContext(Map<String, String> explicitImports, List<String> wildcardImports) {
    }

    private record AnnotationParseResult(String content, int endIndex) {
    }

    private record MetaAnnotationMatch(String annotationName, int annotationIndex) {
    }

    private record GroupDefinition(String fqn,
                                   Path sourceFile,
                                   Path moduleRoot,
                                   List<String> parentGroupFqns,
                                   List<StudioPropertySignature> directProperties) {
    }

    private record MetaDefinition(String classFqn,
                                  String xmlElement,
                                  List<String> propertyGroups,
                                  Path moduleRoot,
                                  Path sourceFile) {
    }

    private record GroupCandidate(String fqn,
                                  Path moduleRoot,
                                  boolean composite,
                                  int propertyCount,
                                  BitSet coverage) {
    }
}

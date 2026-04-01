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

import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
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
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("\\bpackage\\s+([A-Za-z_][A-Za-z0-9_.]*)\\s*;");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\bimport\\s+([A-Za-z_][A-Za-z0-9_.*]*)\\s*;");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile(
            "(?:public\\s+)?(?:static\\s+)?interface\\s+([A-Za-z_][A-Za-z0-9_]*)"
                    + "(?:\\s+extends\\s+([^\\{]+))?\\s*\\{"
    );

    private final Path workspaceRoot;
    private final List<Path> scanRoots;

    private @Nullable Map<String, GroupDefinition> groupDefinitions;
    private @Nullable Map<String, List<StudioPropertySignature>> flattenedProperties;

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

    private List<GroupDefinition> parseGroupDefinitions(Path sourceFile, String source) {
        String strippedSource = stripComments(source);
        String packageName = parsePackageName(strippedSource);
        if (packageName == null) {
            return List.of();
        }

        String outerClassName = sourceFile.getFileName().toString().replaceFirst("\\.java$", "");
        ImportContext importContext = parseImports(strippedSource);
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
            List<String> parentGroups = parseParentGroups(extendsClause, packageName, outerClassName, importContext);
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
        Map<StudioPropertySignature, Deque<Integer>> indexesBySignature = new HashMap<>();
        for (int i = 0; i < generatedProperties.size(); i++) {
            indexesBySignature.computeIfAbsent(generatedProperties.get(i), key -> new ArrayDeque<>()).addLast(i);
        }

        BitSet coverage = new BitSet(generatedProperties.size());
        for (StudioPropertySignature groupProperty : groupProperties) {
            Deque<Integer> indexes = indexesBySignature.get(groupProperty);
            if (indexes == null || indexes.isEmpty()) {
                return null;
            }
            coverage.set(indexes.removeFirst());
        }
        return coverage;
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
                                           ImportContext importContext) {
        if (extendsClause == null || extendsClause.isBlank()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String reference : splitTopLevel(extendsClause, ',')) {
            String resolvedReference = resolveGroupReference(reference.strip(), packageName, outerClassName, importContext);
            if (resolvedReference != null) {
                result.add(resolvedReference);
            }
        }
        return List.copyOf(result);
    }

    private @Nullable String resolveGroupReference(String reference,
                                                   String packageName,
                                                   String outerClassName,
                                                   ImportContext importContext) {
        if (reference.isBlank()) {
            return null;
        }

        String sanitizedReference = reference.replace(".class", "").strip();
        if (!sanitizedReference.contains(".")) {
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

        for (String wildcardImport : importContext.wildcardImports()) {
            return wildcardImport + "." + sanitizedReference;
        }

        return packageName + "." + sanitizedReference;
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

    private record GroupDefinition(String fqn,
                                   Path sourceFile,
                                   Path moduleRoot,
                                   List<String> parentGroupFqns,
                                   List<StudioPropertySignature> directProperties) {
    }

    private record GroupCandidate(String fqn,
                                  Path moduleRoot,
                                  boolean composite,
                                  int propertyCount,
                                  BitSet coverage) {
    }
}

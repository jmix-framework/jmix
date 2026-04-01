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

package io.jmix.flowui.kit.meta.generator

import io.jmix.flowui.kit.meta.StudioPropertyGroups
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

class StudioMetaDescriptionGeneratorTest extends Specification {

    private static final String LAYOUT_XSD =
            'jmix/jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/layout.xsd'
    private static final String DATA_XSD =
            'jmix/jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/data.xsd'

    private static final String STUDIO_COMPONENTS_SOURCE =
            'jmix/jmix-flowui/flowui-kit/src/main/java/io/jmix/flowui/kit/meta/component/StudioComponents.java'
    private static final String STUDIO_ELEMENTS_SOURCE =
            'jmix/jmix-flowui/flowui-kit/src/main/java/io/jmix/flowui/kit/meta/element/StudioElements.java'
    private static final String STUDIO_ACTIONS_SOURCE =
            'jmix/jmix-flowui/flowui-kit/src/main/java/io/jmix/flowui/kit/meta/action/StudioActions.java'
    private static final String STUDIO_DATA_COMPONENTS_SOURCE =
            'jmix/jmix-flowui/flowui-kit/src/main/java/io/jmix/flowui/kit/meta/datacomponent/StudioDataComponents.java'
    private static final String STUDIO_FACETS_SOURCE =
            'jmix/jmix-flowui/flowui-kit/src/main/java/io/jmix/flowui/kit/meta/facet/StudioFacets.java'
    private static final String TEST_META_DESCRIPTIONS_SOURCE =
            'jmix/jmix-flowui/flowui/src/test/java/io/jmix/flowui/kit/meta/generator/TestStudioMetaDescriptions.java'

    private static final List<String> SUPPORTED_META_ANNOTATIONS = [
            'StudioFacet',
            'StudioComponent',
            'StudioElement',
            'StudioAction',
            'StudioDataComponent'
    ].asImmutable()

    @Shared
    Path workspaceRoot = StudioMetaDescriptionGenerator.detectWorkspaceRoot(Path.of('').toAbsolutePath())

    @Shared
    Path layoutXsd = workspaceRoot.resolve(LAYOUT_XSD).normalize()

    @Shared
    Path dataXsd = workspaceRoot.resolve(DATA_XSD).normalize()

    @Shared
    StudioMetaDescriptionGenerator generator = new StudioMetaDescriptionGenerator(workspaceRoot)

    @Shared
    Map<String, List<String>> javaClassIndex = buildJavaClassIndex()

    def "test generated dataLoadCoordinator meta matches exact fixture property group"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'dataLoadCoordinator') { candidate ->
            candidate.contextNames().contains('facets')
        }

        then:
        generatedMeta.toComparableMap() == expectedMeta(TEST_META_DESCRIPTIONS_SOURCE, 'dataLoadCoordinator').toComparableMap()
        generatedMeta.propertyGroups == [TestStudioMetaPropertyGroups.DataLoadCoordinatorGeneratedProperties.canonicalName]
        !generatedMeta.propertyGroups.contains(StudioPropertyGroups.Auto.canonicalName)
    }

    def "test generated timer meta uses only exact property groups"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'timer') { candidate ->
            candidate.contextNames().contains('facets')
        }

        then:
        propertyShape(generatedMeta.properties) == propertyShape(expectedMeta(STUDIO_FACETS_SOURCE, 'timer').properties)
        generatedMeta.propertyGroups == [StudioPropertyGroups.RequiredId.canonicalName]
        !generatedMeta.propertyGroups.contains(TestStudioMetaPropertyGroups.TimerPropertiesWithDifferentRepeatingDefaultValue.canonicalName)
    }

    def "test generated avatar meta matches existing StudioComponents description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'avatar') { true }
        def expectedMeta = expectedMeta(STUDIO_COMPONENTS_SOURCE, 'avatar')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties) == propertyAttributes(expectedMeta.properties)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.ClassNamesAndCss.canonicalName)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.ThemeNames.canonicalName)
    }

    def "test generated accordionPanel meta matches existing StudioElements description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'accordionPanel') { true }
        def expectedMeta = expectedMeta(STUDIO_ELEMENTS_SOURCE, 'accordionPanel')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties).containsAll(propertyAttributes(expectedMeta.properties))
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.Colspan.canonicalName)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.Enabled.canonicalName)
    }

    def "test generated view action reuses common StudioActions defaults"() {
        given:
        def expectedBaseAction = expectedMeta(STUDIO_ACTIONS_SOURCE, 'baseAction')

        when:
        def generatedMeta = generateMeta(layoutXsd, 'action') { candidate ->
            candidate.contextNames().contains('viewActions')
        }

        then:
        generatedMeta.kind == StudioMetaKind.ACTION
        generatedMeta.xmlElement == 'action'
        propertyAttributes(generatedMeta.properties).containsAll(propertyAttributes(expectedBaseAction.properties) + ['type'])
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.RequiredId.canonicalName)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.Text.canonicalName)
        generatedMeta.propertyGroups.any { it.endsWith('.Description') || it.endsWith('.DescriptionWithoutCategory') }
    }

    def "test generated collection data component matches existing StudioDataComponents description"() {
        when:
        def generatedMeta = generateMeta(dataXsd, 'collection') { candidate ->
            candidate.contextNames().contains('viewData')
        }
        def expectedMeta = expectedMeta(STUDIO_DATA_COMPONENTS_SOURCE, 'collection')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties) == propertyAttributes(expectedMeta.properties)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.RequiredId.canonicalName)
        generatedMeta.propertyGroups.contains(StudioPropertyGroups.EntityClass.canonicalName)
    }

    private MetaProjection generateMeta(Path schemaPath,
                                        String elementName,
                                        Closure<Boolean> candidateSelector) {
        StudioXsdElementCandidate candidate = findCandidate(schemaPath, elementName, candidateSelector)
        Path outputPath = Files.createTempDirectory('generated-meta-test').resolve('TestGeneratedMeta.java')

        parseMetaSource(generator.generate(candidate, StudioMetaKind.AUTO, outputPath).source())
    }

    private StudioXsdElementCandidate findCandidate(Path schemaPath,
                                                    String elementName,
                                                    Closure<Boolean> candidateSelector) {
        List<StudioXsdElementCandidate> candidates = generator.findElementCandidates(schemaPath, elementName)
                .findAll(candidateSelector)
        assert candidates.size() == 1: "Unexpected candidates for ${elementName}: ${candidates*.description()}"
        candidates.first()
    }

    private MetaProjection expectedMeta(String relativeSourcePath, String methodName) {
        Path sourcePath = workspaceRoot.resolve(relativeSourcePath).normalize()
        String source = Files.readString(sourcePath, StandardCharsets.UTF_8)
        parseMetaSource(source, methodName, sourcePath.fileName.toString().replaceFirst(/\.java$/, ''))
    }

    private MetaProjection parseMetaSource(String source) {
        parseMetaSource(source, null, parseOuterClassName(stripComments(source)))
    }

    private MetaProjection parseMetaSource(String source, String methodName, String outerClassName) {
        String strippedSource = stripComments(source)
        ImportContext importContext = parseImports(strippedSource)
        String packageName = parsePackageName(strippedSource)
        AnnotationParseResult annotation = methodName != null
                ? findMethodAnnotation(strippedSource, methodName)
                : findFirstMetaAnnotation(strippedSource)
        Map<String, String> annotationArguments = parseNamedArguments(annotation.content)

        new MetaProjection(
                kind: metaKind(annotation.name),
                xmlElement: unquoteJavaString(annotationArguments.getOrDefault('xmlElement', '')),
                propertyGroups: parseClassArray(annotationArguments.get('propertyGroups'), packageName, outerClassName, importContext),
                properties: parseProperties(annotationArguments.get('properties'))
        )
    }

    private static AnnotationParseResult findMethodAnnotation(String source, String methodName) {
        Matcher methodMatcher = Pattern.compile("\\b${Pattern.quote(methodName)}\\s*\\(").matcher(source)
        assert methodMatcher.find(): "Method ${methodName} not found"

        int methodIndex = methodMatcher.start()
        AnnotationParseResult result = null
        for (String annotationName : SUPPORTED_META_ANNOTATIONS) {
            Matcher annotationMatcher = Pattern.compile("@${annotationName}\\s*\\(").matcher(source)
            while (annotationMatcher.find()) {
                if (annotationMatcher.start() >= methodIndex) {
                    break
                }
                result = new AnnotationParseResult(
                        annotationName,
                        parseAnnotationContent(source, annotationMatcher.end() - 1)
                )
            }
        }

        assert result != null: "No Studio meta annotation found for ${methodName}"
        result
    }

    private static AnnotationParseResult findFirstMetaAnnotation(String source) {
        List<Map<String, Object>> matches = SUPPORTED_META_ANNOTATIONS.collect { annotationName ->
            Matcher matcher = Pattern.compile("@${annotationName}\\s*\\(").matcher(source)
            matcher.find() ? [name: annotationName, index: matcher.start(), parenIndex: matcher.end() - 1] : null
        }.findAll { it != null } as List<Map<String, Object>>

        assert !matches.empty: 'No Studio meta annotation found in generated source'

        Map<String, Object> firstMatch = matches.min { it.index as int }
        new AnnotationParseResult(
                firstMatch.name as String,
                parseAnnotationContent(source, firstMatch.parenIndex as int)
        )
    }

    private static AnnotationContent parseAnnotationContent(String source, int openingParenthesisIndex) {
        int depth = 0
        boolean inString = false
        boolean escaped = false
        int startIndex = openingParenthesisIndex + 1

        for (int i = openingParenthesisIndex; i < source.length(); i++) {
            char current = source.charAt(i)
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (current == '\\' as char) {
                    escaped = true
                } else if (current == '"' as char) {
                    inString = false
                }
                continue
            }

            if (current == '"' as char) {
                inString = true
                continue
            }
            if (current == '(' as char) {
                depth++
                continue
            }
            if (current == ')' as char) {
                depth--
                if (depth == 0) {
                    return new AnnotationContent(source.substring(startIndex, i), i + 1)
                }
            }
        }

        throw new IllegalArgumentException('Cannot parse annotation content')
    }

    private static Map<String, String> parseNamedArguments(String annotationContent) {
        Map<String, String> arguments = [:]
        splitTopLevel(annotationContent, ',').each { String part ->
            if (part.isBlank()) {
                return
            }

            int equalsIndex = findTopLevelEquals(part)
            if (equalsIndex < 0) {
                return
            }

            arguments.put(part.substring(0, equalsIndex).strip(), part.substring(equalsIndex + 1).strip())
        }
        arguments
    }

    private static int findTopLevelEquals(String value) {
        boolean inString = false
        boolean escaped = false
        int parenthesesDepth = 0
        int bracesDepth = 0

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i)
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (current == '\\' as char) {
                    escaped = true
                } else if (current == '"' as char) {
                    inString = false
                }
                continue
            }

            if (current == '"' as char) {
                inString = true
            } else if (current == '(' as char) {
                parenthesesDepth++
            } else if (current == ')' as char) {
                parenthesesDepth--
            } else if (current == '{' as char) {
                bracesDepth++
            } else if (current == '}' as char) {
                bracesDepth--
            } else if (current == '=' as char && parenthesesDepth == 0 && bracesDepth == 0) {
                return i
            }
        }

        -1
    }

    private List<String> parseClassArray(String value,
                                         String packageName,
                                         String outerClassName,
                                         ImportContext importContext) {
        if (!value) {
            return List.of()
        }

        String normalized = stripWrappingBraces(value.strip())
        if (normalized.isBlank()) {
            return List.of()
        }

        splitTopLevel(normalized, ',')
                .collect { it.strip() }
                .findAll { !it.isBlank() }
                .collect { resolveClassReference(it, packageName, outerClassName, importContext) }
    }

    private static List<StudioPropertySignature> parseProperties(String propertiesValue) {
        if (!propertiesValue) {
            return List.of()
        }

        List<StudioPropertySignature> properties = []
        int currentIndex = 0
        while (true) {
            int propertyIndex = propertiesValue.indexOf('@StudioProperty', currentIndex)
            if (propertyIndex < 0) {
                break
            }

            int openingParenthesisIndex = propertiesValue.indexOf('(', propertyIndex)
            AnnotationContent propertyAnnotation = parseAnnotationContent(propertiesValue, openingParenthesisIndex)
            properties.add(parseProperty(propertyAnnotation.content))
            currentIndex = propertyAnnotation.endIndex
        }

        properties
    }

    private static StudioPropertySignature parseProperty(String propertyAnnotationContent) {
        Map<String, String> arguments = parseNamedArguments(propertyAnnotationContent)

        new StudioPropertySignature(
                unquoteJavaString(arguments.get('xmlAttribute')),
                lastSegment(arguments.get('type')),
                unquoteJavaString(arguments.getOrDefault('classFqn', '')),
                arguments.containsKey('category') ? lastSegment(arguments.get('category')) : null,
                Boolean.parseBoolean(arguments.getOrDefault('required', 'false')),
                unquoteJavaString(arguments.getOrDefault('defaultValue', '')),
                unquoteJavaString(arguments.getOrDefault('defaultValueRef', '')),
                unquoteJavaString(arguments.getOrDefault('initialValue', '')),
                parseStringArray(arguments.get('options')),
                unquoteJavaString(arguments.getOrDefault('setMethod', '')),
                unquoteJavaString(arguments.getOrDefault('setParameterFqn', '')),
                unquoteJavaString(arguments.getOrDefault('addMethod', '')),
                unquoteJavaString(arguments.getOrDefault('addParameterFqn', '')),
                unquoteJavaString(arguments.getOrDefault('removeMethod', '')),
                unquoteJavaString(arguments.getOrDefault('removeParameterFqn', '')),
                unquoteJavaString(arguments.getOrDefault('typeParameter', '')),
                Boolean.parseBoolean(arguments.getOrDefault('useAsInjectionType', 'false')),
                parseStringArray(arguments.get('componentRefTags')),
                unquoteJavaString(arguments.getOrDefault('cdataWrapperTag', ''))
        )
    }

    private static List<String> parseStringArray(String value) {
        if (!value) {
            return List.of()
        }

        String normalized = stripWrappingBraces(value.strip())
        if (normalized.isBlank()) {
            return List.of()
        }

        splitTopLevel(normalized, ',')
                .collect { unquoteJavaString(it.strip()) }
                .findAll { !it.isBlank() }
    }

    private String resolveClassReference(String reference,
                                         String packageName,
                                         String outerClassName,
                                         ImportContext importContext) {
        String sanitizedReference = reference.replace('.class', '').strip()
        if (!sanitizedReference.contains('.')) {
            String explicitImport = importContext.explicitImports[sanitizedReference]
            if (explicitImport) {
                return explicitImport
            }
            String samePackageClass = findIndexedClassFqn(sanitizedReference, packageName)
            if (samePackageClass) {
                return samePackageClass
            }
            return "${packageName}.${outerClassName}.${sanitizedReference}"
        }

        String firstSegment = sanitizedReference.substring(0, sanitizedReference.indexOf('.'))
        if (Character.isLowerCase(firstSegment.charAt(0))) {
            return sanitizedReference
        }
        if (outerClassName == firstSegment) {
            return "${packageName}.${sanitizedReference}"
        }

        String importedOuterClass = importContext.explicitImports[firstSegment]
        if (importedOuterClass) {
            return importedOuterClass + sanitizedReference.substring(firstSegment.length())
        }
        String indexedSamePackage = findIndexedClassFqn(firstSegment, packageName)
        if (indexedSamePackage) {
            return indexedSamePackage + sanitizedReference.substring(firstSegment.length())
        }
        String indexedWildcard = findIndexedClassFqn(firstSegment, importContext.wildcardImports)
        if (indexedWildcard) {
            return indexedWildcard + sanitizedReference.substring(firstSegment.length())
        }
        if (!importContext.wildcardImports.empty) {
            return "${importContext.wildcardImports.first()}.${sanitizedReference}"
        }
        return "${packageName}.${sanitizedReference}"
    }

    private String findIndexedClassFqn(String simpleName, String packageName) {
        javaClassIndex.getOrDefault(simpleName, List.of())
                .find { it.startsWith("${packageName}.") }
    }

    private String findIndexedClassFqn(String simpleName, List<String> candidatePackages) {
        javaClassIndex.getOrDefault(simpleName, List.of())
                .find { String fqn -> candidatePackages.any { candidatePackage -> fqn.startsWith("${candidatePackage}.") } }
    }

    private static ImportContext parseImports(String source) {
        Map<String, String> explicitImports = [:]
        List<String> wildcardImports = []

        Matcher matcher = Pattern.compile("\\bimport\\s+([A-Za-z_][A-Za-z0-9_.*]*)\\s*;").matcher(source)
        while (matcher.find()) {
            String imported = matcher.group(1)
            if (imported.endsWith('.*')) {
                wildcardImports.add(imported.substring(0, imported.length() - 2))
            } else {
                explicitImports[imported.substring(imported.lastIndexOf('.') + 1)] = imported
            }
        }

        new ImportContext(explicitImports, wildcardImports)
    }

    private static String parsePackageName(String source) {
        Matcher matcher = Pattern.compile("\\bpackage\\s+([A-Za-z_][A-Za-z0-9_.]*)\\s*;").matcher(source)
        assert matcher.find(): 'Package declaration not found'
        matcher.group(1)
    }

    private static String parseOuterClassName(String source) {
        Matcher matcher = Pattern.compile("\\b(?:public\\s+)?(?:final\\s+)?(?:class|interface)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b")
                .matcher(source)
        assert matcher.find(): 'Outer class name not found'
        matcher.group(1)
    }

    private Map<String, List<String>> buildJavaClassIndex() {
        Map<String, List<String>> index = [:].withDefault { [] }
        [workspaceRoot.resolve('jmix'), workspaceRoot.resolve('jmix-premium')].findAll { Files.isDirectory(it) }.each { root ->
            Files.walk(root)
                    .filter { path -> path.toString().endsWith('.java') }
                    .forEach { Path path ->
                        String source = Files.readString(path, StandardCharsets.UTF_8)
                        String strippedSource = stripComments(source)
                        Matcher packageMatcher = Pattern.compile("\\bpackage\\s+([A-Za-z_][A-Za-z0-9_.]*)\\s*;").matcher(strippedSource)
                        Matcher classMatcher = Pattern.compile("\\b(?:public\\s+)?(?:final\\s+)?(?:class|interface)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b")
                                .matcher(strippedSource)
                        if (packageMatcher.find() && classMatcher.find()) {
                            String fqn = "${packageMatcher.group(1)}.${classMatcher.group(1)}"
                            index[classMatcher.group(1)] = index[classMatcher.group(1)] + fqn
                        }
                    }
        }
        index
    }

    private static String stripComments(String source) {
        source
                .replaceAll(/(?s)\/\*.*?\*\//, '')
                .replaceAll(/(?m)^\s*\/\/.*$/, '')
    }

    private static List<String> splitTopLevel(String value, String delimiter) {
        List<String> result = []
        char delimiterChar = delimiter.charAt(0)
        boolean inString = false
        boolean escaped = false
        int parenthesesDepth = 0
        int bracesDepth = 0
        int bracketDepth = 0
        int startIndex = 0

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i)
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (current == '\\' as char) {
                    escaped = true
                } else if (current == '"' as char) {
                    inString = false
                }
                continue
            }

            if (current == '"' as char) {
                inString = true
            } else if (current == '(' as char) {
                parenthesesDepth++
            } else if (current == ')' as char) {
                parenthesesDepth--
            } else if (current == '{' as char) {
                bracesDepth++
            } else if (current == '}' as char) {
                bracesDepth--
            } else if (current == '[' as char) {
                bracketDepth++
            } else if (current == ']' as char) {
                bracketDepth--
            } else if (current == delimiterChar && parenthesesDepth == 0 && bracesDepth == 0 && bracketDepth == 0) {
                result.add(value.substring(startIndex, i))
                startIndex = i + 1
            }
        }

        result.add(value.substring(startIndex))
        result
    }

    private static String stripWrappingBraces(String value) {
        value.startsWith('{') && value.endsWith('}') ? value.substring(1, value.length() - 1).strip() : value
    }

    private static String unquoteJavaString(String value) {
        if (value == null) {
            return ''
        }

        String stripped = value.strip()
        if (stripped.length() >= 2 && stripped.startsWith('"') && stripped.endsWith('"')) {
            return stripped.substring(1, stripped.length() - 1)
        }
        stripped
    }

    private static String lastSegment(String value) {
        if (!value) {
            return ''
        }
        String stripped = value.strip()
        int separatorIndex = stripped.lastIndexOf('.')
        separatorIndex >= 0 ? stripped.substring(separatorIndex + 1) : stripped
    }

    private static List<Map<String, Object>> propertyShape(List<StudioPropertySignature> properties) {
        properties.collect { StudioPropertySignature property ->
            [
                    xmlAttribute: property.xmlAttribute(),
                    type        : property.type(),
                    required    : property.required()
            ]
        }.sort { it.xmlAttribute as String } as List<Map<String, Object>>
    }

    private static List<String> propertyAttributes(List<StudioPropertySignature> properties) {
        properties.collect { it.xmlAttribute() }.sort()
    }

    private static StudioMetaKind metaKind(String annotationName) {
        switch (annotationName) {
            case 'StudioFacet':
                return StudioMetaKind.FACET
            case 'StudioComponent':
                return StudioMetaKind.COMPONENT
            case 'StudioElement':
                return StudioMetaKind.ELEMENT
            case 'StudioAction':
                return StudioMetaKind.ACTION
            case 'StudioDataComponent':
                return StudioMetaKind.DATA_COMPONENT
            default:
                throw new IllegalArgumentException("Unsupported meta annotation: ${annotationName}")
        }
    }

    private static final class AnnotationParseResult {
        final String name
        final String content
        final int endIndex

        AnnotationParseResult(String name, AnnotationContent content) {
            this.name = name
            this.content = content.content
            this.endIndex = content.endIndex
        }
    }

    private static final class AnnotationContent {
        final String content
        final int endIndex

        AnnotationContent(String content, int endIndex) {
            this.content = content
            this.endIndex = endIndex
        }
    }

    private static final class ImportContext {
        final Map<String, String> explicitImports
        final List<String> wildcardImports

        ImportContext(Map<String, String> explicitImports, List<String> wildcardImports) {
            this.explicitImports = explicitImports
            this.wildcardImports = wildcardImports
        }
    }

    private static final class MetaProjection {
        StudioMetaKind kind
        String xmlElement
        List<String> propertyGroups
        List<StudioPropertySignature> properties

        Map<String, Object> toComparableMap() {
            [
                    kind          : kind,
                    xmlElement    : xmlElement,
                    propertyGroups: propertyGroups,
                    properties    : properties
            ]
        }
    }
}

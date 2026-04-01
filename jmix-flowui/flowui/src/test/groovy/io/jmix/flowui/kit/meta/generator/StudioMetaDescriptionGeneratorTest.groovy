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

import io.jmix.flowui.kit.meta.StudioAction
import io.jmix.flowui.kit.meta.StudioComponent
import io.jmix.flowui.kit.meta.StudioDataComponent
import io.jmix.flowui.kit.meta.StudioElement
import io.jmix.flowui.kit.meta.StudioFacet
import io.jmix.flowui.kit.meta.StudioPropertyGroups
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

class StudioMetaDescriptionGeneratorTest extends Specification {

    @Shared
    Path workspaceRoot = StudioMetaDescriptionGenerator.detectWorkspaceRoot(Path.of('').toAbsolutePath())

    @Shared
    Path layoutXsd = requiredClasspathResource(LAYOUT_SCHEMA_RESOURCE)

    @Shared
    Path dataXsd = requiredClasspathResource(DATA_SCHEMA_RESOURCE)

    @Shared
    StudioMetaDescriptionGenerator generator = new StudioMetaDescriptionGenerator(workspaceRoot)

    @Shared
    Map<String, List<String>> javaClassIndex = buildJavaClassIndex()

    def "test generated dataLoadCoordinator meta matches exact fixture property group"() {
        when:
        def generatedMeta = generateFacetMeta('dataLoadCoordinator')

        then:
        generatedMeta.toComparableMap() == expectedMeta(TestStudioMetaDescriptions, 'dataLoadCoordinator').toComparableMap()
        generatedMeta.propertyGroups == [TestStudioMetaPropertyGroups.DataLoadCoordinatorGeneratedProperties.canonicalName]
        !containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.Auto)
    }

    def "test generated timer meta uses only exact property groups"() {
        when:
        def generatedMeta = generateFacetMeta('timer')

        then:
        propertyShape(generatedMeta.properties) == propertyShape(expectedMeta(STUDIO_FACETS_CLASS_NAME, 'timer').properties)
        generatedMeta.propertyGroups == [StudioPropertyGroups.RequiredId.canonicalName]
        !generatedMeta.propertyGroups.contains(TestStudioMetaPropertyGroups.TimerPropertiesWithDifferentRepeatingDefaultValue.canonicalName)
    }

    def "test generated avatar meta matches existing StudioComponents description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'avatar') { true }
        def expectedMeta = expectedMeta(STUDIO_COMPONENTS_CLASS_NAME, 'avatar')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties) == propertyAttributes(expectedMeta.properties)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.ClassNamesAndCss)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.ThemeNames)
    }

    def "test generated accordionPanel meta matches existing StudioElements description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'accordionPanel') { true }
        def expectedMeta = expectedMeta(STUDIO_ELEMENTS_CLASS_NAME, 'accordionPanel')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties).containsAll(propertyAttributes(expectedMeta.properties))
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.Colspan)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.Enabled)
    }

    def "test generated view action reuses common StudioActions defaults"() {
        given:
        def expectedBaseAction = expectedMeta(STUDIO_ACTIONS_CLASS_NAME, "baseAction")

        when:
        def generatedMeta = generateViewActionMeta(VIEW_ACTION_XML_ELEMENT)

        then:
        generatedMeta.kind == StudioMetaKind.ACTION
        generatedMeta.xmlElement == VIEW_ACTION_XML_ELEMENT
        propertyAttributes(generatedMeta.properties).containsAll(
                propertyAttributes(expectedBaseAction.properties) + [ACTION_TYPE_XML_ATTRIBUTE]
        )
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.RequiredId)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.Text)
        containsAnyPropertyGroup(generatedMeta.propertyGroups,
                StudioPropertyGroups.Description,
                StudioPropertyGroups.DescriptionWithoutCategory)
    }

    def "test generated collection data component matches existing StudioDataComponents description"() {
        when:
        def generatedMeta = generateViewDataMeta('collection')
        def expectedMeta = expectedMeta(STUDIO_DATA_COMPONENTS_CLASS_NAME, 'collection')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        propertyAttributes(generatedMeta.properties) == propertyAttributes(expectedMeta.properties)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.RequiredId)
        containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.EntityClass)
    }

    private MetaProjection generateFacetMeta(String elementName) {
        generateMeta(layoutXsd, elementName, candidateInContext(FACETS_CONTEXT))
    }

    private MetaProjection generateViewActionMeta(String elementName) {
        generateMeta(layoutXsd, elementName, candidateInContext(VIEW_ACTIONS_CONTEXT))
    }

    private MetaProjection generateViewDataMeta(String elementName) {
        generateMeta(dataXsd, elementName, candidateInContext(VIEW_DATA_CONTEXT))
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

    private static Closure<Boolean> candidateInContext(String contextName) {
        return { StudioXsdElementCandidate candidate -> candidate.contextNames().contains(contextName) }
    }

    private MetaProjection expectedMeta(Class<?> sourceClass, String methodName) {
        expectedMeta(sourcePathOf(sourceClass), methodName)
    }

    private MetaProjection expectedMeta(String sourceClassName, String methodName) {
        expectedMeta(sourcePathOf(sourceClassName), methodName)
    }

    private MetaProjection expectedMeta(Path sourcePath, String methodName) {
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
        repositoryRoots().findAll { Files.isDirectory(it) }.each { root ->
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

    private List<Path> repositoryRoots() {
        [
                workspaceRoot.resolve(OPEN_SOURCE_REPOSITORY_DIRECTORY),
                workspaceRoot.resolve(PREMIUM_REPOSITORY_DIRECTORY)
        ].collect { it.normalize() }
    }

    private static Path requiredClasspathResource(String resourcePath) {
        URL resource = StudioMetaDescriptionGeneratorTest.classLoader.getResource(resourcePath)
        assert resource != null: "Classpath resource ${resourcePath} not found"
        Path.of(resource.toURI())
    }

    private static Path sourcePathOf(String className) {
        sourcePathOf(loadSourceClass(className))
    }

    private static Path sourcePathOf(Class<?> sourceClass) {
        Path compiledClassRoot = Path.of(sourceClass.protectionDomain.codeSource.location.toURI())
        Path buildDirectory = findAncestor(compiledClassRoot, BUILD_DIRECTORY)
        assert buildDirectory != null: "Cannot resolve build directory for ${sourceClass.name}"

        Path moduleRoot = buildDirectory.parent
        String relativeSourcePath = sourceClass.name.replace('.', '/') + '.java'
        List<String> preferredSourceSets = compiledClassRoot.fileName?.toString() == 'test'
                ? TEST_SOURCE_SETS + MAIN_SOURCE_SETS
                : MAIN_SOURCE_SETS + TEST_SOURCE_SETS

        Path sourcePath = preferredSourceSets.stream()
                .map { sourceSet -> moduleRoot.resolve(sourceSet).resolve(relativeSourcePath).normalize() }
                .filter { Files.exists(it) }
                .findFirst()
                .orElse(null)

        assert sourcePath != null: "Cannot resolve source path for ${sourceClass.name}"
        sourcePath
    }

    private static Class<?> loadSourceClass(String className) {
        Class.forName(className, false, StudioMetaDescriptionGeneratorTest.classLoader)
    }

    private static Path findAncestor(Path path, String directoryName) {
        Path current = path
        while (current != null) {
            if (current.fileName?.toString() == directoryName) {
                return current
            }
            current = current.parent
        }
        null
    }

    private static boolean containsPropertyGroup(List<String> propertyGroups, Class<?> propertyGroup) {
        propertyGroups.contains(propertyGroup.canonicalName)
    }

    private static boolean containsAnyPropertyGroup(List<String> propertyGroups, Class<?>... propertyGroupsToCheck) {
        propertyGroupsToCheck.any { containsPropertyGroup(propertyGroups, it) }
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
        StudioMetaKind metaKind = META_KIND_BY_ANNOTATION[annotationName]
        if (metaKind == null) {
            throw new IllegalArgumentException("Unsupported meta annotation: ${annotationName}")
        }
        metaKind
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

    private static final String BUILD_DIRECTORY = 'build'

    private static final String OPEN_SOURCE_REPOSITORY_DIRECTORY = 'jmix'
    private static final String PREMIUM_REPOSITORY_DIRECTORY = 'jmix-premium'

    private static final String MAIN_JAVA_SOURCE_SET = 'src/main/java'
    private static final String MAIN_GROOVY_SOURCE_SET = 'src/main/groovy'
    private static final String TEST_JAVA_SOURCE_SET = 'src/test/java'
    private static final String TEST_GROOVY_SOURCE_SET = 'src/test/groovy'

    private static final String FLOW_RESOURCES__BASE_DIR = 'io/jmix/flowui/view/'
    private static final String LAYOUT_SCHEMA_RESOURCE = FLOW_RESOURCES__BASE_DIR + 'layout.xsd'
    private static final String DATA_SCHEMA_RESOURCE = FLOW_RESOURCES__BASE_DIR + 'data.xsd'

    private static final String FACETS_CONTEXT = 'facets'
    private static final String VIEW_ACTIONS_CONTEXT = 'viewActions'
    private static final String VIEW_DATA_CONTEXT = 'viewData'

    private static final String ACTION_TYPE_XML_ATTRIBUTE = 'type'
    private static final String VIEW_ACTION_XML_ELEMENT = 'action'

    // use string fqn because interfaces are package-private
    private static final String STUDIO_COMPONENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.component.StudioComponents'
    private static final String STUDIO_ELEMENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.element.StudioElements'
    private static final String STUDIO_ACTIONS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.action.StudioActions'
    private static final String STUDIO_DATA_COMPONENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.datacomponent.StudioDataComponents'
    private static final String STUDIO_FACETS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.facet.StudioFacets'

    private static final Map<String, StudioMetaKind> META_KIND_BY_ANNOTATION = [
            (StudioFacet.simpleName)        : StudioMetaKind.FACET,
            (StudioComponent.simpleName)    : StudioMetaKind.COMPONENT,
            (StudioElement.simpleName)      : StudioMetaKind.ELEMENT,
            (StudioAction.simpleName)       : StudioMetaKind.ACTION,
            (StudioDataComponent.simpleName): StudioMetaKind.DATA_COMPONENT
    ].asImmutable()

    private static final List<String> SUPPORTED_META_ANNOTATIONS =
            META_KIND_BY_ANNOTATION.keySet().asList().asImmutable()

    private static final List<String> TEST_SOURCE_SETS = [
            TEST_JAVA_SOURCE_SET,
            TEST_GROOVY_SOURCE_SET
    ].asImmutable()

    private static final List<String> MAIN_SOURCE_SETS = [
            MAIN_JAVA_SOURCE_SET,
            MAIN_GROOVY_SOURCE_SET
    ].asImmutable()
}

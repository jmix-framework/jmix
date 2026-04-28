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

import io.jmix.flowui.kit.meta.*
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
    StudioPropertyGroupsMatcher propertyGroupsMatcher = new StudioPropertyGroupsMatcher(workspaceRoot)

    @Shared
    Map<String, List<String>> javaClassIndex = buildJavaClassIndex()

    def "test CLI run supports interactive mode without arguments"() {
        given:
        Path sourceLayoutXsd = workspaceRoot.resolve('jmix/jmix-flowui/flowui/src/main/resources/' + LAYOUT_SCHEMA_RESOURCE)
        int layoutSchemaIndex = generator.findKnownSchemas().indexOf(sourceLayoutXsd)
        assert layoutSchemaIndex >= 0

        Path tempDirectory = Files.createTempDirectory('studio-meta-cli-run')
        Path outputPath = tempDirectory.resolve('io/jmix/flowui/kit/meta/StudioButtonCliGenerated.java')
        String input = """

                ${layoutSchemaIndex + 1}
                button

                ${outputPath}
                """.stripIndent()
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        InputStream originalIn = System.in
        PrintStream originalOut = System.out

        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()))

        when:
        StudioMetaDescriptionGenerator.CLI.run([] as String[])

        then:
        Files.exists(outputPath)
        Files.readString(outputPath, StandardCharsets.UTF_8).contains('@StudioComponent')

        String stdout = output.toString(StandardCharsets.UTF_8.name())
        stdout.contains('Known XSD schemas:')
        stdout.contains('Enter workspace root')
        stdout.contains('Generated meta description in')

        cleanup:
        System.setIn(originalIn)
        System.setOut(originalOut)
        deleteRecursively(tempDirectory)
    }

    def "test CLI run exits after printing schemas in list-schemas mode"() {
        given:
        Path sourceLayoutXsd = workspaceRoot.resolve('jmix/jmix-flowui/flowui/src/main/resources/' + LAYOUT_SCHEMA_RESOURCE)
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        InputStream originalIn = System.in
        PrintStream originalOut = System.out

        System.setIn(new ByteArrayInputStream(new byte[0]))
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()))

        when:
        StudioMetaDescriptionGenerator.CLI.run([
                '--root', workspaceRoot.toString(),
                '--list-schemas'
        ] as String[])

        then:
        String stdout = output.toString(StandardCharsets.UTF_8.name())
        stdout.contains(workspaceRoot.relativize(sourceLayoutXsd).toString().replace('\\', '/'))
        !stdout.contains('Generated meta description in')

        cleanup:
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    def "test generated dataLoadCoordinator meta ignores test fixture property groups"() {
        when:
        def generatedMeta = generateFacetMeta('dataLoadCoordinator')
        def fixtureMeta = expectedMeta(TestStudioMetaDescriptions, 'dataLoadCoordinator')
        def expectedMeta = expectedMeta(STUDIO_FACETS_CLASS_NAME, 'dataLoadCoordinator')

        then:
        generatedMeta.kind == fixtureMeta.kind
        generatedMeta.xmlElement == fixtureMeta.xmlElement
        effectivePropertyAttributes(generatedMeta) == effectivePropertyAttributes(fixtureMeta)
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
        !generatedMeta.propertyGroups.any { it.startsWith(TestStudioMetaPropertyGroups.canonicalName) }
        !containsPropertyGroup(generatedMeta.propertyGroups, StudioPropertyGroups.Auto)
    }

    def "test property group matcher scans only production sources"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('property-group-matcher-test')
        Path testGroupPath = testWorkspaceRoot.resolve(
                'jmix/test-module/src/test/java/test/StudioTestPropertyGroups.java')
        Files.createDirectories(testGroupPath.parent)
        Files.writeString(testGroupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "${StudioXmlAttributes.ID}",
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL))
                    public interface TestId {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        when:
        def groups = new StudioPropertyGroupsMatcher(testWorkspaceRoot).findMatchingGroupFqns([
                StudioPropertySignature.of(StudioXmlAttributes.ID, 'COMPONENT_ID', 'GENERAL', false, '', [])
        ], null)

        then:
        groups.empty
    }

    def "test property group matcher allows category-free groups for categorized generated properties"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('property-group-matcher-category-test')
        Path testGroupPath = testWorkspaceRoot.resolve(
                'jmix/test-module/src/main/java/test/StudioTestPropertyGroups.java')
        Files.createDirectories(testGroupPath.parent)
        Files.writeString(testGroupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "${StudioXmlAttributes.TEXT}",
                            type = StudioPropertyType.LOCALIZED_STRING))
                    public interface TextWithoutCategory {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        when:
        def groups = new StudioPropertyGroupsMatcher(testWorkspaceRoot).findMatchingGroupFqns([
                StudioPropertySignature.of(StudioXmlAttributes.TEXT, 'LOCALIZED_STRING', 'GENERAL', false, '', [])
        ], null)

        then:
        groups == ['test.StudioTestPropertyGroups.TextWithoutCategory']
    }

    def "test property group matcher resolves wildcard-imported parent groups"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('property-group-matcher-wildcard-parent-test')
        Path baseGroupPath = testWorkspaceRoot.resolve(
                'jmix/test-module/src/main/java/test/BasePropertyGroups.java')
        Path testGroupPath = testWorkspaceRoot.resolve(
                'jmix/test-module/src/main/java/test/StudioTestPropertyGroups.java')
        Files.createDirectories(testGroupPath.parent)
        Files.writeString(baseGroupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class BasePropertyGroups {
                    private BasePropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(
                            xmlAttribute = "${StudioXmlAttributes.ID}",
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID,
                            required = true
                    ))
                    public interface RequiredId {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)
        Files.writeString(testGroupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import test.BasePropertyGroups.*;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup
                    public interface DefaultProperties extends RequiredId {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        when:
        def groups = new StudioPropertyGroupsMatcher(testWorkspaceRoot).findMatchingGroupFqns([
                StudioPropertySignature.of(StudioXmlAttributes.ID, 'COMPONENT_ID', 'GENERAL', true, '', [])
        ], null)

        then:
        groups == ['test.StudioTestPropertyGroups.DefaultProperties']
    }

    def "test property group matcher finds property groups by java class and xml element"() {
        when:
        def groups = new StudioPropertyGroupsMatcher(workspaceRoot).findPropertyGroupsByClassFqn(
                'io.jmix.flowui.component.details.JmixDetails',
                'details',
                workspaceRoot.resolve('jmix/jmix-flowui/flowui-kit')
        )

        then:
        groups == [StudioPropertyGroups.DetailsDefaultProperties.canonicalName]
    }

    def "test property group matcher disambiguates shared xml element by java class"() {
        given:
        def matcher = new StudioPropertyGroupsMatcher(workspaceRoot)
        def moduleRoot = workspaceRoot.resolve('jmix/jmix-flowui/flowui-kit')

        when:
        def dropdownTextItemGroups = matcher.findPropertyGroupsByClassFqn(
                'io.jmix.flowui.kit.component.dropdownbutton.TextItem',
                'textItem',
                moduleRoot
        )
        def userMenuTextItemGroups = matcher.findPropertyGroupsByClassFqn(
                'io.jmix.flowui.kit.component.usermenu.TextUserMenuItem',
                'textItem',
                moduleRoot
        )

        then:
        dropdownTextItemGroups == [
                StudioPropertyGroups.RequiredId.canonicalName,
                StudioPropertyGroups.Text.canonicalName
        ]
        userMenuTextItemGroups == [StudioPropertyGroups.TextUserItemUserMenuItemComponent.canonicalName]
    }

    def "test generator prefers explicit complex type to java class mapping before fallback"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('generator-explicit-property-groups-test')
        Path groupPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/java/test/StudioTestPropertyGroups.java')
        Path uiKitPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/java/test/StudioTestComponents.java')
        Path schemaPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/resources/test-layout.xsd')

        Files.createDirectories(groupPath.parent)
        Files.createDirectories(schemaPath.parent)

        Files.writeString(groupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = {
                            @StudioProperty(xmlAttribute = "${StudioXmlAttributes.ID}",
                                    category = StudioProperty.Category.GENERAL,
                                    type = StudioPropertyType.COMPONENT_ID),
                            @StudioProperty(xmlAttribute = "${StudioXmlAttributes.VISIBLE}",
                                    category = StudioProperty.Category.GENERAL,
                                    type = StudioPropertyType.BOOLEAN,
                                    defaultValue = "true")
                    })
                    public interface Preferred {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        Files.writeString(uiKitPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioComponent;
                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyType;
                import io.jmix.flowui.kit.meta.StudioUiKit;
                import io.jmix.flowui.kit.meta.StudioXmlElements;

                @StudioUiKit
                public interface StudioTestComponents {

                    @StudioComponent(
                            name = "TestComponent",
                            classFqn = "test.TestComponent",
                            category = "Test",
                            xmlElement = StudioXmlElements.TEST_COMPONENT,
                            propertyGroups = StudioTestPropertyGroups.Preferred.class,
                            properties = @StudioProperty(
                                    xmlAttribute = "${StudioXmlAttributes.ID}",
                                    category = StudioProperty.Category.GENERAL,
                                    type = StudioPropertyType.COMPONENT_ID
                            )
                    )
                    Object testComponent();
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        Files.writeString(schemaPath, """
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                           targetNamespace="http://test"
                           xmlns="http://test"
                           elementFormDefault="qualified">
                    <xs:element name="testComponent" type="testComponentType"/>

                    <xs:complexType name="testComponentType">
                        <xs:attribute name="${StudioXmlAttributes.ID}" type="xs:string"/>
                    </xs:complexType>
                </xs:schema>
                """.stripIndent(), StandardCharsets.UTF_8)

        def generator = new StudioMetaDescriptionGenerator(testWorkspaceRoot, ['testComponentType': 'test.TestComponent'])
        def candidate = generator.findElementCandidates(schemaPath, 'testComponent').first()

        when:
        def generatedMeta = parseMetaSource(
                generator.generate(candidate, StudioMetaKind.AUTO, schemaPath.resolveSibling('GeneratedTestMeta.java')).source()
        )

        then:
        generatedMeta.propertyGroups == ['test.StudioTestPropertyGroups.Preferred']
    }

    def "test generator falls back to property signature lookup when explicit mapping has no match"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('generator-property-groups-fallback-test')
        Path groupPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/java/test/StudioTestPropertyGroups.java')
        Path schemaPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/resources/test-layout.xsd')

        Files.createDirectories(groupPath.parent)
        Files.createDirectories(schemaPath.parent)

        Files.writeString(groupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(
                            xmlAttribute = "${StudioXmlAttributes.ID}",
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID
                    ))
                    public interface Exact {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        Files.writeString(schemaPath, """
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                           targetNamespace="http://test"
                           xmlns="http://test"
                           elementFormDefault="qualified">
                    <xs:element name="testComponent" type="testComponentType"/>

                    <xs:complexType name="testComponentType">
                        <xs:attribute name="${StudioXmlAttributes.ID}" type="xs:string"/>
                    </xs:complexType>
                </xs:schema>
                """.stripIndent(), StandardCharsets.UTF_8)

        def generator = new StudioMetaDescriptionGenerator(testWorkspaceRoot, ['testComponentType': 'test.MissingComponent'])
        def candidate = generator.findElementCandidates(schemaPath, 'testComponent').first()

        when:
        def generatedMeta = parseMetaSource(
                generator.generate(candidate, StudioMetaKind.AUTO, schemaPath.resolveSibling('GeneratedTestMeta.java')).source()
        )

        then:
        generatedMeta.propertyGroups == ['test.StudioTestPropertyGroups.Exact']
    }

    def "test generator supplements explicit complex type property groups with fallback"() {
        given:
        Path testWorkspaceRoot = Files.createTempDirectory('generator-explicit-partial-property-groups-test')
        Path groupPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/java/test/StudioTestPropertyGroups.java')
        Path schemaPath = testWorkspaceRoot.resolve('jmix/test-module/src/main/resources/test-layout.xsd')

        Files.createDirectories(groupPath.parent)
        Files.createDirectories(schemaPath.parent)

        Files.writeString(groupPath, """
                package test;

                import io.jmix.flowui.kit.meta.StudioProperty;
                import io.jmix.flowui.kit.meta.StudioPropertyGroup;
                import io.jmix.flowui.kit.meta.StudioPropertyType;

                public final class StudioTestPropertyGroups {
                    private StudioTestPropertyGroups() {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(
                            xmlAttribute = "${StudioXmlAttributes.MESSAGE}",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ))
                    public interface Message {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(
                            xmlAttribute = "${StudioXmlAttributes.BEAN}",
                            type = StudioPropertyType.STRING,
                            required = true
                    ))
                    public interface Bean {
                    }

                    @StudioPropertyGroup(properties = @StudioProperty(
                            xmlAttribute = "${StudioXmlAttributes.NAME}",
                            type = StudioPropertyType.STRING
                    ))
                    public interface Name {
                    }
                }
                """.stripIndent(), StandardCharsets.UTF_8)

        Files.writeString(schemaPath, """
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                           targetNamespace="http://test"
                           xmlns="http://test"
                           elementFormDefault="qualified">
                    <xs:element name="custom" type="customValidatorType"/>

                    <xs:complexType name="messageValidatorType">
                        <xs:attribute name="${StudioXmlAttributes.MESSAGE}" type="xs:string"/>
                    </xs:complexType>

                    <xs:complexType name="customValidatorType">
                        <xs:complexContent>
                            <xs:extension base="messageValidatorType">
                                <xs:attribute name="${StudioXmlAttributes.BEAN}" type="xs:string" use="required"/>
                                <xs:attribute name="${StudioXmlAttributes.NAME}" type="xs:string"/>
                            </xs:extension>
                        </xs:complexContent>
                    </xs:complexType>
                </xs:schema>
                """.stripIndent(), StandardCharsets.UTF_8)

        def generator = new StudioMetaDescriptionGenerator(
                testWorkspaceRoot,
                [:],
                ['customValidatorType': ['test.StudioTestPropertyGroups.Message', 'test.StudioTestPropertyGroups.Bean']]
        )
        def candidate = generator.findElementCandidates(schemaPath, 'custom').first()

        when:
        def generatedMeta = parseMetaSource(
                generator.generate(candidate, StudioMetaKind.AUTO, schemaPath.resolveSibling('GeneratedTestMeta.java')).source()
        )

        then:
        generatedMeta.propertyGroups == [
                'test.StudioTestPropertyGroups.Message',
                'test.StudioTestPropertyGroups.Bean',
                'test.StudioTestPropertyGroups.Name'
        ]
    }

    def "test generated timer meta uses only exact property groups"() {
        when:
        def generatedMeta = generateFacetMeta('timer')
        def expectedMeta = expectedMeta(STUDIO_FACETS_CLASS_NAME, 'timer')

        then:
        effectivePropertyShape(generatedMeta) == effectivePropertyShape(expectedMeta)
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
        !generatedMeta.propertyGroups.contains(TestStudioMetaPropertyGroups.TimerPropertiesWithDifferentRepeatingDefaultValue.canonicalName)
    }

    def "test generated avatar meta matches existing StudioComponents description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'avatar') { true }
        def expectedMeta = expectedMeta(STUDIO_COMPONENTS_CLASS_NAME, 'avatar')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        normalizedEffectivePropertyAttributes(generatedMeta) == normalizedEffectivePropertyAttributes(expectedMeta)
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
    }

    def "test generated button meta prefers grouped properties over inline properties"() {
        when:
        StudioXsdElementCandidate candidate = findCandidate(layoutXsd, 'button') { true }
        Path outputPath = Files.createTempDirectory('generated-meta-test').resolve('TestGeneratedMeta.java')
        String generatedSource = generator.generate(candidate, StudioMetaKind.AUTO, outputPath).source()
        def generatedMeta = parseMetaSource(generatedSource)
        def expectedMeta = expectedMeta(STUDIO_COMPONENTS_CLASS_NAME, 'button')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        generatedMeta.propertyGroups == [StudioPropertyGroups.ButtonComponent.canonicalName]
        generatedMeta.properties.empty
        !generatedSource.contains('properties =')
        effectivePropertyAttributes(generatedMeta) == effectivePropertyAttributes(expectedMeta)
    }

    def "test generated accordionPanel meta matches existing StudioElements description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'accordionPanel') { true }
        def expectedMeta = expectedMeta(STUDIO_ELEMENTS_CLASS_NAME, 'accordionPanel')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        effectivePropertyAttributes(generatedMeta).containsAll(effectivePropertyAttributes(expectedMeta))
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
    }

    def "test generated view action reuses common StudioActions defaults"() {
        given:
        def expectedBaseAction = expectedMeta(STUDIO_ACTIONS_CLASS_NAME, "baseAction")

        when:
        def generatedMeta = generateViewActionMeta(VIEW_ACTION_XML_ELEMENT)

        then:
        generatedMeta.kind == StudioMetaKind.ACTION
        generatedMeta.xmlElement == VIEW_ACTION_XML_ELEMENT
        effectivePropertyAttributes(generatedMeta).containsAll(
                effectivePropertyAttributes(expectedBaseAction) + [ACTION_TYPE_XML_ATTRIBUTE]
        )
        propertyAttributes(generatedMeta.properties) == [ACTION_TYPE_XML_ATTRIBUTE]
        !generatedMeta.propertyGroups.empty
    }

    def "test generated collection data component matches existing StudioDataComponents description"() {
        when:
        def generatedMeta = generateViewDataMeta('collection')
        def expectedMeta = expectedMeta(STUDIO_DATA_COMPONENTS_CLASS_NAME, 'collection')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        normalizedEffectivePropertyAttributes(generatedMeta) == normalizedEffectivePropertyAttributes(expectedMeta)
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
    }

    def "test generated future validator meta matches existing StudioValidatorsElements description"() {
        when:
        def generatedMeta = generateMeta(layoutXsd, 'future') { true }
        def expectedMeta = expectedMeta(STUDIO_VALIDATORS_ELEMENTS_CLASS_NAME, 'future')

        then:
        generatedMeta.kind == expectedMeta.kind
        generatedMeta.xmlElement == expectedMeta.xmlElement
        effectivePropertyAttributes(generatedMeta) == effectivePropertyAttributes(expectedMeta)
        generatedMeta.propertyGroups == expectedMeta.propertyGroups
    }

    def "test generated source uses StudioXmlAttributes and StudioXmlElements for known values"() {
        given:
        StudioXsdElementCandidate componentCandidate = findCandidate(layoutXsd, 'avatar') { true }
        StudioXsdElementCandidate facetCandidate = findCandidate(layoutXsd, 'dataLoadCoordinator', candidateInContext(FACETS_CONTEXT))
        Path componentOutputPath = Files.createTempDirectory('generated-meta-source-test').resolve('TestGeneratedMeta.java')
        Path facetOutputPath = Files.createTempDirectory('generated-facet-meta-source-test').resolve('TestGeneratedFacetMeta.java')

        when:
        String componentSource = generator.generate(componentCandidate, StudioMetaKind.AUTO, componentOutputPath).source()
        String facetSource = generator.generate(facetCandidate, StudioMetaKind.AUTO, facetOutputPath).source()

        then:
        componentSource.contains("xmlElement = ${StudioXmlElements.name}.AVATAR")
        componentSource.contains("${StudioPropertyGroups.name}.AvatarComponent.class")
        componentSource.contains("xmlAttribute = ${StudioXmlAttributes.name}.NAME")
        facetSource.contains("xmlElement = ${StudioXmlElements.name}.DATA_LOAD_COORDINATOR")
    }

    def "production meta descriptions do not combine property groups with inline properties"() {
        when:
        def violations = scanProductionStudioUiKitMetaProjections()
                .findAll { it.projection.propertyGroupsDeclared && !it.projection.properties.empty }
                .collect { "${it.sourceClassName}#${it.methodName}" }

        then:
        violations.empty
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

    private List<MetaSourceProjection> scanProductionStudioUiKitMetaProjections() {
        scanProductionStudioUiKitMetaProjections(workspaceRoot)
    }

    private List<MetaSourceProjection> scanProductionStudioUiKitMetaProjections(Path scanWorkspaceRoot) {
        List<MetaSourceProjection> projections = []
        Map<String, List<String>> classIndex = buildJavaClassIndex(scanWorkspaceRoot)

        repositoryRoots(scanWorkspaceRoot).findAll { Files.isDirectory(it) }.each { Path repositoryRoot ->
            Files.walk(repositoryRoot).withCloseable { stream ->
                stream
                        .filter { path -> path.toString().endsWith('.java') }
                        .filter { path -> path.toString().contains("/${MAIN_JAVA_SOURCE_SET}/") }
                        .sorted()
                        .forEach { Path sourcePath ->
                            String source = Files.readString(sourcePath, StandardCharsets.UTF_8)
                            if (!source.contains('@StudioUiKit')) {
                                return
                            }
                            projections.addAll(parseMetaSourceFile(source, classIndex))
                        }
            }
        }

        projections
    }

    private static List<StudioPropertySignature> collectSnapshotProperties(MetaProjection projection,
                                                                           StudioPropertyGroupsMatcher matcher,
                                                                           MetaPropertyCollectionMode collectionMode) {
        Set<StudioPropertySignature> properties = new LinkedHashSet<>()

        switch (collectionMode) {
            case MetaPropertyCollectionMode.EFFECTIVE:
                if (projection.propertyGroupsDeclared) {
                    properties.addAll(matcher.flattenProperties(projection.propertyGroups))
                }
                properties.addAll(projection.properties)
                break
            case MetaPropertyCollectionMode.PROPERTY_GROUPS_FIRST_WITH_INLINE_FALLBACK:
                if (projection.propertyGroupsDeclared) {
                    properties.addAll(matcher.flattenProperties(projection.propertyGroups))
                } else {
                    properties.addAll(projection.properties)
                }
                break
        }

        properties.toList().sort { StudioPropertySignature left, StudioPropertySignature right ->
            signatureSortKey(left) <=> signatureSortKey(right)
        }
    }

    private List<MetaSourceProjection> parseMetaSourceFile(String source, Map<String, List<String>> classIndex) {
        String strippedSource = stripComments(source)
        if (!strippedSource.contains('@StudioUiKit')) {
            return List.of()
        }

        String packageName = parsePackageName(strippedSource)
        String outerClassName = parseOuterClassName(strippedSource)
        ImportContext importContext = parseImports(strippedSource)
        List<MetaSourceProjection> projections = []

        int currentIndex = 0
        while (true) {
            AnnotationParseResult annotation = findNextMetaAnnotation(strippedSource, currentIndex)
            if (annotation == null) {
                break
            }

            Map<String, String> annotationArguments = parseNamedArguments(annotation.content)
            String methodName = findNextMethodName(strippedSource, annotation.endIndex)
            MetaProjection projection = new MetaProjection(
                    kind: metaKind(annotation.name),
                    xmlElement: unquoteJavaString(annotationArguments.getOrDefault('xmlElement', methodName)),
                    propertyGroupsDeclared: annotationArguments.containsKey('propertyGroups'),
                    propertyGroups: parseClassArray(annotationArguments.get('propertyGroups'), packageName, outerClassName,
                            importContext, classIndex),
                    properties: parseProperties(annotationArguments.get('properties'))
            )

            projections.add(new MetaSourceProjection(
                    name: unquoteJavaString(annotationArguments.getOrDefault('name', methodName)),
                    sourceClassName: "${packageName}.${outerClassName}",
                    methodName: methodName,
                    projection: projection
            ))

            currentIndex = annotation.endIndex
        }

        projections
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
                propertyGroupsDeclared: annotationArguments.containsKey('propertyGroups'),
                propertyGroups: parseClassArray(annotationArguments.get('propertyGroups'), packageName, outerClassName,
                        importContext, javaClassIndex),
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

    private static AnnotationParseResult findNextMetaAnnotation(String source, int startIndex) {
        List<Map<String, Object>> matches = SUPPORTED_META_ANNOTATIONS.collect { annotationName ->
            Matcher matcher = Pattern.compile("@${annotationName}\\s*\\(").matcher(source)
            matcher.region(startIndex, source.length())
            matcher.find() ? [name: annotationName, index: matcher.start(), parenIndex: matcher.end() - 1] : null
        }.findAll { it != null } as List<Map<String, Object>>

        if (matches.empty) {
            return null
        }

        Map<String, Object> firstMatch = matches.min { it.index as int }
        new AnnotationParseResult(
                firstMatch.name as String,
                parseAnnotationContent(source, firstMatch.parenIndex as int)
        )
    }

    private static String findNextMethodName(String source, int startIndex) {
        Matcher matcher = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\(").matcher(source)
        matcher.region(startIndex, source.length())
        assert matcher.find(): 'Method declaration after annotation not found'
        matcher.group(1)
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
                                         ImportContext importContext,
                                         Map<String, List<String>> classIndex) {
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
                .collect { resolveClassReference(it, packageName, outerClassName, importContext, classIndex) }
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
                                         ImportContext importContext,
                                         Map<String, List<String>> classIndex) {
        String sanitizedReference = reference.replace('.class', '').strip()
        if (!sanitizedReference.contains('.')) {
            String explicitImport = importContext.explicitImports[sanitizedReference]
            if (explicitImport) {
                return explicitImport
            }
            String samePackageClass = findIndexedClassFqn(classIndex, sanitizedReference, packageName)
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
        String indexedSamePackage = findIndexedClassFqn(classIndex, firstSegment, packageName)
        if (indexedSamePackage) {
            return indexedSamePackage + sanitizedReference.substring(firstSegment.length())
        }
        String indexedWildcard = findIndexedClassFqn(classIndex, firstSegment, importContext.wildcardImports)
        if (indexedWildcard) {
            return indexedWildcard + sanitizedReference.substring(firstSegment.length())
        }
        if (!importContext.wildcardImports.empty) {
            return "${importContext.wildcardImports.first()}.${sanitizedReference}"
        }
        return "${packageName}.${sanitizedReference}"
    }

    private static String findIndexedClassFqn(Map<String, List<String>> classIndex, String simpleName, String packageName) {
        classIndex.getOrDefault(simpleName, List.of())
                .find { it.startsWith("${packageName}.") }
    }

    private static String findIndexedClassFqn(Map<String, List<String>> classIndex,
                                              String simpleName,
                                              List<String> candidatePackages) {
        classIndex.getOrDefault(simpleName, List.of())
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
        buildJavaClassIndex(workspaceRoot)
    }

    private Map<String, List<String>> buildJavaClassIndex(Path scanWorkspaceRoot) {
        Map<String, List<String>> index = [:].withDefault { [] }
        repositoryRoots(scanWorkspaceRoot).findAll { Files.isDirectory(it) }.each { root ->
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

    private static List<Path> repositoryRoots(Path scanWorkspaceRoot) {
        [
                scanWorkspaceRoot.resolve(OPEN_SOURCE_REPOSITORY_DIRECTORY),
                scanWorkspaceRoot.resolve(PREMIUM_REPOSITORY_DIRECTORY)
        ].collect { it.normalize() }
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return
        }

        Files.walk(root)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
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

    private static Path currentModuleRoot() {
        Path compiledClassRoot = Path.of(StudioMetaDescriptionGeneratorTest.protectionDomain.codeSource.location.toURI())
        Path buildDirectory = findAncestor(compiledClassRoot, BUILD_DIRECTORY)
        assert buildDirectory != null: "Cannot resolve build directory for ${StudioMetaDescriptionGeneratorTest.name}"
        buildDirectory.parent
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
        String constantValue = StudioXmlAttributes.resolveConstantValue(stripped)
        if (constantValue != null) {
            return constantValue
        }
        constantValue = StudioXmlElements.resolveConstantValue(stripped)
        if (constantValue != null) {
            return constantValue
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

    private List<StudioPropertySignature> effectiveProperties(MetaProjection projection) {
        collectSnapshotProperties(projection, propertyGroupsMatcher, MetaPropertyCollectionMode.EFFECTIVE)
    }

    private List<Map<String, Object>> effectivePropertyShape(MetaProjection projection) {
        propertyShape(effectiveProperties(projection))
    }

    private List<String> effectivePropertyAttributes(MetaProjection projection) {
        propertyAttributes(effectiveProperties(projection))
    }

    private List<String> normalizedEffectivePropertyAttributes(MetaProjection projection) {
        effectivePropertyAttributes(projection).toSet().toList().sort()
    }

    private static String signatureSortKey(StudioPropertySignature signature) {
        "${signature.xmlAttribute()}|${formatPropertySignature(signature)}"
    }

    private static String formatPropertySignature(StudioPropertySignature propertySignature) {
        List<String> parts = [
                propertySignature.xmlAttribute(),
                "type=${propertySignature.type()}"
        ]

        if (propertySignature.classFqn()) {
            parts.add("classFqn=${propertySignature.classFqn()}")
        }
        if (propertySignature.category() != null) {
            parts.add("category=${propertySignature.category()}")
        }
        if (propertySignature.required()) {
            parts.add('required=true')
        }
        if (propertySignature.defaultValue()) {
            parts.add("default=${propertySignature.defaultValue()}")
        }
        if (propertySignature.defaultValueRef()) {
            parts.add("defaultRef=${propertySignature.defaultValueRef()}")
        }
        if (propertySignature.initialValue()) {
            parts.add("initial=${propertySignature.initialValue()}")
        }
        if (!propertySignature.options().empty) {
            parts.add("options=${propertySignature.options()}")
        }
        if (propertySignature.setMethod()) {
            parts.add("setMethod=${propertySignature.setMethod()}")
        }
        if (propertySignature.setParameterFqn()) {
            parts.add("setParameter=${propertySignature.setParameterFqn()}")
        }
        if (propertySignature.addMethod()) {
            parts.add("addMethod=${propertySignature.addMethod()}")
        }
        if (propertySignature.addParameterFqn()) {
            parts.add("addParameter=${propertySignature.addParameterFqn()}")
        }
        if (propertySignature.removeMethod()) {
            parts.add("removeMethod=${propertySignature.removeMethod()}")
        }
        if (propertySignature.removeParameterFqn()) {
            parts.add("removeParameter=${propertySignature.removeParameterFqn()}")
        }
        if (propertySignature.typeParameter()) {
            parts.add("typeParameter=${propertySignature.typeParameter()}")
        }
        if (propertySignature.useAsInjectionType()) {
            parts.add('useAsInjectionType=true')
        }
        if (!propertySignature.componentRefTags().empty) {
            parts.add("componentRefTags=${propertySignature.componentRefTags()}")
        }
        if (propertySignature.cdataWrapperTag()) {
            parts.add("cdataWrapperTag=${propertySignature.cdataWrapperTag()}")
        }

        parts.join('; ')
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
        boolean propertyGroupsDeclared
        List<String> propertyGroups
        List<StudioPropertySignature> properties
    }

    private static final class MetaSourceProjection {
        String name
        String sourceClassName
        String methodName
        MetaProjection projection
    }

    private static enum MetaPropertyCollectionMode {
        EFFECTIVE,
        PROPERTY_GROUPS_FIRST_WITH_INLINE_FALLBACK
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

    private static final String ACTION_TYPE_XML_ATTRIBUTE = StudioXmlAttributes.TYPE
    private static final String VIEW_ACTION_XML_ELEMENT = 'action'

    // use string fqn because interfaces are package-private
    private static final String STUDIO_COMPONENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.component.StudioComponents'
    private static final String STUDIO_ELEMENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.element.StudioElements'
    private static final String STUDIO_VALIDATORS_ELEMENTS_CLASS_NAME =
            'io.jmix.flowui.kit.meta.element.StudioValidatorsElements'
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

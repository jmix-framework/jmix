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

package io.jmix.gradle

import groovy.xml.MarkupBuilder
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvedDependency

import static io.jmix.gradle.MetaModelUtil.*

class EnhancingAction implements Action<Task> {
    static String CONVERTERS_LIST_PROPERTY = "io.jmix.enhancing.converters-list"

    private String sourceSetName

    EnhancingAction(String sourceSetName) {
        this.sourceSetName = sourceSetName
    }

    @Override
    void execute(Task task) {
        Project project = task.getProject()

        project.logger.lifecycle "Enhancing entities in $project for source set '$sourceSetName'"

        ClassesInfo classesInfo = new ClassesInfo()
        def sourceSet = project.sourceSets.findByName(sourceSetName)

        generateEntityClassesList(project, sourceSet, classesInfo)
        project.logger.lifecycle("Found JPA entities: ${classesInfo.mappedClasses()}, other model objects: $classesInfo.nonMappedClasses")

        project.jmix.entitiesEnhancing.jpaConverters.each {
            classesInfo.converters.add(it)
        }

        constructPersistenceXml(project, sourceSet, classesInfo)

        runEclipseLinkEnhancing(project, sourceSet, classesInfo)

        runJmixEnhancing(project, sourceSet, classesInfo)
    }

    protected void generateEntityClassesList(Project project, sourceSet, ClassesInfo classesInfo/*classNames,  nonMappedClassNames, Map<String, String> storesByClasses*/) {
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()
        classPool.insertClassPath(sourceSet.java.outputDir.absolutePath)

        sourceSet.allJava.getSrcDirs().each { File srcDir ->
            project.fileTree(srcDir).each { File file ->
                if (file.name.endsWith('.java')) {
                    String pathStr = srcDir.toPath().relativize(file.toPath()).join('.')
                    String className = pathStr.substring(0, pathStr.length() - '.java'.length())

                    CtClass ctClass = null
                    try {
                        ctClass = classPool.get(className)
                    } catch (NotFoundException e) {
                        project.logger.info "Cannot find $className in ${project} for enhancing: $e"
                    }

                    if (ctClass != null) {
                        if (isJpaEntity(ctClass) || isJpaMappedSuperclass(ctClass) || isJpaEmbeddable(ctClass)) {
                            classesInfo.classesByStores[findStoreName(ctClass) ?: "main"].add(className)
//todo taimanov "main" to constant
                        } else if (isJpaConverter(ctClass)) {
                            classesInfo.converters.add(className)
                        } else if (isModelObject(ctClass)) {
                            classesInfo.nonMappedClasses.add(className)
                        }
                    }
                }
            }
        }
    }

    protected boolean findEclipseLink(Set<ResolvedDependency> deps) {
        for (def dep : deps) {
            if (dep.moduleGroup == 'org.eclipse.persistence' && dep.moduleName == 'org.eclipse.persistence.jpa')
                return true
            if (findEclipseLink(dep.children))
                return true
        }
        return false
    }

    protected void constructPersistenceXml(Project project, sourceSet, ClassesInfo classesInfo) {
        if (classesInfo.hasMappedClasses()) {
            def jars = sourceSet.compileClasspath.asList().findAll { it.name.endsWith('.jar') }
            jars.each { lib ->
                project.zipTree(lib).matching { include "**/*persistence.xml" }.each {
                    Node doc = new XmlParser().parse(it)
                    def docPu = doc.'persistence-unit'[0]
                    List<String> currentEntities = docPu.'class'.collect { it.text() }
                    String storeName = docPu.@name
                    classesInfo.classesByStores[storeName ?: "main"].addAll(currentEntities)

                    String converters = docPu.'properties'.'*'.find { it.@name == CONVERTERS_LIST_PROPERTY }?.@value

                    converters?.split(';')?.each { classesInfo.converters.add(it) }

                    project.logger.info("Converters: '$converters'")

                    project.logger.info("Found $it.name in $lib.name. Entities: $currentEntities.")
                }
            }

            for (String storeName : classesInfo.allStores()) {
                String persistenceFilePath = "${storeName == "main" ? "" : (storeName + '-')}persistence/META-INF/persistence.xml"

                File file = new File(project.buildDir, "tmp/entitiesEnhancing/$sourceSetName/$persistenceFilePath")


                file.parentFile.mkdirs()
                file.withWriter { writer ->
                    def xml = new MarkupBuilder(writer)
                    xml.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8")
                    xml.persistence(version: '2.0', xmlns: 'http://java.sun.com/xml/ns/persistence',
                            'xmlns:xsi': "http://www.w3.org/2001/XMLSchema-instance",
                            'xsi:schemaLocation': "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd") {
                        'persistence-unit'(name: storeName) {
                            'provider'('io.jmix.data.impl.JmixPersistenceProvider')
                            classesInfo.storeClasses(storeName).each { String name ->
                                'class'(name)
                            }
                            'exclude-unlisted-classes'()
                            'properties'() {
                                'property'(name: 'eclipselink.weaving', value: 'static')
                                'property'(name: CONVERTERS_LIST_PROPERTY, value: classesInfo.converters.join(';'))
                            }
                        }
                    }
                }

                project.logger.info("Constructed $persistenceFilePath for ${project.displayName}.")
                if ('main'.equals(sourceSetName)) {
                    project.jar {
                        from("$project.buildDir/tmp/entitiesEnhancing/$sourceSetName/") {
                            include persistenceFilePath
                        }
                    }
                }
            }
        }
    }

    protected void runEclipseLinkEnhancing(Project project, sourceSet, ClassesInfo classesInfo) {
        for (storeName in classesInfo.allStores()) {
            def conf = project.configurations.findByName(sourceSet.getCompileClasspathConfigurationName())
            if (!findEclipseLink(conf.resolvedConfiguration.firstLevelModuleDependencies)) {
                project.logger.info("EclipseLink not found in classpath, EclipseLink enhancer will not run")
                return
            }

            project.logger.lifecycle "Running EclipseLink enhancer in $project for $sourceSet"

            project.javaexec {
                main = 'org.eclipse.persistence.tools.weaving.jpa.StaticWeave'
                classpath(
                        project.configurations.enhancing.asFileTree.asPath,
                        sourceSet.compileClasspath,
                        sourceSet.java.outputDir
                )
                args "-loglevel"
                args "INFO"
                args "-persistenceinfo"
                args "${project.buildDir}/tmp/entitiesEnhancing/$sourceSetName/${storeName == "main" ? "" : (storeName + '-')}persistence"
                args sourceSet.java.outputDir.absolutePath
                args sourceSet.java.outputDir.absolutePath
                debug = project.hasProperty("debugEnhancing") ? Boolean.valueOf(project.getProperty("debugEnhancing")) : false
            }
        }
    }

    protected void runJmixEnhancing(Project project, sourceSet, ClassesInfo classesInfo) {
        if (!classesInfo.allEntities().isEmpty()) {
            project.logger.lifecycle "Running Jmix enhancer in $project for $sourceSet"

            String javaOutputDir = sourceSet.java.outputDir.absolutePath

            for (EnhancingStep step : enhancingSteps()) {

                ClassPool classPool = new ClassPool(null)
                classPool.appendSystemPath()

                for (File file in sourceSet.compileClasspath) {
                    classPool.insertClassPath(file.getAbsolutePath())
                }

                classPool.insertClassPath(javaOutputDir)

                project.configurations.enhancing.files.each { File dep ->
                    classPool.insertClassPath(dep.absolutePath)
                }

                step.classPool = classPool
                step.outputDir = javaOutputDir
                step.logger = project.logger

                for (className in classesInfo.allEntities()) {
                    def classFileName = className.replace('.', '/') + '.class'
                    def classFile = new File(javaOutputDir, classFileName)

                    if (classFile.exists()) {
                        // skip files from dependencies, enhance only classes from `javaOutputDir`
                        step.execute(className)
                    }
                }
            }
        }
    }

    protected static List<EnhancingStep> enhancingSteps() {
        Arrays.asList(
                new JmixEntityEnhancingStep(),
                new EntityEntryEnhancingStep(),
                new SettersEnhancingStep(),
                new TransientAnnotationEnhancingStep())
    }


    private class ClassesInfo {
        Map<String, Set<String>> classesByStores = [:].withDefault { _ -> new HashSet<String>() }
        Set<String> converters = [] as Set<String>
        Set<String> nonMappedClasses = [] as Set<String>

        Collection<String> mappedClasses() {
            return classesByStores.values().flatten()
        }

        Collection<String> allEntities() {
            return mappedClasses() + nonMappedClasses
        }

        Collection<String> storeClasses(String store) {
            return classesByStores[store] + converters
        }

        def hasMappedClasses() {
            return classesByStores.values().any { !it.isEmpty() } || !converters.isEmpty()
        }

        Collection<String> allStores() {
            return classesByStores.size() > 0 ? classesByStores.keySet() :
                    converters.size() > 0 ? ['main'] : []
        }

    }

}

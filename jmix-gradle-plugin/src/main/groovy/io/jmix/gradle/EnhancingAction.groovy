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

import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

import static io.jmix.gradle.DescriptorGenerationUtils.CONVERTERS_LIST_PROPERTY
import static io.jmix.gradle.MetaModelUtil.*

class EnhancingAction implements Action<Task> {

    static final String MAIN_SET_NAME = "main"
    static final String TEST_SET_NAME = "test"

    static final String MAIN_STORE_NAME = "main"

    private String sourceSetName

    EnhancingAction(String sourceSetName) {
        this.sourceSetName = sourceSetName
    }

    @Override
    void execute(Task task) {
        Project project = task.getProject()
        project.logger.lifecycle "Enhancing entities in $project for source set '$sourceSetName'"
        SourceSet sourceSet = project.sourceSets.findByName(sourceSetName)

        ClassesInfo classesInfo = collectClasses(project, sourceSet)

        boolean entitiesEnhancingRequired = !project.jmix.entitiesEnhancing.skipUnmodifiedEntitiesEnhancing ||
                entityClassesChangedSinceLastBuild(project, sourceSet, classesInfo)

        if (entitiesEnhancingRequired) {
            constructDescriptors(project, sourceSet, classesInfo)
            persistenceProviderEnhancing().run(project, sourceSet, classesInfo.allStores())
            runJmixEnhancing(project, sourceSet, classesInfo)

            if (project.jmix.entitiesEnhancing.skipUnmodifiedEntitiesEnhancing) {
                saveEntityClassesChecksumForNextBuild(project, sourceSet, classesInfo)
            }
        } else {
            project.logger.lifecycle "Entities enhancing was skipped, because entity classes haven't been changed since the last build"
        }
    }

    protected ClassesInfo collectClasses(Project project, SourceSet sourceSet) {
        ClassesInfo classesInfo = new ClassesInfo()

        if (sourceSetName == TEST_SET_NAME) {
            collectEntitiesFromSources(project, project.sourceSets.findByName(MAIN_SET_NAME), classesInfo)
        }

        collectEntitiesFromSources(project, sourceSet, classesInfo)

        if (sourceSetName == TEST_SET_NAME && classesInfo.modulePaths.size() > 1) {
            //test sourceSet can have 0 or more Configurations. Production Configuration path should be removed in case of test Configuration presence
            classesInfo.modulePaths.remove(0);
        }

        project.logger.lifecycle("Project entities:\n    JPA: ${classesInfo.getJpaEntities()};\n    DTO: ${classesInfo.getDtoEntities()};\n" +
                "Project converters: ${classesInfo.getConverters()}.")

        project.jmix.entitiesEnhancing.jpaConverters.each {
            classesInfo.converters.add(it)
        }

        collectEntitiesFromClasspathes(project, sourceSet, classesInfo)

        project.logger.info("Found entities:\n    JPA: ${classesInfo.getJpaEntities()};\n    DTO: ${classesInfo.getDtoEntities()}.\n" +
                "Converters: ${classesInfo.getConverters()}")

        return classesInfo
    }

    protected void collectEntitiesFromSources(Project project, sourceSet, ClassesInfo classesInfo) {
        ClassPool classPool = createClassPool(project, sourceSet)

        sourceSet.allJava.getSrcDirs().each { File srcDir ->
            project.fileTree(srcDir).each { File file ->
                if (file.name.endsWith('.java') || file.name.endsWith('.kt')) {
                    String ext = file.name.endsWith('.java') ? '.java' : '.kt'
                    String pathStr = srcDir.toPath().relativize(file.toPath()).join('.')
                    String className = pathStr.substring(0, pathStr.length() - ext.length())

                    try {
                        CtClass ctClass = classPool.get(className)

                        examineClass(ctClass, classesInfo)

                        if (isModuleConfig(ctClass)) {
                            classesInfo.modulePaths.add(ctClass.getPackageName().replace('.', '/'));
                        }
                    } catch (NotFoundException e) {
                        project.logger.info "Cannot find $className in ${project} for enhancing: $e"
                    }
                }
            }
        }
    }

    protected void collectEntitiesFromClasspathes(Project project, sourceSet, ClassesInfo classesInfo) {
        ClassPool classPool = createClassPool(project, sourceSet)

        ClassesInfo compileClasses = collectEntitiesFromClasspath(project, sourceSet.compileClasspath, classPool)
        ClassesInfo runtimeClasses = collectEntitiesFromClasspath(project, sourceSet.runtimeClasspath, classPool)

        classesInfo.addClassesIntersection(runtimeClasses, compileClasses)
    }

    protected ClassesInfo collectEntitiesFromClasspath(Project project, FileCollection currentClasspath, ClassPool classPool) {
        ClassesInfo classesInfo = new ClassesInfo()

        def jars = currentClasspath.asList().findAll { it.name.endsWith('.jar') }
        jars.each { lib ->
            if (lib.exists()) {
                project.zipTree(lib).matching { include "**/*persistence.xml" }.each {
                    Node doc = new XmlParser().parse(it)
                    def docPu = doc.'persistence-unit'[0]
                    List<String> currentEntities = docPu.'class'.collect { it.text() }
                    String storeName = docPu.@name
                    classesInfo.classesByStores[storeName ?: MAIN_STORE_NAME].addAll(currentEntities)

                    String converters = docPu.'properties'.'*'.find { it.@name == CONVERTERS_LIST_PROPERTY }?.@value

                    converters?.split(';')?.each { classesInfo.converters.add(it) }

                    project.logger.debug("Found $it.name in $lib.name. Entities: $currentEntities.\n Converters: $converters")
                }
            }
        }

        def folders = currentClasspath.asList().findAll { it.isDirectory() }
        folders.each { File folder ->
            project.fileTree(folder).each {
                if (it.name.endsWith('.class')) {
                    String pathStr = folder.toPath().relativize(it.toPath()).join('.')
                    String className = pathStr.substring(0, pathStr.length() - '.class'.length())

                    try {
                        CtClass ctClass = classPool.get(className)
                        examineClass(ctClass, classesInfo)
                    } catch (NotFoundException e) {
                        project.logger.info "Cannot determine $className in classpath folder '${folder}': $e"
                    }
                }
            }
        }
        return classesInfo
    }

    protected void examineClass(CtClass ctClass, ClassesInfo classesInfo) {
        if (isJpaEntity(ctClass) || isJpaMappedSuperclass(ctClass) || isJpaEmbeddable(ctClass)) {
            classesInfo.classesByStores[findStoreName(ctClass) ?: MAIN_STORE_NAME].add(ctClass.getName())
        } else if (isJpaConverter(ctClass)) {
            classesInfo.converters.add(ctClass.getName())
        } else if (isJmixEntity(ctClass)) {
            classesInfo.nonMappedClasses[findStoreName(ctClass) ?: MAIN_STORE_NAME].add(ctClass.getName())
        }
    }

    protected void constructDescriptors(Project project, sourceSet, ClassesInfo classesInfo) {
        for (String storeName : classesInfo.allStores()) {
            for (String modulePath : classesInfo.modulePaths) {

                String storePrefix = storeName == MAIN_STORE_NAME ? "" : (storeName + '-')
                String enhancingWorkDir = "$project.buildDir/tmp/entitiesEnhancing/$sourceSetName/${storePrefix}persistence"

                String persistenceFileName = "$enhancingWorkDir/META-INF/persistence.xml"

                String ormRelativeFileName = "$modulePath/${storePrefix}orm.xml";
                String ormFileName = "$enhancingWorkDir/$ormRelativeFileName"

                //create persistence.xml in tmp work directory to be processed by orm-provider weaving (e.g. EclipseLink accepts '*/META-INF/persistence.xml' file path only)
                DescriptorGenerationUtils.constructPersistenceXml(
                        persistenceFileName,
                        storeName,
                        ormRelativeFileName,
                        classesInfo.getJpaEntitiesAndConverters(storeName),
                        classesInfo.getConverters())

                DescriptorGenerationUtils.constructOrmXml(
                        ormFileName,
                        classesInfo.getJpaEntitiesAndConverters(storeName),
                        createClassPool(project, sourceSet))

                //store all generated persistence/orm xml files in temporary resources dir, because output resources dir is not prepared yet
                project.copy {
                    from persistenceFileName, ormFileName
                    into "$project.buildDir/tmp/entitiesEnhancing/resources/$sourceSetName/$modulePath"
                    rename "persistence.xml", "${storePrefix}persistence.xml"
                }
            }
        }
    }

    static void copyGeneratedFiles(Project project, String sourceSetName) {
        project.copy {
            from "$project.buildDir/tmp/entitiesEnhancing/resources/$sourceSetName/"
            into "$project.buildDir/resources/$sourceSetName/"
        }
    }

    protected void runJmixEnhancing(Project project, sourceSet, ClassesInfo classesInfo) {
        if (!classesInfo.getAllEntities().isEmpty()) {
            project.logger.lifecycle "Running Jmix enhancer in $project for $sourceSet"

            String javaOutputDir = sourceSet.java.destinationDirectory.get().getAsFile().absolutePath

            for (EnhancingStep step : enhancingSteps()) {

                ClassPool classPool = createClassPool(project, sourceSet)

                step.classPool = classPool
                step.outputDir = javaOutputDir
                step.logger = project.logger

                for (className in classesInfo.getAllEntities()) {
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

    /**
     * Evaluates a checksum of enhanced entity classes and saves it to the file.
     * <p>
     * On the next project build, the checksum of entities classes from the "build" directory will be compared with the
     * checksum saved to the file to find out if entities have been changed since the last compilation.
     */
    protected void saveEntityClassesChecksumForNextBuild(Project project, sourceSet, ClassesInfo classesInfo) {
        def allEntityClassesChecksum = evaluateEntityClassesChecksum(sourceSet, classesInfo)
        Path checksumFilePath = getEntitiesChecksumFilePath(project)
        if (!Files.exists(checksumFilePath.getParent())) {
            Files.createDirectories(checksumFilePath.getParent())
        }
        Files.write(checksumFilePath, allEntityClassesChecksum.getBytes(StandardCharsets.UTF_8))
    }

    /**
     * Evaluates a single checksum for all entity classes (of the current project) extracted from the ClassesInfo.
     */
    protected String evaluateEntityClassesChecksum(sourceSet, ClassesInfo classesInfo) {
        StringBuilder sb = new StringBuilder()
        String javaOutputDir = sourceSet.java.destinationDirectory.get().getAsFile().absolutePath
        Collection<String> allEntities = classesInfo.getAllEntities()
        allEntities.sort()
        for (className in allEntities) {
            def classFileName = className.replace('.', '/') + '.class'
            def classFile = new File(javaOutputDir, classFileName)

            // skip files from dependencies, copy only classes from `javaOutputDir`
            if (classFile.exists()) {
                sb.append(checkSum(classFile))
            }
        }
        return checkSum(sb.toString().getBytes(StandardCharsets.UTF_8))
    }

    /**
     * Checks if entity classes in a project build directory and entity classes from last project build are not
     * the same. Method compares the checksum of entity classes saved during previous compilation with the checksum of
     * actual entity classes collection.
     */
    protected boolean entityClassesChangedSinceLastBuild(Project project, SourceSet sourceSet, ClassesInfo classesInfo) {
        Path entitiesChecksumFilePath = getEntitiesChecksumFilePath(project)
        if (!Files.exists(entitiesChecksumFilePath)) {
            //previous entity classes checksum was not saved
            return true
        }
        def prevChecksum = Files.readString(entitiesChecksumFilePath)
        def currentChecksum = evaluateEntityClassesChecksum(sourceSet, classesInfo)
        return prevChecksum != currentChecksum
    }

    protected String checkSum(File file) {
        byte[] data = Files.readAllBytes(file.toPath())
        return checkSum(data)
    }

    protected String checkSum(byte[] data) {
        byte[] hash = MessageDigest.getInstance("MD5").digest(data)
        return new BigInteger(1, hash).toString(16)
    }

    /**
     * Returns a path to a file where entity classes checksum will be stored to.
     */
    protected Path getEntitiesChecksumFilePath(Project project) {
        return Paths.get("$project.buildDir/tmp/entitiesEnhancing/entities.checksum")
    }

    static ClassPool createClassPool(Project project, sourceSet) {
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()

        for (File file in sourceSet.compileClasspath) {
            classPool.insertClassPath(file.getAbsolutePath())
        }

        classPool.insertClassPath(sourceSet.java.destinationDirectory.get().getAsFile().absolutePath)

        project.configurations.enhancing.files.each { File dep ->
            classPool.insertClassPath(dep.absolutePath)
        }

        return classPool
    }

    protected static PersistenceProviderEnhancing persistenceProviderEnhancing() {
        return new EclipselinkEnhancing()
    }

    protected static List<EnhancingStep> enhancingSteps() {
        Arrays.asList(
                new JmixEntityEnhancingStep(),
                new EntityEntryEnhancingStep(),
                new SettersEnhancingStep(),
                new TransientAnnotationEnhancingStep())
    }

    /**
     * Stores information about entities and converters
     */
    private class ClassesInfo {
        Map<String, Set<String>> classesByStores = [:].withDefault { _ -> new HashSet<String>() }
        Set<String> converters = [] as Set<String>
        Map<String, Set<String>> nonMappedClasses = [:].withDefault { _ -> new HashSet<String>() }

        List<String> modulePaths = []

        Collection<String> getJpaEntities() {
            return classesByStores.values().flatten()
        }

        Collection<String> getDtoEntities() {
            return nonMappedClasses.values().flatten()
        }

        /**
         * @return JPA and DTO entities
         */
        Collection<String> getAllEntities() {
            return getJpaEntities() + nonMappedClasses.values().flatten()
        }

        Set<String> getJpaEntitiesAndConverters(String store) {
            return (classesByStores[store] + converters).toSet()
        }

        /**
         * @return list of store names
         */
        Set<String> allStores() {
            return classesByStores.size() > 0 ? classesByStores.keySet() :
                    converters.size() > 0 ? ['main'] : new HashSet<String>()
        }

        /**
         *  Adds classes that exists in both {@code first} and {@code second} ClassesInfos
         */
        void addClassesIntersection(ClassesInfo first, ClassesInfo second) {

            for (String store : first.classesByStores.keySet()) {
                classesByStores[store].addAll(first.classesByStores[store].intersect(second.classesByStores[store]))
            }

            for (String store : first.nonMappedClasses.keySet()) {
                nonMappedClasses[store].addAll(first.nonMappedClasses[store].intersect(second.nonMappedClasses[store]))
            }

            converters.addAll(first.converters.intersect(second.converters))
        }

    }

}

package org.openstreetmap.josm.plugins.scripting.build

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@SuppressWarnings('unused')
abstract class GitDeployTask extends DefaultTask{
    @Input
    abstract Property<String> getDeployBranch()

    @Input
    abstract Property<String> getCurrentPluginVersion()

    @Input
    abstract Property<String> getForJosmVersion()

    GitDeployTask() {
        deployBranch.convention("deploy")
    }

    @TaskAction
    def deploy() {
        if (!currentPluginVersion.isPresent()) {
            throw new IllegalStateException("mandatory property 'currentPluginVersion' not set")
        }
        if (!forJosmVersion.isPresent()) {
            throw new IllegalStateException("mandatory property 'forJosmVersion' not set")
        }

        //def jar = "$projectDir/dist/${archivesBaseName}.jar"
        def jar = "${project.projectDir.absolutePath}/dist/${project.property("archivesBaseName")}.jar"
        Git.ensureOnBranch(deployBranch.get())
        Git.add(jar)
        Git.commit(jar, "commited plugin build ${currentPluginVersion.get()}")
        def tag = "for-josm-${forJosmVersion.get()}"
        Git.tagHead(tag)
        Git.push("origin", deployBranch.get())
        Git.pushTags()
    }
}

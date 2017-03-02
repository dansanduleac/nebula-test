/*
 * Copyright 2016-2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TestName

abstract class IntegrationTestKitSpec extends BaseIntegrationSpec {
    boolean keepFiles = false
    File buildFile
    File settingsFile

    def setup() {
        settingsFile = new File(projectDir, "settings.gradle")
        buildFile = new File(projectDir, "build.gradle")

        configurePluginClasspath()
    }

    def configurePluginClasspath() {
        def pluginClasspathResource = this.class.classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        def pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace("\\", "\\\\") } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")
        buildFile << """\
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
        """.stripIndent()
    }

    def cleanup() {
        if (!keepFiles) {
            projectDir.deleteDir()
        }
    }

    void addSubproject(String name, String buildGradle) {
        def subdir = new File(projectDir, name)
        subdir.mkdirs()

        settingsFile << "include \"${name}\""

        new File(subdir, "build.gradle").text = buildGradle
    }

    BuildResult runTasks(String... tasks) {
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(*tasks.plus("-i"))
                .forwardOutput()
                .build()
    }
}

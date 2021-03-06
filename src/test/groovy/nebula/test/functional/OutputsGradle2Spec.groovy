package nebula.test.functional

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/nebula-plugins/nebula-test/issues/29")
class OutputsGradle2Spec extends Specification {

    private static final boolean FORK_MODE = true

    @Rule TemporaryFolder tmp

    def "println included in standardOutput in fork mode"() {
        given:
        GradleRunner runner = GradleRunnerFactory.createTooling(FORK_MODE)
        tmp.newFile("build.gradle") << """
            apply plugin: ${SomePlugin.name}
        """

        when:
        ExecutionResult result = runner.run(tmp.root, ["print"])

        then:
        result.standardOutput.contains("Printed (stdout)")
    }

    def "err.println included in standardError or standardOutput in fork mode"() {
        given:
        GradleRunner runner = GradleRunnerFactory.createTooling(FORK_MODE)
        tmp.newFile("build.gradle") << """
            apply plugin: ${SomePlugin.name}
        """

        when:
        ExecutionResult result = runner.run(tmp.root, ["print"])

        then:
        def expectedMessage = "Printed (stderr)"
        //Gradle 4.7 started to print error log messages into standard output
        //we run build with version lower then 4.7 as well higher so we check both places
        result.standardError.contains(expectedMessage) || result.standardOutput.contains(expectedMessage)

    }

    def "stdout redirected to WARN included in standardOutput in fork mode"() {
        given:
        GradleRunner runner = GradleRunnerFactory.createTooling(FORK_MODE)
        tmp.newFile("build.gradle") << """
            logging.captureStandardOutput LogLevel.WARN
            apply plugin: ${SomePlugin.name}
        """

        when:
        ExecutionResult result = runner.run(tmp.root, ["print"])

        then:
        result.standardOutput.contains("Printed (stdout)")
    }

    def "stdout redirected to ignored logging level not included in standardOutput in fork mode"() {
        given:
        GradleRunner runner = GradleRunnerFactory.createTooling(FORK_MODE)
        tmp.newFile("build.gradle") << """
            logging.captureStandardOutput LogLevel.TRACE
            apply plugin: ${SomePlugin.name}
        """

        when:
        ExecutionResult result = runner.run(tmp.root, ["print"])

        then:
        !result.standardOutput.contains("Printed (stdout)")
    }
}

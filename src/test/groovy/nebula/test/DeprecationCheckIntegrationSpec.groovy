package nebula.test

import spock.lang.IgnoreIf

class DeprecationCheckIntegrationSpec extends IntegrationSpec {
    @IgnoreIf({ jvm.isJava9Compatible() })
    def 'deprecation warnings cause test to fail'() {
        given:
        buildFile << """
            apply plugin: 'java'
            
            tasks.jar.deleteAllActions()
        """

        gradleVersion = '4.7'

        when:
        runTasks()

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Deprecation warnings were found (Set the ignoreDeprecations system property during the test to ignore)')
    }
}

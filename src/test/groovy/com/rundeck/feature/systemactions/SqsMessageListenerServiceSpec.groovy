package com.rundeck.feature.systemactions

import software.amazon.awssdk.regions.Region
import spock.lang.Requires
import spock.lang.Specification

class SqsMessageListenerServiceSpec extends Specification {

    @Requires({System.getProperty("test.sqs").equals("yes")})
    def "service test"() {
        setup:
        SystemActionsFeatureConfig config = new SystemActionsFeatureConfig()
        config.awsRegion = Region.US_WEST_2
        config.awsSqsQueue = "hackweek-test"
        config.messagePollTimeoutSeconds = 2
        SqsMessageListenerService svc = new SqsMessageListenerService(config)

        when:
        svc.start()
        Thread.sleep(5000)
        svc.stop()
        println svc.receivedMessages

        then:
        noExceptionThrown()
        !svc.receivedMessages.isEmpty()

    }
}

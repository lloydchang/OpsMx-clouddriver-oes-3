/*
 * Copyright 2018 Netflix, Inc.
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

package com.netflix.spinnaker.clouddriver.aws.lifecycle

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.clouddriver.aws.provider.AwsCleanupProvider
import com.netflix.spinnaker.clouddriver.aws.provider.AwsProvider
import com.netflix.spinnaker.clouddriver.aws.security.AmazonClientProvider
import com.netflix.spinnaker.clouddriver.aws.security.AmazonCredentials
import com.netflix.spinnaker.clouddriver.aws.security.NetflixAmazonCredentials
import com.netflix.spinnaker.credentials.CredentialsRepository
import com.netflix.spinnaker.clouddriver.tags.EntityTagger
import spock.lang.Specification
import spock.lang.Subject

/*
 * Copyright 2017 Netflix, Inc.
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

class LaunchFailureNotificationAgentProviderSpec extends Specification {
  def objectMapper = Mock(ObjectMapper)
  def amazonClientProvider = Mock(AmazonClientProvider)
  def credentialsRepository = Mock(CredentialsRepository)
  def serverGroupTagger = Mock(EntityTagger)

  def launchFailureConfigurationProperties = new LaunchFailureConfigurationProperties(
    "mgmt",
    "arn:aws:sns:{{region}}:{{accountId}}:topicName",
    "arn:aws:sqs:{{region}}:{{accountId}}:queueName",
    -1,
    -1,
    -1
  )

  @Subject
  def agentProvider = new LaunchFailureNotificationAgentProvider(
    objectMapper,
    amazonClientProvider,
    credentialsRepository,
    launchFailureConfigurationProperties,
    serverGroupTagger
  )

  void "should support AwsProvider"() {
    expect:
    agentProvider.supports(AwsProvider.name)
    !agentProvider.supports(AwsCleanupProvider.name)
  }

  void "should return an agent per region in specified account"() {
    given:
    def regions = ["us-west-1", "us-west-2", "us-east-1", "eu-west-1"]
    def mgmtCredentials = Mock(NetflixAmazonCredentials) {
      getRegions() >> {
        return regions.collect { new AmazonCredentials.AWSRegion(it, [])}
      }
      getAccountId() >> { return "100" }
      getName() >> { return "mgmt" }
    }

    when:
    def agents = agentProvider.agents(mgmtCredentials)

    then:
    regions.each { String region ->
      assert agents.find { it.agentType == "mgmt/${region}/LaunchFailureNotificationAgent".toString() } != null
    }
  }
}

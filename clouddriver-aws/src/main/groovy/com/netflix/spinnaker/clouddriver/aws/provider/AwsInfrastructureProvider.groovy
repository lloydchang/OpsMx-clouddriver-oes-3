/*
 * Copyright 2015 Netflix, Inc.
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

package com.netflix.spinnaker.clouddriver.aws.provider

import com.fasterxml.jackson.core.type.TypeReference
import com.netflix.spinnaker.clouddriver.aws.cache.Keys
import com.netflix.spinnaker.clouddriver.cache.KeyParser
import com.netflix.spinnaker.clouddriver.cache.SearchableProvider
import com.netflix.spinnaker.clouddriver.security.BaseProvider

import static com.netflix.spinnaker.clouddriver.aws.cache.Keys.Namespace.SECURITY_GROUPS

class AwsInfrastructureProvider extends BaseProvider implements SearchableProvider {
  public static final TypeReference<Map<String, Object>> ATTRIBUTES = new TypeReference<Map<String, Object>>() {}

  public final String PROVIDER_NAME = AwsInfrastructureProvider.name

  private final KeyParser keyParser = new Keys()

  @Override
  String getProviderName() {
    return PROVIDER_NAME
  }

  final Set<String> defaultCaches = [SECURITY_GROUPS.ns].asImmutable()

  final Map<String, String> urlMappingTemplates = [
    (SECURITY_GROUPS.ns): '/securityGroups/$account/$provider/$name?region=$region'
  ]

  final Map<SearchableResource, SearchResultHydrator> searchResultHydrators = Collections.emptyMap()

  @Override
  Map<String, String> parseKey(String key) {
    return Keys.parse(key)
  }

  @Override
  Optional<KeyParser> getKeyParser() {
    return Optional.of(keyParser)
  }
}

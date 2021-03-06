/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.circuitbreaker.resilience4j;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ryan Baxter
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux",
		"io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator" })
@ConditionalOnProperty(name = "spring.cloud.circuitbreaker.resilience4j.enabled",
		matchIfMissing = true)
public class ReactiveResilience4JAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ReactiveCircuitBreakerFactory.class)
	public ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory() {
		return new ReactiveResilience4JCircuitBreakerFactory();
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(
			name = { "reactor.core.publisher.Mono", "reactor.core.publisher.Flux" })
	@ConditionalOnMissingClass({
			"io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics" })
	public static class ReactiveResilience4JCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<ReactiveResilience4JCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired(required = false)
		private ReactiveResilience4JCircuitBreakerFactory factory;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = { "reactor.core.publisher.Mono",
			"reactor.core.publisher.Flux",
			"io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics" })
	@ConditionalOnBean({ MeterRegistry.class })
	public static class MicrometerReactiveResilience4JCustomizerConfiguration {

		@Autowired(required = false)
		private List<Customizer<ReactiveResilience4JCircuitBreakerFactory>> customizers = new ArrayList<>();

		@Autowired(required = false)
		private ReactiveResilience4JCircuitBreakerFactory factory;

		@Autowired
		private MeterRegistry meterRegistry;

		@PostConstruct
		public void init() {
			customizers.forEach(customizer -> customizer.customize(factory));
			if (factory != null) {
				TaggedCircuitBreakerMetrics
						.ofCircuitBreakerRegistry(factory.getCircuitBreakerRegistry())
						.bindTo(meterRegistry);
			}
		}

	}

}

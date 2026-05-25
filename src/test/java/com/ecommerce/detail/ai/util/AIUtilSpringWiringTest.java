package com.ecommerce.detail.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AIUtilSpringWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withPropertyValues(
                    "ai.relay.enabled=false",
                    "ai.relay.base-url=",
                    "ai.relay.api-key=",
                    "ai.relay.model=",
                    "ai.relay.temperature=0.7",
                    "ai.relay.max-tokens=4000",
                    "ai.relay.timeout-seconds=120"
            );

    @Test
    void aiUtilCanBeCreatedBySpringWithConfiguredConstructor() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(AIUtil.class));
    }

    @Configuration
    @ComponentScan(basePackageClasses = AIUtil.class)
    static class TestConfig {
    }
}

package com.alibaba.csp.sentinel.dashboard.config;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.Client;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliyunlogProperties.class)
public class AliyunlogConfiguration {
    private final AliyunlogProperties properties;

    public AliyunlogConfiguration(AliyunlogProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Producer aliyunlogProducer() {
        if(StringUtils.isBlank(properties.getProject())){
            return null;
        }
        ProjectConfig projectConfig = new ProjectConfig(properties.getProject(), properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret(), null, "sentinel");
        ProducerConfig producerConfig = new ProducerConfig();
        Producer producer = new LogProducer(producerConfig);
        producer.putProjectConfig(projectConfig);
        return producer;
    }

    @Bean
    public Client aliyunlogClient() {
        if(StringUtils.isBlank(properties.getEndpoint())){
            return null;
        }
        return new Client(properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
    }

}

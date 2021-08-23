package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aliyunlog")
public class AliyunlogProperties {

    @Value("${aliyunlog.project}")
    private String project;

    @Value("${aliyunlog.endpoint}")
    private String endpoint;

    @Value("${aliyunlog.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyunlog.accessKeySecret}")
    private String accessKeySecret;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
}

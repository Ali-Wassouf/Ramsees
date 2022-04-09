package com.tyche.ramsees.api.dto.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.scheduler")
@Setter
@Getter
public class JobConfigProps {

    private int frequency;
}

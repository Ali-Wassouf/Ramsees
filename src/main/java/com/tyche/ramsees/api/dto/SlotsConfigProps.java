package com.tyche.ramsees.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.average-slots-splits")
@Setter
@Getter
public class SlotsConfigProps {

    private Integer fast;
    private Integer slow;
}

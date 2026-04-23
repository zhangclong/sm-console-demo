package com.uh.system.service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

public interface ApplicationConfigService {
    @PostConstruct
    void init();

    /**
     *  是否开启开发模式
     * @param open
     */
    void switchDevelopmentMode(Boolean open);

}

package com.uh.system.service;

import com.uh.common.core.tenant.TenantContext;
import com.uh.system.domain.SysOperLog;
import com.uh.system.service.config.TestAppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
class SysOperLogServiceTest {

    private static final Long TEST_TENANT_ID = 1L;

    @Autowired
    private SysOperLogService sysOperLogService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void insertAndQuery_shouldTranslateMenuCodeTitle() {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("system:user");
        operLog.setBusinessType("1");
        operLog.setMethod("test.method()");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType(1);
        operLog.setOperName("admin");
        operLog.setOperUrl("/test/menu-code");
        operLog.setOperIp("127.0.0.1");
        operLog.setOperLocation("local");
        operLog.setStatus(0);
        sysOperLogService.insertOperlog(operLog);

        List<SysOperLog> logs = sysOperLogService.selectOperLogList(new SysOperLog());
        Optional<SysOperLog> matched = logs.stream()
                .filter(item -> "/test/menu-code".equals(item.getOperUrl()))
                .findFirst();

        assertFalse(matched.isEmpty(), "应能查询到刚插入的操作日志");
        assertEquals("基础信息.用户管理", matched.get().getTitleName());
    }

    @Test
    void insertAndQuery_shouldFallbackToOriginalTitleWhenMenuCodeMissing() {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("LegacyModuleName");
        operLog.setBusinessType("1");
        operLog.setMethod("test.method()");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType(1);
        operLog.setOperName("admin");
        operLog.setOperUrl("/test/legacy-title");
        operLog.setOperIp("127.0.0.1");
        operLog.setOperLocation("local");
        operLog.setStatus(0);
        sysOperLogService.insertOperlog(operLog);

        List<SysOperLog> logs = sysOperLogService.selectOperLogList(new SysOperLog());
        Optional<SysOperLog> matched = logs.stream()
                .filter(item -> "/test/legacy-title".equals(item.getOperUrl()))
                .findFirst();

        assertFalse(matched.isEmpty(), "应能查询到刚插入的操作日志");
        assertEquals("LegacyModuleName", matched.get().getTitleName());
    }
}

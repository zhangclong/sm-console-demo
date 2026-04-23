package com.uh.system.service.impl;

import com.uh.common.constant.HttpStatus;
import com.uh.common.exception.ServiceException;
import com.uh.system.domain.SysTenant;
import com.uh.system.mapper.SysTenantMapper;
import com.uh.system.service.SysTenantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租户会话管理服务实现
 *
 * @author Zhang Chenlong
 */
@Service
public class SysTenantServiceImpl implements SysTenantService
{
    @Resource
    private SysTenantMapper tenantMapper;

    @Override
    public List<SysTenant> selectTenantsByUserId(Long userId)
    {
        return tenantMapper.selectTenantsByUserId(userId);
    }

    @Override
    public List<Long> selectTenantIdsByUserId(Long userId)
    {
        return tenantMapper.selectTenantIdsByUserId(userId);
    }

    @Override
    public void validateUserTenant(Long userId, Long tenantId)
    {
        int count = tenantMapper.countUserTenant(userId, tenantId);
        if (count == 0)
        {
            throw new ServiceException(HttpStatus.FORBIDDEN, "tenant.access.denied");
        }
    }
}

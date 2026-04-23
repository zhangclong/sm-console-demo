package com.uh.framework.aspectj;

import com.uh.common.constant.HttpStatus;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.exception.ServiceException;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.uh.common.annotation.PrePermission;
import static com.uh.common.constant.Constants.ALL_PERMISSION;

import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    /**
     * 拦截具有@PrePermission定义的方法，在方法即将执行时拦截(@Before)，如果具有权限：hasPermi(PrePermission.value)就继续执行该方法;
     * 如果没有权限就返回：AjaxResult.error(HttpStatus.FORBIDDEN, "Permission denied");
     */
    @Before("@annotation(prePermission)")
    public void checkPermission(JoinPoint joinPoint, PrePermission prePermission) {

        //打印出被拦截的类名和方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        //System.out.println("PermissionAspect Intercepted: " + className + "." + methodName);

        String permission = prePermission.value();
        if (!hasPermi(permission)) {
            // 如果没有权限，代替原函数返回 AjaxResult.error( "Permission denied")
            throw new ServiceException(HttpStatus.FORBIDDEN, null, "no.permission", prePermission.value());
        }
    }

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission)
    {
        if (StringUtils.isEmpty(permission))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        {
            return false;
        }

        return hasPermissions(loginUser.getPermissions(), permission);
    }


    /**
     * 判断是否包含权限
     *
     * @param permissions 权限列表
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    private boolean hasPermissions(Set<String> permissions, String permission)
    {
        return permissions.contains(ALL_PERMISSION) || permissions.contains(StringUtils.trim(permission));
    }
}

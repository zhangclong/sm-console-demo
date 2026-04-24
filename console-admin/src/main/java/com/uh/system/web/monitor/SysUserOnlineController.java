package com.uh.system.web.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.uh.framework.cache.ObjectCache;
import com.uh.system.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import com.uh.common.annotation.PrePermission;
import org.springframework.web.bind.annotation.*;
import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.core.page.TableDataInfo;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.StringUtils;
import com.uh.system.domain.SysUserOnline;
import com.uh.system.service.SysUserOnlineService;

import javax.annotation.Resource;

/**
 * 在线用户监控
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private SysUserOnlineService userOnlineService;

    @Resource
    private TokenService tokenService;

    @Autowired
    private ObjectCache cache;

    @PrePermission("monitor:online:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "ipaddr", required = false) String ipaddr,@RequestParam(value = "userName", required = false) String userName)
    {
        List<LoginUser> users = tokenService.getLoginUsers();
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (LoginUser user : users) {
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername()))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                }
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                if (StringUtils.equals(ipaddr, user.getIpaddr()))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                }
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                if (StringUtils.equals(userName, user.getUsername()))
                {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                }
            }
            else
            {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PrePermission("monitor:online:forceLogout")
    @Log(title = "monitor:online", businessType = BusinessTypeConstants.FORCE)
    @GetMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable("tokenId") String tokenId)
    {
        tokenService.delLoginUser(tokenId);
        return AjaxResult.success();
    }
}

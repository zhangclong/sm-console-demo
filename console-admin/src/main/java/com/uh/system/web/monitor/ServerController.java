package com.uh.system.web.monitor;

import com.uh.common.annotation.PrePermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.domain.metrics.Server;

/**
 * 服务器监控
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController
{
    @PrePermission("monitor:server:list")
    @GetMapping()
    public AjaxResult getInfo() throws Exception
    {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }
}

package com.uh.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.domain.SysDictData;
import com.uh.system.manage.DictRegistry;

/**
 * 数据字典信息
 * <p>
 * 字典已迁移为枚举驱动，仅保留按类型查询接口，增删改查操作已移除。
 * </p>
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController
{
    @Autowired
    private DictRegistry dictRegistry;

    /**
     * 根据字典类型查询字典数据信息（枚举驱动，不再查数据库）
     */
    @GetMapping(value = "/type/{dictType}")
    public AjaxResult dictType(@PathVariable("dictType") String dictType)
    {
        return AjaxResult.success(dictRegistry.getDictDataList(dictType));
    }
}

package com.uh.system.manage;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;
import com.uh.system.domain.SysDictData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典注册中心
 * <p>
 * Spring 容器启动时，扫描 {@code com.uh} 包下所有带 {@link DictType} 注解且实现了
 * {@link SystemDictEnum} 接口的枚举，将枚举常量转换为 {@link SysDictData} 列表并缓存在内存中。
 * 后续字典查询接口直接读内存，无需访问数据库。
 * </p>
 */
@Component
public class DictRegistry {

    private static final Logger log = LoggerFactory.getLogger(DictRegistry.class);

    private final Map<String, List<SysDictData>> dictCache = new ConcurrentHashMap<>();

    /**
     * Spring 容器初始化完成后，扫描并装载所有枚举字典。
     * <p>
     * 扫描根包为 {@code com.uh.system.dict}；若业务枚举放置于其它包下，
     * 可在此处追加 {@code scanner.findCandidateComponents("com.uh.console.dict")} 等调用。
     * </p>
     */
    @PostConstruct
    public void init() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DictType.class));

        // Scan all dedicated dict packages
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.uh.system.dict");
        candidates.addAll(scanner.findCandidateComponents("com.uh.console.dict"));
        for (BeanDefinition candidate : candidates) {
            String className = candidate.getBeanClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if (!clazz.isEnum() || !SystemDictEnum.class.isAssignableFrom(clazz)) {
                    continue;
                }
                DictType dictTypeAnnotation = clazz.getAnnotation(DictType.class);
                String dictType = dictTypeAnnotation.value();
                Object[] constants = clazz.getEnumConstants();
                List<SysDictData> dictDataList = new ArrayList<>();
                long dictCode = 1;
                for (Object constant : constants) {
                    SystemDictEnum dictEnum = (SystemDictEnum) constant;
                    SysDictData dictData = new SysDictData();
                    dictData.setDictCode(dictCode++);
                    dictData.setDictLabel(dictEnum.getLabel());
                    dictData.setDictValue(dictEnum.getValue());
                    dictData.setDictType(dictType);
                    dictData.setListClass(dictEnum.getListClass());
                    dictData.setIsDefault("N");
                    dictDataList.add(dictData);
                }
                dictCache.put(dictType, Collections.unmodifiableList(dictDataList));
                log.debug("DictRegistry loaded: {} ({} entries)", dictType, dictDataList.size());
            } catch (ClassNotFoundException e) {
                log.error("DictRegistry: cannot load dict enum class {}", className, e);
            }
        }
        log.info("DictRegistry initialized with {} dict types", dictCache.size());
    }

    /**
     * 根据字典类型获取字典数据列表；若注册中心中无该类型，返回空列表。
     *
     * @param dictType 字典类型键名，如 {@code "sys_user_sex"}
     * @return 字典数据列表（不可变），不存在时返回空列表
     */
    public List<SysDictData> getDictDataList(String dictType) {
        List<SysDictData> result = dictCache.get(dictType);
        return result != null ? result : Collections.emptyList();
    }

    /**
     * 获取注册中心中全部已注册的字典类型键名列表。
     *
     * @return 字典类型列表
     */
    public List<String> getDictTypes() {
        return new ArrayList<>(dictCache.keySet());
    }
}

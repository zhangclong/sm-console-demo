package com.uh.system.domain;

import java.util.*;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.uh.common.annotation.Excel;
import com.uh.common.annotation.Excel.ColumnType;
import com.uh.common.core.domain.BaseEntity;

import static com.uh.util.StringUtils.isNotEmpty;

/**
 * 角色表 sys_role
 *
 * @author XiaoZhangTongZhi
 */
public class SysRole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    private Long roleId;

    /** 角色名称 */
    @Excel(name = "角色名称")
    private String roleName;

    /** 角色权限 */
    @Excel(name = "角色权限")
    private String roleKey;

    /** 角色排序 */
    @Excel(name = "角色排序")
    private String roleSort;

    /** 菜单树选择项是否父子联动（ 0：不联动 1：父子联动） */
    private boolean menuCheckStrictly;

    /** Kubernetes 名称空间, 以逗号（,）间隔的多个名称空间 */
    private String namespaces;

    /** Kubernetes 名称空间列表 */
    private List<String> namespacesList = new ArrayList<>(); // 名称空间列表

    /** 角色状态（0正常 1停用） */
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（ 0 代表存在 2 代表删除 ） */
    private String delFlag;

    /** 用户是否存在此角色标识 默认不存在 */
    private boolean flag = false;

    /** 菜单编码组 */
    private String[] menuCodes;

    /** 角色菜单权限 */
    private Set<String> permissions;

    public SysRole() { }

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    @NotBlank(message = "显示顺序不能为空")
    public String getRoleSort()
    {
        return roleSort;
    }

    public void setRoleSort(String roleSort)
    {
        this.roleSort = roleSort;
    }

    public boolean isMenuCheckStrictly()
    {
        return menuCheckStrictly;
    }

    public void setMenuCheckStrictly(boolean menuCheckStrictly)
    {
        this.menuCheckStrictly = menuCheckStrictly;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public boolean isFlag()
    {
        return flag;
    }

    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    public String[] getMenuCodes()
    {
        return menuCodes;
    }

    public void setMenuCodes(String[] menuCodes)
    {
        this.menuCodes = menuCodes;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public String getNamespaces() {
        return namespaces;
    }

    public List<String> getNamespacesList() {
        return namespacesList;
    }

    public void setNamespacesList(List<String> namespacesList) {
        if (namespacesList != null && !namespacesList.isEmpty()) {
            // 去重并保留顺序，去除 null/空并 trim
            this.namespacesList = namespacesList.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());
            // 规范化 namespaces 字符串（去重后的）
            this.namespaces = String.join(",", this.namespacesList);
        }
        else {
            this.namespacesList = new ArrayList<>();
            this.namespaces = "";
        }
    }

    public void setNamespaces(String namespaces) {
        this.namespaces = namespaces;
        if(isNotEmpty(namespaces)){
            this.namespacesList = Arrays.asList(namespaces.split(","));
        }
        else {
            this.namespacesList = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("roleName", getRoleName())
            .append("roleKey", getRoleKey())
            .append("roleSort", getRoleSort())
            .append("menuCheckStrictly", isMenuCheckStrictly())
            .append("namespaces", getNamespaces())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

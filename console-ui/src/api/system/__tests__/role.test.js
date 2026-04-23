/**
 * system/role.js API 层测试。
 * 验证角色管理（CRUD、权限分配）等 API 函数的 URL、HTTP 方法、请求参数。
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

// ---- Mock request 模块 ----
vi.mock('@/utils/request', () => ({
  default: vi.fn(),
}));

import request from '@/utils/request';
import {
  listRole,
  getRole,
  addRole,
  updateRole,
  delRole,
  changeRoleStatus,
  dataScope,
  allocatedUserList,
  unallocatedUserList,
  authUserCancel,
  authUserCancelAll,
  authUserSelectAll,
  exportRole,
  deptTreeSelect,
} from '@/api/system/role';

describe('Role API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    request.mockResolvedValue({ code: 200, data: {} });
  });

  // ---- 角色列表 ----

  describe('listRole()', () => {
    it('should call /system/role/list with GET method', () => {
      listRole({ pageNum: 1 });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/list', method: 'get' })
      );
    });

    it('should pass query params', () => {
      const query = { pageNum: 1, roleName: 'admin' };
      listRole(query);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ params: query })
      );
    });
  });

  // ---- 角色详情 ----

  describe('getRole()', () => {
    it('should call /system/role/{id} with GET method', () => {
      getRole(1);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/1', method: 'get' })
      );
    });
  });

  // ---- 新增角色 ----

  describe('addRole()', () => {
    it('should call /system/role with POST method', () => {
      const role = { roleName: 'newRole', roleKey: 'new', roleSort: '10' };
      addRole(role);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role', method: 'post', data: role })
      );
    });
  });

  // ---- 修改角色 ----

  describe('updateRole()', () => {
    it('should call /system/role/edit with POST method', () => {
      const role = { roleId: 1, roleName: 'updated' };
      updateRole(role);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/edit', method: 'post', data: role })
      );
    });
  });

  // ---- 删除角色 ----

  describe('delRole()', () => {
    it('should call /system/role/delete/{id} with GET method', () => {
      delRole(2);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/delete/2', method: 'get' })
      );
    });
  });

  // ---- 状态变更 ----

  describe('changeRoleStatus()', () => {
    it('should call /system/role/changeStatus with POST method', () => {
      changeRoleStatus(1, '1');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/changeStatus', method: 'post' })
      );
    });

    it('should include roleId and status in data', () => {
      changeRoleStatus(3, '0');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ roleId: 3, status: '0' }),
        })
      );
    });
  });

  // ---- 数据权限 ----

  describe('dataScope()', () => {
    it('should call /system/role/dataScope with POST method', () => {
      dataScope({ roleId: 1, dataScope: '1' });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/dataScope', method: 'post' })
      );
    });
  });

  // ---- 已授权用户列表 ----

  describe('allocatedUserList()', () => {
    it('should call /system/role/authUser/allocatedList with GET', () => {
      allocatedUserList({ roleId: 1 });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: '/system/role/authUser/allocatedList',
          method: 'get',
        })
      );
    });
  });

  // ---- 未授权用户列表 ----

  describe('unallocatedUserList()', () => {
    it('should call /system/role/authUser/unallocatedList with GET', () => {
      unallocatedUserList({ roleId: 1 });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: '/system/role/authUser/unallocatedList',
          method: 'get',
        })
      );
    });
  });

  // ---- 取消用户授权 ----

  describe('authUserCancel()', () => {
    it('should call /system/role/authUser/cancel with POST method', () => {
      authUserCancel({ userId: 1, roleId: 2 });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/authUser/cancel', method: 'post' })
      );
    });
  });

  describe('authUserCancelAll()', () => {
    it('should call /system/role/authUser/cancelAll with POST method', () => {
      authUserCancelAll({ roleId: 1, userIds: '1,2' });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/authUser/cancelAll', method: 'post' })
      );
    });
  });

  // ---- 授权用户 ----

  describe('authUserSelectAll()', () => {
    it('should call /system/role/authUser/selectAll with POST method', () => {
      authUserSelectAll({ roleId: 1, userIds: '3,4' });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/authUser/selectAll', method: 'post' })
      );
    });
  });

  // ---- 导出角色 ----

  describe('exportRole()', () => {
    it('should call /system/role/export with GET method', () => {
      exportRole({});
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/export', method: 'get' })
      );
    });
  });

  // ---- 部门树 ----

  describe('deptTreeSelect()', () => {
    it('should call /system/role/deptTree/{roleId} with GET', () => {
      deptTreeSelect(1);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/role/deptTree/1', method: 'get' })
      );
    });
  });
});

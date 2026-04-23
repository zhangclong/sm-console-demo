/**
 * system/user.js API 层测试。
 * 验证用户管理（CRUD）、个人信息（Profile）等 API 函数的 URL、HTTP 方法、请求参数。
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

// ---- Mock request 模块 ----
vi.mock('@/utils/request', () => ({
  default: vi.fn(),
}));

// ---- Mock ruoyi 工具（praseStrEmpty） ----
vi.mock('@/utils/ruoyi', () => ({
  praseStrEmpty: vi.fn((v) => (v === null || v === undefined ? '' : String(v))),
}));

// ---- Mock AES 加密工具 ----
vi.mock('@/utils/aesutils.js', () => ({
  default: {
    encrypt: vi.fn((s) => `encrypted_${s}`),
  },
}));

import request from '@/utils/request';
import {
  listUser,
  getUser,
  addUser,
  updateUser,
  delUser,
  resetUserPwd,
  changeUserStatus,
  getUserProfile,
  updateUserProfile,
  updateUserPwd,
  uploadAvatar,
  exportUser,
  unlockUser,
} from '@/api/system/user';

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    request.mockResolvedValue({ code: 200, data: {} });
  });

  // ---- 用户列表 ----

  describe('listUser()', () => {
    it('should call /system/user/list with GET method', () => {
      listUser({ pageNum: 1, pageSize: 10 });
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/list', method: 'get' })
      );
    });

    it('should pass query params', () => {
      const query = { pageNum: 1, userName: 'admin' };
      listUser(query);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ params: query })
      );
    });
  });

  // ---- 用户详情 ----

  describe('getUser()', () => {
    it('should call /system/user/{id} with GET method', () => {
      getUser(42);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/42', method: 'get' })
      );
    });

    it('should handle empty userId (praseStrEmpty returns empty string)', () => {
      getUser(null);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/' })
      );
    });
  });

  // ---- 新增用户 ----

  describe('addUser()', () => {
    it('should call /system/user with POST method', () => {
      const user = { userName: 'newuser', password: 'pass' };
      addUser(user);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user', method: 'post', data: user })
      );
    });
  });

  // ---- 修改用户 ----

  describe('updateUser()', () => {
    it('should call /system/user/edit with POST method', () => {
      const user = { userId: 1, nickName: '管理员' };
      updateUser(user);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/edit', method: 'post', data: user })
      );
    });
  });

  // ---- 删除用户 ----

  describe('delUser()', () => {
    it('should call /system/user/delete/{id} with GET method', () => {
      delUser(5);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/delete/5', method: 'get' })
      );
    });
  });

  // ---- 密码重置 ----

  describe('resetUserPwd()', () => {
    it('should call /system/user/resetPwd with POST method', () => {
      resetUserPwd(1, 'newPass');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/resetPwd', method: 'post' })
      );
    });

    it('should encrypt the new password', () => {
      resetUserPwd(1, 'newPass');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ password: 'encrypted_newPass' }),
        })
      );
    });
  });

  // ---- 状态变更 ----

  describe('changeUserStatus()', () => {
    it('should call /system/user/changeStatus with POST method', () => {
      changeUserStatus(1, '1');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/changeStatus', method: 'post' })
      );
    });

    it('should include userId and status in data', () => {
      changeUserStatus(3, '0');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ userId: 3, status: '0' }),
        })
      );
    });
  });

  // ---- 解锁用户 ----

  describe('unlockUser()', () => {
    it('should call /system/user/unlock/{id} with GET method', () => {
      unlockUser(2);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/unlock/2', method: 'get' })
      );
    });
  });

  // ---- 个人信息（Profile）----

  describe('getUserProfile()', () => {
    it('should call /system/user/profile with GET method', () => {
      getUserProfile();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/profile', method: 'get' })
      );
    });
  });

  describe('updateUserProfile()', () => {
    it('should call /system/user/profile/edit with POST method', () => {
      const profileData = { nickName: 'New Name', email: 'new@email.com' };
      updateUserProfile(profileData);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/profile/edit', method: 'post', data: profileData })
      );
    });
  });

  describe('updateUserPwd()', () => {
    it('should call /system/user/profile/updatePwd with POST method', () => {
      updateUserPwd('oldPass', 'newPass');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/profile/updatePwd', method: 'post' })
      );
    });

    it('should include old and new passwords in params', () => {
      updateUserPwd('old', 'new');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          params: expect.objectContaining({ oldPassword: 'old', newPassword: 'new' }),
        })
      );
    });
  });

  describe('uploadAvatar()', () => {
    it('should call /system/user/profile/avatar with POST method', () => {
      const formData = new FormData();
      uploadAvatar(formData);
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/profile/avatar', method: 'post' })
      );
    });
  });

  // ---- 导出用户 ----

  describe('exportUser()', () => {
    it('should call /system/user/export with GET method', () => {
      exportUser({});
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/system/user/export', method: 'get' })
      );
    });
  });
});

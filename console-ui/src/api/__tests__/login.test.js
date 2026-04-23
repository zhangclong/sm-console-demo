/**
 * login.js API 层测试。
 * 验证登录/退出/验证码/用户信息等 API 函数的 URL、HTTP 方法、请求参数是否正确。
 *
 * 使用 vi.mock 模拟 request 和 aesutils，避免真实 HTTP 调用。
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

// ---- Mock request 模块 ----
vi.mock('@/utils/request', () => ({
  default: vi.fn(),
}));

// ---- Mock AES 加密工具 ----
vi.mock('@/utils/aesutils.js', () => ({
  default: {
    encrypt: vi.fn((s) => `encrypted_${s}`),
  },
}));

import request from '@/utils/request';
import { login, getInfo, logout, getCodeImg, getisEditPassword } from '@/api/login';

describe('Login API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    request.mockResolvedValue({ code: 200, data: { token: 'test_token' } });
  });

  // ---- 登录接口 ----

  describe('login()', () => {
    it('should call /login with POST method', () => {
      login('admin', 'password123', '1234', 'uuid-abc');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/login', method: 'post' })
      );
    });

    it('should include username in request data', () => {
      login('admin', 'password123', '1234', 'uuid-abc');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ username: 'admin' }),
        })
      );
    });

    it('should encrypt password before sending', () => {
      login('admin', 'password123', '1234', 'uuid-abc');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ password: 'encrypted_password123' }),
        })
      );
    });

    it('should include captcha code and uuid', () => {
      login('admin', 'password123', '9876', 'uuid-xyz');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ code: '9876', uuid: 'uuid-xyz' }),
        })
      );
    });

    it('should set isToken:false header (no token required for login)', () => {
      login('admin', 'password123', '1234', 'uuid-abc');
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({ isToken: false }),
        })
      );
    });
  });

  // ---- 获取用户信息 ----

  describe('getInfo()', () => {
    it('should call /getInfo with GET method', () => {
      getInfo();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/getInfo', method: 'get' })
      );
    });
  });

  // ---- 退出登录 ----

  describe('logout()', () => {
    it('should call /logout with POST method', () => {
      logout();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/logout', method: 'post' })
      );
    });
  });

  // ---- 验证码 ----

  describe('getCodeImg()', () => {
    it('should call /captchaImage with GET method', () => {
      getCodeImg();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/captchaImage', method: 'get' })
      );
    });

    it('should set isToken:false header (no token required)', () => {
      getCodeImg();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({ isToken: false }),
        })
      );
    });

    it('should set a longer timeout for captcha', () => {
      getCodeImg();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ timeout: 20000 })
      );
    });
  });

  // ---- 密码修改建议 ----

  describe('getisEditPassword()', () => {
    it('should call /passwordSuggest with GET method', () => {
      getisEditPassword();
      expect(request).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/passwordSuggest', method: 'get' })
      );
    });
  });
});

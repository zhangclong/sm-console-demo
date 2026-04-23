/**
 * 登录/退出 API 集成测试。
 * 通过 /interLogin 接口获取 token，再调用真实后端接口验证响应。
 *
 * 前提：后端已使用 -Ptest profile 启动（http://localhost:8083）。
 */
import { describe, it, expect } from 'vitest';
import { interLogin, apiGet, apiPost } from './helpers/apiClient.js';

describe('Login Integration Tests', () => {
  it('interLogin should return a non-empty token', async () => {
    const token = await interLogin('admin', 'admin123');
    expect(token).toBeTruthy();
    expect(typeof token).toBe('string');
    expect(token.length).toBeGreaterThan(0);
  });

  it('GET /getInfo should return admin user info', async () => {
    const token = await interLogin('admin', 'admin123');
    const data = await apiGet(token, '/getInfo');
    expect(data.code).toBe(200);
    expect(data.user).toBeDefined();
    expect(data.user.userName).toBe('admin');
  });

  it('GET /passwordSuggest should return code 200', async () => {
    const token = await interLogin('admin', 'admin123');
    const data = await apiGet(token, '/passwordSuggest');
    expect(data.code).toBe(200);
  });

  it('POST /logout should return code 200', async () => {
    const token = await interLogin('admin', 'admin123');
    const data = await apiPost(token, '/logout');
    expect(data.code).toBe(200);
  });
});

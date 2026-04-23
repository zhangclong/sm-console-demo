/**
 * 用户管理 API 集成测试。
 * 验证用户列表、用户详情、个人信息等只读接口。
 *
 * 前提：后端已使用 -Ptest profile 启动（http://localhost:8083）。
 */
import { describe, it, expect, beforeAll } from 'vitest';
import { interLogin, apiGet } from './helpers/apiClient.js';

describe('User API Integration Tests', () => {
  let token;

  beforeAll(async () => {
    token = await interLogin('admin', 'admin123');
  });

  it('GET /system/user/list should return code 200 with array data', async () => {
    const data = await apiGet(token, '/system/user/list');
    expect(data.code).toBe(200);
    expect(Array.isArray(data.rows)).toBe(true);
  });

  it('GET /system/user/1 should return code 200 (admin user)', async () => {
    const data = await apiGet(token, '/system/user/1');
    expect(data.code).toBe(200);
  });

  it('GET /system/user/profile should return code 200', async () => {
    const data = await apiGet(token, '/system/user/profile');
    expect(data.code).toBe(200);
  });
});

/**
 * 角色管理 API 集成测试。
 * 验证角色列表、角色详情等只读接口。
 *
 * 前提：后端已使用 -Ptest profile 启动（http://localhost:8083）。
 */
import { describe, it, expect, beforeAll } from 'vitest';
import { interLogin, apiGet } from './helpers/apiClient.js';

describe('Role API Integration Tests', () => {
  let token;

  beforeAll(async () => {
    token = await interLogin('admin', 'admin123');
  });

  it('GET /system/role/list should return code 200 with non-empty rows', async () => {
    const data = await apiGet(token, '/system/role/list');
    expect(data.code).toBe(200);
    expect(Array.isArray(data.rows)).toBe(true);
    expect(data.rows.length).toBeGreaterThan(0);
  });

  it('GET /system/role/1 should return code 200 (admin role)', async () => {
    const data = await apiGet(token, '/system/role/1');
    expect(data.code).toBe(200);
  });
});

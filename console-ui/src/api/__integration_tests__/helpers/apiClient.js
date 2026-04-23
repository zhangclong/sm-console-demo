/**
 * 前端 API 集成测试辅助模块。
 * 直接使用 axios 发真实 HTTP 请求，不经过前端 request 拦截器和浏览器 API。
 *
 * 需要后端服务已用 -Ptest profile 启动（开启 /interLogin 接口，admin 密码非过期）。
 */
import axios from 'axios';

/**
 * 后端 base URL，可通过 BACKEND_BASE_URL 环境变量覆盖。
 * CI 中可设置 BACKEND_BASE_URL=http://localhost:8083/web-api
 */
export const baseURL = process.env.BACKEND_BASE_URL || 'http://localhost:8083/web-api';

/**
 * 通过 /interLogin 接口登录，获取 token。
 * 不需要验证码，直接使用 username + password（明文，不加密）。
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<string>} token
 */
export async function interLogin(username, password) {
  const response = await axios.post(`${baseURL}/interLogin`, { username, password });
  const data = response.data;
  if (!data || !data.token) {
    throw new Error(`interLogin failed: ${JSON.stringify(data)}`);
  }
  return data.token;
}

/**
 * 带 Authorization header 的 GET 请求。
 *
 * @param {string} token
 * @param {string} path  - 相对于 baseURL 的路径，如 '/getInfo'
 * @param {object} [params] - 查询参数
 * @returns {Promise<object>} 响应 data
 */
export async function apiGet(token, path, params) {
  const response = await axios.get(`${baseURL}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
    params,
  });
  return response.data;
}

/**
 * 带 Authorization header 的 POST 请求。
 *
 * @param {string} token
 * @param {string} path  - 相对于 baseURL 的路径，如 '/logout'
 * @param {object} [data] - 请求体
 * @returns {Promise<object>} 响应 data
 */
export async function apiPost(token, path, data) {
  const response = await axios.post(`${baseURL}${path}`, data, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data;
}

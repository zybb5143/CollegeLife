import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: '/api',  // 使用相对路径，通过 Nginx 代理
  timeout: 120000  // 120 秒超时，支持 AI 对话和多次重试
})

// 请求拦截器 - 自动添加 JWT Token
api.interceptors.request.use(
  config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 添加 Authorization 头
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response.data
  },
  error => {
    // 错误处理：从 error.response 中获取错误信息
    const message = error.response?.data || error.message || '请求失败'
    console.error('API Error:', message)
    
    // 401 未授权，清除 Token 并跳转到登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('user')
      // 用 SPA 路由跳转替代整页刷新，避免 token 过期时的白屏闪烁；已在登录页则不重复跳转
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
    
    // 将错误信息包装成对象，方便前端处理
    return Promise.reject({
      message: typeof message === 'string' ? message : '请求失败',
      status: error.response?.status
    })
  }
)

export default api

import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL
  || (import.meta.env.PROD && typeof window !== "undefined"
    ? `${window.location.origin}/api`
    : "http://localhost:8081/api");

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

export const extractApiErrorMessage = (err, fallbackMessage = "Request failed.") => {
  const response = err?.response;
  const data = response?.data;
  const status = response?.status;
  const contentType = String(response?.headers?.["content-type"] || "").toLowerCase();

  if (err?.code === "ERR_NETWORK") {
    return "Unable to connect to backend API. Check backend deployment and VITE_API_URL.";
  }

  if (data?.message) {
    return data.message;
  }

  if (data?.errors && typeof data.errors === "object") {
    const firstFieldError = Object.values(data.errors)[0];
    if (firstFieldError) {
      return String(firstFieldError);
    }
  }

  if (typeof data === "string") {
    const looksLikeHtml = contentType.includes("text/html")
      || data.toLowerCase().includes("<!doctype html")
      || data.toLowerCase().includes("<html");
    if (looksLikeHtml && import.meta.env.PROD) {
      return "Backend API route is not configured for this deployment. Set VITE_API_URL to your backend URL ending with /api.";
    }
  }

  if ((status === 404 || status === 502 || status === 503) && import.meta.env.PROD) {
    return "Backend API is unavailable for this deployment. Verify VITE_API_URL and backend health.";
  }

  return fallbackMessage;
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken");
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem("authToken", token);
  } else {
    localStorage.removeItem("authToken");
  }
};

export const clearAuthToken = () => {
  localStorage.removeItem("authToken");
};

export const authApi = {
  register: (payload) => api.post("/auth/register", payload),
  login: (payload) => api.post("/auth/login", payload),
  forgotPassword: (payload) => api.post("/auth/forgot-password", payload),
  resetPassword: (payload) => api.post("/auth/reset-password", payload),
};

export const courseApi = {
  getAll: () => api.get("/courses"),
  getById: (id) => api.get(`/courses/${id}`),
  create: (payload) => api.post("/courses", payload),
  update: (id, payload) => api.put(`/courses/${id}`, payload),
  remove: (id) => api.delete(`/courses/${id}`),
};

export const assignmentApi = {
  getAll: () => api.get("/assignments"),
  getByCourse: (courseId) => api.get(`/assignments/course/${courseId}`),
  create: (formData) => api.post("/assignments", formData, { headers: { "Content-Type": "multipart/form-data" } }),
  update: (id, formData) => api.put(`/assignments/${id}`, formData, { headers: { "Content-Type": "multipart/form-data" } }),
  submit: (id, formData) => api.post(`/assignments/${id}/submit`, formData, { headers: { "Content-Type": "multipart/form-data" } }),
  grade: (submissionId, payload) => api.post(`/assignments/submissions/${submissionId}/grade`, payload),
};

export const quizApi = {
  getAll: () => api.get("/quizzes"),
  getByCourse: (courseId) => api.get(`/quizzes/course/${courseId}`),
  create: (payload) => api.post("/quizzes", payload),
  update: (id, payload) => api.put(`/quizzes/${id}`, payload),
  submit: (id, studentId, payload) => api.post(`/quizzes/${id}/submit?studentId=${studentId}`, payload),
};

export const progressApi = {
  save: (payload) => api.post("/student/progress", payload),
  getByStudent: (studentId) => api.get(`/student/progress?studentId=${studentId}`),
  getSingle: (studentId, courseId) => api.get(`/student/progress/course?studentId=${studentId}&courseId=${courseId}`),
};

export const contentApi = {
  getAll: () => api.get("/content"),
  getByCourse: (courseId) => api.get(`/content/course/${courseId}`),
  create: (formData) => api.post("/content", formData, { headers: { "Content-Type": "multipart/form-data" } }),
  update: (id, formData) => api.put(`/content/${id}`, formData, { headers: { "Content-Type": "multipart/form-data" } }),
  remove: (id) => api.delete(`/content/${id}`),
};

export const adminApi = {
  getUsers: () => api.get("/admin/users"),
  getStudents: () => api.get("/admin/students"),
  updateUserStatus: (id, active) => api.patch(`/admin/users/${id}/status`, { active }),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
};

export default api;
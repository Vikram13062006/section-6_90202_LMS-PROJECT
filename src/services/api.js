import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081/api",
  headers: {
    "Content-Type": "application/json",
  },
});

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

export default api;
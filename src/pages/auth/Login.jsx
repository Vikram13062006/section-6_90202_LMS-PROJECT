import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Captcha from "../../components/Captcha";
import "./Auth.css";
import { ALL_ROLES } from "../../constants/roles";
import { authApi, extractApiErrorMessage, setAuthToken } from "../../services/api";
import { getRoleHome, setCurrentUser } from "../../utils/auth";

const roles = ALL_ROLES;

function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    role: "",
    captcha: "",
  });
  const [captchaCode, setCaptchaCode] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const setField = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const email = form.email.trim().toLowerCase();
    const password = form.password;
    const role = form.role;
    const captcha = form.captcha.trim();

    if (!email || !password || !role || !captcha) {
      setError("All fields are required.");
      return;
    }
    if (captcha !== captchaCode) {
      setError("Captcha is incorrect.");
      return;
    }

    setSubmitting(true);
    try {
      const res = await authApi.login({ email, password });
      const user = res?.data?.user;
      const token = res?.data?.token;
      if (!user || !token) throw new Error("Invalid response");
      setAuthToken(token);
      setCurrentUser(user);
      navigate(getRoleHome(user.role), { replace: true });
    } catch (err) {
      setError(extractApiErrorMessage(err, "Login failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <section className="auth-left">
        <div className="auth-panel">
          <h1>Welcome Back</h1>
          <p>Sign in securely to continue your learning workflow.</p>
        </div>
      </section>

      <section className="auth-right">
        <form className="auth-card" onSubmit={onSubmit} noValidate>
          <h2>Sign In</h2>
          {error ? <div className="auth-error" role="alert">{error}</div> : null}

          <div className="auth-field">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              value={form.email}
              onChange={(e) => setField("email", e.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              value={form.password}
              onChange={(e) => setField("password", e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="login-role">Role</label>
            <select
              id="login-role"
              value={form.role}
              onChange={(e) => setField("role", e.target.value)}
            >
              <option value="">Select role</option>
              {roles.map((r) => (
                <option key={r} value={r}>
                  {r.charAt(0).toUpperCase() + r.slice(1)}
                </option>
              ))}
            </select>
          </div>

          <div className="auth-field">
            <label htmlFor="login-captcha">Captcha</label>
            <Captcha onChange={setCaptchaCode} />
            <input
              id="login-captcha"
              value={form.captcha}
              onChange={(e) => setField("captcha", e.target.value)}
              placeholder="Enter captcha"
            />
          </div>

          <button className="auth-btn" type="submit" disabled={submitting}>
            {submitting ? "Signing In..." : "Sign In"}
          </button>

          <p style={{ marginTop: 10 }}>
            <Link to="/forgot-password">Forgot password?</Link>
          </p>

          <p style={{ marginTop: 12 }}>
            New user? <Link to="/register">Create account</Link>
          </p>
        </form>
      </section>
    </div>
  );
}

export default Login;
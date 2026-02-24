import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Captcha from "../../components/Captcha";
import "./Auth.css";
import { ALL_ROLES } from "../../constants/roles";
import {
  getRegisteredUsers,
  getRoleHome,
  normalizeRole,
  setCurrentUser,
  setRegisteredUsers,
} from "../../utils/auth";

const roles = ALL_ROLES;

function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    confirm: "",
    role: "",
    captcha: "",
  });
  const [captchaCode, setCaptchaCode] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const setField = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  const onSubmit = (e) => {
    e.preventDefault();
    setError("");

    const email = form.email.trim().toLowerCase();
    const password = form.password;
    const confirm = form.confirm;
    const role = form.role;
    const captcha = form.captcha.trim();

    if (!email || !password || !confirm || !role || !captcha) {
      setError("All fields are required.");
      return;
    }
    if (password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    if (password !== confirm) {
      setError("Passwords do not match.");
      return;
    }
    if (captcha !== captchaCode) {
      setError("Captcha is incorrect.");
      return;
    }

    setSubmitting(true);
    try {
      const users = getRegisteredUsers();
      const exists = users.some(
        (u) => u.email === email && normalizeRole(u.role) === normalizeRole(role)
      );
      if (exists) {
        setError("User already exists for this role.");
        return;
      }

      const newUser = { id: String(Date.now()), email, password, role };
      setRegisteredUsers([...users, newUser]);
      setCurrentUser({ id: newUser.id, email: newUser.email, role: newUser.role });

      navigate(getRoleHome(role), { replace: true });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <section className="auth-left">
        <div className="auth-panel">
          <h1>Join EduLMS</h1>
          <p>Create your account and start with a role-specific workspace.</p>
        </div>
      </section>

      <section className="auth-right">
        <form className="auth-card" onSubmit={onSubmit} noValidate>
          <h2>Create Account</h2>
          {error ? <div className="auth-error" role="alert">{error}</div> : null}

          <div className="auth-field">
            <label htmlFor="register-email">Email</label>
            <input
              id="register-email"
              type="email"
              value={form.email}
              onChange={(e) => setField("email", e.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="register-password">Password</label>
            <input
              id="register-password"
              type="password"
              value={form.password}
              onChange={(e) => setField("password", e.target.value)}
              placeholder="Minimum 6 characters"
              autoComplete="new-password"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="register-confirm">Confirm Password</label>
            <input
              id="register-confirm"
              type="password"
              value={form.confirm}
              onChange={(e) => setField("confirm", e.target.value)}
              placeholder="Re-enter password"
              autoComplete="new-password"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="register-role">Role</label>
            <select
              id="register-role"
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
            <label htmlFor="register-captcha">Captcha</label>
            <Captcha onChange={setCaptchaCode} />
            <input
              id="register-captcha"
              value={form.captcha}
              onChange={(e) => setField("captcha", e.target.value)}
              placeholder="Enter captcha"
            />
          </div>

          <button className="auth-btn" type="submit" disabled={submitting}>
            {submitting ? "Creating..." : "Create Account"}
          </button>

          <p style={{ marginTop: 12 }}>
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </section>
    </div>
  );
}

export default Register;
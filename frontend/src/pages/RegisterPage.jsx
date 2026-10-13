import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (event) => {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      await api.register(form);
      navigate('/login');
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="eyebrow">New account</div>
        <h1>Start your journey</h1>
        <p>Create a secure account for your personalized finance planning.</p>

        <form onSubmit={submit} className="auth-form">
          <label>
            Full name
            <input name="fullName" value={form.fullName} onChange={onChange} required />
          </label>

          <label>
            Email
            <input name="email" type="email" value={form.email} onChange={onChange} required />
          </label>

          <label>
            Password
            <input name="password" type="password" value={form.password} onChange={onChange} minLength={8} required />
          </label>

          <label>
            Confirm password
            <input name="confirmPassword" type="password" value={form.confirmPassword} onChange={onChange} minLength={8} required />
          </label>

          {error && <div className="error-box">{error}</div>}

          <button type="submit" disabled={loading}>{loading ? 'Creating account...' : 'Create account'}</button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}

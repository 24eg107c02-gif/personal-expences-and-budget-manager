import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function ProfilePage() {
  const { setUser } = useAuth();
  const [form, setForm] = useState({ fullName: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const data = await api.getProfile();
        setForm({ fullName: data.fullName });
        setUser((prev) => ({ ...prev, fullName: data.fullName, email: data.email }));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');

    try {
      const data = await api.updateProfile({ fullName: form.fullName });
      setUser((prev) => ({ ...prev, fullName: data.fullName, email: data.email }));
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Settings</div>
          <h1>Your profile</h1>
        </div>
      </div>

      <section className="panel-card narrow-panel">
        <div className="panel-header"><h3>Account preferences</h3></div>
        {loading ? (
          <div className="inline-loading">Loading profile...</div>
        ) : (
          <form className="stack-form" onSubmit={submit}>
            <label>
              Full name
              <input value={form.fullName} onChange={(e) => setForm({ fullName: e.target.value })} required />
            </label>
            {error && <div className="error-box">{error}</div>}
            <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save changes'}</button>
          </form>
        )}
      </section>
    </div>
  );
}

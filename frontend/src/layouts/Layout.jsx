import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { to: '/', label: 'Overview' },
  { to: '/transactions', label: 'Transactions' },
  { to: '/budgets', label: 'Budgets' },
  { to: '/reports', label: 'Reports' },
  { to: '/profile', label: 'Profile' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">F</div>
          <div>
            <strong>Finora</strong>
            <small>Smart finance</small>
          </div>
        </div>

        <nav className="nav">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="user-card">
          <div className="avatar">{user?.fullName?.slice(0, 1).toUpperCase() || 'U'}</div>
          <div>
            <strong>{user?.fullName || 'User'}</strong>
            <small>{user?.email || 'demo@finora.app'}</small>
          </div>
        </div>

        <button className="logout-button" onClick={handleLogout}>Log out</button>
      </aside>

      <main className="main-panel">
        <Outlet />
      </main>
    </div>
  );
}

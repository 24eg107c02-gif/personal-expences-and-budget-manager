import { createContext, useContext, useEffect, useMemo, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('finora-token') || '');
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('finora-user');
    return stored ? JSON.parse(stored) : null;
  });

  useEffect(() => {
    if (token) {
      localStorage.setItem('finora-token', token);
    } else {
      localStorage.removeItem('finora-token');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('finora-user', JSON.stringify(user));
    } else {
      localStorage.removeItem('finora-user');
    }
  }, [user]);

  const value = useMemo(() => ({
    token,
    user,
    setToken,
    setUser,
    logout: () => {
      setToken('');
      setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

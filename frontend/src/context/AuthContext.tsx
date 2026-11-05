import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from '../api/axios';

interface User {
  user_id: number;
  full_name: string;
  email: string;
  role: 'user' | 'driver' | 'admin' | 'super_admin' | 'dispatcher' | 'hospital' | 'doctor';
  vehicle?: {
    ambulance_id: number;
    vehicle_number: string;
    ambulance_type: 'BLS' | 'ALS' | 'CCU';
    status: 'online' | 'busy' | 'offline';
  };
}

interface Toast {
  id: number;
  title: string;
  desc: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

interface AuthContextType {
  token: string | null;
  user: User | null;
  globalLoading: boolean;
  toasts: Toast[];
  unreadAlertsCount: number;
  setGlobalLoading: (loading: boolean) => void;
  showToast: (title: string, desc: string, type: 'success' | 'error' | 'info' | 'warning') => void;
  login: (token: string, user: User) => void;
  logout: () => void;
  updateUserLocal: (updatedUser: User) => void;
  refreshUnreadAlerts: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(sessionStorage.getItem('safereach_token'));
  const [user, setUser] = useState<User | null>(() => {
    const u = sessionStorage.getItem('safereach_user');
    return u ? JSON.parse(u) : null;
  });
  const [globalLoading, setGlobalLoading] = useState<boolean>(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [unreadAlertsCount, setUnreadAlertsCount] = useState<number>(0);
  
  const showToast = (title: string, desc: string, type: 'success' | 'error' | 'info' | 'warning') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, title, desc, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 5000);
  };

  const login = (jwtToken: string, userData: User) => {
    setToken(jwtToken);
    setUser(userData);
    sessionStorage.setItem('safereach_token', jwtToken);
    sessionStorage.setItem('safereach_user', JSON.stringify(userData));
    
    // Set axios auth header immediately
    axios.defaults.headers.common['Authorization'] = `Bearer ${jwtToken}`;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setUnreadAlertsCount(0);
    sessionStorage.removeItem('safereach_token');
    sessionStorage.removeItem('safereach_user');
    delete axios.defaults.headers.common['Authorization'];
    showToast("Session Closed", "Successfully logged out from SafeReach.", "info");
  };

  const updateUserLocal = (updatedUser: User) => {
    setUser(updatedUser);
    sessionStorage.setItem('safereach_user', JSON.stringify(updatedUser));
  };

  const refreshUnreadAlerts = async () => {
    if (!token || !user || user.role !== 'user') return;
    try {
      const res = await axios.get('/user/notifications.php');
      if (res.data && res.data.success) {
        const count = res.data.data.filter((n: any) => !n.is_read).length;
        setUnreadAlertsCount(count);
      }
    } catch (e) {
      console.warn("Failed to update unread notifications counts", e);
    }
  };

  useEffect(() => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      refreshUnreadAlerts();
      const interval = setInterval(refreshUnreadAlerts, 20000);
      return () => clearInterval(interval);
    }
  }, [token]);

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        globalLoading,
        toasts,
        unreadAlertsCount,
        setGlobalLoading,
        showToast,
        login,
        logout,
        updateUserLocal,
        refreshUnreadAlerts,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

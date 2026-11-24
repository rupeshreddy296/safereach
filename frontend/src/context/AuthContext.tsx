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

// FLAG: Set to true to remove the login page & username/password requirement.
// Change back to false whenever you want the login page restored.
export const BYPASS_LOGIN_MODE = true;

const DEFAULT_BYPASS_USER: User = {
  user_id: 1,
  full_name: 'Rupesh',
  email: 'rupesh112599@gmail.com',
  role: 'user',
};

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
  switchRole: (role: 'user' | 'driver' | 'admin') => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(sessionStorage.getItem('safereach_token'));
  const [token, setToken] = useState<string | null>(() => {
    const t = sessionStorage.getItem('safereach_token');
    if (t) return t;
    return BYPASS_LOGIN_MODE ? 'bypass_active_session_token' : null;
  });

  const [user, setUser] = useState<User | null>(() => {
    const u = sessionStorage.getItem('safereach_user');
    return u ? JSON.parse(u) : null;
    if (u) {
      try { return JSON.parse(u); } catch { /* ignore */ }
    }
    return BYPASS_LOGIN_MODE ? DEFAULT_BYPASS_USER : null;
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

  const switchRole = (newRole: 'user' | 'driver' | 'admin') => {
    let updatedUser: User;
    if (newRole === 'admin') {
      updatedUser = {
        user_id: 1,
        full_name: 'Administrator',
        email: 'admin@safereach.com',
        role: 'admin',
      };
    } else if (newRole === 'driver') {
      updatedUser = {
        user_id: 2,
        full_name: 'Driver Ramesh',
        email: 'driver@safereach.com',
        role: 'driver',
        vehicle: {
          ambulance_id: 101,
          vehicle_number: 'TN-01-AMB-108',
          ambulance_type: 'ALS',
          status: 'online',
        },
      };
    } else {
      updatedUser = {
        user_id: 3,
        full_name: 'Rupesh',
        email: 'rupesh112599@gmail.com',
        role: 'user',
      };
    }
    setUser(updatedUser);
    sessionStorage.setItem('safereach_user', JSON.stringify(updatedUser));
    showToast('Role Switched', `Active view changed to ${updatedUser.full_name} (${newRole.toUpperCase()})`, 'info');
  };

  const logout = () => {
    if (BYPASS_LOGIN_MODE) {
      switchRole('user');
      showToast("Session Reset", "Bypass mode active. Reset to Citizen view.", "info");
      return;
    }
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
        switchRole,
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

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useAuth, BYPASS_LOGIN_MODE } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { token, user } = useAuth();

  // If login is bypassed, allow direct access without username/password
  if (BYPASS_LOGIN_MODE) {
    return <>{children}</>;
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    if (user.role === 'driver') return <Navigate to="/driver" replace />;
    if (user.role === 'admin') return <Navigate to="/admin" replace />;
    return <Navigate to="/citizen" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;

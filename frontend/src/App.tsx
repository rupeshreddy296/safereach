import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import CitizenLayout from './components/CitizenLayout';

// Pages
import Welcome from './pages/citizen/Welcome';
import Login from './pages/citizen/Login';
import SignUp from './pages/citizen/SignUp';
import ForgotPassword from './pages/citizen/ForgotPassword';
import ResetPassword from './pages/citizen/ResetPassword';
import Dashboard from './pages/citizen/Dashboard';
import Tracker from './pages/citizen/Tracker';
import History from './pages/citizen/History';
import Profile from './pages/citizen/Profile';
import Addresses from './pages/citizen/Addresses';
import Notifications from './pages/citizen/Notifications';
import Hospitals from './pages/citizen/Hospitals';
import HealthTips from './pages/citizen/HealthTips';
import Contacts from './pages/citizen/Contacts';
import DriverLayout from './pages/driver/DriverLayout';
import DriverDashboard from './pages/driver/DriverDashboard';
import ActiveMission from './pages/driver/ActiveMission';
import DriverHistory from './pages/driver/DriverHistory';
import DriverProfile from './pages/driver/DriverProfile';
import DriverVehicle from './pages/driver/DriverVehicle';
import DriverNotifications from './pages/driver/DriverNotifications';
import DriverSettings from './pages/driver/DriverSettings';
import DriverHelp from './pages/driver/DriverHelp';
import AdminLayout from './components/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminBookings from './pages/admin/AdminBookings';
import AdminMonitor from './pages/admin/AdminMonitor';
import AdminSOS from './pages/admin/AdminSOS';
import AdminDrivers from './pages/admin/AdminDrivers';
import AdminCitizens from './pages/admin/AdminCitizens';
import AdminAmbulances from './pages/admin/AdminAmbulances';
import AdminHospitals from './pages/admin/AdminHospitals';
import AdminAnalytics from './pages/admin/AdminAnalytics';
import AdminReports from './pages/admin/AdminReports';
import AdminNotifications from './pages/admin/AdminNotifications';
import AdminSettings from './pages/admin/AdminSettings';

import { AlertTriangle, CheckCircle, HelpCircle, Info, XCircle } from 'lucide-react';

const iconMap: Record<string, any> = {
  AlertTriangle, CheckCircle, HelpCircle, Info, XCircle
};

const AppContent: React.FC = () => {
  const { globalLoading, toasts } = useAuth();

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  return (
    <BrowserRouter>
      {/* Global Spinner Overlay */}
      {globalLoading && (
        <div className="fixed inset-0 bg-slate-950/80 z-[9999] flex flex-col items-center justify-center gap-4">
          <div className="w-12 h-12 rounded-full border-4 border-t-accentRed border-white/10 animate-spin"></div>
          <p className="text-sm font-semibold tracking-wide text-gray-300">Connecting to SafeReach emergency servers...</p>
        </div>
      )}

      {/* Global Toast Alerts */}
      <div className="fixed top-20 right-6 z-[9999] flex flex-col gap-3 pointer-events-none max-w-sm w-full">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`pointer-events-auto p-4 rounded-2xl glass-panel border border-white/5 shadow-2xl flex gap-3 animate-slide-in items-start`}
          >
            <div className={`p-2 rounded-lg flex items-center justify-center shrink-0 ${
              toast.type === 'success'
                ? 'bg-emerald-500/15 text-emerald-400'
                : toast.type === 'error'
                ? 'bg-red-500/15 text-accentRed'
                : toast.type === 'warning'
                ? 'bg-amber-500/15 text-amber-400'
                : 'bg-blue-500/15 text-blue-400'
            }`}>
              {toast.type === 'success' && getLucideIcon('CheckCircle', { size: 16 })}
              {toast.type === 'error' && getLucideIcon('XCircle', { size: 16 })}
              {toast.type === 'warning' && getLucideIcon('AlertTriangle', { size: 16 })}
              {toast.type === 'info' && getLucideIcon('Info', { size: 16 })}
            </div>
            <div className="flex-1 space-y-1">
              <h4 className="text-xs font-extrabold text-white leading-snug">{toast.title}</h4>
              <p className="text-[10px] text-gray-400 leading-normal">{toast.desc}</p>
            </div>
          </div>
        ))}
      </div>

      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Welcome />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Protected Citizen Routes */}
        <Route
          path="/citizen"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Dashboard />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/track/:id"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Tracker />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/history"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <History />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/profile"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Profile />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/addresses"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Addresses />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/notifications"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Notifications />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/hospitals"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Hospitals />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/health-tips"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <HealthTips />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/contacts"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <CitizenLayout>
                <Contacts />
              </CitizenLayout>
            </ProtectedRoute>
          }
        />

        {/* Protected Driver Routes */}
        <Route
          path="/driver"
          element={
            <ProtectedRoute allowedRoles={['driver']}>
              <DriverLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<DriverDashboard />} />
          <Route path="trip" element={<ActiveMission />} />
          <Route path="history" element={<DriverHistory />} />
          <Route path="vehicle" element={<DriverVehicle />} />
          <Route path="profile" element={<DriverProfile />} />
          <Route path="notifications" element={<DriverNotifications />} />
          <Route path="settings" element={<DriverSettings />} />
          <Route path="help" element={<DriverHelp />} />
        </Route>

        {/* Protected Admin Routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['admin', 'super_admin', 'dispatcher']}>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="bookings" element={<AdminBookings />} />
          <Route path="monitor" element={<AdminMonitor />} />
          <Route path="sos" element={<AdminSOS />} />
          <Route path="drivers" element={<AdminDrivers />} />
          <Route path="citizens" element={<AdminCitizens />} />
          <Route path="ambulances" element={<AdminAmbulances />} />
          <Route path="hospitals" element={<AdminHospitals />} />
          <Route path="analytics" element={<AdminAnalytics />} />
          <Route path="reports" element={<AdminReports />} />
          <Route path="notifications" element={<AdminNotifications />} />
          <Route path="settings" element={<AdminSettings />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

const App: React.FC = () => {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
};

export default App;

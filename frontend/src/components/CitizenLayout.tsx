import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  HelpCircle, LayoutDashboard, History, User, MapPin, Building2,
  Bell, ShieldAlert, LogOut, X, Menu
} from 'lucide-react';

const iconMap: Record<string, React.ElementType> = {
  HelpCircle, LayoutDashboard, History, User, MapPin, Building2,
  Bell, ShieldAlert, LogOut, X, Menu
};

const CitizenLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { logout, unreadAlertsCount } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const navLinks = [
    { label: 'Dashboard', path: '/citizen', icon: 'LayoutDashboard' },
    { label: 'History', path: '/citizen/history', icon: 'History' },
    { label: 'Profile', path: '/citizen/profile', icon: 'User' },
    { label: 'Addresses', path: '/citizen/addresses', icon: 'MapPin' },
    { label: 'Hospitals', path: '/citizen/hospitals', icon: 'Building2' },
    { label: 'Alerts', path: '/citizen/notifications', icon: 'Bell', badge: unreadAlertsCount },
  ];

  const handleNavigate = (path: string) => {
    navigate(path);
    setMobileMenuOpen(false);
  };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex flex-col">
      {/* Top Navigation Header */}
      <header className="glass-panel border-b border-white/5 fixed top-0 inset-x-0 h-16 z-40 px-4 flex items-center justify-between">
        <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/citizen')}>
          <div className="text-accentRed">{getLucideIcon('ShieldAlert', { size: 22 })}</div>
          <span className="font-heading font-extrabold text-lg text-white tracking-wide">
            SAFE<span className="text-accentRed">REACH</span>
          </span>
        </div>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-6">
          {navLinks.map((link) => {
            const isActive = location.pathname === link.path;
            return (
              <button
                key={link.path}
                onClick={() => handleNavigate(link.path)}
                className={`flex items-center gap-2 text-xs font-semibold tracking-wide transition relative py-2 px-3 rounded-lg ${
                  isActive ? 'bg-accentRed/10 text-white' : 'text-gray-400 hover:text-white'
                }`}
              >
                {getLucideIcon(link.icon, { size: 14 })}
                {link.label}
                {!!link.badge && (
                  <span className="absolute -top-1 -right-1 bg-accentRed text-white text-[9px] font-bold px-1.5 py-0.5 rounded-full">
                    {link.badge}
                  </span>
                )}
              </button>
            );
          })}

          <button
            onClick={logout}
            className="flex items-center gap-2 text-xs font-semibold text-gray-400 hover:text-accentRed transition border border-white/10 px-3 py-1.5 rounded-xl bg-white/5"
          >
            {getLucideIcon('LogOut', { size: 14 })}
            Sign Out
          </button>
        </nav>

        {/* Mobile menu trigger */}
        <div className="flex items-center gap-4 md:hidden">
          <button
            onClick={() => handleNavigate('/citizen/notifications')}
            className="relative p-2 text-gray-400 hover:text-white transition"
          >
            {getLucideIcon('Bell', { size: 20 })}
            {unreadAlertsCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-accentRed"></span>
            )}
          </button>
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-gray-400 hover:text-white transition"
          >
            {getLucideIcon(mobileMenuOpen ? 'X' : 'Menu', { size: 22 })}
          </button>
        </div>
      </header>

      {/* Mobile Drawer Panel */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 top-16 bg-slate-950/95 z-40 flex flex-col p-6 animate-fade-in md:hidden">
          <div className="flex-1 space-y-4">
            {navLinks.map((link) => {
              const isActive = location.pathname === link.path;
              return (
                <button
                  key={link.path}
                  onClick={() => handleNavigate(link.path)}
                  className={`w-full flex items-center gap-4 py-3.5 px-4 rounded-xl text-sm font-bold tracking-wide transition ${
                    isActive ? 'bg-accentRed/10 text-white' : 'text-gray-400 hover:text-white'
                  }`}
                >
                  {getLucideIcon(link.icon, { size: 18, className: 'text-accentRed' })}
                  <span className="flex-1 text-left">{link.label}</span>
                  {!!link.badge && (
                    <span className="bg-accentRed text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                      {link.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          <button
            onClick={logout}
            className="w-full flex items-center justify-center gap-2 py-3.5 border border-white/15 bg-white/5 rounded-xl font-bold hover:text-accentRed transition"
          >
            {getLucideIcon('LogOut', { size: 16 })}
            Log Out
          </button>
        </div>
      )}

      {/* Main View Area */}
      <main className="flex-1 pt-20 pb-12 px-4 max-w-7xl w-full mx-auto animate-fade-in">
        {children}
      </main>

      {/* Bottom Footer */}
      <footer className="glass-panel border-t border-white/5 py-6 text-center text-xs text-gray-500">
        <p>&copy; 2026 SafeReach Emergency System. Designed for premium coordination and logistics.</p>
      </footer>
    </div>
  );
};

export default CitizenLayout;

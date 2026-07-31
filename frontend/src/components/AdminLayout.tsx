import React from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, Bell, AlertTriangle, Users, Truck, Building2, 
  Activity, FileText, Settings, LogOut, Search, ShieldAlert, UserCheck
} from 'lucide-react';

const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Live Monitor', path: '/admin/monitor', icon: Activity },
    { name: 'SOS Requests', path: '/admin/sos', icon: AlertTriangle, alert: true },
    { name: 'Driver Mgmt', path: '/admin/drivers', icon: UserCheck },
    { name: 'Citizen Mgmt', path: '/admin/citizens', icon: Users },
    { name: 'Ambulances', path: '/admin/ambulances', icon: Truck },
    { name: 'Hospitals', path: '/admin/hospitals', icon: Building2 },
    { name: 'Analytics', path: '/admin/analytics', icon: Activity },
    { name: 'Reports', path: '/admin/reports', icon: FileText },
    { name: 'Notifications', path: '/admin/notifications', icon: Bell },
    { name: 'Settings', path: '/admin/settings', icon: Settings },
  ];

  return (
    <div className="flex h-screen bg-darkBg text-white overflow-hidden font-body">
      {/* Sidebar */}
      <aside className="w-64 glass-panel border-r border-white/5 flex flex-col z-20 hidden md:flex">
        {/* Brand */}
        <div className="h-16 flex items-center px-6 border-b border-white/5">
          <ShieldAlert className="text-accentRed mr-2" size={24} />
          <span className="font-heading font-extrabold text-xl tracking-wider">
            SAFE<span className="text-accentRed">REACH</span>
          </span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-1 custom-scrollbar">
          <div className="text-[10px] uppercase font-bold text-gray-500 tracking-wider mb-2 px-3">
            Command Center
          </div>
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition ${
                  isActive 
                    ? 'bg-accentRed/10 text-accentRed border border-accentRed/20 shadow-[0_0_15px_rgba(220,38,38,0.1)]' 
                    : 'text-gray-400 hover:text-white hover:bg-white/5 border border-transparent'
                }`}
              >
                <Icon size={18} className={isActive ? 'text-accentRed' : 'text-gray-500'} />
                <span className="text-sm font-semibold">{item.name}</span>
                {item.alert && (
                  <span className="ml-auto flex h-2 w-2 relative">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
                  </span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* User Profile Footer */}
        <div className="p-4 border-t border-white/5">
          <div className="glass-card rounded-xl p-3 flex items-center gap-3 border border-white/5">
            <div className="w-10 h-10 rounded-full bg-slate-800 border border-white/10 flex items-center justify-center font-bold text-accentRed">
              {user?.full_name?.charAt(0) || 'A'}
            </div>
            <div className="flex-1 overflow-hidden">
              <p className="text-xs font-bold text-white truncate">{user?.full_name}</p>
              <p className="text-[10px] text-gray-400 font-mono">System Admin</p>
            </div>
            <button onClick={handleLogout} className="text-gray-500 hover:text-accentRed transition">
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full relative overflow-hidden">
        {/* Top Header */}
        <header className="h-16 glass-panel border-b border-white/5 px-6 flex items-center justify-between z-10 shrink-0">
          <div className="flex items-center gap-4">
            <h1 className="font-heading text-lg font-bold">Emergency Operations</h1>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="relative hidden md:block">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={14} />
              <input 
                type="text" 
                placeholder="Global tracking search..." 
                className="bg-slate-900/50 border border-white/10 rounded-full py-1.5 pl-9 pr-4 text-xs text-white focus:outline-none focus:border-white/20 w-64 transition"
              />
            </div>
            <button className="relative p-2 text-gray-400 hover:text-white transition rounded-full hover:bg-white/5">
              <Bell size={18} />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-accentRed rounded-full border border-darkBg"></span>
            </button>
          </div>
        </header>

        {/* Scrollable Page Content */}
        <main className="flex-1 overflow-y-auto custom-scrollbar p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { ChevronRight, HelpCircle, Lock, LogIn, Mail, ShieldAlert, ShieldCheck, Truck, User } from 'lucide-react';

const iconMap: Record<string, any> = {
  ChevronRight, HelpCircle, Lock, LogIn, Mail, ShieldAlert, ShieldCheck, Truck, User
};

interface RecentAccount {
  email: string;
  name: string;
  role: 'user' | 'driver' | 'admin';
}

const ROLE_TABS: { key: 'user' | 'driver' | 'admin'; label: string; icon: string; desc: string }[] = [
  { key: 'user',   label: 'User',   icon: 'User',        desc: 'Citizen emergency access' },
  { key: 'driver', label: 'Driver', icon: 'Truck',       desc: 'Paramedic & ambulance crew' },
  { key: 'admin',  label: 'Admin',  icon: 'ShieldCheck', desc: 'Command centre access' },
];

const Login: React.FC = () => {
  const { login, showToast, setGlobalLoading } = useAuth();
  const navigate = useNavigate();

  const [role, setRole]             = useState<'user' | 'driver' | 'admin'>('user');
  const [email, setEmail]           = useState('');
  const [password, setPassword]     = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [recentAccounts, setRecentAccounts] = useState<RecentAccount[]>([]);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  /* ─── Load recently signed-in accounts ─── */
  useEffect(() => {
    try {
      const stored = localStorage.getItem('safereach_recent_accounts');
      if (stored) setRecentAccounts(JSON.parse(stored));
    } catch { /* ignore */ }
  }, []);

  const saveRecentAccount = (savedEmail: string, name: string, userRole: 'user' | 'driver' | 'admin') => {
    try {
      const stored = localStorage.getItem('safereach_recent_accounts');
      const existing: RecentAccount[] = stored ? JSON.parse(stored) : [];
      const updated = [
        { email: savedEmail, name, role: userRole },
        ...existing.filter((a) => a.email !== savedEmail),
      ].slice(0, 3);
      localStorage.setItem('safereach_recent_accounts', JSON.stringify(updated));
      setRecentAccounts(updated);
    } catch { /* ignore */ }
  };

  /* ─── Tab switch ─── */
  const handleTabChange = (newRole: 'user' | 'driver' | 'admin') => {
    setRole(newRole);
    setEmail('');
    setPassword('');
  };

  /* ─── Login submit ─── */
  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalLoading(true);
    try {
      const res = await axios.post('/auth/login.php', { email, password, role });

      if (res.data?.success && res.data.data?.token) {
        const d = res.data.data;
        const userObj = {
          user_id:   d.user_id,
          full_name: d.full_name,
          email:     d.email,
          role:      d.role,
          vehicle:   d.vehicle,
        };

        login(d.token, userObj);

        const mappedRole: 'user' | 'driver' | 'admin' =
          d.role === 'driver' ? 'driver'
          : ['admin', 'super_admin', 'dispatcher'].includes(d.role) ? 'admin'
          : 'user';

        saveRecentAccount(email, d.full_name, mappedRole);
        showToast('Welcome Back!', `Signed in as ${d.full_name}`, 'success');

        if (d.role === 'driver') navigate('/driver');
        else if (['admin', 'super_admin', 'dispatcher'].includes(d.role)) navigate('/admin');
        else navigate('/citizen');
      }
    } catch (err: any) {
      showToast('Sign In Failed', err.response?.data?.message || err.message, 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  /* ─── Recent account quick-fill ─── */
  const handleRecentSelect = (account: RecentAccount) => {
    setEmail(account.email);
    setRole(account.role);
    setPassword('');
    showToast('Account Selected', `Enter your password to continue as ${account.name}`, 'info');
  };

  const handleRemoveRecent = (acctEmail: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const updated = recentAccounts.filter((a) => a.email !== acctEmail);
    setRecentAccounts(updated);
    localStorage.setItem('safereach_recent_accounts', JSON.stringify(updated));
  };

  const roleIconMap: Record<string, string> = { user: 'User', driver: 'Truck', admin: 'ShieldCheck' };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex flex-col justify-center p-5 max-w-md mx-auto">

      {/* Brand Header */}
      <div className="text-center space-y-2 mb-8">
        <div className="w-16 h-16 rounded-2xl glass-panel flex items-center justify-center border border-red-500/10 mx-auto text-accentRed shadow-lg">
          {getLucideIcon('ShieldAlert', { size: 36 })}
        </div>
        <h1 className="text-2xl font-heading font-extrabold text-white">SafeReach Response</h1>
        <p className="text-gray-400 text-sm">Secure Portal Login</p>
      </div>

      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-2xl space-y-5">

        {/* Role Tabs */}
        <div className="grid grid-cols-3 gap-1.5 bg-slate-900/60 p-1 rounded-xl border border-white/5">
          {ROLE_TABS.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => handleTabChange(tab.key)}
              className={`py-2.5 rounded-lg font-heading text-xs font-bold transition flex flex-col items-center gap-1 ${
                role === tab.key ? 'bg-accentRed text-white shadow-md' : 'text-gray-400 hover:text-white'
              }`}
            >
              {getLucideIcon(tab.icon, { size: 14 })}
              {tab.label}
            </button>
          ))}
        </div>

        <p className="text-center text-[10px] text-gray-500 -mt-1">
          {ROLE_TABS.find((t) => t.key === role)?.desc}
        </p>

        {/* Login Form — Email/Phone + Password */}
        <form onSubmit={handleLoginSubmit} className="space-y-4">

          {/* Email or Phone */}
          <div className="space-y-1">
            <label className="text-xs font-semibold text-gray-300">Email or Phone Number</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('User', { size: 16 })}
              </span>
              <input
                type="text"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email or Phone Number"
                required
                autoComplete="username"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-3 pl-10 pr-4 text-sm focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1">
            <div className="flex justify-between items-center">
              <label className="text-xs font-semibold text-gray-300">Password</label>
              <button
                type="button"
                onClick={() => navigate('/forgot-password')}
                className="text-xs text-blue-400 hover:underline"
              >
                Forgot password?
              </button>
            </div>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Lock', { size: 16 })}
              </span>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                autoComplete="current-password"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-3 pl-10 pr-10 text-sm focus:outline-none focus:border-accentRed text-white transition"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-500 hover:text-white transition"
              >
                {getLucideIcon(showPassword ? 'EyeOff' : 'Eye', { size: 15 })}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3.5 rounded-xl shadow-lg transition flex items-center justify-center gap-2"
          >
            {getLucideIcon('LogIn', { size: 16 })}
            Sign In
          </button>
        </form>

        {/* Create Account link */}
        <div className="flex justify-center items-center text-xs text-gray-400 gap-1.5">
          <span>Don't have an account?</span>
          <button onClick={() => navigate('/signup')} className="text-accentRed hover:underline font-bold">
            Create Account
          </button>
        </div>

        {/* Recently Signed-In — only accounts the user actually logged into */}
        {recentAccounts.length > 0 && (
          <div className="pt-4 border-t border-white/5 space-y-3">
            <p className="text-center text-[10px] uppercase font-bold tracking-wider text-gray-500">
              Recently Signed In
            </p>
            <div className="space-y-2">
              {recentAccounts.map((account) => (
                <button
                  key={account.email}
                  onClick={() => handleRecentSelect(account)}
                  className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-white/5 bg-slate-900/30 hover:bg-slate-900/60 hover:border-white/10 transition group text-left"
                >
                  <div className="w-8 h-8 rounded-lg bg-accentRed/10 border border-accentRed/20 text-accentRed flex items-center justify-center shrink-0">
                    {getLucideIcon(roleIconMap[account.role] || 'User', { size: 14 })}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-bold text-white truncate">{account.name}</p>
                    <p className="text-[10px] text-gray-500 truncate">
                      {account.email} · {account.role.charAt(0).toUpperCase() + account.role.slice(1)}
                    </p>
                  </div>
                  <div className="flex items-center gap-1.5 opacity-0 group-hover:opacity-100 transition shrink-0">
                    <span
                      role="button"
                      onClick={(e) => handleRemoveRecent(account.email, e)}
                      className="p-1 text-gray-600 hover:text-accentRed transition cursor-pointer"
                      title="Remove"
                    >
                      {getLucideIcon('X', { size: 11 })}
                    </span>
                    {getLucideIcon('ChevronRight', { size: 14, className: 'text-gray-500' })}
                  </div>
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Login;

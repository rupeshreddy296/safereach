import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { HelpCircle, Lock, ShieldAlert, ShieldCheck } from 'lucide-react';

const iconMap: Record<string, any> = {
  HelpCircle, Lock, ShieldAlert, ShieldCheck
};

const ResetPassword: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const phone = searchParams.get('phone') || '';
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phone || !code || !newPassword) return;
    setGlobalLoading(true);
    try {
      const res = await axios.post('/auth/reset_password.php', {
        phone_number: phone,
        otp: code,
        new_password: newPassword,
      });

      if (res.data && res.data.success) {
        showToast("Password Saved", "Your password has been successfully updated.", "success");
        navigate('/login');
      }
    } catch (err: any) {
      showToast("Reset Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex flex-col justify-center p-6 max-w-md mx-auto">
      <div className="text-center space-y-2 mb-6">
        <div className="w-14 h-14 rounded-2xl glass-panel flex items-center justify-center border border-red-500/10 mx-auto text-accentRed shadow-lg">
          {getLucideIcon('ShieldAlert', { size: 30 })}
        </div>
        <h1 className="text-xl font-heading font-extrabold text-white">Save New Password</h1>
        <p className="text-gray-400 text-xs">Enter code and write a new secure password</p>
      </div>

      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-2xl space-y-4">
        <form onSubmit={handleResetSubmit} className="space-y-4">
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Registered Phone</label>
            <input
              type="text"
              readOnly
              value={phone}
              className="w-full bg-slate-900/30 border border-white/5 rounded-xl py-2.5 px-4 text-xs text-gray-400 focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">SMS Verification Code</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('ShieldCheck', { size: 15 })}
              </span>
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="Enter 6-digit code"
                required
                maxLength={6}
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition tracking-widest text-center font-bold"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">New Secure Password</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Lock', { size: 15 })}
              </span>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Minimum 6 characters"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs"
          >
            Update Password
          </button>
        </form>
      </div>
    </div>
  );
};

export default ResetPassword;

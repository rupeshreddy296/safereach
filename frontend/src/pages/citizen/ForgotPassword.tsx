import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { HelpCircle, KeyRound, Phone } from 'lucide-react';

const iconMap: Record<string, any> = {
  HelpCircle, KeyRound, Phone
};

const ForgotPassword: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();
  const navigate = useNavigate();
  const [phone, setPhone] = useState('');

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const handleSendCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phone) return;
    setGlobalLoading(true);
    try {
      const res = await axios.post('/auth/forgot_password.php', { phone_number: phone });
      if (res.data && res.data.success) {
        showToast("Code Dispatched", "A verification reset code was sent successfully.", "success");
        navigate(`/reset-password?phone=${encodeURIComponent(phone)}`);
      }
    } catch (err: any) {
      showToast("Request Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex flex-col justify-center p-6 max-w-md mx-auto">
      <div className="text-center space-y-2 mb-6">
        <div className="w-14 h-14 rounded-2xl glass-panel flex items-center justify-center border border-red-500/10 mx-auto text-accentRed shadow-lg">
          {getLucideIcon('KeyRound', { size: 30 })}
        </div>
        <h1 className="text-xl font-heading font-extrabold text-white">Reset SafeReach Password</h1>
        <p className="text-gray-400 text-xs">Verify credentials via SMS link</p>
      </div>

      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-2xl space-y-4">
        <form onSubmit={handleSendCode} className="space-y-4">
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Registered Phone Number</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Phone', { size: 15 })}
              </span>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="e.g. +919999999999"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs"
          >
            Send Verification Code
          </button>
        </form>

        <button
          onClick={() => navigate('/login')}
          className="w-full py-2.5 text-xs text-gray-400 hover:text-white font-bold transition rounded-xl text-center"
        >
          Cancel and Return
        </button>
      </div>
    </div>
  );
};

export default ForgotPassword;

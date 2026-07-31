import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Phone, Mail, Award, Calendar, Shield, Truck, Activity, Navigation, CheckCircle } from 'lucide-react';

const DriverProfile: React.FC = () => {
  const { setGlobalLoading, showToast } = useAuth();
  const [profile, setProfile] = useState<any>(null);

  // Change Password forms
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/driver/profile.php');
      if (res.data?.success) {
        setProfile(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to load profile", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      showToast("Password Error", "New passwords do not match.", "warning");
      return;
    }
    setGlobalLoading(true);
    try {
      const res = await axios.post('/auth/change_password.php', {
        old_password: oldPassword,
        new_password: newPassword
      });
      if (res.data?.success) {
        showToast("Success", "Password changed successfully.", "success");
        setOldPassword('');
        setNewPassword('');
        setConfirmPassword('');
      }
    } catch (err: any) {
      showToast("Change Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  if (!profile) return null;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">My Profile</h2>
          <p className="text-sm text-slate-400 mt-1">Manage your driver credentials, personal information, and assigned vehicle details.</p>
        </div>
      </div>

      <div className="glass-panel rounded-3xl p-8 border border-white/5 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/10 rounded-full blur-[100px] pointer-events-none -mr-32 -mt-32"></div>
        
        <div className="flex items-center gap-6 relative z-10 mb-8 pb-8 border-b border-white/5">
          <div className="w-24 h-24 rounded-2xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30 shadow-lg text-3xl font-extrabold uppercase">
            {profile.full_name?.substring(0, 2) || 'DR'}
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-white">{profile.full_name}</h3>
            <span className="text-xs font-bold uppercase tracking-widest text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-lg mt-2 inline-block">
              {profile.driver_role || 'Emergency Paramedic'}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative z-10">
          <div className="space-y-6">
            <h4 className="text-sm font-extrabold uppercase tracking-wider text-slate-500 border-b border-white/5 pb-2">Contact Details</h4>
            
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
                <Phone className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Phone Number</p>
                <p className="text-white font-bold">{profile.phone_number}</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
                <Mail className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Email Address</p>
                <p className="text-white font-bold">{profile.email}</p>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <h4 className="text-sm font-extrabold uppercase tracking-wider text-slate-500 border-b border-white/5 pb-2">Professional Details</h4>
            
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
                <Shield className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">License Number</p>
                <p className="text-white font-bold font-mono">{profile.license_number || 'Not Provided'}</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
                <Award className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Experience</p>
                <p className="text-white font-bold">{profile.experience_years ? `${profile.experience_years} Years` : 'Not Provided'}</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
                <Calendar className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Member Since</p>
                <p className="text-white font-bold">{new Date(profile.member_since).toLocaleDateString()}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Ambulance Vehicle Details Card */}
      {profile.ambulance_id && (
        <div className="glass-panel rounded-3xl p-8 border border-white/5 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-blue-500/10 rounded-full blur-[100px] pointer-events-none -mr-32 -mt-32"></div>
          
          <div className="flex items-center gap-6 relative z-10 mb-8 pb-8 border-b border-white/5">
            <div className="w-24 h-24 rounded-2xl bg-slate-900 text-emerald-400 flex items-center justify-center border border-white/10 shadow-lg">
              <Truck className="w-12 h-12" />
            </div>
            <div>
              <h3 className="text-3xl font-heading font-extrabold text-white tracking-widest uppercase">{profile.vehicle_number}</h3>
              <span className="text-xs font-bold uppercase tracking-widest text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-lg mt-2 inline-block">
                {profile.ambulance_type || 'Standard Ambulance'}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative z-10">
            <div className="space-y-6">
              <h4 className="text-sm font-extrabold uppercase tracking-wider text-slate-500 border-b border-white/5 pb-2">Vehicle Status</h4>
              
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Current Duty Status</p>
                  <p className="text-white font-bold uppercase tracking-wide">{profile.duty_status || 'Offline'}</p>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
                  <Navigation className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Last Known GPS</p>
                  <p className="text-white font-mono text-sm">
                    {profile.current_latitude && profile.current_longitude 
                      ? `${profile.current_latitude}, ${profile.current_longitude}` 
                      : 'Unknown Location'}
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-6">
              <h4 className="text-sm font-extrabold uppercase tracking-wider text-slate-500 border-b border-white/5 pb-2">Equipment Checklist</h4>
              
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 rounded-xl bg-emerald-500/5 border border-emerald-500/10">
                  <span className="text-sm font-bold text-slate-300">Defibrillator (AED)</span>
                  <CheckCircle className="w-4 h-4 text-emerald-500" />
                </div>
                <div className="flex items-center justify-between p-3 rounded-xl bg-emerald-500/5 border border-emerald-500/10">
                  <span className="text-sm font-bold text-slate-300">Oxygen Cylinders</span>
                  <CheckCircle className="w-4 h-4 text-emerald-500" />
                </div>
                <div className="flex items-center justify-between p-3 rounded-xl bg-emerald-500/5 border border-emerald-500/10">
                  <span className="text-sm font-bold text-slate-300">First Aid Traumas Kit</span>
                  <CheckCircle className="w-4 h-4 text-emerald-500" />
                </div>
                <div className="flex items-center justify-between p-3 rounded-xl bg-emerald-500/5 border border-emerald-500/10">
                  <span className="text-sm font-bold text-slate-300">ECG Monitor</span>
                  <CheckCircle className="w-4 h-4 text-emerald-500" />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Change Password Card */}
      <div className="glass-panel rounded-3xl p-8 border border-white/5 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-red-500/10 rounded-full blur-[100px] pointer-events-none -mr-32 -mt-32"></div>
        <div>
          <h2 className="text-xl font-heading font-extrabold text-white mb-2">Change Password</h2>
          <p className="text-slate-400 text-xs mb-6">Update your account security credentials</p>
        </div>

        <form onSubmit={handleChangePassword} className="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Current Password</label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-xs focus:outline-none focus:border-emerald-500 text-white transition"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-xs focus:outline-none focus:border-emerald-500 text-white transition"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Confirm New Password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-xs focus:outline-none focus:border-emerald-500 text-white transition"
            />
          </div>

          <div className="md:col-span-3 flex justify-end">
            <button
              type="submit"
              className="bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-3 px-6 rounded-xl text-xs transition shadow-md"
            >
              Change Password
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DriverProfile;
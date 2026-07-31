import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { ChevronDown, HelpCircle, Mail, User } from 'lucide-react';

const iconMap: Record<string, any> = {
  ChevronDown, HelpCircle, Mail, User
};

const Profile: React.FC = () => {
  const { showToast, setGlobalLoading, user, updateUserLocal } = useAuth();

  // Profile forms
  const [fullName, setFullName] = useState(user?.full_name || '');
  const [email, setEmail] = useState(user?.email || '');
  
  // Medical file forms
  const [bloodGroup, setBloodGroup] = useState('');
  const [emergencyContactName, setEmergencyContactName] = useState('');
  const [emergencyContactPhone, setEmergencyContactPhone] = useState('');
  const [allergies, setAllergies] = useState('');
  const [chronicConditions, setChronicConditions] = useState('');

  // Change Password forms
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const loadProfileData = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/user/profile.php');
      if (res.data && res.data.success) {
        const data = res.data.data;
        setFullName(data.full_name);
        setEmail(data.email);
        setBloodGroup(data.blood_group || '');
        setEmergencyContactName(data.emergency_contact_name || '');
        setEmergencyContactPhone(data.emergency_contact_phone || '');
        setAllergies(data.allergies || '');
        setChronicConditions(data.chronic_conditions || '');
      }
    } catch (err: any) {
      showToast("Load Failed", err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  useEffect(() => {
    loadProfileData();
  }, []);

  const handleSavePersonal = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalLoading(true);
    try {
      const res = await axios.put('/user/profile.php', {
        full_name: fullName,
        email: email,
      });

      if (res.data && res.data.success) {
        showToast("Profile Updated", "Personal account data saved.", "success");
        if (user) {
          updateUserLocal({ ...user, full_name: fullName, email: email });
        }
      }
    } catch (err: any) {
      showToast("Update Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleSaveMedical = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalLoading(true);
    try {
      const res = await axios.put('/user/profile.php', {
        blood_group: bloodGroup,
        emergency_contact_name: emergencyContactName,
        emergency_contact_phone: emergencyContactPhone,
        allergies: allergies,
        chronic_conditions: chronicConditions,
      });

      if (res.data && res.data.success) {
        showToast("Profile Updated", "Medical emergency file saved.", "success");
      }
    } catch (err: any) {
      showToast("Update Failed", err.response?.data?.message || err.message, "error");
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

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 max-w-4xl mx-auto">
      {/* Personal Settings Card */}
      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Personal Account Information</h2>
          <p className="text-gray-400 text-xs mt-0.5">Manage your credentials and profiles</p>
        </div>

        <form onSubmit={handleSavePersonal} className="space-y-4">
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Account Full Name</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('User', { size: 15 })}
              </span>
              <input
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Email Address</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Mail', { size: 15 })}
              </span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          <button
            type="submit"
            className="bg-accentRed hover:bg-red-700 text-white font-bold py-2.5 px-4 rounded-xl text-xs transition shadow-md"
          >
            Update Account Info
          </button>
        </form>
      </div>

      {/* Medical Info Card */}
      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Responders Emergency File</h2>
          <p className="text-gray-400 text-xs mt-0.5">Responders access this instantly during dispatch calls</p>
        </div>

        <form onSubmit={handleSaveMedical} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Blood Group</label>
              <div className="relative">
                <select
                  value={bloodGroup}
                  onChange={(e) => setBloodGroup(e.target.value)}
                  className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none cursor-pointer"
                >
                  <option value="" className="bg-slate-900">Select Type</option>
                  <option value="A+" className="bg-slate-900">A+</option>
                  <option value="A-" className="bg-slate-900">A-</option>
                  <option value="B+" className="bg-slate-900">B+</option>
                  <option value="B-" className="bg-slate-900">B-</option>
                  <option value="AB+" className="bg-slate-900">AB+</option>
                  <option value="AB-" className="bg-slate-900">AB-</option>
                  <option value="O+" className="bg-slate-900">O+</option>
                  <option value="O-" className="bg-slate-900">O-</option>
                </select>
                <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                  {getLucideIcon('ChevronDown', { size: 14 })}
                </div>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Emergency Phone</label>
              <input
                type="tel"
                value={emergencyContactPhone}
                onChange={(e) => setEmergencyContactPhone(e.target.value)}
                placeholder="Relative phone number"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Primary Emergency Contact Name</label>
            <input
              type="text"
              value={emergencyContactName}
              onChange={(e) => setEmergencyContactName(e.target.value)}
              placeholder="e.g. Spouse/Parent Name"
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Known Medical Allergies</label>
            <textarea
              value={allergies}
              onChange={(e) => setAllergies(e.target.value)}
              placeholder="List food, drug, or chemical allergies..."
              rows={2}
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition resize-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Chronic Illness / Context Conditions</label>
            <textarea
              value={chronicConditions}
              onChange={(e) => setChronicConditions(e.target.value)}
              placeholder="Diabetes, Asthma, Epilepsy, cardiac history..."
              rows={2}
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition resize-none"
            />
          </div>

          <button
            type="submit"
            className="bg-accentRed hover:bg-red-700 text-white font-bold py-2.5 px-4 rounded-xl text-xs transition shadow-md"
          >
            Update Emergency File
          </button>
        </form>
      </div>

      {/* Change Password Card */}
      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4 lg:col-span-2">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Change Password</h2>
          <p className="text-gray-400 text-xs mt-0.5">Update your account security credentials</p>
        </div>

        <form onSubmit={handleChangePassword} className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Current Password</label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
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
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
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
              className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
            />
          </div>

          <div className="md:col-span-3 flex justify-end">
            <button
              type="submit"
              className="bg-accentRed hover:bg-red-700 text-white font-bold py-2.5 px-6 rounded-xl text-xs transition shadow-md"
            >
              Change Password
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Profile;

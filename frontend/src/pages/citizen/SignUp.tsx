import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { Activity, AlertCircle, Award, CalendarX2, ChevronDown, CreditCard, HelpCircle, Lock, LockKeyhole, Mail, Phone, ShieldAlert, ShieldCheck, Stethoscope, Truck, User, UserPlus } from 'lucide-react';

const iconMap: Record<string, any> = {
  Activity, AlertCircle, Award, CalendarX2, ChevronDown, CreditCard, HelpCircle, Lock, LockKeyhole, Mail, Phone, ShieldAlert, ShieldCheck, Stethoscope, Truck, User, UserPlus
};

const SignUp: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();
  const navigate = useNavigate();

  const [name, setName]         = useState('');
  const [phone, setPhone]       = useState('');
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole]         = useState<'user' | 'driver' | 'admin'>('user');
  const [showPassword, setShowPassword] = useState(false);

  // Driver-only fields
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [ambulanceType, setAmbulanceType] = useState<'BLS' | 'ALS' | 'CCU'>('BLS');
  const [driverRole, setDriverRole] = useState('Ambulance Driver');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [licenseExpiry, setLicenseExpiry] = useState('');
  const [experienceYears, setExperienceYears] = useState(0);

  const DRIVER_ROLES = [
    'Ambulance Driver', 'EMT', 'Paramedic', 'First Responder',
    'Critical Care Paramedic', 'Doctor on Wheels', 'Rescue Technician', 'Nurse'
  ];

  const getLucideIcon = (n: string, props: any = {}) => {
    const C = iconMap[n] || iconMap.HelpCircle;
    return <C {...props} />;
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      showToast('Password Mismatch', 'Your passwords do not match. Please re-enter.', 'warning');
      return;
    }
    if (password.length < 6) {
      showToast('Weak Password', 'Password must be at least 6 characters.', 'warning');
      return;
    }

    setGlobalLoading(true);
    try {
      const payload: any = {
        full_name:    name,
        phone_number: phone,
        email:        email,
        password:     password,
        role:         role,
      };

      if (role === 'driver') {
        if (!vehicleNumber || !licenseNumber) {
          showToast('Missing Info', 'Vehicle Number and License are required for drivers.', 'warning');
          setGlobalLoading(false);
          return;
        }
        payload.vehicle_number = vehicleNumber;
        payload.ambulance_type = ambulanceType;
        payload.driver_role = driverRole;
        payload.license_number = licenseNumber;
        payload.license_expiry = licenseExpiry;
        payload.experience_years = experienceYears;
      }

      const res = await axios.post('/auth/register.php', payload);
      if (res.data?.success) {
        showToast('Account Created!', 'Registration successful. Please sign in.', 'success');
        navigate('/login');
      }
    } catch (err: any) {
      showToast('Registration Failed', err.response?.data?.message || err.message, 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex flex-col justify-center p-5 max-w-md mx-auto py-10">

      {/* Header */}
      <div className="text-center space-y-2 mb-7">
        <div className="w-14 h-14 rounded-2xl glass-panel flex items-center justify-center border border-red-500/10 mx-auto text-accentRed shadow-lg">
          {getLucideIcon('ShieldAlert', { size: 30 })}
        </div>
        <h1 className="text-xl font-heading font-extrabold text-white">Create SafeReach Account</h1>
        <p className="text-gray-400 text-xs">Register to access immediate emergency logistics</p>
      </div>

      <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-2xl space-y-4">
        <form onSubmit={handleRegisterSubmit} className="space-y-3.5">

          {/* Full Name */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Full Name</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('User', { size: 15 })}
              </span>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your full name"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          {/* Email */}
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
                placeholder="you@example.com"
                required
                autoComplete="email"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          {/* Phone (optional contact number) */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">
              Phone Number <span className="text-gray-600 font-normal">(optional)</span>
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Phone', { size: 15 })}
              </span>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+91 XXXXX XXXXX"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Password</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('Lock', { size: 15 })}
              </span>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minimum 6 characters"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-10 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-500 hover:text-white transition"
              >
                {getLucideIcon(showPassword ? 'EyeOff' : 'Eye', { size: 14 })}
              </button>
            </div>
          </div>

          {/* Confirm Password */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Confirm Password</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                {getLucideIcon('LockKeyhole', { size: 15 })}
              </span>
              <input
                type={showPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Re-enter password"
                required
                className={`w-full bg-slate-900/50 border rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none text-white transition ${
                  confirmPassword && confirmPassword !== password
                    ? 'border-red-500/50 focus:border-red-500'
                    : 'border-white/10 focus:border-accentRed'
                }`}
              />
              {confirmPassword && confirmPassword !== password && (
                <span className="absolute inset-y-0 right-0 pr-3 flex items-center text-red-400">
                  {getLucideIcon('AlertCircle', { size: 14 })}
                </span>
              )}
            </div>
            {confirmPassword && confirmPassword !== password && (
              <p className="text-[10px] text-red-400 pl-1">Passwords do not match</p>
            )}
          </div>

          {/* Role selector — User / Driver / Admin */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-gray-300">Account Type</label>
            <div className="grid grid-cols-3 gap-2">
              {(
                [
                  { key: 'user',   label: 'User',   icon: 'User'        },
                  { key: 'driver', label: 'Driver', icon: 'Truck'       },
                  { key: 'admin',  label: 'Admin',  icon: 'ShieldCheck' },
                ] as const
              ).map((r) => (
                <button
                  key={r.key}
                  type="button"
                  onClick={() => setRole(r.key)}
                  className={`py-2.5 flex flex-col items-center gap-1 rounded-xl border text-[11px] font-bold transition ${
                    role === r.key
                      ? 'bg-accentRed/10 border-accentRed text-white'
                      : 'border-white/10 bg-slate-900/30 text-gray-400 hover:text-white hover:border-white/20'
                  }`}
                >
                  {getLucideIcon(r.icon, { size: 14 })}
                  {r.label}
                </button>
              ))}
            </div>
          </div>

          {/* Driver-only vehicle fields */}
          {role === 'driver' && (
            <div className="space-y-3 p-3.5 rounded-xl border border-white/5 bg-slate-900/30 animate-slide-in">
              <p className="text-[10px] uppercase font-bold text-gray-500 tracking-wider">Ambulance Details</p>

              <div className="space-y-1">
                <label className="text-[11px] font-semibold text-gray-300">Vehicle Registration Number</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                    {getLucideIcon('Truck', { size: 14 })}
                  </span>
                  <input
                    type="text"
                    value={vehicleNumber}
                    onChange={(e) => setVehicleNumber(e.target.value)}
                    placeholder="e.g. TN-07-SR-1234"
                    required
                    className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-[11px] font-semibold text-gray-300">Ambulance Type</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                    {getLucideIcon('Activity', { size: 14 })}
                  </span>
                  <select
                    value={ambulanceType}
                    onChange={(e: any) => setAmbulanceType(e.target.value)}
                    className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none cursor-pointer"
                  >
                    <option value="BLS">Basic Life Support (BLS)</option>
                    <option value="ALS">Advanced Life Support (ALS)</option>
                    <option value="CCU">Critical Care Unit (CCU)</option>
                  </select>
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                    {getLucideIcon('ChevronDown', { size: 14 })}
                  </div>
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-[11px] font-semibold text-gray-300">Driver Role / Specialization</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                    {getLucideIcon('Stethoscope', { size: 14 })}
                  </span>
                  <select
                    value={driverRole}
                    onChange={(e: any) => setDriverRole(e.target.value)}
                    className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none cursor-pointer"
                  >
                    {DRIVER_ROLES.map(r => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                    {getLucideIcon('ChevronDown', { size: 14 })}
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-[11px] font-semibold text-gray-300">License Number</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                      {getLucideIcon('CreditCard', { size: 14 })}
                    </span>
                    <input
                      type="text"
                      value={licenseNumber}
                      onChange={(e) => setLicenseNumber(e.target.value)}
                      placeholder="e.g. TN-DL-1234"
                      className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-2 text-xs focus:outline-none focus:border-accentRed text-white transition"
                    />
                  </div>
                </div>
                <div className="space-y-1">
                  <label className="text-[11px] font-semibold text-gray-300">Expiry Date</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                      {getLucideIcon('CalendarX2', { size: 14 })}
                    </span>
                    <input
                      type="date"
                      value={licenseExpiry}
                      onChange={(e) => setLicenseExpiry(e.target.value)}
                      className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-2 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none"
                    />
                  </div>
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-[11px] font-semibold text-gray-300">Years of Experience</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-500">
                    {getLucideIcon('Award', { size: 14 })}
                  </span>
                  <input
                    type="number"
                    min="0"
                    value={experienceYears}
                    onChange={(e) => setExperienceYears(parseInt(e.target.value) || 0)}
                    placeholder="e.g. 5"
                    className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs focus:outline-none focus:border-accentRed text-white transition"
                  />
                </div>
              </div>
            </div>
          )}

          <button
            type="submit"
            className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs mt-2"
          >
            {getLucideIcon('UserPlus', { size: 14 })}
            Create Account
          </button>
        </form>

        <div className="flex justify-center items-center text-xs text-gray-400 gap-1.5 pt-1">
          <span>Already have an account?</span>
          <button onClick={() => navigate('/login')} className="text-accentRed hover:underline font-bold">
            Sign In
          </button>
        </div>
      </div>
    </div>
  );
};

export default SignUp;

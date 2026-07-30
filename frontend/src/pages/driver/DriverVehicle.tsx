import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Truck, Navigation, CheckCircle, Activity, Info } from 'lucide-react';

const DriverVehicle: React.FC = () => {
  const { setGlobalLoading } = useAuth();
  const [vehicle, setVehicle] = useState<any>(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/driver/profile.php');
      if (res.data?.success) {
        setVehicle(res.data.data.driver);
      }
    } catch (e) {
      console.warn("Failed to load profile", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  if (!vehicle) return null;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">Ambulance Details</h2>
          <p className="text-sm text-slate-400 mt-1">Information regarding your assigned emergency vehicle.</p>
        </div>
      </div>

      {!vehicle.ambulance_id ? (
        <div className="bg-slate-900 border border-white/5 rounded-3xl p-12 text-center shadow-xl">
          <div className="w-20 h-20 rounded-full bg-slate-800 flex items-center justify-center mx-auto mb-4 text-slate-600">
            <Info className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-extrabold text-white mb-2">No Vehicle Assigned</h3>
          <p className="text-slate-400 max-w-md mx-auto">You do not have an ambulance assigned to you yet. Please contact your dispatch administrator.</p>
        </div>
      ) : (
        <div className="glass-panel rounded-3xl p-8 border border-white/5 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/10 rounded-full blur-[100px] pointer-events-none -mr-32 -mt-32"></div>
          
          <div className="flex items-center gap-6 relative z-10 mb-8 pb-8 border-b border-white/5">
            <div className="w-24 h-24 rounded-2xl bg-slate-900 text-emerald-400 flex items-center justify-center border border-white/10 shadow-lg">
              <Truck className="w-12 h-12" />
            </div>
            <div>
              <h3 className="text-3xl font-heading font-extrabold text-white tracking-widest uppercase">{vehicle.vehicle_number}</h3>
              <span className="text-xs font-bold uppercase tracking-widest text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-lg mt-2 inline-block">
                {vehicle.ambulance_type || 'Standard Ambulance'}
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
                  <p className="text-white font-bold uppercase tracking-wide">{vehicle.duty_status || 'Offline'}</p>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
                  <Navigation className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs text-slate-500 uppercase font-bold tracking-wider mb-0.5">Last Known GPS</p>
                  <p className="text-white font-mono text-sm">{vehicle.current_latitude}, {vehicle.current_longitude}</p>
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
    </div>
  );
};

export default DriverVehicle;
import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Save, Cpu, Database } from 'lucide-react';

const AdminSettings: React.FC = () => {
  const { showToast } = useAuth();
  const [systemName, setSystemName] = useState('SafeReach Operations Control');
  const [smsGateway, setSmsGateway] = useState('Twilio SMS Gateway');
  const [autoDispatch, setAutoDispatch] = useState(true);
  const [maintenanceMode, setMaintenanceMode] = useState(false);
  const [alertDistance, setAlertDistance] = useState(15); // in km

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    showToast('Settings Saved', 'System configurations updated successfully.', 'success');
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h2 className="text-3xl font-heading font-extrabold text-white">System Settings</h2>
        <p className="text-sm text-slate-400 mt-1">Configure dispatcher policies, auto-assignment rules, and system parameter nodes.</p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {/* Operations Config */}
        <div className="bg-slate-900 border border-white/5 rounded-3xl p-6 shadow-xl space-y-6">
          <h3 className="text-base font-extrabold text-white flex items-center gap-2 pb-3 border-b border-white/5">
            <Cpu className="w-5 h-5 text-blue-400" /> Dispatcher Policies
          </h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Command Center Node Title</label>
              <input 
                type="text" 
                value={systemName}
                onChange={(e) => setSystemName(e.target.value)}
                className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">SMS Gateway Node</label>
              <input 
                type="text" 
                value={smsGateway}
                onChange={(e) => setSmsGateway(e.target.value)}
                className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Alert Threshold Radius (KM)</label>
              <input 
                type="number" 
                value={alertDistance}
                onChange={(e) => setAlertDistance(parseInt(e.target.value))}
                className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
              />
            </div>
          </div>
        </div>

        {/* System Rules */}
        <div className="bg-slate-900 border border-white/5 rounded-3xl p-6 shadow-xl space-y-6">
          <h3 className="text-base font-extrabold text-white flex items-center gap-2 pb-3 border-b border-white/5">
            <Database className="w-5 h-5 text-emerald-400" /> System Automation
          </h3>
          
          <div className="space-y-4">
            <div className="flex items-center justify-between p-4 bg-slate-950 rounded-2xl border border-white/5">
              <div>
                <h4 className="font-bold text-white text-sm">AI Assisted Auto-Dispatch</h4>
                <p className="text-xs text-slate-500">Automatically assign the nearest online ambulance to new pending SOS requests.</p>
              </div>
              <input 
                type="checkbox" 
                checked={autoDispatch}
                onChange={(e) => setAutoDispatch(e.target.checked)}
                className="w-5 h-5 accent-emerald-500"
              />
            </div>

            <div className="flex items-center justify-between p-4 bg-slate-950 rounded-2xl border border-white/5">
              <div>
                <h4 className="font-bold text-white text-sm">System Maintenance Mode</h4>
                <p className="text-xs text-slate-500">Pause incoming dispatches and lock citizen dashboards for database upgrade.</p>
              </div>
              <input 
                type="checkbox" 
                checked={maintenanceMode}
                onChange={(e) => setMaintenanceMode(e.target.checked)}
                className="w-5 h-5 accent-red-500"
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button 
            type="submit"
            className="flex items-center gap-2 px-6 py-3 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl transition shadow-lg shadow-emerald-500/20"
          >
            <Save className="w-4 h-4" /> Save System Settings
          </button>
        </div>
      </form>
    </div>
  );
};

export default AdminSettings;
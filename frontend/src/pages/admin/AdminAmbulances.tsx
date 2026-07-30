import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';


const AdminAmbulances: React.FC = () => {
  const { showToast } = useAuth();
  const [ambulances, setAmbulances] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchAmbulances = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`/admin/ambulances.php`);
      if (res.data?.success) {
        setAmbulances(res.data.data);
      }
    } catch (e) {
      showToast('Error', 'Failed to fetch ambulances', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAmbulances();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-heading font-extrabold text-white">Ambulance Fleet</h1>
          <p className="text-xs text-gray-400">Manage your entire fleet of emergency vehicles</p>
        </div>
      </div>

      <div className="glass-panel rounded-2xl border border-white/5 shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-900/60 border-b border-white/5 text-gray-400 font-bold uppercase tracking-wider">
                <th className="py-3 px-4">Vehicle Details</th>
                <th className="py-3 px-4">Assigned Driver</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {loading && ambulances.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-gray-500">Loading fleet...</td>
                </tr>
              ) : ambulances.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-gray-500">No ambulances found.</td>
                </tr>
              ) : (
                ambulances.map((a) => (
                  <tr key={a.ambulance_id} className="hover:bg-white/[0.01] transition text-[11px]">
                    <td className="py-3 px-4">
                      <div className="font-bold text-white font-mono bg-white/5 inline-block px-2 py-0.5 rounded border border-white/10">{a.plate_number}</div>
                      <div className="text-gray-500 mt-0.5">{a.vehicle_model}</div>
                    </td>
                    <td className="py-3 px-4">
                      {a.driver_name ? (
                        <>
                          <div className="font-bold text-blue-400">{a.driver_name}</div>
                          <div className="text-gray-500 font-mono mt-0.5">{a.driver_phone}</div>
                        </>
                      ) : (
                        <span className="text-gray-500 italic">Unassigned</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border ${
                        a.status === 'available' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                        a.status === 'busy' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                        a.status === 'maintenance' ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' :
                        'bg-slate-800 text-gray-400 border-white/10'
                      }`}>
                        {a.status || 'offline'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <button className="px-3 py-1 bg-white/5 hover:bg-white/10 text-white rounded-lg font-bold transition">
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminAmbulances;
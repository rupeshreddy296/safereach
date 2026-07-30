import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { CheckCircle, XCircle } from 'lucide-react';

const AdminDrivers: React.FC = () => {
  const { showToast } = useAuth();
  const [drivers, setDrivers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchDrivers = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`/admin/driver_management.php`);
      if (res.data?.success) {
        setDrivers(res.data.data);
      }
    } catch (e) {
      showToast('Error', 'Failed to fetch drivers', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDrivers();
  }, []);

  const handleStatusChange = async (userId: number, newStatus: string) => {
    try {
      const res = await axios.put('/admin/driver_management.php', {
        user_id: userId,
        action: newStatus
      });
      if (res.data?.success) {
        showToast('Success', `Driver status updated to ${newStatus}`, 'success');
        fetchDrivers();
      }
    } catch (e) {
      showToast('Action Failed', 'Could not update driver status', 'error');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-heading font-extrabold text-white">Driver Management</h1>
          <p className="text-xs text-gray-400">Verify, activate, and manage emergency drivers</p>
        </div>
      </div>

      <div className="glass-panel rounded-2xl border border-white/5 shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-900/60 border-b border-white/5 text-gray-400 font-bold uppercase tracking-wider">
                <th className="py-3 px-4">Driver Profile</th>
                <th className="py-3 px-4">Contact</th>
                <th className="py-3 px-4">Assigned Ambulance</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {loading && drivers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-10 text-center text-gray-500">Loading drivers...</td>
                </tr>
              ) : drivers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-10 text-center text-gray-500">No drivers found.</td>
                </tr>
              ) : (
                drivers.map((d) => (
                  <tr key={d.user_id} className="hover:bg-white/[0.01] transition text-[11px]">
                    <td className="py-3 px-4 flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center font-bold text-gray-400 border border-white/10">
                        {d.full_name?.charAt(0) || 'D'}
                      </div>
                      <div>
                        <div className="font-bold text-white">{d.full_name}</div>
                        <div className="text-gray-500 font-mono mt-0.5 text-[9px]">ID: {d.user_id}</div>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="text-gray-300">{d.phone_number}</div>
                      <div className="text-gray-500 mt-0.5">{d.email}</div>
                    </td>
                    <td className="py-3 px-4">
                      {d.plate_number ? (
                        <span className="font-bold text-blue-400 font-mono bg-blue-500/10 px-2 py-0.5 rounded border border-blue-500/20">
                          {d.plate_number}
                        </span>
                      ) : (
                        <span className="text-gray-500 italic">Unassigned</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border ${
                        d.status === 'active' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                        d.status === 'suspended' ? 'bg-red-500/10 text-red-400 border-red-500/20' :
                        'bg-slate-800 text-gray-400 border-white/10'
                      }`}>
                        {d.status || 'inactive'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right space-x-2">
                      {d.status !== 'active' && (
                        <button 
                          onClick={() => handleStatusChange(d.user_id, 'active')}
                          className="px-2 py-1 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 rounded flex items-center gap-1 inline-flex transition"
                        >
                          <CheckCircle size={12} /> Activate
                        </button>
                      )}
                      {d.status !== 'suspended' && (
                        <button 
                          onClick={() => handleStatusChange(d.user_id, 'suspended')}
                          className="px-2 py-1 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded flex items-center gap-1 inline-flex transition"
                        >
                          <XCircle size={12} /> Suspend
                        </button>
                      )}
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

export default AdminDrivers;
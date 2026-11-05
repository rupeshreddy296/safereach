import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { CheckCircle, XCircle, User, Mail, Phone, Shield, Award, Calendar, Truck, X } from 'lucide-react';

const AdminDrivers: React.FC = () => {
  const { showToast } = useAuth();
  const [drivers, setDrivers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDriver, setSelectedDriver] = useState<any | null>(null);

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
        if (selectedDriver && selectedDriver.user_id === userId) {
          setSelectedDriver((prev: any) => ({ ...prev, status: newStatus }));
        }
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
                        <button
                          onClick={() => setSelectedDriver(d)}
                          className="font-bold text-white hover:text-blue-400 hover:underline text-left focus:outline-none transition block"
                        >
                          {d.full_name}
                        </button>
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
                      <button 
                        onClick={() => setSelectedDriver(d)}
                        className="px-2 py-1 bg-slate-900 hover:bg-slate-850 text-white rounded border border-white/5 inline-flex transition items-center gap-1"
                      >
                        Profile
                      </button>
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

      {/* Driver Profile Detail Modal */}
      {selectedDriver && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="glass-panel w-full max-w-lg rounded-3xl border border-white/10 overflow-hidden shadow-2xl animate-fade-in">
            {/* Modal Header */}
            <div className="flex justify-between items-center px-6 py-4 bg-slate-900 border-b border-white/5">
              <h2 className="text-lg font-heading font-extrabold text-white flex items-center gap-2">
                <User className="text-blue-400 w-5 h-5" /> Driver Credentials Profile
              </h2>
              <button 
                onClick={() => setSelectedDriver(null)}
                className="p-1.5 hover:bg-white/5 rounded-lg text-gray-400 hover:text-white transition"
              >
                <X size={18} />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-5">
              {/* Profile Card Header */}
              <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-950/50 border border-white/5">
                <div className="w-14 h-14 rounded-2xl bg-blue-500/10 border border-blue-500/25 flex items-center justify-center text-xl font-black text-blue-400">
                  {selectedDriver.full_name?.charAt(0) || 'D'}
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-white">{selectedDriver.full_name}</h3>
                  <div className="flex items-center gap-2 mt-1.5">
                    <span className="text-[9px] text-gray-500 font-mono bg-slate-900 px-2 py-0.5 rounded border border-white/5">ID: {selectedDriver.user_id}</span>
                    <span className={`px-2 py-0.5 rounded text-[9px] font-bold uppercase border ${
                      selectedDriver.status === 'active' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      selectedDriver.status === 'suspended' ? 'bg-red-500/10 text-red-400 border-red-500/20' :
                      'bg-slate-800 text-gray-400 border-white/10'
                    }`}>
                      {selectedDriver.status}
                    </span>
                  </div>
                </div>
              </div>

              {/* Grid of Profile Details */}
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Mail size={12} className="text-blue-400" /> Email Address
                  </div>
                  <p className="text-xs text-white font-medium truncate">{selectedDriver.email}</p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Phone size={12} className="text-blue-400" /> Mobile Number
                  </div>
                  <p className="text-xs text-white font-medium">{selectedDriver.phone_number}</p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Award size={12} className="text-blue-400" /> License Certificate
                  </div>
                  <p className="text-xs text-white font-bold font-mono">{selectedDriver.license_number || 'N/A'}</p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Calendar size={12} className="text-blue-400" /> License Expiration
                  </div>
                  <p className="text-xs text-white font-medium">{selectedDriver.license_expiry || 'N/A'}</p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Shield size={12} className="text-blue-400" /> Professional Experience
                  </div>
                  <p className="text-xs text-white font-bold">{selectedDriver.experience_years !== null && selectedDriver.experience_years !== undefined ? `${selectedDriver.experience_years} Years` : 'N/A'}</p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5">
                  <div className="flex items-center gap-1.5 text-gray-400 text-[10px] uppercase font-bold tracking-wider mb-1">
                    <Truck size={12} className="text-blue-400" /> Ambulance Level
                  </div>
                  <p className="text-xs text-white font-bold font-mono text-emerald-400">{selectedDriver.ambulance_type || 'N/A'}</p>
                </div>
              </div>

              {/* Assigned Vehicle Details */}
              <div className="p-4 rounded-xl bg-slate-900/40 border border-white/5 flex items-center justify-between text-xs">
                <span className="text-gray-400">Assigned Emergency Vehicle Plate:</span>
                <span className="font-bold text-blue-400 font-mono bg-blue-500/10 px-3 py-1 rounded border border-blue-500/20">
                  {selectedDriver.plate_number || 'Unassigned'}
                </span>
              </div>
            </div>

            {/* Modal Footer */}
            <div className="flex justify-end px-6 py-4 bg-slate-900/80 border-t border-white/5">
              <button 
                onClick={() => setSelectedDriver(null)}
                className="px-4 py-2 bg-slate-850 hover:bg-slate-800 border border-white/5 rounded-xl text-xs font-bold text-white transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDrivers;
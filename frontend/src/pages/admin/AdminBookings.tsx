import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';

import { Clock } from 'lucide-react';

const AdminBookings: React.FC = () => {
  const { showToast } = useAuth();
  const [requests, setRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`/admin/requests_management.php?filter=${filter}`);
      if (res.data?.success) {
        setRequests(res.data.data);
      }
    } catch (e) {
      showToast('Error', 'Failed to fetch bookings', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
    const interval = setInterval(fetchRequests, 10000); // Live update every 10s
    return () => clearInterval(interval);
  }, [filter]);

  const handleAction = async (requestId: number, action: string, driverId?: number) => {
    try {
      const payload: any = { request_id: requestId, action };
      if (driverId) payload.driver_id = driverId;
      
      const res = await axios.put('/admin/requests_management.php', payload);
      if (res.data?.success) {
        showToast('Success', 'Action completed successfully', 'success');
        fetchRequests();
      }
    } catch (e) {
      showToast('Action Failed', 'Could not complete the action', 'error');
    }
  };

  const getStatusBadge = (status: string) => {
    let color = 'bg-slate-800 text-gray-400 border-white/10';
    if (status === 'pending') color = 'bg-amber-500/10 text-amber-400 border-amber-500/20';
    if (status === 'accepted') color = 'bg-blue-500/10 text-blue-400 border-blue-500/20';
    if (status === 'driver_assigned' || status === 'driver_arriving') color = 'bg-purple-500/10 text-purple-400 border-purple-500/20';
    if (status === 'patient_picked_up' || status === 'hospital_reached') color = 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20';
    if (status === 'completed') color = 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
    if (status === 'cancelled') color = 'bg-red-500/10 text-red-400 border-red-500/20';
    
    return (
      <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border ${color}`}>
        {status.replace('_', ' ')}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-heading font-extrabold text-white">Booking Requests</h1>
          <p className="text-xs text-gray-400">Live monitoring of all ambulance dispatches</p>
        </div>
        <div className="flex gap-2">
          <select 
            value={filter} 
            onChange={(e) => setFilter(e.target.value)}
            className="bg-slate-900/50 border border-white/10 rounded-xl py-2 px-4 text-xs text-white focus:outline-none focus:border-white/20"
          >
            <option value="all">All Time</option>
            <option value="today">Today</option>
            <option value="active">Active Only</option>
          </select>
        </div>
      </div>

      <div className="glass-panel rounded-2xl border border-white/5 shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-900/60 border-b border-white/5 text-gray-400 font-bold uppercase tracking-wider">
                <th className="py-3 px-4">Request ID & Time</th>
                <th className="py-3 px-4">Patient Details</th>
                <th className="py-3 px-4">Emergency Type</th>
                <th className="py-3 px-4">Assigned Driver</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {loading && requests.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-10 text-center text-gray-500">Loading requests...</td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-10 text-center text-gray-500">No requests found.</td>
                </tr>
              ) : (
                requests.map((r) => (
                  <tr key={r.request_id} className="hover:bg-white/[0.01] transition text-[11px]">
                    <td className="py-3 px-4">
                      <div className="font-bold text-white font-mono text-[12px]">#{r.request_id}</div>
                      <div className="text-gray-500 flex items-center gap-1 mt-0.5">
                        <Clock size={10} /> {new Date(r.created_at).toLocaleString()}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-bold text-gray-200">{r.patient_name || 'Unknown'}</div>
                      <div className="text-gray-500 font-mono mt-0.5">{r.patient_phone || 'N/A'}</div>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`font-bold ${r.emergency_type === 'SOS' ? 'text-accentRed' : 'text-blue-400'}`}>
                        {r.emergency_type}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      {r.driver_name ? (
                        <>
                          <div className="font-bold text-emerald-400">{r.driver_name}</div>
                          <div className="text-gray-500 font-mono mt-0.5">{r.driver_phone}</div>
                        </>
                      ) : (
                        <span className="text-gray-500 italic">Unassigned</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">
                      {getStatusBadge(r.status)}
                    </td>
                    <td className="py-3 px-4 text-right space-x-2">
                      {r.status !== 'completed' && r.status !== 'cancelled' && (
                        <button 
                          onClick={() => handleAction(r.request_id, 'cancel')}
                          className="px-3 py-1 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-lg font-bold transition"
                        >
                          Cancel
                        </button>
                      )}
                      <button className="px-3 py-1 bg-white/5 hover:bg-white/10 text-white rounded-lg font-bold transition">
                        View
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

export default AdminBookings;
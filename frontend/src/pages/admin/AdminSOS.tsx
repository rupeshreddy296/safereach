import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Clock, MapPin, Activity, Phone, Search, Trash2 } from 'lucide-react';

const AdminSOS: React.FC = () => {
  const { setGlobalLoading, showToast } = useAuth();
  const [requests, setRequests] = useState<any[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');

  const fetchRequests = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/requests/history.php');
      if (res.data?.success) {
        setRequests(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to fetch SOS requests", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleCancelRequest = async (id: number) => {
    if (!confirm(`Are you sure you want to cancel request #${id}?`)) return;
    setGlobalLoading(true);
    try {
      const res = await axios.post('/requests/update_status.php', {
        request_id: id,
        status: 'cancelled'
      });
      if (res.data?.success) {
        showToast('Cancelled Successfully', `Emergency request #${id} has been cancelled.`, 'success');
        fetchRequests();
      }
    } catch (e: any) {
      showToast('Action Failed', e.response?.data?.message || 'Failed to update status', 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  const filteredRequests = requests.filter(req => {
    const matchesSearch = 
      (req.patient_name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (req.pickup_address || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (req.emergency_type || '').toLowerCase().includes(searchQuery.toLowerCase());
      
    const matchesStatus = filterStatus === 'all' || req.status === filterStatus;
    
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">SOS & Emergency Logs</h2>
          <p className="text-sm text-slate-400 mt-1">Manage and audit incoming critical ambulance dispatch bookings.</p>
        </div>
      </div>

      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/50 p-4 rounded-2xl border border-white/5">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input 
            type="text" 
            placeholder="Search by patient, address, or emergency type..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
          />
        </div>
        <div className="flex items-center gap-3">
          <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Status Filter:</label>
          <select 
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="bg-slate-950 border border-white/5 rounded-xl py-2 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
          >
            <option value="all">All Dispatches</option>
            <option value="pending">Pending</option>
            <option value="accepted">Accepted</option>
            <option value="arrived">Arrived</option>
            <option value="boarded">Boarded</option>
            <option value="completed">Completed</option>
            <option value="cancelled">Cancelled</option>
          </select>
        </div>
      </div>

      <div className="bg-slate-900 border border-white/5 rounded-3xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="text-xs uppercase bg-slate-950/50 text-slate-500 font-bold tracking-wider">
              <tr>
                <th className="px-6 py-4">ID</th>
                <th className="px-6 py-4">Patient</th>
                <th className="px-6 py-4">Emergency Type</th>
                <th className="px-6 py-4">Address Details</th>
                <th className="px-6 py-4">Responder Status</th>
                <th className="px-6 py-4">Timestamp</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredRequests.length > 0 ? filteredRequests.map((req) => (
                <tr key={req.request_id} className="hover:bg-white/5 transition-colors">
                  <td className="px-6 py-4 font-mono text-red-500 font-bold">#{req.request_id}</td>
                  <td className="px-6 py-4">
                    <div className="font-bold text-white">{req.patient_name || req.requester_name}</div>
                    <div className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
                      <Phone className="w-3 h-3" /> {req.patient_phone || 'N/A'}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-xs uppercase bg-red-500/10 text-red-400 border border-red-500/20 px-2.5 py-1 rounded-lg font-bold">
                      {req.emergency_type}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-1 text-xs text-slate-300">
                        <MapPin className="w-3 h-3 text-red-400 shrink-0" />
                        <span className="truncate max-w-[200px]">{req.pickup_address}</span>
                      </div>
                      {req.hospital_name && (
                        <div className="flex items-center gap-1 text-xs text-slate-400">
                          <Activity className="w-3 h-3 text-emerald-400 shrink-0" />
                          <span>{req.hospital_name}</span>
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex flex-col gap-1">
                      <span className={`w-fit px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider border ${
                        req.status === 'completed' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                        req.status === 'cancelled' ? 'bg-red-500/10 text-red-400 border-red-500/20' :
                        'bg-blue-500/10 text-blue-400 border-blue-500/20'
                      }`}>
                        {req.status}
                      </span>
                      {req.driver_name && (
                        <span className="text-[10px] text-slate-500">Driver: {req.driver_name} ({req.vehicle_number})</span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="block text-white font-bold">{new Date(req.created_at).toLocaleDateString()}</span>
                    <span className="text-[10px] text-slate-500">{new Date(req.created_at).toLocaleTimeString()}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {!['completed', 'cancelled'].includes(req.status) && (
                      <button 
                        onClick={() => handleCancelRequest(req.request_id)}
                        className="p-2 bg-red-500/10 text-red-400 hover:bg-red-500/20 rounded-xl transition border border-red-500/10"
                        title="Cancel Dispatch"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                    <Clock className="w-10 h-10 mx-auto mb-3 opacity-50" />
                    <p>No dispatches matching filter criteria.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminSOS;
import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Clock, MapPin, Activity, Star } from 'lucide-react';

const DriverHistory: React.FC = () => {
  const { setGlobalLoading } = useAuth();
  const [history, setHistory] = useState<any[]>([]);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/requests/history.php');
      if (res.data?.success) {
        setHistory(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to load history", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-heading font-extrabold text-white">Trip History</h2>
          <p className="text-sm text-slate-400 mt-1">Review your past emergency dispatches.</p>
        </div>
      </div>

      <div className="bg-slate-900 border border-white/5 rounded-3xl shadow-xl overflow-hidden">
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="text-xs uppercase bg-slate-950/50 text-slate-500 font-bold tracking-wider">
              <tr>
                <th className="px-6 py-4">Request ID</th>
                <th className="px-6 py-4">Date</th>
                <th className="px-6 py-4">Patient</th>
                <th className="px-6 py-4">Route</th>
                <th className="px-6 py-4">Rating</th>
                <th className="px-6 py-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {history.length > 0 ? history.map((req) => (
                <tr key={req.request_id} className="hover:bg-white/5 transition-colors">
                  <td className="px-6 py-4 font-mono text-emerald-400 font-bold">#{req.request_id}</td>
                  <td className="px-6 py-4">
                    <span className="block text-white font-bold">{new Date(req.created_at).toLocaleDateString()}</span>
                    <span className="text-[10px] text-slate-500">{new Date(req.created_at).toLocaleTimeString()}</span>
                  </td>
                  <td className="px-6 py-4 font-bold text-white">{req.patient_name || req.requester_name}</td>
                  <td className="px-6 py-4">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-1.5 text-xs text-slate-300">
                        <MapPin className="w-3 h-3 text-red-400" />
                        <span className="truncate max-w-[150px]">{req.pickup_address}</span>
                      </div>
                      {req.hospital_name && (
                        <div className="flex items-center gap-1.5 text-xs text-slate-300">
                          <Activity className="w-3 h-3 text-emerald-400" />
                          <span className="truncate max-w-[150px]">{req.hospital_name}</span>
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    {req.rating ? (
                      <div className="flex items-center gap-1 text-amber-400 font-bold">
                        <Star className="w-4 h-4 fill-amber-400" /> {req.rating}.0
                      </div>
                    ) : (
                      <span className="text-slate-600 text-xs">-</span>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider border ${
                      req.status === 'completed' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      req.status === 'cancelled' ? 'bg-red-500/10 text-red-400 border-red-500/20' :
                      'bg-blue-500/10 text-blue-400 border-blue-500/20'
                    }`}>
                      {req.status}
                    </span>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-500">
                    <Clock className="w-10 h-10 mx-auto mb-3 opacity-50" />
                    <p>No trip history found.</p>
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

export default DriverHistory;
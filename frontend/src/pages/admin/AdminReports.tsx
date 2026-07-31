import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { FileText, Printer, Calendar, ShieldCheck, Star } from 'lucide-react';

const AdminReports: React.FC = () => {
  const { setGlobalLoading } = useAuth();
  const [requests, setRequests] = useState<any[]>([]);

  const fetchHistory = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/requests/history.php');
      if (res.data?.success) {
        setRequests(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to load report data", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const totalTrips = requests.length;
  const completedTrips = requests.filter(r => r.status === 'completed').length;
  const cancelledTrips = requests.filter(r => r.status === 'cancelled').length;
  const ratings = requests.filter(r => r.rating !== null).map(r => r.rating);
  const averageRating = ratings.length > 0 ? (ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1) : 'N/A';

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center print:hidden">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">System Reports</h2>
          <p className="text-sm text-slate-400 mt-1">Generate and print system performance audit statements.</p>
        </div>
        <button 
          onClick={handlePrint}
          className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-bold rounded-xl transition shadow-lg"
        >
          <Printer className="w-4 h-4" /> Print Statement
        </button>
      </div>

      {/* Printable Report Header (Visible only when printing) */}
      <div className="hidden print:block text-center border-b pb-6 mb-8 border-slate-300">
        <h1 className="text-2xl font-bold text-slate-900">SafeReach Emergency Response Network</h1>
        <p className="text-xs text-slate-500 mt-1">Confidential Operational Audit Report</p>
        <p className="text-xs text-slate-500">Report Generated: {new Date().toLocaleString()}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-slate-900 border border-white/5 p-6 rounded-2xl shadow-xl flex items-center justify-between print:border-slate-300 print:text-black">
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider mb-1">Total Bookings</p>
            <h3 className="text-3xl font-extrabold text-white print:text-black">{totalTrips}</h3>
          </div>
          <FileText className="w-8 h-8 text-blue-400 opacity-80 print:hidden" />
        </div>
        <div className="bg-slate-900 border border-white/5 p-6 rounded-2xl shadow-xl flex items-center justify-between print:border-slate-300 print:text-black">
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider mb-1">Completed Dispatches</p>
            <h3 className="text-3xl font-extrabold text-emerald-400">{completedTrips}</h3>
          </div>
          <ShieldCheck className="w-8 h-8 text-emerald-400 opacity-80 print:hidden" />
        </div>
        <div className="bg-slate-900 border border-white/5 p-6 rounded-2xl shadow-xl flex items-center justify-between print:border-slate-300 print:text-black">
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider mb-1">Cancelled Requests</p>
            <h3 className="text-3xl font-extrabold text-red-400">{cancelledTrips}</h3>
          </div>
          <Calendar className="w-8 h-8 text-red-400 opacity-80 print:hidden" />
        </div>
        <div className="bg-slate-900 border border-white/5 p-6 rounded-2xl shadow-xl flex items-center justify-between print:border-slate-300 print:text-black">
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider mb-1">Avg Patient Rating</p>
            <h3 className="text-3xl font-extrabold text-amber-400">{averageRating} ★</h3>
          </div>
          <Star className="w-8 h-8 text-amber-400 opacity-80 print:hidden" />
        </div>
      </div>

      <div className="bg-slate-900 border border-white/5 rounded-3xl overflow-hidden shadow-2xl print:bg-white print:border-slate-300">
        <div className="p-6 border-b border-white/5 print:border-slate-300 flex justify-between items-center">
          <h3 className="font-extrabold text-white print:text-black">Emergency Dispatch History</h3>
          <span className="text-[10px] bg-slate-950 px-3 py-1.5 rounded-lg border border-white/5 text-slate-500 font-bold uppercase tracking-widest print:hidden">
            Audit Ready
          </span>
        </div>
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left text-sm text-gray-300 print:text-black">
            <thead className="text-xs uppercase bg-slate-950/50 text-slate-500 font-bold tracking-wider print:bg-slate-100">
              <tr>
                <th className="px-6 py-4">Request ID</th>
                <th className="px-6 py-4">Patient</th>
                <th className="px-6 py-4">Location</th>
                <th className="px-6 py-4">Ambulance</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 print:divide-slate-300">
              {requests.length > 0 ? requests.map((req) => (
                <tr key={req.request_id} className="hover:bg-white/5 transition-colors print:hover:bg-transparent">
                  <td className="px-6 py-4 font-mono font-bold text-blue-400">#{req.request_id}</td>
                  <td className="px-6 py-4 font-bold text-white print:text-black">{req.patient_name || req.requester_name}</td>
                  <td className="px-6 py-4 text-xs max-w-[200px] truncate">{req.pickup_address}</td>
                  <td className="px-6 py-4 font-mono text-xs">{req.vehicle_number || 'N/A'}</td>
                  <td className="px-6 py-4 uppercase font-bold text-xs">{req.status}</td>
                  <td className="px-6 py-4 text-slate-400 print:text-slate-600">{new Date(req.created_at).toLocaleDateString()}</td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-slate-500">No logs found in registry.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminReports;
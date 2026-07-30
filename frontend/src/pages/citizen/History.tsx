import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { Star, Loader2, RefreshCw, ClipboardList, HelpCircle } from 'lucide-react';

const iconMap: Record<string, React.ElementType> = {
  Star, Loader2, RefreshCw, ClipboardList, HelpCircle
};

interface HistoryLog {
  request_id: number;
  created_at: string;
  status: string;
  patient_name: string;
  emergency_type: string;
  vehicle_number: string | null;
  requested_ambulance_type: string;
  hospital_name: string | null;
  rating: number | null;
  comments: string | null;
}

const History: React.FC = () => {
  const { showToast } = useAuth();
  const [logs, setLogs] = useState<HistoryLog[]>([]);
  const [loading, setLoading] = useState(false);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const loadHistory = async () => {
    setLoading(true);
    try {
      const res = await axios.get('/requests/history.php');
      if (res.data && res.data.success) {
        setLogs(res.data.data);
      }
    } catch (err: any) {
      showToast('History Error', err.response?.data?.message || err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const renderStars = (rating: number | null) => {
    if (!rating) return null;
    return (
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((star) => (
          <span key={star} className={star <= rating ? 'text-amber-400' : 'text-gray-700'}>
            {getLucideIcon('Star', { size: 13, fill: star <= rating ? 'currentColor' : 'none' })}
          </span>
        ))}
      </div>
    );
  };

  const statusConfig: Record<string, { color: string; label: string }> = {
    pending:   { color: 'bg-amber-500/10  border-amber-500/20  text-amber-400',  label: 'PENDING'   },
    accepted:  { color: 'bg-blue-500/10   border-blue-500/20   text-blue-400',   label: 'ACCEPTED'  },
    arrived:   { color: 'bg-purple-500/10 border-purple-500/20 text-purple-400', label: 'ARRIVED'   },
    boarded:   { color: 'bg-cyan-500/10   border-cyan-500/20   text-cyan-400',   label: 'BOARDED'   },
    completed: { color: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400', label: 'COMPLETED' },
    cancelled: { color: 'bg-red-500/10    border-red-500/20    text-red-400',    label: 'CANCELLED' },
  };

  return (
    <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center pb-3 border-b border-white/5">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Emergency Requests History</h2>
          <p className="text-gray-400 text-xs mt-0.5">Complete log of past medical dispatch calls</p>
        </div>
        <button
          onClick={loadHistory}
          disabled={loading}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-900 hover:bg-slate-800 border border-white/5 rounded-xl text-[10px] font-bold text-white transition disabled:opacity-50"
        >
          {getLucideIcon(loading ? 'Loader2' : 'RefreshCw', {
            size: 12,
            className: loading ? 'animate-spin' : '',
          })}
          {loading ? 'Refreshing...' : 'Refresh Logs'}
        </button>
      </div>

      {/* List */}
      <div className="space-y-4">
        {loading && logs.length === 0 ? (
          <div className="flex justify-center py-12">
            {getLucideIcon('Loader2', { size: 28, className: 'text-accentRed animate-spin' })}
          </div>
        ) : logs.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-14 text-center">
            {getLucideIcon('ClipboardList', { size: 36, className: 'text-gray-700' })}
            <p className="text-sm text-gray-500 font-semibold">No emergency history found.</p>
            <p className="text-xs text-gray-600">Past dispatch requests will appear here.</p>
          </div>
        ) : (
          logs.map((log) => {
            const cfg = statusConfig[log.status] || { color: 'bg-slate-700 text-gray-300', label: log.status.toUpperCase() };
            return (
              <div
                key={log.request_id}
                className="glass-card rounded-xl p-5 border border-white/5 space-y-4 hover:border-white/10 transition"
              >
                {/* Row 1: ID + Status + Date */}
                <div className="flex justify-between items-start gap-4">
                  <div className="space-y-0.5">
                    <span className="text-[10px] font-mono font-bold text-accentRed">
                      CALL #{log.request_id}
                    </span>
                    <p className="text-[10px] text-gray-500">
                      {new Date(log.created_at).toLocaleString('en-IN', {
                        day: '2-digit', month: 'short', year: 'numeric',
                        hour: '2-digit', minute: '2-digit',
                      })}
                    </p>
                  </div>
                  <span className={`text-[9px] uppercase font-extrabold px-2.5 py-1 rounded-lg border ${cfg.color}`}>
                    {cfg.label}
                  </span>
                </div>

                {/* Row 2: Info Grid */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-[11px]">
                  <div className="space-y-0.5">
                    <span className="text-[9px] uppercase font-bold text-gray-500 tracking-wider">Patient</span>
                    <p className="text-white font-semibold">{log.patient_name}</p>
                  </div>
                  <div className="space-y-0.5">
                    <span className="text-[9px] uppercase font-bold text-gray-500 tracking-wider">Emergency Type</span>
                    <p className="text-white font-semibold leading-snug">{log.emergency_type || '—'}</p>
                  </div>
                  <div className="space-y-0.5">
                    <span className="text-[9px] uppercase font-bold text-gray-500 tracking-wider">Ambulance Unit</span>
                    <p className="text-white font-semibold">
                      {log.vehicle_number || 'N/A'}
                      {log.requested_ambulance_type && (
                        <span className="ml-1.5 text-[9px] font-bold bg-blue-500/10 border border-blue-500/20 text-blue-400 px-1.5 py-0.5 rounded">
                          {log.requested_ambulance_type}
                        </span>
                      )}
                    </p>
                  </div>
                  <div className="space-y-0.5">
                    <span className="text-[9px] uppercase font-bold text-gray-500 tracking-wider">Hospital</span>
                    <p className="text-white font-semibold">{log.hospital_name || 'N/A'}</p>
                  </div>
                </div>

                {/* Row 3: Rating + Comments */}
                {(log.rating || log.comments) && (
                  <div className="pt-3 border-t border-white/5 space-y-2">
                    {log.rating && (
                      <div className="flex items-center gap-2">
                        <span className="text-[9px] uppercase font-bold text-gray-500 tracking-wider">Care Rating:</span>
                        {renderStars(log.rating)}
                        <span className="text-[10px] text-amber-400 font-bold">{log.rating}/5</span>
                      </div>
                    )}
                    {log.comments && (
                      <p className="text-[10px] text-gray-400 italic leading-relaxed">
                        "{log.comments}"
                      </p>
                    )}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default History;

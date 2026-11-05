import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { AlertTriangle, CheckCircle, CheckSquare, Eye, HelpCircle, Info } from 'lucide-react';

const iconMap: Record<string, any> = {
  AlertTriangle, CheckCircle, CheckSquare, Eye, HelpCircle, Info
};

const DriverNotifications: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();
  const [alerts, setAlerts] = useState<any[]>([]);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const loadAlerts = async () => {
    try {
      const res = await axios.get('/user/notifications.php');
      if (res.data && res.data.success) {
        setAlerts(res.data.data);
      }
    } catch (e: any) {
      showToast("Error loading alerts", e.message, "error");
    }
  };

  useEffect(() => {
    loadAlerts();
  }, []);

  const handleMarkAsRead = async (notiId: number | null) => {
    setGlobalLoading(true);
    try {
      await axios.put('/user/notifications.php', { notification_id: notiId });
      loadAlerts();
      showToast("Notification Updated", "Alert state marked as read.", "success");
    } catch (err: any) {
      showToast("Update Failed", err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl max-w-2xl mx-auto space-y-6">
      <div className="flex justify-between items-center pb-2 border-b border-white/5">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Alerts & System Notifications</h2>
          <p className="text-gray-400 text-xs mt-0.5">Real-time status updates and dispatcher messages</p>
        </div>
        {alerts.some((a) => !a.is_read) && (
          <button
            onClick={() => handleMarkAsRead(null)}
            className="px-3 py-1.5 bg-slate-900 hover:bg-slate-850 border border-white/5 rounded-xl text-[10px] font-bold text-white transition flex items-center gap-1.5"
          >
            {getLucideIcon('CheckSquare', { size: 12 })} Mark All Read
          </button>
        )}
      </div>

      <div className="space-y-3.5 max-h-[500px] overflow-y-auto pr-1">
        {alerts.length === 0 ? (
          <p className="text-xs text-gray-500 text-center py-10">No alert notifications found.</p>
        ) : (
          alerts.map((note) => {
            const isUnread = !note.is_read;
            const notiType = note.type || 'info';

            return (
              <div
                key={note.notification_id}
                className={`glass-card rounded-xl p-4 border border-white/5 flex gap-4 transition relative overflow-hidden ${
                  isUnread ? 'bg-white/[0.02]' : 'opacity-70'
                }`}
              >
                {isUnread && (
                  <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500"></div>
                )}

                <div className={`p-2.5 rounded-lg flex items-center justify-center shrink-0 ${
                  notiType === 'alert'
                    ? 'bg-red-500/10 text-red-400'
                    : notiType === 'success'
                    ? 'bg-emerald-500/10 text-emerald-400'
                    : 'bg-blue-500/10 text-blue-400'
                }`}>
                  {notiType === 'alert' && getLucideIcon('AlertTriangle', { size: 18 })}
                  {notiType === 'success' && getLucideIcon('CheckCircle', { size: 18 })}
                  {notiType !== 'alert' && notiType !== 'success' && getLucideIcon('Info', { size: 18 })}
                </div>

                <div className="flex-1 space-y-1">
                  <div className="flex justify-between items-start gap-4">
                    <h4 className="text-xs font-extrabold text-white leading-snug">{note.title}</h4>
                    <span className="text-[9px] text-gray-500 whitespace-nowrap">
                      {new Date(note.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p className="text-[10px] text-gray-400 leading-relaxed">{note.description}</p>
                </div>

                {isUnread && (
                  <button
                    onClick={() => handleMarkAsRead(note.notification_id)}
                    className="self-center p-2 text-gray-500 hover:text-white transition focus:outline-none"
                    title="Mark as read"
                  >
                    {getLucideIcon('Eye', { size: 16 })}
                  </button>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default DriverNotifications;
import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Bell, Search, Send, Clock, X, Info, AlertTriangle, CheckCircle } from 'lucide-react';

const AdminNotifications: React.FC = () => {
  const { setGlobalLoading, showToast } = useAuth();
  const [notifications, setNotifications] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Broadcast states
  const [showModal, setShowModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState('info');
  const [targetUserId, setTargetUserId] = useState('');

  const fetchNotifications = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/user/notifications.php');
      if (res.data?.success) {
        setNotifications(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to load notifications", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const res = await axios.get('/admin/users.php');
      if (res.data?.success) {
        setUsers(res.data.data);
      }
    } catch (e) {
      console.warn("Failed to load users for target select", e);
    }
  };

  useEffect(() => {
    fetchNotifications();
    fetchUsers();
  }, []);

  const handleBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !description) {
      showToast('Validation Error', 'Please fill in all fields.', 'warning');
      return;
    }

    setGlobalLoading(true);
    try {
      const res = await axios.post('/user/notifications.php', {
        title,
        description,
        type,
        user_id: targetUserId ? parseInt(targetUserId) : null
      });
      if (res.data?.success) {
        showToast('Broadcasted Successfully', 'System alert dispatched.', 'success');
        setShowModal(false);
        setTitle('');
        setDescription('');
        setType('info');
        setTargetUserId('');
        fetchNotifications();
      }
    } catch (e: any) {
      showToast('Dispatch failed', e.response?.data?.message || 'Error broadcasting alert', 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  const filteredNotifications = notifications.filter(notif => 
    (notif.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (notif.description || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">System Notifications</h2>
          <p className="text-sm text-slate-400 mt-1">Audit log of system dispatches and broadcast emergency alerts to users.</p>
        </div>
        <button 
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2.5 bg-red-600 hover:bg-red-500 text-white text-sm font-bold rounded-xl transition shadow-lg shadow-red-500/20"
        >
          <Send className="w-4 h-4" /> Broadcast Alert
        </button>
      </div>

      <div className="flex bg-slate-900/50 p-4 rounded-2xl border border-white/5">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input 
            type="text" 
            placeholder="Search notification history logs..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
          />
        </div>
      </div>

      <div className="space-y-4">
        {filteredNotifications.length > 0 ? filteredNotifications.map((notif) => (
          <div 
            key={notif.notification_id} 
            className="bg-slate-900 border border-white/5 rounded-3xl p-6 shadow-xl relative overflow-hidden flex gap-4 items-start"
          >
            <div className={`p-3 rounded-2xl shrink-0 ${
              notif.type === 'alert' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
              notif.type === 'success' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
              'bg-blue-500/10 text-blue-400 border border-blue-500/20'
            }`}>
              {notif.type === 'alert' ? <AlertTriangle className="w-6 h-6" /> :
               notif.type === 'success' ? <CheckCircle className="w-6 h-6" /> :
               <Info className="w-6 h-6" />}
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-3">
                <h4 className="text-white font-extrabold text-base">{notif.title}</h4>
                {!notif.is_read && (
                  <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
                )}
              </div>
              <p className="text-slate-300 text-sm">{notif.description}</p>
              <div className="text-[10px] text-slate-500 flex items-center gap-1.5 pt-2">
                <Clock className="w-3.5 h-3.5" />
                {new Date(notif.created_at).toLocaleString()}
              </div>
            </div>
          </div>
        )) : (
          <div className="bg-slate-900/50 border border-white/5 rounded-3xl p-12 text-center text-slate-500">
            <Bell className="w-12 h-12 mx-auto mb-3 opacity-50" />
            <p>No system logs or notifications recorded.</p>
          </div>
        )}
      </div>

      {/* Broadcast Alert Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md z-50 flex items-center justify-center p-6">
          <div className="bg-slate-900 border border-white/10 rounded-3xl p-8 max-w-md w-full shadow-2xl relative animate-scale-up">
            <button 
              onClick={() => setShowModal(false)}
              className="absolute top-6 right-6 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="text-xl font-heading font-extrabold text-white mb-6">
              Broadcast System Alert
            </h3>

            <form onSubmit={handleBroadcast} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Target Audience</label>
                <select 
                  value={targetUserId}
                  onChange={(e) => setTargetUserId(e.target.value)}
                  className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                >
                  <option value="">Broadcast to All Registered Users</option>
                  {users.map(u => (
                    <option key={u.user_id} value={u.user_id}>{u.full_name} ({u.role})</option>
                  ))}
                </select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Alert Level</label>
                <select 
                  value={type}
                  onChange={(e) => setType(e.target.value)}
                  className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                >
                  <option value="info">Information (Blue)</option>
                  <option value="alert">Critical Alert (Red)</option>
                  <option value="success">Success Acknowledgment (Green)</option>
                </select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Alert Title</label>
                <input 
                  type="text" 
                  placeholder="Enter alert title" 
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Alert Description</label>
                <textarea 
                  rows={4}
                  placeholder="Enter detailed alert description..." 
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition resize-none"
                />
              </div>

              <button 
                type="submit"
                className="w-full py-3 bg-red-600 hover:bg-red-500 text-white font-bold rounded-xl transition text-sm shadow-lg shadow-red-500/20 mt-4 flex items-center justify-center gap-2"
              >
                <Send className="w-4 h-4" /> Dispatch Alert
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminNotifications;
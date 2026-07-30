import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { 
  Activity, CheckCircle, Clock, Navigation, 
  MapPin, Star, AlertTriangle
} from 'lucide-react';
import { Link } from 'react-router-dom';

const DriverDashboard: React.FC = () => {
  const { user } = useAuth();
  
  const [stats, setStats] = useState({
    todayTrips: 0,
    completedTrips: 0,
    activeRequests: 0,
    avgRating: 0.0
  });

  const [activeMission, setActiveMission] = useState<any>(null);

  useEffect(() => {
    fetchStats();
    fetchActiveMission();
    const interval = setInterval(fetchActiveMission, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchStats = async () => {
    try {
      const res = await axios.get('/requests/history.php');
      if (res.data?.success) {
        const history = res.data.data;
        
        let completed = 0;
        let today = 0;
        let totalRating = 0;
        let ratingCount = 0;

        const todayDate = new Date().toISOString().split('T')[0];

        history.forEach((req: any) => {
          if (req.status === 'completed') {
            completed++;
            if (req.created_at.startsWith(todayDate)) today++;
            if (req.rating) {
              totalRating += req.rating;
              ratingCount++;
            }
          }
        });

        setStats({
          todayTrips: today,
          completedTrips: completed,
          activeRequests: 0, // Not explicitly tracked here
          avgRating: ratingCount > 0 ? (totalRating / ratingCount) : 0
        });
      }
    } catch (e) {
      console.warn("Failed to load history stats", e);
    }
  };

  const fetchActiveMission = async () => {
    try {
      const res = await axios.get('/requests/active.php');
      if (res.data?.success) {
        const list = res.data.data;
        if (list.length > 0 && ['accepted', 'arrived', 'boarded'].includes(list[0].status)) {
          setActiveMission(list[0]);
        } else {
          setActiveMission(null);
        }
      }
    } catch (e) {
      console.warn(e);
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Welcome Banner */}
      <div className="glass-panel rounded-3xl p-8 border border-white/5 shadow-2xl relative overflow-hidden bg-gradient-to-r from-emerald-900/40 to-slate-900">
        <div className="absolute right-0 top-0 w-64 h-64 bg-emerald-500/10 rounded-full blur-[80px] pointer-events-none -mr-32 -mt-32"></div>
        <div className="relative z-10 flex justify-between items-center">
          <div>
            <h2 className="text-3xl font-heading font-extrabold text-white mb-2">Welcome back, {user?.full_name}</h2>
            <p className="text-emerald-400 font-bold uppercase tracking-widest text-xs">SafeReach Emergency Responder</p>
          </div>
          <div className="w-16 h-16 rounded-2xl bg-emerald-500/20 flex items-center justify-center border border-emerald-500/30 shadow-lg text-emerald-400">
            <Activity size={32} />
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="glass-panel rounded-3xl p-6 border border-white/5 relative overflow-hidden group">
          <div className="absolute inset-0 bg-blue-500/5 group-hover:bg-blue-500/10 transition"></div>
          <div className="relative z-10">
            <div className="w-10 h-10 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center mb-4">
              <Clock size={20} />
            </div>
            <p className="text-xs uppercase font-bold text-gray-400 tracking-wider">Today's Trips</p>
            <h3 className="text-3xl font-heading font-extrabold text-white mt-1">{stats.todayTrips}</h3>
          </div>
        </div>

        <div className="glass-panel rounded-3xl p-6 border border-white/5 relative overflow-hidden group">
          <div className="absolute inset-0 bg-emerald-500/5 group-hover:bg-emerald-500/10 transition"></div>
          <div className="relative z-10">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center mb-4">
              <CheckCircle size={20} />
            </div>
            <p className="text-xs uppercase font-bold text-gray-400 tracking-wider">Total Rescues</p>
            <h3 className="text-3xl font-heading font-extrabold text-white mt-1">{stats.completedTrips}</h3>
          </div>
        </div>

        <div className="glass-panel rounded-3xl p-6 border border-white/5 relative overflow-hidden group">
          <div className="absolute inset-0 bg-amber-500/5 group-hover:bg-amber-500/10 transition"></div>
          <div className="relative z-10">
            <div className="w-10 h-10 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center mb-4">
              <Star size={20} />
            </div>
            <p className="text-xs uppercase font-bold text-gray-400 tracking-wider">Avg Rating</p>
            <h3 className="text-3xl font-heading font-extrabold text-white mt-1">{stats.avgRating.toFixed(1)}</h3>
          </div>
        </div>

        <div className="glass-panel rounded-3xl p-6 border border-white/5 relative overflow-hidden group">
          <div className="absolute inset-0 bg-red-500/5 group-hover:bg-red-500/10 transition"></div>
          <div className="relative z-10">
            <div className="w-10 h-10 rounded-xl bg-red-500/20 text-red-400 flex items-center justify-center mb-4">
              <AlertTriangle size={20} />
            </div>
            <p className="text-xs uppercase font-bold text-gray-400 tracking-wider">Active Mission</p>
            <h3 className="text-xl font-heading font-extrabold text-white mt-1">{activeMission ? 'IN PROGRESS' : 'NONE'}</h3>
          </div>
        </div>
      </div>

      {/* Active Mission Quick View */}
      {activeMission ? (
        <div className="bg-slate-900 border border-blue-500/30 rounded-3xl p-8 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 w-1 bg-blue-500 h-full"></div>
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-extrabold text-white flex items-center gap-2">
              <Navigation className="w-5 h-5 text-blue-500" /> Current Emergency Assignment
            </h3>
            <span className="px-3 py-1 text-xs font-bold uppercase rounded-lg bg-blue-500/20 text-blue-400">
              {activeMission.status}
            </span>
          </div>
          
          <div className="flex gap-12 text-sm text-slate-300">
            <div>
              <p className="text-xs text-slate-500 font-bold uppercase mb-1">Patient</p>
              <p className="font-extrabold text-white">{activeMission.patient_name}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500 font-bold uppercase mb-1">Location</p>
              <p className="font-bold flex items-center gap-1"><MapPin className="w-4 h-4 text-emerald-500" /> {activeMission.pickup_address || 'Unknown'}</p>
            </div>
            <div className="ml-auto">
              <Link to="/driver/trip" className="px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white font-extrabold rounded-xl transition shadow-[0_0_15px_rgba(37,99,235,0.4)]">
                Open Mission Control
              </Link>
            </div>
          </div>
        </div>
      ) : (
        <div className="bg-slate-900/50 border border-white/5 rounded-3xl p-12 text-center shadow-xl">
          <div className="w-20 h-20 rounded-full bg-slate-800 flex items-center justify-center mx-auto mb-4 text-slate-600">
            <CheckCircle className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-extrabold text-white mb-2">You're All Caught Up</h3>
          <p className="text-slate-400 max-w-md mx-auto">There are currently no active emergencies assigned to you. Ensure your status is set to "Online" to receive nearby dispatches.</p>
        </div>
      )}

    </div>
  );
};

export default DriverDashboard;

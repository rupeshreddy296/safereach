import React, { useState, useEffect } from 'react';
import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import {
  LogOut, ShieldAlert, Activity,
  HelpCircle, Bell,
  Bell,
  MapPin, Navigation, Clock, User
} from 'lucide-react';

const DriverLayout: React.FC = () => {
  const { user, logout, showToast } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [dutyStatus, setDutyStatus] = useState<'online' | 'busy' | 'offline'>(
    user?.vehicle?.status || 'offline'
  );
  
  const [incomingAlert, setIncomingAlert] = useState<any>(null);

  // Poll for active alerts globally across all driver pages
  const pollActiveAlerts = async () => {
    try {
      const res = await axios.get('/requests/active.php');
      if (res.data && res.data.success) {
        const list = res.data.data;
        if (list.length > 0) {
          const req = list[0];
          // If pending, show global incoming alert
          if (req.status === 'pending') {
            setIncomingAlert(req);
          } else if (['accepted', 'arrived', 'boarded'].includes(req.status)) {
            // Already accepted/active mission - clear the incoming alert modal
            setIncomingAlert(null);
            // If they are not on the active mission page, we might want to show a small toast,
            // but for now, the ActiveMission page handles the actual trip.
          }
        } else {
          setIncomingAlert(null);
        }
      }
    } catch (e) {
      console.warn("Global polling error", e);
    }
  };

  useEffect(() => {
    pollActiveAlerts();
    const interval = setInterval(pollActiveAlerts, 4000);
    return () => clearInterval(interval);
  }, []);

  const handleUpdateStatus = async (newStatus: 'online' | 'busy' | 'offline') => {
    try {
      const res = await axios.post('/driver/status.php', { status: newStatus });
      if (res.data?.success) {
        setDutyStatus(newStatus);
        showToast('Status Updated', `You are now ${newStatus}`, 'success');
      }
    } catch (e: any) {
      showToast('Error', e.response?.data?.message || 'Failed to update status', 'error');
    }
  };

  const handleAcceptRequest = async (reqId: number) => {
    try {
      const res = await axios.post('/requests/update_status.php', {
        request_id: reqId,
        status: 'accepted'
      });
      if (res.data?.success) {
        showToast('Request Accepted', 'Navigating to active mission.', 'success');
        setIncomingAlert(null);
        navigate('/driver/trip');
      }
    } catch (e: any) {
      showToast('Failed to accept', e.response?.data?.message || e.message, 'error');
    }
  };

  const handleRejectRequest = async () => {
    // In a real app, rejecting might just re-assign it. The backend currently doesn't have a "rejected" state for the driver alone, it updates the whole request. 
    // Wait, the backend doesn't support rejecting natively unless we set it back to 'cancelled' which cancels it for the citizen too.
    // Let's just clear it locally and set driver to busy.
    handleUpdateStatus('busy');
    setIncomingAlert(null);
    showToast('Request Ignored', 'You have been set to busy.', 'warning');
  };

  const handleLogout = () => {
    logout();
    navigate('/login/driver');
  };

  const navigation = [
    { name: 'Dashboard', href: '/driver', icon: Activity },
    { name: 'Active Mission', href: '/driver/trip', icon: Navigation },
    { name: 'Trip History', href: '/driver/history', icon: Clock },
    { name: 'My Profile', href: '/driver/profile', icon: User },
    { name: 'Notifications', href: '/driver/notifications', icon: Bell },
  ];


  return (
    <div className="min-h-screen bg-slate-950 flex overflow-hidden font-sans text-slate-300">
      {/* Background glow effects */}
      <div className="fixed top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-500/10 rounded-full blur-[120px]"></div>
        <div className="absolute bottom-[-10%] right-[-10%] w-[30%] h-[30%] bg-emerald-500/10 rounded-full blur-[100px]"></div>
      </div>

      {/* Sidebar Navigation */}
      <div className="w-64 bg-slate-900/50 border-r border-white/5 backdrop-blur-xl flex flex-col relative z-20 shadow-2xl">
        <div className="h-20 flex items-center px-6 border-b border-white/5">
          <ShieldAlert className="w-8 h-8 text-emerald-500" />
          <span className="ml-3 font-heading font-extrabold text-xl text-white tracking-tight">
            Driver<span className="text-emerald-500">Portal</span>
          </span>
        </div>

        <div className="flex-1 overflow-y-auto py-6 px-4 space-y-1 custom-scrollbar">
          <div className="mb-6 px-2">
            <h3 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-3">Menu</h3>
            {navigation.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.href;
              return (
                <NavLink
                  key={item.name}
                  to={item.href}
                  className={`flex items-center px-3 py-2.5 mb-1 rounded-xl text-sm font-bold transition-all duration-300 ${
                    isActive 
                      ? 'bg-emerald-500/10 text-emerald-400' 
                      : 'text-slate-400 hover:bg-white/5 hover:text-white'
                  }`}
                >
                  <Icon className={`w-4 h-4 mr-3 ${isActive ? 'text-emerald-400' : 'text-slate-500'}`} />
                  {item.name}
                  {item.name === 'Active Mission' && incomingAlert && (
                    <span className="ml-auto w-2 h-2 rounded-full bg-red-500 animate-pulse"></span>
                  )}
                </NavLink>
              );
            })}
          </div>


        </div>

        {/* User / Duty Status Panel at Bottom */}
        <div className="p-4 border-t border-white/5 bg-slate-900/30">
          <div className="flex items-center mb-4">
            <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center border border-emerald-500/30">
              <span className="text-emerald-400 font-bold text-sm">
                {user?.full_name?.substring(0, 2).toUpperCase() || 'DR'}
              </span>
            </div>
            <div className="ml-3 overflow-hidden">
              <p className="text-sm font-extrabold text-white truncate">{user?.full_name}</p>
              <p className="text-[10px] text-emerald-400 uppercase tracking-wider font-bold">Driver ID: {user?.user_id}</p>
            </div>
          </div>
          
          <div className="bg-slate-950 rounded-xl border border-white/5 p-1 mb-3 flex">
            <button 
              onClick={() => handleUpdateStatus('online')}
              className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all ${
                dutyStatus === 'online' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'text-slate-500 hover:text-white'
              }`}
            >
              Online
            </button>
            <button 
              onClick={() => handleUpdateStatus('busy')}
              className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all ${
                dutyStatus === 'busy' ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30' : 'text-slate-500 hover:text-white'
              }`}
            >
              Busy
            </button>
          </div>
          
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center px-3 py-2.5 rounded-xl text-xs font-bold text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all border border-transparent hover:border-red-500/20"
          >
            <LogOut className="w-3.5 h-3.5 mr-2" />
            Sign Out
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col relative z-10 h-screen overflow-hidden">
        
        {/* Top App Bar */}
        <header className="h-20 flex items-center justify-between px-8 border-b border-white/5 bg-slate-900/30 backdrop-blur-md">
          <div>
            <h1 className="text-xl font-heading font-extrabold text-white capitalize">
              {location.pathname === '/driver' ? 'Driver Dashboard' : location.pathname.split('/').pop()?.replace('-', ' ')}
            </h1>
            <p className="text-xs text-slate-400 mt-1">SafeReach Emergency Response Network</p>
          </div>
          <div className="flex items-center gap-4">
            <div className={`px-4 py-1.5 rounded-full border text-xs font-bold uppercase tracking-wider flex items-center gap-2 shadow-lg ${
              dutyStatus === 'online' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' :
              dutyStatus === 'busy' ? 'bg-amber-500/10 border-amber-500/30 text-amber-400' :
              'bg-slate-800 border-slate-700 text-slate-400'
            }`}>
              <div className={`w-2 h-2 rounded-full ${
                dutyStatus === 'online' ? 'bg-emerald-400 animate-pulse' :
                dutyStatus === 'busy' ? 'bg-amber-400' :
                'bg-slate-500'
              }`}></div>
              {dutyStatus}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto p-8 custom-scrollbar">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>

      {/* GLOBAL INCOMING EMERGENCY MODAL */}
      {incomingAlert && (
        <div className="fixed inset-0 bg-slate-950/90 backdrop-blur-xl z-50 flex items-center justify-center p-6">
          <div className="bg-slate-900 border border-red-500/30 rounded-3xl p-8 max-w-lg w-full shadow-[0_0_50px_rgba(239,68,68,0.2)] relative overflow-hidden animate-scale-up">
            <div className="absolute top-0 left-0 w-full h-1.5 bg-red-500"></div>
            <div className="absolute top-0 right-0 w-64 h-64 bg-red-500/10 rounded-full blur-[80px] pointer-events-none -mr-32 -mt-32"></div>

            <div className="text-center space-y-3 relative z-10">
              <div className="w-20 h-20 rounded-full bg-red-500/10 border border-red-500/30 text-red-500 flex items-center justify-center mx-auto mb-4 animate-pulse">
                <Bell className="w-10 h-10" />
              </div>
              <h2 className="text-3xl font-heading font-extrabold text-white">EMERGENCY DISPATCH</h2>
              <p className="text-red-400 font-bold tracking-widest uppercase text-xs">Request #{incomingAlert.request_id}</p>
            </div>

            <div className="mt-8 space-y-4 relative z-10">
              <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
                <div className="mt-1"><ShieldAlert className="w-5 h-5 text-red-500" /></div>
                <div>
                  <div className="text-xs text-red-500 font-bold uppercase tracking-wider mb-1">Emergency Type</div>
                  <div className="text-white font-extrabold capitalize">{incomingAlert.emergency_type || 'General Emergency'}</div>
                </div>
              </div>

              <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
                <div className="mt-1"><User className="w-5 h-5 text-slate-500" /></div>
                <div>
                  <div className="text-xs text-slate-500 font-bold uppercase tracking-wider mb-1">Patient Name</div>
                  <div className="text-white font-extrabold text-lg">{incomingAlert.patient_name || 'Emergency Patient'}</div>
                  <div className="text-slate-400 text-sm mt-1">{incomingAlert.patient_phone || 'Number Hidden'}</div>
                </div>
              </div>
              
              <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
                <div className="mt-1"><MapPin className="w-5 h-5 text-blue-500" /></div>
                <div>
                  <div className="text-xs text-blue-500 font-bold uppercase tracking-wider mb-1">Pickup Location</div>
                  <div className="text-white font-bold">{incomingAlert.pickup_address || 'Unknown Address'}</div>
                </div>
              </div>

              {incomingAlert.hospital_name && (
                <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
                  <div className="mt-1"><Activity className="w-5 h-5 text-emerald-500" /></div>
                  <div>
                    <div className="text-xs text-emerald-500 font-bold uppercase tracking-wider mb-1">Destination ER</div>
                    <div className="text-white font-bold">{incomingAlert.hospital_name}</div>
                  </div>
                </div>
              )}
            </div>

            <div className="mt-8 flex gap-4 relative z-10">
              <button 
                onClick={() => handleRejectRequest()}
                className="flex-1 py-4 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-xl transition text-sm"
              >
                Reject & Set Busy
              </button>
              <button 
                onClick={() => handleAcceptRequest(incomingAlert.request_id)}
                className="flex-1 py-4 bg-red-600 hover:bg-red-500 text-white font-extrabold rounded-xl transition text-sm shadow-[0_0_20px_rgba(239,68,68,0.4)]"
              >
                ACCEPT MISSION
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default DriverLayout;

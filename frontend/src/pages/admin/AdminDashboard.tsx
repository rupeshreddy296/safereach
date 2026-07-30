import React, { useState, useEffect, useRef } from 'react';
import axios from '../../api/axios';
import { 
  Users, Truck, AlertTriangle, Activity
} from 'lucide-react';
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer 
} from 'recharts';

const AdminDashboard: React.FC = () => {
  
  const [stats, setStats] = useState<any>(null);
  const [chartData, setChartData] = useState<any[]>([]);
  const [activeCalls, setActiveCalls] = useState<any[]>([]);
  const [hospitals, setHospitals] = useState<any[]>([]);
  const [ambulances, setAmbulances] = useState<any[]>([]);
  
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const markersRef = useRef<any[]>([]);

  // Fetch Dashboard Stats
  const fetchStats = async () => {
    try {
      const res = await axios.get('/admin/dashboard_stats.php');
      if (res.data && res.data.success) {
        setStats(res.data.data);
      }
    } catch (e) {
      console.warn('Failed to fetch stats', e);
    }
  };

  // Fetch Chart Data
  const fetchChartData = async () => {
    try {
      const res = await axios.get('/admin/analytics_data.php');
      if (res.data && res.data.success) {
        setChartData(res.data.data);
      }
    } catch (e) {
      console.warn('Failed to fetch chart data', e);
    }
  };

  // Fetch Map Entities (Hospitals, Ambulances, Active Emergencies)
  const fetchMapData = async () => {
    try {
      const [reqRes, hospRes, ambRes] = await Promise.all([
        axios.get('/requests/active.php'),
        axios.get('/admin/hospitals.php'),
        axios.get('/admin/ambulances.php')
      ]);
      
      if (reqRes.data?.success) setActiveCalls(reqRes.data.data);
      if (hospRes.data?.success) setHospitals(hospRes.data.data);
      if (ambRes.data?.success) setAmbulances(ambRes.data.data);
    } catch (e) {
      console.warn('Failed to fetch map data', e);
    }
  };

  useEffect(() => {
    fetchStats();
    fetchChartData();
    fetchMapData();

    const interval = setInterval(() => {
      fetchStats();
      fetchMapData();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  // Initialize Map
  useEffect(() => {
    if (!mapRef.current || !window.google) return;

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = new window.google.maps.Map(mapRef.current, {
        center: { lat: 13.0232, lng: 80.0175 }, // Default to Chennai
        zoom: 12,
        mapTypeId: 'satellite',
        disableDefaultUI: false,
      });
    }
  }, []);

  // Update Map Markers
  useEffect(() => {
    if (!mapInstanceRef.current || !window.google) return;

    // Clear old markers
    markersRef.current.forEach(m => m.setMap(null));
    markersRef.current = [];

    const svgIcons = {
      hospital: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#0f172a" stroke="#38bdf8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20"/><path d="M2 12h20"/><path d="M12 7a5 5 0 0 0-5 5h10a5 5 0 0 0-5-5Z"/></svg>`),
      emergency: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#ef4444" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>`),
      ambulance: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#3b82f6" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="16" height="12" x="4" y="6" rx="2"/><path d="M20 18h2v-4a2 2 0 0 0-2-2h-4"/><path d="M12 10v4"/><path d="M10 12h4"/><circle cx="8" cy="18" r="2"/><circle cx="16" cy="18" r="2"/></svg>`)
    };

    // Plot Hospitals
    hospitals.forEach(h => {
      if (h.latitude && h.longitude) {
        const marker = new window.google.maps.Marker({
          position: { lat: parseFloat(h.latitude), lng: parseFloat(h.longitude) },
          map: mapInstanceRef.current,
          icon: { url: svgIcons.hospital, scaledSize: new window.google.maps.Size(32, 32) },
          title: h.name
        });
        markersRef.current.push(marker);
      }
    });

    // Plot Active Emergencies
    activeCalls.forEach(call => {
      if (call.pickup_latitude && call.pickup_longitude) {
        const marker = new window.google.maps.Marker({
          position: { lat: parseFloat(call.pickup_latitude), lng: parseFloat(call.pickup_longitude) },
          map: mapInstanceRef.current,
          icon: { url: svgIcons.emergency, scaledSize: new window.google.maps.Size(32, 32) },
          title: `Emergency: ${call.patient_name}`
        });
        markersRef.current.push(marker);
      }
    });

  }, [hospitals, activeCalls, ambulances]);


  const StatCard = ({ title, value, icon: Icon, color, subtitle }: any) => (
    <div className="glass-panel rounded-2xl p-5 border border-white/5 flex items-center justify-between">
      <div>
        <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider mb-1">{title}</p>
        <h3 className="text-3xl font-heading font-extrabold text-white">{value || 0}</h3>
        {subtitle && <p className={`text-[10px] mt-1 ${color}`}>{subtitle}</p>}
      </div>
      <div className={`w-12 h-12 rounded-xl flex items-center justify-center bg-white/5 ${color}`}>
        <Icon size={24} />
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      
      {/* KPI Cards Row 1 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Active Emergencies" 
          value={stats?.active_emergencies} 
          icon={AlertTriangle} 
          color="text-accentRed"
          subtitle={`${stats?.sos_requests || 0} SOS Requests`}
        />
        <StatCard 
          title="Available Ambulances" 
          value={stats?.available_ambulances} 
          icon={Truck} 
          color="text-emerald-400"
          subtitle={`Out of ${stats?.total_ambulances || 0} Total`}
        />
        <StatCard 
          title="Today's Bookings" 
          value={stats?.today_bookings} 
          icon={Activity} 
          color="text-blue-400"
        />
        <StatCard 
          title="Total Citizens" 
          value={stats?.total_citizens} 
          icon={Users} 
          color="text-purple-400"
          subtitle={`${stats?.total_drivers || 0} Registered Drivers`}
        />
      </div>

      {/* Row 2: Map & Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[500px]">
        {/* Unified Map */}
        <div className="lg:col-span-8 glass-panel rounded-2xl overflow-hidden border border-white/5 relative h-full">
          <div className="absolute top-4 left-4 z-10 bg-slate-950/90 border border-white/5 px-3 py-1.5 rounded-lg text-[9px] uppercase font-bold text-white flex items-center gap-1.5 shadow-xl">
            <div className="w-2 h-2 rounded-full bg-accentRed animate-ping"></div>
            Live Satellite Radar
          </div>
          <div ref={mapRef} className="w-full h-full"></div>
        </div>

        {/* Analytics Mini-Chart */}
        <div className="lg:col-span-4 glass-panel rounded-2xl p-5 border border-white/5 flex flex-col">
          <div className="mb-4">
            <h3 className="text-sm font-extrabold text-white">Booking Trends</h3>
            <p className="text-[10px] text-gray-400">Last 7 Days</p>
          </div>
          <div className="flex-1 w-full min-h-0">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorBookings" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#dc2626" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#dc2626" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis dataKey="name" stroke="#64748b" fontSize={10} tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" fontSize={10} tickLine={false} axisLine={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: 'rgba(255,255,255,0.1)', borderRadius: '8px' }}
                  itemStyle={{ color: '#fff', fontSize: '12px' }}
                  labelStyle={{ color: '#94a3b8', fontSize: '10px' }}
                />
                <Area type="monotone" dataKey="bookings" stroke="#dc2626" strokeWidth={3} fillOpacity={1} fill="url(#colorBookings)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

    </div>
  );
};

export default AdminDashboard;

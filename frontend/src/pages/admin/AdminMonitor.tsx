import React, { useState, useEffect, useRef } from 'react';
import axios from '../../api/axios';
import { MapPin, Activity } from 'lucide-react';

const AdminMonitor: React.FC = () => {
  const [activeCalls, setActiveCalls] = useState<any[]>([]);
  const [hospitals, setHospitals] = useState<any[]>([]);
  const [ambulances, setAmbulances] = useState<any[]>([]);
  
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const markersRef = useRef<any[]>([]);

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
    fetchMapData();
    const interval = setInterval(fetchMapData, 5000);
    return () => clearInterval(interval);
  }, []);

  // Initialize Map
  useEffect(() => {
    if (!mapRef.current || !window.google) return;

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = new window.google.maps.Map(mapRef.current, {
        center: { lat: 13.0232, lng: 80.0175 }, // Chennai
        zoom: 12,
        mapTypeId: 'roadmap',
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
      ambulance: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#10b981" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="16" height="12" x="4" y="6" rx="2"/><path d="M20 18h2v-4a2 2 0 0 0-2-2h-4"/><path d="M12 10v4"/><path d="M10 12h4"/><circle cx="8" cy="18" r="2"/><circle cx="16" cy="18" r="2"/></svg>`)
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
          title: `Emergency: ${call.patient_name} (${call.emergency_type})`
        });
        markersRef.current.push(marker);
      }
    });

    // Plot Ambulances
    ambulances.forEach(amb => {
      if (amb.current_latitude && amb.current_longitude && amb.status === 'online') {
        const marker = new window.google.maps.Marker({
          position: { lat: parseFloat(amb.current_latitude), lng: parseFloat(amb.current_longitude) },
          map: mapInstanceRef.current,
          icon: { url: svgIcons.ambulance, scaledSize: new window.google.maps.Size(32, 32) },
          title: `Ambulance: ${amb.vehicle_number} (${amb.status})`
        });
        markersRef.current.push(marker);
      }
    });

  }, [hospitals, activeCalls, ambulances]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-heading font-extrabold text-white">Live Monitor</h2>
        <p className="text-sm text-slate-400 mt-1">Real-time emergency telemetry radar tracking and dispatches.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 h-[calc(100vh-14rem)]">
        {/* Active Emergency Feed */}
        <div className="lg:col-span-4 flex flex-col gap-4 overflow-y-auto custom-scrollbar h-full pr-2">
          <h3 className="text-sm font-extrabold text-white uppercase tracking-wider text-slate-400 mb-2">Active Emergency Feed</h3>
          {activeCalls.length > 0 ? (
            activeCalls.map((call) => (
              <div key={call.request_id} className="bg-slate-900 border border-white/5 rounded-3xl p-5 shadow-xl relative overflow-hidden flex flex-col gap-3">
                <div className="flex justify-between items-center">
                  <span className="text-xs bg-slate-950 border border-white/10 px-2.5 py-1 rounded-xl text-blue-400 font-bold font-mono">
                    #{call.request_id}
                  </span>
                  <span className={`text-[10px] font-extrabold uppercase px-2.5 py-1 rounded-xl tracking-wider border ${
                    call.status === 'pending' ? 'bg-red-500/10 text-red-400 border-red-500/20' :
                    call.status === 'accepted' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                    'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  }`}>
                    {call.status}
                  </span>
                </div>
                <div>
                  <h4 className="text-white font-extrabold text-sm">{call.patient_name}</h4>
                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-blue-400" />
                    <span className="truncate">{call.pickup_address}</span>
                  </p>
                </div>
                <div className="pt-2 border-t border-white/5 flex justify-between items-center text-xs">
                  <span className="text-slate-500 font-bold uppercase tracking-wider">{call.emergency_type}</span>
                  <span className="text-slate-400">{new Date(call.created_at).toLocaleTimeString()}</span>
                </div>
              </div>
            ))
          ) : (
            <div className="bg-slate-900/50 border border-white/5 rounded-3xl p-8 text-center text-slate-500 h-40 flex flex-col justify-center items-center">
              <Activity className="w-8 h-8 opacity-50 mb-2" />
              <p>No active dispatches right now.</p>
            </div>
          )}
        </div>

        {/* Live Radar Map */}
        <div className="lg:col-span-8 glass-panel rounded-3xl overflow-hidden border border-white/10 relative h-full shadow-2xl">
          <div className="absolute top-4 left-4 z-10 bg-slate-950/90 border border-white/5 px-3 py-1.5 rounded-lg text-[9px] uppercase font-bold text-white flex items-center gap-1.5 shadow-xl">
            <div className="w-2 h-2 rounded-full bg-red-500 animate-ping"></div>
            Live Satellite Radar
          </div>
          <div ref={mapRef} className="w-full h-full bg-slate-950"></div>
        </div>
      </div>
    </div>
  );
};

export default AdminMonitor;
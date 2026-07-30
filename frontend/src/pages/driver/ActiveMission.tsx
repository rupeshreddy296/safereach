import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { 
  UserCircle, AlertTriangle,
  MapPin, Activity, Navigation, Heart, Wind, Gauge, Thermometer,
  CheckCircle, XCircle
} from 'lucide-react';

const googleMapsDarkStyle = [
  { elementType: "geometry", stylers: [{ color: "#0c1322" }] },
  { elementType: "labels.text.stroke", stylers: [{ color: "#0c1322" }] },
  { elementType: "labels.text.fill", stylers: [{ color: "#74889e" }] },
  { featureType: "administrative.locality", elementType: "labels.text.fill", stylers: [{ color: "#a8b5c6" }] },
  { featureType: "poi", elementType: "labels.text.fill", stylers: [{ color: "#a8b5c6" }] },
  { featureType: "poi.park", elementType: "geometry", stylers: [{ color: "#0f1b2e" }] },
  { featureType: "poi.park", elementType: "labels.text.fill", stylers: [{ color: "#4f6785" }] },
  { featureType: "road", elementType: "geometry", stylers: [{ color: "#16253b" }] },
  { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#1e314f" }] },
  { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#74889e" }] },
  { featureType: "road.highway", elementType: "geometry", stylers: [{ color: "#22395d" }] },
  { featureType: "road.highway", elementType: "geometry.stroke", stylers: [{ color: "#2b4671" }] },
  { featureType: "road.highway", elementType: "labels.text.fill", stylers: [{ color: "#a8b5c6" }] },
  { featureType: "water", elementType: "geometry", stylers: [{ color: "#080e1a" }] },
  { featureType: "water", elementType: "labels.text.fill", stylers: [{ color: "#4f6785" }] }
];

const svgIcons = {
  patient: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 30" width="36" height="42">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#ef4444" stroke="#ffffff" stroke-width="1.5"/>
          <circle cx="12" cy="9" r="3" fill="#ffffff"/>
      </svg>
  `),
  ambulance: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 30" width="36" height="42">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#10b981" stroke="#ffffff" stroke-width="1.5"/>
          <rect x="8" y="6" width="8" height="6" fill="#ffffff" rx="1"/>
          <path d="M9 9h6M12 6v6" stroke="#10b981" stroke-width="1.5"/>
      </svg>
  `),
  hospital: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 30" width="36" height="42">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#3b82f6" stroke="#ffffff" stroke-width="1.5"/>
          <path d="M9 9h6M12 6v6" stroke="#ffffff" stroke-width="2"/>
      </svg>
  `)
};

const ActiveMission: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();

  const mapRef = useRef<HTMLDivElement>(null);
  const mapsInstanceRef = useRef<google.maps.Map | null>(null);
  const selfMarkerRef = useRef<google.maps.Marker | null>(null);
  const patientMarkerRef = useRef<google.maps.Marker | null>(null);
  const hospMarkerRef = useRef<google.maps.Marker | null>(null);
  const directionsRendererRef = useRef<google.maps.DirectionsRenderer | null>(null);

  const [lat, setLat] = useState<number>(13.0232);
  const [lng, setLng] = useState<number>(80.0175);
  const [activeMission, setActiveMission] = useState<any>(null);

  // Vitals inputs
  const [vitalsHeart, setVitalsHeart] = useState('75');
  const [vitalsBp, setVitalsBp] = useState('120/80');
  const [vitalsSpo2, setVitalsSpo2] = useState('98');
  const [vitalsTemp, setVitalsTemp] = useState('37.0');
  const [vitalsCondition, setVitalsCondition] = useState<'stable' | 'improving' | 'critical' | 'unconscious'>('stable');
  const [selectedHospitalId, setSelectedHospitalId] = useState('');
  const [hospitalsList, setHospitalsList] = useState<any[]>([]);
  const [etaText, setEtaText] = useState('Calculating...');



  // Watch GPS
  useEffect(() => {
    if (navigator.geolocation) {
      const watchId = navigator.geolocation.watchPosition(
        (pos) => {
          setLat(pos.coords.latitude);
          setLng(pos.coords.longitude);
        },
        (err) => console.warn(err),
        { enableHighAccuracy: true, maximumAge: 10000, timeout: 5000 }
      );
      return () => navigator.geolocation.clearWatch(watchId);
    }
  }, []);

  // Poll for active alerts checks
  const pollActiveAlerts = async () => {
    try {
      const res = await axios.get('/requests/active.php');
      if (res.data && res.data.success) {
        const list = res.data.data;
        if (list.length > 0) {
          const req = list[0];
          if (['accepted', 'arrived', 'boarded'].includes(req.status)) {
            setActiveMission(req);
            if (req.status === 'boarded' && hospitalsList.length === 0) {
              fetchHospitalsDropdown();
            }
          } else {
            setActiveMission(null);
            clearMapPins();
          }
          updateDriverMapPins(req);
        } else {
          setActiveMission(null);
          clearMapPins();
        }
      }
    } catch (e) {
      console.warn(e);
    }
  };

  useEffect(() => {
    pollActiveAlerts();
    const interval = setInterval(pollActiveAlerts, 4000);
    return () => clearInterval(interval);
  }, [lat, lng, hospitalsList]);

  const fetchHospitalsDropdown = async () => {
    try {
      const res = await axios.get('/hospitals/list.php');
      if (res.data && res.data.success) {
        setHospitalsList(res.data.data);
      }
    } catch (e) {
      console.warn(e);
    }
  };

  // Maps setup
  useEffect(() => {
    if (mapRef.current && typeof google !== 'undefined' && !mapsInstanceRef.current) {
      mapsInstanceRef.current = new google.maps.Map(mapRef.current, {
        center: { lat: 13.0232, lng: 80.0175 },
        zoom: 14,
        styles: googleMapsDarkStyle,
        mapTypeControl: false,
        streetViewControl: false,
      });

      directionsRendererRef.current = new google.maps.DirectionsRenderer({
        map: mapsInstanceRef.current,
        suppressMarkers: true,
        polylineOptions: { strokeColor: "#3b82f6", strokeOpacity: 0.8, strokeWeight: 6 },
      });
    }
  }, []);

  const clearMapPins = () => {
    if (selfMarkerRef.current) selfMarkerRef.current.setMap(null);
    if (patientMarkerRef.current) patientMarkerRef.current.setMap(null);
    if (hospMarkerRef.current) hospMarkerRef.current.setMap(null);
    if (directionsRendererRef.current) directionsRendererRef.current.setDirections({ routes: [] } as any);
  };

  const updateDriverMapPins = (req: any) => {
    if (!mapsInstanceRef.current) return;

    if (!selfMarkerRef.current) {
      selfMarkerRef.current = new google.maps.Marker({
        map: mapsInstanceRef.current,
        icon: { url: svgIcons.ambulance, scaledSize: new google.maps.Size(36, 42) }
      });
    }
    selfMarkerRef.current.setPosition({ lat, lng });

    const bounds = new google.maps.LatLngBounds();
    bounds.extend({ lat, lng });

    let destination: google.maps.LatLng | null = null;

    if (['accepted', 'arrived'].includes(req?.status) && req?.pickup_latitude) {
      if (!patientMarkerRef.current) {
        patientMarkerRef.current = new google.maps.Marker({
          map: mapsInstanceRef.current,
          icon: { url: svgIcons.patient, scaledSize: new google.maps.Size(36, 42) }
        });
      }
      const pLat = parseFloat(req.pickup_latitude);
      const pLng = parseFloat(req.pickup_longitude);
      patientMarkerRef.current.setPosition({ lat: pLat, lng: pLng });
      bounds.extend({ lat: pLat, lng: pLng });
      destination = new google.maps.LatLng(pLat, pLng);
    } else {
      if (patientMarkerRef.current) {
        patientMarkerRef.current.setMap(null);
        patientMarkerRef.current = null;
      }
    }

    if (req?.status === 'boarded' && req?.destination_hospital_id) {
      if (!hospMarkerRef.current) {
        hospMarkerRef.current = new google.maps.Marker({
          map: mapsInstanceRef.current,
          icon: { url: svgIcons.hospital, scaledSize: new google.maps.Size(36, 42) }
        });
      }
      
      const hosp = hospitalsList.find(h => h.hospital_id === req.destination_hospital_id);
      if (hosp) {
        const hLat = parseFloat(hosp.latitude);
        const hLng = parseFloat(hosp.longitude);
        hospMarkerRef.current.setPosition({ lat: hLat, lng: hLng });
        bounds.extend({ lat: hLat, lng: hLng });
        destination = new google.maps.LatLng(hLat, hLng);
      }

    } else {
      if (hospMarkerRef.current) {
        hospMarkerRef.current.setMap(null);
        hospMarkerRef.current = null;
      }
    }

    mapsInstanceRef.current.fitBounds(bounds);

    if (destination && directionsRendererRef.current) {
      const directionsService = new google.maps.DirectionsService();
      directionsService.route({
        origin: new google.maps.LatLng(lat, lng),
        destination,
        travelMode: google.maps.TravelMode.DRIVING,
      }, (result, status) => {
        if (status === 'OK' && result && directionsRendererRef.current) {
          directionsRendererRef.current.setDirections(result);
          const leg = result.routes[0]?.legs[0];
          if (leg) {
            setEtaText(`${leg.duration?.text} (${leg.distance?.text})`);
          }
        }
      });
    }
  };

  const handleUpdateTripStatus = async (status: string, hospitalId?: number) => {
    setGlobalLoading(true);
    try {
      const payload: any = { request_id: activeMission.request_id, status };
      if (hospitalId) {
        payload.hospital_id = hospitalId;
      }
      await axios.post('/requests/update_status.php', payload);
      showToast('Status Updated', `Trip status is now ${status}`, 'success');
      pollActiveAlerts();
    } catch (e: any) {
      showToast('Failed to Update', e.response?.data?.message || 'Error updating status', 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  const handlePushVitals = async () => {
    setGlobalLoading(true);
    try {
      await axios.post('/driver/vitals.php', {
        request_id: activeMission.request_id,
        heart_rate: vitalsHeart,
        blood_pressure: vitalsBp,
        oxygen_saturation: vitalsSpo2,
        temperature: vitalsTemp,
        condition: vitalsCondition
      });
      showToast('Vitals Transmitted', 'Hospital is receiving telemetry.', 'success');
    } catch (e: any) {
      showToast('Failed to push vitals', e.message, 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  if (!activeMission) {
    return (
      <div className="flex flex-col items-center justify-center h-[70vh] bg-slate-900/50 rounded-3xl border border-white/5 shadow-2xl">
        <div className="w-24 h-24 rounded-full bg-slate-800/50 flex items-center justify-center mb-6 border border-white/5">
          <Navigation className="w-10 h-10 text-slate-500" />
        </div>
        <h2 className="text-2xl font-heading font-extrabold text-white mb-2">No Active Mission</h2>
        <p className="text-slate-400 max-w-sm text-center">
          You are currently available for dispatch. Ensure your status is set to "Online" to receive emergency requests.
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 h-[calc(100vh-8rem)]">
      {/* Left Panel - Mission Control */}
      <div className="lg:col-span-5 flex flex-col gap-6 h-full overflow-y-auto custom-scrollbar pr-2">
        
        {/* Mission Status Header */}
        <div className="bg-slate-900 border border-white/10 rounded-3xl p-6 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/10 rounded-full blur-[50px] -mr-16 -mt-16 pointer-events-none"></div>
          <div className="flex items-center gap-3 mb-2">
            <span className="text-xs bg-slate-950 border border-white/10 px-3 py-1.5 rounded-xl text-blue-400 font-bold font-mono tracking-widest">
              MISSION #{activeMission.request_id}
            </span>
            <span className="text-xs uppercase font-extrabold text-white bg-blue-500 px-3 py-1.5 rounded-xl animate-pulse">
              {activeMission.status}
            </span>
          </div>
          <h2 className="text-2xl font-heading font-extrabold text-white">Emergency Response</h2>
          
          <div className="mt-6 space-y-4">
            <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
              <div className="mt-1"><UserCircle className="w-5 h-5 text-slate-500" /></div>
              <div>
                <div className="text-xs text-slate-500 font-bold uppercase tracking-wider mb-1">Patient Details</div>
                <div className="text-white font-bold">{activeMission.patient_name || 'Emergency Patient'}</div>
                <div className="text-slate-400 text-sm mt-1">
                  <a href={`tel:${activeMission.patient_phone}`} className="hover:text-blue-400 transition">
                    {activeMission.patient_phone || 'Number Hidden'}
                  </a>
                </div>
              </div>
            </div>

            <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
              <div className="mt-1"><AlertTriangle className="w-5 h-5 text-red-500" /></div>
              <div>
                <div className="text-xs text-red-500 font-bold uppercase tracking-wider mb-1">Emergency Type</div>
                <div className="text-white font-bold capitalize">{activeMission.emergency_type || 'General Emergency'}</div>
                <div className="text-slate-400 text-xs mt-1">Booked at {new Date(activeMission.created_at).toLocaleTimeString()}</div>
              </div>
            </div>

            <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
              <div className="mt-1"><MapPin className="w-5 h-5 text-blue-500" /></div>
              <div>
                <div className="text-xs text-blue-500 font-bold uppercase tracking-wider mb-1">Pickup Location</div>
                <div className="text-white font-bold">{activeMission.pickup_address || 'Unknown Address'}</div>
              </div>
            </div>
            
            {activeMission.status === 'boarded' && activeMission.hospital_name && (
              <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
                <div className="mt-1"><Activity className="w-5 h-5 text-emerald-500" /></div>
                <div>
                  <div className="text-xs text-emerald-500 font-bold uppercase tracking-wider mb-1">Destination Hospital</div>
                  <div className="text-white font-bold">{activeMission.hospital_name}</div>
                </div>
              </div>
            )}
            
            <div className="bg-slate-950 rounded-xl p-4 border border-white/5 flex gap-4">
              <div className="mt-1"><Navigation className="w-5 h-5 text-purple-500" /></div>
              <div>
                <div className="text-xs text-purple-500 font-bold uppercase tracking-wider mb-1">Ambulance Assigned</div>
                <div className="text-white font-bold tracking-widest uppercase">{activeMission.vehicle_number || 'SafeReach Responder'}</div>
              </div>
            </div>
          </div>
        </div>

        {/* Action Controls */}
        <div className="bg-slate-900 border border-white/10 rounded-3xl p-6 shadow-2xl">
          <h3 className="text-sm font-extrabold text-white mb-4 uppercase tracking-wider text-slate-400">Mission Controls</h3>
          
          <div className="space-y-4">
            {activeMission.status === 'accepted' && (
              <button 
                onClick={() => handleUpdateTripStatus('arrived')}
                className="w-full py-4 bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold rounded-xl transition text-sm shadow-[0_0_15px_rgba(245,158,11,0.3)] flex items-center justify-center gap-2"
              >
                <MapPin className="w-5 h-5" /> Reached Pickup Location
              </button>
            )}

            {activeMission.status === 'arrived' && (
              <button 
                onClick={() => handleUpdateTripStatus('boarded')}
                className="w-full py-4 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-extrabold rounded-xl transition text-sm shadow-[0_0_15px_rgba(16,185,129,0.3)] flex items-center justify-center gap-2"
              >
                <Activity className="w-5 h-5" /> Patient Boarded & En-route
              </button>
            )}

            {activeMission.status === 'boarded' && !activeMission.destination_hospital_id && (
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-400">Assign Destination ER</label>
                  <select 
                    className="w-full bg-slate-950 border border-white/10 rounded-xl py-3 px-4 text-sm text-white focus:outline-none focus:border-emerald-500 transition"
                    value={selectedHospitalId}
                    onChange={(e) => setSelectedHospitalId(e.target.value)}
                  >
                    <option value="">-- Select Destination Hospital --</option>
                    {hospitalsList.map((h: any) => (
                      <option key={h.hospital_id} value={h.hospital_id}>{h.name}</option>
                    ))}
                  </select>
                </div>
                <button 
                  onClick={() => handleUpdateTripStatus('completed', parseInt(selectedHospitalId))}
                  disabled={!selectedHospitalId}
                  className={`w-full py-4 font-extrabold rounded-xl transition text-sm flex items-center justify-center gap-2 ${
                    selectedHospitalId ? 'bg-blue-600 hover:bg-blue-500 text-white shadow-[0_0_15px_rgba(37,99,235,0.4)]' : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                  }`}
                >
                  <CheckCircle className="w-5 h-5" /> Handover to Hospital & Complete
                </button>
              </div>
            )}

            {activeMission.status === 'boarded' && activeMission.destination_hospital_id && (
              <button 
                onClick={() => handleUpdateTripStatus('completed')}
                className="w-full py-4 bg-blue-600 hover:bg-blue-500 text-white font-extrabold rounded-xl transition text-sm shadow-[0_0_15px_rgba(37,99,235,0.4)] flex items-center justify-center gap-2"
              >
                <CheckCircle className="w-5 h-5" /> Complete Mission
              </button>
            )}

            <button 
              onClick={() => {
                if(confirm('Are you sure you want to abort this emergency request?')) {
                  handleUpdateTripStatus('cancelled');
                }
              }}
              className="w-full py-4 bg-transparent border border-red-500/20 hover:bg-red-500/10 text-red-500 font-extrabold rounded-xl transition text-sm flex items-center justify-center gap-2"
            >
              <XCircle className="w-5 h-5" /> Abort Mission
            </button>
          </div>
        </div>

        {/* Patient Vitals Stream Panel */}
        {['arrived', 'boarded'].includes(activeMission.status) && (
          <div className="bg-slate-900 border border-white/10 rounded-3xl p-6 shadow-2xl">
            <h3 className="text-sm font-extrabold text-white mb-4 uppercase tracking-wider text-slate-400">Live Telemetry & Vitals</h3>
            
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="space-y-1">
                <label className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Heart Rate (BPM)</label>
                <div className="relative">
                  <Heart className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-red-400" />
                  <input type="text" value={vitalsHeart} onChange={e=>setVitalsHeart(e.target.value)} className="w-full bg-slate-950 border border-white/5 rounded-xl py-2 pl-9 pr-3 text-sm text-white focus:outline-none focus:border-blue-500" />
                </div>
              </div>
              <div className="space-y-1">
                <label className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">BP (mmHg)</label>
                <div className="relative">
                  <Gauge className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-blue-400" />
                  <input type="text" value={vitalsBp} onChange={e=>setVitalsBp(e.target.value)} className="w-full bg-slate-950 border border-white/5 rounded-xl py-2 pl-9 pr-3 text-sm text-white focus:outline-none focus:border-blue-500" />
                </div>
              </div>
              <div className="space-y-1">
                <label className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">SpO2 (%)</label>
                <div className="relative">
                  <Wind className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-emerald-400" />
                  <input type="text" value={vitalsSpo2} onChange={e=>setVitalsSpo2(e.target.value)} className="w-full bg-slate-950 border border-white/5 rounded-xl py-2 pl-9 pr-3 text-sm text-white focus:outline-none focus:border-blue-500" />
                </div>
              </div>
              <div className="space-y-1">
                <label className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Temp (°C)</label>
                <div className="relative">
                  <Thermometer className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-orange-400" />
                  <input type="text" value={vitalsTemp} onChange={e=>setVitalsTemp(e.target.value)} className="w-full bg-slate-950 border border-white/5 rounded-xl py-2 pl-9 pr-3 text-sm text-white focus:outline-none focus:border-blue-500" />
                </div>
              </div>
            </div>

            <div className="space-y-1 mb-4">
              <label className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Patient Condition</label>
              <select value={vitalsCondition} onChange={e=>setVitalsCondition(e.target.value as any)} className="w-full bg-slate-950 border border-white/5 rounded-xl py-2 px-3 text-sm text-white focus:outline-none focus:border-blue-500">
                <option value="stable">Stable</option>
                <option value="improving">Improving</option>
                <option value="critical">Critical</option>
                <option value="unconscious">Unconscious</option>
              </select>
            </div>

            <button onClick={handlePushVitals} className="w-full py-3 bg-white/5 hover:bg-white/10 text-white font-bold rounded-xl border border-white/10 transition text-sm flex items-center justify-center gap-2">
              <Activity className="w-4 h-4 text-emerald-400" /> Push Vitals to Hospital
            </button>
          </div>
        )}
      </div>

      {/* Right Panel - Map */}
      <div className="lg:col-span-7 flex flex-col relative h-full rounded-3xl overflow-hidden border-2 border-white/10 shadow-2xl">
        <div className="absolute top-6 left-6 z-10 flex gap-3">
          <div className="bg-slate-950/90 backdrop-blur-md border border-white/10 px-4 py-3 rounded-2xl shadow-2xl flex flex-col gap-1">
            <span className="text-[10px] uppercase font-bold text-emerald-400 tracking-wider">Estimated Arrival</span>
            <span className="text-xl font-heading font-extrabold text-white">{etaText}</span>
          </div>
        </div>

        <div ref={mapRef} className="flex-1 w-full h-full bg-slate-900"></div>
        <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-slate-950/80 to-transparent pointer-events-none z-10"></div>
      </div>
    </div>
  );
};

export default ActiveMission;
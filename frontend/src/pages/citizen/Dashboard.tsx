import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { ChevronDown, Crosshair, HelpCircle, PhoneCall } from 'lucide-react';

const iconMap: Record<string, any> = {
  ChevronDown, Crosshair, HelpCircle, PhoneCall
};

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

const Dashboard: React.FC = () => {
  const { showToast, setGlobalLoading, user } = useAuth();
  const navigate = useNavigate();

  const mapRef = useRef<HTMLDivElement>(null);
  const mapsInstanceRef = useRef<google.maps.Map | null>(null);
  const patientMarkerRef = useRef<google.maps.Marker | null>(null);
  const hospMarkersRef = useRef<google.maps.Marker[]>([]);
  const ambMarkersRef = useRef<google.maps.Marker[]>([]);

  // Booking fields
  const [patientName, setPatientName] = useState(user?.full_name || '');
  const [patientPhone, setPatientPhone] = useState('');
  const [emergencyType, setEmergencyType] = useState('');
  const [ambulanceType, setAmbulanceType] = useState<'BLS' | 'ALS' | 'CCU'>('BLS');
  const [addressDesc, setAddressDesc] = useState('');

  // Simulating position (Chennai default)
  const [lat, setLat] = useState<number>(13.0232);
  const [lng, setLng] = useState<number>(80.0175);

  const [savedLocs, setSavedLocs] = useState<any[]>([]);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  // Watch position
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLat(pos.coords.latitude);
          setLng(pos.coords.longitude);
        },
        (err) => console.warn(err)
      );
    }
  }, []);

  // Fetch initial configs
  useEffect(() => {
    // Check if there is an active request
    const checkActiveCall = async () => {
      try {
        const res = await axios.get('/requests/active.php');
        if (res.data && res.data.success && res.data.data.length > 0) {
          const active = res.data.data[0];
          showToast("Active Incident", "Routing to active telemetry tracking.", "info");
          navigate(`/citizen/track/${active.request_id}`);
        }
      } catch (e) {
        console.warn(e);
      }
    };

    const fetchSavedAddresses = async () => {
      try {
        const res = await axios.get('/user/addresses.php');
        if (res.data && res.data.success) {
          setSavedLocs(res.data.data);
        }
      } catch (e) {
        console.warn(e);
      }
    };

    checkActiveCall();
    fetchSavedAddresses();
  }, []);

  // Map Setup
  useEffect(() => {
    if (mapRef.current && typeof google !== 'undefined') {
      const centerPos = { lat, lng };

      if (!mapsInstanceRef.current) {
        mapsInstanceRef.current = new google.maps.Map(mapRef.current, {
          center: centerPos,
          zoom: 14,
          styles: googleMapsDarkStyle,
          mapTypeControl: false,
          streetViewControl: false,
        });

        mapsInstanceRef.current.addListener('click', (e: google.maps.MapMouseEvent) => {
          if (e.latLng) {
            updateMarker(e.latLng.lat(), e.latLng.lng());
          }
        });
      } else {
        mapsInstanceRef.current.setCenter(centerPos);
      }

      updateMarker(lat, lng);
    }
  }, [lat, lng]);

  const updateMarker = (selectedLat: number, selectedLng: number) => {
    setLat(selectedLat);
    setLng(selectedLng);

    if (patientMarkerRef.current) {
      patientMarkerRef.current.setPosition({ lat: selectedLat, lng: selectedLng });
    } else if (mapsInstanceRef.current) {
      patientMarkerRef.current = new google.maps.Marker({
        position: { lat: selectedLat, lng: selectedLng },
        map: mapsInstanceRef.current,
        icon: {
          url: svgIcons.patient,
          scaledSize: new google.maps.Size(36, 42),
        },
      });
    }

    if (mapsInstanceRef.current) {
      mapsInstanceRef.current.panTo({ lat: selectedLat, lng: selectedLng });
    }

    fetchNearbyResources(selectedLat, selectedLng);
  };

  const fetchNearbyResources = async (cLat: number, cLng: number) => {
    try {
      // 1. Nearby Hospitals
      const hospRes = await axios.get(`/hospitals/list.php?lat=${cLat}&lng=${cLng}`);
      hospMarkersRef.current.forEach((m) => m.setMap(null));
      hospMarkersRef.current = [];

      if (hospRes.data && hospRes.data.success && mapsInstanceRef.current) {
        hospRes.data.data.forEach((h: any) => {
          const marker = new google.maps.Marker({
            position: { lat: parseFloat(h.latitude), lng: parseFloat(h.longitude) },
            map: mapsInstanceRef.current!,
            icon: {
              url: svgIcons.hospital,
              scaledSize: new google.maps.Size(36, 42),
            },
            title: h.name,
          });

          const info = new google.maps.InfoWindow({
            content: `<div class="p-2 text-slate-900"><strong class="block text-sm">${h.name}</strong><span class="text-xs">${h.address}</span><br/><span class="text-xs text-blue-600 font-bold">ICU Beds: ${h.available_icu_beds}</span></div>`,
          });

          marker.addListener('click', () => info.open(mapsInstanceRef.current, marker));
          hospMarkersRef.current.push(marker);
        });
      }

      // 2. Nearby Ambulances
      const ambRes = await axios.get(`/driver/nearby.php?lat=${cLat}&lng=${cLng}`);
      ambMarkersRef.current.forEach((m) => m.setMap(null));
      ambMarkersRef.current = [];

      if (ambRes.data && ambRes.data.success && mapsInstanceRef.current) {
        ambRes.data.data.forEach((a: any) => {
          const marker = new google.maps.Marker({
            position: { lat: parseFloat(a.latitude), lng: parseFloat(a.longitude) },
            map: mapsInstanceRef.current!,
            icon: {
              url: svgIcons.ambulance,
              scaledSize: new google.maps.Size(36, 42),
            },
            title: a.vehicle_number,
          });

          const info = new google.maps.InfoWindow({
            content: `<div class="p-2 text-slate-900"><strong class="block text-sm">Ambulance: ${a.vehicle_number}</strong><span class="text-xs">Type: ${a.ambulance_type}</span><br/><span class="text-xs text-emerald-600 font-bold">Distance: ${a.distance} km (${a.eta} mins)</span></div>`,
          });

          marker.addListener('click', () => info.open(mapsInstanceRef.current, marker));
          ambMarkersRef.current.push(marker);
        });
      }
    } catch (e) {
      console.warn("Failed loading nearby items", e);
    }
  };

  const handleCreateRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!lat || !lng) {
      showToast("Position Required", "Tap on the map to specify emergency coordinates.", "warning");
      return;
    }
    setGlobalLoading(true);
    try {
      const res = await axios.post('/requests/create.php', {
        patient_name: patientName,
        patient_phone: patientPhone,
        pickup_address: addressDesc,
        pickup_latitude: lat,
        pickup_longitude: lng,
        requested_ambulance_type: ambulanceType,
        emergency_type: emergencyType,
      });

      if (res.data && res.data.success) {
        showToast("Emergency Dispatched", "Booking successfully generated.", "success");
        navigate(`/citizen/track/${res.data.data.request_id}`);
      }
    } catch (err: any) {
      showToast("Dispatch Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleLoadChipAddress = (addr: any) => {
    setAddressDesc(addr.address);
    updateMarker(parseFloat(addr.latitude), parseFloat(addr.longitude));
    showToast("Address Selected", `Position focused on: ${addr.title}`, "success");
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-[calc(100vh-10rem)]">
      {/* Left Form controls */}
      <div className="lg:col-span-4 flex flex-col gap-6">
        <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4">
          <div>
            <h2 className="text-lg font-heading font-extrabold text-white">Emergency Request</h2>
            <p className="text-gray-400 text-xs mt-0.5">Specify patient details and request dispatch</p>
          </div>

          <form onSubmit={handleCreateRequest} className="space-y-4">
            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Patient Name</label>
              <input
                type="text"
                value={patientName}
                onChange={(e) => setPatientName(e.target.value)}
                placeholder="Full name of patient"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Emergency Phone</label>
              <input
                type="tel"
                value={patientPhone}
                onChange={(e) => setPatientPhone(e.target.value)}
                placeholder="Paramedic contact number"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Emergency Case Category</label>
              <div className="relative">
                <select
                  value={emergencyType}
                  onChange={(e) => setEmergencyType(e.target.value)}
                  required
                  className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-3 pr-8 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none cursor-pointer"
                >
                  <option value="" className="bg-slate-900">Select Category</option>
                  <option value="Cardiac Arrest / Chest Pain" className="bg-slate-900">Cardiac Arrest / Chest Pain</option>
                  <option value="Road Accident / Trauma" className="bg-slate-900">Road Accident / Trauma</option>
                  <option value="Severe Respiratory Distress" className="bg-slate-900">Severe Respiratory Distress</option>
                  <option value="Stroke / Neurological" className="bg-slate-900">Stroke / Neurological</option>
                  <option value="Pregnancy / Labor Emergency" className="bg-slate-900">Pregnancy / Labor Emergency</option>
                  <option value="Unconscious / Fainting" className="bg-slate-900">Unconscious / Fainting</option>
                  <option value="Other Medical Criticality" className="bg-slate-900">Other Medical Criticality</option>
                </select>
                <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                  {getLucideIcon('ChevronDown', { size: 14 })}
                </div>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Ambulance Tier Type</label>
              <div className="relative">
                <select
                  value={ambulanceType}
                  onChange={(e: any) => setAmbulanceType(e.target.value)}
                  required
                  className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 pl-3 pr-8 text-xs focus:outline-none focus:border-accentRed text-white transition appearance-none cursor-pointer"
                >
                  <option value="BLS" className="bg-slate-900">Basic Life Support (BLS)</option>
                  <option value="ALS" className="bg-slate-900">Advanced Life Support (ALS)</option>
                  <option value="CCU" className="bg-slate-900">Critical Care Unit (CCU)</option>
                </select>
                <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                  {getLucideIcon('ChevronDown', { size: 14 })}
                </div>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Street Address Details</label>
              <textarea
                value={addressDesc}
                onChange={(e) => setAddressDesc(e.target.value)}
                placeholder="Street address, landmark clues..."
                required
                rows={2}
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition resize-none"
              />
            </div>

            {/* Coordinates metadata */}
            <div className="grid grid-cols-2 gap-3 p-3 bg-slate-900/60 rounded-xl text-[10px] text-gray-400 font-mono border border-white/5">
              <div>LAT: <span className="text-white font-bold">{lat.toFixed(6)}</span></div>
              <div>LNG: <span className="text-white font-bold">{lng.toFixed(6)}</span></div>
            </div>

            {/* Saved addresses quick chips */}
            {savedLocs.length > 0 && (
              <div className="space-y-1.5 pt-2">
                <label className="text-[10px] uppercase font-bold text-gray-500 tracking-wider">Fast-Pin Stored Address</label>
                <div className="flex flex-wrap gap-1.5">
                  {savedLocs.map((addr) => (
                    <button
                      key={addr.address_id}
                      type="button"
                      onClick={() => handleLoadChipAddress(addr)}
                      className="px-2.5 py-1 text-[10px] font-bold rounded-lg border border-white/10 bg-slate-900/30 text-gray-300 hover:text-white transition flex items-center gap-1"
                    >
                      {getLucideIcon(addr.title.toLowerCase() === 'home' ? 'Home' : addr.title.toLowerCase() === 'work' ? 'Work' : 'MapPin', { size: 10, className: 'text-accentRed' })}
                      {addr.title}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <button
              type="submit"
              className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3.5 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs pulse-red"
            >
              {getLucideIcon('PhoneCall', { size: 14 })} Request Instant Dispatch
            </button>
          </form>
        </div>

        {/* Emergency Contacts Widget */}
        <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4">
          <div>
            <h2 className="text-sm font-heading font-extrabold text-white">Emergency Hotline Contacts</h2>
            <p className="text-gray-400 text-[10px] mt-0.5">Quick access dial numbers for instant support</p>
          </div>
          
          <div className="grid grid-cols-1 gap-2.5">
            <div className="flex items-center justify-between p-3 rounded-xl bg-red-500/10 border border-red-500/20">
              <div>
                <p className="text-xs font-extrabold text-white">SafeReach Command ER</p>
                <p className="text-[10px] text-red-400 font-semibold mt-0.5">Saveetha Campus Dispatch</p>
              </div>
              <a href="tel:+914466726616" className="px-2.5 py-1.5 bg-red-600 hover:bg-red-500 text-white text-[10px] font-bold rounded-lg transition shadow-md">
                +91 44 6672 6616
              </a>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-blue-500/10 border border-blue-500/20">
              <div>
                <p className="text-xs font-extrabold text-white">National Ambulance Services</p>
                <p className="text-[10px] text-blue-400 font-semibold mt-0.5">Primary Medical Response</p>
              </div>
              <a href="tel:102" className="px-2.5 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-[10px] font-bold rounded-lg transition shadow-md">
                Dial 102
              </a>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-white/5">
              <div>
                <p className="text-xs font-extrabold text-white">National Emergency Hotline</p>
                <p className="text-[10px] text-gray-400 font-semibold mt-0.5">Universal SOS Access</p>
              </div>
              <a href="tel:112" className="px-2.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-white text-[10px] font-bold rounded-lg transition border border-white/5">
                Dial 112
              </a>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-white/5">
              <div>
                <p className="text-xs font-extrabold text-white">Fire & Rescue Services</p>
                <p className="text-[10px] text-gray-400 font-semibold mt-0.5">Fire Accidents Support</p>
              </div>
              <a href="tel:101" className="px-2.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-white text-[10px] font-bold rounded-lg transition border border-white/5">
                Dial 101
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* Right Map */}
      <div className="lg:col-span-8 flex flex-col glass-panel rounded-2xl overflow-hidden border border-white/5 shadow-xl relative min-h-[300px] lg:min-h-0">
        <div className="absolute top-4 left-4 z-10 bg-slate-950/85 backdrop-blur border border-white/5 px-4 py-2.5 rounded-xl flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-ping"></div>
          <span className="text-[10px] uppercase tracking-wider font-extrabold text-white">Live System Network Matrix</span>
        </div>

        <button
          onClick={() => {
            if (navigator.geolocation) {
              navigator.geolocation.getCurrentPosition((pos) => {
                updateMarker(pos.coords.latitude, pos.coords.longitude);
              });
            }
          }}
          className="absolute bottom-4 right-4 z-10 p-3 bg-slate-950/90 hover:bg-slate-900 border border-white/5 text-white rounded-xl shadow-md transition"
        >
          {getLucideIcon('Crosshair', { size: 18 })}
        </button>

        <div ref={mapRef} className="flex-1 w-full h-full"></div>
      </div>
    </div>
  );
};

export default Dashboard;

import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { CheckCircle, Gauge, Heart, HelpCircle, Hospital, Phone, Star, Thermometer, Wind, XCircle, Truck } from 'lucide-react';

const iconMap: Record<string, any> = {
  CheckCircle, Gauge, Heart, HelpCircle, Hospital, Phone, Star, Thermometer, Wind, XCircle, Truck
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

const Tracker: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { showToast, setGlobalLoading } = useAuth();
  const navigate = useNavigate();

  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<google.maps.Map | null>(null);
  const patientPinRef = useRef<google.maps.Marker | null>(null);
  const ambPinRef = useRef<google.maps.Marker | null>(null);
  const hospPinRef = useRef<google.maps.Marker | null>(null);
  const directionsRendererRef = useRef<google.maps.DirectionsRenderer | null>(null);
  const lastRouteCoordsRef = useRef<{originLat: number, originLng: number, destLat: number, destLng: number} | null>(null);
  const initialBoundsFitRef = useRef<boolean>(false);

  const [activeRequest, setActiveRequest] = useState<any>(null);
  const [etaText, setEtaText] = useState('ETA: Calculating...');
  const [feedbackRating, setFeedbackRating] = useState(0);
  const [feedbackComments, setFeedbackComments] = useState('');
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const loadTrackingDetails = async () => {
    try {
      const res = await axios.get(`/requests/track.php?request_id=${id}`);
      if (res.data && res.data.success) {
        const data = res.data.data;
        setActiveRequest(data);
        updateTrackingMapPins(data);

        if (data.status === 'completed') {
          showToast("Missions Completed", "Paramedic dispatch arrived at destination hospital.", "success");
          setShowFeedbackModal(true);
        } else if (data.status === 'cancelled') {
          showToast("Incident Aborted", "The emergency request was cancelled.", "warning");
          navigate('/citizen');
        }
      }
    } catch (e: any) {
      console.warn("Failed tracking log retrieve", e);
    }
  };

  useEffect(() => {
    if (id) {
      loadTrackingDetails();
      const interval = setInterval(loadTrackingDetails, 3000);
      return () => clearInterval(interval);
    }
  }, [id]);

  const updateTrackingMapPins = (data: any) => {
    if (!mapContainerRef.current || typeof google === 'undefined') return;

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = new google.maps.Map(mapContainerRef.current, {
        center: { lat: 13.0232, lng: 80.0175 },
        zoom: 14,
        styles: googleMapsDarkStyle,
        mapTypeControl: false,
        streetViewControl: false,
      });
    }

    const bounds = new google.maps.LatLngBounds();
    let hasBounds = false;

    // 1. Patient pickup pin
    if (data.pickup) {
      const pos = { lat: parseFloat(data.pickup.latitude), lng: parseFloat(data.pickup.longitude) };
      if (!patientPinRef.current) {
        patientPinRef.current = new google.maps.Marker({
          position: pos,
          map: mapInstanceRef.current!,
          icon: { url: svgIcons.patient, scaledSize: new google.maps.Size(36, 42) },
        });
      } else {
        patientPinRef.current.setPosition(pos);
      }
      bounds.extend(pos);
      hasBounds = true;
    } else {
      if (patientPinRef.current) {
        patientPinRef.current.setMap(null);
        patientPinRef.current = null;
      }
    }

    // 2. Ambulance pin
    if (data.assigned_ambulance && data.assigned_ambulance.latitude && data.assigned_ambulance.longitude) {
      const pos = { lat: parseFloat(data.assigned_ambulance.latitude), lng: parseFloat(data.assigned_ambulance.longitude) };
      if (!ambPinRef.current) {
        ambPinRef.current = new google.maps.Marker({
          position: pos,
          map: mapInstanceRef.current!,
          icon: { url: svgIcons.ambulance, scaledSize: new google.maps.Size(36, 42) },
        });
      } else {
        ambPinRef.current.setPosition(pos);
      }
      bounds.extend(pos);
      hasBounds = true;
    } else {
      if (ambPinRef.current) {
        ambPinRef.current.setMap(null);
        ambPinRef.current = null;
      }
    }

    // 3. Hospital destination pin
    if (data.destination_hospital && data.destination_hospital.latitude && data.destination_hospital.longitude) {
      const pos = { lat: parseFloat(data.destination_hospital.latitude), lng: parseFloat(data.destination_hospital.longitude) };
      if (!hospPinRef.current) {
        hospPinRef.current = new google.maps.Marker({
          position: pos,
          map: mapInstanceRef.current!,
          icon: { url: svgIcons.hospital, scaledSize: new google.maps.Size(36, 42) },
        });
      } else {
        hospPinRef.current.setPosition(pos);
      }
      bounds.extend(pos);
      hasBounds = true;
    } else {
      if (hospPinRef.current) {
        hospPinRef.current.setMap(null);
        hospPinRef.current = null;
      }
    }

    if (hasBounds && !initialBoundsFitRef.current) {
      mapInstanceRef.current.fitBounds(bounds);
      initialBoundsFitRef.current = true;
    }

    // Draw route directions
    let origin: google.maps.LatLng | null = null;
    let destination: google.maps.LatLng | null = null;

    if (data.status === 'accepted' || data.status === 'arrived') {
      if (data.assigned_ambulance && data.assigned_ambulance.latitude) {
        origin = new google.maps.LatLng(data.assigned_ambulance.latitude, data.assigned_ambulance.longitude);
        destination = new google.maps.LatLng(data.pickup.latitude, data.pickup.longitude);
      }
    } else if (data.status === 'boarded' && data.destination_hospital) {
      origin = data.assigned_ambulance?.latitude
        ? new google.maps.LatLng(data.assigned_ambulance.latitude, data.assigned_ambulance.longitude)
        : new google.maps.LatLng(data.pickup.latitude, data.pickup.longitude);
      destination = new google.maps.LatLng(data.destination_hospital.latitude, data.destination_hospital.longitude);
    }

    if (origin && destination) {
      if (!directionsRendererRef.current) {
        directionsRendererRef.current = new google.maps.DirectionsRenderer({
          map: mapInstanceRef.current,
          suppressMarkers: true,
          polylineOptions: { strokeColor: "#ef4444", strokeOpacity: 0.8, strokeWeight: 6 },
        });
      }

      // Check if coordinates moved significantly to avoid excessive API requests
      const currentOriginLat = origin.lat();
      const currentOriginLng = origin.lng();
      const currentDestLat = destination.lat();
      const currentDestLng = destination.lng();

      const lastCoords = lastRouteCoordsRef.current;
      const hasMovedSignificantly = !lastCoords ||
        Math.abs(lastCoords.originLat - currentOriginLat) > 0.0001 ||
        Math.abs(lastCoords.originLng - currentOriginLng) > 0.0001 ||
        Math.abs(lastCoords.destLat - currentDestLat) > 0.0001 ||
        Math.abs(lastCoords.destLng - currentDestLng) > 0.0001;

      if (hasMovedSignificantly) {
        lastRouteCoordsRef.current = {
          originLat: currentOriginLat,
          originLng: currentOriginLng,
          destLat: currentDestLat,
          destLng: currentDestLng
        };

        const directionsService = new google.maps.DirectionsService();
        directionsService.route({
          origin,
          destination,
          travelMode: google.maps.TravelMode.DRIVING,
        }, (result, status) => {
          if (status === 'OK' && result && directionsRendererRef.current) {
            directionsRendererRef.current.setDirections(result);
            const leg = result.routes[0]?.legs[0];
            if (leg) {
              setEtaText(`ETA: ${leg.duration?.text} (${leg.distance?.text})`);
            }
          }
        });
      }
    } else {
      if (directionsRendererRef.current) {
        directionsRendererRef.current.setMap(null);
        directionsRendererRef.current = null;
      }
      lastRouteCoordsRef.current = null;
    }
  };

  const handleCancelIncident = async () => {
    if (!confirm("Confirm emergency request cancellation?")) return;
    setGlobalLoading(true);
    try {
      await axios.post('/requests/update_status.php', {
        request_id: parseInt(id!),
        status: 'cancelled',
      });
      showToast("Request Cancelled", "Incident successfully aborted by patient.", "warning");
      navigate('/citizen');
    } catch (e: any) {
      showToast("Cancellation Failed", e.response?.data?.message || e.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleSubmitFeedback = async (e: React.FormEvent) => {
    e.preventDefault();
    if (feedbackRating < 1) {
      showToast("Rating Missing", "Please select a star rating first.", "warning");
      return;
    }
    setGlobalLoading(true);
    try {
      await axios.post('/requests/feedback.php', {
        request_id: parseInt(id!),
        rating: feedbackRating,
        comments: feedbackComments,
      });
      showToast("Feedback Logged", "Star ratings successfully saved.", "success");
      setShowFeedbackModal(false);
      navigate('/citizen');
    } catch (err: any) {
      showToast("Submit Failed", err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 min-h-[calc(100vh-8rem)]">
      {/* Left panel checkpoints details */}
      <div className="lg:col-span-5 flex flex-col gap-6">
        <div className="glass-panel rounded-3xl p-8 border border-white/10 shadow-2xl space-y-8 flex-1 relative overflow-hidden">
          {/* Subtle glow background */}
          <div className="absolute top-0 right-0 w-64 h-64 bg-accentRed/10 rounded-full blur-[100px] pointer-events-none -mr-32 -mt-32"></div>

          <div className="flex justify-between items-start relative z-10">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xs bg-slate-900 border border-white/10 px-3 py-1.5 rounded-xl text-accentRed font-bold font-mono tracking-widest shadow-inner">
                  INCIDENT #{id}
                </span>
                {activeRequest?.status === 'pending' && (
                  <span className="text-xs uppercase font-extrabold text-white animate-pulse bg-accentRed px-3 py-1.5 rounded-xl shadow-[0_0_15px_rgba(239,68,68,0.5)]">
                    Searching...
                  </span>
                )}
              </div>
              <h2 className="text-3xl font-heading font-extrabold text-white tracking-tight">Live Tracking</h2>
              <p className="text-gray-400 text-sm mt-1">Real-time emergency telemetry</p>
            </div>
          </div>

          {/* Huge ETA Display if Assigned */}
          {activeRequest?.assigned_ambulance && (
            <div className="bg-gradient-to-br from-slate-900 to-slate-950 rounded-2xl p-6 border border-emerald-500/30 relative overflow-hidden shadow-xl">
              <div className="absolute top-0 left-0 w-full h-1 bg-emerald-500"></div>
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-full bg-emerald-500/20 flex items-center justify-center border border-emerald-500/40 text-emerald-400">
                  {getLucideIcon('Truck', { size: 28 })}
                </div>
                <div>
                  <h3 className="text-emerald-400 font-bold text-xs uppercase tracking-widest mb-1">Estimated Arrival</h3>
                  <div className="text-3xl font-heading font-extrabold text-white">{etaText.replace('ETA: ', '')}</div>
                </div>
              </div>
            </div>
          )}

          {/* Timeline Process */}
          <div className="space-y-6 pt-4 relative z-10">
            {[
              { status: 'pending', title: 'Pending Match', desc: 'Locating nearby ambulances' },
              { status: 'accepted', title: 'Unit Dispatched', desc: 'Ambulance is moving to pickup' },
              { status: 'arrived', title: 'Arrived at Scene', desc: 'Responders are boarding patient' },
              { status: 'boarded', title: 'Patient Loaded', desc: 'En-route to destination ER' },
            ].map((step, idx) => {
              const currentStatus = activeRequest?.status;
              const isCompleted =
                currentStatus === 'completed' ||
                (step.status === 'pending' && ['accepted', 'arrived', 'boarded'].includes(currentStatus)) ||
                (step.status === 'accepted' && ['arrived', 'boarded'].includes(currentStatus)) ||
                (step.status === 'arrived' && ['boarded'].includes(currentStatus)) ||
                currentStatus === step.status;

              const isActive = currentStatus === step.status;

              return (
                <div key={step.status} className="flex gap-5 relative items-start">
                  {idx < 3 && (
                    <div className={`absolute left-[19px] top-10 bottom-[-24px] w-1 rounded-full ${isCompleted ? 'bg-accentRed shadow-[0_0_10px_rgba(239,68,68,0.5)]' : 'bg-slate-800'}`}></div>
                  )}

                  <div className={`w-10 h-10 rounded-full border-2 flex items-center justify-center font-extrabold text-sm z-10 transition-all duration-300 ${
                    isCompleted
                      ? 'bg-accentRed border-accentRed text-white shadow-[0_0_15px_rgba(239,68,68,0.4)]'
                      : isActive
                      ? 'bg-slate-900 border-accentRed text-accentRed animate-pulse'
                      : 'bg-slate-900 border-slate-700 text-slate-500'
                  }`}>
                    {isCompleted ? getLucideIcon('CheckCircle', { size: 18 }) : idx + 1}
                  </div>

                  <div className="flex-1 pt-1">
                    <h4 className={`text-base font-extrabold ${isActive || isCompleted ? 'text-white' : 'text-slate-500'}`}>
                      {step.title}
                    </h4>
                    <p className={`text-xs mt-1 ${isActive || isCompleted ? 'text-gray-400' : 'text-slate-600'}`}>{step.desc}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Assigned Unit Panel */}
          {activeRequest?.assigned_ambulance && (
            <div className="bg-slate-900/50 rounded-2xl p-5 border border-white/5 space-y-4 relative z-10">
              <div className="flex justify-between items-center">
                <div>
                  <h4 className="text-sm font-extrabold text-white flex items-center gap-2">
                    {activeRequest.assigned_ambulance.driver_name}
                    <span className="text-[10px] bg-blue-500/20 text-blue-400 px-2 py-0.5 rounded uppercase tracking-wider border border-blue-500/30">Paramedic</span>
                  </h4>
                  <p className="text-xs text-gray-400 mt-1 font-mono bg-black/20 inline-block px-2 py-1 rounded-lg border border-white/5">
                    Vehicle: <span className="text-white font-bold">{activeRequest.assigned_ambulance.vehicle_number}</span>
                  </p>
                </div>
                <a
                  href={`tel:${activeRequest.assigned_ambulance.driver_phone}`}
                  className="w-12 h-12 bg-white/5 hover:bg-white/10 border border-white/10 rounded-full text-white transition flex items-center justify-center flex-shrink-0"
                >
                  {getLucideIcon('Phone', { size: 20 })}
                </a>
              </div>
            </div>
          )}

          {/* Destination Hospital */}
          {activeRequest?.destination_hospital && (
            <div className="bg-slate-900/50 rounded-2xl p-5 border border-white/5 flex items-center gap-4 relative z-10">
              <div className="w-12 h-12 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center border border-blue-500/20 flex-shrink-0">
                {getLucideIcon('Hospital', { size: 24 })}
              </div>
              <div>
                <h4 className="text-sm font-extrabold text-white">{activeRequest.destination_hospital.name}</h4>
                <p className="text-xs text-gray-400 mt-1 line-clamp-1">{activeRequest.destination_hospital.address}</p>
              </div>
            </div>
          )}

          {/* Patient Vitals Stream Panel */}
          {activeRequest?.vitals && (
            <div className="bg-slate-900/50 rounded-2xl p-5 border border-white/5 space-y-4 relative z-10">
              <div className="flex justify-between items-center">
                <h3 className="text-sm font-extrabold text-white flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-accentRed animate-ping"></span>
                  Live Vitals
                </h3>
                <span className={`px-3 py-1 rounded-lg font-bold text-[10px] uppercase border ${
                  activeRequest.vitals.condition === 'critical' ? 'bg-red-500/20 border-red-500/40 text-red-400 animate-pulse shadow-[0_0_10px_rgba(239,68,68,0.3)]' : 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
                }`}>
                  Condition: {activeRequest.vitals.condition}
                </span>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div className="p-4 bg-slate-950/50 border border-white/5 rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider block mb-1">Heart Rate</span>
                  <span className="text-2xl font-heading font-extrabold text-white">{activeRequest.vitals.heart_rate} <span className="text-xs font-normal text-gray-500">BPM</span></span>
                </div>
                <div className="p-4 bg-slate-950/50 border border-white/5 rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider block mb-1">Blood Pressure</span>
                  <span className="text-2xl font-heading font-extrabold text-white">{activeRequest.vitals.blood_pressure}</span>
                </div>
                <div className="p-4 bg-slate-950/50 border border-white/5 rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider block mb-1">Oxygen SpO2</span>
                  <span className="text-2xl font-heading font-extrabold text-white">{activeRequest.vitals.oxygen_saturation} <span className="text-xs font-normal text-gray-500">%</span></span>
                </div>
                <div className="p-4 bg-slate-950/50 border border-white/5 rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider block mb-1">Temperature</span>
                  <span className="text-2xl font-heading font-extrabold text-white">{activeRequest.vitals.temperature} <span className="text-xs font-normal text-gray-500">°C</span></span>
                </div>
              </div>
            </div>
          )}

          {activeRequest?.status !== 'completed' && activeRequest?.status !== 'cancelled' && (
            <button
              onClick={handleCancelIncident}
              className="w-full py-4 bg-slate-950 hover:bg-red-950/40 border border-white/10 hover:border-red-900/50 text-gray-400 hover:text-red-500 font-extrabold rounded-2xl transition-all duration-300 text-sm flex items-center justify-center gap-2 relative z-10"
            >
              {getLucideIcon('XCircle', { size: 18 })} Cancel Emergency Call
            </button>
          )}
        </div>
      </div>

      {/* Right Map */}
      <div className="lg:col-span-7 flex flex-col relative min-h-[500px] lg:min-h-0 rounded-3xl overflow-hidden border-2 border-white/10 shadow-2xl">
        {/* Map Overlay Badges */}
        <div className="absolute top-6 left-6 z-10 flex gap-3">
          <div className="bg-slate-950/90 backdrop-blur-md border border-white/10 px-4 py-2 rounded-xl text-xs uppercase font-extrabold text-white flex items-center gap-2 shadow-2xl">
            <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></div>
            LIVE GPS TRACKING
          </div>
        </div>

        <div ref={mapContainerRef} className="flex-1 w-full h-full bg-slate-900"></div>
        
        {/* Gradient overlay for bottom edge of map to blend with background if needed */}
        <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-slate-950/80 to-transparent pointer-events-none z-10"></div>
      </div>

      {/* Review Modal popup */}
      {showFeedbackModal && (
        <div className="fixed inset-0 bg-slate-950/80 z-50 flex items-center justify-center p-6 backdrop-blur-sm">
          <div className="glass-panel border border-white/5 rounded-2xl p-6 max-w-sm w-full space-y-6 shadow-2xl animate-scale-up">
            <div className="text-center space-y-2">
              <div className="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto shadow">
                {getLucideIcon('CheckCircle', { size: 36 })}
              </div>
              <h2 className="text-lg font-heading font-extrabold text-white">Missions Completed</h2>
              <p className="text-gray-400 text-xs">Help us score response paramedic care</p>
            </div>

            <form onSubmit={handleSubmitFeedback} className="space-y-4">
              <div className="space-y-2 text-center">
                <label className="text-xs font-semibold text-gray-300">Rate Care Service</label>
                <div className="flex justify-center gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setFeedbackRating(star)}
                      className="p-1 focus:outline-none"
                    >
                      {getLucideIcon('Star', {
                        size: 32,
                        className: star <= feedbackRating ? 'text-amber-500 fill-amber-500' : 'text-gray-650 hover:text-amber-500',
                      })}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-gray-300">Comments or Suggestions</label>
                <textarea
                  value={feedbackComments}
                  onChange={(e) => setFeedbackComments(e.target.value)}
                  placeholder="Paramedic response remarks..."
                  rows={3}
                  className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition resize-none"
                />
              </div>

              <button
                type="submit"
                className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3 rounded-xl transition text-xs shadow-lg"
              >
                Submit Incident Review
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Tracker;

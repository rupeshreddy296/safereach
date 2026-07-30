import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { HelpCircle } from 'lucide-react';

const iconMap: Record<string, any> = {
  HelpCircle
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

const Addresses: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();

  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<google.maps.Map | null>(null);
  const pinMarkerRef = useRef<google.maps.Marker | null>(null);
  const addressMarkersRef = useRef<google.maps.Marker[]>([]);

  // Stored lists
  const [addresses, setAddresses] = useState<any[]>([]);

  // Form states
  const [title, setTitle] = useState('');
  const [subtitle, setSubtitle] = useState('');
  const [address, setAddress] = useState('');
  const [lat, setLat] = useState<number | string>('--');
  const [lng, setLng] = useState<number | string>('--');

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const fetchAddresses = async () => {
    try {
      const res = await axios.get('/user/addresses.php');
      if (res.data && res.data.success) {
        setAddresses(res.data.data);
        plotAddressesOnMap(res.data.data);
      }
    } catch (err: any) {
      showToast("Fetch Failed", err.message, "error");
    }
  };

  useEffect(() => {
    fetchAddresses();
  }, []);

  // Set up Map
  useEffect(() => {
    if (mapContainerRef.current && typeof google !== 'undefined') {
      if (!mapInstanceRef.current) {
        mapInstanceRef.current = new google.maps.Map(mapContainerRef.current, {
          center: { lat: 13.0232, lng: 80.0175 },
          zoom: 13,
          styles: googleMapsDarkStyle,
          mapTypeControl: false,
          streetViewControl: false,
        });

        mapInstanceRef.current.addListener('click', (e: google.maps.MapMouseEvent) => {
          if (e.latLng) {
            const latitude = parseFloat(e.latLng.lat().toFixed(6));
            const longitude = parseFloat(e.latLng.lng().toFixed(6));
            setLat(latitude);
            setLng(longitude);

            if (pinMarkerRef.current) {
              pinMarkerRef.current.setPosition(e.latLng);
            } else if (mapInstanceRef.current) {
              pinMarkerRef.current = new google.maps.Marker({
                position: e.latLng,
                map: mapInstanceRef.current!,
                icon: {
                  url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 30" width="30" height="36">
                        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#f59e0b" stroke="#ffffff" stroke-width="1.5"/>
                    </svg>
                  `),
                  scaledSize: new google.maps.Size(30, 36),
                },
              });
            }
          }
        });
      }
    }
  }, []);

  const plotAddressesOnMap = (list: any[]) => {
    if (typeof google === 'undefined' || !mapInstanceRef.current) return;

    // Clear old list pins
    addressMarkersRef.current.forEach((m) => m.setMap(null));
    addressMarkersRef.current = [];

    const bounds = new google.maps.LatLngBounds();
    let hasBounds = false;

    list.forEach((addr) => {
      const pos = { lat: parseFloat(addr.latitude), lng: parseFloat(addr.longitude) };
      const marker = new google.maps.Marker({
        position: pos,
        map: mapInstanceRef.current!,
        icon: {
          url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 30" width="30" height="36">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#3b82f6" stroke="#ffffff" stroke-width="1.5"/>
            </svg>
          `),
          scaledSize: new google.maps.Size(30, 36),
        },
        title: addr.title,
      });

      bounds.extend(pos);
      hasBounds = true;
      addressMarkersRef.current.push(marker);
    });

    if (hasBounds) {
      mapInstanceRef.current.fitBounds(bounds);
    }
  };

  const handleSaveAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (lat === '--' || lng === '--') {
      showToast("Select Location", "Click on the map container to pin coordinates.", "warning");
      return;
    }
    setGlobalLoading(true);
    try {
      const res = await axios.post('/user/addresses.php', {
        title,
        subtitle,
        address,
        latitude: lat,
        longitude: lng,
      });

      if (res.data && res.data.success) {
        showToast("Address Saved", "Location details saved to your profile.", "success");
        setTitle('');
        setSubtitle('');
        setAddress('');
        setLat('--');
        setLng('--');
        if (pinMarkerRef.current) {
          pinMarkerRef.current.setMap(null);
          pinMarkerRef.current = null;
        }
        fetchAddresses();
      }
    } catch (err: any) {
      showToast("Save Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleDeleteAddress = async (addrId: number) => {
    if (!confirm("Are you sure you want to delete this saved location?")) return;
    setGlobalLoading(true);
    try {
      const res = await axios.delete(`/user/addresses.php?id=${addrId}`);
      if (res.data && res.data.success) {
        showToast("Address Deleted", "Location removed from profile lists.", "success");
        fetchAddresses();
      }
    } catch (err: any) {
      showToast("Delete Failed", err.response?.data?.message || err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-[calc(100vh-10rem)]">
      {/* Left Registration Form */}
      <div className="lg:col-span-4 flex flex-col gap-6">
        <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl space-y-4">
          <div>
            <h2 className="text-lg font-heading font-extrabold text-white">Save New Address</h2>
            <p className="text-gray-400 text-xs mt-0.5">Define custom titles for one-click dispatches</p>
          </div>

          <form onSubmit={handleSaveAddress} className="space-y-4">
            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Location Name Title</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Home, Office, Parents Place"
                required
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Subtitle Description</label>
              <input
                type="text"
                value={subtitle}
                onChange={(e) => setSubtitle(e.target.value)}
                placeholder="e.g. Near Bus Stand"
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-semibold text-gray-300">Street Address Details</label>
              <textarea
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="Apartment, building number, street..."
                required
                rows={3}
                className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2.5 px-3 text-xs focus:outline-none focus:border-accentRed text-white transition resize-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 p-3 bg-slate-900/60 rounded-xl text-[10px] text-gray-400 font-mono border border-white/5">
              <div>LAT: <span className="text-white font-bold">{lat}</span></div>
              <div>LNG: <span className="text-white font-bold">{lng}</span></div>
            </div>

            <button
              type="submit"
              className="w-full bg-accentRed hover:bg-red-700 text-white font-bold py-3 rounded-xl transition text-xs shadow-md"
            >
              Save Address
            </button>
          </form>
        </div>
      </div>

      {/* Right List Directory & Maps */}
      <div className="lg:col-span-8 grid grid-rows-2 gap-6 min-h-[400px] lg:min-h-0">
        {/* Map Panel */}
        <div className="glass-panel rounded-2xl overflow-hidden border border-white/5 shadow-xl relative">
          <div ref={mapContainerRef} className="w-full h-full"></div>
        </div>

        {/* Directory Panel */}
        <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl overflow-y-auto max-h-[300px] space-y-4">
          <h3 className="text-sm font-extrabold text-white">Stored Address Directory</h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {addresses.length === 0 ? (
              <p className="text-xs text-gray-500 col-span-2 text-center py-6">
                No saved address cards. Tap coordinate map to register new nodes.
              </p>
            ) : (
              addresses.map((item) => (
                <div key={item.address_id} className="glass-card rounded-xl p-4 border border-white/5 space-y-3 flex flex-col justify-between">
                  <div className="space-y-1">
                    <div className="flex justify-between items-start">
                      <h4 className="text-xs font-extrabold text-white flex items-center gap-1">
                        {getLucideIcon(item.title.toLowerCase() === 'home' ? 'Home' : item.title.toLowerCase() === 'work' ? 'Work' : 'MapPin', { size: 12, className: 'text-accentRed' })}
                        {item.title}
                      </h4>
                      {item.subtitle && (
                        <span className="text-[9px] bg-slate-900 border border-white/5 px-2 py-0.5 rounded text-gray-400">
                          {item.subtitle}
                        </span>
                      )}
                    </div>
                    <p className="text-[10px] text-gray-400 leading-relaxed">{item.address}</p>
                  </div>
                  <div className="flex justify-between items-center pt-2 border-t border-white/5">
                    <span className="text-[9px] font-mono text-gray-500">
                      {parseFloat(item.latitude).toFixed(4)}, {parseFloat(item.longitude).toFixed(4)}
                    </span>
                    <button
                      onClick={() => handleDeleteAddress(item.address_id)}
                      className="text-[9px] font-bold text-accentRed hover:underline"
                    >
                      Delete Address
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Addresses;

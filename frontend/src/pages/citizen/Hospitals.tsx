import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axios from '../../api/axios';
import { HelpCircle, RefreshCw } from 'lucide-react';

const iconMap: Record<string, any> = {
  HelpCircle, RefreshCw
};

const Hospitals: React.FC = () => {
  const { showToast, setGlobalLoading } = useAuth();
  const [hospitals, setHospitals] = useState<any[]>([]);

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const loadHospitals = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/hospitals/list.php');
      if (res.data && res.data.success) {
        setHospitals(res.data.data);
      }
    } catch (err: any) {
      showToast("Load Failed", err.message, "error");
    } finally {
      setGlobalLoading(false);
    }
  };

  useEffect(() => {
    loadHospitals();
  }, []);

  return (
    <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl max-w-4xl mx-auto space-y-6">
      <div className="flex justify-between items-center pb-2 border-b border-white/5">
        <div>
          <h2 className="text-lg font-heading font-extrabold text-white">Emergency ER Facilities Directory</h2>
          <p className="text-gray-400 text-xs mt-0.5">Real-time ICU/NICU bed availability matrices</p>
        </div>
        <button
          onClick={loadHospitals}
          className="px-3 py-1.5 bg-slate-900 hover:bg-slate-850 border border-white/5 rounded-xl text-[10px] font-bold text-white transition flex items-center gap-1.5"
        >
          {getLucideIcon('RefreshCw', { size: 12 })} Refresh Table
        </button>
      </div>

      <div className="overflow-x-auto rounded-xl border border-white/5 bg-slate-950/20">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="bg-slate-900/60 border-b border-white/5 text-gray-400 font-bold uppercase tracking-wider">
              <th className="py-3.5 px-4 font-bold">Facility Name</th>
              <th className="py-3.5 px-4 font-bold">ER Contact</th>
              <th className="py-3.5 px-4 font-bold text-center">Available ICU Beds</th>
              <th className="py-3.5 px-4 font-bold text-center">Available NICU Beds</th>
              <th className="py-3.5 px-4 font-bold text-center">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {hospitals.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-gray-500">
                  No emergency facilities listed.
                </td>
              </tr>
            ) : (
              hospitals.map((h) => (
                <tr key={h.hospital_id} className="hover:bg-white/[0.01] transition">
                  <td className="py-4 px-4 font-bold text-white">
                    {h.name}
                    <span className="block text-[10px] text-gray-400 font-normal mt-0.5">{h.address}</span>
                  </td>
                  <td className="py-4 px-4 text-gray-300 font-medium">{h.phone_number || 'N/A'}</td>
                  <td className="py-4 px-4 text-center font-bold text-blue-400 text-sm">
                    {h.available_icu_beds}
                  </td>
                  <td className="py-4 px-4 text-center font-bold text-purple-400 text-sm">
                    {h.available_nicu_beds}
                  </td>
                  <td className="py-4 px-4 text-center">
                    <span className={`px-2 py-0.5 rounded font-extrabold text-[9px] uppercase ${
                      h.status === 'active'
                        ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400'
                        : 'bg-red-500/10 border border-red-500/20 text-red-400'
                    }`}>
                      {h.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Hospitals;

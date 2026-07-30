import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { ChevronDown } from 'lucide-react';

const AdminHospitals: React.FC = () => {
  const { showToast } = useAuth();
  const [hospitals, setHospitals] = useState<any[]>([]);
  
  const [hospId, setHospId] = useState<number | null>(null);
  const [hospName, setHospName] = useState('');
  const [hospAddress, setHospAddress] = useState('');
  const [hospPhone, setHospPhone] = useState('');
  const [hospIcu, setHospIcu] = useState(0);
  const [hospNicu, setHospNicu] = useState(0);
  const [hospStatus, setHospStatus] = useState('active');
  const [formTitle, setFormTitle] = useState('Add New Hospital');

  const fetchHospitals = async () => {
    try {
      const res = await axios.get('/admin/hospitals.php');
      if (res.data?.success) {
        setHospitals(res.data.data);
      }
    } catch (e) {
      showToast('Error', 'Failed to fetch hospitals', 'error');
    }
  };

  useEffect(() => {
    fetchHospitals();
  }, []);

  const handleResetForm = () => {
    setHospId(null);
    setHospName('');
    setHospAddress('');
    setHospPhone('');
    setHospIcu(0);
    setHospNicu(0);
    setHospStatus('active');
    setFormTitle('Add New Hospital');
  };

  const handleEditHospital = (h: any) => {
    setHospId(h.hospital_id);
    setHospName(h.name);
    setHospAddress(h.address || '');
    setHospPhone(h.phone_number || '');
    setHospIcu(h.available_icu_beds || 0);
    setHospNicu(h.available_nicu_beds || 0);
    setHospStatus(h.status || 'active');
    setFormTitle('Edit Hospital Node');
  };

  const handleDeleteHospital = async (id: number) => {
    if (!window.confirm('Are you sure you want to remove this hospital?')) return;
    try {
      const res = await axios.delete(`/admin/hospitals.php?id=${id}`);
      if (res.data?.success) {
        showToast('Deleted', 'Hospital removed from network', 'success');
        fetchHospitals();
      }
    } catch (e) {
      showToast('Error', 'Failed to delete hospital', 'error');
    }
  };

  const handleHospitalSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        name: hospName,
        address: hospAddress,
        phone_number: hospPhone,
        available_icu_beds: hospIcu,
        available_nicu_beds: hospNicu,
        status: hospStatus
      };

      if (hospId) {
        await axios.put(`/admin/hospitals.php?id=${hospId}`, payload);
        showToast('Updated', 'Hospital node updated successfully', 'success');
      } else {
        await axios.post('/admin/hospitals.php', payload);
        showToast('Created', 'Hospital registered in network', 'success');
      }
      
      handleResetForm();
      fetchHospitals();
    } catch (error) {
      showToast('Submission Failed', 'Could not save hospital node data', 'error');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-heading font-extrabold text-white">Hospital Management</h1>
          <p className="text-xs text-gray-400">Manage emergency facilities and bed availability matrices</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Form */}
        <div className="lg:col-span-4 glass-panel rounded-2xl p-5 border border-white/5 shadow-xl space-y-4">
          <div>
            <h2 className="text-sm font-extrabold text-white">{formTitle}</h2>
            <p className="text-[10px] text-gray-400 mt-0.5">Setup hospital parameters and bed counts</p>
          </div>

          <form onSubmit={handleHospitalSubmit} className="space-y-3.5">
            <div className="space-y-1">
              <label className="text-[10px] font-semibold text-gray-350">Hospital Name</label>
              <input type="text" value={hospName} onChange={(e) => setHospName(e.target.value)} required className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs text-white focus:outline-none" />
            </div>

            <div className="space-y-1">
              <label className="text-[10px] font-semibold text-gray-355">Street Address</label>
              <textarea value={hospAddress} onChange={(e) => setHospAddress(e.target.value)} required rows={2} className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs text-white focus:outline-none resize-none" />
            </div>

            <div className="space-y-1">
              <label className="text-[10px] font-semibold text-gray-350">Emergency Contact Phone</label>
              <input type="tel" value={hospPhone} onChange={(e) => setHospPhone(e.target.value)} className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs text-white focus:outline-none" />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-gray-300">ICU Beds</label>
                <input type="number" value={hospIcu} onChange={(e) => setHospIcu(parseInt(e.target.value) || 0)} min="0" className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs text-white focus:outline-none" />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-gray-300">NICU Beds</label>
                <input type="number" value={hospNicu} onChange={(e) => setHospNicu(parseInt(e.target.value) || 0)} min="0" className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 px-3 text-xs text-white focus:outline-none" />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[10px] font-semibold text-gray-300">Facility Status</label>
              <div className="relative">
                <select value={hospStatus} onChange={(e: any) => setHospStatus(e.target.value)} className="w-full bg-slate-900/50 border border-white/10 rounded-xl py-2 pl-3 pr-8 text-xs text-white focus:outline-none appearance-none cursor-pointer">
                  <option value="active">Active System Node</option>
                  <option value="inactive">Temporarily Inactive</option>
                </select>
                <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-500">
                  <ChevronDown size={14} />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-2">
              <button type="submit" className="bg-accentRed hover:bg-red-700 text-white font-bold py-2 rounded-xl text-xs transition shadow-md">
                {hospId ? 'Save Changes' : 'Register Node'}
              </button>
              <button type="button" onClick={handleResetForm} className="bg-slate-900 hover:bg-slate-850 border border-white/5 text-gray-400 hover:text-white font-bold py-2 rounded-xl text-xs transition">
                Reset Form
              </button>
            </div>
          </form>
        </div>

        {/* List */}
        <div className="lg:col-span-8 glass-panel rounded-2xl p-5 border border-white/5 shadow-xl space-y-4">
          <h3 className="text-xs font-extrabold text-white">Emergency Facilities Network Directory</h3>
          <div className="overflow-x-auto rounded-xl border border-white/5 bg-slate-950/20">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-900/60 border-b border-white/5 text-gray-400 font-bold uppercase tracking-wider">
                  <th className="py-2.5 px-3">Hospital Node Details</th>
                  <th className="py-2.5 px-3">ER Contact</th>
                  <th className="py-2.5 px-3 text-center">ICU / NICU Beds</th>
                  <th className="py-2.5 px-3 text-center">Status</th>
                  <th className="py-2.5 px-3 text-center">Controls</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {hospitals.map((h) => (
                  <tr key={h.hospital_id} className="hover:bg-white/[0.01] transition text-[11px]">
                    <td className="py-3 px-3 font-bold text-white">
                      {h.name}
                      <span className="block text-[9px] text-gray-500 font-normal mt-0.5">{h.address}</span>
                    </td>
                    <td className="py-3 px-3 text-gray-300 font-medium">{h.phone_number || 'N/A'}</td>
                    <td className="py-3 px-3 text-center">
                      <span className="text-blue-400 font-bold">ICU: {h.available_icu_beds}</span>
                      <span className="mx-1 text-gray-700">|</span>
                      <span className="text-purple-400 font-bold">NICU: {h.available_nicu_beds}</span>
                    </td>
                    <td className="py-3 px-3 text-center">
                      <span className={`px-2 py-0.5 rounded font-extrabold text-[8px] uppercase ${
                        h.status === 'active' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-red-500/10 text-red-400 border border-red-500/20'
                      }`}>
                        {h.status}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-center">
                      <div className="flex gap-2 justify-center">
                        <button onClick={() => handleEditHospital(h)} className="text-[10px] font-bold text-blue-400 hover:underline">
                          Edit
                        </button>
                        <button onClick={() => handleDeleteHospital(h.hospital_id)} className="text-[10px] font-bold text-accentRed hover:underline">
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminHospitals;
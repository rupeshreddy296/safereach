import React, { useState, useEffect } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Search, User, Phone, Mail, Trash2, Edit2, Plus, X } from 'lucide-react';

const AdminCitizens: React.FC = () => {
  const { setGlobalLoading, showToast } = useAuth();
  const [citizens, setCitizens] = useState<any[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Modal states
  const [showModal, setShowModal] = useState(false);
  const [editingCitizen, setEditingCitizen] = useState<any>(null);
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const fetchCitizens = async () => {
    setGlobalLoading(true);
    try {
      const res = await axios.get('/admin/users.php');
      if (res.data?.success) {
        const filtered = res.data.data.filter((u: any) => u.role === 'user');
        setCitizens(filtered);
      }
    } catch (e) {
      console.warn("Failed to load citizens", e);
    } finally {
      setGlobalLoading(false);
    }
  };

  useEffect(() => {
    fetchCitizens();
  }, []);

  const handleOpenAddModal = () => {
    setEditingCitizen(null);
    setFullName('');
    setPhoneNumber('');
    setEmail('');
    setPassword('');
    setShowModal(true);
  };

  const handleOpenEditModal = (cit: any) => {
    setEditingCitizen(cit);
    setFullName(cit.full_name);
    setPhoneNumber(cit.phone_number);
    setEmail(cit.email);
    setPassword('');
    setShowModal(true);
  };

  const handleSaveCitizen = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName || !phoneNumber || !email || (!editingCitizen && !password)) {
      showToast('Validation Error', 'Please fill in all required fields.', 'warning');
      return;
    }

    setGlobalLoading(true);
    try {
      if (editingCitizen) {
        const res = await axios.put('/admin/users.php', {
          user_id: editingCitizen.user_id,
          full_name: fullName,
          phone_number: phoneNumber,
          email: email,
          password: password || undefined,
          role: 'user'
        });
        if (res.data?.success) {
          showToast('Updated successfully', 'Citizen details updated.', 'success');
          setShowModal(false);
          fetchCitizens();
        }
      } else {
        const res = await axios.post('/admin/users.php', {
          full_name: fullName,
          phone_number: phoneNumber,
          email: email,
          password: password,
          role: 'user'
        });
        if (res.data?.success) {
          showToast('Registered successfully', 'New citizen registered.', 'success');
          setShowModal(false);
          fetchCitizens();
        }
      }
    } catch (e: any) {
      showToast('Action failed', e.response?.data?.message || 'Error processing request', 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  const handleDeleteCitizen = async (id: number) => {
    if (!confirm('Are you sure you want to delete this citizen user? This action cannot be undone.')) return;
    setGlobalLoading(true);
    try {
      const res = await axios.delete(`/admin/users.php?id=${id}`);
      if (res.data?.success) {
        showToast('Deleted successfully', 'Citizen user deleted.', 'success');
        fetchCitizens();
      }
    } catch (e: any) {
      showToast('Failed to delete', e.response?.data?.message || 'Error deleting citizen', 'error');
    } finally {
      setGlobalLoading(false);
    }
  };

  const filteredCitizens = citizens.filter(cit => 
    (cit.full_name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (cit.phone_number || '').includes(searchQuery) ||
    (cit.email || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-heading font-extrabold text-white">Citizen Directory</h2>
          <p className="text-sm text-slate-400 mt-1">Manage registered citizens, emergency credentials, and contact dispatches.</p>
        </div>
        <button 
          onClick={handleOpenAddModal}
          className="flex items-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-bold rounded-xl transition shadow-lg shadow-emerald-500/20"
        >
          <Plus className="w-4 h-4" /> Add Citizen
        </button>
      </div>

      <div className="flex bg-slate-900/50 p-4 rounded-2xl border border-white/5">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input 
            type="text" 
            placeholder="Search citizens by name, phone, or email..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
          />
        </div>
      </div>

      <div className="bg-slate-900 border border-white/5 rounded-3xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="text-xs uppercase bg-slate-950/50 text-slate-500 font-bold tracking-wider">
              <tr>
                <th className="px-6 py-4">Citizen Name</th>
                <th className="px-6 py-4">Phone Number</th>
                <th className="px-6 py-4">Email</th>
                <th className="px-6 py-4">Registration Date</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredCitizens.length > 0 ? filteredCitizens.map((cit) => (
                <tr key={cit.user_id} className="hover:bg-white/5 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-xl bg-slate-950 flex items-center justify-center border border-white/5 text-emerald-400 font-bold">
                        {cit.full_name?.substring(0,2).toUpperCase()}
                      </div>
                      <div className="font-bold text-white">{cit.full_name}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 font-mono text-slate-300">{cit.phone_number}</td>
                  <td className="px-6 py-4 text-slate-300">{cit.email}</td>
                  <td className="px-6 py-4">
                    <span className="block text-white font-bold">{new Date(cit.created_at).toLocaleDateString()}</span>
                    <span className="text-[10px] text-slate-500">{new Date(cit.created_at).toLocaleTimeString()}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button 
                        onClick={() => handleOpenEditModal(cit)}
                        className="p-2 bg-blue-500/10 text-blue-400 hover:bg-blue-500/20 rounded-xl transition border border-blue-500/10"
                        title="Edit Citizen Details"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleDeleteCitizen(cit.user_id)}
                        className="p-2 bg-red-500/10 text-red-400 hover:bg-red-500/20 rounded-xl transition border border-red-500/10"
                        title="Delete Citizen User"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-slate-500">
                    <User className="w-10 h-10 mx-auto mb-3 opacity-50" />
                    <p>No citizens registered in directory.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add / Edit Citizen Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md z-50 flex items-center justify-center p-6">
          <div className="bg-slate-900 border border-white/10 rounded-3xl p-8 max-w-md w-full shadow-2xl relative animate-scale-up">
            <button 
              onClick={() => setShowModal(false)}
              className="absolute top-6 right-6 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="text-xl font-heading font-extrabold text-white mb-6">
              {editingCitizen ? 'Edit Citizen Details' : 'Register New Citizen'}
            </h3>

            <form onSubmit={handleSaveCitizen} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Full Name</label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input 
                    type="text" 
                    placeholder="Enter full name" 
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Phone Number</label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input 
                    type="text" 
                    placeholder="Enter phone number" 
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Email Address</label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input 
                    type="email" 
                    placeholder="Enter email address" 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Password {editingCitizen && <span className="text-[10px] text-slate-500 lowercase">(leave blank to keep current)</span>}
                </label>
                <input 
                  type="password" 
                  placeholder={editingCitizen ? "••••••••" : "Enter password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full bg-slate-950 border border-white/5 rounded-xl py-2.5 px-4 text-sm text-white focus:outline-none focus:border-blue-500 transition"
                />
              </div>

              <button 
                type="submit"
                className="w-full py-3 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl transition text-sm shadow-lg shadow-emerald-500/20 mt-4"
              >
                {editingCitizen ? 'Save Changes' : 'Register Citizen'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCitizens;
import React from 'react';
import { Flame, HeartPulse, HelpCircle, Phone, PhoneCall, ShieldAlert, Wind } from 'lucide-react';

const iconMap: Record<string, any> = {
  Flame, HeartPulse, HelpCircle, Phone, PhoneCall, ShieldAlert, Wind
};

const Contacts: React.FC = () => {
  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const hotlines = [
    { name: 'National Emergency Helpline', number: '112', icon: 'PhoneCall', desc: 'Unified single helpline for all emergencies.' },
    { name: 'Ambulance Hotline', number: '102', icon: 'HeartPulse', desc: 'Direct ambulance dispatch and medical transport.' },
    { name: 'Fire Response Services', number: '101', icon: 'Flame', desc: 'Fire brigade alerts and rescue dispatch.' },
    { name: 'Police Control Room', number: '100', icon: 'ShieldAlert', desc: 'Law enforcement dispatch and safety operations.' },
    { name: 'Disaster Management Force', number: '1078', icon: 'Wind', desc: 'Severe storm, flood, and earthquake assistance.' },
  ];

  return (
    <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl max-w-2xl mx-auto space-y-6">
      <div>
        <h2 className="text-lg font-heading font-extrabold text-white">Emergency Telephone Directory</h2>
        <p className="text-gray-400 text-xs mt-0.5">Quick-dial emergency lines for critical situations</p>
      </div>

      <div className="space-y-4">
        {hotlines.map((hl) => (
          <div key={hl.number} className="glass-card rounded-xl p-4 border border-white/5 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-red-500/10 text-accentRed rounded-xl shrink-0">
                {getLucideIcon(hl.icon, { size: 20 })}
              </div>
              <div>
                <h3 className="text-sm font-extrabold text-white">{hl.name}</h3>
                <p className="text-[10px] text-gray-400 mt-0.5">{hl.desc}</p>
              </div>
            </div>
            <a
              href={`tel:${hl.number}`}
              className="px-4 py-2.5 bg-accentRed hover:bg-red-700 text-white font-heading font-extrabold text-xs rounded-xl shadow-md transition flex items-center gap-1.5 whitespace-nowrap"
            >
              {getLucideIcon('Phone', { size: 12 })} Dial {hl.number}
            </a>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Contacts;

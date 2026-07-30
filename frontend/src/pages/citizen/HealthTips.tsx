import React from 'react';
import { Activity, AlertOctagon, Heart, HelpCircle } from 'lucide-react';

const iconMap: Record<string, any> = {
  Activity, AlertOctagon, Heart, HelpCircle
};

const HealthTips: React.FC = () => {
  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  const tips = [
    {
      title: 'Cardiac Arrest Response (CPR)',
      icon: 'Heart',
      color: 'text-red-400 bg-red-500/10',
      steps: [
        'Confirm if the patient is responsive and breathing.',
        'Call emergency dispatch immediately.',
        'Place hands in the center of the chest and push hard and fast (100-120 compressions per minute).',
        'Allow chest to return to normal position between compressions.',
      ],
    },
    {
      title: 'Stroke Identification (FAST)',
      icon: 'Activity',
      color: 'text-blue-400 bg-blue-500/10',
      steps: [
        'Face Drooping: Ask the person to smile. Is one side drooped?',
        'Arm Weakness: Ask the person to raise both arms. Does one arm drift downward?',
        'Speech Difficulty: Ask the person to repeat a simple sentence. Is speech slurred?',
        'Time to Call: If any signs are present, request immediate ambulance dispatch.',
      ],
    },
    {
      title: 'Severe Bleeding Control',
      icon: 'AlertOctagon',
      color: 'text-orange-400 bg-orange-500/10',
      steps: [
        'Apply direct firm pressure on the wound using a clean cloth.',
        'Elevate the injured limb above heart level if possible.',
        'Do not remove blood-soaked bandages; wrap additional cloth on top.',
        'Keep the patient warm and lying flat to prevent shock.',
      ],
    },
  ];

  return (
    <div className="glass-panel rounded-2xl p-6 border border-white/5 shadow-xl max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-lg font-heading font-extrabold text-white">First Aid & Medical Tips</h2>
        <p className="text-gray-400 text-xs mt-0.5">Quick guides to help patients before first responders arrive</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {tips.map((tip) => (
          <div key={tip.title} className="glass-card rounded-xl p-5 border border-white/5 space-y-4">
            <div className="flex items-center gap-3">
              <div className={`p-2.5 rounded-lg ${tip.color}`}>
                {getLucideIcon(tip.icon, { size: 20 })}
              </div>
              <h3 className="text-sm font-extrabold text-white">{tip.title}</h3>
            </div>
            <ul className="space-y-2">
              {tip.steps.map((step, idx) => (
                <li key={idx} className="flex gap-2 text-xs text-gray-300 leading-relaxed">
                  <span className="text-accentRed font-bold">{idx + 1}.</span>
                  <span>{step}</span>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
};

export default HealthTips;

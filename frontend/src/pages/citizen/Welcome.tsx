import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, HeartPulse, HelpCircle, Navigation, ShieldAlert, Truck } from 'lucide-react';

const iconMap: Record<string, any> = {
  ArrowRight, HeartPulse, HelpCircle, Navigation, ShieldAlert, Truck
};

const Welcome: React.FC = () => {
  const [step, setStep] = useState<number>(0);
  const navigate = useNavigate();

  const handleNext = () => setStep((prev) => prev + 1);
  const handleBack = () => setStep((prev) => prev - 1);
  const handleSkip = () => navigate('/login');

  const getLucideIcon = (name: string, props: any = {}) => {
    const IconComponent = iconMap[name] || iconMap.HelpCircle;
    return <IconComponent {...props} />;
  };

  return (
    <div className="min-h-screen flex flex-col justify-between p-6 bg-darkBg text-gray-100 max-w-md mx-auto relative overflow-hidden">
      {/* Top Header branding */}
      <div className="flex justify-between items-center py-4">
        <div className="flex items-center gap-2">
          <div className="text-accentRed">{getLucideIcon('ShieldAlert', { size: 24 })}</div>
          <span className="font-heading font-extrabold text-lg text-white tracking-wide">
            SAFE<span className="text-accentRed">REACH</span>
          </span>
        </div>
        {step > 0 && step < 4 && (
          <button onClick={handleSkip} className="text-sm font-medium text-gray-400 hover:text-white transition">
            Skip
          </button>
        )}
      </div>

      {/* Main Content Stepper Area */}
      <div className="flex-1 flex flex-col justify-center items-center text-center my-6">
        {step === 0 && (
          <div className="space-y-6 animate-fade-in">
            <div className="relative">
              <div className="absolute inset-0 bg-red-600/10 rounded-full blur-xl scale-125"></div>
              <div className="w-32 h-32 rounded-full glass-panel flex items-center justify-center border border-red-500/20 pulse-red mx-auto">
                {getLucideIcon('ShieldAlert', { size: 64, className: 'text-accentRed' })}
              </div>
            </div>
            <div className="space-y-2">
              <h1 className="text-3xl font-heading font-extrabold">SafeReach Response</h1>
              <p className="text-gray-400 text-sm max-w-xs mx-auto">
                Instant emergency dispatch, moving location telemetry, and ER beds allocation matrices.
              </p>
            </div>
            <button
              onClick={handleNext}
              className="w-full bg-accentRed hover:bg-red-700 active:bg-red-800 text-white font-bold py-4 rounded-xl shadow-lg transition duration-200 flex items-center justify-center gap-2"
            >
              Get Started {getLucideIcon('ArrowRight', { size: 16 })}
            </button>
          </div>
        )}

        {step === 1 && (
          <div className="space-y-6 animate-fade-in">
            <div className="w-48 h-48 rounded-3xl bg-slate-800/40 flex items-center justify-center mx-auto border border-white/5">
              {getLucideIcon('Truck', { size: 80, className: 'text-accentRed' })}
            </div>
            <div className="space-y-2">
              <h2 className="text-2xl font-heading font-extrabold text-white">Emergency Onboarding</h2>
              <p className="text-gray-400 text-sm max-w-xs mx-auto">
                Request instant ambulances equipped with life-support tools matching your medical requirements.
              </p>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-6 animate-fade-in">
            <div className="w-48 h-48 rounded-3xl bg-slate-800/40 flex items-center justify-center mx-auto border border-white/5">
              {getLucideIcon('Navigation', { size: 80, className: 'text-emerald-500' })}
            </div>
            <div className="space-y-2">
              <h2 className="text-2xl font-heading font-extrabold text-white">Telemetry tracking</h2>
              <p className="text-gray-400 text-sm max-w-xs mx-auto">
                Watch ambulance units move closer to your location in real-time, with calculated distance ETAs.
              </p>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="space-y-6 animate-fade-in">
            <div className="w-48 h-48 rounded-3xl bg-slate-800/40 flex items-center justify-center mx-auto border border-white/5">
              {getLucideIcon('HeartPulse', { size: 80, className: 'text-blue-500' })}
            </div>
            <div className="space-y-2">
              <h2 className="text-2xl font-heading font-extrabold text-white">Smart Response Network</h2>
              <p className="text-gray-400 text-sm max-w-xs mx-auto">
                Automatically allocate ICU/NICU beds en-route to bypass crowded hospitals.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Control Actions */}
      {step > 0 && (
        <div className="py-6 space-y-4">
          {/* Progress Indicators */}
          <div className="flex justify-center gap-2">
            {[1, 2, 3].map((s) => (
              <div
                key={s}
                className={`h-2 rounded-full transition-all duration-300 ${
                  s === step ? 'w-8 bg-accentRed' : 'w-2 bg-gray-600'
                }`}
              ></div>
            ))}
          </div>

          <div className="flex gap-4">
            <button
              onClick={handleBack}
              className="flex-1 glass-card hover:bg-slate-700/30 text-white font-bold py-3.5 rounded-xl border border-white/10 transition"
            >
              Back
            </button>
            {step < 3 ? (
              <button
                onClick={handleNext}
                className="flex-1 bg-accentRed hover:bg-red-700 text-white font-bold py-3.5 rounded-xl transition"
              >
                Next
              </button>
            ) : (
              <button
                onClick={handleSkip}
                className="flex-1 bg-accentRed hover:bg-red-700 text-white font-bold py-3.5 rounded-xl pulse-red transition"
              >
                Let's Go!
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Welcome;

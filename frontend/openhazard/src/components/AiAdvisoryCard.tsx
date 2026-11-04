import React from 'react';
import { Sparkles, ShieldCheck } from 'lucide-react';
import type { LocationDetail } from '../types';

interface Props {
  location: LocationDetail;
}

export const AiAdvisoryCard: React.FC<Props> = ({ location }) => {
  const advisory = location.aiAdvisory;

  if (!advisory) return null;

  return (
    <div className="bg-gradient-to-br from-slate-900 to-slate-800 text-white rounded-xl p-6 shadow-md border border-slate-700 relative overflow-hidden">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-emerald-400 animate-pulse" />
          <h3 className="font-bold text-lg tracking-wide text-emerald-400">
            AI Safety Advisory
          </h3>
        </div>
        <span className="text-[10px] bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded border border-emerald-500/30 uppercase font-bold tracking-wider">
          Groq LLM Powered
        </span>
      </div>

      <p className="text-sm font-semibold text-slate-200 mb-4 italic">
        "{advisory.summaryHeadline}"
      </p>

      <div className="space-y-2">
        <p className="text-xs font-bold uppercase text-slate-400 tracking-wider">
          Recommended Actions
        </p>
        <ul className="space-y-2">
          {advisory.keyActionSteps.map((step, idx) => (
            <li key={idx} className="flex items-start gap-2 text-xs sm:text-sm text-slate-300">
              <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <span>{step}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};
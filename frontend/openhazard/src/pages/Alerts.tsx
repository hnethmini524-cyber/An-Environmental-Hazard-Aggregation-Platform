import React, { useEffect, useState } from 'react';
import { apiClient } from '../api/client';
import type { AlertItem, HazardType } from '../types';
import { RiskBadge } from '../components/RiskBadge';
import { Bell, Flame, CloudRain, Wind, AlertTriangle } from 'lucide-react';

const getHazardIcon = (type: HazardType) => {
  switch (type) {
    case 'HEAT':
      return <Flame className="w-5 h-5 text-amber-500 shrink-0" />;
    case 'FLOOD':
      return <CloudRain className="w-5 h-5 text-blue-500 shrink-0" />;
    case 'AIR_QUALITY':
      return <Wind className="w-5 h-5 text-purple-500 shrink-0" />;
    default:
      return <AlertTriangle className="w-5 h-5 text-red-500 shrink-0" />;
  }
};

const formatHazardTitle = (type: HazardType) => {
  switch (type) {
    case 'HEAT':
      return 'EXTREME HEAT ALERT';
    case 'FLOOD':
      return 'FLASH FLOOD & RAINFALL ALERT';
    case 'AIR_QUALITY':
      return 'AIR QUALITY WARNING';
    default:
      return 'OVERALL ENVIRONMENTAL RISK';
  }
};

export const Alerts: React.FC = () => {
  const [alerts, setAlerts] = useState<AlertItem[]>([]);

  useEffect(() => {
    // Fetch initial alert history from database
    apiClient.getAlerts().then(setAlerts);

    // Listen to real-time SSE stream for new alerts
    const sse = apiClient.subscribeToLiveAlerts((newAlert) => {
      setAlerts((prev) => [newAlert, ...prev]);
    });

    return () => sse.close();
  }, []);

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex items-center gap-3">
        <Bell className="w-8 h-8 text-red-500" />
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Environmental Hazard Alerts</h1>
          <p className="text-xs text-slate-500">Live SSE push updates from OpenHazard Risk Engine</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 divide-y divide-slate-100 overflow-hidden">
        {alerts.length === 0 ? (
          <div className="p-8 text-center text-slate-400">No active hazard alerts detected.</div>
        ) : (
          alerts.map((a) => (
            <div key={a.id} className="p-4 flex items-start justify-between gap-4 hover:bg-slate-50/50 transition-colors">
              <div className="flex items-start gap-3">
                <div className="p-2 bg-slate-50 border border-slate-100 rounded-lg mt-0.5">
                  {getHazardIcon(a.hazardType)}
                </div>
                <div>
                  <p className="font-bold text-slate-900 text-sm sm:text-base">
                    {a.locationName} — {formatHazardTitle(a.hazardType)}
                  </p>
                  <p className="text-xs sm:text-sm text-slate-600 mt-0.5">{a.message}</p>
                  <p className="text-[11px] text-slate-400 mt-1">
                    {new Date(a.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>
              <RiskBadge level={a.severity} />
            </div>
          ))
        )}
      </div>
    </div>
  );
};
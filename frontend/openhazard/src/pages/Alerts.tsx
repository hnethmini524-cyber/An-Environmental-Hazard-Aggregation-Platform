import React, { useEffect, useState } from 'react';
import { apiClient } from '../api/client';
import type { AlertItem } from '../types';
import { RiskBadge } from '../components/RiskBadge';
import { Bell, ShieldAlert } from 'lucide-react';

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

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 divide-y divide-slate-100">
        {alerts.length === 0 ? (
          <div className="p-8 text-center text-slate-400">No active hazard alerts detected.</div>
        ) : (
          alerts.map((a) => (
            <div key={a.id} className="p-4 flex items-start justify-between gap-4">
              <div className="flex items-start gap-3">
                <ShieldAlert className="w-5 h-5 text-red-500 mt-1 flex-shrink-0" />
                <div>
                  <p className="font-bold text-slate-900">{a.locationName} — {a.hazardType} ALERT</p>
                  <p className="text-sm text-slate-600 mt-0.5">{a.message}</p>
                  <p className="text-xs text-slate-400 mt-1">
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
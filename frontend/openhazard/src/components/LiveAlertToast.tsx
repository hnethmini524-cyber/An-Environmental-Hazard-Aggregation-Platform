import React, { useEffect, useState } from 'react';
import { ShieldAlert, X, Flame, CloudRain, Wind, AlertTriangle } from 'lucide-react';
import type { AlertItem, HazardType, RiskLevel } from '../types';
import { RiskBadge } from './RiskBadge';

interface Props {
  alert: AlertItem | null;
  onDismiss: () => void;
  autoDismissMs?: number;
}

export const LiveAlertToast: React.FC<Props> = ({
  alert,
  onDismiss,
  autoDismissMs = 10000, // Auto-dismiss after 10 seconds
}) => {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (alert) {
      setVisible(true);
      const timer = setTimeout(() => {
        handleClose();
      }, autoDismissMs);

      return () => clearTimeout(timer);
    } else {
      setVisible(false);
    }
  }, [alert, autoDismissMs]);

  const handleClose = () => {
    setVisible(false);
    setTimeout(() => {
      onDismiss();
    }, 300); // Allow fade-out animation to complete before unmounting
  };

  if (!alert) return null;

  // Icon selector based on hazard type
  const getHazardIcon = (type: HazardType) => {
    switch (type) {
      case 'HEAT':
        return <Flame className="w-6 h-6 text-orange-500 animate-pulse" />;
      case 'FLOOD':
      case 'RAINFALL':
        return <CloudRain className="w-6 h-6 text-blue-500 animate-pulse" />;
      case 'AIR_QUALITY':
        return <Wind className="w-6 h-6 text-amber-500 animate-pulse" />;
      default:
        return <AlertTriangle className="w-6 h-6 text-red-500 animate-pulse" />;
    }
  };

  // Severity border accent
  const getSeverityBorder = (level: RiskLevel) => {
    switch (level) {
      case 'CRITICAL':
        return 'border-l-8 border-purple-600 bg-slate-900 text-white';
      case 'HIGH':
        return 'border-l-8 border-red-600 bg-slate-900 text-white';
      case 'MODERATE':
        return 'border-l-8 border-yellow-500 bg-slate-900 text-white';
      default:
        return 'border-l-8 border-green-500 bg-slate-900 text-white';
    }
  };

  return (
    <div
      className={`fixed bottom-6 right-6 z-50 max-w-md w-full p-4 rounded-xl shadow-2xl transition-all duration-300 transform ${
        visible ? 'translate-y-0 opacity-100 scale-100' : 'translate-y-4 opacity-0 scale-95'
      } ${getSeverityBorder(alert.severity)}`}
      role="alert"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="p-2 bg-slate-800 rounded-lg flex-shrink-0">
            {getHazardIcon(alert.hazardType)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold tracking-wider text-emerald-400 uppercase">
                LIVE SSE ALERT
              </span>
              <RiskBadge level={alert.severity} />
            </div>
            <h4 className="font-bold text-base text-white mt-1">
              {alert.locationName.toUpperCase()} — {alert.hazardType}
            </h4>
            <p className="text-xs text-slate-300 mt-1 leading-relaxed">
              {alert.message}
            </p>
            <p className="text-[10px] text-slate-400 mt-2">
              Received: {new Date(alert.createdAt).toLocaleTimeString()}
            </p>
          </div>
        </div>

        <button
          onClick={handleClose}
          className="text-slate-400 hover:text-white p-1 rounded-md transition-colors flex-shrink-0"
          aria-label="Close notification"
        >
          <X className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
};
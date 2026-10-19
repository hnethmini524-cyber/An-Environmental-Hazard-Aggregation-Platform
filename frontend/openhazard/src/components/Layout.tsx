import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ShieldAlert, Map, Bell, Info, Home } from 'lucide-react';
import { apiClient } from '../api/client';
import type { AlertItem } from '../types';
import { LiveAlertToast } from './LiveAlertToast';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const [liveAlert, setLiveAlert] = useState<AlertItem | null>(null);

  useEffect(() => {
    // Subscribe to SSE stream endpoint
    const sse = apiClient.subscribeToLiveAlerts((alert) => {
      setLiveAlert(alert);
    });

    return () => sse.close();
  }, []);

  const navItems = [
    { label: 'Home', path: '/', icon: Home },
    { label: 'Sri Lanka Map', path: '/map', icon: Map },
    { label: 'Alerts', path: '/alerts', icon: Bell },
    { label: 'Data Sources', path: '/about', icon: Info },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
      {/* Navbar Header */}
      <header className="bg-slate-900 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-bold text-xl text-emerald-400">
            <ShieldAlert className="w-6 h-6 text-emerald-400" />
            <span>OpenHazard <span className="text-slate-400 text-xs font-normal">SL</span></span>
          </Link>

          <nav className="flex items-center gap-1 sm:gap-6">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-1.5 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive ? 'bg-slate-800 text-emerald-400' : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="hidden sm:inline">{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      </header>

      {/* Main Content View */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 py-6">{children}</main>

      {/* Floating Live Real-time SSE Notification Toast */}
      <LiveAlertToast alert={liveAlert} onDismiss={() => setLiveAlert(null)} />

      <footer className="bg-slate-900 text-slate-400 py-6 text-center text-xs border-t border-slate-800">
        <p>OpenHazard Platform — Aggregating public environmental open-data across Sri Lanka.</p>
      </footer>
    </div>
  );
};
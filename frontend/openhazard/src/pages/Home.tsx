import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { LocationOverview } from '../types';
import { RiskBadge } from '../components/RiskBadge';
import { Map, AlertTriangle, CheckCircle, Flame } from 'lucide-react';

export const Home: React.FC = () => {
  const [locations, setLocations] = useState<LocationOverview[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient.getLocations().then((data) => {
      setLocations(data);
      setLoading(false);
    });
  }, []);

  const highCount = locations.filter((l) => l.overallRisk === 'HIGH' || l.overallRisk === 'CRITICAL').length;
  const modCount = locations.filter((l) => l.overallRisk === 'MODERATE').length;
  const lowCount = locations.filter((l) => l.overallRisk === 'LOW').length;

  return (
    <div className="space-y-8">
      {/* Hero Banner */}
      <div className="bg-slate-900 text-white rounded-2xl p-8 shadow-xl relative overflow-hidden">
        <div className="relative z-10 max-w-2xl space-y-4">
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Sri Lanka Environmental Conditions
          </h1>
          <p className="text-slate-300 text-sm sm:text-base">
            Real-time environmental monitoring aggregating public weather, air-quality, and hazard data into actionable location-based risk indicators.
          </p>

          <div className="pt-2 flex flex-wrap gap-4">
            <Link
              to="/map"
              className="inline-flex items-center gap-2 bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold px-5 py-2.5 rounded-lg transition-colors"
            >

<Map className="w-5 h-5" />
Explore Interactive Map
            </Link>
          </div>
        </div>
      </div>

      {/* National Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase">High Risk Areas</p>
            <p className="text-2xl font-bold text-red-600 mt-1">{loading ? '...' : highCount}</p>
          </div>
          <div className="p-3 bg-red-50 rounded-lg text-red-600"><AlertTriangle className="w-6 h-6" /></div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase">Moderate Risk Areas</p>
            <p className="text-2xl font-bold text-yellow-600 mt-1">{loading ? '...' : modCount}</p>
          </div>
          <div className="p-3 bg-yellow-50 rounded-lg text-yellow-600"><Flame className="w-6 h-6" /></div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase">Low Risk Areas</p>
            <p className="text-2xl font-bold text-green-600 mt-1">{loading ? '...' : lowCount}</p>
          </div>
          <div className="p-3 bg-green-50 rounded-lg text-green-600"><CheckCircle className="w-6 h-6" /></div>
        </div>
      </div>

      {/* Monitored Locations Table Preview */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex justify-between items-center">
          <h2 className="font-bold text-slate-800">Monitored Locations Overview</h2>
          <Link to="/map" className="text-xs text-emerald-600 font-semibold hover:underline">View on Map &rarr;</Link>
        </div>
        <div className="divide-y divide-slate-100">
          {locations.map((loc) => (
            <Link
              key={loc.id}
              to={`/location/${loc.name}`}
              className="p-4 flex items-center justify-between hover:bg-slate-50 transition-colors"
            >
              <div>
                <p className="font-bold text-slate-900">{loc.name}</p>
                <p className="text-xs text-slate-500">{loc.district} District</p>
              </div>
              <div className="flex items-center gap-3">
                <RiskBadge level={loc.overallRisk} />
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
};
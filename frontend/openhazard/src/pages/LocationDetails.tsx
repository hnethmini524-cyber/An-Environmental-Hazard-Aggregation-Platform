import React, { useEffect, useState } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { LocationDetail } from '../types';
import { RiskBadge } from '../components/RiskBadge';
import { AiAdvisoryCard } from '../components/AiAdvisoryCard';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { ArrowLeft, Thermometer, Droplets, CloudRain } from 'lucide-react';

export const LocationDetails: React.FC = () => {
  const { name } = useParams<{ name: string }>();
  const [searchParams] = useSearchParams();
  const latParam = searchParams.get('lat');
  const lonParam = searchParams.get('lon');

  const [data, setData] = useState<LocationDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!name) return;

    setLoading(true);

    if (latParam && lonParam) {
      const lat = parseFloat(latParam);
      const lon = parseFloat(lonParam);
      apiClient
        .exploreLocation(name, lat, lon)
        .then((res) => {
          setData(res);
          setLoading(false);
        })
        .catch((err) => {
          console.error('Error exploring location:', err);
          setLoading(false);
        });
    } else {
      // Fallback: Fetch DB-seeded hub details
      apiClient
        .getLocationDetails(name)
        .then((res) => {
          setData(res);
          setLoading(false);
        })
        .catch((err) => {
          console.error('Error loading location details:', err);
          setLoading(false);
        });
    }
  }, [name, latParam, lonParam]);

  if (loading || !data) {
    return <div className="p-8 text-center text-slate-500">Loading location environmental telemetry...</div>;
  }

  const rec = data.currentRecord;

  // Format historical trend data points for Recharts (empty array if exploring new area)
  const chartData = (data.history || []).map((h) => ({
    time: new Date(h.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    temp: h.temperatureCelsius,
    rainfall: h.rainfallMmPerHour,
  }));

  return (
    <div className="space-y-6">
      <Link to="/map" className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900">
        <ArrowLeft className="w-4 h-4" /> Back to Map
      </Link>

      {/* Location Header Banner */}
      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-3xl font-extrabold text-slate-900">{data.name.toUpperCase()}</h1>
            {latParam && (
              <span className="bg-slate-100 text-slate-600 text-xs px-2 py-0.5 rounded font-semibold uppercase">
                Explored Area
              </span>
            )}
          </div>
          <p className="text-sm text-slate-500">{data.district} • Sri Lanka</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs text-slate-500">Overall Risk:</span>
          <RiskBadge level={rec.overallRisk} />
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">
              <Thermometer className="w-4 h-4 text-red-500" /> Temperature & Heat Risk
            </span>
            <RiskBadge level={rec.heatRisk} />
          </div>
          <p className="text-2xl font-black text-slate-900">{rec.temperatureCelsius ?? '--'}°C</p>
          <p className="text-xs text-slate-400">Humidity: {rec.humidityPercentage ?? '--'}%</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">
              <Droplets className="w-4 h-4 text-blue-500" /> Air Quality
            </span>
            <RiskBadge level={rec.airQualityRisk} />
          </div>
          <p className="text-2xl font-black text-slate-900">AQI {rec.aqiValue ?? 'N/A'}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">
              <CloudRain className="w-4 h-4 text-indigo-500" /> Flood & Rainfall
            </span>
            <RiskBadge level={rec.floodRisk} />
          </div>
          <p className="text-2xl font-black text-slate-900">{rec.rainfallMmPerHour ?? 0} mm/hr</p>
        </div>
      </div>

      {/* AI Safety Advisory Card */}
      <AiAdvisoryCard location={data} />

      {/* Historical Trend Chart (Only renders if history data exists) */}
      {chartData.length > 0 && (
        <div className="bg-white p-6 rounded-xl border border-slate-200 space-y-4">
          <h2 className="font-bold text-slate-800 text-lg">24-Hour Temperature Trend</h2>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="time" stroke="#94a3b8" fontSize={12} />
                <YAxis stroke="#94a3b8" fontSize={12} domain={['dataMin - 2', 'dataMax + 2']} />
                <Tooltip />
                <Line type="monotone" dataKey="temp" stroke="#ef4444" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
};
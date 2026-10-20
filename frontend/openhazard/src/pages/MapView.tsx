import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { Link } from 'react-router-dom';
import L from 'leaflet';
import { apiClient } from '../api/client';
import type { LocationOverview, RiskLevel } from '../types';
import { RiskBadge } from '../components/RiskBadge';

// Helper function to render color-coded map pin markers
const createCustomMarkerIcon = (risk: RiskLevel) => {
  const colors: Record<RiskLevel, string> = {
    LOW: '#22c55e',
    MODERATE: '#eab308',
    HIGH: '#ef4444',
    CRITICAL: '#9333ea',
  };

  const color = colors[risk] || '#22c55e';

  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `<div style="background-color: ${color}; width: 18px; height: 18px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 6px rgba(0,0,0,0.4);"></div>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  });
};

export const MapView: React.FC = () => {
  const [locations, setLocations] = useState<LocationOverview[]>([]);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    apiClient.getLocations().then(setLocations);
  }, []);

  const filtered = locations.filter((loc) =>
    loc.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-4 h-[calc(100vh-140px)] flex flex-col">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Sri Lanka Hazard Map</h1>
        <input
          type="text"
          placeholder="Search location (e.g. Colombo, Kandy)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="px-4 py-2 text-sm border border-slate-300 rounded-lg w-full sm:w-72 focus:outline-none focus:ring-2 focus:ring-emerald-500"
        />
      </div>

      <div className="flex-1 bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden relative">
        <MapContainer
          center={[7.8731, 80.7718]} // Center of Sri Lanka
          zoom={8}
          scrollWheelZoom={true}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {filtered.map((loc) => (
            <Marker
              key={loc.id}
              position={[loc.latitude, loc.longitude]}
              icon={createCustomMarkerIcon(loc.overallRisk)}
            >
              <Popup>
                <div className="p-1 space-y-2">
                  <h3 className="font-bold text-base">{loc.name}</h3>
                  <p className="text-xs text-slate-500">{loc.district} District</p>
                  <div className="my-1">
                    <RiskBadge level={loc.overallRisk} />
                  </div>
                  <Link
                    to={`/location/${loc.name}`}
                    className="inline-block mt-2 text-xs text-emerald-600 font-bold hover:underline"
                  >
                    View Detailed Risk Dashboard &rarr;
                  </Link>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>
    </div>
  );
};
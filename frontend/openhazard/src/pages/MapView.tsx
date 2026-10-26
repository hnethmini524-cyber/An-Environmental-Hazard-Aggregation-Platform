import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { Link } from 'react-router-dom';
import L from 'leaflet';
import { apiClient } from '../api/client';
import type { LocationOverview, LocationDetail, RiskLevel } from '../types';
import { RiskBadge } from '../components/RiskBadge';
import { Search, Loader2 } from 'lucide-react';

interface GeocodeResult {
  name: string;
  latitude: number;
  longitude: number;
  admin1?: string;
}

// Color-coded pin markers
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
    html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 8px rgba(0,0,0,0.4);"></div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  });
};

// Helper component to pan map smoothly when selecting a searched location
const FlyToMarker: React.FC<{ center: [number, number] }> = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    map.flyTo(center, 10, { duration: 1.5 });
  }, [center, map]);
  return null;
};

export const MapView: React.FC = () => {
  const [locations, setLocations] = useState<LocationOverview[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<GeocodeResult[]>([]);
  const [exploredLocation, setExploredLocation] = useState<LocationDetail | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>([7.8731, 80.7718]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    apiClient.getLocations().then(setLocations);
  }, []);

  // Filter monitored hubs from backend
  const filteredMonitored = locations.filter((loc) =>
    loc.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Dynamic search trigger against Open-Meteo Geocoding
  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTerm.trim()) return;

    setSearching(true);
    try {
      const res = await fetch(
        `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(
          searchTerm
        )}&count=5&country=LK&language=en`
      );
      const data = await res.json();
      setSearchResults(data.results || []);
    } catch (err) {
      console.error('Failed to geocode location:', err);
    } finally {
      setSearching(false);
    }
  };

  // Select dynamic searched location
  const handleSelectSearchResult = async (result: GeocodeResult) => {
    setSearching(true);
    try {
      const detail = await apiClient.exploreLocation(
        result.name,
        result.latitude,
        result.longitude
      );
      setExploredLocation(detail);
      setMapCenter([result.latitude, result.longitude]);
      setSearchResults([]);
    } catch (err) {
      console.error('Failed to explore location:', err);
    } finally {
      setSearching(false);
    }
  };

  return (
    <div className="space-y-4 h-[calc(100vh-140px)] flex flex-col">
      {/* Top Search & Controls Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 relative">
        <h1 className="text-2xl font-bold text-slate-900">Sri Lanka Hazard Map</h1>

        <div className="w-full sm:w-96 relative">
          <form onSubmit={handleSearchSubmit} className="flex gap-2">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Search any location (e.g. Nuwara Eliya, Ella)..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-4 pr-10 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
              {searching && (
                <Loader2 className="w-4 h-4 animate-spin absolute right-3 top-2.5 text-slate-400" />
              )}
            </div>
            <button
              type="submit"
              className="bg-emerald-500 hover:bg-emerald-600 text-slate-950 px-4 py-2 rounded-lg text-sm font-bold transition-colors flex items-center gap-1"
            >
              <Search className="w-4 h-4" /> Search
            </button>
          </form>

          {/* Autocomplete Dropdown Results */}
          {searchResults.length > 0 && (
            <div className="absolute left-0 right-0 top-12 bg-white border border-slate-200 rounded-lg shadow-xl z-50 divide-y divide-slate-100 max-h-60 overflow-y-auto">
              {searchResults.map((item, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSelectSearchResult(item)}
                  className="w-full text-left p-3 hover:bg-slate-50 text-sm flex justify-between items-center transition-colors"
                >
                  <span className="font-semibold text-slate-800">{item.name}</span>
                  <span className="text-xs text-slate-500">{item.admin1 || 'Sri Lanka'}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Main Map Box */}
      <div className="flex-1 bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden relative">
        <MapContainer center={mapCenter} zoom={8} scrollWheelZoom={true} className="h-full w-full">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <FlyToMarker center={mapCenter} />

          {/* Render Monitored Hub Pins */}
          {filteredMonitored.map((loc) => (
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

          {/* Render Explored Searched Location Pin */}
          {exploredLocation && (
            <Marker
              position={[Number(exploredLocation.latitude), Number(exploredLocation.longitude)]}
              icon={createCustomMarkerIcon(exploredLocation.currentRecord?.overallRisk || 'LOW')}
            >
              <Popup>
                <div className="p-1 space-y-2">
                  <div className="flex items-center justify-between gap-2">
                    <h3 className="font-bold text-base">{exploredLocation.name}</h3>
                    <span className="bg-slate-100 text-slate-600 text-[10px] px-1.5 py-0.5 rounded uppercase font-semibold">
                      Explored
                    </span>
                  </div>
                  <p className="text-xs text-slate-500">
                    Temp: {exploredLocation.currentRecord?.temperatureCelsius ?? 'N/A'}°C | AQI:{' '}
                    {exploredLocation.currentRecord?.aqiValue ?? 'N/A'}
                  </p>
                  <div className="my-1">
                    {exploredLocation.currentRecord && (
                      <RiskBadge level={exploredLocation.currentRecord.overallRisk} />
                    )}
                  </div>
                </div>
              </Popup>
            </Marker>
          )}
        </MapContainer>
      </div>
    </div>
  );
};
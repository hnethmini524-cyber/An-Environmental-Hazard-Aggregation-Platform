import React from 'react';
import { Cpu, Cloud, Sparkles, Server } from 'lucide-react';

export const DataSources: React.FC = () => {
  return (
    <div className="space-y-8 max-w-5xl mx-auto pb-12">
      {/* Page Title Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-slate-900">Data Sources & System Architecture</h1>
        <p className="text-sm text-slate-500 mt-1">
          Technical specifications, telemetry providers, and risk assessment methodology powering OpenHazard SL.
        </p>
      </div>

      {/* Grid of Sources */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* Card 1: Telemetry APIs */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg border border-blue-100">
              <Cloud className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-bold text-slate-900 text-lg">Environmental Telemetry</h2>
              <p className="text-xs text-slate-500">Open-Meteo Weather & AQI APIs</p>
            </div>
          </div>
          <p className="text-sm text-slate-600 leading-relaxed">
            Real-time weather metrics (temperature, humidity, precipitation) and atmospheric pollutants (PM2.5, PM10, NO2) are ingested via Open-Meteo REST APIs. Monitored hubs are updated on a 15-minute cron schedule.
          </p>
          <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
            <span>Latency: ~120ms</span>
            <span className="font-semibold text-emerald-600">Active Pipeline</span>
          </div>
        </div>

        {/* Card 2: AI Safety Engine */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg border border-emerald-100">
              <Sparkles className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-bold text-slate-900 text-lg">AI Advisory Engine</h2>
              <p className="text-xs text-slate-500">Groq LLM (Llama 3.3 70B Versatile)</p>
            </div>
          </div>
          <p className="text-sm text-slate-600 leading-relaxed">
            Live environmental risk metrics are formatted and processed by Groq Cloud's Llama 3.3 model. The AI synthesizes actionable safety guidelines tailored to Sri Lankan climatic conditions and caches responses in Redis.
          </p>
          <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
            <span>Provider: Groq Cloud API</span>
            <span className="font-semibold text-emerald-600">Redis Cached</span>
          </div>
        </div>

        {/* Card 3: Risk Rule Engine */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-purple-50 text-purple-600 rounded-lg border border-purple-100">
              <Cpu className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-bold text-slate-900 text-lg">Environmental Risk Engine</h2>
              <p className="text-xs text-slate-500">Custom Spring Boot Domain Engine</p>
            </div>
          </div>
          <p className="text-sm text-slate-600 leading-relaxed">
            Evaluates raw metrics against NWS Heat Index formulas, US EPA AQI standards, and WMO precipitation thresholds to output tiered risk levels (<code className="text-xs bg-slate-100 px-1 py-0.5 rounded">LOW</code>, <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">MODERATE</code>, <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">HIGH</code>, <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">CRITICAL</code>).
          </p>
          <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
            <span>Standards: NWS / EPA / WMO</span>
            <span className="font-semibold text-purple-600">Deterministic Engine</span>
          </div>
        </div>

        {/* Card 4: Tiered Data Model */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-amber-50 text-amber-600 rounded-lg border border-amber-100">
              <Server className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-bold text-slate-900 text-lg">Tiered Storage & Streaming</h2>
              <p className="text-xs text-slate-500">MySQL, Redis & Server-Sent Events (SSE)</p>
            </div>
          </div>
          <p className="text-sm text-slate-600 leading-relaxed">
            Maintains 10 primary monitored hubs in MySQL with 24-hour historical trend tracking. Dynamic searches trigger on-demand telemetry queries, while real-time hazard alerts stream to clients via SSE.
          </p>
          <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
            <span>Database: MySQL + Redis</span>
            <span className="font-semibold text-amber-600">SSE Live Alerts</span>
          </div>
        </div>

      </div>
    </div>
  );
};
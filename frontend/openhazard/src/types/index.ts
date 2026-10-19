export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
export type HazardType = 'HEAT' | 'AIR_QUALITY' | 'FLOOD' | 'RAINFALL' | 'OVERALL';

export interface LocationOverview {
  id: number;
  name: string;
  district: string;
  latitude: number;
  longitude: number;
  overallRisk: RiskLevel;
  heatRisk: RiskLevel;
  airQualityRisk: RiskLevel;
  floodRisk: RiskLevel;
  updatedAt: string;
}

export interface EnvironmentalRecord {
  temperatureCelsius: number | null;
  humidityPercentage: number | null;
  aqiValue: number | null;
  rainfallMmPerHour: number | null;
  riverLevelMeters: number | null;
  heatRisk: RiskLevel;
  airQualityRisk: RiskLevel;
  floodRisk: RiskLevel;
  overallRisk: RiskLevel;
  recordedAt: string;
}

export interface LocationDetail {
  id: number;
  name: string;
  district: string;
  latitude: number;
  longitude: number;
  currentRecord: EnvironmentalRecord;
  history: EnvironmentalRecord[];
}

export interface AlertItem {
  id: number;
  locationName: string;
  hazardType: HazardType;
  severity: RiskLevel;
  message: string;
  createdAt: string;
}
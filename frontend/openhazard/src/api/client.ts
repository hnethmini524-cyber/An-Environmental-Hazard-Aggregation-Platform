import axios from 'axios';
import type { LocationOverview, LocationDetail, AlertItem } from '../types';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export const apiClient = {
  getLocations: async (): Promise<LocationOverview[]> => {
    const response = await axios.get(`${API_BASE_URL}/locations`);
    return response.data;
  },

  getLocationDetails: async (name: string): Promise<LocationDetail> => {
    const response = await axios.get(`${API_BASE_URL}/locations/${name}`);
    return response.data;
  },

  getAlerts: async (): Promise<AlertItem[]> => {
    const response = await axios.get(`${API_BASE_URL}/alerts`);
    return response.data;
  },

  // Connects to Spring Boot SseEmitter endpoint
  subscribeToLiveAlerts: (onAlertReceived: (alert: AlertItem) => void): EventSource => {
    const eventSource = new EventSource(`${API_BASE_URL}/alerts/stream`);

    eventSource.addEventListener('HAZARD_ALERT', (event: MessageEvent) => {
      try {
        const data: AlertItem = JSON.parse(event.data);
        onAlertReceived(data);
      } catch (err) {
        console.error('Error parsing live SSE alert:', err);
      }
    });

    return eventSource;
  }
};
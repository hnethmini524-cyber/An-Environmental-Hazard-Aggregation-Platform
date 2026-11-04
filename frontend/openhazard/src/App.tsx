import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Home } from './pages/Home';
import { MapView } from './pages/MapView';
import { LocationDetails } from './pages/LocationDetails';
import { Alerts } from './pages/Alerts';
import { DataSources } from './pages/DataSources';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/map" element={<MapView />} />
          <Route path="/location/:name" element={<LocationDetails />} />
          <Route path="/alerts" element={<Alerts />} />
          <Route path="/about" element={<DataSources />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
};

export default App;

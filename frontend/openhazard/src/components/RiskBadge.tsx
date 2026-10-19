import React from 'react';
import type { RiskLevel } from '../types';

interface Props {
  level: RiskLevel;
}

export const RiskBadge: React.FC<Props> = ({ level }) => {
  const styles: Record<RiskLevel, string> = {
    LOW: 'bg-green-100 text-green-800 border-green-300',
    MODERATE: 'bg-yellow-100 text-yellow-800 border-yellow-300',
    HIGH: 'bg-red-100 text-red-800 border-red-300',
    CRITICAL: 'bg-purple-100 text-purple-800 border-purple-300',
  };

  return (
    <span className={`px-2.5 py-0.5 text-xs font-semibold rounded-full border ${styles[level] || styles.LOW}`}>
      {level}
    </span>
  );
};
// frontend/src/services/types.ts

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export interface User {
  user_id: number;
  full_name: string;
  email: string;
  role: string;
  phone_number?: string;
  vehicle?: Vehicle | null;
  hospital?: Hospital | null;
}

export interface Vehicle {
  ambulance_id: number;
  vehicle_number: string;
  ambulance_type: string;
  status: string;
}

export interface Hospital {
  hospital_id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  available_icu_beds: number;
  available_nicu_beds: number;
  status: string;
}

export interface RequestHistoryItem {
  request_id: number;
  patient_name: string;
  patient_phone: string;
  pickup_address: string;
  emergency_type: string;
  status: string;
  created_at: string;
  hospital_name?: string;
}

export interface AuthResponse {
  token: string;
  user_id: number;
  full_name: string;
  email: string;
  role: string;
  vehicle: Vehicle | null;
  hospital: Hospital | null;
}

export interface AnalyticsSummary {
  statistics: {
    total_requests: number;
    completed_requests: number;
    pending_requests: number;
    total_users: number;
    online_ambulances: number;
  };
  emergency_types: { emergency_type: string; count: number }[];
  response_time_trends: { date: string; avg_response_time_minutes: number }[];
}

import request from '@/utils/request';

export interface LoginDevice {
  id: number;
  deviceName: string;
  platform: string;
  ipAddress?: string;
  lastLoginAt?: string;
  isCurrent: boolean;
  isTrusted: boolean;
}

export const fetchLoginDevices = () =>
  request.get<unknown, LoginDevice[]>('/h5/login-devices');

export const trustLoginDevice = (id: number) =>
  request.post<unknown, LoginDevice>(`/h5/login-devices/${id}/trust`);

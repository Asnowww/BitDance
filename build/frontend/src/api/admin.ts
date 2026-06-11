import request from '@/utils/request';

export interface ReportTicket {
  id: number;
  reporterUserId: number;
  targetType: string;
  targetId: number;
  reasonCode: string;
  reasonDetail?: string;
  reportStatus: string;
  handledByUserId?: number;
  handledAt?: string;
  handleResult?: string;
  createdAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const fetchReportTickets = (status = 'pending') =>
  request.get<unknown, PageResponse<ReportTicket>>('/admin/report-tickets', {
    params: { status, page: 1, pageSize: 20 }
  });

export const processReportTicket = (id: number) =>
  request.post<unknown, ReportTicket>(`/admin/report-tickets/${id}/process`);

export const closeReportTicket = (id: number, handleResult: string) =>
  request.post<unknown, ReportTicket>(`/admin/report-tickets/${id}/close`, { handleResult });

export const rejectReportTicket = (id: number, handleResult: string) =>
  request.post<unknown, ReportTicket>(`/admin/report-tickets/${id}/reject`, { handleResult });

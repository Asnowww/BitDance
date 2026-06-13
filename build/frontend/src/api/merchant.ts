import request from '@/utils/request';

export interface MerchantWorkshopOrderRow {
  orderId: number;
  workshopId: number;
  workshopTitle: string;
  buyerName: string;
  sessionDate: string;
  sessionTime: string;
  amount: number;
  status: string;
  checkinCode: string | null;
}

export const fetchMerchantWorkshopOrders = () =>
  request.get<unknown, MerchantWorkshopOrderRow[]>('/merchant/workshop-orders');

export const checkinMerchantWorkshopOrder = (orderId: number, code: string) =>
  request.post<unknown, unknown>(`/merchant/workshop-orders/${orderId}/checkin`, { code });

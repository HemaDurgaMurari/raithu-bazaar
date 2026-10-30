import { api } from "./client";

export interface SellerDashboard {
  summary: {
    paidOrderCount: number;
    orderCount: number;
    todayTurnover: number;
    monthlyTurnover: number;
    totalTurnover: number;
  };
  orders: SellerOrder[];
}

export interface SellerOrder {
  orderId: number;
  status: string;
  totalAmount: number;
  subtotalAmount: number;
  deliveryCharge: number;
  carryingCharge: number;
  gstAmount: number;
  createdAt: string;
  scheduledFor?: string;
  deliveryName: string;
  customer: { customerId: number; name: string; email: string };
  deliveryAddress: string;
  phone: string;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
  }>;
}

export async function getSellerDashboard(): Promise<SellerDashboard> {
  return (await api.get<SellerDashboard>("/api/seller/dashboard")).data;
}

import { api } from "./client";

export interface OrderItem {
  orderItemId: number;
  quantity: number;
  price: number;
  product: { productId: number; productName: string };
}

export interface CustomerOrder {
  orderId: number;
  status: string;
  totalAmount: number;
  deliveryName: string;
  phone: string;
  address: string;
  createdAt: string;
  scheduledFor?: string;
  items: OrderItem[];
}

export async function getMyOrders(): Promise<CustomerOrder[]> {
  return (await api.get<CustomerOrder[]>("/api/orders/my")).data;
}

export async function rateOrder(orderId: number, productId: number, rating: number, review: string) {
  return (await api.post(`/api/customer/orders/${orderId}/ratings`, { productId, rating, review })).data;
}

export async function submitComplaint(orderId: number | undefined, category: string, subject: string, message: string) {
  return (await api.post("/api/customer/complaints", { orderId, category, subject, message })).data;
}

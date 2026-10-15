import type { CartItem } from "../types/cart";
import { api } from "./client";

export interface DeliveryDetails {
  deliveryName: string;
  phone: string;
  address: string;
}

export interface CreatedOrder {
  orderId: number;
  totalAmount: number;
}

export interface RazorpayOrder {
  keyId: string;
  orderId: number;
  razorpayOrderId: string;
  amount: number;
  currency: string;
}

export interface PaymentResult {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
}

export async function createOrder(items: CartItem[], delivery: DeliveryDetails): Promise<CreatedOrder> {
  return (await api.post<CreatedOrder>("/api/orders", {
    items: items.map(({ product, quantity }) => ({ productId: product.productId, quantity })),
    ...delivery,
  })).data;
}

export async function createPayment(orderId: number): Promise<RazorpayOrder> {
  return (await api.post<RazorpayOrder>(`/api/payment/create/${orderId}`)).data;
}

export async function verifyPayment(orderId: number, result: PaymentResult): Promise<void> {
  await api.post("/api/payment/verify", {
    orderId,
    razorpayOrderId: result.razorpay_order_id,
    razorpayPaymentId: result.razorpay_payment_id,
    razorpaySignature: result.razorpay_signature,
  });
}

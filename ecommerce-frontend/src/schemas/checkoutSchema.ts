import { z } from "zod";

export const checkoutSchema = z.object({
  deliveryName: z.string().min(3, "Please enter your full name"),
  phone: z.string().regex(/^[0-9]{10}$/, "Enter a 10-digit mobile number"),
  address: z.string().min(10, "Please enter a complete delivery address"),
});

export type CheckoutFormData = z.infer<typeof checkoutSchema>;

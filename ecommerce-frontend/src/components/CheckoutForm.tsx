import { useRef, useState, type FormEvent } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { ArrowLeft, CreditCard, LoaderCircle, ShieldCheck } from "lucide-react";
import { createOrder, createPayment, verifyPayment } from "../api/paymentApi";
import { errorMessage } from "../api/client";
import { checkoutSchema, type CheckoutFormData } from "../schemas/checkoutSchema";
import { useAuthStore } from "../store/authStore";
import { useCartStore } from "../store/cartStore";

interface CheckoutFormProps {
  onBack: () => void;
  onRequireSignIn: () => void;
  onSuccess: (orderId: number) => void;
}

export function CheckoutForm({ onBack, onRequireSignIn, onSuccess }: CheckoutFormProps) {
  const user = useAuthStore((state) => state.user);
  const { cartItems, clearCart } = useCartStore();
  const [notice, setNotice] = useState("");
  const [paying, setPaying] = useState(false);
  const [activeOrderId, setActiveOrderId] = useState<number | null>(null);
  const [scheduled, setScheduled] = useState(false);
  const [scheduledFor, setScheduledFor] = useState("");
  const paymentFailed = useRef(false);
  const total = cartItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
  const deliveryCharge = 49;
  const carryingCharge = 2;
  const gst = total * 0.05;
  const grandTotal = total + deliveryCharge + carryingCharge + gst;
  const { register, handleSubmit, formState: { errors } } = useForm<CheckoutFormData>({
    resolver: zodResolver(checkoutSchema),
    defaultValues: { deliveryName: user?.name ?? "" },
  });

  async function submit(data: CheckoutFormData) {
    if (!user || user.role !== "CUSTOMER") {
      setNotice("Please sign in with a customer account to place an order.");
      return;
    }
    if (cartItems.length === 0) {
      setNotice("Your basket is empty. Add something fresh first.");
      return;
    }
    if (!window.Razorpay) {
      setNotice("The Razorpay checkout could not load. Check your internet connection and refresh.");
      return;
    }
    setPaying(true);
    setNotice("");
    paymentFailed.current = false;
    let attemptedOrderId = activeOrderId;
    try {
      const orderId = activeOrderId ?? (await createOrder(cartItems, {
        ...data,
        scheduledFor: scheduled ? scheduledFor : undefined,
      })).orderId;
      attemptedOrderId = orderId;
      setActiveOrderId(orderId);
      const payment = await createPayment(orderId);
      const checkout = new window.Razorpay({
        key: payment.keyId,
        amount: payment.amount,
        currency: payment.currency,
        name: "Raithu Bazaar",
        description: `Fresh groceries · Order #${orderId}`,
        order_id: payment.razorpayOrderId,
        prefill: { name: user.name, email: user.email, contact: data.phone },
        theme: { color: "#1b6849" },
        handler: async (result) => {
          try {
            await verifyPayment(orderId, result);
            clearCart();
            setActiveOrderId(null);
            onSuccess(orderId);
          } catch (error) {
            paymentFailed.current = true;
            setNotice(`Payment for order #${orderId} could not be confirmed: ${errorMessage(error)}. The order is not confirmed. If your bank shows a debit, contact the store before retrying.`);
          } finally {
            setPaying(false);
          }
        },
        modal: {
          ondismiss: () => {
            if (!paymentFailed.current) {
              setNotice(`Checkout closed. Order #${orderId} is still pending and has not been confirmed. You can retry payment within 30 minutes. If your bank shows a debit, contact the store before paying again.`);
            }
            setPaying(false);
          },
        },
      });
      checkout.on("payment.failed", (result) => {
        paymentFailed.current = true;
        const reason = result.error.description || result.error.reason || "Razorpay could not complete the payment";
        setNotice(`Payment failed for order #${orderId}: ${reason}. The order is not confirmed; you can retry within 30 minutes. If your bank shows a debit, contact the store before retrying.`);
        setPaying(false);
      });
      checkout.open();
    } catch (error) {
      setNotice(attemptedOrderId
        ? `Could not start payment for order #${attemptedOrderId}: ${errorMessage(error)}. The order remains pending; you can retry within 30 minutes.`
        : `We couldn't place your order: ${errorMessage(error)}. Your basket is unchanged; please check the details and try again.`);
      setPaying(false);
    }
  }

  const submitForm = (event: FormEvent<HTMLFormElement>) => {
    void handleSubmit(submit)(event);
  };

  return (
    <section className="checkout-panel">
      <button className="back-link" onClick={onBack}><ArrowLeft size={16} /> Back to basket</button>
      <div className="checkout-title"><span className="eyebrow">ALMOST THERE</span><h2>Delivery details</h2><p>Where should we bring your fresh finds?</p></div>
      {!user && <div className="notice"><span>You're browsing as a guest.</span> <button className="inline-link" onClick={onRequireSignIn}>Sign in to continue</button></div>}
      {user?.role === "SELLER" && <div className="notice error-notice">Seller accounts cannot place customer orders. Sign out and create a customer account to shop.</div>}
      <form className="checkout-form" onSubmit={submitForm}>
        <label>Full name<input {...register("deliveryName")} placeholder="e.g. Ananya Rao" />{errors.deliveryName && <small className="field-error">{errors.deliveryName.message}</small>}</label>
        <label>Mobile number<input {...register("phone")} inputMode="numeric" maxLength={10} placeholder="10-digit mobile number" />{errors.phone && <small className="field-error">{errors.phone.message}</small>}</label>
        <label>Delivery address<textarea {...register("address")} rows={3} placeholder="House / flat, street, area, city, PIN code" />{errors.address && <small className="field-error">{errors.address.message}</small>}</label>
        <label className="schedule-option"><input type="checkbox" checked={scheduled} onChange={(event) => setScheduled(event.target.checked)} /> Schedule this delivery</label>
        {scheduled && <label>Delivery time<input type="datetime-local" value={scheduledFor} min={new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString().slice(0, 16)} onChange={(event) => setScheduledFor(event.target.value)} /></label>}
        <div className="checkout-breakdown">
          <span>Subtotal <b>₹{total.toFixed(2)}</b></span>
          <span>Delivery charge <b>₹{deliveryCharge.toFixed(2)}</b></span>
          <span>Carrying charge <b>₹{carryingCharge.toFixed(2)}</b></span>
          <span>GST (5%) <b>₹{gst.toFixed(2)}</b></span>
        </div>
        <div className="checkout-total"><span>Total to pay</span><strong>₹{grandTotal.toFixed(2)}</strong></div>
        {notice && <div className="notice error-notice">{notice}</div>}
        <button className="button pay-button" type="submit" disabled={paying || cartItems.length === 0}><CreditCard size={18} />{paying ? <><LoaderCircle className="spin" size={18} /> Opening secure checkout…</> : <>Pay securely with Razorpay · ₹{grandTotal.toFixed(2)}</>}</button>
        <div className="payment-trust"><ShieldCheck size={16} /> Payments are encrypted and verified securely</div>
      </form>
    </section>
  );
}

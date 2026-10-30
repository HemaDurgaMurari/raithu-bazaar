import { useEffect, useState } from "react";
import { Clock3, Headphones, PackageCheck, Star } from "lucide-react";
import { getMyOrders, rateOrder, submitComplaint, type CustomerOrder } from "../api/customerApi";

export function AccountPage() {
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [complaintOrder, setComplaintOrder] = useState("");
  const [category, setCategory] = useState("Delivery");
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  const [ratings, setRatings] = useState<Record<string, number>>({});

  async function loadOrders() {
    try {
      setOrders(await getMyOrders());
    } catch {
      setError("Your orders could not be loaded.");
    }
  }

  useEffect(() => { void loadOrders(); }, []);

  async function saveRating(orderId: number, productId: number) {
    try {
      await rateOrder(orderId, productId, ratings[`${orderId}-${productId}`] || 5, "");
      setNotice("Thanks! Your rating has been saved.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Rating could not be saved.");
    }
  }

  async function sendComplaint() {
    try {
      await submitComplaint(complaintOrder ? Number(complaintOrder) : undefined, category, subject, message);
      setNotice("Your complaint was submitted. Our support team will contact you by email.");
      setSubject("");
      setMessage("");
    } catch {
      setError("Your complaint could not be submitted.");
    }
  }

  return (
    <main className="account-page">
      <header className="account-heading"><span className="eyebrow">YOUR RAITHU BAZAAR</span><h1>My orders & support</h1><p>Track every order, rate your purchases, and get help.</p></header>
      {notice && <div className="notice">{notice}</div>}
      {error && <div className="notice error-notice">{error}</div>}
      <div className="account-grid">
        <section className="order-history">
          <h2><PackageCheck size={20} /> Order history</h2>
          {orders.length === 0 && <p className="empty-state">No orders yet. Your fresh finds will appear here.</p>}
          {orders.map((order) => (
            <article className="order-card" key={order.orderId}>
              <div className="order-card-head">
                <div><small>ORDER #{order.orderId}</small><b>{new Date(order.createdAt).toLocaleString()}</b></div>
                <span className={`order-status status-${order.status.toLowerCase()}`}>{order.status}</span>
              </div>
              {order.scheduledFor && <p className="scheduled-note"><Clock3 size={15} /> Scheduled for {new Date(order.scheduledFor).toLocaleString()}</p>}
              <div className="order-items">
                {order.items.map((item) => (
                  <div className="order-item" key={item.orderItemId}>
                    <div><b>{item.product.productName}</b><small>Qty {item.quantity} · ₹{item.price.toFixed(2)} each</small></div>
                    {(order.status === "PAID" || order.status === "DELIVERED") && (
                      <div className="star-picker" aria-label={`Rate ${item.product.productName}`}>
                        {[1, 2, 3, 4, 5].map((star) => (
                          <button key={star} type="button" onClick={() => setRatings((current) => ({ ...current, [`${order.orderId}-${item.product.productId}`]: star }))}>
                            <Star size={18} fill={(ratings[`${order.orderId}-${item.product.productId}`] || 0) >= star ? "currentColor" : "none"} />
                          </button>
                        ))}
                        <button className="inline-link" type="button" onClick={() => void saveRating(order.orderId, item.product.productId)}>Save</button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
              <div className="order-card-foot"><span>{order.address}</span><strong>₹{order.totalAmount.toFixed(2)}</strong></div>
            </article>
          ))}
        </section>
        <aside className="support-card">
          <h2><Headphones size={20} /> Raise a complaint</h2>
          <p>We will email the Raithu Bazaar support team and keep a copy of your request.</p>
          <label>Related order<select value={complaintOrder} onChange={(e) => setComplaintOrder(e.target.value)}><option value="">General enquiry</option>{orders.map((order) => <option value={order.orderId} key={order.orderId}>Order #{order.orderId}</option>)}</select></label>
          <label>Category<select value={category} onChange={(e) => setCategory(e.target.value)}><option>Delivery</option><option>Payment</option><option>Product quality</option><option>Missing item</option><option>Other</option></select></label>
          <label>Subject<input value={subject} maxLength={300} onChange={(e) => setSubject(e.target.value)} placeholder="Briefly describe the issue" /></label>
          <label>Message<textarea value={message} maxLength={3000} rows={5} onChange={(e) => setMessage(e.target.value)} placeholder="What happened?" /></label>
          <button className="button" disabled={!subject.trim() || !message.trim()} onClick={() => void sendComplaint()}>Submit complaint</button>
        </aside>
      </div>
    </main>
  );
}

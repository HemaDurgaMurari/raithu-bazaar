import { useEffect, useState, type FormEvent } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { BadgeIndianRupee, Boxes, ChevronDown, CirclePlus, PackageCheck, Phone, CalendarDays, MapPin, Mail, ShoppingBag } from "lucide-react";
import { addProduct } from "../api/productApi";
import { errorMessage } from "../api/client";
import { BackgroundJobMonitor } from "../components/BackgroundJobMonitor";
import { ProductList } from "../components/ProductList";
import type { Product, ProductInput } from "../types/product";
import { getSellerDashboard, type SellerDashboard } from "../api/sellerApi";

const emptyProduct: ProductInput = {
  productName: "", category: "Vegetables", description: "", price: 0, stock: 0,
  unit: "500 g", imageUrl: "", rating: 4.8,
};

export function SellerDashboard() {
  const queryClient = useQueryClient();
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState<ProductInput>(emptyProduct);
  const [dashboard, setDashboard] = useState<SellerDashboard | null>(null);
  const [dashboardError, setDashboardError] = useState("");
  const [activeView, setActiveView] = useState<"overview" | "inventory">("overview");
  const [expandedOrders, setExpandedOrders] = useState<Set<number>>(new Set());

  useEffect(() => {
    void getSellerDashboard().then(setDashboard).catch((error) => setDashboardError(errorMessage(error)));
  }, []);

  const money = (value: number) => new Intl.NumberFormat("en-IN", {
    style: "currency", currency: "INR", maximumFractionDigits: 2,
  }).format(value);

  function change<K extends keyof ProductInput>(key: K, value: ProductInput[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function toggleOrder(orderId: number) {
    setExpandedOrders((current) => {
      const next = new Set(current);
      if (next.has(orderId)) next.delete(orderId);
      else next.add(orderId);
      return next;
    });
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice("");
    setBusy(true);
    try {
      await addProduct(form);
      await queryClient.invalidateQueries({ queryKey: ["products"] });
      setForm(emptyProduct);
      setNotice("Your fresh find is now listed in the bazaar.");
    } catch (error) {
      setNotice(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="seller-page">
      <section className="seller-welcome">
        <div><span className="eyebrow">YOUR LITTLE CORNER OF THE MARKET</span><h1>Seller studio <span>🌾</span></h1><p>List what's fresh, keep your stock up to date, and watch orders come in.</p></div>
        <div className="seller-metrics"><div><Boxes size={18} /><span>Products</span><b>Manage below</b></div><div><PackageCheck size={18} /><span>Order tasks</span><b>Live updates</b></div></div>
      </section>
      <nav className="seller-tabs" aria-label="Seller sections">
        <button className={activeView === "overview" ? "active" : ""} onClick={() => setActiveView("overview")}><PackageCheck size={18} /> Orders, sales & turnover</button>
        <button className={activeView === "inventory" ? "active" : ""} onClick={() => setActiveView("inventory")}><Boxes size={18} /> Product & stock shop</button>
      </nav>
      {activeView === "overview" ? (
      <>
      <section className="seller-sales-overview" aria-label="Sales overview">
        <article><span className="seller-stat-icon"><ShoppingBag /></span><div><span>Total sales</span><strong>{dashboard?.summary.paidOrderCount ?? 0}</strong><small>Paid and delivered orders</small></div></article>
        <article><span className="seller-stat-icon"><BadgeIndianRupee /></span><div><span>Total turnover</span><strong>{money(dashboard?.summary.totalTurnover ?? 0)}</strong><small>All successful orders</small></div></article>
        <article><span className="seller-stat-icon"><CalendarDays /></span><div><span>Today's turnover</span><strong>{money(dashboard?.summary.todayTurnover ?? 0)}</strong><small>Orders created today</small></div></article>
        <article><span className="seller-stat-icon"><CalendarDays /></span><div><span>This month's turnover</span><strong>{money(dashboard?.summary.monthlyTurnover ?? 0)}</strong><small>Current calendar month</small></div></article>
      </section>
      <section className="seller-orders">
        <div className="seller-section-title">
          <div className="seller-icon"><PackageCheck size={21} /></div>
          <div><span className="eyebrow">FULFILMENT DESK</span><h2>Orders and deliveries</h2></div>
        </div>
        {dashboardError && <p className="form-error">{dashboardError}</p>}
        {!dashboard && !dashboardError && <p className="seller-loading">Loading your orders…</p>}
        {dashboard?.orders.map((order) => (
          <article className={`seller-order-card${expandedOrders.has(order.orderId) ? " expanded" : ""}`} key={order.orderId}>
            <button type="button" className="seller-order-summary" onClick={() => toggleOrder(order.orderId)} aria-expanded={expandedOrders.has(order.orderId)}>
              <div><span>Order</span><strong>#{order.orderId}</strong><small>{order.deliveryName || order.customer.name}</small></div>
              <span className={`order-status ${order.status.toLowerCase()}`}>{order.status}</span>
              <div className="seller-order-total"><span>Total</span><strong>{money(order.totalAmount)}</strong></div>
              <ChevronDown className="seller-order-chevron" size={20} />
            </button>
            {expandedOrders.has(order.orderId) && (
            <div className="seller-order-body">
              <div className="seller-customer">
                <span className="eyebrow">CUSTOMER & DELIVERY</span>
                <h3>{order.deliveryName || order.customer.name}</h3>
                <a href={`mailto:${order.customer.email}`}><Mail size={15} />{order.customer.email}</a>
                <a href={`tel:${order.phone}`}><Phone size={15} />{order.phone}</a>
                <p><MapPin size={15} />{order.deliveryAddress}</p>
                <small><CalendarDays size={14} /> Ordered {order.createdAt ? new Date(order.createdAt).toLocaleString("en-IN") : "Recently"}</small>
                {order.scheduledFor && <small>Scheduled: {new Date(order.scheduledFor).toLocaleString("en-IN")}</small>}
              </div>
              <div className="seller-order-items">
                <span className="eyebrow">ITEMS TO PACK</span>
                {order.items.map((item) => (
                  <div key={item.productId}><span><b>{item.productName}</b><small>Qty {item.quantity} × {money(item.unitPrice)}</small></span><strong>{money(item.lineTotal)}</strong></div>
                ))}
              </div>
              <div className="seller-bill-summary">
                <span>Subtotal <b>{money(order.subtotalAmount)}</b></span>
                <span>Delivery charge <b>{money(order.deliveryCharge)}</b></span>
                <span>Carrying charge <b>{money(order.carryingCharge)}</b></span>
                <span>GST (5%) <b>{money(order.gstAmount)}</b></span>
                <span className="seller-grand-total">Grand total <b>{money(order.totalAmount)}</b></span>
              </div>
            </div>
            )}
          </article>
        ))}
        {dashboard?.orders.length === 0 && <p className="seller-loading">No orders have been placed yet.</p>}
      </section>
      </>
      ) : (
      <>
      <section className="seller-create">
        <div className="seller-section-title"><div className="seller-icon"><CirclePlus size={21} /></div><div><span className="eyebrow">ADD TO THE MARKET</span><h2>List a fresh find</h2></div></div>
        <form onSubmit={(event) => void submit(event)} className="product-form">
          <label>Product name<input required value={form.productName} onChange={(event) => change("productName", event.target.value)} placeholder="e.g. Sweet, sun-ripened mangoes" /></label>
          <label>Category<select value={form.category} onChange={(event) => change("category", event.target.value as Product["category"])}><option>Vegetables</option><option>Fruits</option><option>Snacks</option><option>Dairy</option></select></label>
          <label>Price in rupees<input required type="number" min="1" step="0.01" value={form.price || ""} onChange={(event) => change("price", Number(event.target.value))} placeholder="e.g. 65" /></label>
          <label>Available stock<input required type="number" min="0" value={form.stock} onChange={(event) => change("stock", Number(event.target.value))} /></label>
          <label>Unit / pack size<input required value={form.unit} onChange={(event) => change("unit", event.target.value)} placeholder="e.g. 500 g, 1 bunch" /></label>
          <label>Photo URL <span className="optional-label">optional · use a direct image link, not a Google search link</span><input type="url" maxLength={2048} value={form.imageUrl} onChange={(event) => change("imageUrl", event.target.value)} placeholder="https://…/photo.jpg" /></label>
          <label className="full-width">A little about it<textarea value={form.description} onChange={(event) => change("description", event.target.value)} placeholder="Tell shoppers what makes this special…" rows={3} /></label>
          <div className="seller-form-bottom">{notice && <span className={notice.includes("now listed") ? "success-text" : "form-error"}>{notice}</span>}<button className="button" disabled={busy}><BadgeIndianRupee size={17} /> {busy ? "Adding…" : "Add to bazaar"}</button></div>
        </form>
      </section>
      <ProductList isSeller compact />
      </>
      )}
      <BackgroundJobMonitor />
    </main>
  );
}

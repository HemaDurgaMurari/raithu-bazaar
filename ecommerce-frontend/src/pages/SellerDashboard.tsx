import { useState, type FormEvent } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { BadgeIndianRupee, Boxes, CirclePlus, PackageCheck } from "lucide-react";
import { addProduct } from "../api/productApi";
import { errorMessage } from "../api/client";
import { BackgroundJobMonitor } from "../components/BackgroundJobMonitor";
import { ProductList } from "../components/ProductList";
import type { Product, ProductInput } from "../types/product";

const emptyProduct: ProductInput = {
  productName: "", category: "Vegetables", description: "", price: 0, stock: 0,
  unit: "500 g", imageUrl: "", rating: 4.8,
};

export function SellerDashboard() {
  const queryClient = useQueryClient();
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState<ProductInput>(emptyProduct);

  function change<K extends keyof ProductInput>(key: K, value: ProductInput[K]) {
    setForm((current) => ({ ...current, [key]: value }));
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
      <BackgroundJobMonitor />
    </main>
  );
}

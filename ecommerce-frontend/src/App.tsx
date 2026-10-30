import { useState } from "react";
import { ArrowRight, CheckCircle2, ChevronDown, Leaf, ShieldCheck, Truck } from "lucide-react";
import { Navbar, type Page } from "./components/Navbar";
import { ProductList } from "./components/ProductList";
import { Cart } from "./components/Cart";
import { CheckoutForm } from "./components/CheckoutForm";
import { AuthPage } from "./pages/AuthPage";
import { SellerDashboard } from "./pages/SellerDashboard";
import { AccountPage } from "./pages/AccountPage";
import { SiteFooter } from "./components/SiteFooter";
import { useAuthStore } from "./store/authStore";
import { useCartStore } from "./store/cartStore";
import type { Role } from "./types/auth";

export default function App() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const clearCart = useCartStore((state) => state.clearCart);
  const cartCount = useCartStore((state) => state.cartItems.reduce((n, item) => n + item.quantity, 0));
  const [page, setPage] = useState<Page>("home");
  const [orderSuccess, setOrderSuccess] = useState<number | null>(null);
  const [returnToCheckout, setReturnToCheckout] = useState(false);

  function authenticated(role: Role) {
    setPage(role === "SELLER" ? "seller" : returnToCheckout ? "checkout" : "home");
    setReturnToCheckout(false);
  }

  function requireSignInForCheckout() {
    setReturnToCheckout(true);
    setPage("signin");
  }

  function signOut() {
    logout();
    clearCart();
    setPage("home");
  }

  function openCart() {
    setPage("home");
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => {
        document.getElementById("basket")?.scrollIntoView({ behavior: "smooth", block: "center" });
      });
    });
  }

  if (page === "signin" || page === "signup") {
    return <><AuthPage mode={page} setPage={setPage} onAuthenticated={authenticated} /><SiteFooter /></>;
  }

  const sellerCannotBrowseShop = Boolean(user?.role === "SELLER" && page !== "seller");

  return (
    <div className="app-shell">
      <Navbar user={user} page={page} setPage={setPage} cartCount={cartCount} onOpenCart={openCart} onLogout={signOut} />
      {page === "seller" || sellerCannotBrowseShop ? (
        user?.role === "SELLER" ? <SellerDashboard /> : <main className="access-denied"><h1>This corner is for our sellers.</h1><p>Sign in with a seller account to manage your products.</p><button className="button" onClick={() => setPage("signin")}>Sign in as seller</button></main>
      ) : page === "account" ? (
        user?.role === "CUSTOMER" ? <AccountPage /> : <main className="access-denied"><h1>Sign in to view your orders.</h1><button className="button" onClick={() => setPage("signin")}>Sign in</button></main>
      ) : page === "checkout" ? (
        <main className="checkout-page">
          <CheckoutForm onBack={() => setPage("home")} onRequireSignIn={requireSignInForCheckout} onSuccess={(orderId) => { setOrderSuccess(orderId); setPage("home"); }} />
        </main>
      ) : (
        <main>
          {orderSuccess !== null && <div className="order-success"><CheckCircle2 size={22} /><div><b>Thank you — your order is confirmed!</b><span>Order #{orderSuccess} is being prepared. A confirmation email is on its way.</span></div><button onClick={() => setOrderSuccess(null)} aria-label="Dismiss">×</button></div>}
          <section className="hero">
            <div className="hero-copy">
              <span className="eyebrow"><span className="eyebrow-dot" /> FROM OUR FARMS TO YOUR FAMILY</span>
              <h1>Good food.<br />Good <em>roots.</em></h1>
              <p>Fresh-picked goodness from the hands that grow it. Meet your neighbourhood farmers, one delicious bite at a time.</p>
              <div className="hero-actions"><button className="button hero-button" onClick={() => document.getElementById("market")?.scrollIntoView({ behavior: "smooth" })}>Shop the harvest <ArrowRight size={17} /></button><span><span className="avatar-stack"><i>👩🏽‍🌾</i><i>👨🏽‍🌾</i><i>🧑🏽‍🌾</i></span><small><b>1,200+</b> happy local growers</small></span></div>
              <div className="hero-trust"><span><Leaf size={16} /> Farm fresh</span><span><Truck size={17} /> Gentle delivery</span><span><ShieldCheck size={16} /> Goodness guaranteed</span></div>
            </div>
            <div className="hero-visual">
              <div className="hero-photo"><img src="https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=90" alt="A colourful basket of fresh produce at the farmers market" /></div>
              <div className="floating-note note-top"><span>🌿</span><div><b>Picked with care</b><small>From farms near you</small></div></div>
              <div className="floating-note note-bottom"><span className="note-stars">★★★★★</span><div><b>Freshness you can taste</b><small>That's the Raithu promise</small></div></div>
              <div className="hero-deco">✳</div>
            </div>
            <button className="scroll-cue" onClick={() => document.getElementById("market")?.scrollIntoView({ behavior: "smooth" })}>SCROLL TO EXPLORE <ChevronDown size={15} /></button>
          </section>
          <section className="promise-strip"><div><span>01</span><Leaf /><b>Picked at peak freshness</b><small>Never sitting on a shelf for days.</small></div><div><span>02</span><Truck /><b>From farm, not far away</b><small>Support the growers in your community.</small></div><div><span>03</span><ShieldCheck /><b>Good food, honest prices</b><small>Clear pricing, happy kitchens.</small></div></section>
          <section className="market-section" id="market">
            <div className="market-browse"><ProductList /></div>
            <div className="basket-column"><Cart onCheckout={() => setPage("checkout")} /><div className="basket-promo"><span>🌼</span><div><b>A little goes a long way</b><p>Every basket supports a local farmer and a happier planet.</p></div></div></div>
          </section>
        </main>
      )}
      <SiteFooter />
    </div>
  );
}

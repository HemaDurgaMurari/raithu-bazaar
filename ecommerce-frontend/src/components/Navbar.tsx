import { ArrowRight, Leaf, MapPin, PackageCheck, ShoppingBag, Store } from "lucide-react";
import { useEffect, useState } from "react";
import type { AuthResponse } from "../types/auth";

export type Page = "home" | "signin" | "signup" | "seller" | "checkout" | "account";

interface NavbarProps {
  user: AuthResponse | null;
  page: Page;
  setPage: (page: Page) => void;
  cartCount: number;
  onOpenCart: () => void;
  onLogout: () => void;
}

export function Navbar({ user, page, setPage, cartCount, onOpenCart, onLogout }: NavbarProps) {
  const [location, setLocation] = useState("your doorstep");
  const [locating, setLocating] = useState(false);

  useEffect(() => {
    let active = true;
    if (!navigator.geolocation) return;
    setLocating(true);
    navigator.geolocation.getCurrentPosition(async ({ coords }) => {
      try {
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${coords.latitude}&lon=${coords.longitude}`);
        if (!response.ok) throw new Error("Location lookup failed");
        const place = await response.json();
        const city = place.address?.city || place.address?.town || place.address?.village || place.address?.county;
        if (active && city) setLocation(String(city));
      } catch {
        if (active) setLocation("current location");
      } finally {
        if (active) setLocating(false);
      }
    }, () => {
      if (active) {
        setLocation("your doorstep");
        setLocating(false);
      }
    }, { enableHighAccuracy: false, timeout: 9000, maximumAge: 300000 });
    return () => { active = false; };
  }, []);

  return (
    <>
      <div className="announcement"><Leaf size={14} /> Fresh from local farms · Delivered with care <span>Free delivery over ₹499</span></div>
      <header className="navbar">
        <button className="brand" onClick={() => setPage(user?.role === "SELLER" ? "seller" : "home")} aria-label="Raithu Bazaar home">
          <span className="brand-mark"><Leaf size={25} strokeWidth={2.4} /></span>
          <span><strong>raithu bazaar</strong><small>FRESH. LOCAL. GOOD.</small></span>
        </button>
        {user?.role !== "SELLER" && <div className="location-chip" title="Detected from this device"><MapPin size={16} /><span>Delivering to <b>{locating ? "detecting location…" : location}</b></span></div>}
        <nav className="nav-links" aria-label="Main navigation">
          {user?.role !== "SELLER" && <button className={page === "home" ? "active" : ""} onClick={() => setPage("home")}>Shop</button>}
          {user?.role === "CUSTOMER" && <button className={page === "account" ? "active" : ""} onClick={() => setPage("account")}><PackageCheck size={16} /> My orders</button>}
          {user?.role === "SELLER" && <button className={page === "seller" ? "active" : ""} onClick={() => setPage("seller")}><Store size={16} /> Seller studio</button>}
        </nav>
        <div className="nav-actions">
          {user?.role !== "SELLER" && (
            <button className="cart-nav" onClick={onOpenCart} aria-label={`Open cart, ${cartCount} items`} title="View your basket">
              <ShoppingBag size={19} /><span>Basket</span>{cartCount > 0 && <i>{cartCount}</i>}
            </button>
          )}
          {user ? (
            <div className="profile-menu">
              <span className="avatar">{user.name.slice(0, 1).toUpperCase()}</span>
              <span className="profile-name">{user.name.split(" ")[0]}</span>
              <button className="text-button" onClick={onLogout}>Sign out <ArrowRight size={14} /></button>
            </div>
          ) : (
            <div className="auth-actions">
              <button className="text-button" onClick={() => setPage("signin")}>Sign in</button>
              <button className="button button-small" onClick={() => setPage("signup")}>Join us</button>
            </div>
          )}
        </div>
      </header>
    </>
  );
}

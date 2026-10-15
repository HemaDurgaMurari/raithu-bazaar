import { ArrowRight, Leaf, MapPin, ShoppingBag, Store } from "lucide-react";
import type { AuthResponse } from "../types/auth";

export type Page = "home" | "signin" | "signup" | "seller" | "checkout";

interface NavbarProps {
  user: AuthResponse | null;
  page: Page;
  setPage: (page: Page) => void;
  cartCount: number;
  onLogout: () => void;
}

export function Navbar({ user, page, setPage, cartCount, onLogout }: NavbarProps) {
  return (
    <>
      <div className="announcement"><Leaf size={14} /> Fresh from local farms · Delivered with care <span>Free delivery over ₹499</span></div>
      <header className="navbar">
        <button className="brand" onClick={() => setPage("home")} aria-label="Raithu Bazaar home">
          <span className="brand-mark"><Leaf size={25} strokeWidth={2.4} /></span>
          <span><strong>raithu bazaar</strong><small>FRESH. LOCAL. GOOD.</small></span>
        </button>
        <div className="location-chip"><MapPin size={16} /><span>Delivering to <b>your doorstep</b></span></div>
        <nav className="nav-links" aria-label="Main navigation">
          <button className={page === "home" ? "active" : ""} onClick={() => setPage("home")}>Shop</button>
          {user?.role === "SELLER" && <button className={page === "seller" ? "active" : ""} onClick={() => setPage("seller")}><Store size={16} /> Seller studio</button>}
        </nav>
        <div className="nav-actions">
          <button className="cart-nav" onClick={() => setPage("home")} aria-label={`Cart, ${cartCount} items`}>
            <ShoppingBag size={19} /><span>Basket</span>{cartCount > 0 && <i>{cartCount}</i>}
          </button>
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

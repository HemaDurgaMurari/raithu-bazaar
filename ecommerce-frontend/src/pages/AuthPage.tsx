import { useState, type FormEvent } from "react";
import { ArrowLeft, ArrowRight, Leaf, LockKeyhole } from "lucide-react";
import { errorMessage } from "../api/client";
import { signin, signup } from "../api/authApi";
import { useAuthStore } from "../store/authStore";
import type { AuthRequest, Role } from "../types/auth";
import type { Page } from "../components/Navbar";

interface AuthPageProps {
  mode: "signin" | "signup";
  setPage: (page: Page) => void;
  onAuthenticated: (role: Role) => void;
}

export function AuthPage({ mode, setPage, onAuthenticated }: AuthPageProps) {
  const login = useAuthStore((state) => state.login);
  const [role, setRole] = useState<Role>("CUSTOMER");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const registering = mode === "signup";

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setBusy(true);
    const request: AuthRequest = { name: registering ? name : undefined, email, password };
    try {
      const user = registering ? await signup(request, role) : await signin(request);
      login(user);
      onAuthenticated(user.role);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-art">
        <div className="auth-art-brand"><span className="brand-mark"><Leaf size={24} /></span><b>raithu bazaar</b></div>
        <div className="auth-art-copy"><span className="eyebrow">GROWN WITH CARE</span><h2>A little closer<br />to the good life.</h2><p>Good food has a way of bringing us together. Let's make every meal count.</p></div>
        <div className="auth-art-note"><span>“</span><p>Fresh picked this morning, on your table by evening.</p><small>— Our farmers, always</small></div>
        <img src="https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1100&q=85" alt="Fresh vegetables at a local market" />
      </section>
      <section className="auth-form-side">
        <button className="back-link" onClick={() => setPage("home")}><ArrowLeft size={16} /> Back to the market</button>
        <div className="auth-card">
          <div className="auth-icon"><Leaf size={23} /></div>
          <span className="eyebrow">{registering ? "MAKE YOURSELF AT HOME" : "WELCOME BACK"}</span>
          <h1>{registering ? "Join our little corner." : "Good to see you."}</h1>
          <p>{registering ? "One account for your farm-fresh favourites." : "Your market basket missed you."}</p>
          <form onSubmit={(event) => void submit(event)}>
            {registering && <label>Your name<input required minLength={2} value={name} onChange={(event) => setName(event.target.value)} placeholder="How should we call you?" /></label>}
            <label>Email address<input required type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /></label>
            <label>Password<input required minLength={8} type="password" autoComplete={registering ? "new-password" : "current-password"} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" /></label>
            {registering && <fieldset className="account-choice"><legend>I am here to…</legend>
              <button type="button" className={role === "CUSTOMER" ? "choice active-choice" : "choice"} onClick={() => setRole("CUSTOMER")}><span>🧺</span><b>Shop fresh</b><small>I'm a customer</small></button>
              <button type="button" className={role === "SELLER" ? "choice active-choice" : "choice"} onClick={() => setRole("SELLER")}><span>🌱</span><b>Sell local</b><small>I'm a farmer / seller</small></button>
            </fieldset>}
            {error && <div className="notice error-notice">{error}</div>}
            <button className="button auth-submit" disabled={busy}>{busy ? "One moment…" : registering ? "Create my account" : "Sign in"} <ArrowRight size={17} /></button>
          </form>
          <div className="privacy-note"><LockKeyhole size={14} /> Your details are safely encrypted</div>
          <p className="auth-switch">{registering ? "Already have an account?" : "New to the bazaar?"} <button className="inline-link" onClick={() => setPage(registering ? "signin" : "signup")}>{registering ? "Sign in" : "Create an account"}</button></p>
        </div>
        <small className="auth-footer">A good day starts with good food. © Raithu Bazaar</small>
      </section>
    </main>
  );
}

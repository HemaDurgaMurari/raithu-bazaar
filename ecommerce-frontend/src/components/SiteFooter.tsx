import { Leaf } from "lucide-react";

const repositoryUrl = "https://github.com/HemaDurgaMurari/raithu-bazaar";

export function SiteFooter() {
  return (
    <footer className="footer">
      <div className="footer-brand"><span className="brand-mark"><Leaf size={23} /></span><div><b>raithu bazaar</b><small>GOOD FOOD. GOOD ROOTS.</small></div></div>
      <p>Grown nearby. Shared with love.</p>
      <div className="footer-meta">
        <small>© 2026 Raithu Bazaar · Made for the love of fresh.</small>
        <span>Developed by Murari · <a href={repositoryUrl} target="_blank" rel="noopener noreferrer">View GitHub repository</a></span>
      </div>
    </footer>
  );
}

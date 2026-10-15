import { useMemo, useState } from "react";
import { Search, SlidersHorizontal } from "lucide-react";
import { useProducts } from "../hooks/useProducts";
import { ProductCard } from "./ProductCard";

interface ProductListProps {
  isSeller?: boolean;
  compact?: boolean;
}

export function ProductList({ isSeller = false, compact = false }: ProductListProps) {
  const { data = [], isLoading, isError, error } = useProducts();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const categories = ["All", "Vegetables", "Fruits", "Snacks", "Dairy"];
  const shown = useMemo(() => data.filter((product) =>
    (category === "All" || product.category === category)
    && product.productName.toLowerCase().includes(search.toLowerCase().trim())),
  [category, data, search]);

  if (isLoading) return <div className="loading-state"><span className="spinner" /> Gathering today's harvest…</div>;
  if (isError) return <div className="notice error-notice">We couldn't reach the market. Check that the backend and PostgreSQL are running. {error.message}</div>;

  return (
    <section className={`catalog ${compact ? "catalog-compact" : ""}`}>
      {!compact && <div className="section-heading"><div><span className="eyebrow">THE GOOD STUFF</span><h2>Today's fresh picks</h2><p>Handpicked with love, straight from farmers near you.</p></div><span className="item-count">{shown.length} lovely finds</span></div>}
      <div className="catalog-toolbar">
        <label className="search-box"><Search size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Find your favourites…" /><kbd>/</kbd></label>
        <div className="category-filters" aria-label="Filter products by category">
          <SlidersHorizontal size={17} className="filter-icon" />
          {categories.map((item) => <button key={item} className={category === item ? "selected" : ""} onClick={() => setCategory(item)}>{item}</button>)}
        </div>
      </div>
      {shown.length > 0 ? (
        <div className="product-grid">{shown.map((product) => <ProductCard key={product.productId} product={product} isSeller={isSeller} />)}</div>
      ) : (
        <div className="empty-products"><span>🥬</span><h3>Nothing in this basket yet</h3><p>Try a different search or category.</p></div>
      )}
    </section>
  );
}

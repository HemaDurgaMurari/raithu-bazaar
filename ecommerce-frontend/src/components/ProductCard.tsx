import { useState, type FormEvent } from "react";
import { Heart, ShoppingBasket, Star, Trash2 } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { deleteProduct, updateProduct } from "../api/productApi";
import { errorMessage } from "../api/client";
import { useCartStore } from "../store/cartStore";
import type { Product, ProductInput } from "../types/product";

interface ProductCardProps {
  product: Product;
  isSeller?: boolean;
}

function productImage(product: Product) {
  if (product.imageUrl?.trim()) return product.imageUrl;
  const name = product.productName.toLowerCase();
  const category = product.category.toLowerCase();
  if (name.includes("tomato")) return "https://images.unsplash.com/photo-1546470427-e5ac89b0b4ed?auto=format&fit=crop&w=900&q=85";
  if (name.includes("banana")) return "https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=900&q=85";
  if (name.includes("mango")) return "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=85";
  if (name.includes("spinach") || name.includes("leaf")) return "https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=900&q=85";
  if (name.includes("milk")) return "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=900&q=85";
  if (category === "fruits") return "https://images.unsplash.com/photo-1619566636858-adf3ef4640b1?auto=format&fit=crop&w=900&q=85";
  if (category === "dairy") return "https://images.unsplash.com/photo-1628088062854-d1870b4553da?auto=format&fit=crop&w=900&q=85";
  if (category === "snacks") return "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?auto=format&fit=crop&w=900&q=85";
  return "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=85";
}

export function ProductCard({ product, isSeller = false }: ProductCardProps) {
  const addToCart = useCartStore((state) => state.addToCart);
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  async function saveEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    const input: ProductInput = {
      productName: String(values.get("productName")),
      category: String(values.get("category")) as Product["category"],
      description: String(values.get("description")),
      price: Number(values.get("price")),
      stock: Number(values.get("stock")),
      unit: String(values.get("unit")),
      imageUrl: String(values.get("imageUrl")),
      rating: product.rating,
    };
    setSaving(true);
    setMessage("");
    try {
      await updateProduct(product.productId, input);
      await queryClient.invalidateQueries({ queryKey: ["products"] });
      setEditing(false);
    } catch (error) {
      setMessage(errorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  async function removeProduct() {
    if (!window.confirm(`Remove ${product.productName} from your store?`)) return;
    try {
      await deleteProduct(product.productId);
      await queryClient.invalidateQueries({ queryKey: ["products"] });
    } catch (error) {
      setMessage(errorMessage(error));
    }
  }

  if (editing) {
    return (
      <form className="edit-product-card" onSubmit={saveEdit}>
        <div className="edit-card-heading"><strong>Edit item</strong><button type="button" className="icon-button" onClick={() => setEditing(false)}>×</button></div>
        <label>Product name<input name="productName" required defaultValue={product.productName} /></label>
        <label>Category<select name="category" defaultValue={product.category}><option>Vegetables</option><option>Fruits</option><option>Snacks</option><option>Dairy</option></select></label>
        <div className="field-pair"><label>Price (₹)<input name="price" type="number" min="1" step="0.01" defaultValue={product.price} /></label><label>Stock<input name="stock" type="number" min="0" defaultValue={product.stock} /></label></div>
        <label>Unit<input name="unit" required defaultValue={product.unit} /></label>
        <label>Image URL<input name="imageUrl" type="url" defaultValue={product.imageUrl} /></label>
        <label>Description<textarea name="description" defaultValue={product.description} /></label>
        {message && <p className="form-error">{message}</p>}
        <button className="button" disabled={saving}>{saving ? "Saving…" : "Save changes"}</button>
      </form>
    );
  }

  const image = productImage(product);
  return (
    <article className="product-card">
      <div className="product-image-wrap">
        <img src={image} alt={product.productName} loading="lazy" onError={(event) => {
          event.currentTarget.onerror = null;
          event.currentTarget.src = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=85";
        }} />
        <span className="product-category">{product.category}</span>
        <button className="favorite-button" aria-label={`Save ${product.productName}`}><Heart size={17} /></button>
      </div>
      <div className="product-content">
        <div className="product-rating"><Star size={14} fill="currentColor" /> {product.rating?.toFixed(1) ?? "4.8"} <span>· Farm fresh</span></div>
        <h3>{product.productName}</h3>
        <p className="product-description">{product.description}</p>
        <div className="product-stock">{product.stock > 0 ? `${product.stock} available` : "Just sold out"}</div>
        {message && <p className="form-error product-action-error">{message}</p>}
        <div className="product-buy-row">
          <div className="product-price"><strong>₹{product.price.toFixed(0)}</strong><span> / {product.unit}</span></div>
          {isSeller ? (
            <div className="seller-card-actions">
              <button className="icon-button edit-button" onClick={() => setEditing(true)} aria-label="Edit product">Edit</button>
              <button className="icon-button delete-button" onClick={removeProduct} aria-label="Delete product"><Trash2 size={17} /></button>
            </div>
          ) : (
            <button className="add-button" disabled={product.stock === 0} onClick={() => addToCart(product)}>
              <ShoppingBasket size={16} /> Add
            </button>
          )}
        </div>
      </div>
    </article>
  );
}

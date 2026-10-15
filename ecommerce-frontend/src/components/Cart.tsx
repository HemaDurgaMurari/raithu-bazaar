import { ArrowRight, Minus, Plus, ShoppingBasket, Trash2 } from "lucide-react";
import { useCartStore } from "../store/cartStore";

export function Cart({ onCheckout }: { onCheckout: () => void }) {
  const { cartItems, increaseQuantity, decreaseQuantity, clearCart } = useCartStore();
  const total = cartItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0);

  return (
    <aside className="cart-panel" id="basket">
      <div className="cart-heading">
        <div><span className="eyebrow">YOUR GOODIES</span><h2>Your basket <span className="cart-count">{cartItems.reduce((n, item) => n + item.quantity, 0)}</span></h2></div>
        {cartItems.length > 0 && <button className="clear-cart" onClick={clearCart}><Trash2 size={14} /> Clear</button>}
      </div>
      {cartItems.length === 0 ? (
        <div className="cart-empty"><ShoppingBasket size={32} /><p>Your basket is taking a little nap.</p><span>Add something fresh to get started.</span></div>
      ) : (
        <>
          <div className="cart-lines">
            {cartItems.map(({ product, quantity }) => (
              <div className="cart-line" key={product.productId}>
                <img src={product.imageUrl} alt="" />
                <div className="cart-line-info"><strong>{product.productName}</strong><small>₹{product.price.toFixed(0)} / {product.unit}</small><div className="quantity-control">
                  <button onClick={() => decreaseQuantity(product.productId)} aria-label={`Remove one ${product.productName}`}><Minus size={13} /></button>
                  <span>{quantity}</span>
                  <button onClick={() => increaseQuantity(product.productId)} disabled={quantity >= product.stock} aria-label={`Add one ${product.productName}`}><Plus size={13} /></button>
                </div></div>
                <b className="line-total">₹{(product.price * quantity).toFixed(0)}</b>
              </div>
            ))}
          </div>
          <div className="cart-subtotal"><span>Subtotal</span><strong>₹{total.toFixed(2)}</strong></div>
          <p className="delivery-note">Delivery and taxes calculated at checkout</p>
          <button className="button checkout-button" onClick={onCheckout}>Continue to checkout <ArrowRight size={17} /></button>
          <p className="secure-note">🔒 Safe and secure checkout</p>
        </>
      )}
    </aside>
  );
}

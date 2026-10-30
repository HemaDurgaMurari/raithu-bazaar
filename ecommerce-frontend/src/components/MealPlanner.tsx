import { useState, type FormEvent } from "react";
import { CookingPot, LoaderCircle, ShoppingBasket, Sparkles } from "lucide-react";
import { errorMessage } from "../api/client";
import { suggestMeal, type MealPlan } from "../api/mealPlannerApi";
import { useCartStore } from "../store/cartStore";
import type { Product } from "../types/product";

export function MealPlanner() {
  const addToCart = useCartStore((state) => state.addToCart);
  const [dish, setDish] = useState("");
  const [servings, setServings] = useState(2);
  const [plan, setPlan] = useState<MealPlan | null>(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    setPlan(null);
    try {
      setPlan(await suggestMeal(dish.trim(), servings));
    } catch (error) {
      setMessage(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  function addProduct(product: MealPlan["ingredients"][number]["products"][number]) {
    addToCart({
      ...product,
      category: "Vegetables",
      description: "Suggested for your dish.",
      rating: 4.8,
      status: product.stock > 0 ? "AVAILABLE" : "OUT_OF_STOCK",
    } as Product);
  }

  return (
    <section className="meal-planner" aria-labelledby="meal-planner-title">
      <div className="meal-planner-heading">
        <span className="meal-planner-icon"><CookingPot size={20} /></span>
        <div><span className="eyebrow">A LITTLE HELP IN THE KITCHEN</span><h2 id="meal-planner-title">What are you cooking?</h2><p>Tell us your dish and we’ll find the fresh vegetables for it.</p></div>
      </div>
      <form className="meal-planner-form" onSubmit={submit}>
        <label className="meal-dish-field">Dish or curry<input value={dish} onChange={(event) => setDish(event.target.value)} placeholder="e.g. vegetable pulao" maxLength={120} required /></label>
        <label className="meal-servings-field">Servings<input type="number" min={1} max={20} value={servings} onChange={(event) => setServings(Number(event.target.value))} required /></label>
        <button className="button" disabled={loading || !dish.trim()}>{loading ? <LoaderCircle className="meal-spin" size={16} /> : <Sparkles size={16} />}{loading ? "Finding vegetables…" : "Suggest ingredients"}</button>
      </form>
      {message && <p className="notice error-notice meal-message">{message}</p>}
      {plan && <div className="meal-results"><p className="meal-note">{plan.note}</p>
        {plan.ingredients.length === 0 ? <p className="meal-note">Try entering a dish name such as “aloo curry” or “vegetable pulao”.</p> : plan.ingredients.map((ingredient) => (
          <article className="meal-ingredient" key={`${ingredient.name}-${ingredient.quantity}`}>
            <div className="meal-ingredient-copy"><b>{ingredient.name}</b><span>{ingredient.quantity}{ingredient.note ? ` · ${ingredient.note}` : ""}</span></div>
            <div className="meal-products">{ingredient.products.length ? ingredient.products.map((product) => (
              <div className="meal-product" key={product.productId}><span>{product.productName}<small>₹{product.price.toFixed(0)} / {product.unit} · {product.stock} in stock</small></span>
                <button className="add-button" onClick={() => addProduct(product)}><ShoppingBasket size={14} /> Add</button></div>
            )) : <span className="meal-unavailable">Not available in today’s market</span>}</div>
          </article>
        ))}
      </div>}
    </section>
  );
}

package com.klu.raithubazaar.config;

import com.klu.raithubazaar.model.Product;
import com.klu.raithubazaar.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private final ProductRepository products;
    public DataLoader(ProductRepository products) { this.products = products; }
    @Override
    public void run(String... args) {
        if (products.count() > 0) return;
        products.saveAll(List.of(
                product("Farm-fresh tomatoes", "Vegetables", "Juicy, sun-ripened tomatoes from local farms.", "38", 44, "500 g", "photo-1546094096-0df4bcaaa337"),
                product("Green spinach", "Vegetables", "Tender leafy greens, freshly picked this morning.", "25", 35, "bunch", "photo-1576045057995-568f588f82fb"),
                product("Desi carrots", "Vegetables", "Sweet, crunchy carrots grown close to home.", "42", 29, "500 g", "photo-1445282768818-728615cc910a"),
                product("Farm cucumbers", "Vegetables", "Cool and crisp cucumbers for salads and snacks.", "30", 31, "500 g", "photo-1604977042946-1eecc30f269e"),
                product("Red onions", "Vegetables", "A kitchen essential with a rich local flavour.", "34", 56, "1 kg", "photo-1508747703725-719777637510"),
                product("Green capsicum", "Vegetables", "Fresh bell peppers with a satisfying crunch.", "48", 24, "500 g", "photo-1563565375-f3fdfdbefa83"),
                product("Sweet bananas", "Fruits", "Naturally sweet bananas, perfect for every day.", "52", 40, "6 pcs", "photo-1571771894821-ce9b6c11b08e"),
                product("Alphonso mangoes", "Fruits", "A fragrant seasonal favourite from Indian orchards.", "149", 18, "500 g", "photo-1553279768-865429fa0078"),
                product("Juicy oranges", "Fruits", "Bright, sweet oranges packed with sunshine.", "75", 28, "1 kg", "photo-1547514701-42782101795e"),
                product("Crisp red apples", "Fruits", "Crisp apples selected for a sweet bite.", "135", 21, "4 pcs", "photo-1560806887-1e4cd0b6cbd6"),
                product("Green grapes", "Fruits", "Seedless, refreshing grapes from local growers.", "90", 19, "500 g", "photo-1537640538966-79f369143f8f"),
                product("Farm masala murukku", "Snacks", "Crunchy, small-batch savoury spirals.", "65", 26, "200 g", "photo-1599490659213-e2b9527bd087"),
                product("Roasted millet mix", "Snacks", "A wholesome, lightly spiced local snack.", "85", 15, "200 g", "photo-1599490659213-e2b9527bd087"),
                product("Traditional jaggery chikki", "Snacks", "Peanuts and jaggery in a crisp, nostalgic bite.", "55", 32, "150 g", "photo-1606313564200-e75d5e30476c"),
                product("Fresh cow milk", "Dairy", "Creamy, farm-fresh milk. Keep refrigerated.", "34", 60, "500 ml", "photo-1563636619-e9143da7973b"),
                product("Homestyle paneer", "Dairy", "Soft, fresh paneer prepared in small batches.", "110", 17, "200 g", "photo-1631452180519-c014fe946bc7"),
                product("Cultured curd", "Dairy", "Mild and creamy curd made fresh each day.", "40", 33, "400 g", "photo-1488477181946-6428a0291777"),
                product("Farm butter", "Dairy", "Rich, smooth butter from local dairy farms.", "92", 12, "100 g", "photo-1589985270826-4b7bb135bc9d")
        ));
    }

    private Product product(String name, String category, String description, String price, int stock, String unit, String photo) {
        Product p = new Product();
        p.setProductName(name);
        p.setCategory(category);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setUnit(unit);
        p.setImageUrl("https://images.unsplash.com/" + photo + "?auto=format&fit=crop&w=900&q=85");
        p.setRating(4.7);
        p.setStatus("AVAILABLE");
        return p;
    }
}

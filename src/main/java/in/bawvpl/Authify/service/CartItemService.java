package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import org.springframework.data.domain.Page;

public interface CartItemService {

    CartItemResponse addItem(String email, CartItemRequest req);

    Page<CartItemResponse> getItemsForUser(String email, int page, int size);

    void removeItem(String email, String productId);

    // 🔥 NEW
    void updateQuantity(String email, String productId, int quantity);

    // 🔥 NEW
    void clearCart(String email);
}
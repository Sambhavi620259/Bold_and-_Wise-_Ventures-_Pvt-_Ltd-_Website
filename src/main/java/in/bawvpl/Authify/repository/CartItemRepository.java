package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.Cart;
import in.bawvpl.Authify.entity.CartItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find specific item
    Optional<CartItem> findByCartAndProductId(Cart cart, String productId);

    // Pagination
    Page<CartItem> findByCart(Cart cart, Pageable pageable);

    // 🔥 REQUIRED for clear cart
    void deleteByCart(Cart cart);
}
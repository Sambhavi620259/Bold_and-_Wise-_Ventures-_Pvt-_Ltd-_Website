package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    // ================= HELPER =================
    private UserEntity getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Cart getOrCreateCart(UserEntity user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", user.getEmail());
                    return cartRepository.save(Cart.builder().user(user).build());
                });
    }

    // ================= ADD ITEM =================
    @Override
    @Transactional
    public CartItemResponse addItem(String email, CartItemRequest req) {

        String normalizedEmail = email.toLowerCase().trim();
        UserEntity user = getUser(normalizedEmail);
        Cart cart = getOrCreateCart(user);

        if (req.getProductId() == null || req.getProductId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID required");
        }

        if (req.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
        }

        CartItem existing = cartItemRepository
                .findByCartAndProductId(cart, req.getProductId())
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            return toResponse(cartItemRepository.save(existing));
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(req.getProductId())
                .productName(req.getProductName())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .build();

        return toResponse(cartItemRepository.save(item));
    }

    // ================= GET ITEMS =================
    @Override
    public Page<CartItemResponse> getItemsForUser(String email, int page, int size) {

        UserEntity user = getUser(email);
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return new PageImpl<>(List.of());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return cartItemRepository.findByCart(cart, pageable)
                .map(this::toResponse);
    }

    // ================= REMOVE ITEM =================
    @Override
    @Transactional
    public void removeItem(String email, String productId) {

        UserEntity user = getUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        cartItemRepository.delete(item);
    }

    // ================= UPDATE QUANTITY =================
    @Override
    @Transactional
    public void updateQuantity(String email, String productId, int quantity) {

        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        UserEntity user = getUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    // ================= CLEAR CART =================
    @Override
    @Transactional
    public void clearCart(String email) {

        UserEntity user = getUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        cartItemRepository.deleteByCart(cart);
    }

    // ================= MAPPER =================
    private CartItemResponse toResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .userId(item.getCart().getUser().getEmail())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
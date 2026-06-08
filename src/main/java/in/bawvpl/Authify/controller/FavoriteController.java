package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.FavoriteResponse;
import in.bawvpl.Authify.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/favorites")
@RequiredArgsConstructor
//@CrossOrigin("*")
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    // =====================================================
    // HELPER
    // =====================================================

    private String getEmail(Authentication auth) {

        if (
                auth == null ||
                        auth.getName() == null ||
                        auth.getName().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        return auth.getName()
                .trim()
                .toLowerCase();
    }

    // =====================================================
    // TOGGLE FAVORITE
    // =====================================================

    @PostMapping("/{appId}")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(

            Authentication auth,

            @PathVariable Long appId
    ) {

        try {

            if (appId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "appId required"
                );
            }

            String email =
                    getEmail(auth);

            String message =
                    favoriteService.toggleFavorite(
                            email,
                            appId
                    );

            return ResponseEntity.ok(

                    ApiResponse.<String>builder()

                            .status(200)

                            .message(message)

                            .data(message)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Toggle favorite failed",
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update favorite"
            );
        }
    }

    // =====================================================
    // GET FAVORITES (/my)
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(

            Authentication auth
    ) {

        try {

            String email =
                    getEmail(auth);

            List<FavoriteResponse> list =
                    favoriteService.get(email);

            return ResponseEntity.ok(

                    ApiResponse.<List<FavoriteResponse>>builder()

                            .status(200)

                            .message("Favorites fetched successfully")

                            .data(list)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Get favorites failed",
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch favorites"
            );
        }
    }

    // =====================================================
    // OPTIONAL BACKWARD COMPATIBILITY
    // =====================================================

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavoritesList(

            Authentication auth
    ) {

        return getFavorites(auth);
    }

    // =====================================================
    // REMOVE FAVORITE
    // =====================================================

    @DeleteMapping("/{appId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(

            Authentication auth,

            @PathVariable Long appId
    ) {

        try {

            if (appId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "appId required"
                );
            }

            String email =
                    getEmail(auth);

            favoriteService.remove(
                    email,
                    appId
            );

            return ResponseEntity.ok(

                    ApiResponse.<String>builder()

                            .status(200)

                            .message("Removed from favorites")

                            .data("Removed from favorites")

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Remove favorite failed",
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to remove favorite"
            );
        }
    }
}
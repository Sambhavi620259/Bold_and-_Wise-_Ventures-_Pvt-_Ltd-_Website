package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.io.ApiResponse;

import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1.0/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    private final UserRepository userRepository;

    // =====================================================
    // HELPER
    // =====================================================

    private Long getUserId() {

        try {

            String email =

                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getName();

            return userRepository

                    .findByEmailIgnoreCase(email)

                    .orElseThrow(() ->

                            new ResponseStatusException(

                                    HttpStatus.UNAUTHORIZED,

                                    "User not found"
                            )
                    )

                    .getId();

        } catch (Exception ex) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Unauthorized"
            );
        }
    }

    // =====================================================
    // ALL TRANSACTIONS
    //
    // IMPORTANT:
    //
    // Frontend contract:
    //
    // GET /transactions?page=0&size=10
    // =====================================================

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionEntity>>> getAll(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String status
    ) {

        try {

            log.info(

                    "Transactions API hit page={} size={} status={}",

                    page,

                    size,

                    status
            );

            page = Math.max(page, 0);

            size = Math.max(size, 1);

            size = Math.min(size, 100);

            Long userId =
                    getUserId();

            Page<TransactionEntity> data =

                    transactionService.getTransactions(

                            userId,

                            page,

                            size,

                            status
                    );

            return ResponseEntity.ok(

                    ApiResponse.<Page<TransactionEntity>>builder()

                            .status(200)

                            .message(
                                    "Transactions fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (ResponseStatusException ex) {

            log.error(
                    "Transaction fetch failed",
                    ex
            );

            return ResponseEntity

                    .status(ex.getStatusCode())

                    .body(

                            ApiResponse.<Page<TransactionEntity>>builder()

                                    .status(
                                            ex.getStatusCode().value()
                                    )

                                    .message(
                                            ex.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception ex) {

            log.error(
                    "Transaction fetch failed",
                    ex
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Page<TransactionEntity>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch transactions"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // MY TRANSACTIONS
    //
    // IMPORTANT:
    //
    // Frontend alias endpoint:
    //
    // GET /transactions/my
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<TransactionEntity>>> myTransactions(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String status
    ) {

        try {

            log.info(

                    "My transactions API hit page={} size={} status={}",

                    page,

                    size,

                    status
            );

            page = Math.max(page, 0);

            size = Math.max(size, 1);

            size = Math.min(size, 100);

            Long userId =
                    getUserId();

            Page<TransactionEntity> data =

                    transactionService.getTransactions(

                            userId,

                            page,

                            size,

                            status
                    );

            return ResponseEntity.ok(

                    ApiResponse.<Page<TransactionEntity>>builder()

                            .status(200)

                            .message(
                                    "My transactions fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (ResponseStatusException ex) {

            log.error(
                    "My transactions fetch failed",
                    ex
            );

            return ResponseEntity

                    .status(ex.getStatusCode())

                    .body(

                            ApiResponse.<Page<TransactionEntity>>builder()

                                    .status(
                                            ex.getStatusCode().value()
                                    )

                                    .message(
                                            ex.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception ex) {

            log.error(
                    "My transactions fetch failed",
                    ex
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Page<TransactionEntity>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch my transactions"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // DETAIL
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionEntity>> detail(

            @PathVariable
            Long id
    ) {

        try {

            log.info(
                    "Transaction detail API hit id={}",
                    id
            );

            TransactionEntity data =

                    transactionService.getDetail(id);

            return ResponseEntity.ok(

                    ApiResponse.<TransactionEntity>builder()

                            .status(200)

                            .message(
                                    "Transaction detail fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (ResponseStatusException ex) {

            log.error(
                    "Transaction detail failed",
                    ex
            );

            return ResponseEntity

                    .status(ex.getStatusCode())

                    .body(

                            ApiResponse.<TransactionEntity>builder()

                                    .status(
                                            ex.getStatusCode().value()
                                    )

                                    .message(
                                            ex.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception ex) {

            log.error(
                    "Transaction detail failed",
                    ex
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<TransactionEntity>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch transaction detail"
                                    )

                                    .build()
                    );
        }
    }
}
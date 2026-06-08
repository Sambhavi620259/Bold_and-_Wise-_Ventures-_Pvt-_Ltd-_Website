package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.TransactionEntity;

import in.bawvpl.Authify.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // =====================================================
    // GET TRANSACTIONS
    //
    // IMPORTANT:
    //
    // Frontend expects:
    //
    // - paginated response
    // - content
    // - totalElements
    //
    // Supports:
    //
    // - optional status filter
    // - newest first ordering
    // =====================================================

    public Page<TransactionEntity> getTransactions(

            Long userId,

            int page,

            int size,

            String status
    ) {

        try {

            // =====================================================
            // VALIDATION
            // =====================================================

            if (userId == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "User id required"
                );
            }

            // =====================================================
            // SAFE PAGINATION
            // =====================================================

            page = Math.max(page, 0);

            if (size <= 0) {

                size = 10;
            }

            size = Math.min(size, 100);

            // =====================================================
            // PAGINATION
            // =====================================================

            Pageable pageable =
                    PageRequest.of(

                            page,

                            size,

                            Sort.by("paymentDate")
                                    .descending()
                    );

            // =====================================================
            // FILTER BY STATUS
            // =====================================================

            if (

                    status != null &&

                            !status.isBlank()
            ) {

                status =
                        status
                                .trim()
                                .toUpperCase();

                log.info(

                        "Fetching transactions userId={} status={}",

                        userId,

                        status
                );

                return transactionRepository
                        .findByUser_IdAndStatus(

                                userId,

                                status,

                                pageable
                        );
            }

            // =====================================================
            // ALL TRANSACTIONS
            // =====================================================

            log.info(
                    "Fetching all transactions userId={}",
                    userId
            );

            return transactionRepository
                    .findByUser_Id(

                            userId,

                            pageable
                    );

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Transaction fetch failed",
                    ex
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to fetch transactions"
            );
        }
    }

    // =====================================================
    // GET DETAIL
    //
    // IMPORTANT:
    //
    // Used by:
    // - transaction detail page
    // - payment history
    // =====================================================

    public TransactionEntity getDetail(
            Long id
    ) {

        try {

            if (id == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Transaction id required"
                );
            }

            log.info(
                    "Fetching transaction detail id={}",
                    id
            );

            return transactionRepository
                    .findById(id)

                    .orElseThrow(() ->

                            new ResponseStatusException(

                                    HttpStatus.NOT_FOUND,

                                    "Transaction not found"
                            )
                    );

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Transaction detail failed",
                    ex
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to fetch transaction detail"
            );
        }
    }
}
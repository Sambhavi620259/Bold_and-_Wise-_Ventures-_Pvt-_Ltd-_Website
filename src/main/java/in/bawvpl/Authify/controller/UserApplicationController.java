package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.UserApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1.0/app")
@RequiredArgsConstructor
//@CrossOrigin("*")
@Slf4j
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    // =====================================================
    // HELPER
    // =====================================================

    private String getEmail(
            Authentication auth
    ) {

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
    // APPLY APP (PATH VARIABLE)
    // =====================================================

    @PostMapping("/apply/{appId}")
    public ResponseEntity<?> applyApp(

            @PathVariable Long appId,

            Authentication authentication
    ) {

        try {

            var result =

                    userApplicationService.applyApp(

                            getEmail(authentication),

                            appId
                    );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "App applied successfully"
                            )

                            .data(result)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Apply app failed",
                    e
            );

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(

                            ApiResponse.builder()

                                    .status(
                                            e.getStatusCode().value()
                                    )

                                    .message(
                                            e.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception e) {

            log.error(
                    "Apply app failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Internal server error"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // APPLY APP (QUERY PARAM SUPPORT)
    // =====================================================

    @PostMapping("/apply")
    public ResponseEntity<?> applyAppQuery(

            @RequestParam Long appId,

            Authentication authentication
    ) {

        try {

            var result =

                    userApplicationService.applyApp(

                            getEmail(authentication),

                            appId
                    );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "App applied successfully"
                            )

                            .data(result)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Apply app failed",
                    e
            );

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(

                            ApiResponse.builder()

                                    .status(
                                            e.getStatusCode().value()
                                    )

                                    .message(
                                            e.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception e) {

            log.error(
                    "Apply app failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Internal server error"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // GET MY APPLICATIONS
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyApplications(

            Authentication authentication
    ) {

        try {

            var result =

                    userApplicationService
                            .getUserApplications(

                                    getEmail(authentication)
                            );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "My applications fetched"
                            )

                            .data(result)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch my applications failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // GET USER APP
    // =====================================================

    @GetMapping("/{appId}")
    public ResponseEntity<?> getUserApp(

            @PathVariable Long appId,

            Authentication authentication
    ) {

        try {

            var result =

                    userApplicationService.getUserApp(

                            getEmail(authentication),

                            appId
                    );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "User app fetched"
                            )

                            .data(result)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch user app failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }
}
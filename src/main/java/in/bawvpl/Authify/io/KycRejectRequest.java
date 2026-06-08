package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRejectRequest {

    // =====================================================
    // REJECTION REASON
    // =====================================================

    private String reason;

    // =====================================================
    // OPTIONAL ALIAS
    // FRONTEND MAY SEND:
    // rejectionReason
    // =====================================================

    private String rejectionReason;
}
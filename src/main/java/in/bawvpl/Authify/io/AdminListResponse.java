package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListResponse {

    private List<AdminResponse> content;

    private int totalPages;

    private long totalElements;

    private int currentPage;

    private int pageSize;

    private boolean hasNext;

    private boolean hasPrevious;
}
package fu.de190599.room.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"size", "page", "totalPages", "totalElements", "first", "last", "content"})
public class PageDTO<T> {

    private int size;
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private List<T> content;

    public static <T> PageDTO<T> of(Page<?> page, List<T> content) {
        return new PageDTO<>(page.getSize(), page.getNumber(), page.getTotalPages(),
                page.getTotalElements(), page.isFirst(), page.isLast(), content);
    }
}

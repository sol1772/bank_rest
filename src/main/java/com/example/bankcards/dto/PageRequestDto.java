package com.example.bankcards.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {
    private Integer pageNumber = 0;
    private Integer pageSize = 10;
    private Sort.Direction sort = Sort.Direction.ASC;
    private String sortByColumn = "id";

    public Pageable getPageable() {
        return PageRequest.of(this.pageNumber, this.pageSize, this.sort, this.sortByColumn);
    }
}

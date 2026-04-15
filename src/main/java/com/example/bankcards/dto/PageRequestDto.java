package com.example.bankcards.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {
    private Integer page = 0;
    private Integer size = 20;
    private String direction = "ASC";
    private String sortBy = "id";

    public int getPage() {
        return page != null && page >= 0 ? page : 0;
    }

    public int getSize() {
        return size != null && size > 0 ? size : 20;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "id";
    }

    public Sort.Direction getDirection() {
        try {
            return Sort.Direction.valueOf(direction.toUpperCase());
        } catch (Exception e) {
            return Sort.Direction.ASC;
        }
    }

    public Pageable toPageable() {
        return PageRequest.of(getPage(), getSize(), Sort.by(getDirection(), getSortBy())
        );
    }
}
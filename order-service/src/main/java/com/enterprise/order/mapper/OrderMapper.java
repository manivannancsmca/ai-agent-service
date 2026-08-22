package com.enterprise.order.mapper;

import com.enterprise.order.dto.OrderDto;
import com.enterprise.order.entity.OrderEntity;
import com.enterprise.order.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity entity) {
        List<OrderDto.OrderItemDto> itemDtos = entity.getItems().stream()
                .map(this::toItemDto)
                .toList();

        return new OrderDto(
                entity.getOrderId(),
                entity.getUserId(),
                itemDtos,
                entity.getTotalAmount(),
                entity.getStatus().name(),
                entity.getShippingAddress(),
                entity.getCreatedAt(),
                entity.getEstimatedDelivery(),
                entity.getCancellationReason()
        );
    }

    public OrderDto.OrderItemDto toItemDto(OrderItemEntity entity) {
        return new OrderDto.OrderItemDto(
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal()
        );
    }
}
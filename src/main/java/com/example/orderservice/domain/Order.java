package com.example.orderservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Доменный объект «Заказ».
 * <p>
 * Содержит бизнес-данные о заказе: идентификатор, описание, статус,
 * идентификатор пользователя-владельца и дату создания.
 * Поддерживает изменение статуса заказа.
 * </p>
 * <p>
 * Поле {@code createdAt} устанавливается извне (например, при сохранении в БД),
 * поэтому оно помечено как {@code @Setter}. Остальные поля неизменяемы после создания.
 * </p>
 */
@Getter
public class Order {
    private final UUID id;
    private String description;
    private Status status;
    private final UUID userId;
    @Setter
    private LocalDateTime createdAt;

    public Order(UUID id, String description, Status status, UUID userId) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}

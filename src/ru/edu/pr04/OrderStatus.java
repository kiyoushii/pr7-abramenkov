package ru.edu.pr04;

/** Состояние заказа: пока DRAFT, состав можно менять; после CONFIRMED заказ неизменяем. */
public enum OrderStatus {
    DRAFT,
    CONFIRMED
}

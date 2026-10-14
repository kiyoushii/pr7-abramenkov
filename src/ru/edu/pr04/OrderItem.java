package ru.edu.pr04;

/**
 * Позиция заказа: товар и количество.
 * Правило количества (вариант 1: максимум 10) задано ровно здесь, в константах.
 */
public class OrderItem {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 10; // вариант 1; по базовому заданию было 100

    private final Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Ошибка: товар не задан");
        }
        checkQuantity(quantity);
        this.product = product;
        this.quantity = quantity;
    }

    /** Единственное место проверки диапазона количества. */
    public static void checkQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Ошибка: количество позиции должно быть от "
                    + MIN_QUANTITY + " до " + MAX_QUANTITY + ", получено " + quantity);
        }
    }

    /** Объединение повторной позиции: итоговое количество тоже обязано уложиться в диапазон. */
    public void increase(int delta) {
        if (delta < MIN_QUANTITY) {
            throw new IllegalArgumentException("Ошибка: прирост количества должен быть не меньше "
                    + MIN_QUANTITY + ", получено " + delta);
        }
        // long исключает переполнение int при огромном delta
        long merged = (long) quantity + delta;
        if (merged > MAX_QUANTITY) {
            throw new IllegalArgumentException("Ошибка: после объединения количество товара "
                    + product.getId() + " станет " + merged + ", максимум " + MAX_QUANTITY);
        }
        quantity = (int) merged;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }
}

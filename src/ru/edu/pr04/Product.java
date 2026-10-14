package ru.edu.pr04;

/** Товар. Инварианты: id и наименование непустые, цена строго больше нуля. Объект неизменяем. */
public class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: id товара не задан");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: наименование товара не задано");
        }
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            throw new IllegalArgumentException("Ошибка: цена должна быть больше 0, получено " + price);
        }
        this.id = id.trim();
        this.name = name.trim();
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

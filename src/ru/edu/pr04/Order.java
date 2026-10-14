package ru.edu.pr04;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заказ сотрудника. Отдельного поля total нет: сумма всегда считается из текущего состава.
 * После confirm() любые изменения запрещены.
 */
public class Order {
    private final Employee employee;
    private final List<OrderItem> items = new ArrayList<OrderItem>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Ошибка: сотрудник не задан");
        }
        this.employee = employee;
    }

    public void addProduct(Product product, int quantity) {
        requireDraft();
        if (product == null) {
            throw new IllegalArgumentException("Ошибка: товар не задан");
        }
        OrderItem existing = find(product.getId());
        if (existing == null) {
            items.add(new OrderItem(product, quantity));
            return;
        }
        if (existing.getProduct().getPrice() != product.getPrice()) {
            throw new IllegalArgumentException("Ошибка: конфликт данных, товар " + product.getId()
                    + " уже в заказе с другой ценой");
        }
        OrderItem.checkQuantity(quantity);
        existing.increase(quantity);
    }

    public void removeProduct(String productId) {
        requireDraft();
        OrderItem existing = (productId == null) ? null : find(productId.trim());
        if (existing == null) {
            throw new IllegalArgumentException("Ошибка: товар " + productId + " в заказе не найден");
        }
        items.remove(existing);
    }

    public double total() {
        double sum = 0;
        for (int i = 0; i < items.size(); i++) {
            sum += items.get(i).getLineTotal();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public void confirm() {
        requireDraft();
        if (items.isEmpty()) {
            throw new IllegalStateException("Ошибка: нельзя подтвердить пустой заказ");
        }
        status = OrderStatus.CONFIRMED;
    }

    /** Защитное представление: внешний код не может изменить внутренний список. */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<OrderItem>(items));
    }

    public Employee getEmployee() {
        return employee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    private OrderItem find(String productId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProduct().getId().equals(productId)) {
                return items.get(i);
            }
        }
        return null;
    }

    private void requireDraft() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Ошибка: заказ подтверждён и не может быть изменён");
        }
    }
}

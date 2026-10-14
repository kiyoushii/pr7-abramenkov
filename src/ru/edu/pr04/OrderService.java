package ru.edu.pr04;

/**
 * Сервисный слой: создаёт заказ, разбирает текстовые команды и координирует сценарий.
 * Формат команд: add/id/наименование/цена/количество, remove/id, confirm.
 */
public class OrderService {

    public Order createOrder(Employee employee) {
        return new Order(employee);
    }

    /** Проверка перед подтверждением; вынесена отдельно, чтобы правило можно было менять в одном месте. */
    public void validateForConfirm(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Ошибка: заказ не задан");
        }
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Ошибка: заказ уже подтверждён");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Ошибка: нельзя подтвердить пустой заказ");
        }
    }

    public void confirm(Order order) {
        validateForConfirm(order);
        order.confirm();
    }

    /** Выполняет одну команду над заказом. Любая ошибка — IllegalArgumentException/IllegalStateException. */
    public void execute(Order order, String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: команда не задана");
        }
        String[] p = command.trim().split("/", -1);
        String name = p[0].trim().toLowerCase();
        if (name.equals("add")) {
            if (p.length != 5) {
                throw new IllegalArgumentException("Ошибка: формат add/id/наименование/цена/количество");
            }
            Product product = new Product(p[1], p[2], parsePrice(p[3]));
            order.addProduct(product, parseQuantity(p[4]));
        } else if (name.equals("remove")) {
            if (p.length != 2) {
                throw new IllegalArgumentException("Ошибка: формат remove/id");
            }
            order.removeProduct(p[1]);
        } else if (name.equals("confirm")) {
            if (p.length != 1) {
                throw new IllegalArgumentException("Ошибка: формат confirm");
            }
            confirm(order);
        } else {
            throw new IllegalArgumentException("Ошибка: неизвестная команда \"" + p[0] + "\"");
        }
    }

    static int parseQuantity(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: количество не задано");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: количество должно быть целым числом, получено \"" + text.trim() + "\"");
        }
    }

    static double parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: цена не задана");
        }
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: цена должна быть числом, получено \"" + text.trim() + "\"");
        }
    }
}

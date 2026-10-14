package ru.edu.pr04;

/** Сотрудник, для которого формируется заказ. Поля неизменяемы. */
public class Employee {
    private final String id;
    private final String fullName;

    public Employee(String id, String fullName) {
        this.id = requireText(id, "id сотрудника");
        this.fullName = requireText(fullName, "ФИО сотрудника");
    }

    private static String requireText(String value, String what) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: " + what + " не задан(о)");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}

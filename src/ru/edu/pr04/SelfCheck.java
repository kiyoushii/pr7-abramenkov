package ru.edu.pr04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/** Точка входа: unit-проверки, data-driven проверки из data/sample.csv, затем интерактивный режим. */
public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        String csv = args.length > 0 ? args[0] : "data/sample.csv";
        System.out.println("=== Unit-проверки ===");
        unitChecks();
        System.out.println("=== Data-driven проверки: " + csv + " ===");
        dataChecks(csv);
        System.out.println("Итого: PASS=" + passed + ", FAIL=" + failed);
        System.out.println(failed == 0 ? "ALL PASS" : "ЕСТЬ ОШИБКИ");
        interactive();
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    private static boolean throwsIae(Runnable r) {
        return throwsOf(r) instanceof IllegalArgumentException;
    }

    private static boolean throwsIse(Runnable r) {
        return throwsOf(r) instanceof IllegalStateException;
    }

    private static RuntimeException throwsOf(Runnable r) {
        try {
            r.run();
            return null;
        } catch (RuntimeException e) {
            return e;
        }
    }

    private static Order newOrder() {
        return new Order(new Employee("E1", "Иванов Иван"));
    }

    private static Product laptop() {
        return new Product("P1", "Ноутбук", 1000.0);
    }

    private static void unitChecks() {
        // --- Product
        check("Product: цена 0.01 допустима", new Product("X", "x", 0.01).getPrice() == 0.01);
        check("Product: цена 0 запрещена", throwsIae(new Runnable() { public void run() { new Product("X", "x", 0); } }));
        check("Product: отрицательная цена запрещена", throwsIae(new Runnable() { public void run() { new Product("X", "x", -5); } }));
        check("Product: NaN запрещён", throwsIae(new Runnable() { public void run() { new Product("X", "x", Double.NaN); } }));
        check("Product: пустой id запрещён", throwsIae(new Runnable() { public void run() { new Product(" ", "x", 1); } }));
        check("Product: null-наименование запрещено", throwsIae(new Runnable() { public void run() { new Product("X", null, 1); } }));

        // --- граничные значения количества (вариант 1: 1..10)
        Order o1 = newOrder();
        o1.addProduct(laptop(), 1);
        check("Граница: количество 1 принимается", o1.getItems().get(0).getQuantity() == 1);
        Order o10 = newOrder();
        o10.addProduct(laptop(), 10);
        check("Граница: количество 10 принимается", o10.getItems().get(0).getQuantity() == 10);
        check("Граница: количество 11 отклоняется", throwsIae(new Runnable() { public void run() { newOrder().addProduct(laptop(), 11); } }));
        check("Граница: количество 0 отклоняется", throwsIae(new Runnable() { public void run() { newOrder().addProduct(laptop(), 0); } }));
        check("Граница: отрицательное количество отклоняется", throwsIae(new Runnable() { public void run() { newOrder().addProduct(laptop(), -3); } }));
        check("Граница: Integer.MAX_VALUE отклоняется", throwsIae(new Runnable() { public void run() { newOrder().addProduct(laptop(), Integer.MAX_VALUE); } }));
        check("Неудачное добавление не меняет заказ", failedAddKeepsOrder());

        // --- объединение
        Order m = newOrder();
        m.addProduct(laptop(), 4);
        m.addProduct(laptop(), 6);
        check("Объединение: 4 + 6 = 10 одной строкой", m.getItems().size() == 1 && m.getItems().get(0).getQuantity() == 10);
        final Order m2 = newOrder();
        m2.addProduct(laptop(), 6);
        check("Объединение: 6 + 5 = 11 отклоняется", throwsIae(new Runnable() { public void run() { m2.addProduct(laptop(), 5); } }));
        check("Объединение: после отказа количество осталось 6", m2.getItems().get(0).getQuantity() == 6);
        check("Объединение: тот же id с другой ценой — конфликт", throwsIae(new Runnable() {
            public void run() { m2.addProduct(new Product("P1", "Ноутбук", 999.0), 1); } }));
        Order two = newOrder();
        two.addProduct(laptop(), 1);
        two.addProduct(new Product("P2", "Мышь", 50.0), 2);
        check("Разные товары — разные строки", two.getItems().size() == 2);

        // --- удаление
        two.removeProduct("P1");
        check("Удаление по id убирает строку", two.getItems().size() == 1 && two.getItems().get(0).getProduct().getId().equals("P2"));
        check("Удаление отсутствующего товара — ошибка", throwsIae(new Runnable() { public void run() { newOrder().removeProduct("NOPE"); } }));
        check("Удаление null-id — контролируемая ошибка", throwsIae(new Runnable() { public void run() { newOrder().removeProduct(null); } }));

        // --- итог
        Order t = newOrder();
        t.addProduct(laptop(), 2);
        t.addProduct(new Product("P2", "Мышь", 49.99), 3);
        check("total: 2*1000 + 3*49.99 = 2149.97", t.total() == 2149.97);
        t.removeProduct("P2");
        check("total пересчитывается после удаления: 2000.00", t.total() == 2000.0);
        check("total пустого заказа = 0", newOrder().total() == 0.0);

        // --- подтверждение
        Order c = newOrder();
        c.addProduct(laptop(), 1);
        c.confirm();
        check("confirm: статус CONFIRMED", c.getStatus() == OrderStatus.CONFIRMED);
        final Order cc = c;
        check("После confirm add запрещён", throwsIse(new Runnable() { public void run() { cc.addProduct(laptop(), 1); } }));
        check("После confirm remove запрещён", throwsIse(new Runnable() { public void run() { cc.removeProduct("P1"); } }));
        check("Повторный confirm запрещён", throwsIse(new Runnable() { public void run() { cc.confirm(); } }));
        check("Подтверждённый заказ сохранил состав и сумму", c.getItems().size() == 1 && c.total() == 1000.0);
        check("Пустой заказ подтвердить нельзя", throwsIse(new Runnable() { public void run() { newOrder().confirm(); } }));

        // --- защита коллекции (фрагмент из задания)
        final Order g = newOrder();
        g.addProduct(laptop(), 1);
        check("getItems: список только для чтения", throwsOf(new Runnable() {
            public void run() { g.getItems().clear(); } }) instanceof UnsupportedOperationException);
        check("getItems: внешний код не меняет заказ", g.getItems().size() == 1);

        // --- сервис и разбор команд
        OrderService s = new OrderService();
        Order so = s.createOrder(new Employee("E2", "Петров Пётр"));
        s.execute(so, "add/P1/Ноутбук/1000/2");
        s.execute(so, "add/P1/Ноутбук/1000.00/3");
        check("Сервис: add объединяет строки (2+3=5)", so.getItems().size() == 1 && so.getItems().get(0).getQuantity() == 5);
        final Order fso = so;
        final OrderService fs = s;
        check("Сервис: количество «abc» — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "add/P2/Мышь/50/abc"); } }));
        check("Сервис: пустое количество — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "add/P2/Мышь/50/"); } }));
        check("Сервис: дробное количество — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "add/P2/Мышь/50/2.5"); } }));
        check("Сервис: цена «abc» — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "add/P2/Мышь/abc/1"); } }));
        check("Сервис: неизвестная команда — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "buy/P1"); } }));
        check("Сервис: пустая команда — ошибка", throwsIae(new Runnable() { public void run() { fs.execute(fso, "  "); } }));
        s.execute(so, "confirm");
        check("Сервис: confirm переводит заказ в CONFIRMED", so.getStatus() == OrderStatus.CONFIRMED);
        check("Employee: пустое ФИО запрещено", throwsIae(new Runnable() { public void run() { new Employee("E", " "); } }));
        check("Order: null-сотрудник запрещён", throwsIae(new Runnable() { public void run() { new Order(null); } }));
    }

    private static boolean failedAddKeepsOrder() {
        Order o = newOrder();
        o.addProduct(laptop(), 2);
        try {
            o.addProduct(new Product("P2", "Мышь", 10), 11);
        } catch (IllegalArgumentException e) {
            return o.getItems().size() == 1 && o.total() == 2000.0;
        }
        return false;
    }

    /** Формат строки: команды через «|» ; ожидание OK:items=N,total=X или ERROR:фрагмент сообщения. */
    private static void dataChecks(String path) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            check("Файл " + path + " доступен", false);
            return;
        }
        OrderService service = new OrderService();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int cut = line.lastIndexOf(';');
            if (cut < 0) {
                check("Строка " + (i + 1) + ": неверный формат строки данных", false);
                continue;
            }
            String commands = line.substring(0, cut);
            String expected = line.substring(cut + 1).trim();
            check("Строка " + (i + 1) + ": " + commands + " -> " + expected, matches(service, commands, expected));
        }
    }

    private static boolean matches(OrderService service, String commands, String expected) {
        Order order = service.createOrder(new Employee("E0", "Тест Тестович"));
        String error = null;
        String[] list = commands.split("\\|", -1);
        for (int i = 0; i < list.length; i++) {
            try {
                service.execute(order, list[i]);
            } catch (IllegalArgumentException e) {
                error = e.getMessage();
                break;
            } catch (IllegalStateException e) {
                error = e.getMessage();
                break;
            }
        }
        if (expected.startsWith("ERROR:")) {
            return error != null && error.contains(expected.substring(6));
        }
        if (error != null) {
            return false;
        }
        String actual = String.format(java.util.Locale.ROOT, "OK:items=%d,total=%.2f", order.getItems().size(), order.total());
        return actual.equals(expected);
    }

    private static void interactive() throws IOException {
        System.out.println("=== Интерактивный режим ===");
        System.out.println("Команды: add/id/наименование/цена/количество, remove/id, confirm, show, exit");
        OrderService service = new OrderService();
        Order order = service.createOrder(new Employee("E1", "Сотрудник"));
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }
            try {
                if (line.equalsIgnoreCase("show")) {
                    show(order);
                } else {
                    service.execute(order, line);
                    System.out.println("OK");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void show(Order order) {
        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            OrderItem it = items.get(i);
            System.out.println(it.getProduct().getId() + " " + it.getProduct().getName() + " x" + it.getQuantity()
                    + " = " + String.format(java.util.Locale.ROOT, "%.2f", it.getLineTotal()));
        }
        System.out.println("Статус: " + order.getStatus() + ", итого: "
                + String.format(java.util.Locale.ROOT, "%.2f", order.total()));
    }
}

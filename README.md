# Практическая работа №7. Корпоративный заказ оборудования

Студент: Абраменков Андрей, группа ЭФБО-12-25, вариант 1 (максимальное количество позиции — 10).

Классы (пакет `ru.edu.pr04`): `Product`, `Employee`, `OrderItem`, `OrderStatus`, `Order`, `OrderService`, `SelfCheck`.

Запуск: `run.bat` (Windows) или `sh run.sh`. Компиляция вручную:
`javac -encoding UTF-8 -d out src/ru/edu/pr04/*.java`, затем `java -Dfile.encoding=UTF-8 -cp out ru.edu.pr04.SelfCheck`.

SelfCheck выполняет 64 проверки (unit и строки `data/sample.csv`), затем переходит в интерактивный режим.
Команды: `add/id/наименование/цена/количество`, `remove/id`, `confirm`, `show`, `exit`.

Диаграммы: `diagrams/activity.png`, `diagrams/class.png` (исходники `.dot`). Решения: `DECISIONS.md`.

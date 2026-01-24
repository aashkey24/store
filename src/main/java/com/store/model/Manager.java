package com.store.model;

public class Manager extends User {

    // У менеджера есть своя зона ответственности (Сектор), например "Electronics"
    private String sector;

    // Конструктор теперь принимает 6 параметров!
    // (5 от родителя User + 1 свой собственный)
    public Manager(String username, String password, String fullName, String phone, double salary, String sector) {

        // super(...) — это передача данных в родительский класс User
        // Важен порядок: login, pass, name, phone, salary
        super(username, password, fullName, phone, salary);

        this.sector = sector;
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    // Это чтобы в списке (ListView) менеджер выглядел красиво
    @Override
    public String toString() {
        return getFullName() + " (Manager: " + sector + ")";
    }

}
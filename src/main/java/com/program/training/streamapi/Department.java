package com.program.training.streamapi;

import java.util.List;

public class Department {
    private final String name;
    private final List<Employee> employees;

    public Department(String name, List<Employee> employees) {
        this.name = name;
        this.employees = employees;
    }

    public String getName() {
        return name;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    @Override
    public String toString() {
        return "Department{name='" + name + "', employeesCount=" + employees.size() + "}";
    }
}

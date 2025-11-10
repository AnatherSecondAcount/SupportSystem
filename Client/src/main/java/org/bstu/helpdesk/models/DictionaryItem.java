package org.bstu.helpdesk.models;

public class DictionaryItem {
    private final int id;
    private final String name;

    public DictionaryItem(int id, String name) { this.id = id; this.name = name; }
    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; } // Это важно, чтобы ComboBox показывал имя!
}

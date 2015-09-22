package com.ahmadsiavashi.list;

import com.orm.SugarRecord;

import java.io.Serializable;

enum Priority {
    VERY_LOW, LOW, NORMAL, HIGH, CRITICAL;

    public static Priority getLowest() {
        return Priority.VERY_LOW;
    }

    public static Priority getHighest() {
        return Priority.CRITICAL;
    }

    public static Priority getNormal() {
        return Priority.NORMAL;
    }

    public static Priority getHigherThan(Priority priority) {
        if (priority == Priority.getHighest())
            return priority;
        return Priority.values()[priority.ordinal() + 1];
    }

    public static Priority getLowerThan(Priority priority) {
        if (priority == Priority.getLowest())
            return priority;
        return Priority.values()[priority.ordinal() - 1];
    }
}

enum Level {
    Q1, Q2, Q3, Q4;

    public static Level getLowest() {
        return Level.Q1;
    }

    public static Level getHighest() {
        return Level.Q4;
    }

    public static Level getNext(Level level) {
        if (level == Level.getHighest())
            return level;
        return Level.values()[level.ordinal() + 1];
    }

    public static Level getPrev(Level level) {
        if (level == Level.getLowest())
            return level;
        return Level.values()[level.ordinal() - 1];
    }
}

/**
 * Created by Navarch on 8/21/2015.
 */

public class Item extends SugarRecord<Item> implements Comparable<Item>, Serializable {
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private String title = "";
    private String description = "";
    private Priority priority;
    private Level level = Level.Q1;
    private String date;

    public Item() {

    }

    public Item(String title, String description, Priority priority, String date) {
        this.setTitle(title);
        this.setDescription(description);
        this.setPriority(priority);
        this.setDate(date);
    }

    public Item(String title, String description, int priority, String date) {
        this.setTitle(title);
        this.setDescription(description);
        this.setPriority(Priority.values()[priority]);
        this.setDate(date);
    }

    @Override
    public int compareTo(Item another) {
        return -(this.getPriority().ordinal() - another.getPriority().ordinal());
    }

    public void toHighestPriority() {
        this.setPriority(Priority.getHighest());
    }

    public void toLowestPriority() {
        this.setPriority(Priority.getLowest());
    }

    public void toLowerPriority() {
        this.setPriority(Priority.getLowerThan(this.getPriority()));
    }

    public void toHigherPriority() {
        this.setPriority(Priority.getHigherThan(this.getPriority()));
    }

    public void toHighestLevel() {
        this.setLevel(Level.getHighest());
    }

    public void toLowestLevel() {
        this.setLevel(Level.getLowest());
    }

    public boolean toLowerLevel() {
        Level newLevel = Level.getPrev(this.getLevel());
        if (getLevel() == newLevel)
            return false;
        setLevel(newLevel);
        return true;
    }

    public boolean toHigherLevel() {
        Level newLevel = Level.getNext(this.getLevel());
        if (getLevel() == newLevel)
            return false;
        setLevel(newLevel);
        return true;
    }

    public boolean setLevelToComplete() {
        Level newLevel = Level.getHighest();
        if (getLevel() == newLevel)
            return false;
        setLevel(newLevel);
        return true;
    }

    public void setPriority(int priority) {
        this.priority = Priority.values()[priority];
    }

    public void increasePriority() {
        this.setPriority(Priority.getHigherThan(this.getPriority()));
    }

    public void decreasePriority() {
        this.setPriority(Priority.getLowerThan(this.getPriority()));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

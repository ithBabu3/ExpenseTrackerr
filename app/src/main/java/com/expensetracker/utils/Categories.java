package com.expensetracker.utils;

import java.util.Arrays;
import java.util.List;

public class Categories {
    public static final List<String> ALL = Arrays.asList(
        "Food & Dining",
        "Transport",
        "Shopping",
        "Entertainment",
        "Health & Medical",
        "Utilities",
        "Rent / Housing",
        "Education",
        "Travel",
        "Personal Care",
        "Gifts & Donations",
        "Other"
    );

    public static final String[] ICONS = {
        "🍔", "🚗", "🛍️", "🎬", "💊", "💡", "🏠", "📚", "✈️", "💅", "🎁", "📦"
    };

    public static String getIcon(String category) {
        int index = ALL.indexOf(category);
        if (index >= 0 && index < ICONS.length) return ICONS[index];
        return "💰";
    }
}

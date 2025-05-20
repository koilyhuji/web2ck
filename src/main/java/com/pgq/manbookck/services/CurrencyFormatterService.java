package com.pgq.manbookck.services;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class CurrencyFormatterService {

    private final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");

    // Option 1: Using NumberFormat.getCurrencyInstance (generally preferred for locales)
    public String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return ""; // Or "0 ₫" or handle as appropriate
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE);
        // By default, NumberFormat might add decimal places for VND if not configured specifically
        // for whole numbers. Let's ensure no fractional digits.
        currencyFormatter.setMaximumFractionDigits(0);
        currencyFormatter.setMinimumFractionDigits(0);
        return currencyFormatter.format(amount); // Example output: "50.000 ₫"
    }

    // Option 2: Using DecimalFormat for more explicit control (if NumberFormat doesn't behave as expected)
    public String formatVndWithDecimalFormat(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        // Pattern for Vietnamese Dong: group by thousands with '.', currency symbol at the end.
        // Using "#,##0" ensures that 0 is displayed as "0" and not empty.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(VIETNAMESE_LOCALE);
        // symbols.setGroupingSeparator('.'); // Default for vi_VN should be '.'
        // symbols.setCurrencySymbol("₫");   // Default for vi_VN should be '₫'
        DecimalFormat vndFormatter = new DecimalFormat("#,##0 '₫'", symbols);
        return vndFormatter.format(amount); // Example output: "50.000 ₫"
    }

}

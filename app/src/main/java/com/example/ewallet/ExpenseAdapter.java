package com.example.ewallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ewallet.Expense;

import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<Expense> {
    private Context context;
    private List<Expense> expensesList;
    private String currencySymbol;


    //CUSTOM expense adapter za prikaz potrosnje u listi
                        //context - Za pristup sistemskim servisima i resource fajlovima
    public ExpenseAdapter(Context context, List<Expense> expensesList, String currencySymbol) {
        super(context, 0, expensesList);
        this.context = context;
        this.expensesList = expensesList;
        this.currencySymbol = currencySymbol;
    }

    @Override                                           //parent - parent view u koji ce itemi biti dodavani
    public View getView(int position, View convertView, ViewGroup parent) {
        Expense expense = expensesList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_list_item, parent, false);
        }

        TextView categoryView = convertView.findViewById(R.id.category);
        TextView amountView = convertView.findViewById(R.id.amount);
        TextView commentView = convertView.findViewById(R.id.comment);
        TextView dateView = convertView.findViewById(R.id.date);

        if(expense != null) {
            if (expense.getCategory() != null) {
                categoryView.setText(expense.getCategory());
            }

            if (expense.getAmount() != null) {
                amountView.setText(expense.getAmount().toString() + " " + currencySymbol);
            }

            if (expense.getComment() != null) {
                commentView.setText(expense.getComment());
            }

            if (expense.getDate() != null) {
                dateView.setText(expense.getDate());
            }
        }


        return convertView;
    }
}

package com.example.ewallet;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertAllExpenses(Expense... expenses);

    @Insert
    void insertExpense(Expense expense);

    @Query("UPDATE Expense SET category = :category, amount = :amount, comment = :comment, date = :date WHERE expense_id = :id")
    void updateExpenseOverID(String category, String amount, String comment, String date, int id);


    @Delete
    void deleteExpense(Expense expense);

    @Query("SELECT * FROM Expense")
    List<Expense> getAllExpenses();
}

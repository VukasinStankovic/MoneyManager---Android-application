package com.example.ewallet;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


//SINGLETON Pattern - proveriti
@Database(entities = {Expense.class}, version = 1)
public abstract class ExpenseDatabase extends RoomDatabase {

    //Vraca instancu ExpenseDao interfejsa koji sluzi za obavalajnje CRUD operacija nad bazom
    public abstract ExpenseDao getDao();

    //Singleton instanca baze
    public static ExpenseDatabase INSTANCE;

    //Metod koji osigurava da je samo jedna istanca baze kreirana
    public static ExpenseDatabase getInstance(Context context){
        if(INSTANCE == null){
            //Keyword osigurava da je samo jedna instanca kreirana
            synchronized (ExpenseDatabase.class) {
                //Ako nijedna instanca nije kreirana kreiramo je
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ExpenseDatabase.class, "ExpenseDatabase").build();
                    }
                }
            }
        return INSTANCE;
    }
}

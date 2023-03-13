package com.example.ewallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FragmentHome extends Fragment implements View.OnClickListener {
    private PieChart pieChart;
    private String currency;
    private OnFragmentHomeInteractionListener mListener;
    private ArrayList<String> categories;
    private AlertDialog dialog;
    private AlertDialog editDialog;
    private AlertDialog.Builder builder ;
    private EditText inputExpenseAmount;
    private EditText inputEditExpenseAmount;
    private EditText inputExpenseComment;
    private EditText inputEditExpenseComment;
    private Spinner spinnerExpenseCategory;
    private Spinner spinnerEditExpenseCategory;
    private DatePicker expenseDatePicker;
    private DatePicker expenseEditDatePicker;
    private ListView listView;
    private List<Expense> expensesList;
    private ExpenseAdapter expenseAdapter;
    private Button buttonEditExpenseItem;
    private Button buttonDeleteExpenseItem;
    private View popup;
    private int liPosition;
    private PopupWindow popupWindow;
    private int itemID;
    private Float itemAmount;
    private String itemCategory;
    private String itemComment;
    private String itemDate;
    private Expense lvExpenseItem;


    private static final String ADD_EXPENSE = "addExpense";
    private static final String DELETE_EXPENSE = "deleteExpense";
    private static final String UPDATE_EXPENSE = "updateExpense";
    private static final String GET_ALL_EXPENSES = "getAllExpenses";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        expensesList = new ArrayList<>();


        Button buttonAddExpense = view.findViewById(R.id.buttonAddExpense);
        buttonAddExpense.setOnClickListener(this);
        Button buttonAddIncome = view.findViewById(R.id.buttonAddIncome);
        buttonAddIncome.setOnClickListener(this);
        listView = view.findViewById(R.id.listViewShowAllExpenses);
        listView.setOnItemLongClickListener((adapterView, view1, i, l) -> {
            popup = inflater.inflate(R.layout.popup_edit_delete_expense, null);
            //Pozivija itema
            liPosition = i;

            //Izvlacimo vrednosti za odabrani expense, koje kasnije koristimo kako bi popunili podlja u Edit expense dialog-u  da korisnik ne bi morao da unosi vrednoti opet
            lvExpenseItem = expensesList.get(i);
            itemID = lvExpenseItem.getExpense_id();
            itemAmount = lvExpenseItem.getAmount();
            itemCategory = lvExpenseItem.getCategory();
            itemComment = lvExpenseItem.getComment();
            itemDate = lvExpenseItem.getDate();

            //Sirina popup prozora 100dp
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            //Visina popup prozora wrap_content
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            //Kreiramo popup prozor                              true - Da li user moze da interaguje sa popup prozorom
            popupWindow = new PopupWindow(popup, width, height, true);

            //Prikazi pop up prozor iznad selektovanog itema
            popupWindow.showAsDropDown(view1, view1.getWidth() - popupWindow.getWidth(), -(view1.getHeight() + popupWindow.getHeight()));
            buttonEditExpenseItem = popup.findViewById(R.id.buttonEditExpenseItem);
            buttonDeleteExpenseItem = popup.findViewById(R.id.buttonDeleteExpenseItem);
            buttonEditExpenseItem.setOnClickListener(FragmentHome.this);
            buttonDeleteExpenseItem.setOnClickListener(FragmentHome.this);

            //Nijedan drugi listener ili handler nece biti dalje obavesten o ovom dogadjaju, u suprotnom se ce biti obavesteni u zavisnoti od toga kako su implementirani i mogu se izvrsiti dodatne radnje
            return true;
        });

        pieChart = view.findViewById(R.id.fragment_home_piechart);

        //Uzimamo vrednost izabrane valute, iz nosece aktivnosti
        Bundle bundle = getArguments();
        //Ukoliko korisnik ne izabere valutu, podrazumevana vrednost ce biti RSD
        if(bundle == null){
            currency = " RSD";
        }else{
            String symbol = bundle.getString("symbol");
            currency = " " + symbol;
        }

        getAllExpenses();
        getCategories();
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentHomeInteractionListener){
            mListener = (OnFragmentHomeInteractionListener) context;
        }else{
            throw new RuntimeException(context.toString() + "mora da implementira odgovarajuci interfejs");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnFragmentHomeInteractionListener {
        void showPieChart(PieChart sentPieChart, PieData data);
    }

    public void showExpenseDialog(){
        builder = new AlertDialog.Builder(getActivity());
        View popupAddExpense = getLayoutInflater().inflate(R.layout.popup_add_expense, null);

        //EditText Amount
        inputExpenseAmount = popupAddExpense.findViewById(R.id.inputExpenseAmount);
        //Ogranicenje da korisnik mora da unese float vrednost
        inputExpenseAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //EditText Comment
        inputExpenseComment = popupAddExpense.findViewById(R.id.inputExpenseComment);

        //Spinner
        spinnerExpenseCategory = popupAddExpense.findViewById(R.id.spinnerExpenseCategory);
            //Popuni spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_list_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(adapter);

        //DatePicker
        expenseDatePicker = popupAddExpense.findViewById(R.id.expenseDatePicker);


        //Button Add
        Button buttonAdd = popupAddExpense.findViewById(R.id.buttonAdd);
        buttonAdd.setBackground(null);
        buttonAdd.setOnClickListener(this);

        //Button Cancel
        Button buttonCancel = popupAddExpense.findViewById(R.id.buttonCancel);
        buttonCancel.setBackground(null);
        buttonCancel.setOnClickListener(this);



        builder.setView(popupAddExpense);
        dialog = builder.create();
        dialog.show();
    }

    public void showEditExpenseDialog(){

        builder = new AlertDialog.Builder(getActivity());
        View popupEditExpense = getLayoutInflater().inflate(R.layout.popup_edit_expense, null);

        //EditText Amount
        inputEditExpenseAmount = popupEditExpense.findViewById(R.id.inputEditExpenseAmount);
            //Popuni Amount sa vec postojecom kolicinom novca koju korisnik zeli da edituje
        inputEditExpenseAmount.setText(itemAmount.toString());
            //Ogranicenje da korisnik mora da unese float vrednost
        inputEditExpenseAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //EditText Comment
        inputEditExpenseComment = popupEditExpense.findViewById(R.id.inputEditExpenseComment);
            //Popuni Comment sa vec postojecim komentarom kojeg korisnik zeli da edituje
        inputEditExpenseComment.setText(itemComment);

        //Spinner
        spinnerEditExpenseCategory = popupEditExpense.findViewById(R.id.spinnerEditExpenseCategory);
         //Popuni Spinner sa vec postojecom kategorijom koju korisnik zeli da edituje
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_list_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditExpenseCategory.setAdapter(adapter);
        int position = adapter.getPosition(itemCategory);
        spinnerEditExpenseCategory.setSelection(position);

        //DatePicker
        expenseEditDatePicker = popupEditExpense.findViewById(R.id.expenseEditDatePicker);
         //Popuni DatePicker sa vec postojecim datumom kojeg korisnik zeli da edituje
        String expenseDate = itemDate;
        String[] dateParts = expenseDate.split("\\.");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int year = Integer.parseInt(dateParts[2]);
        expenseEditDatePicker.updateDate(year, month, day);


        //Button Edit
        Button buttonPopupEditEdit = popupEditExpense.findViewById(R.id.buttonEditExpenseEdit);
        buttonPopupEditEdit.setBackground(null);
        buttonPopupEditEdit.setOnClickListener(this);

        //Button CancelEdit
        Button buttonPopupEditCancel = popupEditExpense.findViewById(R.id.buttonEditExpenseCancel);
        buttonPopupEditCancel.setBackground(null);
        buttonPopupEditCancel.setOnClickListener(this);



        builder.setView(popupEditExpense);
        editDialog = builder.create();
        editDialog.show();
    }

    public void showIncomeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View popupAddIncome = getLayoutInflater().inflate(R.layout.popup_add_income, null);
        builder.setView(popupAddIncome);
        builder.create().show();
    }

    public void createPieChart(){
        //setupPieChart
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.setEntryLabelTextSize(9);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Spending by Category");
        pieChart.setCenterTextSize(18);
        pieChart.setNoDataText("There were no expenses today");
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);

        //Todo: Prodji kroz exepnseList i prikazi podatke u chartu
        ArrayList<PieEntry> entries = new ArrayList<>();
        //Ako korisnik doda 2 ili vise Expense pod istom kategorijom, sabiramo iznose prikazujemo kao jedan expense pod istom bojom
        HashMap<String, Float> categoryMap = new HashMap<>();

        for (Expense expense : expensesList) {
            String category = expense.getCategory();
            float amount = expense.getAmount();
            if (categoryMap.containsKey(category)) {
                categoryMap.put(category, categoryMap.get(category) + amount);
            } else {
                categoryMap.put(category, amount);
            }
        }

        for (Map.Entry<String, Float> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }





        ArrayList<Integer> colors = new ArrayList<>();
        for(int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }

        for(int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Category");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);


        //Customizujom formater da umesto default float vrednosti prikazuje  int vrednosti, a nakon vrednosti ce biti ispisana izabrana valuta
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + currency;
            }
        });
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        mListener.showPieChart(pieChart, data);
    }

    public void getCategories(){
        //Uzimamo kategorije iz SharedPrefs
        SharedPreferences prefs = requireActivity().getSharedPreferences(FragmentCategory.getSharedPrefPrefix(), 0);
                                                                                         //U slucaju da je preft pod kljucem prazan, default value ce biti prazan HesSet
        Set<String> set = prefs.getStringSet(FragmentCategory.getSharedPrefKeyCategory(), new HashSet<>());
        categories = new ArrayList<>(set);
    }


    private String[] checkExpenseInputAndAdd(){
        if(inputExpenseAmount.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Polje Amount ne sme biti prazno", Toast.LENGTH_SHORT).show();
            return  null;
        }else if(inputExpenseComment.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Polje Comment ne sme biti prazno", Toast.LENGTH_SHORT).show();
            return  null;
        }else if(spinnerExpenseCategory.getCount() == 0){
            Toast.makeText(getContext(), "Morate dodati minimum jednu kategoriju", Toast.LENGTH_SHORT).show();
            return  null;
        }else{
            //Uzimanje vrednosti iz forme
            String inputExpenseAmountString = inputExpenseAmount.getText().toString();
            float inputExpenseAmountFloat = Float.parseFloat(inputExpenseAmountString);
            String commentInput = inputExpenseComment.getText().toString();
            String categorytInput = spinnerExpenseCategory.getSelectedItem().toString();
            String expenseDate = String.format("%02d.%02d.%4d", expenseDatePicker.getDayOfMonth(), expenseDatePicker.getMonth() + 1, expenseDatePicker.getYear());
            Toast.makeText(getContext(), "Uspesno ste dodali novu potrosnju", Toast.LENGTH_SHORT).show();

            return new String[]{Float.toString(inputExpenseAmountFloat), commentInput, categorytInput, expenseDate};
        }
    }

    private String[] checkEditedExpenseInputAndAdd(){
        if(inputEditExpenseAmount.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Polje Amount ne sme biti prazno", Toast.LENGTH_SHORT).show();
            return  null;
        }else if(inputEditExpenseComment.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Polje Comment ne sme biti prazno", Toast.LENGTH_SHORT).show();
            return  null;
        }else if(spinnerEditExpenseCategory.getSelectedItem() == null){
            Toast.makeText(getContext(), "Morate izabrati kategoriju", Toast.LENGTH_SHORT).show();
            return  null;
        }else{
            //Uzimanje vrednosti iz forme
            String inputEditedExpenseAmountString = inputEditExpenseAmount.getText().toString();
            Float inputEditedExpenseAmountFloat = Float.parseFloat(inputEditedExpenseAmountString);
            String editedCommentInput = inputEditExpenseComment.getText().toString();
            String editedCategoryInput = spinnerEditExpenseCategory.getSelectedItem().toString();
            String editedExpenseDate = String.format("%02d.%02d.%4d", expenseEditDatePicker.getDayOfMonth(), expenseEditDatePicker.getMonth() + 1, expenseEditDatePicker.getYear());
            Toast.makeText(getContext(), "Uspesno ste editovali expense", Toast.LENGTH_SHORT).show();

            return new String[]{inputEditedExpenseAmountFloat.toString(), editedCommentInput, editedCategoryInput, editedExpenseDate};
        }
    }

    private Expense insertSingleExpenseToList(String[] expenseData){
        if (expenseData != null) {
            // process the expenseData here
            Float inputExpenseAmountFloat = Float.parseFloat(expenseData[0]);
            String commentInput = expenseData[1];
            String categorytInput = expenseData[2];
            String expenseDate = expenseData[3];
            Expense expense = new Expense(categorytInput,inputExpenseAmountFloat, commentInput, expenseDate);

            return expense;
        }
        return null;
    }

    private Expense insertSingleEditedExpenseToList(String[] editedExpenseData){
        if (editedExpenseData != null) {
            // process the editedExpenseData here
            Float inputEditedExpenseAmountFloat = Float.parseFloat(editedExpenseData[0]);
            String editedCommentInput = editedExpenseData[1];
            String editedCategoryInput = editedExpenseData[2];
            String editedExpenseDate = editedExpenseData[3];
            Expense editedExpense = new Expense(editedCategoryInput,inputEditedExpenseAmountFloat, editedCommentInput, editedExpenseDate);

            return editedExpense;
        }
        return null;
    }

    private void insertSingleExpense(String[] expenseData){
        if (expenseData != null) {
            // process the expenseData here
            Float inputExpenseAmountFloat = Float.parseFloat(expenseData[0]);
            String commentInput = expenseData[1];
            String categorytInput = expenseData[2];
            String expenseDate = expenseData[3];
            Expense expense = new Expense(categorytInput,inputExpenseAmountFloat, commentInput, expenseDate);
            InsertAsyncTask insertAsyncTask = new InsertAsyncTask(ADD_EXPENSE);
            insertAsyncTask.execute(expense);
        }
    }
    //DO OVOGA

    private void getAllExpenses(){
        InsertAsyncTask insertAsyncTask = new InsertAsyncTask(GET_ALL_EXPENSES);
        insertAsyncTask.execute();
    }


    public void deleteExpense(Expense expense){
        InsertAsyncTask insertAsyncTask = new InsertAsyncTask(DELETE_EXPENSE);
        insertAsyncTask.execute(expense);
    }

    public void updateExpenseOverID(Expense expense, int id){
        InsertAsyncTask insertAsyncTask = new InsertAsyncTask(UPDATE_EXPENSE);
        insertAsyncTask.execute(expense);
    }



    public void updateExpenseInList(Expense updatedExpense) {
        for (int i = 0; i < expensesList.size(); i++) {
            Expense expense = expensesList.get(i);
            if (expense.getExpense_id() == (updatedExpense.getExpense_id())) {
                expensesList.remove(i);
                expensesList.add(i, updatedExpense);
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.buttonAddExpense:
                showExpenseDialog();
                break;
            case R.id.buttonAddIncome:
                showIncomeDialog();
                break;
            case R.id.buttonAdd:
                String[] expenseData = checkExpenseInputAndAdd();
                if(expenseData != null){
                    insertSingleExpense(expenseData);
                }

                Expense newExpense = insertSingleExpenseToList(expenseData);
                if(newExpense != null) {
                    expensesList.add(newExpense);
                }
                expenseAdapter.notifyDataSetChanged();
                break;
            case R.id.buttonCancel:
                dialog.dismiss();
                break;
            case R.id.buttonEditExpenseItem:
                showEditExpenseDialog();
                popupWindow.dismiss();
                break;
            case R.id.buttonDeleteExpenseItem:
                deleteExpense(lvExpenseItem);
                popupWindow.dismiss();
                Toast.makeText(getContext(), "Uspesno ste izbrisali expense", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonEditExpenseEdit:
                String[] editedExpenseData = checkEditedExpenseInputAndAdd();

                Expense editedExpense = insertSingleEditedExpenseToList(editedExpenseData);
                if(editedExpense != null){
                    updateExpenseOverID(editedExpense, itemID);
                    updateExpenseInList(editedExpense);
                }
                expenseAdapter.notifyDataSetChanged();
                break;
            case R.id.buttonEditExpenseCancel:
                editDialog.dismiss();

                break;
        }
    }

    class InsertAsyncTask extends AsyncTask<Expense, Void, Void>{

        private String action;

        public InsertAsyncTask(String action) {
            this.action = action;
        }

        @Override
        //Ovoj metodi mozemo dodeliti prozivoljan broj parametara. U ovom biramo izmedju Expense ili  ID u zavisnosti od toga sta nam treba
        protected Void doInBackground(Expense... expenses) {
            switch (action) {
                case ADD_EXPENSE:
                    ExpenseDatabase.getInstance(getContext()).getDao().insertExpense(expenses[0]);
                    break;
                case DELETE_EXPENSE:
                    ExpenseDatabase.getInstance(getContext()).getDao().deleteExpense(expenses[0]);
                    break;
                case UPDATE_EXPENSE:
                    ExpenseDatabase.getInstance(getContext()).getDao().updateExpenseOverID(expenses[0].getCategory(), expenses[0].getAmount().toString(), expenses[0].getComment(), expenses[0].getDate(), itemID);
                    break;
                case GET_ALL_EXPENSES:
                        expensesList = ExpenseDatabase.getInstance(getContext()).getDao().getAllExpenses();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(expensesList != null){
                createPieChart();
            }
            switch (action) {
                case DELETE_EXPENSE:
                    expensesList.remove(liPosition);
                    expenseAdapter.notifyDataSetChanged();
                    getAllExpenses();
                    break;
                case GET_ALL_EXPENSES:
                    expenseAdapter = new ExpenseAdapter(getContext(), expensesList, currency);
                    listView.setAdapter(expenseAdapter);
                    break;
                case UPDATE_EXPENSE:
                    getAllExpenses();
                    break;
            }
        }
    }





}


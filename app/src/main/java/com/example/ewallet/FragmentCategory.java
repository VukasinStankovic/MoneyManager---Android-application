package com.example.ewallet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class FragmentCategory extends Fragment implements View.OnClickListener {
    private final static String SHARED_PREF_PREFIX = "FragmentCategory";
    private final static String SHARED_PREF_KEY_CATEGORY = "category";

    private EditText inputCategory;
    private Button addCategory;
    private Button buttonDeleteCategory;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> categories;
    private SharedPreferences prefs;
    private View popup;
    private PopupWindow popupWindow;
    private int liPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);


        inputCategory = view.findViewById(R.id.inputCategory);
        addCategory = view.findViewById(R.id.buttonAddCategory);
        addCategory.setOnClickListener(this);
        listView = view.findViewById(R.id.listViewCategories);
        prefs = getActivity().getSharedPreferences(SHARED_PREF_PREFIX, 0);
        categories = new ArrayList<>(prefs.getStringSet(SHARED_PREF_KEY_CATEGORY, new HashSet<>()));
        adapter = new ArrayAdapter<>(getContext(), R.layout.category_list_item, categories);
        listView.setAdapter(adapter);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                popup = inflater.inflate(R.layout.popup_delete_category, null);
                liPosition = i;

                //Sirina popup prozora 100dp
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                //Visina popup prozora wrap_content
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;

                //Kreiramo popup prozor                              true - Da li user moze da interaguje sa popup prozorom
                popupWindow = new PopupWindow(popup, width, height, true);

                //Prikazi pop up prozor iznad selektovanog itema
                popupWindow.showAsDropDown(view, view.getWidth() - popupWindow.getWidth(), -(view.getHeight() + popupWindow.getHeight()));
                buttonDeleteCategory = popup.findViewById(R.id.buttonDeleteCategory);
                buttonDeleteCategory.setOnClickListener(FragmentCategory.this);

                return true;
            }
        });

        return view;
    }

    public static String getSharedPrefPrefix() {
        return SHARED_PREF_PREFIX;
    }

    public static String getSharedPrefKeyCategory() {
        return SHARED_PREF_KEY_CATEGORY;
    }

    public void saveCategories(){
        Set<String> set = new HashSet<>(categories);
        prefs.edit().putStringSet(SHARED_PREF_KEY_CATEGORY, set).apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveCategories();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonAddCategory:
                String input = inputCategory.getText().toString().trim();
                if(!input.isEmpty()&&!categories.contains(input)) {
                    categories.add(input);
                    inputCategory.setText("");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Kategorija je uspesno dodata", Toast.LENGTH_SHORT).show();
                }else{
                    if (input.isEmpty()){
                        Toast.makeText(getContext(), "Morate uneti ime kategorije", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), "Kategorija vec postoji", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.buttonDeleteCategory:
                categories.remove(liPosition);
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
        }



    }
}

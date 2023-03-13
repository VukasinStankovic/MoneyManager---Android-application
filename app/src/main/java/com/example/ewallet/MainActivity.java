package com.example.ewallet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentHome.OnFragmentHomeInteractionListener, FragmentCurrency.OnSymbolSelectedListener {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private TextView labela_main_activity;
    private FragmentHome fragmentHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();

        //Klasa koja se koristi za otvaranje i zatvaranje drawera                            2 i 3 arg - dve komponente koje ce ova klasa povezati               4 i 5 arg - stringovi koji ce sluziti kao pomoc kako bi ljudi sa posebnim potrebama znali sta se dogadja u aplikaciji
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //Animacija rotiranja hamburger ikonice --> PROVERITI ZA SVAKI SLUCAJ
        actionBarDrawerToggle.syncState();

        //Postavljamo listener koji ce da osluskuje kada se odabere neki item u drawer-u
        navigationView.setNavigationItemSelectedListener(this);


        //Prilikom pokretanja aplikacije ce se prikazati fragment_home, a u slucaju rotacije uredjaja ostace onaj fragment koji je poslednji selektovan
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void initComponents() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        //Umesto default action bara postavljamo toolbar koji smo kreirali u activity_main.xml fajlu
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.nav_view);
        labela_main_activity = findViewById(R.id.labela_main_activity);

    }


    //Metoda koja ce prilikom klika na back dugme zatvoriti drawer u slucaju da je otvoren
    @Override
    public void onBackPressed() {
        //Proveravamo da li je drawer otvoren, ako jeste zatvaramo ga. S obzirom na to da je drawer sa leve stranje stavljamo vrednost START, da je drawer sa desne strane stavili bismo END
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        //Ukoliko drawer nije otvoren zatvoricemo aktivnost kao i sto je podrazumevano
        }else{
            super.onBackPressed();
        }
    }

    //Metoda koja ce uraditi nesto kada izaberemo item iz Drawera
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commit();
                break;
            case R.id.nav_category:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentCategory()).commit();
                break;
            case R.id.nav_currency:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentCurrency()).commit();
                break;
        }



        //Nakon sto izaberemo item (fragment) iz drawera, zelimo da se drawer zatvori
        drawerLayout.closeDrawer(GravityCompat.START);

        //Zelimo da selektujemo item kada kliknemo na na njega
        return true;
    }

    @Override
    public void showPieChart(PieChart sentPieChart, PieData data) {
        sentPieChart.setData(data);
        //Iscrtavanje
        sentPieChart.invalidate();
    }

    @Override
    public void onSymbolSelected(String symbol) {
        Bundle bundle = new Bundle();
        bundle.putString("symbol", symbol);
        FragmentHome fragmentHome = new FragmentHome();
        fragmentHome.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragmentHome).commit();

    }
}


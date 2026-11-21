package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHello;
    private TextView tvStatPatients;
    private TextView tvStatToday;
    private TextView tvStatUpcoming;
    private TextView tvStatConsult;
    private TextView tvFinance;
    private TextView tvNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        PatientStore.seedIfFirstLaunch(this);
        PatientStore.seedDemoV2(this);
        PatientStore.seedDemoV3(this);
        ConsultationStore.seedDemo(this);
        ConsultationStore.seedDemoV3(this);

        tvHello = findViewById(R.id.tvHello);
        tvStatPatients = findViewById(R.id.tvStatPatients);
        tvStatToday = findViewById(R.id.tvStatToday);
        tvStatUpcoming = findViewById(R.id.tvStatUpcoming);
        tvStatConsult = findViewById(R.id.tvStatConsult);
        tvFinance = findViewById(R.id.tvFinance);
        tvNext = findViewById(R.id.tvNext);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvLogout = findViewById(R.id.tvLogout);
        Button btnPatients = findViewById(R.id.btnPatients);
        Button btnAgenda = findViewById(R.id.btnAgenda);
        Button btnNew = findViewById(R.id.btnNew);
        Button btnStats = findViewById(R.id.btnStats);
        Button btnSettings = findViewById(R.id.btnSettings);

        String date = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE).format(new Date());
        if (!date.isEmpty()) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvDate.setText(date);

        btnPatients.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PatientsActivity.class)));
        btnAgenda.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AgendaActivity.class)));
        btnNew.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PatientFormActivity.class)));
        btnStats.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, StatsActivity.class)));
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        tvLogout.setOnClickListener(v -> {
            Intent logout = new Intent(HomeActivity.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String doctor = SettingsStore.getDoctor(this).trim();
        boolean aDejaDr = doctor.toLowerCase().startsWith("dr") || doctor.toLowerCase().startsWith("docteur");
        tvHello.setText(doctor.isEmpty() ? "Bonjour Docteur 👋" : "Bonjour " + (aDejaDr ? "" : "Dr ") + doctor + " 👋");

        List<Patient> patients = PatientStore.load(this);
        int today = 0;
        int upcoming = 0;
        Patient next = null;
        long nextDays = Long.MAX_VALUE;
        for (Patient p : patients) {
            long d = DateUtils.daysFromToday(p.rdv);
            if (d == DateUtils.NO_DATE || d < 0) {
                continue;
            }
            if (d == 0) {
                today++;
            } else {
                upcoming++;
            }
            if (d < nextDays) {
                nextDays = d;
                next = p;
            }
        }
        tvStatPatients.setText(String.valueOf(patients.size()));
        tvStatToday.setText(String.valueOf(today));
        tvStatUpcoming.setText(String.valueOf(upcoming));

        List<Consultation> all = ConsultationStore.loadAll(this);
        tvStatConsult.setText("🩺  " + all.size() + " consultation(s) enregistrée(s)");

        int month = DateUtils.currentMonthKey();
        int revenue = 0;
        for (Consultation c : all) {
            if (c.paye && DateUtils.monthKey(c.date) == month) {
                revenue += c.montant;
            }
        }
        tvFinance.setText("💰  Revenus du mois : " + revenue + " DT\n⏳  Impayés : "
                + ConsultationStore.totalUnpaid(all) + " DT");

        if (next == null) {
            tvNext.setText("📅  Aucun rendez-vous à venir");
        } else {
            tvNext.setText("📅  Prochain rendez-vous : " + next.nom + "\n"
                    + next.rdv + " (" + DateUtils.describe(nextDays) + ")");
        }
    }
}

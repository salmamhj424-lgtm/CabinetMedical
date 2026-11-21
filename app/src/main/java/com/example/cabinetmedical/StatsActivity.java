package com.example.cabinetmedical;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private static final String[] MOIS = {"janv.", "févr.", "mars", "avr.", "mai", "juin",
            "juil.", "août", "sept.", "oct.", "nov.", "déc."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView tvKpi = findViewById(R.id.tvKpi);
        LinearLayout llAges = findViewById(R.id.llAges);
        LinearLayout llMonths = findViewById(R.id.llMonths);
        LinearLayout llRevenue = findViewById(R.id.llRevenue);

        List<Patient> patients = PatientStore.load(this);
        List<Consultation> consultations = ConsultationStore.loadAll(this);

        tvKpi.setText("👥  " + patients.size() + " patients\n"
                + "🩺  " + consultations.size() + " consultations\n"
                + "💰  Encaissé : " + ConsultationStore.totalPaid(consultations) + " DT\n"
                + "⏳  Impayés : " + ConsultationStore.totalUnpaid(consultations) + " DT");

        int[] ages = new int[4];
        for (Patient p : patients) {
            if (p.age < 18) {
                ages[0]++;
            } else if (p.age < 36) {
                ages[1]++;
            } else if (p.age < 61) {
                ages[2]++;
            } else {
                ages[3]++;
            }
        }
        fill(llAges, new String[]{"0 - 17 ans", "18 - 35 ans", "36 - 60 ans", "60+ ans"},
                ages, 0xFF1976D2);

        String[] labels = new String[6];
        int[] counts = new int[6];
        int[] revenue = new int[6];
        int currentKey = DateUtils.currentMonthKey();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 6; i++) {
            int key = currentKey - (5 - i);
            labels[i] = MOIS[((key % 12) + 12) % 12];
            for (Consultation c : consultations) {
                if (DateUtils.monthKey(c.date) == key) {
                    counts[i]++;
                    if (c.paye) {
                        revenue[i] += c.montant;
                    }
                }
            }
        }
        fill(llMonths, labels, counts, 0xFF00897B);
        fill(llRevenue, labels, revenue, 0xFFEF6C00);
    }

    private void fill(LinearLayout parent, String[] labels, int[] values, int color) {
        parent.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int max = 1;
        for (int v : values) {
            max = Math.max(max, v);
        }
        int maxBar = (int) (170 * density);
        for (int i = 0; i < labels.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, (int) (4 * density), 0, (int) (4 * density));

            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextColor(0xFF444444);
            label.setTextSize(13);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (90 * density), LinearLayout.LayoutParams.WRAP_CONTENT));

            View bar = new View(this);
            bar.setBackgroundColor(color);
            int width = values[i] == 0
                    ? (int) (2 * density)
                    : Math.max((int) (4 * density), (int) ((long) values[i] * maxBar / max));
            bar.setLayoutParams(new LinearLayout.LayoutParams(width, (int) (18 * density)));

            TextView value = new TextView(this);
            value.setText(String.valueOf(values[i]));
            value.setTextColor(0xFF212121);
            value.setTextSize(13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) (8 * density), 0, 0, 0);
            value.setLayoutParams(lp);

            row.addView(label);
            row.addView(bar);
            row.addView(value);
            parent.addView(row);
        }
    }
}

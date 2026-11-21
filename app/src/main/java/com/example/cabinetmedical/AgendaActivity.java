package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgendaActivity extends AppCompatActivity {

    private final List<Patient> shown = new ArrayList<>();
    private RdvAdapter adapter;
    private TextView tabAll;
    private TextView tabToday;
    private TextView tabUpcoming;
    private TextView tvEmpty;
    private TextView tvCount;
    private int filter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        tabAll = findViewById(R.id.tabAll);
        tabToday = findViewById(R.id.tabToday);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        tvEmpty = findViewById(R.id.tvAgendaEmpty);
        tvCount = findViewById(R.id.tvAgendaCount);
        ListView lv = findViewById(R.id.lvAgenda);

        adapter = new RdvAdapter(this, shown);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AgendaActivity.this, PatientDetailActivity.class);
            intent.putExtra("id", shown.get(position).id);
            startActivity(intent);
        });

        tabAll.setOnClickListener(v -> {
            filter = 0;
            refresh();
        });
        tabToday.setOnClickListener(v -> {
            filter = 1;
            refresh();
        });
        tabUpcoming.setOnClickListener(v -> {
            filter = 2;
            refresh();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void styleTab(TextView t, boolean selected) {
        if (selected) {
            t.setBackgroundResource(R.drawable.bg_chip_sel);
            t.setTextColor(0xFFFFFFFF);
        } else {
            t.setBackgroundResource(R.drawable.bg_input);
            t.setTextColor(0xFF212121);
        }
    }

    private void refresh() {
        styleTab(tabAll, filter == 0);
        styleTab(tabToday, filter == 1);
        styleTab(tabUpcoming, filter == 2);

        shown.clear();
        for (Patient p : PatientStore.load(this)) {
            long d = DateUtils.daysFromToday(p.rdv);
            if (d == DateUtils.NO_DATE) {
                continue;
            }
            if (filter == 1 && d != 0) {
                continue;
            }
            if (filter == 2 && d <= 0) {
                continue;
            }
            shown.add(p);
        }
        Collections.sort(shown, (a, b) -> Long.compare(
                DateUtils.daysFromToday(a.rdv), DateUtils.daysFromToday(b.rdv)));
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText(shown.size() + " rendez-vous");
    }
}

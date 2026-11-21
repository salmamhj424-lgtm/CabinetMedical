package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PatientsActivity extends AppCompatActivity {

    private final List<Patient> allPatients = new ArrayList<>();
    private final List<Patient> shownPatients = new ArrayList<>();
    private PatientAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty;
    private TextView tvCount;
    private TextView tvSort;
    private int sortMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        PatientStore.seedIfFirstLaunch(this);

        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        ListView lvPatients = findViewById(R.id.lvPatients);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        TextView tvLogout = findViewById(R.id.tvLogout);
        tvSort = findViewById(R.id.tvSort);

        adapter = new PatientAdapter(this, shownPatients);
        lvPatients.setAdapter(adapter);

        lvPatients.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(PatientsActivity.this, PatientDetailActivity.class);
            intent.putExtra("id", shownPatients.get(position).id);
            startActivity(intent);
        });

        lvPatients.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(shownPatients.get(position));
            return true;
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(PatientsActivity.this, PatientFormActivity.class)));

        tvLogout.setOnClickListener(v -> {
            Intent logout = new Intent(PatientsActivity.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        });

        tvSort.setOnClickListener(v -> {
            sortMode = (sortMode == 0) ? 1 : 0;
            refresh();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refresh();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        allPatients.clear();
        allPatients.addAll(PatientStore.load(this));
        refresh();
    }

    private void refresh() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        shownPatients.clear();
        for (Patient p : allPatients) {
            if (query.isEmpty()
                    || p.nom.toLowerCase(Locale.ROOT).contains(query)
                    || p.telephone.contains(query)) {
                shownPatients.add(p);
            }
        }
        Collections.sort(shownPatients, (a, b) -> {
            if (sortMode == 1) {
                long da = DateUtils.daysFromToday(a.rdv);
                long db = DateUtils.daysFromToday(b.rdv);
                boolean na = da == DateUtils.NO_DATE;
                boolean nb = db == DateUtils.NO_DATE;
                if (na && !nb) {
                    return 1;
                }
                if (!na && nb) {
                    return -1;
                }
                if (!na) {
                    int c = Long.compare(da, db);
                    if (c != 0) {
                        return c;
                    }
                }
            }
            return a.nom.compareToIgnoreCase(b.nom);
        });
        tvSort.setText(sortMode == 0 ? "Tri : A → Z" : "Tri : par rendez-vous");
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(shownPatients.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText(shownPatients.size() + " patient(s)");
    }

    private void confirmDelete(final Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le patient")
                .setMessage("Voulez-vous supprimer " + patient.nom + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    List<Patient> list = PatientStore.load(PatientsActivity.this);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).id.equals(patient.id)) {
                            list.remove(i);
                            break;
                        }
                    }
                    PatientStore.save(PatientsActivity.this, list);
                    ConsultationStore.deleteForPatient(PatientsActivity.this, patient.id);
                    allPatients.clear();
                    allPatients.addAll(list);
                    refresh();
                    Toast.makeText(PatientsActivity.this, "Patient supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

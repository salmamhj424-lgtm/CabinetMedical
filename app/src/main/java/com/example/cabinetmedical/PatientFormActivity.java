package com.example.cabinetmedical;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PatientFormActivity extends AppCompatActivity {

    public static final String[] GROUPES = {"Non précisé", "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"};

    private EditText etNom;
    private EditText etAge;
    private EditText etTel;
    private EditText etRdv;
    private EditText etNotes;
    private EditText etAllergies;
    private Spinner spGroupe;
    private Patient editing = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_form);

        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        etNom = findViewById(R.id.etNom);
        etAge = findViewById(R.id.etAge);
        etTel = findViewById(R.id.etTel);
        etRdv = findViewById(R.id.etRdv);
        etNotes = findViewById(R.id.etNotes);
        etAllergies = findViewById(R.id.etAllergies);
        spGroupe = findViewById(R.id.spGroupe);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<String> groupeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, GROUPES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.parseColor("#212121"));
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.parseColor("#212121"));
                v.setBackgroundColor(Color.WHITE);
                return v;
            }
        };
        groupeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroupe.setAdapter(groupeAdapter);

        String id = getIntent().getStringExtra("id");
        if (id != null) {
            for (Patient p : PatientStore.load(this)) {
                if (p.id.equals(id)) {
                    editing = p;
                    break;
                }
            }
        }

        if (editing != null) {
            tvFormTitle.setText("Modifier le patient");
            etNom.setText(editing.nom);
            etAge.setText(String.valueOf(editing.age));
            etTel.setText(editing.telephone);
            etRdv.setText(editing.rdv);
            etNotes.setText(editing.notes);
            etAllergies.setText(editing.allergies);
            for (int i = 0; i < GROUPES.length; i++) {
                if (GROUPES[i].equals(editing.groupe)) {
                    spGroupe.setSelection(i);
                }
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Nouveau patient");
        }

        etRdv.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etRdv.setText(String.format(Locale.FRANCE, "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String nom = etNom.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String tel = etTel.getText().toString().replace(" ", "");
        String rdv = etRdv.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        int pos = spGroupe.getSelectedItemPosition();
        String groupe = pos <= 0 ? "" : GROUPES[pos];

        if (nom.isEmpty()) {
            etNom.setError("Entrez le nom du patient");
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            etAge.setError("Âge invalide");
            return;
        }
        if (age < 0 || age > 120) {
            etAge.setError("Âge invalide");
            return;
        }
        if (tel.length() < 8 || !tel.matches("\\d+")) {
            etTel.setError("Numéro invalide (8 chiffres)");
            return;
        }

        List<Patient> list = PatientStore.load(this);
        if (editing == null) {
            list.add(new Patient(UUID.randomUUID().toString(), nom, age, tel, notes, rdv,
                    groupe, allergies));
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id.equals(editing.id)) {
                    list.set(i, new Patient(editing.id, nom, age, tel, notes, rdv,
                            groupe, allergies));
                    break;
                }
            }
        }
        PatientStore.save(this, list);
        Toast.makeText(this, "Patient enregistré", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le patient")
                .setMessage("Voulez-vous supprimer " + editing.nom + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    List<Patient> list = PatientStore.load(PatientFormActivity.this);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).id.equals(editing.id)) {
                            list.remove(i);
                            break;
                        }
                    }
                    PatientStore.save(PatientFormActivity.this, list);
                    ConsultationStore.deleteForPatient(PatientFormActivity.this, editing.id);
                    Toast.makeText(PatientFormActivity.this, "Patient supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

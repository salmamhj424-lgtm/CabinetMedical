package com.example.cabinetmedical;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class ConsultationFormActivity extends AppCompatActivity {

    private EditText etDate;
    private EditText etMotif;
    private EditText etDiag;
    private EditText etTrait;
    private EditText etMontant;
    private CheckBox cbPaye;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation_form);

        patientId = getIntent().getStringExtra("patientId");

        etDate = findViewById(R.id.etCDate);
        etMotif = findViewById(R.id.etMotif);
        etDiag = findViewById(R.id.etDiag);
        etTrait = findViewById(R.id.etTrait);
        etMontant = findViewById(R.id.etMontant);
        cbPaye = findViewById(R.id.cbPaye);
        Button btnSave = findViewById(R.id.btnCSave);
        Button btnCancel = findViewById(R.id.btnCCancel);

        etDate.setText(DateUtils.today());
        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format(Locale.FRANCE, "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String motif = etMotif.getText().toString().trim();
        if (motif.isEmpty()) {
            etMotif.setError("Entrez le motif de la consultation");
            return;
        }
        int montant = 0;
        String montantStr = etMontant.getText().toString().trim();
        if (!montantStr.isEmpty()) {
            try {
                montant = Integer.parseInt(montantStr);
            } catch (NumberFormatException e) {
                etMontant.setError("Montant invalide");
                return;
            }
        }
        ConsultationStore.add(this, new Consultation(
                UUID.randomUUID().toString(),
                patientId,
                etDate.getText().toString().trim(),
                motif,
                etDiag.getText().toString().trim(),
                etTrait.getText().toString().trim(),
                montant,
                cbPaye.isChecked()));
        Toast.makeText(this, "Consultation enregistrée", Toast.LENGTH_SHORT).show();
        finish();
    }
}

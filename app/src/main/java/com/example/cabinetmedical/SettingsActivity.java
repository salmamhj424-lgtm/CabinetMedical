package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private EditText etDoctor;
    private EditText etOld;
    private EditText etNew;
    private EditText etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etDoctor = findViewById(R.id.etDoctor);
        etOld = findViewById(R.id.etOldPwd);
        etNew = findViewById(R.id.etNewPwd);
        etConfirm = findViewById(R.id.etConfirmPwd);
        Button btnSaveDoctor = findViewById(R.id.btnSaveDoctor);
        Button btnSavePwd = findViewById(R.id.btnSavePwd);
        Button btnExport = findViewById(R.id.btnExport);
        Button btnClear = findViewById(R.id.btnClear);

        etDoctor.setText(SettingsStore.getDoctor(this));

        btnSaveDoctor.setOnClickListener(v -> {
            SettingsStore.setDoctor(SettingsActivity.this, etDoctor.getText().toString().trim());
            Toast.makeText(SettingsActivity.this, "Nom enregistré", Toast.LENGTH_SHORT).show();
        });
        btnSavePwd.setOnClickListener(v -> changePassword());
        btnExport.setOnClickListener(v -> exportPatients());
        btnClear.setOnClickListener(v -> confirmClear());
    }

    private void changePassword() {
        String oldPwd = etOld.getText().toString();
        String newPwd = etNew.getText().toString();
        String confirm = etConfirm.getText().toString();
        if (!oldPwd.equals(SettingsStore.getPassword(this))) {
            etOld.setError("Ancien mot de passe incorrect");
            return;
        }
        if (newPwd.length() < 4) {
            etNew.setError("Au moins 4 caractères");
            return;
        }
        if (!newPwd.equals(confirm)) {
            etConfirm.setError("Les mots de passe ne correspondent pas");
            return;
        }
        SettingsStore.setPassword(this, newPwd);
        etOld.setText("");
        etNew.setText("");
        etConfirm.setText("");
        Toast.makeText(this, "Mot de passe modifié", Toast.LENGTH_SHORT).show();
    }

    private void exportPatients() {
        List<Patient> patients = PatientStore.load(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Nom;Age;Telephone;Groupe sanguin;Allergies;Prochain RDV;Notes\n");
        for (Patient p : patients) {
            sb.append(clean(p.nom)).append(';')
                    .append(p.age).append(';')
                    .append(clean(p.telephone)).append(';')
                    .append(clean(p.groupe)).append(';')
                    .append(clean(p.allergies)).append(';')
                    .append(clean(p.rdv)).append(';')
                    .append(clean(p.notes)).append('\n');
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Liste des patients - Cabinet Médical");
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, "Exporter les patients (CSV)"));
    }

    private String clean(String s) {
        return s == null ? "" : s.replace(';', ',').replace('\n', ' ');
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer toutes les données")
                .setMessage("Tous les patients et toutes les consultations seront supprimés. "
                        + "Cette action est définitive.")
                .setPositiveButton("Tout supprimer", (dialog, which) -> {
                    PatientStore.clearAll(SettingsActivity.this);
                    ConsultationStore.clearAll(SettingsActivity.this);
                    Toast.makeText(SettingsActivity.this, "Données supprimées",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

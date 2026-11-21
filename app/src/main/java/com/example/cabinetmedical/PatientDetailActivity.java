package com.example.cabinetmedical;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailActivity extends AppCompatActivity {

    private String patientId;
    private Patient patient;
    private final List<Consultation> consultations = new ArrayList<>();
    private ConsultationAdapter adapter;

    private TextView tvInitials;
    private TextView tvNom;
    private TextView tvAge;
    private TextView tvTel;
    private TextView tvRdv;
    private TextView tvNotes;
    private TextView tvGroupe;
    private TextView tvAllergies;
    private TextView tvFinance;
    private TextView tvNoConsult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        patientId = getIntent().getStringExtra("id");

        tvInitials = findViewById(R.id.tvDInitials);
        tvNom = findViewById(R.id.tvDNom);
        tvAge = findViewById(R.id.tvDAge);
        tvTel = findViewById(R.id.tvDTel);
        tvRdv = findViewById(R.id.tvDRdv);
        tvNotes = findViewById(R.id.tvDNotes);
        tvGroupe = findViewById(R.id.tvDGroupe);
        tvAllergies = findViewById(R.id.tvDAllergies);
        tvFinance = findViewById(R.id.tvDFinance);
        tvNoConsult = findViewById(R.id.tvNoConsult);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnSms = findViewById(R.id.btnSms);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnAddConsult = findViewById(R.id.btnAddConsult);
        ListView lv = findViewById(R.id.lvConsult);

        adapter = new ConsultationAdapter(this, consultations);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) ->
                showConsultationOptions(consultations.get(position)));

        btnCall.setOnClickListener(v -> launch(Intent.ACTION_DIAL, "tel:"));
        btnSms.setOnClickListener(v -> launch(Intent.ACTION_SENDTO, "smsto:"));
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailActivity.this, PatientFormActivity.class);
            intent.putExtra("id", patientId);
            startActivity(intent);
        });
        btnAddConsult.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailActivity.this,
                    ConsultationFormActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
        });
    }

    private void launch(String action, String scheme) {
        if (patient == null) {
            return;
        }
        try {
            startActivity(new Intent(action, Uri.parse(scheme + patient.telephone)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Action non disponible sur cet appareil",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        patient = null;
        for (Patient p : PatientStore.load(this)) {
            if (p.id.equals(patientId)) {
                patient = p;
                break;
            }
        }
        if (patient == null) {
            finish();
            return;
        }
        tvInitials.setText(patient.initials());
        tvNom.setText(patient.nom);
        tvAge.setText(patient.age + " ans");
        tvTel.setText("📞  " + patient.telephone);
        if (patient.rdv.isEmpty()) {
            tvRdv.setText("📅  Aucun rendez-vous prévu");
        } else {
            long d = DateUtils.daysFromToday(patient.rdv);
            tvRdv.setText("📅  Prochain RDV : " + patient.rdv
                    + (d == DateUtils.NO_DATE ? "" : " (" + DateUtils.describe(d) + ")"));
        }
        tvNotes.setText(patient.notes.isEmpty()
                ? "📝  Aucune note médicale" : "📝  " + patient.notes);
        if (patient.groupe.isEmpty()) {
            tvGroupe.setVisibility(View.GONE);
        } else {
            tvGroupe.setVisibility(View.VISIBLE);
            tvGroupe.setText("🩸  Groupe sanguin : " + patient.groupe);
        }
        if (patient.allergies.isEmpty()) {
            tvAllergies.setVisibility(View.GONE);
        } else {
            tvAllergies.setVisibility(View.VISIBLE);
            tvAllergies.setText("⚠️  Allergies : " + patient.allergies);
        }
        loadConsultations();
    }

    private void loadConsultations() {
        consultations.clear();
        consultations.addAll(ConsultationStore.loadFor(this, patientId));
        adapter.notifyDataSetChanged();
        tvNoConsult.setVisibility(consultations.isEmpty() ? View.VISIBLE : View.GONE);
        tvFinance.setText("💰  Payé : " + ConsultationStore.totalPaid(consultations)
                + " DT   |   Impayé : " + ConsultationStore.totalUnpaid(consultations) + " DT");
    }

    private void showConsultationOptions(final Consultation c) {
        String[] options = {
                "📤  Partager l'ordonnance",
                c.paye ? "↩️  Marquer comme non payée" : "✅  Marquer comme payée",
                "🗑️  Supprimer"
        };
        new AlertDialog.Builder(this)
                .setTitle("Consultation du " + c.date)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareOrdonnance(c);
                    } else if (which == 1) {
                        ConsultationStore.update(PatientDetailActivity.this,
                                new Consultation(c.id, c.patientId, c.date, c.motif,
                                        c.diagnostic, c.traitement, c.montant, !c.paye));
                        loadConsultations();
                    } else {
                        confirmDeleteConsultation(c);
                    }
                })
                .show();
    }

    private void shareOrdonnance(Consultation c) {
        if (patient == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CABINET MÉDICAL - ORDONNANCE\n\n");
        sb.append("Patient : ").append(patient.nom).append(" (").append(patient.age)
                .append(" ans)\n");
        sb.append("Date : ").append(c.date).append("\n");
        sb.append("Motif : ").append(c.motif).append("\n");
        if (!c.diagnostic.isEmpty()) {
            sb.append("Diagnostic : ").append(c.diagnostic).append("\n");
        }
        if (!c.traitement.isEmpty()) {
            sb.append("Traitement : ").append(c.traitement).append("\n");
        }
        if (!patient.allergies.isEmpty()) {
            sb.append("Allergies : ").append(patient.allergies).append("\n");
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Ordonnance - " + patient.nom);
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, "Partager l'ordonnance"));
    }

    private void confirmDeleteConsultation(final Consultation c) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la consultation")
                .setMessage("Voulez-vous supprimer la consultation du " + c.date + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    ConsultationStore.delete(PatientDetailActivity.this, c.id);
                    loadConsultations();
                    Toast.makeText(PatientDetailActivity.this, "Consultation supprimée",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

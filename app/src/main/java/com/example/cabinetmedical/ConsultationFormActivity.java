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

/**
 * Écran formulaire pour ajouter une nouvelle consultation à un patient.
 * Il utilise ConsultationStore (pour enregistrer), Consultation (l'objet) et DateUtils.
 * On y arrive depuis l'écran de détail du patient (PatientDetailActivity),
 * qui envoie l'identifiant du patient dans un Intent.
 */
public class ConsultationFormActivity extends AppCompatActivity {

    // Champ de la date de consultation
    private EditText etDate;
    // Champ du motif (pourquoi le patient vient)
    private EditText etMotif;
    // Champ du diagnostic
    private EditText etDiag;
    // Champ du traitement
    private EditText etTrait;
    // Champ du montant à payer
    private EditText etMontant;
    // Case à cocher "payé"
    private CheckBox cbPaye;
    // Identifiant du patient concerné par cette consultation
    private String patientId;

    /**
     * Appelée par Android quand l'écran (Activity) est créé : on prépare le formulaire.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appel obligatoire de la classe parente
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran (fichier XML activity_consultation_form)
        setContentView(R.layout.activity_consultation_form);

        // getIntent() = l'Intent (message) qui a ouvert cet écran ; on lit l'id du patient dedans
        patientId = getIntent().getStringExtra("patientId");

        // findViewById : on récupère chaque élément du XML grâce à son id
        etDate = findViewById(R.id.etCDate);
        // Champ motif
        etMotif = findViewById(R.id.etMotif);
        // Champ diagnostic
        etDiag = findViewById(R.id.etDiag);
        // Champ traitement
        etTrait = findViewById(R.id.etTrait);
        // Champ montant
        etMontant = findViewById(R.id.etMontant);
        // Case à cocher "payé"
        cbPaye = findViewById(R.id.cbPaye);
        // Bouton "Enregistrer"
        Button btnSave = findViewById(R.id.btnCSave);
        // Bouton "Annuler"
        Button btnCancel = findViewById(R.id.btnCCancel);

        // Par défaut, la date proposée est celle d'aujourd'hui
        etDate.setText(DateUtils.today());
        // Au clic sur le champ date, on ouvre le calendrier (lambda : petite fonction au clic)
        etDate.setOnClickListener(v -> showDatePicker());
        // Au clic sur "Enregistrer", on lance la méthode save()
        btnSave.setOnClickListener(v -> save());
        // Au clic sur "Annuler", on ferme simplement l'écran (finish)
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Affiche une fenêtre calendrier (Dialog) pour choisir la date.
     * Un Dialog est une petite fenêtre qui s'affiche par-dessus l'écran.
     */
    private void showDatePicker() {
        // On récupère la date d'aujourd'hui pour que le calendrier s'ouvre dessus
        Calendar c = Calendar.getInstance();
        // On crée le calendrier. Quand l'utilisateur choisit un jour, la lambda est appelée :
        // elle écrit la date au format jj/mm/aaaa dans le champ (month + 1 car janvier = 0).
        // Les 3 derniers arguments sont l'année, le mois et le jour de départ ; show() l'affiche.
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format(Locale.FRANCE, "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Vérifie le formulaire puis enregistre la consultation.
     */
    private void save() {
        // On lit le motif écrit par l'utilisateur, sans espaces inutiles (trim)
        String motif = etMotif.getText().toString().trim();
        // Si le motif est vide (champ obligatoire)...
        if (motif.isEmpty()) {
            // ...on affiche une erreur sur le champ
            etMotif.setError("Entrez le motif de la consultation");
            // On arrête la méthode : on n'enregistre rien
            return;
        }
        // Montant par défaut : 0 (si l'utilisateur ne met rien)
        int montant = 0;
        // On lit le texte du montant
        String montantStr = etMontant.getText().toString().trim();
        // Si l'utilisateur a écrit quelque chose...
        if (!montantStr.isEmpty()) {
            // try/catch : on essaie, et si ça échoue on gère l'erreur sans faire planter l'appli
            try {
                // On transforme le texte en nombre entier
                montant = Integer.parseInt(montantStr);
            } catch (NumberFormatException e) {
                // Le texte n'est pas un nombre valide : on affiche une erreur
                etMontant.setError("Montant invalide");
                // On arrête la méthode
                return;
            }
        }
        // On crée un objet Consultation avec toutes les infos et on l'ajoute dans le stockage.
        // UUID.randomUUID() fabrique un identifiant unique au hasard.
        ConsultationStore.add(this, new Consultation(
                UUID.randomUUID().toString(),
                patientId,
                etDate.getText().toString().trim(),
                motif,
                etDiag.getText().toString().trim(),
                etTrait.getText().toString().trim(),
                montant,
                cbPaye.isChecked()));
        // Toast = petit message temporaire pour confirmer à l'utilisateur
        Toast.makeText(this, "Consultation enregistrée", Toast.LENGTH_SHORT).show();
        // On ferme l'écran pour revenir à l'écran précédent
        finish();
    }
}

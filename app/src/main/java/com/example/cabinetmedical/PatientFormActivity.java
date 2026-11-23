// On indique le dossier (package) où se trouve cette classe
package com.example.cabinetmedical;

// Boîte de dialogue qui affiche un calendrier pour choisir une date
import android.app.DatePickerDialog;
// Outil pour manipuler les couleurs
import android.graphics.Color;
// Bundle : petit sac de données transmis à l'écran quand il se crée
import android.os.Bundle;
// View : classe de base de tout élément visible à l'écran
import android.view.View;
// ViewGroup : un conteneur qui contient d'autres éléments visibles
import android.view.ViewGroup;
// ArrayAdapter : relie un tableau de textes à une liste déroulante
import android.widget.ArrayAdapter;
// Bouton cliquable
import android.widget.Button;
// Champ où l'utilisateur tape du texte
import android.widget.EditText;
// Spinner : liste déroulante
import android.widget.Spinner;
// Zone de texte non modifiable
import android.widget.TextView;
// Toast : petit message qui apparaît quelques secondes
import android.widget.Toast;

// Boîte de dialogue d'alerte (avec boutons)
import androidx.appcompat.app.AlertDialog;
// Classe de base de nos écrans (Activity)
import androidx.appcompat.app.AppCompatActivity;

// Calendar : sert à connaître la date d'aujourd'hui
import java.util.Calendar;
// List : une liste d'éléments
import java.util.List;
// Locale : une langue / un pays (pour le format des nombres et dates)
import java.util.Locale;
// UUID : sert à fabriquer un identifiant unique
import java.util.UUID;

/**
 * Écran formulaire pour ajouter un nouveau patient ou modifier un patient existant.
 * Une Activity est un écran de l'application ; ici elle affiche activity_patient_form.xml.
 * Elle utilise PatientStore (sauvegarde des patients) et ConsultationStore.
 * On y arrive depuis PatientsActivity (bouton + ou modification d'un patient).
 */
public class PatientFormActivity extends AppCompatActivity {

    // Liste fixe des groupes sanguins proposés dans la liste déroulante
    // "static final" veut dire que cette valeur est partagée et ne change jamais
    public static final String[] GROUPES = {"Non précisé", "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"};

    // Champ pour le nom du patient
    private EditText etNom;
    // Champ pour l'âge
    private EditText etAge;
    // Champ pour le numéro de téléphone
    private EditText etTel;
    // Champ pour la date du rendez-vous
    private EditText etRdv;
    // Champ pour les notes
    private EditText etNotes;
    // Champ pour les allergies
    private EditText etAllergies;
    // Liste déroulante du groupe sanguin
    private Spinner spGroupe;
    // Patient en cours de modification ; null veut dire qu'on crée un nouveau patient
    private Patient editing = null;

    /**
     * Méthode appelée par Android quand l'écran est créé : on prépare tout ici.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle le onCreate de la classe mère (obligatoire)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML
        setContentView(R.layout.activity_patient_form);

        // findViewById : on récupère un élément du XML grâce à son id, pour l'utiliser en Java
        // Le titre du formulaire (change selon "nouveau" ou "modifier")
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        // On récupère le champ du nom
        etNom = findViewById(R.id.etNom);
        // On récupère le champ de l'âge
        etAge = findViewById(R.id.etAge);
        // On récupère le champ du téléphone
        etTel = findViewById(R.id.etTel);
        // On récupère le champ du rendez-vous
        etRdv = findViewById(R.id.etRdv);
        // On récupère le champ des notes
        etNotes = findViewById(R.id.etNotes);
        // On récupère le champ des allergies
        etAllergies = findViewById(R.id.etAllergies);
        // On récupère la liste déroulante du groupe sanguin
        spGroupe = findViewById(R.id.spGroupe);
        // On récupère le bouton Enregistrer
        Button btnSave = findViewById(R.id.btnSave);
        // On récupère le bouton Supprimer
        Button btnDelete = findViewById(R.id.btnDelete);
        // On récupère le bouton Annuler
        Button btnCancel = findViewById(R.id.btnCancel);

        // Un Adapter fait le lien entre les données (GROUPES) et la liste déroulante.
        // Ici on crée un adaptateur et on change un peu son affichage (couleurs du texte).
        // this = cet écran ; c'est un Context (il donne accès aux ressources de l'appli)
        ArrayAdapter<String> groupeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, GROUPES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // On demande à la classe mère de fabriquer la ligne normale
                View v = super.getView(position, convertView, parent);
                // On met le texte en gris foncé pour qu'il soit bien lisible
                ((TextView) v).setTextColor(Color.parseColor("#212121"));
                // On renvoie la ligne modifiée
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // On fabrique la ligne normale de la liste qui s'ouvre
                View v = super.getDropDownView(position, convertView, parent);
                // Texte en gris foncé
                ((TextView) v).setTextColor(Color.parseColor("#212121"));
                // Fond blanc pour bien voir le texte
                v.setBackgroundColor(Color.WHITE);
                // On renvoie la ligne modifiée
                return v;
            }
        };
        // On choisit le design des lignes quand la liste déroulante est ouverte
        groupeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // On branche l'adaptateur sur la liste déroulante
        spGroupe.setAdapter(groupeAdapter);

        // Intent : message qui a servi à ouvrir cet écran.
        // On y lit l'id du patient (s'il y en a un)
        String id = getIntent().getStringExtra("id");
        // Si un id a été transmis, on est en mode modification
        if (id != null) {
            // On parcourt tous les patients enregistrés
            for (Patient p : PatientStore.load(this)) {
                // Si c'est le patient qui a le même id que celui reçu
                if (p.id.equals(id)) {
                    // On le garde pour remplir le formulaire
                    editing = p;
                    // On arrête la boucle, on l'a trouvé
                    break;
                }
            }
        }

        // Si on a trouvé un patient à modifier, on pré-remplit le formulaire
        if (editing != null) {
            // On change le titre de l'écran
            tvFormTitle.setText("Modifier le patient");
            // On affiche le nom actuel
            etNom.setText(editing.nom);
            // L'âge est un nombre : on le transforme en texte pour l'afficher
            etAge.setText(String.valueOf(editing.age));
            // On affiche le téléphone actuel
            etTel.setText(editing.telephone);
            // On affiche la date de rendez-vous actuelle
            etRdv.setText(editing.rdv);
            // On affiche les notes actuelles
            etNotes.setText(editing.notes);
            // On affiche les allergies actuelles
            etAllergies.setText(editing.allergies);
            // On cherche le groupe sanguin du patient dans la liste GROUPES
            for (int i = 0; i < GROUPES.length; i++) {
                // Si ce groupe correspond à celui du patient
                if (GROUPES[i].equals(editing.groupe)) {
                    // On sélectionne cette ligne dans la liste déroulante
                    spGroupe.setSelection(i);
                }
            }
            // En modification, on rend le bouton Supprimer visible
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Sinon c'est un nouveau patient : on change le titre
            tvFormTitle.setText("Nouveau patient");
        }

        // Lambda (v -> ...) : petite fonction exécutée au clic. Ici on ouvre le calendrier
        etRdv.setOnClickListener(v -> showDatePicker());
        // Au clic sur Enregistrer, on appelle la méthode save()
        btnSave.setOnClickListener(v -> save());
        // Au clic sur Annuler, finish() ferme l'écran et on revient en arrière
        btnCancel.setOnClickListener(v -> finish());
        // Au clic sur Supprimer, on demande confirmation
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    /**
     * Affiche un calendrier et met la date choisie dans le champ du rendez-vous.
     */
    private void showDatePicker() {
        // On récupère la date d'aujourd'hui pour que le calendrier s'ouvre dessus
        Calendar c = Calendar.getInstance();
        // On crée la boîte de dialogue calendrier et on l'affiche avec show()
        // Quand l'utilisateur choisit une date, la lambda ci-dessous est exécutée
        // On écrit la date au format jj/mm/aaaa (month + 1 car les mois commencent à 0)
        new DatePickerDialog(this, (view, year, month, day) ->
                etRdv.setText(String.format(Locale.FRANCE, "%02d/%02d/%04d", day, month + 1, year)),
                // Année, mois et jour de départ du calendrier : aujourd'hui
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Vérifie les champs, puis enregistre le patient (nouveau ou modifié).
     */
    private void save() {
        // On lit le nom ; trim() enlève les espaces au début et à la fin
        String nom = etNom.getText().toString().trim();
        // On lit l'âge sous forme de texte
        String ageStr = etAge.getText().toString().trim();
        // On lit le téléphone et on enlève tous les espaces
        String tel = etTel.getText().toString().replace(" ", "");
        // On lit la date de rendez-vous
        String rdv = etRdv.getText().toString().trim();
        // On lit les notes
        String notes = etNotes.getText().toString().trim();
        // On lit les allergies
        String allergies = etAllergies.getText().toString().trim();
        // Position de la ligne choisie dans la liste déroulante (0 = première ligne)
        int pos = spGroupe.getSelectedItemPosition();
        // Si la position est 0 ("Non précisé"), on garde un texte vide, sinon le groupe choisi
        String groupe = pos <= 0 ? "" : GROUPES[pos];

        // Si le nom est vide, c'est une erreur
        if (nom.isEmpty()) {
            // On affiche un message d'erreur sur le champ du nom
            etNom.setError("Entrez le nom du patient");
            // return : on arrête la méthode, rien n'est enregistré
            return;
        }
        // Variable qui va contenir l'âge sous forme de nombre
        int age;
        // try/catch : on essaie quelque chose qui peut échouer
        try {
            // On transforme le texte en nombre entier
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            // Si ce n'est pas un nombre (ex : vide ou des lettres), on arrive ici
            etAge.setError("Âge invalide");
            // On arrête la méthode
            return;
        }
        // Si l'âge est négatif ou plus grand que 120, il n'est pas réaliste
        if (age < 0 || age > 120) {
            // On affiche l'erreur sur le champ de l'âge
            etAge.setError("Âge invalide");
            // On arrête la méthode
            return;
        }
        // Le téléphone doit avoir au moins 8 caractères et contenir seulement des chiffres
        // "\\d+" est une expression qui veut dire "un ou plusieurs chiffres"
        if (tel.length() < 8 || !tel.matches("\\d+")) {
            // On affiche l'erreur sur le champ du téléphone
            etTel.setError("Numéro invalide (8 chiffres)");
            // On arrête la méthode
            return;
        }

        // Si editing est null, on crée un nouveau patient
        if (editing == null) {
            // INSERT : on ajoute un patient avec un identifiant unique fabriqué par UUID
            PatientStore.insert(this, new Patient(UUID.randomUUID().toString(), nom, age, tel,
                    notes, rdv, groupe, allergies));
        } else {
            // UPDATE : sinon on modifie le patient existant (même id, nouvelles infos)
            PatientStore.update(this, new Patient(editing.id, nom, age, tel, notes, rdv,
                    groupe, allergies));
        }
        // Toast : petit message pour confirmer à l'utilisateur
        Toast.makeText(this, "Patient enregistré", Toast.LENGTH_SHORT).show();
        // On ferme le formulaire et on revient à l'écran précédent
        finish();
    }

    /**
     * Demande confirmation, puis supprime le patient et ses consultations.
     */
    private void confirmDelete() {
        // Dialog : fenêtre qui s'ouvre au-dessus de l'écran. On la construit étape par étape
        new AlertDialog.Builder(this)
                // Titre de la fenêtre
                .setTitle("Supprimer le patient")
                // Message qui contient le nom du patient
                .setMessage("Voulez-vous supprimer " + editing.nom + " ?")
                // Bouton "Supprimer" ; la lambda s'exécute quand on clique dessus
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // PatientFormActivity.this = cet écran (nécessaire dans une lambda)
                    // DELETE : on supprime le patient dans la base
                    PatientStore.delete(PatientFormActivity.this, editing.id);
                    // On supprime aussi ses consultations
                    ConsultationStore.deleteForPatient(PatientFormActivity.this, editing.id);
                    // On affiche un petit message de confirmation
                    Toast.makeText(PatientFormActivity.this, "Patient supprimé", Toast.LENGTH_SHORT).show();
                    // On ferme le formulaire
                    finish();
                })
                // Bouton "Annuler" : null veut dire qu'on ne fait rien, la fenêtre se ferme
                .setNegativeButton("Annuler", null)
                // On affiche la fenêtre
                .show();
    }
}

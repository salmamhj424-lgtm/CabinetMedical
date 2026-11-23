package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Écran des paramètres : nom du médecin, mot de passe, export CSV et suppression des données.
 * Il utilise SettingsStore (réglages enregistrés), PatientStore et ConsultationStore.
 * On y arrive en cliquant sur le bouton "Paramètres" de HomeActivity.
 */
// Une Activity = un écran de l'application. AppCompatActivity est la version moderne.
public class SettingsActivity extends AppCompatActivity {

    // Champ de saisie du nom du médecin
    private EditText etDoctor;
    // Champ de saisie de l'ancien mot de passe
    private EditText etOld;
    // Champ de saisie du nouveau mot de passe
    private EditText etNew;
    // Champ de saisie pour confirmer le nouveau mot de passe
    private EditText etConfirm;

    /**
     * Appelée une seule fois quand l'écran est créé : on prépare les champs et les boutons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle d'abord le onCreate de la classe parente (obligatoire sur Android)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML activity_settings
        setContentView(R.layout.activity_settings);

        // findViewById : on récupère un élément du XML grâce à son id, pour pouvoir le modifier
        etDoctor = findViewById(R.id.etDoctor);
        // On récupère le champ de l'ancien mot de passe
        etOld = findViewById(R.id.etOldPwd);
        // On récupère le champ du nouveau mot de passe
        etNew = findViewById(R.id.etNewPwd);
        // On récupère le champ de confirmation
        etConfirm = findViewById(R.id.etConfirmPwd);
        // On récupère le bouton qui enregistre le nom du médecin
        Button btnSaveDoctor = findViewById(R.id.btnSaveDoctor);
        // On récupère le bouton qui enregistre le mot de passe
        Button btnSavePwd = findViewById(R.id.btnSavePwd);
        // On récupère le bouton d'export des patients
        Button btnExport = findViewById(R.id.btnExport);
        // On récupère le bouton qui supprime toutes les données
        Button btnClear = findViewById(R.id.btnClear);

        // On affiche dans le champ le nom du médecin déjà enregistré
        // ("this" = cet écran, aussi appelé Context : il donne accès aux fonctions Android)
        etDoctor.setText(SettingsStore.getDoctor(this));

        // Quand on clique sur le bouton, on exécute le code après la flèche.
        // "v -> {...}" est une lambda : une petite fonction écrite directement ici.
        btnSaveDoctor.setOnClickListener(v -> {
            // On enregistre le nom tapé (trim() enlève les espaces au début et à la fin)
            // Il est gardé dans le téléphone grâce aux SharedPreferences (via SettingsStore)
            SettingsStore.setDoctor(SettingsActivity.this, etDoctor.getText().toString().trim());
            // Un Toast est un petit message qui apparaît quelques secondes puis disparaît
            Toast.makeText(SettingsActivity.this, "Nom enregistré", Toast.LENGTH_SHORT).show();
        });
        // Clic sur "enregistrer le mot de passe" : on appelle la méthode changePassword()
        btnSavePwd.setOnClickListener(v -> changePassword());
        // Clic sur "exporter" : on appelle la méthode exportPatients()
        btnExport.setOnClickListener(v -> exportPatients());
        // Clic sur "supprimer" : on appelle confirmClear() (qui demande confirmation)
        btnClear.setOnClickListener(v -> confirmClear());
    }

    /**
     * Vérifie les champs puis change le mot de passe si tout est correct.
     */
    private void changePassword() {
        // On lit le texte tapé dans le champ de l'ancien mot de passe
        String oldPwd = etOld.getText().toString();
        // On lit le texte du nouveau mot de passe
        String newPwd = etNew.getText().toString();
        // On lit le texte de la confirmation
        String confirm = etConfirm.getText().toString();
        // Si l'ancien mot de passe tapé est différent de celui enregistré
        if (!oldPwd.equals(SettingsStore.getPassword(this))) {
            // On affiche une erreur sur le champ
            etOld.setError("Ancien mot de passe incorrect");
            // return : on arrête la méthode ici, rien n'est modifié
            return;
        }
        // Si le nouveau mot de passe a moins de 4 caractères
        if (newPwd.length() < 4) {
            // On affiche une erreur sur le champ
            etNew.setError("Au moins 4 caractères");
            // On arrête la méthode
            return;
        }
        // Si le nouveau mot de passe est différent de sa confirmation
        if (!newPwd.equals(confirm)) {
            // On affiche une erreur sur le champ de confirmation
            etConfirm.setError("Les mots de passe ne correspondent pas");
            // On arrête la méthode
            return;
        }
        // Tout est correct : on enregistre le nouveau mot de passe
        SettingsStore.setPassword(this, newPwd);
        // On vide le champ de l'ancien mot de passe
        etOld.setText("");
        // On vide le champ du nouveau mot de passe
        etNew.setText("");
        // On vide le champ de confirmation
        etConfirm.setText("");
        // On affiche un petit message de réussite
        Toast.makeText(this, "Mot de passe modifié", Toast.LENGTH_SHORT).show();
    }

    /**
     * Crée un texte CSV avec tous les patients et propose de l'envoyer vers une autre appli.
     * CSV = un tableau écrit en texte, avec des ";" entre les colonnes.
     */
    private void exportPatients() {
        // On charge la liste des patients
        List<Patient> patients = PatientStore.load(this);
        // StringBuilder sert à construire un long texte morceau par morceau
        StringBuilder sb = new StringBuilder();
        // On écrit la première ligne : les titres des colonnes
        sb.append("Nom;Age;Telephone;Groupe sanguin;Allergies;Prochain RDV;Notes\n");
        // On regarde les patients un par un
        for (Patient p : patients) {
            // On ajoute une ligne pour ce patient : une valeur par colonne, séparées par ';'
            // clean() enlève les caractères qui casseraient le tableau
            // Le '\n' à la fin passe à la ligne suivante
            sb.append(clean(p.nom)).append(';')
                    .append(p.age).append(';')
                    .append(clean(p.telephone)).append(';')
                    .append(clean(p.groupe)).append(';')
                    .append(clean(p.allergies)).append(';')
                    .append(clean(p.rdv)).append(';')
                    .append(clean(p.notes)).append('\n');
        }
        // Un Intent = un message qui dit à Android ce qu'on veut faire
        // Ici ACTION_SEND veut dire "envoyer du contenu vers une autre application"
        Intent send = new Intent(Intent.ACTION_SEND);
        // On précise que le contenu est du texte simple
        send.setType("text/plain");
        // On ajoute un objet (sujet), utile par exemple pour un e-mail
        send.putExtra(Intent.EXTRA_SUBJECT, "Liste des patients - Cabinet Médical");
        // On ajoute le texte à envoyer : tout le CSV construit plus haut
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        // On affiche la liste des applis possibles (mail, WhatsApp...) pour faire un choix
        startActivity(Intent.createChooser(send, "Exporter les patients (CSV)"));
    }

    /**
     * Nettoie un texte pour qu'il ne casse pas le CSV.
     */
    private String clean(String s) {
        // Si le texte est null on renvoie un texte vide ; sinon on remplace
        // les ';' par des ',' et les retours à la ligne par des espaces
        return s == null ? "" : s.replace(';', ',').replace('\n', ' ');
    }

    /**
     * Affiche une fenêtre qui demande confirmation avant de tout supprimer.
     */
    private void confirmClear() {
        // Un AlertDialog est une petite fenêtre (boîte de dialogue) au-dessus de l'écran
        // Le Builder la construit étape par étape, en enchaînant les appels
        new AlertDialog.Builder(this)
                // Le titre de la fenêtre
                .setTitle("Supprimer toutes les données")
                // Le message d'avertissement
                .setMessage("Tous les patients et toutes les consultations seront supprimés. "
                        + "Cette action est définitive.")
                // Le bouton "oui" ; le code après la flèche s'exécute si on clique dessus
                .setPositiveButton("Tout supprimer", (dialog, which) -> {
                    // On supprime tous les patients
                    PatientStore.clearAll(SettingsActivity.this);
                    // On supprime toutes les consultations
                    ConsultationStore.clearAll(SettingsActivity.this);
                    // On affiche un petit message pour confirmer
                    Toast.makeText(SettingsActivity.this, "Données supprimées",
                            Toast.LENGTH_SHORT).show();
                })
                // Le bouton "non" ; null = on ne fait rien, la fenêtre se ferme simplement
                .setNegativeButton("Annuler", null)
                // On affiche enfin la fenêtre à l'écran
                .show();
    }
}

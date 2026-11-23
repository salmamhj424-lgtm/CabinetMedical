// On indique le dossier (package) où se trouve cette classe
package com.example.cabinetmedical;

// Intent : message pour ouvrir un autre écran
import android.content.Intent;
// Bundle : petit sac de données transmis à l'écran quand il se crée
import android.os.Bundle;
// Editable : le texte en cours de saisie dans un champ
import android.text.Editable;
// TextWatcher : "surveillant" qui nous prévient quand le texte d'un champ change
import android.text.TextWatcher;
// View : classe de base de tout élément visible à l'écran
import android.view.View;
// Champ où l'utilisateur tape du texte
import android.widget.EditText;
// ListView : liste qui affiche les patients
import android.widget.ListView;
// Zone de texte non modifiable
import android.widget.TextView;
// Toast : petit message qui apparaît quelques secondes
import android.widget.Toast;

// Boîte de dialogue d'alerte (avec boutons)
import androidx.appcompat.app.AlertDialog;
// Classe de base de nos écrans (Activity)
import androidx.appcompat.app.AppCompatActivity;

// Bouton rond flottant (le bouton + )
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// ArrayList : un type de liste que l'on peut agrandir
import java.util.ArrayList;
// Collections : outils pour les listes (ici, le tri)
import java.util.Collections;
// List : une liste d'éléments
import java.util.List;
// Locale : une langue / un pays (utile pour mettre en minuscules)
import java.util.Locale;

/**
 * Écran principal : affiche la liste des patients avec recherche et tri.
 * Une Activity est un écran de l'application ; ici elle affiche activity_patients.xml.
 * Elle utilise PatientStore (données), PatientAdapter (affichage de la liste) et DateUtils.
 * On y arrive depuis l'écran de connexion (MainActivity).
 */
public class PatientsActivity extends AppCompatActivity {

    // Liste de TOUS les patients enregistrés
    private final List<Patient> allPatients = new ArrayList<>();
    // Liste des patients affichés (après recherche et tri)
    private final List<Patient> shownPatients = new ArrayList<>();
    // Adapter : fait le lien entre la liste de patients et la ListView
    private PatientAdapter adapter;
    // Champ de recherche
    private EditText etSearch;
    // Message "aucun patient" quand la liste est vide
    private TextView tvEmpty;
    // Texte qui indique le nombre de patients
    private TextView tvCount;
    // Texte sur lequel on clique pour changer le tri
    private TextView tvSort;
    // Mode de tri : 0 = ordre alphabétique, 1 = par date de rendez-vous
    private int sortMode = 0;

    /**
     * Méthode appelée par Android quand l'écran est créé : on prépare tout ici.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle le onCreate de la classe mère (obligatoire)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML
        setContentView(R.layout.activity_patients);

        // Au tout premier lancement, on ajoute quelques patients d'exemple
        PatientStore.seedIfFirstLaunch(this);

        // findViewById : on récupère un élément du XML grâce à son id, pour l'utiliser en Java
        // On récupère le champ de recherche
        etSearch = findViewById(R.id.etSearch);
        // On récupère le message "liste vide"
        tvEmpty = findViewById(R.id.tvEmpty);
        // On récupère le texte du compteur
        tvCount = findViewById(R.id.tvCount);
        // On récupère la liste qui affiche les patients
        ListView lvPatients = findViewById(R.id.lvPatients);
        // On récupère le bouton rond + pour ajouter un patient
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        // On récupère le texte "Déconnexion"
        TextView tvLogout = findViewById(R.id.tvLogout);
        // On récupère le texte qui change le tri
        tvSort = findViewById(R.id.tvSort);

        // On crée l'adapter ; this = cet écran (un Context : il donne accès aux ressources)
        adapter = new PatientAdapter(this, shownPatients);
        // On branche l'adapter sur la liste : elle affichera shownPatients
        lvPatients.setAdapter(adapter);

        // Lambda (... -> ...) : petite fonction exécutée quand on clique sur une ligne
        lvPatients.setOnItemClickListener((parent, view, position, id) -> {
            // Intent : on prépare l'ouverture de l'écran de détails du patient
            Intent intent = new Intent(PatientsActivity.this, PatientDetailActivity.class);
            // On joint l'id du patient cliqué (position = numéro de la ligne cliquée)
            intent.putExtra("id", shownPatients.get(position).id);
            // On ouvre l'écran de détails
            startActivity(intent);
        });

        // Exécuté quand on reste appuyé longtemps sur une ligne
        lvPatients.setOnItemLongClickListener((parent, view, position, id) -> {
            // On demande confirmation avant de supprimer ce patient
            confirmDelete(shownPatients.get(position));
            // true = on dit à Android que l'appui long est traité (pas de clic normal en plus)
            return true;
        });

        // Au clic sur le bouton +, on ouvre le formulaire (sans id = nouveau patient)
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(PatientsActivity.this, PatientFormActivity.class)));

        // Au clic sur "Déconnexion"
        tvLogout.setOnClickListener(v -> {
            // On prépare le retour à l'écran de connexion
            Intent logout = new Intent(PatientsActivity.this, MainActivity.class);
            // Ces options effacent les anciens écrans : on ne peut plus revenir avec "retour"
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // On ouvre l'écran de connexion
            startActivity(logout);
        });

        // Au clic sur le texte du tri
        tvSort.setOnClickListener(v -> {
            // On alterne : si le mode est 0 on passe à 1, sinon on revient à 0
            sortMode = (sortMode == 0) ? 1 : 0;
            // On remet la liste à jour avec le nouveau tri
            refresh();
        });

        // On ajoute un "surveillant" qui réagit quand le texte de recherche change
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Après chaque changement du texte, on met à jour la liste affichée
                refresh();
            }
        });
    }

    /**
     * Appelée à chaque fois que l'écran redevient visible (ex : retour depuis le formulaire).
     */
    @Override
    protected void onResume() {
        // On appelle le onResume de la classe mère (obligatoire)
        super.onResume();
        // On vide la liste complète
        allPatients.clear();
        // On la remplit avec les patients enregistrés (données les plus récentes)
        allPatients.addAll(PatientStore.load(this));
        // On met à jour l'affichage
        refresh();
    }

    /**
     * Refait la liste affichée : filtre selon la recherche, trie, puis met à jour l'écran.
     */
    private void refresh() {
        // On lit le texte de recherche, sans espaces autour et en minuscules
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        // On vide la liste affichée pour la reconstruire
        shownPatients.clear();
        // On regarde chaque patient de la liste complète
        for (Patient p : allPatients) {
            // On garde le patient si la recherche est vide,
            // ou si son nom contient le texte, ou si son téléphone le contient
            if (query.isEmpty()
                    || p.nom.toLowerCase(Locale.ROOT).contains(query)
                    || p.telephone.contains(query)) {
                // On ajoute ce patient à la liste affichée
                shownPatients.add(p);
            }
        }
        // On trie la liste ; la lambda (a, b) compare deux patients pour dire lequel passe avant
        Collections.sort(shownPatients, (a, b) -> {
            // Tri par rendez-vous seulement si le mode est 1
            if (sortMode == 1) {
                // Nombre de jours entre aujourd'hui et le rendez-vous du patient a
                long da = DateUtils.daysFromToday(a.rdv);
                // Pareil pour le patient b
                long db = DateUtils.daysFromToday(b.rdv);
                // na = vrai si a n'a pas de date de rendez-vous
                boolean na = da == DateUtils.NO_DATE;
                // nb = vrai si b n'a pas de date de rendez-vous
                boolean nb = db == DateUtils.NO_DATE;
                // Si a n'a pas de date mais b en a une
                if (na && !nb) {
                    // a passe après b (les patients sans rendez-vous vont à la fin)
                    return 1;
                }
                // Si a a une date mais pas b
                if (!na && nb) {
                    // a passe avant b
                    return -1;
                }
                // Si les deux ont une date (na est faux, donc nb aussi)
                if (!na) {
                    // On compare les deux nombres de jours (le plus proche d'abord)
                    int c = Long.compare(da, db);
                    // Si les dates sont différentes
                    if (c != 0) {
                        // On renvoie le résultat de la comparaison
                        return c;
                    }
                }
            }
            // Sinon (ou si même date) : ordre alphabétique des noms,
            // sans tenir compte des majuscules
            return a.nom.compareToIgnoreCase(b.nom);
        });
        // On écrit dans le texte du tri le mode actuel
        tvSort.setText(sortMode == 0 ? "Tri : A → Z" : "Tri : par rendez-vous");
        // On prévient l'adapter que la liste a changé pour qu'il redessine les lignes
        adapter.notifyDataSetChanged();
        // Si aucun patient à afficher, on montre le message "vide", sinon on le cache
        tvEmpty.setVisibility(shownPatients.isEmpty() ? View.VISIBLE : View.GONE);
        // On affiche le nombre de patients affichés
        tvCount.setText(shownPatients.size() + " patient(s)");
    }

    /**
     * Demande confirmation, puis supprime le patient donné (et ses consultations).
     */
    private void confirmDelete(final Patient patient) {
        // Dialog : fenêtre qui s'ouvre au-dessus de l'écran. On la construit étape par étape
        new AlertDialog.Builder(this)
                // Titre de la fenêtre
                .setTitle("Supprimer le patient")
                // Message qui contient le nom du patient
                .setMessage("Voulez-vous supprimer " + patient.nom + " ?")
                // Bouton "Supprimer" ; la lambda s'exécute quand on clique dessus
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // DELETE : on supprime le patient dans la base de données
                    PatientStore.delete(PatientsActivity.this, patient.id);
                    // On supprime aussi toutes les consultations de ce patient
                    ConsultationStore.deleteForPatient(PatientsActivity.this, patient.id);
                    // On vide la liste complète
                    allPatients.clear();
                    // On la remplit avec la liste mise à jour
                    allPatients.addAll(PatientStore.load(PatientsActivity.this));
                    // On met à jour l'affichage
                    refresh();
                    // On affiche un petit message de confirmation
                    Toast.makeText(PatientsActivity.this, "Patient supprimé", Toast.LENGTH_SHORT).show();
                })
                // Bouton "Annuler" : null veut dire qu'on ne fait rien, la fenêtre se ferme
                .setNegativeButton("Annuler", null)
                // On affiche la fenêtre
                .show();
    }
}

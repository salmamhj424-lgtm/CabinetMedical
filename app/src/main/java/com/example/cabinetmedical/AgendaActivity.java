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

/**
 * Écran "Agenda" : il affiche la liste des rendez-vous des patients.
 * On peut filtrer : tous, aujourd'hui, ou à venir (3 onglets).
 * Utilise RdvAdapter (pour dessiner chaque ligne), PatientStore (les données) et DateUtils.
 * On y arrive depuis l'accueil (HomeActivity) ; un clic sur une ligne ouvre PatientDetailActivity.
 */
public class AgendaActivity extends AppCompatActivity {

    // Liste des patients actuellement affichés (après filtrage)
    private final List<Patient> shown = new ArrayList<>();
    // L'Adapter fait le lien entre la liste de données et la ListView (l'écran)
    private RdvAdapter adapter;
    // Onglet "Tous"
    private TextView tabAll;
    // Onglet "Aujourd'hui"
    private TextView tabToday;
    // Onglet "À venir"
    private TextView tabUpcoming;
    // Texte affiché quand la liste est vide
    private TextView tvEmpty;
    // Texte qui affiche le nombre de rendez-vous
    private TextView tvCount;
    // Filtre choisi : 0 = tous, 1 = aujourd'hui, 2 = à venir
    private int filter = 0;

    /**
     * Appelée par Android quand l'écran est créé : on prépare les vues et les clics.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appel obligatoire de la classe parente
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran (fichier XML activity_agenda)
        setContentView(R.layout.activity_agenda);

        // findViewById : on récupère les éléments du XML grâce à leur id
        tabAll = findViewById(R.id.tabAll);
        // Onglet "Aujourd'hui"
        tabToday = findViewById(R.id.tabToday);
        // Onglet "À venir"
        tabUpcoming = findViewById(R.id.tabUpcoming);
        // Texte "liste vide"
        tvEmpty = findViewById(R.id.tvAgendaEmpty);
        // Texte du compteur
        tvCount = findViewById(R.id.tvAgendaCount);
        // La ListView est la liste défilante qui contient les rendez-vous
        ListView lv = findViewById(R.id.lvAgenda);

        // On crée l'Adapter : il transforme chaque patient de "shown" en une ligne visible
        // "this" est le Context : l'écran actuel
        adapter = new RdvAdapter(this, shown);
        // On branche l'Adapter sur la liste
        lv.setAdapter(adapter);
        // Quand on clique sur une ligne de la liste (position = numéro de la ligne cliquée)
        lv.setOnItemClickListener((parent, view, position, id) -> {
            // Intent = message pour demander à Android d'ouvrir l'écran de détail du patient
            Intent intent = new Intent(AgendaActivity.this, PatientDetailActivity.class);
            // On glisse dans l'Intent l'id du patient cliqué, pour que l'autre écran le retrouve
            intent.putExtra("id", shown.get(position).id);
            // On ouvre l'écran de détail
            startActivity(intent);
        });

        // Clic sur "Tous" (lambda : petite fonction exécutée au clic)
        tabAll.setOnClickListener(v -> {
            // On choisit le filtre "tous"
            filter = 0;
            // On recalcule et réaffiche la liste
            refresh();
        });
        // Clic sur "Aujourd'hui"
        tabToday.setOnClickListener(v -> {
            // On choisit le filtre "aujourd'hui"
            filter = 1;
            // On réaffiche la liste
            refresh();
        });
        // Clic sur "À venir"
        tabUpcoming.setOnClickListener(v -> {
            // On choisit le filtre "à venir"
            filter = 2;
            // On réaffiche la liste
            refresh();
        });
    }

    /**
     * Appelée à chaque fois que l'écran redevient visible (ex. retour depuis un autre écran).
     */
    @Override
    protected void onResume() {
        // Appel obligatoire de la classe parente
        super.onResume();
        // On recharge la liste pour avoir des données à jour
        refresh();
    }

    /**
     * Change l'apparence d'un onglet selon qu'il est sélectionné ou non.
     */
    private void styleTab(TextView t, boolean selected) {
        // Si l'onglet est sélectionné...
        if (selected) {
            // ...on met le fond "sélectionné" (défini dans res/drawable)
            t.setBackgroundResource(R.drawable.bg_chip_sel);
            // ...et un texte blanc (0xFFFFFFFF = couleur blanche, format ARGB)
            t.setTextColor(0xFFFFFFFF);
        } else {
            // Sinon, fond normal
            t.setBackgroundResource(R.drawable.bg_input);
            // Et texte gris foncé
            t.setTextColor(0xFF212121);
        }
    }

    /**
     * Recalcule la liste des rendez-vous à afficher selon le filtre, puis met l'écran à jour.
     */
    private void refresh() {
        // On colore chaque onglet : seul celui du filtre actuel est "sélectionné"
        styleTab(tabAll, filter == 0);
        // Onglet "Aujourd'hui" sélectionné si filter vaut 1
        styleTab(tabToday, filter == 1);
        // Onglet "À venir" sélectionné si filter vaut 2
        styleTab(tabUpcoming, filter == 2);

        // On vide la liste affichée avant de la remplir à nouveau
        shown.clear();
        // On parcourt tous les patients enregistrés (PatientStore les lit dans le stockage)
        for (Patient p : PatientStore.load(this)) {
            // d = nombre de jours entre aujourd'hui et le rendez-vous (0 = aujourd'hui, >0 = futur)
            long d = DateUtils.daysFromToday(p.rdv);
            // Si le patient n'a pas de date de rendez-vous valide...
            if (d == DateUtils.NO_DATE) {
                // ...on passe au patient suivant (continue)
                continue;
            }
            // Si on veut seulement "aujourd'hui" et que le rendez-vous n'est pas aujourd'hui...
            if (filter == 1 && d != 0) {
                // ...on ignore ce patient
                continue;
            }
            // Si on veut seulement "à venir" et que le rendez-vous n'est pas dans le futur...
            if (filter == 2 && d <= 0) {
                // ...on ignore ce patient
                continue;
            }
            // Le patient passe tous les tests : on l'ajoute à la liste affichée
            shown.add(p);
        }
        // On trie la liste : les rendez-vous les plus proches en premier
        // (la lambda compare deux patients a et b selon leur nombre de jours)
        Collections.sort(shown, (a, b) -> Long.compare(
                DateUtils.daysFromToday(a.rdv), DateUtils.daysFromToday(b.rdv)));
        // On prévient l'Adapter que les données ont changé, pour qu'il redessine la liste
        adapter.notifyDataSetChanged();
        // Si la liste est vide on montre le texte "vide" (VISIBLE), sinon on le cache (GONE)
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        // On affiche le nombre de rendez-vous (shown.size() = taille de la liste)
        tvCount.setText(shown.size() + " rendez-vous");
    }
}

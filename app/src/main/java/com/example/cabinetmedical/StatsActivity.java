package com.example.cabinetmedical;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;

/**
 * Écran des statistiques du cabinet : chiffres clés et petits graphiques en barres.
 * Il montre les patients par tranche d'âge, les consultations par mois
 * et les revenus par mois (sur les 6 derniers mois).
 * Il utilise PatientStore, ConsultationStore et DateUtils.
 * On y arrive en cliquant sur le bouton "Statistiques" de HomeActivity.
 */
// Une Activity = un écran de l'application. AppCompatActivity est la version moderne.
public class StatsActivity extends AppCompatActivity {

    // Tableau (liste fixe) des noms de mois abrégés ; l'indice 0 = janvier, 11 = décembre
    private static final String[] MOIS = {"janv.", "févr.", "mars", "avr.", "mai", "juin",
            "juil.", "août", "sept.", "oct.", "nov.", "déc."};

    /**
     * Appelée une seule fois quand l'écran est créé : on calcule et on dessine les chiffres.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle d'abord le onCreate de la classe parente (obligatoire sur Android)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML activity_stats
        setContentView(R.layout.activity_stats);

        // findViewById : on récupère un élément du XML grâce à son id, pour pouvoir le modifier
        TextView tvKpi = findViewById(R.id.tvKpi);
        // On récupère la zone (verticale) qui contiendra les barres des âges
        LinearLayout llAges = findViewById(R.id.llAges);
        // On récupère la zone qui contiendra les barres des consultations par mois
        LinearLayout llMonths = findViewById(R.id.llMonths);
        // On récupère la zone qui contiendra les barres des revenus par mois
        LinearLayout llRevenue = findViewById(R.id.llRevenue);

        // On charge la liste des patients ("this" = cet écran, aussi appelé Context)
        List<Patient> patients = PatientStore.load(this);
        // On charge la liste de toutes les consultations
        List<Consultation> consultations = ConsultationStore.loadAll(this);

        // On écrit les chiffres clés : patients, consultations, argent encaissé et impayés
        tvKpi.setText("👥  " + patients.size() + " patients\n"
                + "🩺  " + consultations.size() + " consultations\n"
                + "💰  Encaissé : " + ConsultationStore.totalPaid(consultations) + " DT\n"
                + "⏳  Impayés : " + ConsultationStore.totalUnpaid(consultations) + " DT");

        // Tableau de 4 compteurs (tous à 0 au début), un par tranche d'âge
        int[] ages = new int[4];
        // On regarde les patients un par un
        for (Patient p : patients) {
            // Si le patient a moins de 18 ans
            if (p.age < 18) {
                // On ajoute 1 à la tranche 0-17 ans
                ages[0]++;
            // Sinon, si le patient a moins de 36 ans (donc entre 18 et 35)
            } else if (p.age < 36) {
                // On ajoute 1 à la tranche 18-35 ans
                ages[1]++;
            // Sinon, si le patient a moins de 61 ans (donc entre 36 et 60)
            } else if (p.age < 61) {
                // On ajoute 1 à la tranche 36-60 ans
                ages[2]++;
            } else {
                // Sinon il a 61 ans ou plus : on ajoute 1 à la tranche 60+
                ages[3]++;
            }
        }
        // On dessine le graphique des âges avec la méthode fill() écrite plus bas
        // Les 4 textes sont les noms des lignes, 0xFF1976D2 est la couleur bleue des barres
        fill(llAges, new String[]{"0 - 17 ans", "18 - 35 ans", "36 - 60 ans", "60+ ans"},
                ages, 0xFF1976D2);

        // Tableau des noms des 6 derniers mois
        String[] labels = new String[6];
        // Tableau du nombre de consultations pour chacun de ces mois
        int[] counts = new int[6];
        // Tableau des revenus pour chacun de ces mois
        int[] revenue = new int[6];
        // Nombre qui représente le mois actuel (année * 12 + mois)
        int currentKey = DateUtils.currentMonthKey();
        // Calendrier actuel (non utilisé ensuite dans le code)
        Calendar cal = Calendar.getInstance();
        // On boucle 6 fois : i va de 0 à 5 (du mois le plus ancien jusqu'au mois actuel)
        for (int i = 0; i < 6; i++) {
            // Pour i = 5 on obtient le mois actuel, pour i = 0 on recule de 5 mois
            int key = currentKey - (5 - i);
            // On cherche le nom du mois dans MOIS ;
            // le calcul avec % 12 donne un numéro de 0 à 11
            labels[i] = MOIS[((key % 12) + 12) % 12];
            // On regarde chaque consultation
            for (Consultation c : consultations) {
                // Si la consultation a eu lieu pendant ce mois
                if (DateUtils.monthKey(c.date) == key) {
                    // On ajoute 1 au nombre de consultations de ce mois
                    counts[i]++;
                    // Si elle est payée
                    if (c.paye) {
                        // On ajoute son montant aux revenus de ce mois
                        revenue[i] += c.montant;
                    }
                }
            }
        }
        // On dessine le graphique des consultations par mois (barres vertes)
        fill(llMonths, labels, counts, 0xFF00897B);
        // On dessine le graphique des revenus par mois (barres oranges)
        fill(llRevenue, labels, revenue, 0xFFEF6C00);
    }

    /**
     * Dessine un graphique en barres horizontales dans le conteneur "parent".
     * Chaque ligne contient : un nom, une barre colorée et la valeur.
     */
    private void fill(LinearLayout parent, String[] labels, int[] values, int color) {
        // On enlève les anciennes lignes pour ne pas les dessiner deux fois
        parent.removeAllViews();
        // La densité de l'écran sert à adapter les tailles en pixels à chaque téléphone
        float density = getResources().getDisplayMetrics().density;
        // Plus grande valeur du graphique (on part de 1 pour ne jamais diviser par 0)
        int max = 1;
        // On regarde chaque valeur
        for (int v : values) {
            // On garde la plus grande des deux : l'ancien max ou la valeur v
            max = Math.max(max, v);
        }
        // Longueur maximale d'une barre en pixels (170 "dp" multipliés par la densité)
        int maxBar = (int) (170 * density);
        // On crée une ligne par nom (labels)
        for (int i = 0; i < labels.length; i++) {
            // Une ligne : on crée un LinearLayout en code Java (au lieu du XML)
            LinearLayout row = new LinearLayout(this);
            // Les éléments de la ligne sont placés les uns à côté des autres
            row.setOrientation(LinearLayout.HORIZONTAL);
            // On centre les éléments de la ligne verticalement
            row.setGravity(Gravity.CENTER_VERTICAL);
            // On ajoute un peu d'espace en haut et en bas de la ligne
            // (les 4 valeurs sont : gauche, haut, droite, bas)
            row.setPadding(0, (int) (4 * density), 0, (int) (4 * density));

            // Le texte du nom de la ligne, créé en code
            TextView label = new TextView(this);
            // On écrit le nom (par exemple "janv.")
            label.setText(labels[i]);
            // On choisit la couleur du texte (gris foncé)
            label.setTextColor(0xFF444444);
            // On choisit la taille du texte
            label.setTextSize(13);
            // On fixe la largeur à 90 dp et la hauteur selon le contenu (WRAP_CONTENT)
            // pour que toutes les barres commencent au même endroit
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (90 * density), LinearLayout.LayoutParams.WRAP_CONTENT));

            // La barre est une simple vue (View) rectangulaire, sans texte
            View bar = new View(this);
            // On la colore avec la couleur donnée à la méthode
            bar.setBackgroundColor(color);
            // Calcul de la largeur de la barre :
            // si la valeur est 0, on met une toute petite barre de 2 dp ;
            // sinon la largeur est proportionnelle à la valeur, avec au moins 4 dp.
            // (long) évite un dépassement si le nombre est très grand.
            int width = values[i] == 0
                    ? (int) (2 * density)
                    : Math.max((int) (4 * density), (int) ((long) values[i] * maxBar / max));
            // On donne à la barre sa largeur calculée et une hauteur de 18 dp
            bar.setLayoutParams(new LinearLayout.LayoutParams(width, (int) (18 * density)));

            // Le texte qui affiche la valeur à droite de la barre
            TextView value = new TextView(this);
            // On convertit le nombre en texte pour l'afficher
            value.setText(String.valueOf(values[i]));
            // Couleur du texte (presque noir)
            value.setTextColor(0xFF212121);
            // Taille du texte
            value.setTextSize(13);
            // Paramètres de taille : largeur et hauteur selon le contenu
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            // On ajoute une marge de 8 dp à gauche pour séparer la valeur de la barre
            lp.setMargins((int) (8 * density), 0, 0, 0);
            // On applique ces paramètres au texte de la valeur
            value.setLayoutParams(lp);

            // On ajoute le nom à la ligne
            row.addView(label);
            // On ajoute la barre à la ligne
            row.addView(bar);
            // On ajoute la valeur à la ligne
            row.addView(value);
            // On ajoute la ligne terminée dans le conteneur affiché à l'écran
            parent.addView(row);
        }
    }
}

package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Écran d'accueil de l'application (tableau de bord du médecin).
 * Il affiche la date, un message de bienvenue, des statistiques rapides
 * (patients, rendez-vous, consultations, revenus) et le prochain rendez-vous.
 * Il utilise PatientStore, ConsultationStore, SettingsStore et DateUtils.
 * On y arrive depuis l'écran de connexion (MainActivity).
 */
// Une Activity = un écran de l'application. AppCompatActivity est la version moderne.
public class HomeActivity extends AppCompatActivity {

    // Zone de texte pour dire bonjour au médecin
    private TextView tvHello;
    // Zone de texte pour le nombre total de patients
    private TextView tvStatPatients;
    // Zone de texte pour le nombre de rendez-vous aujourd'hui
    private TextView tvStatToday;
    // Zone de texte pour le nombre de rendez-vous à venir
    private TextView tvStatUpcoming;
    // Zone de texte pour le nombre de consultations
    private TextView tvStatConsult;
    // Zone de texte pour l'argent (revenus et impayés)
    private TextView tvFinance;
    // Zone de texte pour le prochain rendez-vous
    private TextView tvNext;

    /**
     * Appelée une seule fois quand l'écran est créé : on prépare l'écran et les boutons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle d'abord le onCreate de la classe parente (obligatoire sur Android)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML activity_home
        setContentView(R.layout.activity_home);

        // On crée des patients de démonstration au premier lancement
        // ("this" = cet écran, aussi appelé Context : il donne accès aux fonctions Android)
        PatientStore.seedIfFirstLaunch(this);
        // On ajoute d'autres patients de démonstration (version 2)
        PatientStore.seedDemoV2(this);
        // On ajoute d'autres patients de démonstration (version 3)
        PatientStore.seedDemoV3(this);
        // On crée des consultations de démonstration
        ConsultationStore.seedDemo(this);
        // On ajoute d'autres consultations de démonstration (version 3)
        ConsultationStore.seedDemoV3(this);

        // findViewById : on récupère un élément du XML grâce à son id, pour pouvoir le modifier
        tvHello = findViewById(R.id.tvHello);
        // On récupère le texte du nombre de patients
        tvStatPatients = findViewById(R.id.tvStatPatients);
        // On récupère le texte des rendez-vous du jour
        tvStatToday = findViewById(R.id.tvStatToday);
        // On récupère le texte des rendez-vous à venir
        tvStatUpcoming = findViewById(R.id.tvStatUpcoming);
        // On récupère le texte des consultations
        tvStatConsult = findViewById(R.id.tvStatConsult);
        // On récupère le texte des finances
        tvFinance = findViewById(R.id.tvFinance);
        // On récupère le texte du prochain rendez-vous
        tvNext = findViewById(R.id.tvNext);
        // On récupère le texte de la date (variable locale : utilisée seulement ici)
        TextView tvDate = findViewById(R.id.tvDate);
        // On récupère le texte "Déconnexion" sur lequel on peut cliquer
        TextView tvLogout = findViewById(R.id.tvLogout);
        // On récupère le bouton qui ouvre la liste des patients
        Button btnPatients = findViewById(R.id.btnPatients);
        // On récupère le bouton qui ouvre l'agenda
        Button btnAgenda = findViewById(R.id.btnAgenda);
        // On récupère le bouton pour créer un nouveau patient
        Button btnNew = findViewById(R.id.btnNew);
        // On récupère le bouton qui ouvre les statistiques
        Button btnStats = findViewById(R.id.btnStats);
        // On récupère le bouton qui ouvre les paramètres
        Button btnSettings = findViewById(R.id.btnSettings);

        // On écrit la date du jour en français, par exemple "samedi 21 novembre 2026"
        String date = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE).format(new Date());
        // On vérifie que le texte n'est pas vide avant de le modifier
        if (!date.isEmpty()) {
            // On met la première lettre en majuscule : "Samedi 21 novembre 2026"
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        // On affiche la date dans la zone de texte
        tvDate.setText(date);

        // Quand on clique sur le bouton, on exécute le code après la flèche.
        // "v -> ..." est une lambda : une petite fonction écrite directement ici.
        btnPatients.setOnClickListener(v ->
                // Un Intent = un message qui dit à Android quel écran ouvrir
                // Ici on ouvre l'écran PatientsActivity
                startActivity(new Intent(HomeActivity.this, PatientsActivity.class)));
        // Clic sur le bouton agenda
        btnAgenda.setOnClickListener(v ->
                // On ouvre l'écran de l'agenda
                startActivity(new Intent(HomeActivity.this, AgendaActivity.class)));
        // Clic sur le bouton "nouveau patient"
        btnNew.setOnClickListener(v ->
                // On ouvre le formulaire de patient
                startActivity(new Intent(HomeActivity.this, PatientFormActivity.class)));
        // Clic sur le bouton des statistiques
        btnStats.setOnClickListener(v ->
                // On ouvre l'écran des statistiques
                startActivity(new Intent(HomeActivity.this, StatsActivity.class)));
        // Clic sur le bouton des paramètres
        btnSettings.setOnClickListener(v ->
                // On ouvre l'écran des paramètres
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        // Clic sur "Déconnexion" (avec des accolades car il y a plusieurs lignes de code)
        tvLogout.setOnClickListener(v -> {
            // On prépare le retour vers l'écran de connexion
            Intent logout = new Intent(HomeActivity.this, MainActivity.class);
            // Ces drapeaux (flags) effacent les anciens écrans : le bouton "retour"
            // ne permet plus de revenir à l'accueil après la déconnexion
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // On lance l'écran de connexion
            startActivity(logout);
        });
    }

    /**
     * Appelée à chaque fois que l'écran redevient visible : on met les chiffres à jour.
     */
    @Override
    protected void onResume() {
        // On appelle d'abord le onResume de la classe parente (obligatoire)
        super.onResume();
        // On lit le nom du médecin enregistré dans les paramètres ; trim() enlève les espaces
        String doctor = SettingsStore.getDoctor(this).trim();
        // On regarde si le nom commence déjà par "dr" ou "docteur" (majuscules ignorées)
        boolean aDejaDr = doctor.toLowerCase().startsWith("dr") || doctor.toLowerCase().startsWith("docteur");
        // Si le nom est vide : "Bonjour Docteur". Sinon : "Bonjour Dr Nom" (sans doubler "Dr")
        tvHello.setText(doctor.isEmpty() ? "Bonjour Docteur 👋" : "Bonjour " + (aDejaDr ? "" : "Dr ") + doctor + " 👋");

        // On charge la liste de tous les patients enregistrés
        List<Patient> patients = PatientStore.load(this);
        // Compteur des rendez-vous d'aujourd'hui
        int today = 0;
        // Compteur des rendez-vous à venir
        int upcoming = 0;
        // Le patient avec le rendez-vous le plus proche (null = on n'en a pas encore trouvé)
        Patient next = null;
        // Nombre de jours avant ce rendez-vous le plus proche (on part d'un énorme nombre)
        long nextDays = Long.MAX_VALUE;
        // On regarde les patients un par un
        for (Patient p : patients) {
            // On calcule le nombre de jours entre aujourd'hui et son rendez-vous
            long d = DateUtils.daysFromToday(p.rdv);
            // Si le patient n'a pas de date valide, ou si le rendez-vous est déjà passé (d < 0)
            if (d == DateUtils.NO_DATE || d < 0) {
                // On saute ce patient et on passe au suivant
                continue;
            }
            // Si le rendez-vous est aujourd'hui (0 jour d'écart)
            if (d == 0) {
                // On ajoute 1 au compteur d'aujourd'hui
                today++;
            } else {
                // Sinon c'est un rendez-vous futur : on ajoute 1 au compteur "à venir"
                upcoming++;
            }
            // Si ce rendez-vous est plus proche que le meilleur trouvé jusqu'ici
            if (d < nextDays) {
                // On retient le nombre de jours
                nextDays = d;
                // On retient aussi ce patient
                next = p;
            }
        }
        // On affiche le nombre de patients (String.valueOf transforme un nombre en texte)
        tvStatPatients.setText(String.valueOf(patients.size()));
        // On affiche le nombre de rendez-vous d'aujourd'hui
        tvStatToday.setText(String.valueOf(today));
        // On affiche le nombre de rendez-vous à venir
        tvStatUpcoming.setText(String.valueOf(upcoming));

        // On charge toutes les consultations enregistrées
        List<Consultation> all = ConsultationStore.loadAll(this);
        // On affiche le nombre de consultations
        tvStatConsult.setText("🩺  " + all.size() + " consultation(s) enregistrée(s)");

        // On calcule un nombre qui représente le mois en cours (année * 12 + mois)
        int month = DateUtils.currentMonthKey();
        // Total des revenus du mois, on part de 0
        int revenue = 0;
        // On regarde les consultations une par une
        for (Consultation c : all) {
            // Si la consultation est payée ET qu'elle date de ce mois-ci
            if (c.paye && DateUtils.monthKey(c.date) == month) {
                // On ajoute son montant au total
                revenue += c.montant;
            }
        }
        // On affiche les revenus du mois et le total des impayés (en dinars, DT)
        tvFinance.setText("💰  Revenus du mois : " + revenue + " DT\n⏳  Impayés : "
                + ConsultationStore.totalUnpaid(all) + " DT");

        // Si on n'a trouvé aucun rendez-vous à venir
        if (next == null) {
            // On affiche un message pour dire qu'il n'y en a pas
            tvNext.setText("📅  Aucun rendez-vous à venir");
        } else {
            // Sinon on affiche le nom du patient, la date et "Demain", "Dans 3 jours", etc.
            tvNext.setText("📅  Prochain rendez-vous : " + next.nom + "\n"
                    + next.rdv + " (" + DateUtils.describe(nextDays) + ")");
        }
    }
}

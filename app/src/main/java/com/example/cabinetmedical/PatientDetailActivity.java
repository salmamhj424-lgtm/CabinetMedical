// On déclare le "package" : le dossier logique où se trouve cette classe.
package com.example.cabinetmedical;

// Exception levée quand aucune appli du téléphone ne peut faire l'action demandée.
import android.content.ActivityNotFoundException;
// Intent : un "message" qui demande à Android d'ouvrir un écran ou une autre appli.
import android.content.Intent;
// Uri : une adresse (ex. "tel:123" pour un numéro de téléphone).
import android.net.Uri;
// Bundle : une petite boîte qui garde l'état de l'écran.
import android.os.Bundle;
// View : la classe de base de tout élément visible à l'écran.
import android.view.View;
// Button : un bouton sur lequel on peut appuyer.
import android.widget.Button;
// ListView : une liste d'éléments qu'on peut faire défiler.
import android.widget.ListView;
// TextView : un texte affiché à l'écran.
import android.widget.TextView;
// Toast : un petit message qui apparaît quelques secondes puis disparaît.
import android.widget.Toast;

// AlertDialog : une boîte de dialogue (petite fenêtre qui pose une question ou propose un choix).
import androidx.appcompat.app.AlertDialog;
// AppCompatActivity : la classe de base d'un écran de l'appli.
import androidx.appcompat.app.AppCompatActivity;

// ArrayList : une liste dont la taille peut changer.
import java.util.ArrayList;
// List : le type général "liste".
import java.util.List;

/**
 * Écran qui affiche la fiche détaillée d'un patient et son historique de consultations.
 * Une Activity est un écran de l'application.
 * Elle utilise PatientStore, ConsultationStore et ConsultationAdapter (qui remplit la liste).
 * On y arrive depuis la liste des patients, avec l'id du patient dans un Intent.
 * Depuis ici, on peut appeler, envoyer un SMS, modifier le patient ou ajouter une consultation.
 */
public class PatientDetailActivity extends AppCompatActivity {

    // L'id du patient affiché (reçu de l'écran précédent).
    private String patientId;
    // Les informations du patient affiché (null tant qu'on ne l'a pas trouvé).
    private Patient patient;
    // La liste des consultations de ce patient ; elle alimente la ListView.
    private final List<Consultation> consultations = new ArrayList<>();
    // L'Adapter : l'objet qui transforme chaque consultation en une ligne visible dans la liste.
    private ConsultationAdapter adapter;

    // Les zones de texte de l'écran, définies dans le fichier XML de mise en page.
    // Initiales du patient.
    private TextView tvInitials;
    // Nom du patient.
    private TextView tvNom;
    // Âge du patient.
    private TextView tvAge;
    // Téléphone du patient.
    private TextView tvTel;
    // Prochain rendez-vous.
    private TextView tvRdv;
    // Notes médicales.
    private TextView tvNotes;
    // Groupe sanguin.
    private TextView tvGroupe;
    // Allergies.
    private TextView tvAllergies;
    // Résumé de l'argent payé et impayé.
    private TextView tvFinance;
    // Message "aucune consultation" quand la liste est vide.
    private TextView tvNoConsult;

    /**
     * Appelée par Android à la création de l'écran : on prépare les vues et les boutons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle d'abord le code de la classe parente (obligatoire).
        super.onCreate(savedInstanceState);
        // On affiche le fichier XML de mise en page de cet écran.
        setContentView(R.layout.activity_patient_detail);

        // On lit l'id du patient envoyé dans l'Intent par l'écran précédent.
        patientId = getIntent().getStringExtra("id");

        // findViewById : on récupère chaque élément défini dans le XML grâce à son id.
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
        // Bouton pour appeler le patient.
        Button btnCall = findViewById(R.id.btnCall);
        // Bouton pour envoyer un SMS.
        Button btnSms = findViewById(R.id.btnSms);
        // Bouton pour modifier la fiche du patient.
        Button btnEdit = findViewById(R.id.btnEdit);
        // Bouton pour ajouter une consultation.
        Button btnAddConsult = findViewById(R.id.btnAddConsult);
        // La liste qui affichera les consultations.
        ListView lv = findViewById(R.id.lvConsult);

        // On crée l'adapter ; "this" est le Context (l'écran actuel, qui donne accès à Android).
        adapter = new ConsultationAdapter(this, consultations);
        // On relie l'adapter à la liste : elle affichera donc nos consultations.
        lv.setAdapter(adapter);
        // On définit ce qui se passe quand on appuie sur une ligne de la liste.
        // La lambda (parent, view, position, id) -> ... est une mini-fonction exécutée au clic.
        lv.setOnItemClickListener((parent, view, position, id) ->
                // On ouvre le menu d'options pour la consultation touchée (position dans la liste).
                showConsultationOptions(consultations.get(position)));

        // Clic sur "Appeler" : la lambda v -> ... s'exécute quand on appuie (v = le bouton).
        // ACTION_DIAL ouvre le composeur avec le numéro, sans lancer l'appel.
        btnCall.setOnClickListener(v -> launch(Intent.ACTION_DIAL, "tel:"));
        // Clic sur "SMS" : ACTION_SENDTO ouvre l'appli de messages vers ce numéro.
        btnSms.setOnClickListener(v -> launch(Intent.ACTION_SENDTO, "smsto:"));
        // Clic sur "Modifier" : on ouvre l'écran du formulaire patient.
        btnEdit.setOnClickListener(v -> {
            // On crée un Intent : de cet écran vers PatientFormActivity.
            Intent intent = new Intent(PatientDetailActivity.this, PatientFormActivity.class);
            // On joint l'id du patient pour que le formulaire sache lequel modifier.
            intent.putExtra("id", patientId);
            // On lance le nouvel écran.
            startActivity(intent);
        });
        // Clic sur "Ajouter une consultation" : on ouvre le formulaire de consultation.
        btnAddConsult.setOnClickListener(v -> {
            // On crée un Intent vers ConsultationFormActivity.
            Intent intent = new Intent(PatientDetailActivity.this,
                    ConsultationFormActivity.class);
            // On joint l'id du patient concerné par la nouvelle consultation.
            intent.putExtra("patientId", patientId);
            // On lance le nouvel écran.
            startActivity(intent);
        });
    }

    /**
     * Ouvre une autre appli (téléphone ou SMS) avec le numéro du patient.
     * action : ce qu'on veut faire ; scheme : le début de l'adresse ("tel:" ou "smsto:").
     */
    private void launch(String action, String scheme) {
        // Si on n'a pas encore les infos du patient (null = rien)...
        if (patient == null) {
            // ...on ne peut rien faire : on quitte la méthode.
            return;
        }
        // try : on essaie, car il se peut qu'aucune appli ne sache faire cette action.
        try {
            // On crée l'Intent avec l'adresse (ex. "tel:" + numéro) et on le lance.
            startActivity(new Intent(action, Uri.parse(scheme + patient.telephone)));
        // catch : aucune appli trouvée, on prévient l'utilisateur.
        } catch (ActivityNotFoundException e) {
            // Toast : petit message temporaire affiché en bas de l'écran.
            Toast.makeText(this, "Action non disponible sur cet appareil",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Appelée à chaque fois que l'écran redevient visible : on rafraîchit les informations.
     */
    @Override
    protected void onResume() {
        // On appelle d'abord le code de la classe parente (obligatoire).
        super.onResume();
        // On repart de zéro : on n'a pas encore trouvé le patient.
        patient = null;
        // On parcourt tous les patients enregistrés.
        for (Patient p : PatientStore.load(this)) {
            // Si l'id de ce patient est celui qu'on affiche...
            if (p.id.equals(patientId)) {
                // ...on garde ce patient.
                patient = p;
                // On l'a trouvé : on arrête la boucle.
                break;
            }
        }
        // Si le patient est introuvable (par exemple supprimé)...
        if (patient == null) {
            // ...on ferme cet écran.
            finish();
            // Et on quitte la méthode.
            return;
        }
        // On affiche les initiales du patient.
        tvInitials.setText(patient.initials());
        // On affiche son nom.
        tvNom.setText(patient.nom);
        // On affiche son âge ; le + colle des textes ensemble (nombre + " ans").
        tvAge.setText(patient.age + " ans");
        // On affiche son téléphone, avec une petite icône au début.
        tvTel.setText("📞  " + patient.telephone);
        // Si le champ du rendez-vous est vide...
        if (patient.rdv.isEmpty()) {
            // ...on affiche qu'il n'y a pas de rendez-vous.
            tvRdv.setText("📅  Aucun rendez-vous prévu");
        } else {
            // Sinon, on calcule combien de jours séparent aujourd'hui du rendez-vous.
            long d = DateUtils.daysFromToday(patient.rdv);
            // On affiche la date du RDV ; si la date est valide, on ajoute une description.
            // (condition ? a : b) veut dire : si la condition est vraie on prend a, sinon b.
            tvRdv.setText("📅  Prochain RDV : " + patient.rdv
                    + (d == DateUtils.NO_DATE ? "" : " (" + DateUtils.describe(d) + ")"));
        }
        // On affiche les notes ; s'il n'y en a pas, on affiche un texte par défaut.
        tvNotes.setText(patient.notes.isEmpty()
                ? "📝  Aucune note médicale" : "📝  " + patient.notes);
        // Si le groupe sanguin n'est pas renseigné...
        if (patient.groupe.isEmpty()) {
            // ...on cache complètement cette ligne (GONE = invisible et sans place).
            tvGroupe.setVisibility(View.GONE);
        } else {
            // Sinon, on rend la ligne visible.
            tvGroupe.setVisibility(View.VISIBLE);
            // Et on y écrit le groupe sanguin.
            tvGroupe.setText("🩸  Groupe sanguin : " + patient.groupe);
        }
        // Si aucune allergie n'est renseignée...
        if (patient.allergies.isEmpty()) {
            // ...on cache la ligne des allergies.
            tvAllergies.setVisibility(View.GONE);
        } else {
            // Sinon, on rend la ligne visible.
            tvAllergies.setVisibility(View.VISIBLE);
            // Et on y écrit les allergies.
            tvAllergies.setText("⚠️  Allergies : " + patient.allergies);
        }
        // On recharge la liste des consultations du patient.
        loadConsultations();
    }

    /**
     * Recharge les consultations du patient et met à jour la liste et le résumé financier.
     */
    private void loadConsultations() {
        // On vide l'ancienne liste.
        consultations.clear();
        // On la remplit avec les consultations enregistrées pour ce patient.
        consultations.addAll(ConsultationStore.loadFor(this, patientId));
        // On prévient l'adapter que les données ont changé pour qu'il redessine la liste.
        adapter.notifyDataSetChanged();
        // On montre le message "aucune consultation" seulement si la liste est vide.
        tvNoConsult.setVisibility(consultations.isEmpty() ? View.VISIBLE : View.GONE);
        // On affiche le total payé et le total impayé (en dinars tunisiens, DT).
        tvFinance.setText("💰  Payé : " + ConsultationStore.totalPaid(consultations)
                + " DT   |   Impayé : " + ConsultationStore.totalUnpaid(consultations) + " DT");
    }

    /**
     * Affiche un menu de choix (partager, changer l'état de paiement, supprimer).
     */
    private void showConsultationOptions(final Consultation c) {
        // Tableau de textes : ce sont les choix proposés dans le menu.
        String[] options = {
                "📤  Partager l'ordonnance",
                // Le 2e choix change selon l'état :
                // si c'est payé on propose "non payée", sinon "payée".
                c.paye ? "↩️  Marquer comme non payée" : "✅  Marquer comme payée",
                "🗑️  Supprimer"
        };
        // On construit une boîte de dialogue (AlertDialog) avec un "Builder".
        new AlertDialog.Builder(this)
                // Le titre de la boîte, avec la date de la consultation.
                .setTitle("Consultation du " + c.date)
                // On affiche la liste des choix ; la lambda s'exécute quand on en touche un.
                // "which" est la position du choix touché (0 = premier, 1 = deuxième...).
                .setItems(options, (dialog, which) -> {
                    // Si l'utilisateur a choisi le premier choix (partager)...
                    if (which == 0) {
                        // ...on partage l'ordonnance.
                        shareOrdonnance(c);
                    // Sinon, si c'est le deuxième choix (changer l'état de paiement)...
                    } else if (which == 1) {
                        // On remplace la consultation par une copie
                        // avec l'état de paiement inversé.
                        // !c.paye veut dire "le contraire de c.paye".
                        ConsultationStore.update(PatientDetailActivity.this,
                                new Consultation(c.id, c.patientId, c.date, c.motif,
                                        c.diagnostic, c.traitement, c.montant, !c.paye));
                        // On recharge la liste pour voir le changement.
                        loadConsultations();
                    } else {
                        // Sinon (troisième choix) : on demande confirmation avant de supprimer.
                        confirmDeleteConsultation(c);
                    }
                })
                // On affiche la boîte de dialogue à l'écran.
                .show();
    }

    /**
     * Prépare le texte de l'ordonnance et l'envoie vers une autre appli (WhatsApp, mail...).
     */
    private void shareOrdonnance(Consultation c) {
        // Si le patient n'est pas chargé...
        if (patient == null) {
            // ...on ne peut pas faire l'ordonnance : on quitte.
            return;
        }
        // StringBuilder : outil pour construire un long texte morceau par morceau.
        StringBuilder sb = new StringBuilder();
        // Titre de l'ordonnance ; \n veut dire "retour à la ligne".
        sb.append("CABINET MÉDICAL - ORDONNANCE\n\n");
        // Nom et âge du patient.
        sb.append("Patient : ").append(patient.nom).append(" (").append(patient.age)
                .append(" ans)\n");
        // Date de la consultation.
        sb.append("Date : ").append(c.date).append("\n");
        // Motif (la raison de la visite).
        sb.append("Motif : ").append(c.motif).append("\n");
        // Si un diagnostic existe (! = "non", donc "pas vide")...
        if (!c.diagnostic.isEmpty()) {
            // ...on l'ajoute au texte.
            sb.append("Diagnostic : ").append(c.diagnostic).append("\n");
        }
        // Si un traitement existe...
        if (!c.traitement.isEmpty()) {
            // ...on l'ajoute au texte.
            sb.append("Traitement : ").append(c.traitement).append("\n");
        }
        // Si le patient a des allergies...
        if (!patient.allergies.isEmpty()) {
            // ...on les ajoute au texte.
            sb.append("Allergies : ").append(patient.allergies).append("\n");
        }
        // On crée un Intent de type "envoyer" (ACTION_SEND) pour partager du contenu.
        Intent send = new Intent(Intent.ACTION_SEND);
        // On précise que le contenu est du texte simple.
        send.setType("text/plain");
        // On ajoute un objet (sujet), utile par exemple pour un e-mail.
        send.putExtra(Intent.EXTRA_SUBJECT, "Ordonnance - " + patient.nom);
        // On ajoute le texte de l'ordonnance, converti en String.
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        // On affiche le sélecteur Android pour choisir avec quelle appli partager.
        startActivity(Intent.createChooser(send, "Partager l'ordonnance"));
    }

    /**
     * Demande « Êtes-vous sûr ? » avant de supprimer une consultation.
     */
    private void confirmDeleteConsultation(final Consultation c) {
        // On construit une boîte de dialogue de confirmation.
        new AlertDialog.Builder(this)
                // Le titre de la boîte.
                .setTitle("Supprimer la consultation")
                // La question posée à l'utilisateur.
                .setMessage("Voulez-vous supprimer la consultation du " + c.date + " ?")
                // Bouton "Supprimer" ; la lambda s'exécute quand on appuie dessus.
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // On supprime la consultation de la sauvegarde.
                    ConsultationStore.delete(PatientDetailActivity.this, c.id);
                    // On recharge la liste pour qu'elle soit à jour.
                    loadConsultations();
                    // On affiche un petit message pour confirmer la suppression.
                    Toast.makeText(PatientDetailActivity.this, "Consultation supprimée",
                            Toast.LENGTH_SHORT).show();
                })
                // Bouton "Annuler" ; null = on ne fait rien, la boîte se ferme simplement.
                .setNegativeButton("Annuler", null)
                // On affiche la boîte de dialogue à l'écran.
                .show();
    }
}

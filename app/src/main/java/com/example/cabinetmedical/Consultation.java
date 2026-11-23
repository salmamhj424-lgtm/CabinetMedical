package com.example.cabinetmedical;

/**
 * Modèle de données : un objet Consultation représente une visite d'un patient chez le médecin.
 * Il ne fait que stocker des informations (date, motif, diagnostic, traitement, montant...).
 * "final" veut dire qu'une valeur ne change plus après la création de l'objet.
 * Il est créé par ConsultationFormActivity et géré par ConsultationStore.
 */
public class Consultation {
    // Identifiant unique de la consultation
    public final String id;
    // Identifiant du patient concerné (le lien avec la classe Patient)
    public final String patientId;
    // Date de la consultation (texte jj/mm/aaaa)
    public final String date;
    // Motif : la raison de la visite
    public final String motif;
    // Diagnostic donné par le médecin
    public final String diagnostic;
    // Traitement prescrit
    public final String traitement;
    // Prix de la consultation en dinars (DT)
    public final int montant;
    // true si la consultation est payée, false sinon
    public final boolean paye;

    /**
     * Premier constructeur (version courte) : sans montant et non payée.
     */
    public Consultation(String id, String patientId, String date, String motif,
                        String diagnostic, String traitement) {
        // this(...) appelle l'autre constructeur avec montant = 0 et paye = false
        this(id, patientId, date, motif, diagnostic, traitement, 0, false);
    }

    /**
     * Deuxième constructeur (version complète) : il reçoit toutes les informations.
     */
    public Consultation(String id, String patientId, String date, String motif,
                        String diagnostic, String traitement, int montant, boolean paye) {
        // "this.id" = le champ de la classe ; "id" = la valeur reçue en paramètre
        this.id = id;
        // On enregistre l'identifiant du patient
        this.patientId = patientId;
        // On enregistre la date
        this.date = date;
        // On enregistre le motif
        this.motif = motif;
        // On enregistre le diagnostic
        this.diagnostic = diagnostic;
        // On enregistre le traitement
        this.traitement = traitement;
        // On enregistre le montant
        this.montant = montant;
        // On enregistre l'état de paiement
        this.paye = paye;
    }
}

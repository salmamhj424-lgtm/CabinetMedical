package com.example.cabinetmedical;

/**
 * Modèle de données : un objet Patient représente un patient du cabinet.
 * Il ne fait que stocker des informations (nom, âge, téléphone, rendez-vous...).
 * "final" veut dire qu'une valeur ne change plus après la création du patient.
 * Il est créé et lu par PatientStore, et affiché par PatientAdapter et RdvAdapter.
 */
public class Patient {
    // Identifiant unique du patient
    public final String id;
    // Nom complet
    public final String nom;
    // Âge en années
    public final int age;
    // Numéro de téléphone
    public final String telephone;
    // Notes libres du médecin
    public final String notes;
    // Date du prochain rendez-vous (texte jj/mm/aaaa)
    public final String rdv;
    // Groupe sanguin
    public final String groupe;
    // Allergies connues
    public final String allergies;

    /**
     * Premier constructeur (version courte) : sans groupe sanguin ni allergies.
     */
    public Patient(String id, String nom, int age, String telephone, String notes, String rdv) {
        // this(...) appelle l'autre constructeur, avec des textes vides pour groupe et allergies
        this(id, nom, age, telephone, notes, rdv, "", "");
    }

    /**
     * Deuxième constructeur (version complète) : il reçoit toutes les informations.
     */
    public Patient(String id, String nom, int age, String telephone, String notes, String rdv,
                   String groupe, String allergies) {
        // "this.id" = le champ de la classe ; "id" = la valeur reçue en paramètre
        this.id = id;
        // On enregistre le nom
        this.nom = nom;
        // On enregistre l'âge
        this.age = age;
        // On enregistre le téléphone
        this.telephone = telephone;
        // On enregistre les notes
        this.notes = notes;
        // On enregistre la date du rendez-vous
        this.rdv = rdv;
        // Si groupe vaut null, on met un texte vide pour éviter les erreurs plus tard
        this.groupe = groupe == null ? "" : groupe;
        // Même chose pour les allergies
        this.allergies = allergies == null ? "" : allergies;
    }

    /**
     * Renvoie les initiales du patient (max 2 lettres), par exemple "Selma Mahjoub" -> "SM".
     */
    public String initials() {
        // On coupe le nom en mots : trim() enlève les espaces autour, split coupe aux espaces
        String[] parts = nom.trim().split("\\s+");
        // Le résultat commence vide, on y ajoutera les lettres
        String result = "";
        // On parcourt les mots, mais on s'arrête après 2 mots maximum
        for (int i = 0; i < parts.length && i < 2; i++) {
            // Si le mot n'est pas vide...
            if (!parts[i].isEmpty()) {
                // ...on ajoute sa première lettre en majuscule
                result += parts[i].substring(0, 1).toUpperCase();
            }
        }
        // S'il n'y a aucune lettre on renvoie "?", sinon les initiales
        return result.isEmpty() ? "?" : result;
    }
}

package com.example.cabinetmedical;

public class Patient {
    public final String id;
    public final String nom;
    public final int age;
    public final String telephone;
    public final String notes;
    public final String rdv;
    public final String groupe;
    public final String allergies;

    public Patient(String id, String nom, int age, String telephone, String notes, String rdv) {
        this(id, nom, age, telephone, notes, rdv, "", "");
    }

    public Patient(String id, String nom, int age, String telephone, String notes, String rdv,
                   String groupe, String allergies) {
        this.id = id;
        this.nom = nom;
        this.age = age;
        this.telephone = telephone;
        this.notes = notes;
        this.rdv = rdv;
        this.groupe = groupe == null ? "" : groupe;
        this.allergies = allergies == null ? "" : allergies;
    }

    public String initials() {
        String[] parts = nom.trim().split("\\s+");
        String result = "";
        for (int i = 0; i < parts.length && i < 2; i++) {
            if (!parts[i].isEmpty()) {
                result += parts[i].substring(0, 1).toUpperCase();
            }
        }
        return result.isEmpty() ? "?" : result;
    }
}

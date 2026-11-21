package com.example.cabinetmedical;

public class Consultation {
    public final String id;
    public final String patientId;
    public final String date;
    public final String motif;
    public final String diagnostic;
    public final String traitement;
    public final int montant;
    public final boolean paye;

    public Consultation(String id, String patientId, String date, String motif,
                        String diagnostic, String traitement) {
        this(id, patientId, date, motif, diagnostic, traitement, 0, false);
    }

    public Consultation(String id, String patientId, String date, String motif,
                        String diagnostic, String traitement, int montant, boolean paye) {
        this.id = id;
        this.patientId = patientId;
        this.date = date;
        this.motif = motif;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.montant = montant;
        this.paye = paye;
    }
}

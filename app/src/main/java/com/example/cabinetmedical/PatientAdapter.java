package com.example.cabinetmedical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Adapter de la liste des patients.
 * Un Adapter transforme chaque objet Patient en une ligne visible (item_patient.xml).
 * Chaque ligne montre les initiales, le nom, l'âge, le téléphone et le prochain rendez-vous.
 * Il est utilisé par l'écran de la liste des patients (PatientsActivity).
 */
public class PatientAdapter extends ArrayAdapter<Patient> {

    /**
     * Constructeur : il reçoit le Context (l'écran) et la liste des patients à afficher.
     */
    public PatientAdapter(Context context, List<Patient> items) {
        // On passe tout à la classe parente ; 0 = pas de layout par défaut (voir getView)
        super(context, 0, items);
    }

    /**
     * Appelée par la liste pour chaque ligne : elle renvoie la vue de la ligne.
     * position = numéro de la ligne ; convertView = ancienne ligne réutilisable (ou null).
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // On part de la ligne recyclée (réutiliser une ligne rend la liste plus rapide)
        View v = convertView;
        // S'il n'y a pas de ligne à réutiliser...
        if (v == null) {
            // ...on en crée une à partir du fichier XML item_patient (LayoutInflater)
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_patient, parent, false);
        }
        // On récupère le patient qui correspond à cette ligne
        Patient p = getItem(position);
        // On récupère chaque TextView de la ligne grâce à son id
        TextView tvInitials = v.findViewById(R.id.tvInitials);
        // Texte du nom
        TextView tvNom = v.findViewById(R.id.tvNom);
        // Texte des détails (âge et téléphone)
        TextView tvDetails = v.findViewById(R.id.tvDetails);
        // Texte du rendez-vous
        TextView tvRdv = v.findViewById(R.id.tvRdv);

        // Si le patient existe bien (pas null)...
        if (p != null) {
            // ...on affiche ses initiales (ex. "SM")
            tvInitials.setText(p.initials());
            // On affiche son nom
            tvNom.setText(p.nom);
            // On affiche son âge et son téléphone
            tvDetails.setText(p.age + " ans - " + p.telephone);
            // Si le patient n'a pas de rendez-vous (null ou texte vide)...
            if (p.rdv == null || p.rdv.isEmpty()) {
                // ...on cache la ligne du rendez-vous
                tvRdv.setVisibility(View.GONE);
            } else {
                // Sinon on la rend visible
                tvRdv.setVisibility(View.VISIBLE);
                // Et on écrit la date du prochain rendez-vous
                tvRdv.setText("Prochain RDV : " + p.rdv);
            }
        }
        // On renvoie la ligne prête à être affichée
        return v;
    }
}

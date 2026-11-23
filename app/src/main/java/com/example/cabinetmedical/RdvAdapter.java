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
 * Adapter de la liste des rendez-vous (agenda).
 * Un Adapter transforme chaque objet Patient en une ligne visible (item_rdv.xml).
 * Chaque ligne montre la date du RDV, son statut (ex. "demain"), le nom et le téléphone.
 * Il utilise DateUtils et il est utilisé par AgendaActivity.
 */
public class RdvAdapter extends ArrayAdapter<Patient> {

    /**
     * Constructeur : il reçoit le Context (l'écran) et la liste des patients à afficher.
     */
    public RdvAdapter(Context context, List<Patient> items) {
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
            // ...on en crée une à partir du fichier XML item_rdv (LayoutInflater)
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_rdv, parent, false);
        }
        // On récupère le patient qui correspond à cette ligne
        Patient p = getItem(position);
        // On récupère chaque TextView de la ligne grâce à son id
        TextView tvDate = v.findViewById(R.id.tvRdvDate);
        // Texte du statut (ex. "Aujourd'hui", "Dans 3 jours")
        TextView tvStatus = v.findViewById(R.id.tvRdvStatus);
        // Texte du nom
        TextView tvNom = v.findViewById(R.id.tvRdvNom);
        // Texte du téléphone
        TextView tvTel = v.findViewById(R.id.tvRdvTel);
        // Si le patient existe bien (pas null)...
        if (p != null) {
            // ...d = nombre de jours entre aujourd'hui et le rendez-vous
            long d = DateUtils.daysFromToday(p.rdv);
            // On affiche la date du rendez-vous
            tvDate.setText(p.rdv);
            // On affiche une description en mots (ex. "demain") calculée à partir de d
            tvStatus.setText(DateUtils.describe(d));
            // On colore le statut selon l'urgence (couleur choisie par DateUtils)
            tvStatus.setTextColor(DateUtils.color(d));
            // On affiche le nom du patient
            tvNom.setText(p.nom);
            // On affiche le téléphone avec une icône
            tvTel.setText("📞 " + p.telephone);
        }
        // On renvoie la ligne prête à être affichée
        return v;
    }
}

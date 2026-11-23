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
 * Adapter de la liste des consultations d'un patient.
 * Un Adapter est un "traducteur" : il transforme chaque objet Consultation
 * en une ligne visible (layout item_consultation.xml) dans une ListView.
 * Il est utilisé par l'écran de détail du patient (PatientDetailActivity).
 */
public class ConsultationAdapter extends ArrayAdapter<Consultation> {

    /**
     * Constructeur : il reçoit le Context (l'écran) et la liste des consultations à afficher.
     */
    public ConsultationAdapter(Context context, List<Consultation> items) {
        // On passe tout à la classe parente ; le 0 veut dire "pas de layout par défaut"
        // car on dessine nous-mêmes les lignes dans getView
        super(context, 0, items);
    }

    /**
     * Appelée par la liste pour chaque ligne à afficher : elle doit renvoyer la vue de la ligne.
     * position = numéro de la ligne ; convertView = ancienne ligne réutilisable (ou null).
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // On part de la ligne recyclée (réutiliser une ligne rend la liste plus rapide)
        View v = convertView;
        // S'il n'y a pas de ligne à réutiliser, il faut en créer une...
        if (v == null) {
            // ...LayoutInflater transforme le fichier XML item_consultation en vraie vue
            v = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_consultation, parent, false);
        }
        // On récupère la consultation qui correspond à cette ligne
        Consultation c = getItem(position);
        // On récupère chaque TextView de la ligne grâce à son id
        TextView tvDate = v.findViewById(R.id.tvCDate);
        // Texte du motif
        TextView tvMotif = v.findViewById(R.id.tvCMotif);
        // Texte du diagnostic
        TextView tvDiag = v.findViewById(R.id.tvCDiag);
        // Texte du traitement
        TextView tvTrait = v.findViewById(R.id.tvCTrait);
        // Texte du montant
        TextView tvMontant = v.findViewById(R.id.tvCMontant);
        // Si la consultation existe bien (pas null)...
        if (c != null) {
            // ...on affiche la date avec une petite icône calendrier
            tvDate.setText("📅 " + c.date);
            // On affiche le motif
            tvMotif.setText(c.motif);
            // On affiche le diagnostic (la méthode show le cache s'il est vide)
            show(tvDiag, "Diagnostic : ", c.diagnostic);
            // Pareil pour le traitement
            show(tvTrait, "Traitement : ", c.traitement);
            // Si un montant a été saisi (plus grand que 0)...
            if (c.montant > 0) {
                // ...on rend le texte du montant visible
                tvMontant.setVisibility(View.VISIBLE);
                // On écrit le montant, puis "Payée" ou "Non payée" selon c.paye
                // (condition ? valeur si vrai : valeur si faux)
                tvMontant.setText("💰 " + c.montant + " DT  -  "
                        + (c.paye ? "Payée ✅" : "Non payée ⏳"));
                // Couleur : vert si payée, rouge sinon
                tvMontant.setTextColor(c.paye ? 0xFF2E7D32 : 0xFFD32F2F);
            } else {
                // Sinon (pas de montant), on cache ce texte (GONE = ne prend pas de place)
                tvMontant.setVisibility(View.GONE);
            }
        }
        // On renvoie la ligne prête à être affichée
        return v;
    }

    /**
     * Affiche "label + valeur" dans un TextView, ou le cache si la valeur est vide.
     */
    private void show(TextView tv, String label, String value) {
        // Si la valeur n'existe pas ou est vide...
        if (value == null || value.isEmpty()) {
            // ...on cache le texte
            tv.setVisibility(View.GONE);
        } else {
            // Sinon on le rend visible
            tv.setVisibility(View.VISIBLE);
            // Et on écrit l'étiquette suivie de la valeur
            tv.setText(label + value);
        }
    }
}

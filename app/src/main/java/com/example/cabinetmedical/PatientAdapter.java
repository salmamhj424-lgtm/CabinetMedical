package com.example.cabinetmedical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class PatientAdapter extends ArrayAdapter<Patient> {

    public PatientAdapter(Context context, List<Patient> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_patient, parent, false);
        }
        Patient p = getItem(position);
        TextView tvInitials = v.findViewById(R.id.tvInitials);
        TextView tvNom = v.findViewById(R.id.tvNom);
        TextView tvDetails = v.findViewById(R.id.tvDetails);
        TextView tvRdv = v.findViewById(R.id.tvRdv);

        if (p != null) {
            tvInitials.setText(p.initials());
            tvNom.setText(p.nom);
            tvDetails.setText(p.age + " ans - " + p.telephone);
            if (p.rdv == null || p.rdv.isEmpty()) {
                tvRdv.setVisibility(View.GONE);
            } else {
                tvRdv.setVisibility(View.VISIBLE);
                tvRdv.setText("Prochain RDV : " + p.rdv);
            }
        }
        return v;
    }
}

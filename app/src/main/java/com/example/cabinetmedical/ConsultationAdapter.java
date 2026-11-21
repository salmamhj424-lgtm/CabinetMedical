package com.example.cabinetmedical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ConsultationAdapter extends ArrayAdapter<Consultation> {

    public ConsultationAdapter(Context context, List<Consultation> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_consultation, parent, false);
        }
        Consultation c = getItem(position);
        TextView tvDate = v.findViewById(R.id.tvCDate);
        TextView tvMotif = v.findViewById(R.id.tvCMotif);
        TextView tvDiag = v.findViewById(R.id.tvCDiag);
        TextView tvTrait = v.findViewById(R.id.tvCTrait);
        TextView tvMontant = v.findViewById(R.id.tvCMontant);
        if (c != null) {
            tvDate.setText("📅 " + c.date);
            tvMotif.setText(c.motif);
            show(tvDiag, "Diagnostic : ", c.diagnostic);
            show(tvTrait, "Traitement : ", c.traitement);
            if (c.montant > 0) {
                tvMontant.setVisibility(View.VISIBLE);
                tvMontant.setText("💰 " + c.montant + " DT  -  "
                        + (c.paye ? "Payée ✅" : "Non payée ⏳"));
                tvMontant.setTextColor(c.paye ? 0xFF2E7D32 : 0xFFD32F2F);
            } else {
                tvMontant.setVisibility(View.GONE);
            }
        }
        return v;
    }

    private void show(TextView tv, String label, String value) {
        if (value == null || value.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(label + value);
        }
    }
}

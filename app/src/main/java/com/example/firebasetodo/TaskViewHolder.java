package com.example.firebasetodo;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView titleEditText, descriptionEditText;
    CheckBox checkBox;
    ImageView deleteButton;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);

        titleEditText = itemView.findViewById(R.id.tv_title);
        descriptionEditText = itemView.findViewById(R.id.tv_desc);
        checkBox = itemView.findViewById(R.id.cb_done);
        deleteButton = itemView.findViewById(R.id.btn_delete);

        deleteButton.setVisibility(View.GONE);
    }
}

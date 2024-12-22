package com.example.notasfire

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class VistaNota(view: View) : RecyclerView.ViewHolder(view) {

    val titulo : TextView = view.findViewById(R.id.noteText)
    val favorito : CheckBox = view.findViewById(R.id.favoritoCheckbox)
    val fecha : TextView = view.findViewById(R.id.noteTime)


}
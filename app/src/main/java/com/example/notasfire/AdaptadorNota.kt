package com.example.notasfire

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AdaptadorNota(  val listaNotas: List<Nota>,private val updateFavoriteStatusInFirestore: (Nota) -> Unit, private val onItemClick: (Nota) -> Unit) : RecyclerView.Adapter<VistaNota>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaNota {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_itemnote, parent, false)

        return VistaNota(vista)
    }

    override fun onBindViewHolder(holder: VistaNota, position: Int) {
        val nota = listaNotas[position]
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechita = nota.fechaCreacion!!.toDate()
        var fechaFormateada = if (fechita != null) formato.format(fechita) else "Fecha no disponible"
        holder.titulo.text = nota.titulo
        holder.fecha.text = fechaFormateada
        holder.favorito.isChecked = nota.favorito


        holder.favorito.setOnCheckedChangeListener { _, isChecked ->

            nota.favorito = isChecked

            updateFavoriteStatusInFirestore(nota)


        }
        holder.itemView.setOnClickListener {
            onItemClick(nota)
        }


    }

    override fun getItemCount(): Int {
        return listaNotas.size
    }



}
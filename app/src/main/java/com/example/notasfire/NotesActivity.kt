package com.example.notasfire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotesActivity : AppCompatActivity() {

    private lateinit var botonCerrarSesion: ImageButton
    private lateinit var botonAgregar: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var listaNotas: MutableList<Nota>
    private lateinit var adapter: AdaptadorNota

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        recyclerView = findViewById(R.id.contenedorNotas)
        listaNotas = mutableListOf()
        adapter = AdaptadorNota(listaNotas, { nota -> updateFavoriteStatusInFirestore(nota) }, { nota -> onItemClick(nota) })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        botonCerrarSesion = findViewById(R.id.logoutButton)
        botonAgregar = findViewById(R.id.fabAddNote)

        botonCerrarSesion.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        botonAgregar.setOnClickListener {
            val intent = Intent(this, ActivityBuild::class.java)
            startActivity(intent)
            finish()
        }

        cargarNotas()
    }

    // Función para cargar las notas desde Firestore y ordenarlas
    fun cargarNotas() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("notes")
                .orderBy("favorito", Query.Direction.DESCENDING) // Ordenar por 'favorito', primero las favoritas
                .get()
                .addOnSuccessListener { result ->
                    // Limpiar la lista antes de agregar los nuevos datos
                    listaNotas.clear()
                    for (document in result) {
                        val nota = document.toObject(Nota::class.java)
                        nota.id = document.id // Asignar el ID de Firestore a la nota
                        listaNotas.add(nota)
                    }

                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting documents.", exception)
                }
        }
    }

    // Función para actualizar el estado del 'favorito' en Firestore y recargar la lista
    fun updateFavoriteStatusInFirestore(nota: Nota) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("notes")
                .document(nota.id!!)  // Asumiendo que cada nota tiene un 'id' único
                .update("favorito", nota.favorito)
                .addOnSuccessListener {
                    Log.d("Firestore", "Estado de favorito actualizado exitosamente")
                    cargarNotas()  // Recargar las notas después de actualizar el estado
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al actualizar el estado de favorito", e)
                }
        }
    }

    // Función para manejar el clic en una nota y abrir la actividad de edición
    fun onItemClick(nota: Nota) {
        val intent = Intent(this, ActivityBuild::class.java)
        intent.putExtra("notaId", nota.id)
        intent.putExtra("titulo", nota.titulo)
        intent.putExtra("texto", nota.texto)
        intent.putExtra("favorito", nota.favorito)
        startActivity(intent)
    }
}

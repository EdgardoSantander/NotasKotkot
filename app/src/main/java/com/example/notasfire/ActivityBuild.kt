package com.example.notasfire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityBuild : AppCompatActivity() {

    private lateinit var botonVolver: ImageButton
    private lateinit var botonGuardar: Button
    private lateinit var tituloNota: EditText
    private lateinit var textoEnLaNota: EditText
    private lateinit var botonBorrar: ImageButton
    private var notaId: String? = null // Almacenará el ID de la nota (si existe)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buildnotes)

        botonVolver = findViewById(R.id.backButton)
        botonGuardar = findViewById(R.id.saveButton)
        tituloNota = findViewById(R.id.tituloNota)
        textoEnLaNota = findViewById(R.id.textoEnLaNota)

        botonBorrar = findViewById(R.id.borrar)

        botonBorrar.setOnClickListener {
            if (notaId == null) {
                // Mostrar mensaje si la nota no tiene ID
                Toast.makeText(this, "No se puede eliminar: La nota aun no existe.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el diálogo de confirmación
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Estás seguro de que deseas eliminar esta nota?")
                .setCancelable(false) // No permitir que se cierre tocando fuera del diálogo
                .setPositiveButton("Sí") { dialog, id ->
                    // Eliminar la nota de Firestore si el usuario confirma
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId).collection("notes")
                        .document(notaId!!) // Usamos notaId!! ya que hemos validado que no es null
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Nota eliminada exitosamente", Toast.LENGTH_SHORT).show()
                            regresarAVistaPrincipal() // Volver a la vista principal tras eliminar
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al eliminar la nota: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Error al eliminar la nota", e)
                        }
                }
                .setNegativeButton("No") { dialog, id ->
                    // El usuario ha cancelado la acción, no hacer nada
                    dialog.dismiss()
                }

            // Mostrar el diálogo
            val alert = builder.create()
            alert.show()
        }








        // Obtención de datos de la nota
        notaId = intent.getStringExtra("notaId")
        val titulo = intent.getStringExtra("titulo")
        val texto = intent.getStringExtra("texto")
        val favorito = intent.getBooleanExtra("favorito", false)

        // Asignación de datos a los campos de texto (solo si es edición)
        tituloNota.setText(titulo)
        textoEnLaNota.setText(texto)

        botonVolver.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
            finish()
        }

        botonGuardar.setOnClickListener {
            val titulo = tituloNota.text.toString()
            val textoContenido = textoEnLaNota.text.toString()

            // Revisión de campos vacíos
            if (titulo.isEmpty() || textoContenido.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Autenticación y obtención del UID del usuario
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            if (notaId == null) {
                // CREAR una nueva nota
                val nuevaNota = Nota(
                    titulo = titulo,
                    texto = textoContenido,
                    fechaCreacion = Timestamp.now(),
                    favorito = false
                )

                db.collection("users").document(userId).collection("notes")
                    .add(nuevaNota)
                    .addOnSuccessListener { documentReference ->
                        nuevaNota.id = documentReference.id
                        db.collection("users").document(userId).collection("notes")
                            .document(nuevaNota.id!!)
                            .update("id", nuevaNota.id)

                        Toast.makeText(this, "Nota guardada exitosamente", Toast.LENGTH_SHORT).show()

                        regresarAVistaPrincipal()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar la nota: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error al guardar la nota", e)
                    }
            } else {
                // ACTUALIZAR una nota existente
                val notaActualizada = mapOf(
                    "titulo" to titulo,
                    "texto" to textoContenido,
                    "fechaCreacion" to Timestamp.now(),
                    "favorito" to favorito
                )

                db.collection("users").document(userId).collection("notes")
                    .document(notaId!!)
                    .update(notaActualizada)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nota actualizada exitosamente", Toast.LENGTH_SHORT).show()
                        regresarAVistaPrincipal()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar la nota: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error al actualizar la nota", e)
                    }
            }
        }
    }

    private fun regresarAVistaPrincipal() {
        val intent = Intent(this, NotesActivity::class.java)
        startActivity(intent)
        finish()
    }
}

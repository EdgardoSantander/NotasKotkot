package com.example.notasfire

import com.google.firebase.Timestamp


data class Nota(
    var id: String? = null,
    var titulo: String = "",
    var texto: String = "",
    var fechaCreacion: Timestamp? = null,
    var favorito: Boolean = false
) {
    // Constructor vacío para Firebase
    constructor() : this(null, "", "", null, false)
}


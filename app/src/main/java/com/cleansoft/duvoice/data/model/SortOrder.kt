package com.cleansoft.duvoice.data.model

enum class SortOrder(val displayName: String) {
    DATE_DESC("Mais recentes"),
    DATE_ASC("Mais antigos"),
    NAME_ASC("Nome (A-Z)"),
    NAME_DESC("Nome (Z-A)"),
    DURATION_DESC("Maior duração"),
    DURATION_ASC("Menor duração")
}


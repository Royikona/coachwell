package com.zenicore.coachwell

data class Atlit(
    var id: String = "",
    val nama: String = "",
    val tinggi: Int = 0,
    val berat: Int = 0,
    val umur: Int = 0,
    var jawaban: Map<String, Map<String, Boolean>> = emptyMap() // Update structure here
)

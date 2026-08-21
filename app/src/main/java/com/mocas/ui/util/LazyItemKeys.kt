package com.mocas.ui.util

/**
 * Compose compara las claves de todos los elementos hermanos, aunque procedan
 * de listas de entidades distintas. El prefijo evita colisiones entre tablas.
 */
internal fun lazyItemKey(type: String, id: Long): String = "$type:$id"

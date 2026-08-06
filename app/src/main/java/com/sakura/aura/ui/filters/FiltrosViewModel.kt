package com.sakura.aura.ui.filters

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.aura.domain.usecase.GetChallengesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FiltrosUiState(
    val cargando: Boolean = true,
    val puntos: Int = 0,
    val desbloqueados: Set<FiltroUi> = emptySet(),
    val seleccionado: FiltroUi? = null,
    /** Ganados justo ahora; la galería los anuncia y luego llama a [avisoVisto]. */
    val recienDesbloqueados: Set<FiltroUi> = emptySet()
)

/**
 * Conecta el sistema de recompensas con los filtros.
 *
 * Los puntos salen de los desafíos **completados** que ya expone el backend
 * (`GetChallengesUseCase`), así que no se inventa una moneda nueva ni se
 * duplica el conteo que la pantalla de Misiones Zen ya muestra.
 */
@HiltViewModel
class FiltrosViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getChallengesUseCase: GetChallengesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiltrosUiState())
    val uiState: StateFlow<FiltrosUiState> = _uiState.asStateFlow()

    init {
        // Se pinta primero lo que ya está en disco para que el carrusel no
        // aparezca vacío mientras la red responde (o si nunca responde).
        _uiState.update {
            it.copy(
                desbloqueados = FiltroStore.desbloqueados(context),
                seleccionado = FiltroStore.seleccionado(context)
            )
        }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            getChallengesUseCase.getChallenges().fold(
                onSuccess = { desafios ->
                    val puntos = FiltroStore.puntosDe(
                        desafios.filter { it.completed }.map { it.points }
                    )
                    val nuevos = FiltroStore.sincronizar(context, puntos)
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            puntos = puntos,
                            desbloqueados = FiltroStore.desbloqueados(context),
                            seleccionado = FiltroStore.seleccionado(context),
                            recienDesbloqueados = nuevos
                        )
                    }
                },
                onFailure = {
                    // Sin red se sigue con lo persistido; los puntos quedan en 0
                    // pero los filtros ya ganados siguen disponibles.
                    _uiState.update { it.copy(cargando = false) }
                }
            )
        }
    }

    fun seleccionar(filtro: FiltroUi) {
        if (filtro !in _uiState.value.desbloqueados) return
        FiltroStore.seleccionar(context, filtro)
        _uiState.update { it.copy(seleccionado = filtro) }
    }

    fun avisoVisto() {
        _uiState.update { it.copy(recienDesbloqueados = emptySet()) }
    }
}

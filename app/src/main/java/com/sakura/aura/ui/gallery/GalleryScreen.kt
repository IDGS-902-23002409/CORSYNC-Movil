package com.sakura.aura.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.filters.FiltroUi
import com.sakura.aura.ui.filters.FiltrosViewModel
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.unity.FiltroUnityActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    filtrosViewModel: FiltrosViewModel = hiltViewModel()
) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val filtrosState by filtrosViewModel.uiState.collectAsState()

    val textMain = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub  = if (isLight) Color(0xFF555555) else Color.White.copy(alpha = 0.5f)
    val cardBg   = if (isLight) Color(0xFFFFFFFF) else Color(0xFF181818).copy(alpha = 0.75f)
    val border   = if (isLight) Color(0xFFE0D8E0) else Color.White.copy(alpha = 0.08f)

    var fotos by remember { mutableStateOf<List<AuraPhoto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var seleccion by remember { mutableStateOf<Set<String>>(emptySet()) }
    var vistaPrevia by remember { mutableStateOf<AuraPhoto?>(null) }

    LaunchedEffect(Unit) {
        fotos = AuraPhotoStore.cargar(context)
        cargando = false
    }

    fun recargar() = scope.launch {
        fotos = AuraPhotoStore.cargar(context)
        seleccion = emptySet()
    }

    // La cámara de filtros es otra activity (y otro proceso): al volver de ella
    // la foto recién tomada no aparecería, porque el LaunchedEffect de arriba
    // solo corre una vez. Se recarga en cada ON_RESUME.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                scope.launch { fotos = AuraPhotoStore.cargar(context) }
                filtrosViewModel.cargar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    val enSeleccion = seleccion.isNotEmpty()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) },
        floatingActionButton = {
            if (enSeleccion) {
                ExtendedFloatingActionButton(
                    onClick = {
                        AuraPhotoStore.compartir(
                            context,
                            fotos.filter { it.uri.toString() in seleccion }.map { it.uri }
                        )
                    },
                    containerColor = Color(0xFFE91E8C),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Outlined.Share, null) },
                    text = { Text("Compartir (${seleccion.size})") }
                )
            }
        }
    ) { innerPadding ->
        SakuraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Mi Galería de Auras",
                    color = textMain,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (enSeleccion) "${seleccion.size} seleccionada(s)"
                           else "Mantén pulsada una foto para seleccionar",
                    color = textSub,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // El carrusel va DENTRO del grid, como cabecera a todo el ancho,
                // en vez de fijo encima: ocupa ~280dp y en una pantalla chica
                // dejaba la cuadrícula de fotos reducida a nada. Así hace scroll
                // junto con las fotos, que es como se comporta Instagram.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            FiltroCarousel(
                                filtros = FiltroUi.entries,
                                desbloqueados = filtrosState.desbloqueados,
                                seleccionado = filtrosState.seleccionado,
                                puntos = filtrosState.puntos,
                                textMain = textMain,
                                textSub = textSub,
                                cardBg = cardBg,
                                border = border,
                                onSeleccionar = { filtrosViewModel.seleccionar(it) },
                                onUsar = { FiltroUnityActivity.launch(context, it) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    when {
                        cargando -> item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = Color(0xFFE91E8C)) }
                        }

                        fotos.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState(
                                textMain = textMain,
                                textSub = textSub,
                                cardBg = cardBg,
                                border = border,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        else -> items(fotos, key = { it.uri.toString() }) { foto ->
                            val id = foto.uri.toString()
                            val marcada = id in seleccion
                            PhotoCell(
                                foto = foto,
                                marcada = marcada,
                                onClick = {
                                    if (enSeleccion) {
                                        seleccion = if (marcada) seleccion - id else seleccion + id
                                    } else {
                                        vistaPrevia = foto
                                    }
                                },
                                onLongClick = {
                                    seleccion = if (marcada) seleccion - id else seleccion + id
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (filtrosState.recienDesbloqueados.isNotEmpty()) {
        val ganados = filtrosState.recienDesbloqueados
        AlertDialog(
            onDismissRequest = { filtrosViewModel.avisoVisto() },
            containerColor = Color(0xFF141414),
            title = {
                Text(
                    if (ganados.size == 1) "¡Filtro desbloqueado!"
                    else "¡${ganados.size} filtros desbloqueados!",
                    color = Color.White
                )
            },
            text = {
                Text(
                    ganados.joinToString("\n") { "✨ ${it.label} — ${it.descripcion}" },
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { filtrosViewModel.avisoVisto() }) {
                    Text("Probarlo", color = Color(0xFFE91E8C))
                }
            }
        )
    }

    vistaPrevia?.let { foto ->
        PhotoDialog(
            foto = foto,
            onCerrar = { vistaPrevia = null },
            onCompartir = { AuraPhotoStore.compartir(context, listOf(foto.uri)) },
            onBorrar = {
                scope.launch {
                    if (AuraPhotoStore.borrar(context, foto.uri)) {
                        vistaPrevia = null
                        recargar()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoCell(
    foto: AuraPhoto,
    marcada: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A1A))
            .then(
                if (marcada) Modifier.border(3.dp, Color(0xFFE91E8C), RoundedCornerShape(14.dp))
                else Modifier
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        AsyncImage(
            model = foto.uri,
            contentDescription = foto.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (marcada) {
            Box(
                Modifier.fillMaxSize().background(Color(0xFFE91E8C).copy(alpha = 0.25f))
            )
        }
        Text(
            text = SimpleDateFormat("d MMM · HH:mm", Locale("es"))
                .format(Date(foto.takenAtMillis)),
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun PhotoDialog(
    foto: AuraPhoto,
    onCerrar: () -> Unit,
    onCompartir: () -> Unit,
    onBorrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Color(0xFF141414),
        title = null,
        text = {
            AsyncImage(
                model = foto.uri,
                contentDescription = foto.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        },
        confirmButton = {
            TextButton(onClick = onCompartir) {
                Icon(Icons.Outlined.Share, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Compartir", color = Color(0xFFE91E8C))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onBorrar) {
                    Icon(
                        Icons.Outlined.Delete, null,
                        Modifier.size(18.dp), tint = Color(0xFFE74C3C)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Borrar", color = Color(0xFFE74C3C))
                }
                TextButton(onClick = onCerrar) {
                    Icon(Icons.Outlined.Close, null, Modifier.size(18.dp), tint = Color.White)
                }
            }
        }
    )
}

@Composable
private fun EmptyState(
    textMain: Color,
    textSub: Color,
    cardBg: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.PhotoCamera, null,
                Modifier.size(48.dp),
                tint = textSub
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Aún no hay fotos",
                color = textMain,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escanea tu aura y ábrela en 3D, o elige un filtro aquí arriba. " +
                    "Pulsa el botón de cámara para guardar tu primera captura.",
                color = textSub,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

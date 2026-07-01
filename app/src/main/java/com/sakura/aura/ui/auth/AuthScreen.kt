package com.sakura.aura.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.SakuraPink

private enum class AuthTab { LOGIN, REGISTER }

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit = {}) {

    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    SakuraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            SakuraLogo()
            Spacer(Modifier.height(16.dp))

            Text(
                "CorSync",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White, fontSize = 36.sp
            )
            Spacer(Modifier.height(4.dp))

            Text(
                "Encuentra la paz en tu energía interior",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))

            AuthTabSelector(selected = selectedTab, onSelect = { selectedTab = it })
            Spacer(Modifier.height(28.dp))

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = Color(0xFFE74C3C),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            when (selectedTab) {
                AuthTab.LOGIN -> LoginForm(
                    isLoading = uiState is AuthUiState.Loading,
                    onLogin   = { user, pass -> viewModel.login(user, pass) }
                )
                AuthTab.REGISTER -> RegisterForm(
                    isLoading  = uiState is AuthUiState.Loading,
                    onRegister = { user, email, pass, nombre ->
                        viewModel.register(user, email, pass, nombre)
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                "Respira hondo. Tu aura te espera bajo el cerezo.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

// ── Logo ──────────────────────────────────────────────────────────────────────
@Composable
private fun SakuraLogo() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
    ) {
        Text("✦", fontSize = 32.sp, color = SakuraPink)
    }
}

// ── Tab Selector ──────────────────────────────────────────────────────────────
@Composable
private fun AuthTabSelector(selected: AuthTab, onSelect: (AuthTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF1A1A1A)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AuthTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) Color(0xFF2E2E2E) else Color.Transparent,
                animationSpec = tween(250), label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                animationSpec = tween(250), label = "tabText"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text       = if (tab == AuthTab.LOGIN) "Iniciar Sesión" else "Registrarse",
                    color      = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize   = 14.sp
                )
            }
        }
    }
}

// ── Login Form ────────────────────────────────────────────────────────────────
@Composable
private fun LoginForm(isLoading: Boolean, onLogin: (String, String) -> Unit) {
    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SakuraTextField(
            value         = username,
            onValueChange = { username = it },
            placeholder   = "Usuario",
            icon          = Icons.Outlined.Person
        )

        // ── Campo contraseña con ojo ───────────────────────────────────────
        SakuraTextField(
            value           = password,
            onValueChange   = { password = it },
            placeholder     = "Contraseña",
            icon            = Icons.Outlined.Lock,
            isPassword      = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                "Olvidé mi contraseña",
                color    = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(Modifier.height(8.dp))

        SakuraButton(
            text    = if (isLoading) "Iniciando..." else "Iniciar Sesión",
            onClick = { if (!isLoading) onLogin(username, password) }
        )
    }
}

// ── Register Form ─────────────────────────────────────────────────────────────
@Composable
private fun RegisterForm(
    isLoading  : Boolean,
    onRegister : (String, String, String, String) -> Unit
) {
    var nombre          by remember { mutableStateOf("") }
    var username        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SakuraTextField(
            value         = nombre,
            onValueChange = { nombre = it },
            placeholder   = "Nombre completo",
            icon          = Icons.Outlined.Person
        )
        SakuraTextField(
            value         = username,
            onValueChange = { username = it },
            placeholder   = "Nombre de usuario",
            icon          = Icons.Outlined.Person
        )
        SakuraTextField(
            value         = email,
            onValueChange = { email = it },
            placeholder   = "Correo",
            icon          = Icons.Outlined.Email,
            keyboardType  = KeyboardType.Email
        )

        // ── Campo contraseña con ojo ───────────────────────────────────────
        SakuraTextField(
            value            = password,
            onValueChange    = { password = it },
            placeholder      = "Contraseña",
            icon             = Icons.Outlined.Lock,
            isPassword       = true,
            passwordVisible  = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(Modifier.height(8.dp))

        SakuraButton(
            text    = if (isLoading) "Registrando..." else "Comenzar el viaje",
            onClick = { if (!isLoading) onRegister(username, email, password, nombre) }
        )
    }
}

// ── TextField con soporte de ojo de contraseña ────────────────────────────────
@Composable
private fun SakuraTextField(
    value            : String,
    onValueChange    : (String) -> Unit,
    placeholder      : String,
    icon             : ImageVector,
    isPassword       : Boolean      = false,
    passwordVisible  : Boolean      = false,
    onTogglePassword : (() -> Unit)? = null,
    keyboardType     : KeyboardType  = KeyboardType.Text
) {
    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else                           -> VisualTransformation.None
    }

    TextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = {
            Text(placeholder, color = Color.White.copy(alpha = 0.35f), fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = Color.White.copy(alpha = 0.45f),
                modifier           = Modifier.size(18.dp)
            )
        },
        // ── Ícono del ojo solo en campos de contraseña ─────────────────────
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Outlined.VisibilityOff
                        else
                            Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña",
                        tint     = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = visualTransformation,
        keyboardOptions      = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
        ),
        singleLine = true,
        shape      = RoundedCornerShape(50.dp),
        colors     = TextFieldDefaults.colors(
            focusedContainerColor   = Color(0xFF1E1E1E),
            unfocusedContainerColor = Color(0xFF1A1A1A),
            focusedTextColor        = Color.White,
            unfocusedTextColor      = Color.White.copy(alpha = 0.85f),
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor             = SakuraPink,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    )
}

// ── Botón principal ───────────────────────────────────────────────────────────
@Composable
private fun SakuraButton(text: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape    = RoundedCornerShape(50.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor   = Color(0xFF1A1A1A)
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}
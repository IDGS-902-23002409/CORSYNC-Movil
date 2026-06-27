package com.sakura.aura.ui.theme.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import com.sakura.aura.ui.theme.components.SakuraBackground
import com.sakura.aura.ui.theme.SakuraPink

// ── Tabs ──────────────────────────────────────────────────────────────────────
private enum class AuthTab { LOGIN, REGISTER }

// ── Pantalla raíz Auth ────────────────────────────────────────────────────────
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    SakuraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            SakuraLogo()

            Spacer(modifier = Modifier.height(16.dp))

            // ── Título ────────────────────────────────────────────────────────
            Text(
                text = "Sakura",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Encuentra la paz en tu energía interior",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Selector de tabs ──────────────────────────────────────────────
            AuthTabSelector(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Formulario dinámico ───────────────────────────────────────────
            when (selectedTab) {
                AuthTab.LOGIN    -> LoginForm(onLoginSuccess = onLoginSuccess)
                AuthTab.REGISTER -> RegisterForm(onRegisterSuccess = onLoginSuccess)
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Mensaje inferior ──────────────────────────────────────────────
            Text(
                text = "Respira hondo. Tu aura te espera bajo el cerezo.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

// ── Logo circular con estrella ─────────────────────────────────────────────────
@Composable
private fun SakuraLogo() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
    ) {
        Text(text = "✦", fontSize = 32.sp, color = SakuraPink)
    }
}

// ── Tab Selector ───────────────────────────────────────────────────────────────
@Composable
private fun AuthTabSelector(
    selected: AuthTab,
    onSelect: (AuthTab) -> Unit
) {
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
                targetValue = if (isSelected) Color(0xFF2E2E2E) else Color.Transparent,
                animationSpec = tween(250),
                label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                animationSpec = tween(250),
                label = "tabText"
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
                    text = if (tab == AuthTab.LOGIN) "Iniciar Sesión" else "Registrarse",
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ── Formulario Login ───────────────────────────────────────────────────────────
@Composable
private fun LoginForm(onLoginSuccess: () -> Unit) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SakuraTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo",
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )

        SakuraTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )

        // Olvidé mi contraseña
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Olvidé mi contraseña",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                modifier = Modifier.clickable { /* TODO: recuperar */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SakuraButton(
            text = "Iniciar Sesión",
            onClick = onLoginSuccess
        )
    }
}

// ── Formulario Register ────────────────────────────────────────────────────────
@Composable
private fun RegisterForm(onRegisterSuccess: () -> Unit) {
    var spiritualName by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SakuraTextField(
            value = spiritualName,
            onValueChange = { spiritualName = it },
            placeholder = "Nombre espiritual",
            icon = Icons.Outlined.Email
        )

        SakuraTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo",
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )

        SakuraTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña",
            icon = Icons.Outlined.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        SakuraButton(
            text = "Comenzar el viaje",
            onClick = onRegisterSuccess
        )
    }
}

// ── Campo de texto reutilizable ────────────────────────────────────────────────
@Composable
private fun SakuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = TextFieldDefaults.colors(
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

// ── Botón principal ────────────────────────────────────────────────────────────
@Composable
private fun SakuraButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor   = Color(0xFF1A1A1A)
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}


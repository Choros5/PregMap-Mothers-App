package com.elvis.pregmap.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.elvis.pregmap.R

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                label, 
                color = Color(0xFF424242)
            ) 
        },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = { 
            if (isError) Text(
                errorText ?: "", 
                color = Color(0xFFD32F2F)
            ) 
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontSize = 16.sp
        ),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedBorderColor = Color(0xFF1976D2),
            unfocusedBorderColor = Color(0xFFBDBDBD),
            focusedLabelColor = Color(0xFF1976D2),
            unfocusedLabelColor = Color(0xFF757575),
            cursorColor = Color(0xFF1976D2),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun PhoneNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                label, 
                color = Color(0xFF424242)
            ) 
        },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = { 
            if (isError) Text(
                errorText ?: "", 
                color = Color(0xFFD32F2F)
            ) 
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontSize = 16.sp
        ),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedBorderColor = Color(0xFF1976D2),
            unfocusedBorderColor = Color(0xFFBDBDBD),
            focusedLabelColor = Color(0xFF1976D2),
            unfocusedLabelColor = Color(0xFF757575),
            cursorColor = Color(0xFF1976D2),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        leadingIcon = {
            Text(
                text = "+254",
                color = Color(0xFF1976D2),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                label, 
                color = Color(0xFF424242)
            ) 
        },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = { 
            if (isError) Text(
                errorText ?: "", 
                color = Color(0xFFD32F2F)
            ) 
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontSize = 16.sp
        ),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedBorderColor = Color(0xFF1976D2),
            unfocusedBorderColor = Color(0xFFBDBDBD),
            focusedLabelColor = Color(0xFF1976D2),
            unfocusedLabelColor = Color(0xFF757575),
            cursorColor = Color(0xFF1976D2),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        trailingIcon = {
            val image = if (passwordVisible) {
                painterResource(id = R.drawable.ic_visibility_off)
            } else {
                painterResource(id = R.drawable.ic_visibility)
            }
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = image, 
                    contentDescription = description,
                    tint = Color(0xFF757575)
                )
            }
        }
    )
} 
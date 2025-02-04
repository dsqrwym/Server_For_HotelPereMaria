package com.hotelperemaria.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@Composable
fun ConfirmationDialog(
    // 对话框显示状态
    showDialog: Boolean,
    // 对话框标题（可选）
    title: String? = null,
    // 消息内容（支持多行）
    message: String,
    // 图标（可选）
    icon: ImageVector? = null,
    // 确认按钮文本（默认"确认"）
    confirmText: String = stringResource(R.string.dialog_confirm),
    // 取消按钮文本（默认"取消"）
    cancelText: String = stringResource(R.string.dialog_cancel),
    // 确认按钮颜色（可选）
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    // 取消按钮颜色（可选）
    cancelButtonColor: Color = MaterialTheme.colorScheme.onSurface,
    // 对话框关闭回调
    onDismiss: () -> Unit,
    // 确认操作回调
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            // 自定义图标和内容布局
            icon = {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            title = {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            // 自定义按钮布局
            confirmButton = {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = cancelButtonColor
                        )
                    ) {
                        Text(cancelText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = confirmButtonColor
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        )
    }
}
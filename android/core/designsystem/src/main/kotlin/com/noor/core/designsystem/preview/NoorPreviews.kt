package com.noor.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light LTR",
    group = "Theme",
    showBackground = true,
)
@Preview(
    name = "Dark LTR",
    group = "Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Light RTL",
    group = "Theme",
    showBackground = true,
    locale = "ar",
)
@Preview(
    name = "Dark RTL",
    group = "Theme",
    showBackground = true,
    locale = "ar",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class NoorThemePreviews

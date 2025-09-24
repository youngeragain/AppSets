package xcj.app.appsets.ui.compose.apps.tools

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.server.model.WeatherInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import java.util.Calendar

@Composable
fun ToolWeatherPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit,
) {
    HideNavBar()
    val configuration = LocalConfiguration.current
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.weather)
        )
        Box(Modifier.weight(1f)) {
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LandscapeWeatherComponent()
            } else {
                PortraitWeatherComponent()
            }
        }
    }

}

@Composable
fun PortraitWeatherComponent() {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        var locationName by remember {
            mutableStateOf("")
        }

        var longitude by remember {
            mutableStateOf("")
        }
        var latitude by remember {
            mutableStateOf("")
        }

        var weatherInfo: WeatherInfo? by remember {
            mutableStateOf<WeatherInfo?>(null)
        }

        val coroutineScope = rememberCoroutineScope()

        val scrollState = rememberScrollState()

        var positionNowWeatherTemperatureInfo by remember {
            mutableIntStateOf(-1)
        }

        val systemUseCase = LocalUseCaseOfSystem.current

        LaunchedEffect(key1 = true) {
            weatherInfo = systemUseCase.getWeatherInfo(locationName, longitude, latitude)
            coroutineScope.launch {
                val nowHourTime = Calendar.getInstance().time.time
                val previousHourTime = nowHourTime - 3600000
                val temperatureInfos = weatherInfo?.temperatureInfo ?: return@launch
                for ((index, temperatureInfo) in temperatureInfos.withIndex()) {
                    if (temperatureInfo.time in previousHourTime..nowHourTime) {
                        positionNowWeatherTemperatureInfo = index
                        break
                    }
                }
                if (positionNowWeatherTemperatureInfo != -1) {
                    delay(100)
                    scrollState.animateScrollTo(positionNowWeatherTemperatureInfo)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val temperatureInfoList = weatherInfo?.temperatureInfo
            if (temperatureInfoList != null) {
                val nowHourTime = Calendar.getInstance().time.time
                val previousHourTime = nowHourTime - 3600000
                temperatureInfoList.forEachIndexed { index, temperatureInfo ->
                    if (temperatureInfo.time > previousHourTime && temperatureInfo.time < nowHourTime) {
                        val weatherTemperature = String.format("%s ℃", temperatureInfo.temperature)
                        val fontSize = 72.sp
                        Text(
                            text = weatherTemperature,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var showInput by remember {
                mutableStateOf(false)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .animateContentSize(
                        animationSpec = tween()
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showInput) {
                    DesignTextField(
                        value = locationName,
                        onValueChange = {
                            locationName = it
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = xcj.app.appsets.R.string.location_name),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    DesignTextField(
                        value = longitude,
                        onValueChange = {
                            longitude = it
                        },
                        placeholder = {
                            Text(text = stringResource(id = xcj.app.appsets.R.string.longitude))
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    )
                    DesignTextField(
                        value = latitude,
                        onValueChange = {
                            latitude = it
                        },
                        placeholder = {
                            Text(text = stringResource(id = xcj.app.appsets.R.string.latitude))
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            showInput = false
                        }
                        .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_location_on_24),
                        contentDescription = "Private key file"
                    )
                    Text(text = stringResource(id = xcj.app.appsets.R.string.auto))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            showInput = true
                        }
                        .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_stylus_24),
                        contentDescription = "Public key file"
                    )
                    Text(text = stringResource(xcj.app.appsets.R.string.input))
                }
            }
        }
    }
}

@Composable
fun LandscapeWeatherComponent() {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var locationName by remember {
            mutableStateOf("")
        }

        var longitude by remember {
            mutableStateOf("")
        }
        var latitude by remember {
            mutableStateOf("")
        }

        var weatherInfo: WeatherInfo? by remember {
            mutableStateOf<WeatherInfo?>(null)
        }

        val coroutineScope = rememberCoroutineScope()

        val scrollState = rememberScrollState()

        var positionNowWeatherTemperatureInfo by remember {
            mutableIntStateOf(-1)
        }
        val systemUseCase = LocalUseCaseOfSystem.current
        LaunchedEffect(key1 = true) {
            weatherInfo = systemUseCase.getWeatherInfo(locationName, longitude, latitude)
            coroutineScope.launch {
                val nowHourTime = Calendar.getInstance().time.time
                val previousHourTime = nowHourTime - 3600000
                val temperatureInfos = weatherInfo?.temperatureInfo ?: return@launch
                for ((index, temperatureInfo) in temperatureInfos.withIndex()) {
                    if (temperatureInfo.time in previousHourTime..nowHourTime) {
                        positionNowWeatherTemperatureInfo = index
                        break
                    }
                }
                if (positionNowWeatherTemperatureInfo != -1) {
                    delay(100)
                    scrollState.animateScrollTo(positionNowWeatherTemperatureInfo)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val temperatureInfoList = weatherInfo?.temperatureInfo
            if (temperatureInfoList != null) {
                val nowHourTime = Calendar.getInstance().time.time
                val previousHourTime = nowHourTime - 3600000
                temperatureInfoList.forEachIndexed { index, temperatureInfo ->
                    if (temperatureInfo.time > previousHourTime && temperatureInfo.time < nowHourTime) {
                        val weatherTemperature = String.format("%s ℃", temperatureInfo.temperature)
                        val fontSize = 72.sp
                        Text(
                            text = weatherTemperature,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var showInput by remember {
                    mutableStateOf(false)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .animateContentSize(
                            animationSpec = tween()
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showInput) {
                        DesignTextField(
                            value = locationName,
                            onValueChange = {
                                locationName = it
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = xcj.app.appsets.R.string.location_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        DesignTextField(
                            value = longitude,
                            onValueChange = {
                                longitude = it
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.longitude))
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        )
                        DesignTextField(
                            value = locationName,
                            onValueChange = {
                                latitude = it
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.latitude))
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.extraLarge
                            )
                            .clickable {
                                showInput = false
                            }
                            .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_location_on_24),
                            contentDescription = "Private key file"
                        )
                        Text(text = stringResource(id = xcj.app.appsets.R.string.auto))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.extraLarge
                            )
                            .clickable {
                                showInput = true
                            }
                            .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_stylus_24),
                            contentDescription = "Public key file"
                        )
                        Text(text = stringResource(xcj.app.appsets.R.string.input))
                    }
                }
            }
        }
    }
}

/*
 * Copyright (c) 2024. Desarrollo de Soluciones Moviles, DISC.
 */

package cl.ucn.disc.dsm.pictwin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ucn.disc.dsm.pictwin.data.model.Persona
import cl.ucn.disc.dsm.pictwin.data.model.PicTwin
import cl.ucn.disc.dsm.pictwin.data.services.Service
import cl.ucn.disc.dsm.pictwin.ui.theme.PicTwinTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Activity: PicTwin
 */
class PicTwinActivity : ComponentActivity() {

    /**
     * onCreate: build the UI
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // call the super !
        super.onCreate(savedInstanceState)

        // setContent: build the UI
        setContent {
            PicTwinTheme {
                PicTwinScaffold()
            }
        }
    }
}

/**
 * PicTwinScaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTwinScaffold() {

    // the scroll behavior
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    // the scaffold
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PicTwinTopBar(
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FabActionButton()
        }
    ) { innerPadding ->
        PicTwinList(
            innerPadding = innerPadding,
        )
    }
}

/**
 * The PicTwin pair.
 */
data class PicturePair(
    val leftImage: Painter,
    val rightImage: Painter
)

/**
 * The List of PicTwins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTwinList(
    innerPadding: PaddingValues,
    vm: PicTwinListViewModel = hiltViewModel()
) {

    // the state of the view model
    val state = vm.state.collectAsState().value

    // Pull to refresh
    PullToRefreshBox(
        isRefreshing = state is PicTwinListViewModel.State.Loading,
        onRefresh = { vm.refresh() },
        indicator = {
            // Empty Box with zero size to hide the indicator
            Box(modifier = Modifier.size(0.dp)) {}
        }
    ) {
        // Render the state
        when (state) {

            // initial or loading
            is PicTwinListViewModel.State.Initial, PicTwinListViewModel.State.Loading -> {
                LoadingBox()
            }

            // error
            is PicTwinListViewModel.State.Error -> {
                ErrorBox(
                    error = state,
                    onAction = { vm.refresh() }
                )
            }

            // success
            is PicTwinListViewModel.State.Success -> {
                PicTwinBox(
                    innerPadding = innerPadding,
                    pictwins = state.persona.picTwins,
                )
            }
        }
    }
}

/**
 * PicTwinBox.
 */
@Composable
fun PicTwinBox(
    innerPadding: PaddingValues,
    pictwins: List<PicTwin>,
){
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding(),
            bottom = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pictwins) { twin ->
            PicTwinRow(
                twin = PicturePair(
                    leftImage = loadImagePainter(twin.twin.photo),
                    rightImage = loadImagePainter(twin.pic.photo)
                )
            )
        }

        if (pictwins.isEmpty()) {
            item {
                Text(
                    text = "No twins available",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Create a Painter from the image in base64.
 */
@Composable
fun loadImagePainter(imageBase64: String): Painter {
    // Decode the Base64 string to a ByteArray
    val imageByteArray = try {
        Base64.decode(imageBase64, Base64.DEFAULT)
    } catch (e: IllegalArgumentException) {
        // Handle invalid Base64 string
        null
    }

    // Convert ByteArray to Bitmap
    val bitmap = imageByteArray?.let { byteArray ->
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    // Create a Painter from the Bitmap
    return bitmap?.let {
        BitmapPainter(image = bitmap.asImageBitmap())
    } ?: painterResource(id = R.drawable.image_portada)
}

/**
 * LoadingBox.
 */
@Composable
fun LoadingBox(
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cargando PicTwins...",
            )
        }
    }
}

/**
 * ErrorBox.
 */
@Composable
fun ErrorBox(
    error: PicTwinListViewModel.State.Error,
    onAction: () -> Unit = { },
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onAction() }) {
                Text("Reintentar")
            }
        }
    }
}

/**
 * PicTwinRow.
 */
@Composable
fun PicTwinRow(twin: PicturePair) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = twin.leftImage,
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = twin.rightImage,
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * PicTwinBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTwinTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = { Text(text = "PicTwin Application") },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

/**
 * FabActionButton.
 */
@Composable
fun FabActionButton(
    onAction: () -> Unit = { },
) {
    FloatingActionButton(
        onClick = {
        },
        containerColor = MaterialTheme.colorScheme.tertiary,
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

/**
 * The ViewModel.
 */
@HiltViewModel
class PicTwinListViewModel @Inject constructor(
    private val service: Service,
) : ViewModel() {

    // the logger
    private val _log = LoggerFactory.getLogger(PicTwinListViewModel::class.java)

    // internal state
    private val _state = MutableStateFlow<State>(State.Initial)

    // public state (as flow)
    val state = _state.asStateFlow()

    // init
    init {
        _log.debug("Fetching Persona...")
        viewModelScope.launch {
            refreshPersona()
        }
    }

    /**
     * Refresh the Persona in the background.
     */
    fun refresh() {
        viewModelScope.launch {
            refreshPersona()
        }
    }

    /**
     * Retrieve the Persona
     */
    private suspend fun refreshPersona() {

        _state.value = State.Loading

        service.retireve()
            .onSuccess { persona ->
                _state.value = State.Success(persona)
                _log.debug("Persona fetched: {}", persona)
            }
            .onFailure { error ->
                _state.value = State.Error(error.message ?: "Unknow error")
                _log.error("Error fetching Persona: {}", error.message)
            }
    }

    /**
     * The State of the view.
     */
    sealed class State {
        object Initial : State()
        object Loading : State()
        data class Success(val persona: Persona) : State()
        data class Error(val message: String) : State()
    }
}

/**
 * Preview.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PicTwinScaffoldPreview() {
    PicTwinTheme {
        PicTwinScaffold()
    }
}
package `in`.mcxiv.geosensitivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.mcxiv.geosensitivity.ui.theme.GeoSensitivityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoSensitivityTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val location = getLocation(context = context)
    val savepoints = remember { mutableListOf<Coordinates>() }
    var threshold by remember { mutableStateOf(10f) }

    var animation by remember { mutableStateOf(0.0) }
    LaunchedEffect(key1 = animation) { animation += 0.01 }

    Column(
        modifier = modifier.padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your current location is")
        Text(text = "$location")
        Text(text = "Threshold = $threshold")
        Slider(value = threshold, valueRange = 1f..20f, onValueChange = { threshold = it })
        Button(onClick = { savepoints.add(location.copy()) }) { Text(text = "Save point") }
        Text(text = "${savepoints.size} saved positions")
        LazyColumn {
            items(savepoints) {
                Card(modifier = modifier.padding(0.dp, 5.dp)) {
                    Column(modifier = modifier.padding(10.dp)) {
                        Text(text = "Location $it")
                        Text(text = "Distance ${location * it}")
                        Text(text = "Is it near? ${location * it < threshold}")
                    }
                }
            }
        }
    }
}
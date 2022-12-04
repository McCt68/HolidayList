package eu.example.holidaylist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.example.holidaylist.ui.theme.HolidayListTheme


// TODO
// If i add a new holiday, when the list is sorted the list don't update until i click the sort button in topbar
// It needs to recompose after i add it
// implement firestore

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			HolidayListTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					// when something in the list holidays change it will be recomposed
					val holidays = remember { mutableListOf<Holiday>() }
					val sort = remember { mutableStateOf(false) }
					// If user makes sort true ( clicks it) sort by first letter in the name -
					// else if sort is false, then don't sort it
					val sortedHolidays =
						if (sort.value) holidays.sortedBy { it.name[0] } else holidays

					HolidayList(
						list = sortedHolidays,
						onAddHoliday = { name, date ->
							holidays.add(Holiday(name, date))
						},
						onSort = {
							sort.value = !sort.value // toogle sort between true - false
						})
				}
			}
		}
	}
}

data class Holiday(
	val name: String,
	val date: String
)

// takes a list of Holiday objects
// takes a lambda function with name and date, and return Unit -
// Note the lambda is the last parameter, so it can be left outside the parentheses -
// When we call this function
@Composable
fun HolidayList(
	list: List<Holiday>,
	onAddHoliday: (name: String, date: String) -> Unit,
	onSort: () -> Unit
) {
	val showDialog = remember { mutableStateOf(false) }

	Scaffold(
		floatingActionButton = {
			AddHolidayFab {
				showDialog.value = true
			}
		},
		topBar = { AddTopBar(onSort) }
	) {
		// put the list of Holiday objects into a LazyColumn, if the list is not empty
		if (list.isEmpty())
			Column(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Text(text = "No holidays available")
			}
		else
			LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
				items(list) {
					Column(
						modifier = Modifier
							.padding(5.dp) // Outside
							.clip(RoundedCornerShape(10.dp))
							.background(Color(0xFFeeeeee))
							.padding(10.dp) // Inside
							.fillMaxWidth()
					) {
						Text(text = it.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
						Text(text = it.date, fontSize = 20.sp, fontWeight = FontWeight.Bold)
					}
				}
			}
	}

	if (showDialog.value) {
		AddHolidayDialog(
			onDismiss = { showDialog.value = false },
			onConfirm = { name, date ->
				showDialog.value = false
				onAddHoliday.invoke(name, date)
			}
		)
	}
}

@Composable
fun AddTopBar(onSort: () -> Unit) {
	val icon = painterResource(id = R.drawable.ic_sort24)
	TopAppBar(title = { Text(text = "Visited countrys") }, actions = {
		IconButton(onClick = onSort) {
			Icon(painter = icon, contentDescription = null)
		}
	})
}


// Hoisted the state to the caller, its a lambda, and the last parameter, so it can be outside the calling -
// functions brackets
@Composable
fun AddHolidayFab(onFabClick: () -> Unit) {
	FloatingActionButton(onClick = onFabClick) {
		Icon(Icons.Default.Add, contentDescription = null)
	}

}

//
@Composable
fun AddHolidayDialog(
	onDismiss: () -> Unit,
	onConfirm: (name: String, date: String) -> Unit
) {
	val holidayName = remember { mutableStateOf(TextFieldValue("")) }
	val holidayDate = remember { mutableStateOf(TextFieldValue("")) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = "Add a holiday",
				modifier = Modifier.padding(5.dp),
				fontWeight = FontWeight.Bold
			)
		},
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				TextField(
					value = holidayName.value,
					onValueChange = { holidayName.value = it },
					label = { Text(text = "Holiday name") },
					placeholder = { Text(text = "Barcelona") },
					modifier = Modifier.padding(5.dp)
				)
				TextField(
					value = holidayDate.value,
					onValueChange = { holidayDate.value = it },
					label = { Text(text = "Holiday date") },
					placeholder = { Text(text = "041222") },
					modifier = Modifier.padding(5.dp)
				)
			}
		},
		confirmButton = {
			Button(onClick = {
				val n = holidayName.value.text
				val d = holidayDate.value.text
				if (n.isNotEmpty() && d.isNotEmpty())
					onConfirm.invoke(n, d)
			}) {
				Text(text = "Add Holiday")
			}
		},
		dismissButton = {
			Button(onClick = onDismiss) {
				Text(text = "Cancel")
			}
		}
	)
}


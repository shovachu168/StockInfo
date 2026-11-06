package com.cathysec.stock.info.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.cathysec.stock.info.R
import com.cathysec.stock.info.model.SortOrder
import kotlinx.coroutines.launch

/**
 * Bottom sheet to choose the sort direction of the stock list (sorted by stock code).
 *
 * @param selected the sort order that is currently applied
 * @param onSelect called after the sheet has slid away, with the order the user picked
 * @param onDismiss called when the user closes the sheet without picking (swipe down or tap outside)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    selected: SortOrder,
    onSelect: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        SortBottomSheetContent(
            selected = selected,
            onSelect = { order ->
                // Let the sheet slide away first, then report the choice.
                scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(order) }
            },
        )
    }
}

@Composable
private fun SortBottomSheetContent(
    selected: SortOrder,
    onSelect: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = dimensionResource(R.dimen.sort_sheet_horizontal_padding)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.sort_sheet_bottom_padding)),
    ) {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = dimensionResource(R.dimen.sort_sheet_title_padding),
            ),
        )
        Column(modifier = Modifier.selectableGroup()) {
            SortOption(
                label = stringResource(R.string.sort_code_descending),
                selected = selected == SortOrder.DESCENDING,
                onClick = { onSelect(SortOrder.DESCENDING) },
            )
            SortOption(
                label = stringResource(R.string.sort_code_ascending),
                selected = selected == SortOrder.ASCENDING,
                onClick = { onSelect(SortOrder.ASCENDING) },
            )
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(R.dimen.sort_option_min_height))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = dimensionResource(R.dimen.sort_sheet_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The whole row is clickable, so the radio button itself does not handle clicks.
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.sort_option_label_gap)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SortBottomSheetContentPreview() {
    StockTheme {
        SortBottomSheetContent(selected = SortOrder.DESCENDING, onSelect = {})
    }
}
